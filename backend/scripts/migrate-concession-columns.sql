-- 为已有库补充让步接收表缺失字段（可重复执行）
USE ai_quality_control;

SET @db := DATABASE();

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'qc_concession_acceptance' AND COLUMN_NAME = 'void_reason') = 0,
    'ALTER TABLE qc_concession_acceptance ADD COLUMN void_reason VARCHAR(500) DEFAULT NULL COMMENT ''失效/作废原因'' AFTER approval_status',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'qc_concession_acceptance' AND COLUMN_NAME = 'last_reminder_time') = 0,
    'ALTER TABLE qc_concession_acceptance ADD COLUMN last_reminder_time DATETIME DEFAULT NULL COMMENT ''最后催确认时间'' AFTER void_reason',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
