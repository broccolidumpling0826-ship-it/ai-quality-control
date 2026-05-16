package com.jhict.quality.common.annotation;

import java.lang.annotation.*;

/**
 * 审计日志注解，标注在 Service 方法上，由 AuditLogAspect 异步记录操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /**
     * 操作类型，例如：VOID_INSPECTION、APPROVE_REJUDGMENT
     */
    String operationType() default "";

    /**
     * 目标实体名称，例如：QcInspectionRecord
     */
    String targetEntity() default "";
}
