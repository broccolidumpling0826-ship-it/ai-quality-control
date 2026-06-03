-- ============================================================
-- 动态菜单 + RBAC 权限体系 — DDL 与 Seed 数据
-- MySQL 5.7.43 | utf8mb4_unicode_ci
-- 执行前请确保已运行 init-schema.sql
-- ============================================================

USE ai_quality_control;

-- ────────────────────────────────────────────────────────────
-- 1. DDL：角色 / 权限 / 菜单 / 关联表
-- ────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS `sys_role` (
  `id`               VARCHAR(64)  NOT NULL COMMENT '主键',
  `company_id`       VARCHAR(64)  DEFAULT NULL,
  `create_user_no`   VARCHAR(64)  DEFAULT NULL,
  `update_user_no`   VARCHAR(64)  DEFAULT NULL,
  `create_date_time` VARCHAR(32)  DEFAULT NULL,
  `update_date_time` VARCHAR(32)  DEFAULT NULL,
  `role_code`        VARCHAR(50)  NOT NULL COMMENT '角色编码',
  `role_name`        VARCHAR(100) NOT NULL COMMENT '角色名称',
  `description`      VARCHAR(500) DEFAULT NULL,
  `sort_order`       INT          NOT NULL DEFAULT 0,
  `status`           TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色';

CREATE TABLE IF NOT EXISTS `sys_permission` (
  `id`               VARCHAR(64)  NOT NULL COMMENT '主键',
  `company_id`       VARCHAR(64)  DEFAULT NULL,
  `create_user_no`   VARCHAR(64)  DEFAULT NULL,
  `update_user_no`   VARCHAR(64)  DEFAULT NULL,
  `create_date_time` VARCHAR(32)  DEFAULT NULL,
  `update_date_time` VARCHAR(32)  DEFAULT NULL,
  `perm_code`        VARCHAR(100) NOT NULL COMMENT '权限编码',
  `perm_name`        VARCHAR(200) NOT NULL COMMENT '权限名称',
  `perm_type`        VARCHAR(20)  NOT NULL DEFAULT 'BUTTON' COMMENT 'MENU/BUTTON/API',
  `description`      VARCHAR(500) DEFAULT NULL,
  `status`           TINYINT(1)   NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_perm_code` (`perm_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统权限';

CREATE TABLE IF NOT EXISTS `sys_menu` (
  `id`               VARCHAR(64)  NOT NULL COMMENT '主键',
  `company_id`       VARCHAR(64)  DEFAULT NULL,
  `create_user_no`   VARCHAR(64)  DEFAULT NULL,
  `update_user_no`   VARCHAR(64)  DEFAULT NULL,
  `create_date_time` VARCHAR(32)  DEFAULT NULL,
  `update_date_time` VARCHAR(32)  DEFAULT NULL,
  `parent_id`        VARCHAR(64)  NOT NULL DEFAULT '0' COMMENT '父菜单ID，0为根',
  `menu_type`        VARCHAR(20)  NOT NULL COMMENT 'DIR/MENU/HIDDEN/LINK/IFRAME',
  `menu_name`        VARCHAR(100) NOT NULL COMMENT '菜单名称',
  `path`             VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
  `component`        VARCHAR(200) DEFAULT NULL COMMENT 'Vue组件相对路径',
  `route_name`       VARCHAR(100) DEFAULT NULL COMMENT 'Vue Router name',
  `icon`             VARCHAR(50)  DEFAULT NULL,
  `perm_code`        VARCHAR(100) DEFAULT NULL COMMENT '关联权限编码',
  `visible`          TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '侧栏是否可见',
  `sort_order`       INT          NOT NULL DEFAULT 0,
  `status`           TINYINT(1)   NOT NULL DEFAULT 1,
  `meta_json`        TEXT         DEFAULT NULL COMMENT '扩展JSON',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统菜单';

CREATE TABLE IF NOT EXISTS `sys_role_menu` (
  `id`      VARCHAR(64) NOT NULL,
  `role_id` VARCHAR(64) NOT NULL,
  `menu_id` VARCHAR(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色-菜单关联';

CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `id`            VARCHAR(64) NOT NULL,
  `role_id`       VARCHAR(64) NOT NULL,
  `permission_id` VARCHAR(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_perm` (`role_id`, `permission_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色-权限关联';

CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id`      VARCHAR(64) NOT NULL,
  `user_id` VARCHAR(64) NOT NULL,
  `role_id` VARCHAR(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-角色关联';

-- ────────────────────────────────────────────────────────────
-- 2. Seed：角色（来自 USER_ROLE 字典）
-- ────────────────────────────────────────────────────────────

INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `description`, `sort_order`, `status`, `create_date_time`)
VALUES
  ('role001', 'QUALITY_ENGINEER',  '质量工程师', '负责标准维护与检验录入', 1, 1, '2026-06-03 00:00:00'),
  ('role002', 'QUALITY_SUPERVISOR', '质检主管',   '负责检验作废与复检审批', 2, 1, '2026-06-03 00:00:00'),
  ('role003', 'QUALITY_MANAGER',   '质量经理',   '负责改判与让步终审',     3, 1, '2026-06-03 00:00:00'),
  ('role004', 'SALES_MANAGER',     '销售经理',   '负责让步一审',           4, 1, '2026-06-03 00:00:00'),
  ('role005', 'ADMIN',             '系统管理员', '系统全部权限',           5, 1, '2026-06-03 00:00:00')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

-- ────────────────────────────────────────────────────────────
-- 3. Seed：权限点
-- ────────────────────────────────────────────────────────────

INSERT INTO `sys_permission` (`id`, `perm_code`, `perm_name`, `perm_type`, `status`, `create_date_time`)
VALUES
  ('perm001', 'menu:dashboard',        '质量工作台',     'MENU',   1, '2026-06-03 00:00:00'),
  ('perm002', 'menu:standard',           '标准库',         'MENU',   1, '2026-06-03 00:00:00'),
  ('perm003', 'menu:inspection',         '检验录入',       'MENU',   1, '2026-06-03 00:00:00'),
  ('perm004', 'menu:judgment',           '判定解释',       'MENU',   1, '2026-06-03 00:00:00'),
  ('perm005', 'menu:reinspection',       '复检管理',       'MENU',   1, '2026-06-03 00:00:00'),
  ('perm006', 'menu:rejudgment',         '改判管理',       'MENU',   1, '2026-06-03 00:00:00'),
  ('perm007', 'menu:concession',         '让步接收',       'MENU',   1, '2026-06-03 00:00:00'),
  ('perm008', 'menu:cert-data',          '质保书数据',     'MENU',   1, '2026-06-03 00:00:00'),
  ('perm009', 'menu:statistics',         '质量统计',       'MENU',   1, '2026-06-03 00:00:00'),
  ('perm010', 'menu:audit',              '权限审计',       'MENU',   1, '2026-06-03 00:00:00'),
  ('perm011', 'menu:admin:users',        '账号管理',       'MENU',   1, '2026-06-03 00:00:00'),
  ('perm012', 'menu:admin:dict',         '数据字典',       'MENU',   1, '2026-06-03 00:00:00'),
  ('perm013', 'menu:admin:menus',        '菜单管理',       'MENU',   1, '2026-06-03 00:00:00'),
  ('perm014', 'menu:admin:roles',        '角色管理',       'MENU',   1, '2026-06-03 00:00:00'),
  ('perm015', 'standard:manage',         '标准维护',       'BUTTON', 1, '2026-06-03 00:00:00'),
  ('perm016', 'indicator:manage',        '指标管理',       'BUTTON', 1, '2026-06-03 00:00:00'),
  ('perm017', 'inspection:void',         '检验作废',       'BUTTON', 1, '2026-06-03 00:00:00'),
  ('perm018', 'rejudgment:approve',      '改判审批',       'BUTTON', 1, '2026-06-03 00:00:00'),
  ('perm019', 'concession:approve:first','让步一审',       'BUTTON', 1, '2026-06-03 00:00:00'),
  ('perm020', 'concession:approve:final','让步终审',       'BUTTON', 1, '2026-06-03 00:00:00'),
  ('perm021', 'admin:user:manage',       '用户管理',       'API',    1, '2026-06-03 00:00:00'),
  ('perm022', 'admin:menu:manage',       '菜单管理',       'API',    1, '2026-06-03 00:00:00'),
  ('perm023', 'admin:role:manage',       '角色管理',       'API',    1, '2026-06-03 00:00:00'),
  ('perm024', 'admin:dict:manage',       '字典管理',       'API',    1, '2026-06-03 00:00:00')
ON DUPLICATE KEY UPDATE perm_name = VALUES(perm_name);

-- ────────────────────────────────────────────────────────────
-- 4. Seed：菜单树（来自 router/index.ts）
-- ────────────────────────────────────────────────────────────

INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_type`, `menu_name`, `path`, `component`, `route_name`, `icon`, `perm_code`, `visible`, `sort_order`, `status`, `create_date_time`)
VALUES
  ('menu001', '0',       'MENU',   '质量工作台', 'dashboard',              'dashboard/index',           'dashboard',              'DataBoard',    'menu:dashboard',  1, 10,  1, '2026-06-03 00:00:00'),
  ('menu010', '0',       'DIR',    '标准库',     NULL,                     NULL,                        NULL,                     NULL,           NULL,              1, 20,  1, '2026-06-03 00:00:00'),
  ('menu011', 'menu010', 'MENU',   '标准维护',   'standard-lib',           'standard-lib/index',        'standard-lib',           'Document',     'menu:standard',   1, 21,  1, '2026-06-03 00:00:00'),
  ('menu012', 'menu010', 'MENU',   '指标项目',   'standard-lib/indicators', 'indicator/index',           'standard-lib-indicators','TrendCharts',  'menu:standard',   1, 22,  1, '2026-06-03 00:00:00'),
  ('menu013', 'menu010', 'MENU',   '覆盖缺口',   'standard-lib/gaps',      'standard-lib/gaps',         'standard-lib-gaps',      'Warning',      'menu:standard',   1, 23,  1, '2026-06-03 00:00:00'),
  ('menu020', '0',       'DIR',    '检验与判定', NULL,                     NULL,                        NULL,                     NULL,           NULL,              1, 30,  1, '2026-06-03 00:00:00'),
  ('menu021', 'menu020', 'MENU',   '检验录入',   'inspection',             'inspection/index',          'inspection',             'EditPen',      'menu:inspection', 1, 31,  1, '2026-06-03 00:00:00'),
  ('menu022', 'menu020', 'MENU',   '判定解释',   'judgment',               'judgment/index',            'judgment',               'Stamp',        'menu:judgment',   1, 32,  1, '2026-06-03 00:00:00'),
  ('menu030', '0',       'DIR',    '质量流程',   NULL,                     NULL,                        NULL,                     NULL,           NULL,              1, 40,  1, '2026-06-03 00:00:00'),
  ('menu031', 'menu030', 'MENU',   '复检管理',   'reinspection',           'reinspection/index',        'reinspection',           'RefreshRight', 'menu:reinspection',1,41, 1, '2026-06-03 00:00:00'),
  ('menu032', 'menu030', 'MENU',   '改判管理',   're-judgment',            're-judgment/index',         're-judgment',            'Edit',         'menu:rejudgment', 1, 42,  1, '2026-06-03 00:00:00'),
  ('menu033', 'menu030', 'MENU',   '让步接收',   'concession',             'concession/index',          'concession',             'Check',        'menu:concession', 1, 43,  1, '2026-06-03 00:00:00'),
  ('menu040', '0',       'DIR',    '数据汇总',   NULL,                     NULL,                        NULL,                     NULL,           NULL,              1, 50,  1, '2026-06-03 00:00:00'),
  ('menu041', 'menu040', 'MENU',   '质保书数据', 'cert-data',              'cert-data/index',           'cert-data',              'Tickets',      'menu:cert-data',  1, 51,  1, '2026-06-03 00:00:00'),
  ('menu042', 'menu040', 'MENU',   '质量统计',   'statistics',             'statistics/index',          'statistics',             'PieChart',     'menu:statistics', 1, 52,  1, '2026-06-03 00:00:00'),
  ('menu050', '0',       'MENU',   '权限审计',   'audit',                  'audit/index',               'audit',                  'Lock',         'menu:audit',      1, 60,  1, '2026-06-03 00:00:00'),
  ('menu060', '0',       'DIR',    '管理员',     NULL,                     NULL,                        NULL,                     NULL,           NULL,              1, 70,  1, '2026-06-03 00:00:00'),
  ('menu061', 'menu060', 'MENU',   '账号管理',   'admin/users',            'admin/users/index',         'admin-users',            'User',         'menu:admin:users',1, 71,  1, '2026-06-03 00:00:00'),
  ('menu062', 'menu060', 'MENU',   '数据字典',   'admin/dict',             'admin/dict/index',          'admin-dict',             'List',         'menu:admin:dict', 1, 72,  1, '2026-06-03 00:00:00'),
  ('menu063', 'menu060', 'MENU',   '菜单管理',   'admin/menus',            'admin/menus/index',         'admin-menus',            'Menu',         'menu:admin:menus',1, 73,  1, '2026-06-03 00:00:00'),
  ('menu064', 'menu060', 'MENU',   '角色管理',   'admin/roles',            'admin/roles/index',         'admin-roles',            'Avatar',       'menu:admin:roles',1, 74,  1, '2026-06-03 00:00:00'),
  ('menu101', '0',       'HIDDEN', '新建检验',   'inspection/form',        'inspection/form',           'inspection-form',        'EditPen',      'menu:inspection', 0, 101, 1, '2026-06-03 00:00:00'),
  ('menu102', '0',       'HIDDEN', '判定详情',   'judgment/explanation',   'judgment/explanation',      'judgment-explanation',   'Stamp',        'menu:judgment',   0, 102, 1, '2026-06-03 00:00:00'),
  ('menu103', '0',       'HIDDEN', '发起改判',   're-judgment/form',       're-judgment/form',          're-judgment-form',       'Edit',         'menu:rejudgment', 0, 103, 1, '2026-06-03 00:00:00'),
  ('menu104', '0',       'HIDDEN', '改判详情',   're-judgment/detail',     're-judgment/detail',        're-judgment-detail',     'Edit',         'menu:rejudgment', 0, 104, 1, '2026-06-03 00:00:00'),
  ('menu105', '0',       'HIDDEN', '发起让步',   'concession/apply',       'concession/form',           'concession-apply',       'Check',        'menu:concession', 0, 105, 1, '2026-06-03 00:00:00'),
  ('menu106', '0',       'HIDDEN', '让步详情',   'concession/detail',      'concession/detail',         'concession-detail',      'Check',        'menu:concession', 0, 106, 1, '2026-06-03 00:00:00'),
  ('menu107', '0',       'HIDDEN', '内嵌页面',   'iframe/:id',             'iframe/index',              'iframe-page',            'Link',         NULL,              0, 107, 1, '2026-06-03 00:00:00')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- ────────────────────────────────────────────────────────────
-- 5. Seed：角色-权限默认映射
-- ────────────────────────────────────────────────────────────

INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`)
VALUES
  ('rp001', 'role001', 'perm001'), ('rp002', 'role001', 'perm002'), ('rp003', 'role001', 'perm003'),
  ('rp004', 'role001', 'perm004'), ('rp005', 'role001', 'perm005'), ('rp006', 'role001', 'perm006'),
  ('rp007', 'role001', 'perm007'), ('rp008', 'role001', 'perm008'), ('rp009', 'role001', 'perm009'),
  ('rp010', 'role001', 'perm010'), ('rp011', 'role001', 'perm015'), ('rp012', 'role001', 'perm016'),
  ('rp020', 'role002', 'perm001'), ('rp021', 'role002', 'perm003'), ('rp022', 'role002', 'perm004'),
  ('rp023', 'role002', 'perm005'), ('rp024', 'role002', 'perm006'), ('rp025', 'role002', 'perm007'),
  ('rp026', 'role002', 'perm008'), ('rp027', 'role002', 'perm009'), ('rp028', 'role002', 'perm010'),
  ('rp029', 'role002', 'perm017'),
  ('rp030', 'role003', 'perm001'), ('rp031', 'role003', 'perm004'), ('rp032', 'role003', 'perm005'),
  ('rp033', 'role003', 'perm006'), ('rp034', 'role003', 'perm007'), ('rp035', 'role003', 'perm008'),
  ('rp036', 'role003', 'perm009'), ('rp037', 'role003', 'perm010'), ('rp038', 'role003', 'perm018'),
  ('rp039', 'role003', 'perm020'),
  ('rp040', 'role004', 'perm001'), ('rp041', 'role004', 'perm007'), ('rp042', 'role004', 'perm019'),
  ('rp050', 'role005', 'perm001'), ('rp051', 'role005', 'perm002'), ('rp052', 'role005', 'perm003'),
  ('rp053', 'role005', 'perm004'), ('rp054', 'role005', 'perm005'), ('rp055', 'role005', 'perm006'),
  ('rp056', 'role005', 'perm007'), ('rp057', 'role005', 'perm008'), ('rp058', 'role005', 'perm009'),
  ('rp059', 'role005', 'perm010'), ('rp060', 'role005', 'perm011'), ('rp061', 'role005', 'perm012'),
  ('rp062', 'role005', 'perm013'), ('rp063', 'role005', 'perm014'), ('rp064', 'role005', 'perm015'),
  ('rp065', 'role005', 'perm016'), ('rp066', 'role005', 'perm017'), ('rp067', 'role005', 'perm018'),
  ('rp068', 'role005', 'perm019'), ('rp069', 'role005', 'perm020'), ('rp070', 'role005', 'perm021'),
  ('rp071', 'role005', 'perm022'), ('rp072', 'role005', 'perm023'), ('rp073', 'role005', 'perm024')
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- ────────────────────────────────────────────────────────────
-- 6. Seed：角色-菜单分配（业务角色全部业务菜单，ADMIN 含管理菜单）
-- ────────────────────────────────────────────────────────────

-- 业务菜单 IDs（不含 admin 目录及子项）
-- menu001-050, menu101-106

-- 业务角色 × 业务菜单（用子查询包裹，避免 MySQL 5.7 将 ON DUPLICATE 误判为 JOIN 条件）
INSERT IGNORE INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT `id`, `role_id`, `menu_id` FROM (
  SELECT CONCAT('rm_biz_', r.role_id, '_', m.menu_id) AS `id`, r.role_id, m.menu_id
  FROM (
    SELECT 'role001' AS role_id UNION SELECT 'role002' UNION SELECT 'role003' UNION SELECT 'role004'
  ) r
  CROSS JOIN (
    SELECT 'menu001' AS menu_id UNION SELECT 'menu010' UNION SELECT 'menu011' UNION SELECT 'menu012'
    UNION SELECT 'menu013' UNION SELECT 'menu020' UNION SELECT 'menu021' UNION SELECT 'menu022'
    UNION SELECT 'menu030' UNION SELECT 'menu031' UNION SELECT 'menu032' UNION SELECT 'menu033'
    UNION SELECT 'menu040' UNION SELECT 'menu041' UNION SELECT 'menu042' UNION SELECT 'menu050'
    UNION SELECT 'menu060' UNION SELECT 'menu101' UNION SELECT 'menu102' UNION SELECT 'menu103'
    UNION SELECT 'menu104' UNION SELECT 'menu105' UNION SELECT 'menu106' UNION SELECT 'menu107'
  ) m
) AS seed_role_menu;

-- ADMIN 全部菜单
INSERT IGNORE INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT CONCAT('rm_admin_', id), 'role005', id FROM sys_menu;

-- ────────────────────────────────────────────────────────────
-- 7. 用户角色迁移：sys_user.role → sys_user_role
-- ────────────────────────────────────────────────────────────

INSERT IGNORE INTO `sys_user_role` (`id`, `user_id`, `role_id`)
SELECT CONCAT('ur_', u.id), u.id, r.id
FROM sys_user u
INNER JOIN sys_role r ON r.role_code = u.role
WHERE u.status = 1;
