-- Legacy qc_standard_document columns compatibility (file_name/file_path/file_type/file_size)
-- Self-contained for MySQL 5.7: includes helper procedure (no dependency on 20260621_01).

USE ai_quality_control;

DROP PROCEDURE IF EXISTS add_column_if_missing;

DELIMITER $$
CREATE PROCEDURE add_column_if_missing(
  IN p_table_name VARCHAR(64),
  IN p_column_name VARCHAR(64),
  IN p_column_definition TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name
      AND column_name = p_column_name
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` ADD COLUMN ', p_column_definition);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;

CALL add_column_if_missing(
    'qc_standard_document',
    'file_name',
    '`file_name` VARCHAR(255) DEFAULT NULL COMMENT ''旧版原始文件名'' AFTER `expiry_date`'
);
CALL add_column_if_missing(
    'qc_standard_document',
    'file_path',
    '`file_path` VARCHAR(500) DEFAULT NULL COMMENT ''旧版原始文件路径'' AFTER `file_name`'
);
CALL add_column_if_missing(
    'qc_standard_document',
    'file_type',
    '`file_type` VARCHAR(50) DEFAULT NULL COMMENT ''旧版文件类型'' AFTER `file_path`'
);
CALL add_column_if_missing(
    'qc_standard_document',
    'file_size',
    '`file_size` BIGINT DEFAULT NULL COMMENT ''旧版文件大小(字节)'' AFTER `file_type`'
);

UPDATE qc_standard_document
SET file_name = source_file_name
WHERE (file_name IS NULL OR file_name = '' OR file_name = 'pending')
  AND source_file_name IS NOT NULL;

UPDATE qc_standard_document
SET file_path = source_file_path
WHERE (file_path IS NULL OR file_path = '' OR file_path = 'pending')
  AND source_file_path IS NOT NULL;

UPDATE qc_standard_document
SET file_type = 'pdf'
WHERE (file_type IS NULL OR file_type = '')
  AND source_file_name LIKE '%.pdf';

-- Relax NOT NULL on legacy columns when they still exist.
SET @sql = (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'qc_standard_document'
              AND COLUMN_NAME = 'file_name'
              AND IS_NULLABLE = 'NO'
        ),
        'ALTER TABLE `qc_standard_document` MODIFY COLUMN `file_name` VARCHAR(255) DEFAULT NULL COMMENT ''旧版原始文件名''',
        'SELECT 1'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'qc_standard_document'
              AND COLUMN_NAME = 'file_path'
              AND IS_NULLABLE = 'NO'
        ),
        'ALTER TABLE `qc_standard_document` MODIFY COLUMN `file_path` VARCHAR(500) DEFAULT NULL COMMENT ''旧版原始文件路径''',
        'SELECT 1'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'qc_standard_document'
              AND COLUMN_NAME = 'file_type'
              AND IS_NULLABLE = 'NO'
        ),
        'ALTER TABLE `qc_standard_document` MODIFY COLUMN `file_type` VARCHAR(50) DEFAULT NULL COMMENT ''旧版文件类型''',
        'SELECT 1'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'qc_standard_document'
              AND COLUMN_NAME = 'file_size'
              AND IS_NULLABLE = 'NO'
        ),
        'ALTER TABLE `qc_standard_document` MODIFY COLUMN `file_size` BIGINT DEFAULT NULL COMMENT ''旧版文件大小(字节)''',
        'SELECT 1'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
