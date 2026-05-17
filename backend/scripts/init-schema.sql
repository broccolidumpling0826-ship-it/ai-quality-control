-- ============================================================
-- 质量判定解释与让步管理系统 — 数据库初始化脚本
-- MySQL 5.7.43 | utf8mb4_unicode_ci | ROW_FORMAT=DYNAMIC
-- ============================================================

CREATE DATABASE IF NOT EXISTS ai_quality_control DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ai_quality_control;

-- 公共字段说明：
-- id             VARCHAR(64)  雪花ID
-- company_id     VARCHAR(64)  公司ID（多租户预留）
-- create_user_no VARCHAR(64)  创建人工号
-- update_user_no VARCHAR(64)  修改人工号
-- create_date_time VARCHAR(32) 创建时间
-- update_date_time VARCHAR(32) 修改时间

-- ────────────────────────────────────────────────────────────
-- 系统用户表
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id`               VARCHAR(64)  NOT NULL COMMENT '主键',
  `company_id`       VARCHAR(64)  DEFAULT NULL COMMENT '公司ID',
  `create_user_no`   VARCHAR(64)  DEFAULT NULL,
  `update_user_no`   VARCHAR(64)  DEFAULT NULL,
  `create_date_time` VARCHAR(32)  DEFAULT NULL,
  `update_date_time` VARCHAR(32)  DEFAULT NULL,
  `user_no`          VARCHAR(64)  NOT NULL COMMENT '工号（登录账号）',
  `username`         VARCHAR(100) NOT NULL COMMENT '姓名',
  `password`         VARCHAR(255) NOT NULL COMMENT 'BCrypt 加密密码',
  `role`             VARCHAR(50)  NOT NULL COMMENT '角色（字典 USER_ROLE）',
  `department`       VARCHAR(100) DEFAULT NULL COMMENT '部门',
  `status`           TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
  `last_login_time`  DATETIME     DEFAULT NULL COMMENT '最后登录时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_no` (`user_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='系统用户';

-- ────────────────────────────────────────────────────────────
-- 数据字典表
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `sys_dict` (
  `id`               VARCHAR(64)  NOT NULL,
  `company_id`       VARCHAR(64)  DEFAULT NULL,
  `create_user_no`   VARCHAR(64)  DEFAULT NULL,
  `update_user_no`   VARCHAR(64)  DEFAULT NULL,
  `create_date_time` VARCHAR(32)  DEFAULT NULL,
  `update_date_time` VARCHAR(32)  DEFAULT NULL,
  `dict_code`        VARCHAR(50)  NOT NULL COMMENT '字典分类编码',
  `dict_name`        VARCHAR(100) NOT NULL COMMENT '字典分类名称',
  `description`      VARCHAR(500) DEFAULT NULL,
  `is_system`        TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否系统内置',
  `sort_no`          INT          NOT NULL DEFAULT 0,
  `status`           TINYINT(1)   NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_code` (`dict_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='数据字典分类';

CREATE TABLE IF NOT EXISTS `sys_dict_item` (
  `id`               VARCHAR(64)  NOT NULL,
  `company_id`       VARCHAR(64)  DEFAULT NULL,
  `create_user_no`   VARCHAR(64)  DEFAULT NULL,
  `update_user_no`   VARCHAR(64)  DEFAULT NULL,
  `create_date_time` VARCHAR(32)  DEFAULT NULL,
  `update_date_time` VARCHAR(32)  DEFAULT NULL,
  `dict_code`        VARCHAR(50)  NOT NULL COMMENT '关联字典分类编码',
  `item_value`       VARCHAR(100) NOT NULL COMMENT '字典项值',
  `item_label`       VARCHAR(200) NOT NULL COMMENT '显示文本',
  `item_label_en`    VARCHAR(200) DEFAULT NULL,
  `color_tag`        VARCHAR(20)  DEFAULT NULL COMMENT 'Element Plus tag type',
  `sort_no`          INT          NOT NULL DEFAULT 0,
  `status`           TINYINT(1)   NOT NULL DEFAULT 1,
  `is_system`        TINYINT(1)   NOT NULL DEFAULT 0,
  `remark`           VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_item` (`dict_code`, `item_value`),
  KEY `idx_dict_code` (`dict_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='数据字典项';

-- ────────────────────────────────────────────────────────────
-- 站内消息表
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `sys_notification` (
  `id`               VARCHAR(64)  NOT NULL,
  `company_id`       VARCHAR(64)  DEFAULT NULL,
  `create_user_no`   VARCHAR(64)  DEFAULT NULL,
  `update_user_no`   VARCHAR(64)  DEFAULT NULL,
  `create_date_time` VARCHAR(32)  DEFAULT NULL,
  `update_date_time` VARCHAR(32)  DEFAULT NULL,
  `receiver_no`      VARCHAR(64)  NOT NULL COMMENT '接收人工号',
  `title`            VARCHAR(200) NOT NULL COMMENT '通知标题',
  `content`          VARCHAR(1000) NOT NULL COMMENT '通知内容',
  `related_type`     VARCHAR(50)  DEFAULT NULL COMMENT '关联业务类型',
  `related_id`       VARCHAR(64)  DEFAULT NULL COMMENT '关联业务ID',
  `is_read`          TINYINT(1)   NOT NULL DEFAULT 0,
  `read_time`        DATETIME     DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_receiver` (`receiver_no`, `is_read`),
  KEY `idx_create_time` (`create_date_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='站内消息';

-- ────────────────────────────────────────────────────────────
-- 指标项目表
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `qc_indicator_item` (
  `id`                VARCHAR(64)  NOT NULL,
  `company_id`        VARCHAR(64)  DEFAULT NULL,
  `create_user_no`    VARCHAR(64)  DEFAULT NULL,
  `update_user_no`    VARCHAR(64)  DEFAULT NULL,
  `create_date_time`  VARCHAR(32)  DEFAULT NULL,
  `update_date_time`  VARCHAR(32)  DEFAULT NULL,
  `indicator_name`    VARCHAR(100) NOT NULL COMMENT '指标名称',
  `indicator_code`    VARCHAR(50)  NOT NULL COMMENT '指标代码（如Rm）',
  `indicator_category` VARCHAR(20) NOT NULL COMMENT '指标类别（字典 INDICATOR_CATEGORY）',
  `unit`              VARCHAR(20)  DEFAULT NULL COMMENT '单位',
  `test_method`       VARCHAR(200) DEFAULT NULL COMMENT '检测方法',
  `description`       VARCHAR(500) DEFAULT NULL COMMENT '备注说明',
  `status`            VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT '状态 ACTIVE/INACTIVE',
  `is_deleted`        TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_indicator_code` (`indicator_code`),
  KEY `idx_category` (`indicator_category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='指标项目';

-- ────────────────────────────────────────────────────────────
-- 质量标准表
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `qc_quality_standard` (
  `id`               VARCHAR(64)  NOT NULL,
  `company_id`       VARCHAR(64)  DEFAULT NULL,
  `create_user_no`   VARCHAR(64)  DEFAULT NULL,
  `update_user_no`   VARCHAR(64)  DEFAULT NULL,
  `create_date_time` VARCHAR(32)  DEFAULT NULL,
  `update_date_time` VARCHAR(32)  DEFAULT NULL,
  `standard_type`    VARCHAR(20)  NOT NULL COMMENT '标准类型（字典 STANDARD_TYPE）',
  `standard_code`    VARCHAR(100) NOT NULL COMMENT '标准编号',
  `standard_name`    VARCHAR(200) NOT NULL COMMENT '标准名称',
  `variety`          VARCHAR(100) NOT NULL COMMENT '品种',
  `grade`            VARCHAR(100) NOT NULL COMMENT '牌号',
  `spec_range`       VARCHAR(200) NOT NULL COMMENT '规格范围',
  `version_no`       VARCHAR(50)  NOT NULL COMMENT '版本号',
  `effective_date`   DATE         NOT NULL COMMENT '生效日期',
  `expiry_date`      DATE         NOT NULL COMMENT '失效日期（9999-12-31表示未失效）',
  `status`           VARCHAR(20)  NOT NULL DEFAULT 'DRAFT' COMMENT '状态（字典 STANDARD_STATUS）',
  `customer_id`      VARCHAR(64)  DEFAULT NULL COMMENT '客户ID（客协专属）',
  `remark`           VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_standard_window` (`standard_type`, `variety`, `grade`, `customer_id`, `effective_date`),
  KEY `idx_lookup` (`standard_type`, `variety`, `grade`, `effective_date`, `expiry_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='质量标准';

-- ────────────────────────────────────────────────────────────
-- 标准指标表
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `qc_standard_indicator` (
  `id`               VARCHAR(64)  NOT NULL,
  `company_id`       VARCHAR(64)  DEFAULT NULL,
  `create_user_no`   VARCHAR(64)  DEFAULT NULL,
  `update_user_no`   VARCHAR(64)  DEFAULT NULL,
  `create_date_time` VARCHAR(32)  DEFAULT NULL,
  `update_date_time` VARCHAR(32)  DEFAULT NULL,
  `standard_id`      VARCHAR(64)  NOT NULL COMMENT '关联质量标准ID',
  `indicator_id`     VARCHAR(64)  NOT NULL COMMENT '关联指标项目ID',
  `upper_limit`      DECIMAL(20,6) DEFAULT NULL COMMENT '上限（NULL表示无上限）',
  `lower_limit`      DECIMAL(20,6) DEFAULT NULL COMMENT '下限（NULL表示无下限）',
  `is_required`      TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否必检',
  `concession_upper` DECIMAL(20,6) DEFAULT NULL COMMENT '让步上限',
  `concession_lower` DECIMAL(20,6) DEFAULT NULL COMMENT '让步下限',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_standard_indicator` (`standard_id`, `indicator_id`),
  KEY `idx_standard_id` (`standard_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='标准指标';

-- ────────────────────────────────────────────────────────────
-- 检验记录表
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `qc_inspection_record` (
  `id`               VARCHAR(64)  NOT NULL,
  `company_id`       VARCHAR(64)  DEFAULT NULL,
  `create_user_no`   VARCHAR(64)  DEFAULT NULL,
  `update_user_no`   VARCHAR(64)  DEFAULT NULL,
  `create_date_time` VARCHAR(32)  DEFAULT NULL,
  `update_date_time` VARCHAR(32)  DEFAULT NULL,
  `heat_no`          VARCHAR(64)  NOT NULL COMMENT '炉号',
  `coil_no`          VARCHAR(64)  NOT NULL COMMENT '卷号',
  `batch_no`         VARCHAR(64)  NOT NULL COMMENT '批次号（由炉号衍生）',
  `sample_type`      VARCHAR(20)  NOT NULL COMMENT '样品类型（字典 SAMPLE_TYPE）',
  `test_time`        DATETIME     NOT NULL COMMENT '检验时间',
  `tester_no`        VARCHAR(64)  NOT NULL COMMENT '检验人工号',
  `customer_id`      VARCHAR(64)  DEFAULT NULL COMMENT '客户ID（用于匹配客协标准）',
  `product_variety`  VARCHAR(100) NOT NULL COMMENT '品种',
  `product_grade`    VARCHAR(100) NOT NULL COMMENT '牌号',
  `product_spec`     VARCHAR(200) NOT NULL COMMENT '规格',
  `status`           VARCHAR(20)  NOT NULL DEFAULT 'NORMAL' COMMENT '状态（字典 INSPECTION_STATUS）',
  `void_reason`      VARCHAR(500) DEFAULT NULL COMMENT '作废原因',
  `void_by`          VARCHAR(64)  DEFAULT NULL COMMENT '作废操作人工号',
  `void_time`        DATETIME     DEFAULT NULL COMMENT '作废时间',
  PRIMARY KEY (`id`),
  KEY `idx_coil_status` (`coil_no`, `status`),
  KEY `idx_batch` (`batch_no`),
  KEY `idx_heat` (`heat_no`),
  KEY `idx_test_time` (`test_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='检验记录';

-- ────────────────────────────────────────────────────────────
-- 检验值表
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `qc_inspection_value` (
  `id`               VARCHAR(64)  NOT NULL,
  `company_id`       VARCHAR(64)  DEFAULT NULL,
  `create_user_no`   VARCHAR(64)  DEFAULT NULL,
  `update_user_no`   VARCHAR(64)  DEFAULT NULL,
  `create_date_time` VARCHAR(32)  DEFAULT NULL,
  `update_date_time` VARCHAR(32)  DEFAULT NULL,
  `record_id`        VARCHAR(64)  NOT NULL COMMENT '关联检验记录ID',
  `indicator_id`     VARCHAR(64)  NOT NULL COMMENT '关联指标项目ID',
  `test_value`       DECIMAL(20,6) DEFAULT NULL COMMENT '实测值（数值型）',
  `value_text`       VARCHAR(500) DEFAULT NULL COMMENT '文本值',
  PRIMARY KEY (`id`),
  KEY `idx_record_id` (`record_id`),
  UNIQUE KEY `uk_record_indicator` (`record_id`, `indicator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='检验值';

-- ────────────────────────────────────────────────────────────
-- 判定结论表
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `qc_judgment_result` (
  `id`                   VARCHAR(64)  NOT NULL,
  `company_id`           VARCHAR(64)  DEFAULT NULL,
  `create_user_no`       VARCHAR(64)  DEFAULT NULL,
  `update_user_no`       VARCHAR(64)  DEFAULT NULL,
  `create_date_time`     VARCHAR(32)  DEFAULT NULL,
  `update_date_time`     VARCHAR(32)  DEFAULT NULL,
  `record_id`            VARCHAR(64)  NOT NULL COMMENT '关联检验记录ID',
  `judgment_type`        VARCHAR(30)  NOT NULL COMMENT '判定结论（字典 JUDGMENT_TYPE）',
  `judgment_time`        DATETIME     NOT NULL COMMENT '判定时间',
  `is_final`             TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否当前最终结论',
  `matched_standard_ids` TEXT         DEFAULT NULL COMMENT '命中标准ID列表（JSON数组）',
  `remark`               VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_record_final` (`record_id`, `is_final`),
  KEY `idx_judgment_type` (`judgment_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='判定结论';

-- ────────────────────────────────────────────────────────────
-- 判定依据表（快照设计）
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `qc_judgment_evidence` (
  `id`               VARCHAR(64)   NOT NULL,
  `company_id`       VARCHAR(64)   DEFAULT NULL,
  `create_user_no`   VARCHAR(64)   DEFAULT NULL,
  `update_user_no`   VARCHAR(64)   DEFAULT NULL,
  `create_date_time` VARCHAR(32)   DEFAULT NULL,
  `update_date_time` VARCHAR(32)   DEFAULT NULL,
  `judgment_id`      VARCHAR(64)   NOT NULL COMMENT '关联判定结论ID',
  `standard_id`      VARCHAR(64)   NOT NULL COMMENT '关联标准ID（快照时用）',
  `indicator_id`     VARCHAR(64)   NOT NULL COMMENT '关联指标ID',
  `test_value`       DECIMAL(20,6) DEFAULT NULL COMMENT '实测值',
  `upper_limit`      DECIMAL(20,6) DEFAULT NULL COMMENT '标准上限（快照值）',
  `lower_limit`      DECIMAL(20,6) DEFAULT NULL COMMENT '标准下限（快照值）',
  `deviation`        DECIMAL(20,6) DEFAULT NULL COMMENT '偏差值',
  `trigger_rule`     VARCHAR(500)  NOT NULL COMMENT '触发规则描述（快照值）',
  `is_passed`        TINYINT(1)    NOT NULL COMMENT '该指标是否通过',
  PRIMARY KEY (`id`),
  KEY `idx_judgment_id` (`judgment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='判定依据（快照）';

-- ────────────────────────────────────────────────────────────
-- 标准覆盖缺口表
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `standard_gap` (
  `id`                VARCHAR(64)  NOT NULL,
  `company_id`        VARCHAR(64)  DEFAULT NULL,
  `create_user_no`    VARCHAR(64)  DEFAULT NULL,
  `update_user_no`    VARCHAR(64)  DEFAULT NULL,
  `create_date_time`  VARCHAR(32)  DEFAULT NULL,
  `update_date_time`  VARCHAR(32)  DEFAULT NULL,
  `variety`           VARCHAR(100) NOT NULL COMMENT '品种',
  `grade`             VARCHAR(100) NOT NULL COMMENT '牌号',
  `indicator_id`      VARCHAR(64)  NOT NULL COMMENT '关联指标ID',
  `first_found_time`  DATETIME     NOT NULL COMMENT '首次发现时间',
  `related_record_id` VARCHAR(64)  NOT NULL COMMENT '关联检验记录ID',
  `is_resolved`       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已处理',
  PRIMARY KEY (`id`),
  KEY `idx_variety_grade` (`variety`, `grade`),
  KEY `idx_indicator` (`indicator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='标准覆盖缺口';

-- ────────────────────────────────────────────────────────────
-- 复检记录表
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `qc_reinspection_record` (
  `id`                     VARCHAR(64)  NOT NULL,
  `company_id`             VARCHAR(64)  DEFAULT NULL,
  `create_user_no`         VARCHAR(64)  DEFAULT NULL,
  `update_user_no`         VARCHAR(64)  DEFAULT NULL,
  `create_date_time`       VARCHAR(32)  DEFAULT NULL,
  `update_date_time`       VARCHAR(32)  DEFAULT NULL,
  `original_judgment_id`   VARCHAR(64)  NOT NULL COMMENT '原判定结论ID',
  `reinspection_reason`    VARCHAR(500) NOT NULL COMMENT '复检原因',
  `new_record_id`          VARCHAR(64)  DEFAULT NULL COMMENT '复检新检验记录ID',
  `responsible_no`         VARCHAR(64)  NOT NULL COMMENT '责任人工号',
  `status`                 VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '状态（字典 REINSPECTION_STATUS）',
  PRIMARY KEY (`id`),
  KEY `idx_judgment_id` (`original_judgment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='复检记录';

-- ────────────────────────────────────────────────────────────
-- 改判申请表
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `qc_rejudgment_request` (
  `id`                       VARCHAR(64)   NOT NULL,
  `company_id`               VARCHAR(64)   DEFAULT NULL,
  `create_user_no`           VARCHAR(64)   DEFAULT NULL,
  `update_user_no`           VARCHAR(64)   DEFAULT NULL,
  `create_date_time`         VARCHAR(32)   DEFAULT NULL,
  `update_date_time`         VARCHAR(32)   DEFAULT NULL,
  `original_judgment_id`     VARCHAR(64)   NOT NULL COMMENT '原判定结论ID',
  `original_judgment_type`   VARCHAR(30)   NOT NULL COMMENT '改判前结论',
  `target_judgment_type`     VARCHAR(30)   NOT NULL COMMENT '改判后目标结论',
  `rejudgment_reason`        VARCHAR(1000) NOT NULL COMMENT '改判原因',
  `affect_scope`             VARCHAR(1000) NOT NULL COMMENT '影响范围',
  `is_reverse`               TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '是否逆向改判',
  `new_evidence_source`      VARCHAR(500)  DEFAULT NULL COMMENT '新证据来源（逆向必填）',
  `evidence_attachment_url`  VARCHAR(500)  DEFAULT NULL COMMENT '证据附件URL（逆向必填）',
  `approval_level`           VARCHAR(20)   NOT NULL COMMENT '审批级别（字典 APPROVAL_LEVEL）',
  `approval_status`          VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT '审批状态（字典 APPROVAL_STATUS）',
  PRIMARY KEY (`id`),
  KEY `idx_judgment_id` (`original_judgment_id`),
  KEY `idx_approval_status` (`approval_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='改判申请';

-- ────────────────────────────────────────────────────────────
-- 改判审批记录表
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `qc_rejudgment_approval` (
  `id`               VARCHAR(64)   NOT NULL,
  `company_id`       VARCHAR(64)   DEFAULT NULL,
  `create_user_no`   VARCHAR(64)   DEFAULT NULL,
  `update_user_no`   VARCHAR(64)   DEFAULT NULL,
  `create_date_time` VARCHAR(32)   DEFAULT NULL,
  `update_date_time` VARCHAR(32)   DEFAULT NULL,
  `request_id`       VARCHAR(64)   NOT NULL COMMENT '关联改判申请ID',
  `approver_no`      VARCHAR(64)   NOT NULL COMMENT '审批人工号',
  `approval_action`  VARCHAR(20)   NOT NULL COMMENT 'APPROVED | REJECTED',
  `approval_comment` VARCHAR(1000) DEFAULT NULL COMMENT '审批意见',
  `approval_time`    DATETIME      NOT NULL COMMENT '审批时间',
  PRIMARY KEY (`id`),
  KEY `idx_request_id` (`request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='改判审批记录';

-- ────────────────────────────────────────────────────────────
-- 让步接收表
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `qc_concession_acceptance` (
  `id`                    VARCHAR(64)   NOT NULL,
  `company_id`            VARCHAR(64)   DEFAULT NULL,
  `create_user_no`        VARCHAR(64)   DEFAULT NULL,
  `update_user_no`        VARCHAR(64)   DEFAULT NULL,
  `create_date_time`      VARCHAR(32)   DEFAULT NULL,
  `update_date_time`      VARCHAR(32)   DEFAULT NULL,
  `judgment_id`           VARCHAR(64)   NOT NULL COMMENT '关联判定结论ID',
  `concession_scope`      VARCHAR(1000) NOT NULL COMMENT '让步范围',
  `risk_description`      VARCHAR(1000) NOT NULL COMMENT '风险说明',
  `effective_date`        DATE          NOT NULL COMMENT '让步有效期开始',
  `expiry_date`           DATE          NOT NULL COMMENT '让步有效期结束',
  `confirm_status`        VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT '客户确认状态（字典 CONFIRM_STATUS）',
  `confirm_attachment_url` VARCHAR(500) DEFAULT NULL COMMENT '确认附件URL（强制，不可替换）',
  `confirm_note`          VARCHAR(1000) DEFAULT NULL COMMENT '确认说明文字（可选）',
  `confirm_uploader_no`   VARCHAR(64)   DEFAULT NULL COMMENT '附件上传人工号',
  `confirm_upload_time`   DATETIME      DEFAULT NULL COMMENT '附件上传时间',
  `approval_status`       VARCHAR(20)   NOT NULL DEFAULT 'PENDING_APPROVAL' COMMENT '让步总状态（字典 CONCESSION_STATUS）',
  `void_reason`           VARCHAR(500)  DEFAULT NULL COMMENT '失效/作废原因',
  `last_reminder_time`    DATETIME      DEFAULT NULL COMMENT '最后催确认时间',
  PRIMARY KEY (`id`),
  KEY `idx_judgment_id` (`judgment_id`),
  KEY `idx_expiry` (`expiry_date`, `approval_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='让步接收';

-- ────────────────────────────────────────────────────────────
-- 质保书数据表
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `qc_quality_cert_data` (
  `id`               VARCHAR(64)  NOT NULL,
  `company_id`       VARCHAR(64)  DEFAULT NULL,
  `create_user_no`   VARCHAR(64)  DEFAULT NULL,
  `update_user_no`   VARCHAR(64)  DEFAULT NULL,
  `create_date_time` VARCHAR(32)  DEFAULT NULL,
  `update_date_time` VARCHAR(32)  DEFAULT NULL,
  `coil_no`          VARCHAR(64)  DEFAULT NULL COMMENT '卷号',
  `batch_no`         VARCHAR(64)  DEFAULT NULL COMMENT '批次号',
  `snapshot_data`    LONGTEXT     NOT NULL COMMENT '汇总指标快照（JSON）',
  `generate_time`    DATETIME     NOT NULL COMMENT '生成时间',
  `generated_by`     VARCHAR(64)  NOT NULL COMMENT '生成操作人工号',
  PRIMARY KEY (`id`),
  KEY `idx_coil` (`coil_no`),
  KEY `idx_batch` (`batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='质保书数据';

-- ────────────────────────────────────────────────────────────
-- 审计日志表（不继承CoreEntity，不自动填充）
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `qc_audit_log` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键（自增，不用雪花ID）',
  `company_id`      VARCHAR(64)  DEFAULT NULL,
  `operation_type`  VARCHAR(50)  NOT NULL COMMENT '操作类型',
  `target_entity`   VARCHAR(50)  NOT NULL COMMENT '操作对象实体名',
  `target_id`       VARCHAR(64)  NOT NULL COMMENT '操作对象ID',
  `before_value`    TEXT         DEFAULT NULL COMMENT '操作前值（JSON）',
  `after_value`     TEXT         DEFAULT NULL COMMENT '操作后值（JSON）',
  `operator_no`     VARCHAR(64)  NOT NULL COMMENT '操作人工号',
  `operate_time`    DATETIME     NOT NULL COMMENT '操作时间',
  `ip_address`      VARCHAR(50)  DEFAULT NULL,
  `remark`          VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_entity`, `target_id`),
  KEY `idx_operator` (`operator_no`),
  KEY `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='审计日志';
