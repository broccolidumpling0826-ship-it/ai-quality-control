-- ============================================================
-- Add AI quality P0 menu and RBAC entries for existing databases.
-- MySQL 5.7 compatible.
-- ============================================================

USE ai_quality_control;

INSERT INTO `sys_permission` (`id`, `perm_code`, `perm_name`, `perm_type`, `status`, `create_date_time`)
VALUES
  ('perm025', 'menu:standard-rag',        '标准RAG检索',    'MENU', 1, '2026-06-21 00:00:00'),
  ('perm026', 'standard-rag:query',       '标准RAG查询',    'API',  1, '2026-06-21 00:00:00'),
  ('perm027', 'menu:standard-conflict',   '标准冲突检测',   'MENU', 1, '2026-06-21 00:00:00'),
  ('perm028', 'standard-conflict:resolve','标准冲突裁决',   'API',  1, '2026-06-21 00:00:00'),
  ('perm029', 'menu:cert-qa',             '质保书问答',     'MENU', 1, '2026-06-21 00:00:00'),
  ('perm030', 'cert:qa',                  '质保书问答接口', 'API',  1, '2026-06-21 00:00:00'),
  ('perm031', 'menu:ai-assessment',       'AI评估审计',     'MENU', 1, '2026-06-21 00:00:00'),
  ('perm032', 'ai-assessment:review',     'AI评估审阅',     'API',  1, '2026-06-21 00:00:00'),
  ('perm033', 'menu:ai-confidence',       'AI置信度配置',   'MENU', 1, '2026-06-21 00:00:00'),
  ('perm034', 'ai-confidence:manage',     'AI置信度管理',   'API',  1, '2026-06-21 00:00:00')
ON DUPLICATE KEY UPDATE
  `perm_name` = VALUES(`perm_name`),
  `perm_type` = VALUES(`perm_type`),
  `status` = VALUES(`status`);

INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_type`, `menu_name`, `path`, `component`, `route_name`, `icon`, `perm_code`, `visible`, `sort_order`, `status`, `create_date_time`)
VALUES
  ('menu014', 'menu010', 'MENU',   '标准RAG检索', 'standard-rag',              'standard-rag/index',       'standard-rag',            'Document', 'menu:standard-rag',      1, 24,  1, '2026-06-21 00:00:00'),
  ('menu034', 'menu030', 'MENU',   '标准冲突检测','standard-conflicts',        'standard-conflicts/index', 'standard-conflicts',      'Warning',  'menu:standard-conflict', 1, 44,  1, '2026-06-21 00:00:00'),
  ('menu043', 'menu040', 'MENU',   '质保书问答',  'cert-data/qa',              'cert-data/qa',             'cert-data-qa',            'Tickets',  'menu:cert-qa',           1, 52,  1, '2026-06-21 00:00:00'),
  ('menu065', 'menu060', 'MENU',   'AI评估审计', 'ai-assessments',            'ai-assessments/index',     'ai-assessments',          'Tickets',  'menu:ai-assessment',     1, 75,  1, '2026-06-21 00:00:00'),
  ('menu066', 'menu060', 'MENU',   'AI置信度配置','admin/ai-confidence',      'admin/ai-confidence/index','admin-ai-confidence',     'Setting',  'menu:ai-confidence',     1, 76,  1, '2026-06-21 00:00:00'),
  ('menu108', '0',       'HIDDEN', '标准冲突详情','standard-conflicts/detail', 'standard-conflicts/detail','standard-conflicts-detail','Warning',  'menu:standard-conflict', 0, 108, 1, '2026-06-21 00:00:00'),
  ('menu109', '0',       'HIDDEN', '标准冲突裁决','standard-conflicts/resolve','standard-conflicts/detail','standard-conflicts-resolve','Warning', 'menu:standard-conflict', 0, 109, 1, '2026-06-21 00:00:00')
ON DUPLICATE KEY UPDATE
  `parent_id` = VALUES(`parent_id`),
  `menu_type` = VALUES(`menu_type`),
  `menu_name` = VALUES(`menu_name`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `route_name` = VALUES(`route_name`),
  `icon` = VALUES(`icon`),
  `perm_code` = VALUES(`perm_code`),
  `visible` = VALUES(`visible`),
  `sort_order` = VALUES(`sort_order`),
  `status` = VALUES(`status`);

UPDATE `sys_menu`
SET `sort_order` = 53
WHERE `id` = 'menu042'
  AND `sort_order` < 53;

INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`)
VALUES
  ('rp_ai_001', 'role001', 'perm025'), ('rp_ai_002', 'role001', 'perm026'),
  ('rp_ai_003', 'role001', 'perm027'), ('rp_ai_004', 'role001', 'perm029'),
  ('rp_ai_005', 'role001', 'perm030'),
  ('rp_ai_011', 'role002', 'perm025'), ('rp_ai_012', 'role002', 'perm026'),
  ('rp_ai_013', 'role002', 'perm027'), ('rp_ai_014', 'role002', 'perm029'),
  ('rp_ai_015', 'role002', 'perm030'),
  ('rp_ai_021', 'role003', 'perm025'), ('rp_ai_022', 'role003', 'perm026'),
  ('rp_ai_023', 'role003', 'perm027'), ('rp_ai_024', 'role003', 'perm028'),
  ('rp_ai_025', 'role003', 'perm029'), ('rp_ai_026', 'role003', 'perm030'),
  ('rp_ai_027', 'role003', 'perm031'), ('rp_ai_028', 'role003', 'perm032'),
  ('rp_ai_031', 'role004', 'perm025'), ('rp_ai_032', 'role004', 'perm026'),
  ('rp_ai_033', 'role004', 'perm027'), ('rp_ai_034', 'role004', 'perm029'),
  ('rp_ai_035', 'role004', 'perm030'),
  ('rp_ai_041', 'role005', 'perm025'), ('rp_ai_042', 'role005', 'perm026'),
  ('rp_ai_043', 'role005', 'perm027'), ('rp_ai_044', 'role005', 'perm028'),
  ('rp_ai_045', 'role005', 'perm029'), ('rp_ai_046', 'role005', 'perm030'),
  ('rp_ai_047', 'role005', 'perm031'), ('rp_ai_048', 'role005', 'perm032'),
  ('rp_ai_049', 'role005', 'perm033'), ('rp_ai_050', 'role005', 'perm034');

INSERT IGNORE INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT `id`, `role_id`, `menu_id` FROM (
  SELECT CONCAT('rm_ai_biz_', r.role_id, '_', m.menu_id) AS `id`, r.role_id, m.menu_id
  FROM (
    SELECT 'role001' AS role_id UNION SELECT 'role002' UNION SELECT 'role003' UNION SELECT 'role004'
  ) r
  CROSS JOIN (
    SELECT 'menu014' AS menu_id UNION SELECT 'menu034' UNION SELECT 'menu043'
    UNION SELECT 'menu108' UNION SELECT 'menu109'
  ) m
) AS seed_ai_role_menu;

INSERT IGNORE INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT CONCAT('rm_admin_', id), 'role005', id
FROM `sys_menu`
WHERE `id` IN ('menu014', 'menu034', 'menu043', 'menu108', 'menu109');
