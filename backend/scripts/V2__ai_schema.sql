-- ============================================================
-- 002 AI/RAG 增量 DDL — MySQL 5.7.43
-- 执行：mysql -u user -p ai_quality_control < backend/scripts/V2__ai_schema.sql
-- ============================================================
USE ai_quality_control;

CREATE TABLE IF NOT EXISTS `qc_standard_document` (
  `id`               VARCHAR(64)  NOT NULL,
  `company_id`       VARCHAR(64)  DEFAULT NULL,
  `create_user_no`   VARCHAR(64)  DEFAULT NULL,
  `update_user_no`   VARCHAR(64)  DEFAULT NULL,
  `create_date_time` VARCHAR(32)  DEFAULT NULL,
  `update_date_time` VARCHAR(32)  DEFAULT NULL,
  `standard_id`      VARCHAR(64)  NOT NULL,
  `file_name`        VARCHAR(255) NOT NULL,
  `file_path`        VARCHAR(500) NOT NULL,
  `file_type`        VARCHAR(20)  NOT NULL,
  `page_count`       INT          DEFAULT NULL,
  `ingest_status`    VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
  `ingest_time`      VARCHAR(32)  DEFAULT NULL,
  `chunk_count`      INT          DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_std_doc_standard_id` (`standard_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS `qc_standard_document_chunk` (
  `id`               VARCHAR(64)  NOT NULL,
  `company_id`       VARCHAR(64)  DEFAULT NULL,
  `create_user_no`   VARCHAR(64)  DEFAULT NULL,
  `update_user_no`   VARCHAR(64)  DEFAULT NULL,
  `create_date_time` VARCHAR(32)  DEFAULT NULL,
  `update_date_time` VARCHAR(32)  DEFAULT NULL,
  `document_id`      VARCHAR(64)  NOT NULL,
  `standard_id`      VARCHAR(64)  NOT NULL,
  `chunk_index`      INT          NOT NULL,
  `section_ref`      VARCHAR(100) DEFAULT NULL,
  `chunk_text`       TEXT         NOT NULL,
  `keyword_tags`     VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_chunk_document` (`document_id`, `chunk_index`),
  KEY `idx_chunk_standard_id` (`standard_id`),
  FULLTEXT KEY `idx_chunk_ft` (`chunk_text`, `keyword_tags`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS `qc_standard_conflict` (
  `id`               VARCHAR(64)  NOT NULL,
  `company_id`       VARCHAR(64)  DEFAULT NULL,
  `create_user_no`   VARCHAR(64)  DEFAULT NULL,
  `update_user_no`   VARCHAR(64)  DEFAULT NULL,
  `create_date_time` VARCHAR(32)  DEFAULT NULL,
  `update_date_time` VARCHAR(32)  DEFAULT NULL,
  `variety`          VARCHAR(100) NOT NULL,
  `grade`            VARCHAR(100) NOT NULL,
  `indicator_id`     VARCHAR(64)  NOT NULL,
  `standard_id_a`    VARCHAR(64)  NOT NULL,
  `standard_id_b`    VARCHAR(64)  NOT NULL,
  `limit_a_upper`    DECIMAL(20,6) DEFAULT NULL,
  `limit_a_lower`    DECIMAL(20,6) DEFAULT NULL,
  `limit_b_upper`    DECIMAL(20,6) DEFAULT NULL,
  `limit_b_lower`    DECIMAL(20,6) DEFAULT NULL,
  `conflict_status`  VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
  `resolution_note`  VARCHAR(500) DEFAULT NULL,
  `resolved_by`      VARCHAR(64)  DEFAULT NULL,
  `resolved_time`    VARCHAR(32)  DEFAULT NULL,
  `demo_flag`        TINYINT(1)   DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_conflict_lookup` (`variety`, `grade`, `indicator_id`, `conflict_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS `qc_ai_prompt_version` (
  `id`               VARCHAR(64)  NOT NULL,
  `company_id`       VARCHAR(64)  DEFAULT NULL,
  `create_user_no`   VARCHAR(64)  DEFAULT NULL,
  `update_user_no`   VARCHAR(64)  DEFAULT NULL,
  `create_date_time` VARCHAR(32)  DEFAULT NULL,
  `update_date_time` VARCHAR(32)  DEFAULT NULL,
  `prompt_key`       VARCHAR(50)  NOT NULL,
  `version_no`       VARCHAR(20)  NOT NULL,
  `file_path`        VARCHAR(200) NOT NULL,
  `is_active`        TINYINT(1)   DEFAULT 0,
  `description`      VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_prompt_version` (`prompt_key`, `version_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS `qc_ai_call_audit_log` (
  `id`                VARCHAR(64)  NOT NULL,
  `company_id`        VARCHAR(64)  DEFAULT NULL,
  `create_user_no`    VARCHAR(64)  DEFAULT NULL,
  `update_user_no`    VARCHAR(64)  DEFAULT NULL,
  `create_date_time`  VARCHAR(32)  DEFAULT NULL,
  `update_date_time`  VARCHAR(32)  DEFAULT NULL,
  `call_source`       VARCHAR(30)  NOT NULL,
  `prompt_key`        VARCHAR(50)  DEFAULT NULL,
  `prompt_version`    VARCHAR(20)  DEFAULT NULL,
  `model_name`        VARCHAR(50)  DEFAULT NULL,
  `input_summary`     VARCHAR(2000) DEFAULT NULL,
  `output_summary`    VARCHAR(2000) DEFAULT NULL,
  `citation_ids`      VARCHAR(500) DEFAULT NULL,
  `prompt_tokens`     INT          DEFAULT 0,
  `completion_tokens` INT          DEFAULT 0,
  `total_tokens`      INT          DEFAULT 0,
  `latency_ms`        INT          DEFAULT NULL,
  `success`           TINYINT(1)   NOT NULL DEFAULT 1,
  `degraded`          TINYINT(1)   DEFAULT 0,
  `error_type`        VARCHAR(30)  DEFAULT NULL,
  `trace_id`          VARCHAR(64)  DEFAULT NULL,
  `biz_ref_id`        VARCHAR(64)  DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_audit_time` (`create_date_time`),
  KEY `idx_audit_source` (`call_source`),
  KEY `idx_audit_trace` (`trace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS `qc_ai_judgment_explanation` (
  `id`                     VARCHAR(64) NOT NULL,
  `company_id`             VARCHAR(64) DEFAULT NULL,
  `create_user_no`         VARCHAR(64) DEFAULT NULL,
  `update_user_no`         VARCHAR(64) DEFAULT NULL,
  `create_date_time`       VARCHAR(32) DEFAULT NULL,
  `update_date_time`       VARCHAR(32) DEFAULT NULL,
  `judgment_id`            VARCHAR(64) NOT NULL,
  `narrative_text`         TEXT,
  `confidence_level`       VARCHAR(10) NOT NULL,
  `manual_review_required` TINYINT(1)  DEFAULT 0,
  `degraded`               TINYINT(1)  DEFAULT 0,
  `baseline_json`          TEXT,
  `citation_ids`           VARCHAR(500) DEFAULT NULL,
  `audit_log_id`           VARCHAR(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_explain_judgment` (`judgment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS `qc_concession_assessment` (
  `id`                   VARCHAR(64) NOT NULL,
  `company_id`           VARCHAR(64) DEFAULT NULL,
  `create_user_no`       VARCHAR(64) DEFAULT NULL,
  `update_user_no`       VARCHAR(64) DEFAULT NULL,
  `create_date_time`     VARCHAR(32) DEFAULT NULL,
  `update_date_time`     VARCHAR(32) DEFAULT NULL,
  `judgment_id`          VARCHAR(64) NOT NULL,
  `concession_id`        VARCHAR(64) DEFAULT NULL,
  `risk_level`           VARCHAR(10) NOT NULL,
  `customer_impact`      TEXT,
  `suggested_conditions` TEXT,
  `historical_cases`     TEXT,
  `confidence_level`     VARCHAR(10) DEFAULT NULL,
  `degraded`             TINYINT(1)  DEFAULT 0,
  `audit_log_id`         VARCHAR(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_assessment_judgment` (`judgment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS `qc_ai_suggestion` (
  `id`                 VARCHAR(64) NOT NULL,
  `company_id`         VARCHAR(64) DEFAULT NULL,
  `create_user_no`     VARCHAR(64) DEFAULT NULL,
  `update_user_no`     VARCHAR(64) DEFAULT NULL,
  `create_date_time`   VARCHAR(32) DEFAULT NULL,
  `update_date_time`   VARCHAR(32) DEFAULT NULL,
  `suggestion_type`    VARCHAR(20) NOT NULL,
  `ref_id`             VARCHAR(64) NOT NULL,
  `recommended_action` VARCHAR(20) NOT NULL,
  `focus_indicators`   VARCHAR(500) DEFAULT NULL,
  `reason_text`        TEXT,
  `confidence_level`   VARCHAR(10) DEFAULT NULL,
  `degraded`           TINYINT(1)  DEFAULT 0,
  `audit_log_id`       VARCHAR(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_suggestion_ref` (`suggestion_type`, `ref_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS `qc_evaluation_sample` (
  `id`               VARCHAR(64) NOT NULL,
  `company_id`       VARCHAR(64) DEFAULT NULL,
  `create_user_no`   VARCHAR(64) DEFAULT NULL,
  `update_user_no`   VARCHAR(64) DEFAULT NULL,
  `create_date_time` VARCHAR(32) DEFAULT NULL,
  `update_date_time` VARCHAR(32) DEFAULT NULL,
  `sample_code`      VARCHAR(50) NOT NULL,
  `category`         VARCHAR(30) NOT NULL,
  `input_payload`    TEXT        NOT NULL,
  `expected_outcome` TEXT        NOT NULL,
  `weight`           DECIMAL(5,2) DEFAULT 1.00,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_eval_sample_code` (`sample_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS `qc_evaluation_run` (
  `id`                     VARCHAR(64) NOT NULL,
  `company_id`             VARCHAR(64) DEFAULT NULL,
  `create_user_no`         VARCHAR(64) DEFAULT NULL,
  `update_user_no`         VARCHAR(64) DEFAULT NULL,
  `create_date_time`       VARCHAR(32) DEFAULT NULL,
  `update_date_time`       VARCHAR(32) DEFAULT NULL,
  `run_no`                 VARCHAR(50) NOT NULL,
  `prompt_version`         VARCHAR(20) DEFAULT NULL,
  `model_name`             VARCHAR(50) DEFAULT NULL,
  `status`                 VARCHAR(20) NOT NULL,
  `total_samples`          INT         DEFAULT NULL,
  `passed_samples`         INT         DEFAULT NULL,
  `rule_pass_rate`         DECIMAL(5,2) DEFAULT NULL,
  `ai_accuracy_rate`       DECIMAL(5,2) DEFAULT NULL,
  `citation_hit_rate`      DECIMAL(5,2) DEFAULT NULL,
  `avg_latency_ms`         INT         DEFAULT NULL,
  `manual_review_hit_rate` DECIMAL(5,2) DEFAULT NULL,
  `report_json`            TEXT,
  `finished_time`          VARCHAR(32) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_eval_run_no` (`run_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

-- MySQL 5.7 无 ADD COLUMN IF NOT EXISTS；重复执行若报 Duplicate column 可忽略
ALTER TABLE `qc_quality_cert_data`
  ADD COLUMN `ai_summary_text` TEXT DEFAULT NULL COMMENT 'AI 生成质保书说明';
