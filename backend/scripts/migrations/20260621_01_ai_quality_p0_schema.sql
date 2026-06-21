-- ============================================================
-- AI quality enhancement P0 additive schema
-- MySQL 5.7 compatible. Execute after init-schema.sql and init-menu-rbac.sql.
-- ============================================================

USE ai_quality_control;

-- ────────────────────────────────────────────────────────────
-- Standard source documents for RAG and citation.
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `qc_standard_document` (
  `id`                 VARCHAR(64)  NOT NULL COMMENT '主键',
  `company_id`         VARCHAR(64)  DEFAULT NULL COMMENT '公司ID',
  `create_user_no`     VARCHAR(64)  DEFAULT NULL COMMENT '创建人工号',
  `update_user_no`     VARCHAR(64)  DEFAULT NULL COMMENT '修改人工号',
  `create_date_time`   VARCHAR(32)  DEFAULT NULL COMMENT '创建时间',
  `update_date_time`   VARCHAR(32)  DEFAULT NULL COMMENT '修改时间',
  `standard_id`        VARCHAR(64)  DEFAULT NULL COMMENT '结构化标准ID，投诉/案例文档可为空',
  `document_code`      VARCHAR(100) NOT NULL COMMENT '文档编号',
  `document_name`      VARCHAR(200) NOT NULL COMMENT '文档名称',
  `document_type`      VARCHAR(30)  NOT NULL COMMENT '文档类型 STANDARD/AGREEMENT/CASE/COMPLAINT',
  `standard_type`      VARCHAR(20)  DEFAULT NULL COMMENT '标准类型 NATIONAL/ENTERPRISE/CUSTOMER',
  `standard_code`      VARCHAR(100) DEFAULT NULL COMMENT '标准编号快照',
  `standard_name`      VARCHAR(200) DEFAULT NULL COMMENT '标准名称快照',
  `version_no`         VARCHAR(50)  DEFAULT NULL COMMENT '版本号',
  `customer_id`        VARCHAR(64)  DEFAULT NULL COMMENT '客户ID',
  `variety`            VARCHAR(100) DEFAULT NULL COMMENT '适用品种',
  `grade`              VARCHAR(100) DEFAULT NULL COMMENT '适用牌号',
  `spec_range`         VARCHAR(200) DEFAULT NULL COMMENT '适用规格范围',
  `usage_scope`        VARCHAR(200) DEFAULT NULL COMMENT '客户用途或适用场景',
  `effective_date`     DATE         DEFAULT NULL COMMENT '生效日期',
  `expiry_date`        DATE         DEFAULT NULL COMMENT '失效日期',
  `source_file_name`   VARCHAR(255) DEFAULT NULL COMMENT '原始文件名',
  `source_file_path`   VARCHAR(500) DEFAULT NULL COMMENT '原始文件路径或URL',
  `source_file_hash`   VARCHAR(128) DEFAULT NULL COMMENT '原始文件哈希',
  `parse_status`       VARCHAR(30)  NOT NULL DEFAULT 'PENDING' COMMENT '解析状态 PENDING/PARSED/FAILED',
  `index_status`       VARCHAR(30)  NOT NULL DEFAULT 'PENDING' COMMENT '索引状态 PENDING/INDEXED/FAILED',
  `parse_error_message` VARCHAR(1000) DEFAULT NULL COMMENT '解析或索引错误',
  `indexed_at`         DATETIME     DEFAULT NULL COMMENT '索引时间',
  `status`             VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT '状态 ACTIVE/INACTIVE',
  `remark`             VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_document_code` (`document_code`),
  KEY `idx_standard_id` (`standard_id`),
  KEY `idx_document_type` (`document_type`, `status`),
  KEY `idx_standard_lookup` (`standard_type`, `standard_code`, `version_no`),
  KEY `idx_applicability` (`customer_id`, `variety`, `grade`, `effective_date`, `expiry_date`),
  KEY `idx_index_status` (`index_status`, `parse_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='标准/协议/案例源文档';

-- ────────────────────────────────────────────────────────────
-- Clause records mirrored into vector index.
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `qc_standard_clause` (
  `id`                 VARCHAR(64)  NOT NULL COMMENT '主键',
  `company_id`         VARCHAR(64)  DEFAULT NULL COMMENT '公司ID',
  `create_user_no`     VARCHAR(64)  DEFAULT NULL COMMENT '创建人工号',
  `update_user_no`     VARCHAR(64)  DEFAULT NULL COMMENT '修改人工号',
  `create_date_time`   VARCHAR(32)  DEFAULT NULL COMMENT '创建时间',
  `update_date_time`   VARCHAR(32)  DEFAULT NULL COMMENT '修改时间',
  `document_id`        VARCHAR(64)  NOT NULL COMMENT '源文档ID',
  `standard_id`        VARCHAR(64)  DEFAULT NULL COMMENT '结构化标准ID',
  `clause_key`         VARCHAR(160) NOT NULL COMMENT '稳定条款键',
  `clause_no`          VARCHAR(80)  DEFAULT NULL COMMENT '条款号',
  `page_no`            INT          DEFAULT NULL COMMENT '页码',
  `paragraph_text`     LONGTEXT     NOT NULL COMMENT '来源段落原文',
  `source_type`        VARCHAR(30)  NOT NULL COMMENT '来源类型 NATIONAL/ENTERPRISE/CUSTOMER/CASE/COMPLAINT',
  `standard_type`      VARCHAR(20)  DEFAULT NULL COMMENT '标准类型快照',
  `standard_code`      VARCHAR(100) DEFAULT NULL COMMENT '标准编号快照',
  `standard_name`      VARCHAR(200) DEFAULT NULL COMMENT '标准名称快照',
  `version_no`         VARCHAR(50)  DEFAULT NULL COMMENT '版本号快照',
  `customer_id`        VARCHAR(64)  DEFAULT NULL COMMENT '客户ID',
  `variety`            VARCHAR(100) DEFAULT NULL COMMENT '适用品种',
  `grade`              VARCHAR(100) DEFAULT NULL COMMENT '适用牌号',
  `spec_range`         VARCHAR(200) DEFAULT NULL COMMENT '适用规格范围',
  `usage_scope`        VARCHAR(200) DEFAULT NULL COMMENT '客户用途或适用场景',
  `indicator_id`       VARCHAR(64)  DEFAULT NULL COMMENT '关联指标ID',
  `indicator_code`     VARCHAR(50)  DEFAULT NULL COMMENT '指标代码',
  `indicator_name`     VARCHAR(100) DEFAULT NULL COMMENT '指标名称',
  `effective_date`     DATE         DEFAULT NULL COMMENT '生效日期',
  `expiry_date`        DATE         DEFAULT NULL COMMENT '失效日期',
  `retrieval_keywords` VARCHAR(500) DEFAULT NULL COMMENT '检索关键词',
  `es_document_key`    VARCHAR(160) DEFAULT NULL COMMENT 'ES文档键',
  `embedding_status`   VARCHAR(30)  NOT NULL DEFAULT 'PENDING' COMMENT '向量状态 PENDING/INDEXED/FAILED',
  `relevance_group`    VARCHAR(50)  DEFAULT NULL COMMENT '演示/评测分组',
  `status`             VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT '状态 ACTIVE/INACTIVE',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_clause_key` (`clause_key`),
  UNIQUE KEY `uk_es_document_key` (`es_document_key`),
  KEY `idx_document_id` (`document_id`),
  KEY `idx_standard_id` (`standard_id`),
  KEY `idx_source_type` (`source_type`, `status`),
  KEY `idx_indicator` (`indicator_id`, `indicator_code`),
  KEY `idx_applicability` (`customer_id`, `variety`, `grade`, `effective_date`, `expiry_date`),
  KEY `idx_embedding_status` (`embedding_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='标准/协议/案例条款';

-- ────────────────────────────────────────────────────────────
-- Standard conflict records. Same-priority conflicts are blocking.
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `standard_conflict` (
  `id`                    VARCHAR(64)  NOT NULL COMMENT '主键',
  `company_id`            VARCHAR(64)  DEFAULT NULL COMMENT '公司ID',
  `create_user_no`        VARCHAR(64)  DEFAULT NULL COMMENT '创建人工号',
  `update_user_no`        VARCHAR(64)  DEFAULT NULL COMMENT '修改人工号',
  `create_date_time`      VARCHAR(32)  DEFAULT NULL COMMENT '创建时间',
  `update_date_time`      VARCHAR(32)  DEFAULT NULL COMMENT '修改时间',
  `conflict_no`           VARCHAR(80)  NOT NULL COMMENT '冲突编号',
  `judgment_id`           VARCHAR(64)  DEFAULT NULL COMMENT '关联判定ID',
  `record_id`             VARCHAR(64)  DEFAULT NULL COMMENT '关联检验记录ID',
  `conflict_type`         VARCHAR(50)  NOT NULL COMMENT '冲突类型 NUMERIC_LIMIT/UNIT/SPEC_RANGE/CALIBER/PRIORITY',
  `conflict_level`        VARCHAR(40)  NOT NULL COMMENT '冲突级别 PRIORITY_RESOLVABLE/BLOCKING',
  `status`                VARCHAR(30)  NOT NULL DEFAULT 'PENDING' COMMENT '状态 PENDING/RESOLVED/VOID',
  `indicator_id`          VARCHAR(64)  DEFAULT NULL COMMENT '指标ID',
  `indicator_name`        VARCHAR(100) DEFAULT NULL COMMENT '指标名称',
  `unit`                  VARCHAR(20)  DEFAULT NULL COMMENT '单位',
  `customer_id`           VARCHAR(64)  DEFAULT NULL COMMENT '客户ID',
  `variety`               VARCHAR(100) DEFAULT NULL COMMENT '品种',
  `grade`                 VARCHAR(100) DEFAULT NULL COMMENT '牌号',
  `product_spec`          VARCHAR(200) DEFAULT NULL COMMENT '规格',
  `inspection_date`       DATE         DEFAULT NULL COMMENT '检验日期',
  `selected_standard_id`  VARCHAR(64)  DEFAULT NULL COMMENT '优先级已选择标准ID',
  `involved_standard_ids` TEXT         DEFAULT NULL COMMENT '涉及标准ID列表(JSON数组)',
  `conflict_detail`       LONGTEXT     DEFAULT NULL COMMENT '冲突明细(JSON)',
  `selected_priority`     VARCHAR(30)  DEFAULT NULL COMMENT '优先级选择依据',
  `decision_standard_id`  VARCHAR(64)  DEFAULT NULL COMMENT '裁决控制标准ID',
  `decision_reason`       VARCHAR(1000) DEFAULT NULL COMMENT '裁决理由',
  `decision_by`           VARCHAR(64)  DEFAULT NULL COMMENT '裁决人工号',
  `decision_time`         DATETIME     DEFAULT NULL COMMENT '裁决时间',
  `rejudge_judgment_id`   VARCHAR(64)  DEFAULT NULL COMMENT '裁决后新判定ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_conflict_no` (`conflict_no`),
  KEY `idx_status_level` (`status`, `conflict_level`),
  KEY `idx_judgment_id` (`judgment_id`),
  KEY `idx_record_id` (`record_id`),
  KEY `idx_indicator` (`indicator_id`, `indicator_name`),
  KEY `idx_scope` (`customer_id`, `variety`, `grade`, `inspection_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='标准冲突记录';

-- ────────────────────────────────────────────────────────────
-- Immutable AI assessment outputs with append-only handling state.
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `qc_ai_assessment` (
  `id`                 VARCHAR(64)  NOT NULL COMMENT '主键',
  `company_id`         VARCHAR(64)  DEFAULT NULL COMMENT '公司ID',
  `create_user_no`     VARCHAR(64)  DEFAULT NULL COMMENT '创建人工号',
  `update_user_no`     VARCHAR(64)  DEFAULT NULL COMMENT '修改人工号',
  `create_date_time`   VARCHAR(32)  DEFAULT NULL COMMENT '创建时间',
  `update_date_time`   VARCHAR(32)  DEFAULT NULL COMMENT '修改时间',
  `assessment_type`    VARCHAR(50)  NOT NULL COMMENT '评估类型 JUDGMENT_EXPLANATION/CONCESSION_RISK/REINSPECTION_ADVICE/REJUDGMENT_ADVICE/CERT_QA/STANDARD_RAG',
  `business_type`      VARCHAR(50)  NOT NULL COMMENT '业务对象类型',
  `business_id`        VARCHAR(64)  NOT NULL COMMENT '业务对象ID',
  `related_judgment_id` VARCHAR(64) DEFAULT NULL COMMENT '关联判定ID',
  `input_snapshot`     LONGTEXT     NOT NULL COMMENT '输入快照(JSON)',
  `references_json`    LONGTEXT     DEFAULT NULL COMMENT '引用来源(JSON)',
  `model_provider`     VARCHAR(50)  DEFAULT NULL COMMENT '模型提供方',
  `model_name`         VARCHAR(100) DEFAULT NULL COMMENT '模型名称',
  `prompt_version`     VARCHAR(50)  DEFAULT NULL COMMENT 'Prompt版本',
  `raw_output`         LONGTEXT     DEFAULT NULL COMMENT '原始输出',
  `structured_output`  LONGTEXT     DEFAULT NULL COMMENT '结构化输出(JSON)',
  `risk_level`         VARCHAR(30)  DEFAULT NULL COMMENT '风险等级 LOW/MEDIUM/HIGH/BLOCKED',
  `confidence_score`   DECIMAL(8,6) DEFAULT NULL COMMENT '置信度分值',
  `confidence_label`   VARCHAR(20)  NOT NULL COMMENT '置信度 HIGH/MEDIUM/LOW',
  `confidence_factors` LONGTEXT     DEFAULT NULL COMMENT '置信因素(JSON)',
  `degradation_source` VARCHAR(30)  NOT NULL COMMENT '降级来源 GENERATED/CACHE/RULE_TEMPLATE/RAW_RETRIEVAL/UNAVAILABLE',
  `cache_hit`          TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否命中缓存',
  `cache_key`          VARCHAR(160) DEFAULT NULL COMMENT '缓存键',
  `adoption_status`    VARCHAR(30)  NOT NULL DEFAULT 'PENDING' COMMENT '处理状态 PENDING/ADOPTED/IGNORED/SUPERSEDED',
  `human_opinion`      VARCHAR(1000) DEFAULT NULL COMMENT '人工意见',
  `handled_by`         VARCHAR(64)  DEFAULT NULL COMMENT '处理人工号',
  `handled_time`       DATETIME     DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`id`),
  KEY `idx_assessment_type` (`assessment_type`, `create_date_time`),
  KEY `idx_business` (`business_type`, `business_id`),
  KEY `idx_related_judgment` (`related_judgment_id`),
  KEY `idx_confidence` (`confidence_label`, `degradation_source`),
  KEY `idx_adoption` (`adoption_status`),
  KEY `idx_cache_key` (`cache_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='AI评估输出记录';

-- ────────────────────────────────────────────────────────────
-- Pre-generated and reusable AI cache entries.
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `qc_ai_cache` (
  `id`                 VARCHAR(64)  NOT NULL COMMENT '主键',
  `company_id`         VARCHAR(64)  DEFAULT NULL COMMENT '公司ID',
  `create_user_no`     VARCHAR(64)  DEFAULT NULL COMMENT '创建人工号',
  `update_user_no`     VARCHAR(64)  DEFAULT NULL COMMENT '修改人工号',
  `create_date_time`   VARCHAR(32)  DEFAULT NULL COMMENT '创建时间',
  `update_date_time`   VARCHAR(32)  DEFAULT NULL COMMENT '修改时间',
  `cache_key`          VARCHAR(160) NOT NULL COMMENT '缓存键',
  `assessment_type`    VARCHAR(50)  NOT NULL COMMENT '评估类型',
  `business_type`      VARCHAR(50)  DEFAULT NULL COMMENT '业务对象类型',
  `business_id`        VARCHAR(64)  DEFAULT NULL COMMENT '业务对象ID',
  `prompt_version`     VARCHAR(50)  DEFAULT NULL COMMENT 'Prompt版本',
  `input_hash`         VARCHAR(128) DEFAULT NULL COMMENT '输入哈希',
  `cached_output`      LONGTEXT     NOT NULL COMMENT '缓存输出(JSON或文本)',
  `references_json`    LONGTEXT     DEFAULT NULL COMMENT '引用来源(JSON)',
  `confidence_label`   VARCHAR(20)  NOT NULL COMMENT '置信度 HIGH/MEDIUM/LOW',
  `confidence_score`   DECIMAL(8,6) DEFAULT NULL COMMENT '置信度分值',
  `degradation_source` VARCHAR(30)  NOT NULL DEFAULT 'CACHE' COMMENT '降级来源',
  `enabled`            TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用',
  `expiry_time`        DATETIME     DEFAULT NULL COMMENT '过期时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cache_key` (`cache_key`),
  KEY `idx_assessment_business` (`assessment_type`, `business_type`, `business_id`),
  KEY `idx_enabled_expiry` (`enabled`, `expiry_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='AI降级缓存';

-- ────────────────────────────────────────────────────────────
-- Confidence configuration. Keep rule banding authoritative.
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `qc_ai_confidence_config` (
  `id`                VARCHAR(64)  NOT NULL COMMENT '主键',
  `company_id`        VARCHAR(64)  DEFAULT NULL COMMENT '公司ID',
  `create_user_no`    VARCHAR(64)  DEFAULT NULL COMMENT '创建人工号',
  `update_user_no`    VARCHAR(64)  DEFAULT NULL COMMENT '修改人工号',
  `create_date_time`  VARCHAR(32)  DEFAULT NULL COMMENT '创建时间',
  `update_date_time`  VARCHAR(32)  DEFAULT NULL COMMENT '修改时间',
  `config_name`       VARCHAR(100) NOT NULL COMMENT '配置名称',
  `rule_weight`       DECIMAL(5,4) NOT NULL DEFAULT 0.6000 COMMENT '规则权重',
  `rag_weight`        DECIMAL(5,4) NOT NULL DEFAULT 0.3000 COMMENT 'RAG权重',
  `llm_weight`        DECIMAL(5,4) NOT NULL DEFAULT 0.1000 COMMENT 'LLM权重',
  `high_threshold`    DECIMAL(5,4) NOT NULL DEFAULT 0.8500 COMMENT '高置信阈值',
  `medium_threshold`  DECIMAL(5,4) NOT NULL DEFAULT 0.6500 COMMENT '中置信阈值',
  `low_threshold`     DECIMAL(5,4) NOT NULL DEFAULT 0.0000 COMMENT '低置信阈值',
  `enabled`           TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用',
  `active_flag`       TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否当前生效',
  `updated_by`        VARCHAR(64)  DEFAULT NULL COMMENT '更新人工号',
  `updated_at`        DATETIME     DEFAULT NULL COMMENT '更新时间',
  `remark`            VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_active` (`active_flag`, `enabled`),
  KEY `idx_updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='AI置信度配置';

INSERT IGNORE INTO `qc_ai_confidence_config` (
  `id`, `config_name`, `rule_weight`, `rag_weight`, `llm_weight`,
  `high_threshold`, `medium_threshold`, `low_threshold`, `enabled`,
  `active_flag`, `updated_by`, `updated_at`, `create_user_no`, `create_date_time`
) VALUES (
  'ai_conf_default', '默认置信度配置', 0.6000, 0.3000, 0.1000,
  0.8500, 0.6500, 0.0000, 1,
  1, 'system', NOW(), 'system', NOW()
);

-- ────────────────────────────────────────────────────────────
-- Alternative qualified stock used by concession risk assessment.
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `alternative_stock` (
  `id`                VARCHAR(64)  NOT NULL COMMENT '主键',
  `company_id`        VARCHAR(64)  DEFAULT NULL COMMENT '公司ID',
  `create_user_no`    VARCHAR(64)  DEFAULT NULL COMMENT '创建人工号',
  `update_user_no`    VARCHAR(64)  DEFAULT NULL COMMENT '修改人工号',
  `create_date_time`  VARCHAR(32)  DEFAULT NULL COMMENT '创建时间',
  `update_date_time`  VARCHAR(32)  DEFAULT NULL COMMENT '修改时间',
  `variety`           VARCHAR(100) NOT NULL COMMENT '品种',
  `grade`             VARCHAR(100) NOT NULL COMMENT '牌号',
  `spec_range`        VARCHAR(200) NOT NULL COMMENT '规格范围',
  `coil_no`           VARCHAR(64)  DEFAULT NULL COMMENT '卷号',
  `batch_no`          VARCHAR(64)  DEFAULT NULL COMMENT '批次号',
  `available_weight`  DECIMAL(20,6) DEFAULT NULL COMMENT '可用重量',
  `location`          VARCHAR(100) DEFAULT NULL COMMENT '库位',
  `status`            VARCHAR(30)  NOT NULL DEFAULT 'AVAILABLE' COMMENT '状态 AVAILABLE/RESERVED/LOCKED',
  `earliest_ship_date` DATE        DEFAULT NULL COMMENT '最早可发运日期',
  `remark`            VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_stock_scope` (`variety`, `grade`, `status`),
  KEY `idx_coil_batch` (`coil_no`, `batch_no`),
  KEY `idx_ship_date` (`earliest_ship_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='让步替代资源库存';

-- ────────────────────────────────────────────────────────────
-- Customer usage profile for concession risk defaults.
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `qc_customer_usage_profile` (
  `id`                VARCHAR(64)  NOT NULL COMMENT '主键',
  `company_id`        VARCHAR(64)  DEFAULT NULL COMMENT '公司ID',
  `create_user_no`    VARCHAR(64)  DEFAULT NULL COMMENT '创建人工号',
  `update_user_no`    VARCHAR(64)  DEFAULT NULL COMMENT '修改人工号',
  `create_date_time`  VARCHAR(32)  DEFAULT NULL COMMENT '创建时间',
  `update_date_time`  VARCHAR(32)  DEFAULT NULL COMMENT '修改时间',
  `customer_id`       VARCHAR(64)  NOT NULL COMMENT '客户ID',
  `customer_name`     VARCHAR(200) DEFAULT NULL COMMENT '客户名称',
  `default_usage`     VARCHAR(200) NOT NULL COMMENT '默认用途',
  `risk_category`     VARCHAR(30)  NOT NULL DEFAULT 'NORMAL' COMMENT '用途风险 NORMAL/HIGH_FORMING/SAFETY_CRITICAL',
  `variety`           VARCHAR(100) DEFAULT NULL COMMENT '适用品种',
  `grade`             VARCHAR(100) DEFAULT NULL COMMENT '适用牌号',
  `status`            VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT '状态 ACTIVE/INACTIVE',
  `remark`            VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_customer` (`customer_id`, `status`),
  KEY `idx_usage_scope` (`customer_id`, `variety`, `grade`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='客户用途画像';

-- ────────────────────────────────────────────────────────────
-- Evaluation cases for business, citation, confidence and safety checks.
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `qc_evaluation_case` (
  `id`                       VARCHAR(64)  NOT NULL COMMENT '主键',
  `company_id`               VARCHAR(64)  DEFAULT NULL COMMENT '公司ID',
  `create_user_no`           VARCHAR(64)  DEFAULT NULL COMMENT '创建人工号',
  `update_user_no`           VARCHAR(64)  DEFAULT NULL COMMENT '修改人工号',
  `create_date_time`         VARCHAR(32)  DEFAULT NULL COMMENT '创建时间',
  `update_date_time`         VARCHAR(32)  DEFAULT NULL COMMENT '修改时间',
  `case_code`                VARCHAR(80)  NOT NULL COMMENT '评测用例编码',
  `case_group`               VARCHAR(50)  NOT NULL COMMENT 'NORMAL/BOUNDARY/LOW_CONFIDENCE/SAFETY',
  `scenario_type`            VARCHAR(50)  NOT NULL COMMENT '场景类型',
  `title`                    VARCHAR(200) NOT NULL COMMENT '标题',
  `input_payload`            LONGTEXT     NOT NULL COMMENT '输入数据(JSON)',
  `expected_judgment_type`   VARCHAR(30)  DEFAULT NULL COMMENT '期望判定结论',
  `expected_citation_ids`    TEXT         DEFAULT NULL COMMENT '期望引用ID(JSON数组)',
  `expected_refusal`         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否期望拒答',
  `expected_confidence_label` VARCHAR(20) DEFAULT NULL COMMENT '期望置信度',
  `numeric_tolerance`        DECIMAL(20,6) DEFAULT NULL COMMENT '数值容差',
  `response_time_target_ms`  INT          DEFAULT NULL COMMENT '响应时间目标毫秒',
  `prompt_injection_flag`    TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否Prompt注入安全场景',
  `enabled`                  TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用',
  `remark`                   VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_case_code` (`case_code`),
  KEY `idx_case_group` (`case_group`, `enabled`),
  KEY `idx_scenario_type` (`scenario_type`),
  KEY `idx_expected_judgment` (`expected_judgment_type`),
  KEY `idx_prompt_injection` (`prompt_injection_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='AI质量增强评测用例';
