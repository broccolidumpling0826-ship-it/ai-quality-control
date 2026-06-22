-- ============================================================
-- Fix standard conflict detection menu routes (P0).
-- Restores menu034/menu108 per rag-explained-judgment-concession spec.
-- MySQL 5.7 compatible.
-- ============================================================

USE ai_quality_control;

UPDATE `sys_menu`
SET
  `parent_id` = 'menu030',
  `menu_type` = 'MENU',
  `menu_name` = CONVERT(UNHEX('E6A087E58786E586B2E7AA81E6A380E6B58B') USING utf8mb4),
  `path` = 'standard-conflicts',
  `component` = 'standard-conflicts/index',
  `route_name` = 'standard-conflicts',
  `icon` = 'Warning',
  `perm_code` = 'menu:standard-conflict',
  `visible` = 1,
  `sort_order` = 44,
  `status` = 1
WHERE `id` = 'menu034';

UPDATE `sys_menu`
SET
  `parent_id` = '0',
  `menu_type` = 'HIDDEN',
  `menu_name` = CONVERT(UNHEX('E6A087E58786E586B2E7AA81E8AFA6E68385') USING utf8mb4),
  `path` = 'standard-conflicts/detail',
  `component` = 'standard-conflicts/detail',
  `route_name` = 'standard-conflicts-detail',
  `icon` = 'Warning',
  `perm_code` = 'menu:standard-conflict',
  `visible` = 0,
  `sort_order` = 108,
  `status` = 1
WHERE `id` = 'menu108';

INSERT IGNORE INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT CONCAT('rm_ai_fix_', r.role_id, '_', m.menu_id), r.role_id, m.menu_id
FROM (
  SELECT 'role001' AS role_id UNION SELECT 'role002' UNION SELECT 'role003' UNION SELECT 'role004'
) r
CROSS JOIN (
  SELECT 'menu034' AS menu_id UNION SELECT 'menu108' UNION SELECT 'menu109'
) m;

INSERT IGNORE INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT CONCAT('rm_admin_fix_', id), 'role005', id
FROM `sys_menu`
WHERE `id` IN ('menu034', 'menu108', 'menu109');
