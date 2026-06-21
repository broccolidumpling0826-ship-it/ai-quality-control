-- Quick fix when legacy file_* columns already exist and are NOT NULL.
-- No stored procedures. Run line-by-line in MySQL client / DataGrip if some columns are missing.

USE ai_quality_control;

ALTER TABLE `qc_standard_document`
  MODIFY COLUMN `file_name` VARCHAR(255) DEFAULT NULL COMMENT '旧版原始文件名';

ALTER TABLE `qc_standard_document`
  MODIFY COLUMN `file_path` VARCHAR(500) DEFAULT NULL COMMENT '旧版原始文件路径';

ALTER TABLE `qc_standard_document`
  MODIFY COLUMN `file_type` VARCHAR(50) DEFAULT NULL COMMENT '旧版文件类型';

ALTER TABLE `qc_standard_document`
  MODIFY COLUMN `file_size` BIGINT DEFAULT NULL COMMENT '旧版文件大小(字节)';

UPDATE qc_standard_document
SET file_type = 'pdf'
WHERE (file_type IS NULL OR file_type = '')
  AND (source_file_name LIKE '%.pdf' OR file_name LIKE '%.pdf');
