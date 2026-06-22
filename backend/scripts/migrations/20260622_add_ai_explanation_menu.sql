-- 注册 AI 判定解释隐藏页路由（修复 /judgment/ai-explanation 404）
-- mysql -h127.0.0.1 -P3307 -uai_quality_control -p --default-character-set=utf8mb4 ai_quality_control < 本文件

USE ai_quality_control;

SET NAMES utf8mb4;

INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_type`, `menu_name`, `path`, `component`, `route_name`, `icon`, `perm_code`, `visible`, `sort_order`, `status`, `create_date_time`)
VALUES
  ('menu108', '0', 'HIDDEN', 'AI 判定解释', 'judgment/ai-explanation', 'judgment/ai-explanation', 'judgment-ai-explanation', 'Stamp', 'menu:judgment', 0, 108, 1, NOW())
ON DUPLICATE KEY UPDATE
  menu_name  = VALUES(menu_name),
  path       = VALUES(path),
  component  = VALUES(component),
  route_name = VALUES(route_name),
  perm_code  = VALUES(perm_code);

-- 业务角色授权
INSERT IGNORE INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT CONCAT('rm_biz_', r.role_id, '_menu108'), r.role_id, 'menu108'
FROM (
  SELECT 'role001' AS role_id UNION SELECT 'role002' UNION SELECT 'role003' UNION SELECT 'role004'
) r;

-- ADMIN 授权
INSERT IGNORE INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
VALUES ('rm_admin_menu108', 'role005', 'menu108');
