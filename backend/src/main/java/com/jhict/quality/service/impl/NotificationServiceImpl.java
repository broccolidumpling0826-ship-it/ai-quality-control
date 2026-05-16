package com.jhict.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.entity.SysNotification;
import com.jhict.quality.mapper.SysNotificationMapper;
import com.jhict.quality.service.api.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Resource
    private SysNotificationMapper notificationMapper;

    @Override
    @Async
    public void send(String receiverNo, String title, String content, String relatedType, String relatedId) {
        try {
            SysNotification notification = new SysNotification();
            notification.setReceiverNo(receiverNo);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setRelatedType(relatedType);
            notification.setRelatedId(relatedId);
            notification.setIsRead(0);
            notificationMapper.insert(notification);
            log.info("站内通知发送成功，receiverNo={}，title={}", receiverNo, title);
        } catch (Exception e) {
            log.error("站内通知发送失败，receiverNo={}，title={}", receiverNo, title, e);
        }
    }

    @Override
    public long getUnreadCount(String userNo) {
        return notificationMapper.selectCount(
                new LambdaQueryWrapper<SysNotification>()
                        .eq(SysNotification::getReceiverNo, userNo)
                        .eq(SysNotification::getIsRead, 0)
        );
    }

    @Override
    public IPage<SysNotification> page(String userNo, int pageNum, int pageSize) {
        return notificationMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysNotification>()
                        .eq(SysNotification::getReceiverNo, userNo)
                        .orderByDesc(SysNotification::getCreateDateTime)
        );
    }

    @Override
    public void markRead(String id) {
        SysNotification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new ServiceException("通知不存在，id=" + id);
        }
        if (notification.getIsRead() == 1) {
            return;
        }
        notification.setIsRead(1);
        notification.setReadTime(LocalDateTime.now());
        notificationMapper.updateById(notification);
    }

    @Override
    public void markAllRead(String userNo) {
        SysNotification update = new SysNotification();
        update.setIsRead(1);
        update.setReadTime(LocalDateTime.now());
        notificationMapper.update(update,
                new LambdaUpdateWrapper<SysNotification>()
                        .eq(SysNotification::getReceiverNo, userNo)
                        .eq(SysNotification::getIsRead, 0)
        );
    }
}
