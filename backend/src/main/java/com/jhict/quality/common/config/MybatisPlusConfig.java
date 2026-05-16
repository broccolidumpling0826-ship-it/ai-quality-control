package com.jhict.quality.common.config;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.annotation.DbType;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

    @Slf4j
    @Component
    public static class CustomMetaObjectHandler implements MetaObjectHandler {

        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        private static final String DEFAULT_COMPANY_ID = "DEFAULT";

        @Override
        public void insertFill(MetaObject metaObject) {
            String now = LocalDateTime.now().format(FORMATTER);
            String loginId = getLoginId();

            this.strictInsertFill(metaObject, "id", () -> IdUtil.getSnowflakeNextIdStr(), String.class);
            this.strictInsertFill(metaObject, "companyId", () -> DEFAULT_COMPANY_ID, String.class);
            this.strictInsertFill(metaObject, "createUserNo", () -> loginId, String.class);
            this.strictInsertFill(metaObject, "updateUserNo", () -> loginId, String.class);
            this.strictInsertFill(metaObject, "createDateTime", () -> now, String.class);
            this.strictInsertFill(metaObject, "updateDateTime", () -> now, String.class);
        }

        @Override
        public void updateFill(MetaObject metaObject) {
            String now = LocalDateTime.now().format(FORMATTER);
            String loginId = getLoginId();

            this.strictUpdateFill(metaObject, "updateUserNo", () -> loginId, String.class);
            this.strictUpdateFill(metaObject, "updateDateTime", () -> now, String.class);
        }

        private String getLoginId() {
            try {
                Object loginId = StpUtil.getLoginIdDefaultNull();
                return loginId != null ? loginId.toString() : "SYSTEM";
            } catch (Exception e) {
                log.debug("获取登录用户ID失败，使用默认值SYSTEM: {}", e.getMessage());
                return "SYSTEM";
            }
        }
    }
}
