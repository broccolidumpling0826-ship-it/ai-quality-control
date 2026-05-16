package com.jhict.quality.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhict.quality.entity.QcConcessionAcceptance;
import com.jhict.quality.mapper.QcConcessionAcceptanceMapper;
import com.jhict.quality.service.api.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 让步接收相关定时任务
 */
@Slf4j
@Component
public class ConcessionScheduler {

    private static final String LOCK_KEY = "lock:scheduler:concession-expiry";
    private static final long LOCK_TTL_SECONDS = 60L;
    private static final int REMINDER_PENDING_DAYS = 3;

    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private QcConcessionAcceptanceMapper concessionMapper;

    @Resource
    private NotificationService notificationService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 每天凌晨1点执行：
     * 1. 处理到期让步，将状态置为 INVALID
     * 2. 催促待确认让步（超过3天未确认）
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void handleConcessionExpiry() {
        // 分布式锁，防止多实例重复执行
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(LOCK_KEY, "1", LOCK_TTL_SECONDS, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)) {
            log.info("[ConcessionScheduler] 未获得分布式锁，跳过本次执行");
            return;
        }

        try {
            log.info("[ConcessionScheduler] 开始执行让步到期处理任务");
            processExpiredConcessions();
            processPendingReminders();
            log.info("[ConcessionScheduler] 让步定时任务执行完成");
        } catch (Exception e) {
            log.error("[ConcessionScheduler] 任务执行异常", e);
        }
    }

    /**
     * 处理已到期的让步：expiryDate < today AND approvalStatus='APPROVED' → INVALID
     */
    private void processExpiredConcessions() {
        LocalDate today = LocalDate.now();
        String todayStr = today.toString(); // yyyy-MM-dd

        List<QcConcessionAcceptance> expired = concessionMapper.selectList(
                new LambdaQueryWrapper<QcConcessionAcceptance>()
                        .lt(QcConcessionAcceptance::getExpiryDate, today)
                        .eq(QcConcessionAcceptance::getApprovalStatus, "APPROVED")
        );

        if (expired.isEmpty()) {
            log.info("[ConcessionScheduler] 没有需要处理的到期让步记录");
            return;
        }

        int count = 0;
        for (QcConcessionAcceptance concession : expired) {
            concession.setApprovalStatus("INVALID");
            concession.setVoidReason("让步有效期已过（到期日：" + concession.getExpiryDate() + "）");
            concessionMapper.updateById(concession);

            // 通知让步创建人
            String creatorNo = concession.getCreateUserNo();
            if (StringUtils.hasText(creatorNo)) {
                notificationService.send(
                        creatorNo,
                        "让步接收到期通知",
                        "让步接收申请（ID：" + concession.getId() + "）已于 " + concession.getExpiryDate()
                                + " 到期，状态已自动更新为无效",
                        "CONCESSION",
                        concession.getId()
                );
            }
            count++;
        }
        log.info("[ConcessionScheduler] 共处理 {} 条到期让步记录", count);
    }

    /**
     * 处理待确认超时催确认：confirmStatus='PENDING' AND createDateTime < 3天前 → 发催确认通知
     */
    private void processPendingReminders() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(REMINDER_PENDING_DAYS);
        String thresholdStr = threshold.format(DT_FORMATTER);

        List<QcConcessionAcceptance> pendingList = concessionMapper.selectList(
                new LambdaQueryWrapper<QcConcessionAcceptance>()
                        .eq(QcConcessionAcceptance::getConfirmStatus, "PENDING")
                        .lt(QcConcessionAcceptance::getCreateDateTime, thresholdStr)
        );

        if (pendingList.isEmpty()) {
            log.info("[ConcessionScheduler] 没有需要催确认的让步记录");
            return;
        }

        int count = 0;
        for (QcConcessionAcceptance concession : pendingList) {
            String creatorNo = concession.getCreateUserNo();
            if (!StringUtils.hasText(creatorNo)) {
                continue;
            }

            notificationService.send(
                    creatorNo,
                    "让步接收待确认催办提醒",
                    "让步接收申请（ID：" + concession.getId() + "）已发起超过 "
                            + REMINDER_PENDING_DAYS + " 天，尚未获得客户确认，请及时跟进",
                    "CONCESSION",
                    concession.getId()
            );

            // 更新最后催提醒时间
            concession.setLastReminderTime(LocalDateTime.now());
            concessionMapper.updateById(concession);
            count++;
        }
        log.info("[ConcessionScheduler] 共发送 {} 条催确认提醒", count);
    }
}
