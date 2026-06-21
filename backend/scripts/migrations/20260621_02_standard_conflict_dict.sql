-- ============================================================
-- Add STANDARD_CONFLICT judgment dictionary item for existing databases.
-- MySQL 5.7 compatible.
-- ============================================================

USE ai_quality_control;

INSERT IGNORE INTO `sys_dict_item` (
  `id`, `dict_code`, `item_value`, `item_label`, `color_tag`, `sort_no`,
  `status`, `is_system`, `create_date_time`, `update_date_time`
) VALUES (
  'di035', 'JUDGMENT_TYPE', 'STANDARD_CONFLICT', '标准冲突', 'danger', 5,
  1, 1, NOW(), NOW()
);

UPDATE `sys_dict_item`
SET `color_tag` = 'warning',
    `sort_no` = 4,
    `update_date_time` = NOW()
WHERE `dict_code` = 'JUDGMENT_TYPE'
  AND `item_value` = 'CAN_CONCESSION';
