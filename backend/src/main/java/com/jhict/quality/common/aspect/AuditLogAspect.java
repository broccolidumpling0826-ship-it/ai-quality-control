package com.jhict.quality.common.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.jhict.quality.common.annotation.AuditLog;
import com.jhict.quality.entity.QcAuditLog;
import com.jhict.quality.mapper.QcAuditLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
public class AuditLogAspect {

    /** 异步写日志的独立 Bean，避免 @Async 在同一类中失效 */
    @Resource
    private AuditLogWriter auditLogWriter;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint pjp, AuditLog auditLog) throws Throwable {
        // 在方法执行前快照 IP，因为执行后 RequestContext 可能已销毁
        String ipAddress = getClientIp();
        Object result = pjp.proceed();

        // 异步写日志（不影响主流程）
        auditLogWriter.writeAsync(pjp.getArgs(), auditLog, ipAddress);
        return result;
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return "UNKNOWN";
            HttpServletRequest request = attrs.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    // =====================================================================
    //  内部异步写日志 Bean
    // =====================================================================

    @Slf4j
    @Component
    public static class AuditLogWriter {

        @Resource
        private QcAuditLogMapper auditLogMapper;

        @Async
        public void writeAsync(Object[] args, AuditLog auditLog, String ipAddress) {
            try {
                String targetId = extractTargetId(args);
                String operatorNo = getLoginUserNo();
                String remark = buildRemark(auditLog, args);

                QcAuditLog entity = new QcAuditLog();
                entity.setOperationType(auditLog.operationType());
                entity.setTargetEntity(auditLog.targetEntity());
                entity.setTargetId(targetId);
                entity.setOperatorNo(operatorNo);
                entity.setOperateTime(LocalDateTime.now());
                entity.setIpAddress(ipAddress);
                entity.setRemark(remark);

                auditLogMapper.insert(entity);
                log.debug("审计日志已保存，operationType={}，targetId={}", auditLog.operationType(), targetId);
            } catch (Exception e) {
                log.error("审计日志保存失败，operationType={}", auditLog.operationType(), e);
            }
        }

        /**
         * 从方法参数中提取 targetId：
         * 优先取第一个 String 参数；若为对象则反射取 id 字段
         */
        private String extractTargetId(Object[] args) {
            if (args == null || args.length == 0) return null;
            Object first = args[0];
            if (first instanceof String) return (String) first;
            if (first != null) {
                try {
                    Field idField = findField(first.getClass(), "id");
                    if (idField != null) {
                        idField.setAccessible(true);
                        Object idVal = idField.get(first);
                        return idVal != null ? idVal.toString() : null;
                    }
                } catch (Exception e) {
                    log.debug("反射获取 targetId 失败", e);
                }
            }
            return null;
        }

        /**
         * 构建备注：逆向改判时追加 [逆向改判]
         */
        private String buildRemark(AuditLog auditLog, Object[] args) {
            StringBuilder sb = new StringBuilder();
            if (auditLog.targetEntity() != null && auditLog.targetEntity().contains("Rejudgment")) {
                if (detectIsReverse(args)) {
                    sb.append("[逆向改判] ");
                }
            }
            sb.append("operationType=").append(auditLog.operationType());
            return sb.toString();
        }

        private boolean detectIsReverse(Object[] args) {
            if (args == null) return false;
            for (Object arg : args) {
                if (arg == null) continue;
                try {
                    Field f = findField(arg.getClass(), "isReverse");
                    if (f != null) {
                        f.setAccessible(true);
                        Object val = f.get(arg);
                        if (Integer.valueOf(1).equals(val)) return true;
                    }
                } catch (Exception ignored) {
                }
            }
            return false;
        }

        private Field findField(Class<?> clazz, String fieldName) {
            Class<?> current = clazz;
            while (current != null && current != Object.class) {
                try {
                    return current.getDeclaredField(fieldName);
                } catch (NoSuchFieldException e) {
                    current = current.getSuperclass();
                }
            }
            return null;
        }

        private String getLoginUserNo() {
            try {
                Object loginId = StpUtil.getLoginIdDefaultNull();
                return loginId != null ? loginId.toString() : "SYSTEM";
            } catch (Exception e) {
                return "SYSTEM";
            }
        }
    }
}
