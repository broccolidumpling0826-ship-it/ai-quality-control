-- 质量标准表新增标准编号、标准名称字段
USE ai_quality_control;

-- MySQL 5.7 不支持 ADD COLUMN IF NOT EXISTS。
-- 使用 information_schema 做幂等检查，兼容新库 baseline 已包含字段、旧库未包含字段两种情况。
DROP PROCEDURE IF EXISTS add_standard_code_name_columns;

DELIMITER $$
CREATE PROCEDURE add_standard_code_name_columns()
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'qc_quality_standard'
      AND column_name = 'standard_code'
  ) THEN
    ALTER TABLE qc_quality_standard
      ADD COLUMN standard_code VARCHAR(100) DEFAULT NULL COMMENT '标准编号' AFTER standard_type;
  END IF;

  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'qc_quality_standard'
      AND column_name = 'standard_name'
  ) THEN
    ALTER TABLE qc_quality_standard
      ADD COLUMN standard_name VARCHAR(200) DEFAULT NULL COMMENT '标准名称' AFTER standard_code;
  END IF;
END$$
DELIMITER ;

CALL add_standard_code_name_columns();

DROP PROCEDURE IF EXISTS add_standard_code_name_columns;

-- 历史数据回填
UPDATE qc_quality_standard SET standard_code = version_no WHERE standard_code IS NULL OR standard_code = '';
UPDATE qc_quality_standard SET standard_name = remark WHERE (standard_name IS NULL OR standard_name = '') AND remark IS NOT NULL AND remark != '';
