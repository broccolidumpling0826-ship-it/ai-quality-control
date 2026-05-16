-- 为已有库补充指标项目 status / test_method 字段（可重复执行）
USE ai_quality_control;

SET @db := DATABASE();

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'qc_indicator_item' AND COLUMN_NAME = 'test_method') = 0,
    'ALTER TABLE qc_indicator_item ADD COLUMN test_method VARCHAR(200) DEFAULT NULL COMMENT ''检测方法'' AFTER unit',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'qc_indicator_item' AND COLUMN_NAME = 'status') = 0,
    'ALTER TABLE qc_indicator_item ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT ''ACTIVE'' COMMENT ''状态 ACTIVE/INACTIVE'' AFTER description',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE qc_indicator_item SET status = 'ACTIVE' WHERE status IS NULL OR status = '';
