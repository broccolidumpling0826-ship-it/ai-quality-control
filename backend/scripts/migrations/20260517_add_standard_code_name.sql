-- 质量标准表新增标准编号、标准名称字段
USE ai_quality_control;

ALTER TABLE qc_quality_standard
  ADD COLUMN standard_code VARCHAR(100) DEFAULT NULL COMMENT '标准编号' AFTER standard_type,
  ADD COLUMN standard_name VARCHAR(200) DEFAULT NULL COMMENT '标准名称' AFTER standard_code;

-- 历史数据回填
UPDATE qc_quality_standard SET standard_code = version_no WHERE standard_code IS NULL OR standard_code = '';
UPDATE qc_quality_standard SET standard_name = remark WHERE (standard_name IS NULL OR standard_name = '') AND remark IS NOT NULL AND remark != '';
