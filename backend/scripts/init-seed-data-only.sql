-- ============================================================
-- 质量判定解释与让步管理系统 — 纯数据初始化脚本（无建表语句）
-- MySQL 5.7+ | utf8mb4 | 幂等：INSERT IGNORE / ON DUPLICATE KEY UPDATE
-- ============================================================
--
-- 【用途】表结构已存在、表数据为空时，一次性写入全部初始/演示数据。
--
-- 【用法】
--   mysql -u root -p -h 127.0.0.1 -P 3307 ai_quality_control < init-seed-data-only.sql
--
-- 【默认账号】admin / Admin123456（BCrypt，所有测试用户同密码）
--
-- 【数据分层】
--   1. 字典与用户
--   2. RBAC（角色/权限/菜单/关联）
--   3. AI 置信度默认配置
--   4. 业务演示（指标/标准/检验/判定/冲突/RAG/缓存）
--
-- ============================================================

USE ai_quality_control;


-- ============================================================
-- 1. 数据字典 + 初始用户
-- ============================================================
-- 字典分类
INSERT IGNORE INTO sys_dict (id, dict_code, dict_name, is_system, sort_no, status, create_date_time, update_date_time) VALUES
('d001', 'STANDARD_TYPE',       '标准类型',       1, 1,  1, NOW(), NOW()),
('d002', 'INDICATOR_CATEGORY',  '指标类别',       1, 2,  1, NOW(), NOW()),
('d003', 'SAMPLE_TYPE',         '样品类型',       1, 3,  1, NOW(), NOW()),
('d004', 'JUDGMENT_TYPE',       '判定结论类型',   1, 4,  1, NOW(), NOW()),
('d005', 'STANDARD_STATUS',     '标准状态',       1, 5,  1, NOW(), NOW()),
('d006', 'INSPECTION_STATUS',   '检验记录状态',   1, 6,  1, NOW(), NOW()),
('d007', 'REINSPECTION_STATUS', '复检状态',       1, 7,  1, NOW(), NOW()),
('d008', 'APPROVAL_LEVEL',      '改判审批级别',   1, 8,  1, NOW(), NOW()),
('d009', 'APPROVAL_STATUS',     '改判审批状态',   1, 9,  1, NOW(), NOW()),
('d010', 'CONFIRM_STATUS',      '让步客户确认状态', 1, 10, 1, NOW(), NOW()),
('d011', 'CONCESSION_STATUS',   '让步总状态',     1, 11, 1, NOW(), NOW()),
('d012', 'USER_ROLE',           '用户角色',       1, 12, 1, NOW(), NOW()),
('d013', 'NEW_EVIDENCE_SOURCE', '新证据来源',     1, 13, 1, NOW(), NOW()),
('d014', 'PRODUCT_VARIETY',     '品种',           0, 14, 1, NOW(), NOW()),
('d015', 'PRODUCT_GRADE',       '牌号',           0, 15, 1, NOW(), NOW()),
('d016', 'AUDIT_OPERATION_TYPE','审计操作类型',   1, 16, 1, NOW(), NOW()),
('d017', 'QC_CUSTOMER',         '关联客户',       0, 17, 1, NOW(), NOW());

-- 标准类型
INSERT IGNORE INTO sys_dict_item (id, dict_code, item_value, item_label, color_tag, sort_no, status, is_system, create_date_time, update_date_time) VALUES
('di001', 'STANDARD_TYPE', 'NATIONAL',   '国标',     'info',    1, 1, 1, NOW(), NOW()),
('di002', 'STANDARD_TYPE', 'ENTERPRISE', '企标',     '',        2, 1, 1, NOW(), NOW()),
('di003', 'STANDARD_TYPE', 'CUSTOMER',   '客户协议', 'success', 3, 1, 1, NOW(), NOW());

-- 指标类别
INSERT IGNORE INTO sys_dict_item (id, dict_code, item_value, item_label, color_tag, sort_no, status, is_system, create_date_time, update_date_time) VALUES
('di011', 'INDICATOR_CATEGORY', 'COMPOSITION', '成分', '',        1, 1, 1, NOW(), NOW()),
('di012', 'INDICATOR_CATEGORY', 'PERFORMANCE', '性能', 'primary', 2, 1, 1, NOW(), NOW()),
('di013', 'INDICATOR_CATEGORY', 'DIMENSION',   '尺寸', 'warning', 3, 1, 1, NOW(), NOW()),
('di014', 'INDICATOR_CATEGORY', 'SURFACE',     '表面', '',        4, 1, 1, NOW(), NOW()),
('di015', 'INDICATOR_CATEGORY', 'SHAPE',       '外形', '',        5, 1, 1, NOW(), NOW());

-- 样品类型
INSERT IGNORE INTO sys_dict_item (id, dict_code, item_value, item_label, color_tag, sort_no, status, is_system, create_date_time, update_date_time) VALUES
('di021', 'SAMPLE_TYPE', 'HEAD',   '头部', '', 1, 1, 1, NOW(), NOW()),
('di022', 'SAMPLE_TYPE', 'TAIL',   '尾部', '', 2, 1, 1, NOW(), NOW()),
('di023', 'SAMPLE_TYPE', 'MIDDLE', '中部', '', 3, 1, 1, NOW(), NOW());

-- 判定结论类型
INSERT IGNORE INTO sys_dict_item (id, dict_code, item_value, item_label, color_tag, sort_no, status, is_system, create_date_time, update_date_time) VALUES
('di031', 'JUDGMENT_TYPE', 'QUALIFIED',         '合格',   'success', 1, 1, 1, NOW(), NOW()),
('di032', 'JUDGMENT_TYPE', 'UNQUALIFIED',       '不合格', 'danger',  2, 1, 1, NOW(), NOW()),
('di033', 'JUDGMENT_TYPE', 'NEED_REINSPECTION', '需复检', 'warning', 3, 1, 1, NOW(), NOW()),
('di034', 'JUDGMENT_TYPE', 'CAN_CONCESSION',    '可让步', 'warning', 4, 1, 1, NOW(), NOW()),
('di035', 'JUDGMENT_TYPE', 'STANDARD_CONFLICT', '标准冲突', 'danger', 5, 1, 1, NOW(), NOW());

-- 标准状态
INSERT IGNORE INTO sys_dict_item (id, dict_code, item_value, item_label, color_tag, sort_no, status, is_system, create_date_time, update_date_time) VALUES
('di041', 'STANDARD_STATUS', 'DRAFT',      '草稿', 'info',    1, 1, 1, NOW(), NOW()),
('di042', 'STANDARD_STATUS', 'PUBLISHED',  '已发布', 'success', 2, 1, 1, NOW(), NOW()),
('di043', 'STANDARD_STATUS', 'DEPRECATED', '已作废', 'danger',  3, 1, 1, NOW(), NOW());

-- 检验记录状态
INSERT IGNORE INTO sys_dict_item (id, dict_code, item_value, item_label, color_tag, sort_no, status, is_system, create_date_time, update_date_time) VALUES
('di051', 'INSPECTION_STATUS', 'NORMAL', '正常', 'success', 1, 1, 1, NOW(), NOW()),
('di052', 'INSPECTION_STATUS', 'VOID',   '作废', 'danger',  2, 1, 1, NOW(), NOW());

-- 复检状态
INSERT IGNORE INTO sys_dict_item (id, dict_code, item_value, item_label, color_tag, sort_no, status, is_system, create_date_time, update_date_time) VALUES
('di061', 'REINSPECTION_STATUS', 'PENDING',   '待复检', 'warning', 1, 1, 1, NOW(), NOW()),
('di062', 'REINSPECTION_STATUS', 'COMPLETED', '已完成', 'success', 2, 1, 1, NOW(), NOW());

-- 改判审批级别
INSERT IGNORE INTO sys_dict_item (id, dict_code, item_value, item_label, color_tag, sort_no, status, is_system, create_date_time, update_date_time) VALUES
('di071', 'APPROVAL_LEVEL', 'NORMAL',   '常规审批', '',       1, 1, 1, NOW(), NOW()),
('di072', 'APPROVAL_LEVEL', 'ENHANCED', '增强审批', 'danger', 2, 1, 1, NOW(), NOW());

-- 改判审批状态
INSERT IGNORE INTO sys_dict_item (id, dict_code, item_value, item_label, color_tag, sort_no, status, is_system, create_date_time, update_date_time) VALUES
('di081', 'APPROVAL_STATUS', 'PENDING',  '待审批', 'warning', 1, 1, 1, NOW(), NOW()),
('di082', 'APPROVAL_STATUS', 'APPROVED', '已批准', 'success', 2, 1, 1, NOW(), NOW()),
('di083', 'APPROVAL_STATUS', 'REJECTED', '已拒绝', 'danger',  3, 1, 1, NOW(), NOW());

-- 让步客户确认状态
INSERT IGNORE INTO sys_dict_item (id, dict_code, item_value, item_label, color_tag, sort_no, status, is_system, create_date_time, update_date_time) VALUES
('di091', 'CONFIRM_STATUS', 'PENDING',   '待确认',   'warning', 1, 1, 1, NOW(), NOW()),
('di092', 'CONFIRM_STATUS', 'CONFIRMED', '已确认',   'success', 2, 1, 1, NOW(), NOW()),
('di093', 'CONFIRM_STATUS', 'REJECTED',  '确认驳回', 'danger',  3, 1, 1, NOW(), NOW());

-- 让步总状态
INSERT IGNORE INTO sys_dict_item (id, dict_code, item_value, item_label, color_tag, sort_no, status, is_system, create_date_time, update_date_time) VALUES
('di101', 'CONCESSION_STATUS', 'PENDING_APPROVAL', '待内部审批', 'warning', 1, 1, 1, NOW(), NOW()),
('di102', 'CONCESSION_STATUS', 'APPROVED',         '已批准',     'success', 2, 1, 1, NOW(), NOW()),
('di103', 'CONCESSION_STATUS', 'INVALID',          '已失效',     'info',    3, 1, 1, NOW(), NOW()),
('di104', 'CONCESSION_STATUS', 'REJECTED',         '已驳回',     'danger',  4, 1, 1, NOW(), NOW());

-- 用户角色
INSERT IGNORE INTO sys_dict_item (id, dict_code, item_value, item_label, color_tag, sort_no, status, is_system, create_date_time, update_date_time) VALUES
('di111', 'USER_ROLE', 'QUALITY_ENGINEER',    '质量工程师', '',        1, 1, 1, NOW(), NOW()),
('di112', 'USER_ROLE', 'QUALITY_SUPERVISOR',  '质检主管',   'primary', 2, 1, 1, NOW(), NOW()),
('di113', 'USER_ROLE', 'QUALITY_MANAGER',     '质量经理',   'warning', 3, 1, 1, NOW(), NOW()),
('di114', 'USER_ROLE', 'SALES_MANAGER',       '销售经理',   '',        4, 1, 1, NOW(), NOW()),
('di115', 'USER_ROLE', 'ADMIN',               '系统管理员', 'danger',  5, 1, 1, NOW(), NOW());

-- 新证据来源
INSERT IGNORE INTO sys_dict_item (id, dict_code, item_value, item_label, color_tag, sort_no, status, is_system, create_date_time, update_date_time) VALUES
('di121', 'NEW_EVIDENCE_SOURCE', 'SUBSEQUENT_PROCESS', '后续工序缺陷发现',   'danger',  1, 1, 1, NOW(), NOW()),
('di122', 'NEW_EVIDENCE_SOURCE', 'CUSTOMER_COMPLAINT', '客户质量异议',       'danger',  2, 1, 1, NOW(), NOW()),
('di123', 'NEW_EVIDENCE_SOURCE', 'BATCH_TRACING',      '同炉批次追溯',       'warning', 3, 1, 1, NOW(), NOW()),
('di124', 'NEW_EVIDENCE_SOURCE', 'THIRD_PARTY',        '第三方检测机构复验', 'warning', 4, 1, 1, NOW(), NOW()),
('di125', 'NEW_EVIDENCE_SOURCE', 'OTHER',              '其他',               '',        5, 1, 1, NOW(), NOW());

-- 品种（示例数据）
INSERT IGNORE INTO sys_dict_item (id, dict_code, item_value, item_label, color_tag, sort_no, status, is_system, create_date_time, update_date_time) VALUES
('di131', 'PRODUCT_VARIETY', '冷轧板', '冷轧板', '', 1, 1, 0, NOW(), NOW()),
('di132', 'PRODUCT_VARIETY', '热轧板', '热轧板', '', 2, 1, 0, NOW(), NOW()),
('di133', 'PRODUCT_VARIETY', '镀锌板', '镀锌板', '', 3, 1, 0, NOW(), NOW());

-- 牌号（示例数据）
INSERT IGNORE INTO sys_dict_item (id, dict_code, item_value, item_label, color_tag, sort_no, status, is_system, create_date_time, update_date_time) VALUES
('di141', 'PRODUCT_GRADE', 'Q235B', 'Q235B', '', 1, 1, 0, NOW(), NOW()),
('di142', 'PRODUCT_GRADE', 'SPHC',  'SPHC',  '', 2, 1, 0, NOW(), NOW()),
('di143', 'PRODUCT_GRADE', 'DC01',  'DC01',  '', 3, 1, 0, NOW(), NOW());

-- 审计操作类型
INSERT IGNORE INTO sys_dict_item (id, dict_code, item_value, item_label, color_tag, sort_no, status, is_system, create_date_time, update_date_time) VALUES
('di151', 'AUDIT_OPERATION_TYPE', 'VOID_INSPECTION',    '作废检验记录',   'warning', 1, 1, 1, NOW(), NOW()),
('di152', 'AUDIT_OPERATION_TYPE', 'APPROVE_REJUDGMENT', '审批改判申请',   'primary', 2, 1, 1, NOW(), NOW()),
('di153', 'AUDIT_OPERATION_TYPE', 'CONFIRM_CONCESSION', '确认让步',       'success', 3, 1, 1, NOW(), NOW()),
('di154', 'AUDIT_OPERATION_TYPE', 'APPROVE_CONCESSION', '审批让步',       'primary', 4, 1, 1, NOW(), NOW()),
('di155', 'AUDIT_OPERATION_TYPE', 'PUBLISH_STANDARD',   '发布质量标准',   'info',    5, 1, 1, NOW(), NOW());

-- 关联客户（客协标准绑定，item_value 为客户ID）
INSERT IGNORE INTO sys_dict_item (id, dict_code, item_value, item_label, color_tag, sort_no, status, is_system, create_date_time, update_date_time) VALUES
('di161', 'QC_CUSTOMER', 'CUST-001', '华东汽车配件有限公司', '', 1, 1, 0, NOW(), NOW()),
('di162', 'QC_CUSTOMER', 'CUST-002', '西南建材集团',         '', 2, 1, 0, NOW(), NOW());

-- 初始管理员用户（密码: Admin123456，BCrypt哈希）
INSERT IGNORE INTO sys_user (id, user_no, username, password, role, department, status, create_date_time, update_date_time) VALUES
('u001', 'admin', '系统管理员', '$2a$10$Z5WD9yNWm3MoDLuSamTQ1usKEbhWbZE4eT.vJZoFCIRL1L.hp6UEC', 'ADMIN', '信息中心', 1, NOW(), NOW()),
('u002', '021001', '张质检', '$2a$10$Z5WD9yNWm3MoDLuSamTQ1usKEbhWbZE4eT.vJZoFCIRL1L.hp6UEC', 'QUALITY_ENGINEER', '质检部', 1, NOW(), NOW()),
('u003', '021002', '李主管', '$2a$10$Z5WD9yNWm3MoDLuSamTQ1usKEbhWbZE4eT.vJZoFCIRL1L.hp6UEC', 'QUALITY_SUPERVISOR', '质检部', 1, NOW(), NOW()),
('u004', '021003', '王经理', '$2a$10$Z5WD9yNWm3MoDLuSamTQ1usKEbhWbZE4eT.vJZoFCIRL1L.hp6UEC', 'QUALITY_MANAGER', '质量部', 1, NOW(), NOW()),
('u005', '021004', '陈销售', '$2a$10$Z5WD9yNWm3MoDLuSamTQ1usKEbhWbZE4eT.vJZoFCIRL1L.hp6UEC', 'SALES_MANAGER', '销售部', 1, NOW(), NOW());
-- 初始密码均为: Admin123456

-- ============================================================
-- 2. RBAC：角色 / 权限 / 菜单 / 关联
-- ============================================================
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
  ('perm024', 'admin:dict:manage',       '字典管理',       'API',    1, '2026-06-03 00:00:00'),
  ('perm025', 'menu:standard-rag',        '标准RAG检索',    'MENU',   1, '2026-06-21 00:00:00'),
  ('perm026', 'standard-rag:query',       '标准RAG查询',    'API',    1, '2026-06-21 00:00:00'),
  ('perm027', 'menu:standard-conflict',   '标准冲突检测',   'MENU',   1, '2026-06-21 00:00:00'),
  ('perm028', 'standard-conflict:resolve','标准冲突裁决',   'API',    1, '2026-06-21 00:00:00'),
  ('perm029', 'menu:cert-qa',             '质保书问答',     'MENU',   1, '2026-06-21 00:00:00'),
  ('perm030', 'cert:qa',                  '质保书问答接口', 'API',    1, '2026-06-21 00:00:00'),
  ('perm031', 'menu:ai-assessment',       'AI评估审计',     'MENU',   1, '2026-06-21 00:00:00'),
  ('perm032', 'ai-assessment:review',     'AI评估审阅',     'API',    1, '2026-06-21 00:00:00'),
  ('perm033', 'menu:ai-confidence',       'AI置信度配置',   'MENU',   1, '2026-06-21 00:00:00'),
  ('perm034', 'ai-confidence:manage',     'AI置信度管理',   'API',    1, '2026-06-21 00:00:00')
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
  ('menu014', 'menu010', 'MENU',   '标准RAG检索','standard-rag',           'standard-rag/index',        'standard-rag',           'Document',     'menu:standard-rag',1,24,  1, '2026-06-21 00:00:00'),
  ('menu020', '0',       'DIR',    '检验与判定', NULL,                     NULL,                        NULL,                     NULL,           NULL,              1, 30,  1, '2026-06-03 00:00:00'),
  ('menu021', 'menu020', 'MENU',   '检验录入',   'inspection',             'inspection/index',          'inspection',             'EditPen',      'menu:inspection', 1, 31,  1, '2026-06-03 00:00:00'),
  ('menu022', 'menu020', 'MENU',   '判定解释',   'judgment',               'judgment/index',            'judgment',               'Stamp',        'menu:judgment',   1, 32,  1, '2026-06-03 00:00:00'),
  ('menu030', '0',       'DIR',    '质量流程',   NULL,                     NULL,                        NULL,                     NULL,           NULL,              1, 40,  1, '2026-06-03 00:00:00'),
  ('menu031', 'menu030', 'MENU',   '复检管理',   'reinspection',           'reinspection/index',        'reinspection',           'RefreshRight', 'menu:reinspection',1,41, 1, '2026-06-03 00:00:00'),
  ('menu032', 'menu030', 'MENU',   '改判管理',   're-judgment',            're-judgment/index',         're-judgment',            'Edit',         'menu:rejudgment', 1, 42,  1, '2026-06-03 00:00:00'),
  ('menu033', 'menu030', 'MENU',   '让步接收',   'concession',             'concession/index',          'concession',             'Check',        'menu:concession', 1, 43,  1, '2026-06-03 00:00:00'),
  ('menu034', 'menu030', 'MENU',   '标准冲突检测','standard-conflicts',     'standard-conflicts/index',  'standard-conflicts',     'Warning',      'menu:standard-conflict',1,44,1,'2026-06-21 00:00:00'),
  ('menu040', '0',       'DIR',    '数据汇总',   NULL,                     NULL,                        NULL,                     NULL,           NULL,              1, 50,  1, '2026-06-03 00:00:00'),
  ('menu041', 'menu040', 'MENU',   '质保书数据', 'cert-data',              'cert-data/index',           'cert-data',              'Tickets',      'menu:cert-data',  1, 51,  1, '2026-06-03 00:00:00'),
  ('menu043', 'menu040', 'MENU',   '质保书问答', 'cert-data/qa',           'cert-data/qa',              'cert-data-qa',           'Tickets',      'menu:cert-qa',    1, 52,  1, '2026-06-21 00:00:00'),
  ('menu042', 'menu040', 'MENU',   '质量统计',   'statistics',             'statistics/index',          'statistics',             'PieChart',     'menu:statistics', 1, 53,  1, '2026-06-03 00:00:00'),
  ('menu050', '0',       'MENU',   '权限审计',   'audit',                  'audit/index',               'audit',                  'Lock',         'menu:audit',      1, 60,  1, '2026-06-03 00:00:00'),
  ('menu060', '0',       'DIR',    '管理员',     NULL,                     NULL,                        NULL,                     NULL,           NULL,              1, 70,  1, '2026-06-03 00:00:00'),
  ('menu061', 'menu060', 'MENU',   '账号管理',   'admin/users',            'admin/users/index',         'admin-users',            'User',         'menu:admin:users',1, 71,  1, '2026-06-03 00:00:00'),
  ('menu062', 'menu060', 'MENU',   '数据字典',   'admin/dict',             'admin/dict/index',          'admin-dict',             'List',         'menu:admin:dict', 1, 72,  1, '2026-06-03 00:00:00'),
  ('menu063', 'menu060', 'MENU',   '菜单管理',   'admin/menus',            'admin/menus/index',         'admin-menus',            'Menu',         'menu:admin:menus',1, 73,  1, '2026-06-03 00:00:00'),
  ('menu064', 'menu060', 'MENU',   '角色管理',   'admin/roles',            'admin/roles/index',         'admin-roles',            'Avatar',       'menu:admin:roles',1, 74,  1, '2026-06-03 00:00:00'),
  ('menu065', 'menu060', 'MENU',   'AI评估审计','ai-assessments',         'ai-assessments/index',      'ai-assessments',         'Tickets',      'menu:ai-assessment',1,75,1,'2026-06-21 00:00:00'),
  ('menu066', 'menu060', 'MENU',   'AI置信度配置','admin/ai-confidence',   'admin/ai-confidence/index', 'admin-ai-confidence',    'Setting',      'menu:ai-confidence',1,76,1,'2026-06-21 00:00:00'),
  ('menu101', '0',       'HIDDEN', '新建检验',   'inspection/form',        'inspection/form',           'inspection-form',        'EditPen',      'menu:inspection', 0, 101, 1, '2026-06-03 00:00:00'),
  ('menu102', '0',       'HIDDEN', '判定详情',   'judgment/explanation',   'judgment/explanation',      'judgment-explanation',   'Stamp',        'menu:judgment',   0, 102, 1, '2026-06-03 00:00:00'),
  ('menu103', '0',       'HIDDEN', '发起改判',   're-judgment/form',       're-judgment/form',          're-judgment-form',       'Edit',         'menu:rejudgment', 0, 103, 1, '2026-06-03 00:00:00'),
  ('menu104', '0',       'HIDDEN', '改判详情',   're-judgment/detail',     're-judgment/detail',        're-judgment-detail',     'Edit',         'menu:rejudgment', 0, 104, 1, '2026-06-03 00:00:00'),
  ('menu105', '0',       'HIDDEN', '发起让步',   'concession/apply',       'concession/form',           'concession-apply',       'Check',        'menu:concession', 0, 105, 1, '2026-06-03 00:00:00'),
  ('menu106', '0',       'HIDDEN', '让步详情',   'concession/detail',      'concession/detail',         'concession-detail',      'Check',        'menu:concession', 0, 106, 1, '2026-06-03 00:00:00'),
  ('menu107', '0',       'HIDDEN', '内嵌页面',   'iframe/:id',             'iframe/index',              'iframe-page',            'Link',         NULL,              0, 107, 1, '2026-06-03 00:00:00'),
  ('menu108', '0',       'HIDDEN', '标准冲突详情','standard-conflicts/detail','standard-conflicts/detail', 'standard-conflicts-detail','Warning',    'menu:standard-conflict',0,108,1,'2026-06-21 00:00:00'),
  ('menu109', '0',       'HIDDEN', '标准冲突裁决','standard-conflicts/resolve','standard-conflicts/detail','standard-conflicts-resolve','Warning',   'menu:standard-conflict',0,109,1,'2026-06-21 00:00:00')
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

-- ────────────────────────────────────────────────────────────
-- 6. Seed：角色-菜单分配（业务角色全部业务菜单，ADMIN 含管理菜单）
-- ────────────────────────────────────────────────────────────

-- 业务菜单 IDs（不含 admin 目录及子项）
-- menu001-050, menu101-109

-- 业务角色 × 业务菜单（用子查询包裹，避免 MySQL 5.7 将 ON DUPLICATE 误判为 JOIN 条件）
INSERT IGNORE INTO `sys_role_menu` (`id`, `role_id`, `menu_id`)
SELECT `id`, `role_id`, `menu_id` FROM (
  SELECT CONCAT('rm_biz_', r.role_id, '_', m.menu_id) AS `id`, r.role_id, m.menu_id
  FROM (
    SELECT 'role001' AS role_id UNION SELECT 'role002' UNION SELECT 'role003' UNION SELECT 'role004'
  ) r
  CROSS JOIN (
    SELECT 'menu001' AS menu_id UNION SELECT 'menu010' UNION SELECT 'menu011' UNION SELECT 'menu012'
    UNION SELECT 'menu013' UNION SELECT 'menu014' UNION SELECT 'menu020' UNION SELECT 'menu021'
    UNION SELECT 'menu022' UNION SELECT 'menu030' UNION SELECT 'menu031' UNION SELECT 'menu032'
    UNION SELECT 'menu033' UNION SELECT 'menu034' UNION SELECT 'menu040' UNION SELECT 'menu041'
    UNION SELECT 'menu042' UNION SELECT 'menu043' UNION SELECT 'menu050'
    UNION SELECT 'menu060' UNION SELECT 'menu101' UNION SELECT 'menu102' UNION SELECT 'menu103'
    UNION SELECT 'menu104' UNION SELECT 'menu105' UNION SELECT 'menu106' UNION SELECT 'menu107'
    UNION SELECT 'menu108' UNION SELECT 'menu109'
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

-- ============================================================
-- 3. AI 置信度默认配置
-- ============================================================
INSERT IGNORE INTO `qc_ai_confidence_config` (
  `id`, `config_name`, `rule_weight`, `rag_weight`, `llm_weight`,
  `high_threshold`, `medium_threshold`, `low_threshold`, `enabled`,
  `active_flag`, `updated_by`, `updated_at`, `create_user_no`, `create_date_time`
) VALUES (
  'ai_conf_default', '默认置信度配置', 0.6000, 0.3000, 0.1000,
  0.8500, 0.6500, 0.0000, 1,
  1, 'system', NOW(), 'system', NOW()
);

-- ============================================================
-- 4. 业务演示：指标 / 标准 / 检验 / 判定
-- ============================================================
-- 测试指标项目
INSERT IGNORE INTO qc_indicator_item (id, indicator_name, indicator_code, indicator_category, unit, description, is_deleted, create_date_time, update_date_time) VALUES
('ind001', '抗拉强度', 'Rm',   'PERFORMANCE', 'MPa', '最大拉伸力/原始横截面积', 0, NOW(), NOW()),
('ind002', '延伸率',   'A',    'PERFORMANCE', '%',   '断后伸长量/原始标距长度',  0, NOW(), NOW()),
('ind003', '屈服强度', 'ReL',  'PERFORMANCE', 'MPa', '屈服点的应力值',           0, NOW(), NOW()),
('ind004', '厚度公差', 'Δt',   'DIMENSION',   'mm',  '实测厚度与名义厚度之差',   0, NOW(), NOW()),
('ind005', '宽度公差', 'Δw',   'DIMENSION',   'mm',  '实测宽度与名义宽度之差',   0, NOW(), NOW()),
('ind006', '表面等级', 'FS',   'SURFACE',     '-',   'FB/FC/FD等级',             0, NOW(), NOW()),
('ind007', '硬度',     'HV',   'PERFORMANCE', 'HV',  'Vickers 硬度',             0, NOW(), NOW()),
('ind008', 'n值',      'n',    'PERFORMANCE', '-',   '应变硬化指数',             0, NOW(), NOW());

-- 测试质量标准：国标 Q235B
INSERT IGNORE INTO qc_quality_standard (id, standard_type, standard_code, standard_name, variety, grade, spec_range, version_no, effective_date, expiry_date, status, customer_id, remark, create_date_time, update_date_time) VALUES
('std001', 'NATIONAL', 'GB/T 912-2008', '冷轧碳素钢板国家标准', '冷轧板', 'Q235B', '厚度0.5-3.0mm，宽度600-1500mm', 'GB/T 912-2008', '2009-06-01', '9999-12-31', 'PUBLISHED', NULL, '冷轧碳素钢板国家标准', NOW(), NOW());

-- 国标 Q235B 指标限值
INSERT IGNORE INTO qc_standard_indicator (id, standard_id, indicator_id, upper_limit, lower_limit, is_required, concession_upper, concession_lower, create_date_time, update_date_time) VALUES
('si001', 'std001', 'ind001', 510.000000, 370.000000, 1, 520.000000, 360.000000, NOW(), NOW()),
('si002', 'std001', 'ind002', NULL, 26.000000, 1, NULL, 24.000000, NOW(), NOW()),
('si003', 'std001', 'ind003', NULL, 235.000000, 1, NULL, NULL, NOW(), NOW()),
('si004', 'std001', 'ind004', 0.120000, -0.120000, 1, 0.150000, -0.150000, NOW(), NOW());

-- 测试质量标准：客户协议（引用较严要求）
INSERT IGNORE INTO qc_quality_standard (id, standard_type, standard_code, standard_name, variety, grade, spec_range, version_no, effective_date, expiry_date, status, customer_id, remark, create_date_time, update_date_time) VALUES
('std002', 'CUSTOMER', '协议C2025-088-v1', '华南汽车零配件厂客户协议', '冷轧板', 'Q235B', '厚度1.0-2.0mm', '协议C2025-088-v1', '2025-01-01', '2025-12-31', 'PUBLISHED', 'CUST-001', '华南汽车零配件厂客户协议', NOW(), NOW());

-- 客协指标限值（抗拉强度下限更高）
INSERT IGNORE INTO qc_standard_indicator (id, standard_id, indicator_id, upper_limit, lower_limit, is_required, concession_upper, concession_lower, create_date_time, update_date_time) VALUES
('si011', 'std002', 'ind001', 500.000000, 380.000000, 1, 510.000000, 370.000000, NOW(), NOW()),
('si012', 'std002', 'ind002', NULL, 28.000000, 1, NULL, 26.000000, NOW(), NOW()),
('si013', 'std002', 'ind004', 0.100000, -0.100000, 1, 0.120000, -0.120000, NOW(), NOW());

-- 测试检验记录：合格
INSERT IGNORE INTO qc_inspection_record (id, heat_no, coil_no, batch_no, sample_type, test_time, tester_no, customer_id, product_variety, product_grade, product_spec, status, create_date_time, update_date_time) VALUES
('rec001', 'H20250514001', 'Z001001', 'H20250514001', 'MIDDLE', '2025-05-14 10:00:00', '021001', NULL, '冷轧板', 'Q235B', '厚度1.5mm×宽度1000mm', 'NORMAL', NOW(), NOW());

INSERT IGNORE INTO qc_inspection_value (id, record_id, indicator_id, test_value, create_date_time, update_date_time) VALUES
('iv001', 'rec001', 'ind001', 430.000000, NOW(), NOW()),
('iv002', 'rec001', 'ind002', 30.500000, NOW(), NOW()),
('iv003', 'rec001', 'ind003', 280.000000, NOW(), NOW()),
('iv004', 'rec001', 'ind004', 0.050000, NOW(), NOW());

-- 对应判定结论：合格
INSERT IGNORE INTO qc_judgment_result (id, record_id, judgment_type, judgment_time, is_final, matched_standard_ids, create_date_time, update_date_time) VALUES
('jud001', 'rec001', 'QUALIFIED', '2025-05-14 10:00:05', 1, '["std001"]', NOW(), NOW());

INSERT IGNORE INTO qc_judgment_evidence (id, judgment_id, standard_id, indicator_id, test_value, upper_limit, lower_limit, deviation, trigger_rule, is_passed, create_date_time, update_date_time) VALUES
('je001', 'jud001', 'std001', 'ind001', 430.0, 510.0, 370.0, 60.0, '实测值 430.0 MPa 在国标范围 [370.0, 510.0] 内', 1, NOW(), NOW()),
('je002', 'jud001', 'std001', 'ind002', 30.5, NULL, 26.0, 4.5, '实测值 30.5% ≥ 国标下限 26.0%', 1, NOW(), NOW());

-- 测试检验记录：不合格（抗拉强度低于下限）
INSERT IGNORE INTO qc_inspection_record (id, heat_no, coil_no, batch_no, sample_type, test_time, tester_no, customer_id, product_variety, product_grade, product_spec, status, create_date_time, update_date_time) VALUES
('rec002', 'H20250514002', 'Z002001', 'H20250514002', 'HEAD', '2025-05-14 14:00:00', '021001', 'CUST-001', '冷轧板', 'Q235B', '厚度1.5mm×宽度1000mm', 'NORMAL', NOW(), NOW());

INSERT IGNORE INTO qc_inspection_value (id, record_id, indicator_id, test_value, create_date_time, update_date_time) VALUES
('iv011', 'rec002', 'ind001', 360.000000, NOW(), NOW()),
('iv012', 'rec002', 'ind002', 27.000000, NOW(), NOW());

-- 对应判定结论：不合格
INSERT IGNORE INTO qc_judgment_result (id, record_id, judgment_type, judgment_time, is_final, matched_standard_ids, create_date_time, update_date_time) VALUES
('jud002', 'rec002', 'UNQUALIFIED', '2025-05-14 14:00:05', 1, '["std002","std001"]', NOW(), NOW());

INSERT IGNORE INTO qc_judgment_evidence (id, judgment_id, standard_id, indicator_id, test_value, upper_limit, lower_limit, deviation, trigger_rule, is_passed, create_date_time, update_date_time) VALUES
('je011', 'jud002', 'std002', 'ind001', 360.0, 500.0, 380.0, -20.0, '实测值 360.0 MPa 低于客户协议下限 380.0 MPa（协议C2025-088-v1）', 0, NOW(), NOW()),
('je012', 'jud002', 'std002', 'ind002', 27.0, NULL, 28.0, -1.0, '实测值 27.0% 低于客户协议下限 28.0%（协议C2025-088-v1）', 0, NOW(), NOW());

-- 测试检验记录：可让步
INSERT IGNORE INTO qc_inspection_record (id, heat_no, coil_no, batch_no, sample_type, test_time, tester_no, customer_id, product_variety, product_grade, product_spec, status, create_date_time, update_date_time) VALUES
('rec003', 'H20250515001', 'Z003001', 'H20250515001', 'MIDDLE', '2025-05-15 09:00:00', '021001', NULL, '冷轧板', 'Q235B', '厚度1.5mm×宽度1000mm', 'NORMAL', NOW(), NOW());

INSERT IGNORE INTO qc_inspection_value (id, record_id, indicator_id, test_value, create_date_time, update_date_time) VALUES
('iv021', 'rec003', 'ind001', 365.000000, NOW(), NOW()),
('iv022', 'rec003', 'ind002', 25.000000, NOW(), NOW());

INSERT IGNORE INTO qc_judgment_result (id, record_id, judgment_type, judgment_time, is_final, matched_standard_ids, create_date_time, update_date_time) VALUES
('jud003', 'rec003', 'CAN_CONCESSION', '2025-05-15 09:00:05', 1, '["std001"]', NOW(), NOW());

-- ============================================================
-- 5. AI P0 演示：让步/冲突/客户画像/替代库存
-- ============================================================
-- Complete the existing concession demo scenario with evidence and customer usage.
UPDATE `qc_inspection_record`
SET `customer_id` = 'CUST-002',
    `update_date_time` = NOW()
WHERE `id` = 'rec003'
  AND (`customer_id` IS NULL OR `customer_id` = '');

INSERT IGNORE INTO `qc_judgment_evidence` (
  `id`, `judgment_id`, `standard_id`, `indicator_id`, `test_value`,
  `upper_limit`, `lower_limit`, `deviation`, `trigger_rule`, `is_passed`,
  `create_date_time`, `update_date_time`
) VALUES
  ('je021', 'jud003', 'std001', 'ind001', 365.000000, 510.000000, 370.000000, -5.000000,
   '实测值 365.0 MPa 低于国标下限 370.0 MPa，但在让步下限 360.0 MPa 范围内，触发 CAN_CONCESSION', 0, NOW(), NOW()),
  ('je022', 'jud003', 'std001', 'ind002', 25.000000, NULL, 26.000000, -1.000000,
   '实测值 25.0% 低于国标下限 26.0%，但在让步下限 24.0% 范围内，触发 CAN_CONCESSION', 0, NOW(), NOW());

INSERT IGNORE INTO `qc_customer_usage_profile` (
  `id`, `customer_id`, `customer_name`, `default_usage`, `risk_category`,
  `variety`, `grade`, `status`, `remark`, `create_user_no`, `create_date_time`
) VALUES
  ('usage_p0_001', 'CUST-002', '西南建材集团', '建筑围护和普通结构件', 'NORMAL',
   '冷轧板', 'Q235B', 'ACTIVE', 'P0 可让步演示默认用途', 'system', NOW());

INSERT IGNORE INTO `alternative_stock` (
  `id`, `variety`, `grade`, `spec_range`, `coil_no`, `batch_no`,
  `available_weight`, `location`, `status`, `earliest_ship_date`,
  `remark`, `create_user_no`, `create_date_time`
) VALUES
  ('alt_p0_001', '冷轧板', 'Q235B', '厚度1.0-2.0mm，宽度600-1500mm',
   'ZALT001', 'HALT20250515', 18.500000, 'A-01-03', 'AVAILABLE', '2025-05-17',
   'P0 可让步演示同规格合格替代资源', 'system', NOW());

-- Add a same-priority customer agreement conflict for the standard-conflict demo.
INSERT IGNORE INTO `qc_quality_standard` (
  `id`, `standard_type`, `standard_code`, `standard_name`, `variety`, `grade`,
  `spec_range`, `version_no`, `effective_date`, `expiry_date`, `status`,
  `customer_id`, `remark`, `create_date_time`, `update_date_time`
) VALUES
  ('std003', 'CUSTOMER', '协议C2025-088-v2-冲突样例', '华东汽车配件客户协议冲突样例',
   '冷轧板', 'Q235B', '厚度1.0-2.0mm', '协议C2025-088-v2', '2025-04-01', '2025-12-31',
   'PUBLISHED', 'CUST-001', 'P0 标准冲突演示：与 std002 同优先级同范围但限值不一致', NOW(), NOW());

INSERT IGNORE INTO `qc_standard_indicator` (
  `id`, `standard_id`, `indicator_id`, `upper_limit`, `lower_limit`, `is_required`,
  `concession_upper`, `concession_lower`, `create_date_time`, `update_date_time`
) VALUES
  ('si021', 'std003', 'ind001', 500.000000, 395.000000, 1, 510.000000, 385.000000, NOW(), NOW()),
  ('si022', 'std003', 'ind002', NULL, 29.000000, 1, NULL, 27.000000, NOW(), NOW()),
  ('si023', 'std003', 'ind004', 0.080000, -0.080000, 1, 0.100000, -0.100000, NOW(), NOW());

INSERT IGNORE INTO `qc_inspection_record` (
  `id`, `heat_no`, `coil_no`, `batch_no`, `sample_type`, `test_time`,
  `tester_no`, `customer_id`, `product_variety`, `product_grade`,
  `product_spec`, `status`, `create_date_time`, `update_date_time`
) VALUES
  ('rec004', 'H20250516001', 'Z004001', 'H20250516001', 'MIDDLE', '2025-05-16 09:30:00',
   '021001', 'CUST-001', '冷轧板', 'Q235B', '厚度1.5mm×宽度1000mm', 'NORMAL', NOW(), NOW());

INSERT IGNORE INTO `qc_inspection_value` (
  `id`, `record_id`, `indicator_id`, `test_value`, `create_date_time`, `update_date_time`
) VALUES
  ('iv031', 'rec004', 'ind001', 390.000000, NOW(), NOW()),
  ('iv032', 'rec004', 'ind002', 29.500000, NOW(), NOW()),
  ('iv033', 'rec004', 'ind004', 0.060000, NOW(), NOW());

INSERT IGNORE INTO `qc_judgment_result` (
  `id`, `record_id`, `judgment_type`, `judgment_time`, `is_final`,
  `matched_standard_ids`, `remark`, `create_date_time`, `update_date_time`
) VALUES
  ('jud004', 'rec004', 'STANDARD_CONFLICT', '2025-05-16 09:30:05', 1,
   '["std002","std003","std001"]', 'P0 演示：客户协议同优先级重叠冲突，需人工裁决', NOW(), NOW());

INSERT IGNORE INTO `qc_judgment_evidence` (
  `id`, `judgment_id`, `standard_id`, `indicator_id`, `test_value`,
  `upper_limit`, `lower_limit`, `deviation`, `trigger_rule`, `is_passed`,
  `create_date_time`, `update_date_time`
) VALUES
  ('je031', 'jud004', 'std002', 'ind001', 390.000000, 500.000000, 380.000000, 10.000000,
   '同一客户协议优先级下，std002 要求 Rm ≥ 380 MPa', 0, NOW(), NOW()),
  ('je032', 'jud004', 'std003', 'ind001', 390.000000, 500.000000, 395.000000, -5.000000,
   '同一客户协议优先级下，std003 要求 Rm ≥ 395 MPa，与 std002 冲突，触发 STANDARD_CONFLICT', 0, NOW(), NOW());

INSERT IGNORE INTO `standard_conflict` (
  `id`, `conflict_no`, `judgment_id`, `record_id`, `conflict_type`,
  `conflict_level`, `status`, `indicator_id`, `indicator_name`, `unit`,
  `customer_id`, `variety`, `grade`, `product_spec`, `inspection_date`,
  `selected_standard_id`, `involved_standard_ids`, `conflict_detail`,
  `selected_priority`, `create_user_no`, `create_date_time`
) VALUES
  ('scf_p0_001', 'SCF-P0-001', 'jud004', 'rec004', 'NUMERIC_LIMIT',
   'BLOCKING', 'PENDING', 'ind001', '抗拉强度', 'MPa',
   'CUST-001', '冷轧板', 'Q235B', '厚度1.5mm×宽度1000mm', '2025-05-16',
   NULL, '["std002","std003"]',
   '{"reason":"同一客户、同一品种牌号、同一规格窗口内存在两个生效客户协议，Rm 下限分别为 380 MPa 和 395 MPa。","standards":[{"standardId":"std002","lowerLimit":380,"upperLimit":500,"effectiveDate":"2025-01-01","expiryDate":"2025-12-31"},{"standardId":"std003","lowerLimit":395,"upperLimit":500,"effectiveDate":"2025-04-01","expiryDate":"2025-12-31"}],"blocking":true}',
   'CUSTOMER', 'system', NOW());

-- ============================================================
-- 6. 标准 RAG 源文档与条款
-- ============================================================
INSERT IGNORE INTO `qc_standard_document` (
  `id`, `standard_id`, `document_code`, `document_name`, `document_type`,
  `standard_type`, `standard_code`, `standard_name`, `version_no`,
  `customer_id`, `variety`, `grade`, `spec_range`, `usage_scope`,
  `effective_date`, `expiry_date`, `source_file_name`, `source_file_path`,
  `source_file_hash`, `parse_status`, `index_status`, `indexed_at`,
  `status`, `remark`, `create_user_no`, `create_date_time`
) VALUES
  ('p0_doc_gb_912_q235b', 'std001', 'GB/T 912-2008-P0', '冷轧碳素钢板国家标准P0摘录', 'STANDARD',
   'NATIONAL', 'GB/T 912-2008', '冷轧碳素钢板国家标准', 'GB/T 912-2008',
   NULL, '冷轧板', 'Q235B', '厚度0.5-3.0mm，宽度600-1500mm', '通用交付',
   '2009-06-01', '9999-12-31', 'gbt-912-2008-q235b-p0.md',
   'backend/scripts/demo-documents/gbt-912-2008-q235b-p0.md',
   'p0-gb-912-q235b', 'PARSED', 'INDEXED', NOW(), 'ACTIVE', 'P0 合格和可让步场景来源', 'system', NOW()),
  ('p0_doc_c2025_088_v1', 'std002', '协议C2025-088-v1-P0', '华东汽车配件客户协议v1 P0摘录', 'AGREEMENT',
   'CUSTOMER', '协议C2025-088-v1', '华东汽车配件客户协议', '协议C2025-088-v1',
   'CUST-001', '冷轧板', 'Q235B', '厚度1.0-2.0mm', '汽车零配件',
   '2025-01-01', '2025-12-31', 'customer-c2025-088-v1-p0.md',
   'backend/scripts/demo-documents/customer-c2025-088-v1-p0.md',
   'p0-c2025-088-v1', 'PARSED', 'INDEXED', NOW(), 'ACTIVE', 'P0 不合格和冲突对比来源', 'system', NOW()),
  ('p0_doc_c2025_088_v2', 'std003', '协议C2025-088-v2-P0', '华东汽车配件客户协议v2冲突P0摘录', 'AGREEMENT',
   'CUSTOMER', '协议C2025-088-v2', '华东汽车配件客户协议冲突样例', '协议C2025-088-v2',
   'CUST-001', '冷轧板', 'Q235B', '厚度1.0-2.0mm', '汽车零配件',
   '2025-04-01', '2025-12-31', 'customer-c2025-088-v2-conflict-p0.md',
   'backend/scripts/demo-documents/customer-c2025-088-v2-conflict-p0.md',
   'p0-c2025-088-v2', 'PARSED', 'INDEXED', NOW(), 'ACTIVE', 'P0 标准冲突场景来源', 'system', NOW()),
  ('p0_doc_case_2025_001', NULL, 'CASE-P0-2025-001', 'P0历史投诉与让步案例摘录', 'CASE',
   NULL, NULL, 'P0历史投诉与让步案例', '2025-P0',
   'CUST-002', '冷轧板', 'Q235B', '厚度1.0-2.0mm', '建筑围护和普通结构件',
   '2025-01-01', '9999-12-31', 'customer-complaint-case-p0.md',
   'backend/scripts/demo-documents/customer-complaint-case-p0.md',
   'p0-case-2025-001', 'PARSED', 'INDEXED', NOW(), 'ACTIVE', 'P0 让步风险评估案例来源', 'system', NOW());

INSERT IGNORE INTO `qc_standard_clause` (
  `id`, `document_id`, `standard_id`, `clause_key`, `clause_no`, `page_no`,
  `paragraph_text`, `source_type`, `standard_type`, `standard_code`,
  `standard_name`, `version_no`, `customer_id`, `variety`, `grade`,
  `spec_range`, `usage_scope`, `indicator_id`, `indicator_code`,
  `indicator_name`, `effective_date`, `expiry_date`, `retrieval_keywords`,
  `es_document_key`, `embedding_status`, `relevance_group`, `status`,
  `create_user_no`, `create_date_time`
) VALUES
  ('p0_clause_gb_rm', 'p0_doc_gb_912_q235b', 'std001', 'GB912-2008-5.1-Rm', '5.1', 1,
   'Q235B 冷轧板抗拉强度 Rm 应为 370 MPa 至 510 MPa。用于质量判定时，实测值低于 370 MPa 或高于 510 MPa 均不满足合格范围。',
   'NATIONAL', 'NATIONAL', 'GB/T 912-2008', '冷轧碳素钢板国家标准', 'GB/T 912-2008',
   NULL, '冷轧板', 'Q235B', '厚度0.5-3.0mm，宽度600-1500mm', '通用交付',
   'ind001', 'Rm', '抗拉强度', '2009-06-01', '9999-12-31',
   'Q235B Rm 抗拉强度 370 510 国标 合格', 'p0:gb912:5.1:rm', 'INDEXED', 'P0_STANDARD', 'ACTIVE', 'system', NOW()),
  ('p0_clause_gb_a', 'p0_doc_gb_912_q235b', 'std001', 'GB912-2008-5.2-A', '5.2', 1,
   'Q235B 冷轧板断后延伸率 A 不应低于 26%。当实测值低于 26% 但不低于让步下限 24% 时，可进入让步评审，不得直接作为正式放行结论。',
   'NATIONAL', 'NATIONAL', 'GB/T 912-2008', '冷轧碳素钢板国家标准', 'GB/T 912-2008',
   NULL, '冷轧板', 'Q235B', '厚度0.5-3.0mm，宽度600-1500mm', '通用交付',
   'ind002', 'A', '延伸率', '2009-06-01', '9999-12-31',
   'Q235B 延伸率 A 26 24 让步', 'p0:gb912:5.2:a', 'INDEXED', 'P0_STANDARD', 'ACTIVE', 'system', NOW()),
  ('p0_clause_gb_rel', 'p0_doc_gb_912_q235b', 'std001', 'GB912-2008-5.3-ReL', '5.3', 1,
   'Q235B 冷轧板屈服强度 ReL 不应低于 235 MPa。该指标为必检性能指标，应随卷号或批次记录。',
   'NATIONAL', 'NATIONAL', 'GB/T 912-2008', '冷轧碳素钢板国家标准', 'GB/T 912-2008',
   NULL, '冷轧板', 'Q235B', '厚度0.5-3.0mm，宽度600-1500mm', '通用交付',
   'ind003', 'ReL', '屈服强度', '2009-06-01', '9999-12-31',
   'Q235B 屈服强度 ReL 235 必检', 'p0:gb912:5.3:rel', 'INDEXED', 'P0_STANDARD', 'ACTIVE', 'system', NOW()),
  ('p0_clause_gb_dt', 'p0_doc_gb_912_q235b', 'std001', 'GB912-2008-6.1-DT', '6.1', 2,
   'Q235B 冷轧板厚度公差应控制在 -0.120 mm 至 0.120 mm。超出合格范围但未超出 -0.150 mm 至 0.150 mm 的，可进入让步评审。',
   'NATIONAL', 'NATIONAL', 'GB/T 912-2008', '冷轧碳素钢板国家标准', 'GB/T 912-2008',
   NULL, '冷轧板', 'Q235B', '厚度0.5-3.0mm，宽度600-1500mm', '通用交付',
   'ind004', 'Δt', '厚度公差', '2009-06-01', '9999-12-31',
   'Q235B 厚度公差 0.120 0.150 让步', 'p0:gb912:6.1:dt', 'INDEXED', 'P0_STANDARD', 'ACTIVE', 'system', NOW()),
  ('p0_clause_c1_rm', 'p0_doc_c2025_088_v1', 'std002', 'C2025-088-v1-3.1-Rm', '3.1', 1,
   '供华东汽车配件有限公司的 Q235B 冷轧板抗拉强度 Rm 应为 380 MPa 至 500 MPa。该客户协议优先于企业标准和国家标准。',
   'CUSTOMER', 'CUSTOMER', '协议C2025-088-v1', '华东汽车配件客户协议', '协议C2025-088-v1',
   'CUST-001', '冷轧板', 'Q235B', '厚度1.0-2.0mm', '汽车零配件',
   'ind001', 'Rm', '抗拉强度', '2025-01-01', '2025-12-31',
   '客户协议 华东汽车 Rm 380 500 优先级', 'p0:c2025-088-v1:3.1:rm', 'INDEXED', 'P0_CUSTOMER', 'ACTIVE', 'system', NOW()),
  ('p0_clause_c1_a', 'p0_doc_c2025_088_v1', 'std002', 'C2025-088-v1-3.2-A', '3.2', 1,
   '供华东汽车配件有限公司的 Q235B 冷轧板断后延伸率 A 不应低于 28%。低于 28% 时不得按国家标准较低限值直接判合格。',
   'CUSTOMER', 'CUSTOMER', '协议C2025-088-v1', '华东汽车配件客户协议', '协议C2025-088-v1',
   'CUST-001', '冷轧板', 'Q235B', '厚度1.0-2.0mm', '汽车零配件',
   'ind002', 'A', '延伸率', '2025-01-01', '2025-12-31',
   '客户协议 华东汽车 延伸率 A 28', 'p0:c2025-088-v1:3.2:a', 'INDEXED', 'P0_CUSTOMER', 'ACTIVE', 'system', NOW()),
  ('p0_clause_c1_concession', 'p0_doc_c2025_088_v1', 'std002', 'C2025-088-v1-3.3-CONCESSION', '3.3', 1,
   '当 Rm 不低于 370 MPa 且 A 不低于 26% 时，可提交让步评审。让步评审必须记录客户用途、偏差程度、历史投诉和替代资源。',
   'CUSTOMER', 'CUSTOMER', '协议C2025-088-v1', '华东汽车配件客户协议', '协议C2025-088-v1',
   'CUST-001', '冷轧板', 'Q235B', '厚度1.0-2.0mm', '汽车零配件',
   NULL, NULL, '让步评审', '2025-01-01', '2025-12-31',
   '让步评审 客户用途 偏差 历史投诉 替代资源', 'p0:c2025-088-v1:3.3:concession', 'INDEXED', 'P0_CONCESSION', 'ACTIVE', 'system', NOW()),
  ('p0_clause_c1_dt', 'p0_doc_c2025_088_v1', 'std002', 'C2025-088-v1-3.4-DT', '3.4', 2,
   '供华东汽车配件有限公司的 Q235B 冷轧板厚度公差应控制在 -0.100 mm 至 0.100 mm。',
   'CUSTOMER', 'CUSTOMER', '协议C2025-088-v1', '华东汽车配件客户协议', '协议C2025-088-v1',
   'CUST-001', '冷轧板', 'Q235B', '厚度1.0-2.0mm', '汽车零配件',
   'ind004', 'Δt', '厚度公差', '2025-01-01', '2025-12-31',
   '客户协议 厚度公差 0.100', 'p0:c2025-088-v1:3.4:dt', 'INDEXED', 'P0_CUSTOMER', 'ACTIVE', 'system', NOW()),
  ('p0_clause_c2_rm', 'p0_doc_c2025_088_v2', 'std003', 'C2025-088-v2-3.1-Rm', '3.1', 1,
   '供华东汽车配件有限公司的 Q235B 冷轧板抗拉强度 Rm 应为 395 MPa 至 500 MPa。本协议与同一客户同一期间的协议C2025-088-v1存在下限差异时，应提交标准冲突裁决。',
   'CUSTOMER', 'CUSTOMER', '协议C2025-088-v2', '华东汽车配件客户协议冲突样例', '协议C2025-088-v2',
   'CUST-001', '冷轧板', 'Q235B', '厚度1.0-2.0mm', '汽车零配件',
   'ind001', 'Rm', '抗拉强度', '2025-04-01', '2025-12-31',
   '客户协议 冲突 Rm 395 500 裁决', 'p0:c2025-088-v2:3.1:rm', 'INDEXED', 'P0_CONFLICT', 'ACTIVE', 'system', NOW()),
  ('p0_clause_c2_a', 'p0_doc_c2025_088_v2', 'std003', 'C2025-088-v2-3.2-A', '3.2', 1,
   '供华东汽车配件有限公司的 Q235B 冷轧板断后延伸率 A 不应低于 29%。若与同优先级客户协议限值不一致，应禁止无依据改判。',
   'CUSTOMER', 'CUSTOMER', '协议C2025-088-v2', '华东汽车配件客户协议冲突样例', '协议C2025-088-v2',
   'CUST-001', '冷轧板', 'Q235B', '厚度1.0-2.0mm', '汽车零配件',
   'ind002', 'A', '延伸率', '2025-04-01', '2025-12-31',
   '客户协议 冲突 延伸率 A 29 禁止无依据改判', 'p0:c2025-088-v2:3.2:a', 'INDEXED', 'P0_CONFLICT', 'ACTIVE', 'system', NOW()),
  ('p0_clause_c2_dt', 'p0_doc_c2025_088_v2', 'std003', 'C2025-088-v2-3.3-DT', '3.3', 2,
   '供华东汽车配件有限公司的 Q235B 冷轧板厚度公差应控制在 -0.080 mm 至 0.080 mm。',
   'CUSTOMER', 'CUSTOMER', '协议C2025-088-v2', '华东汽车配件客户协议冲突样例', '协议C2025-088-v2',
   'CUST-001', '冷轧板', 'Q235B', '厚度1.0-2.0mm', '汽车零配件',
   'ind004', 'Δt', '厚度公差', '2025-04-01', '2025-12-31',
   '客户协议 冲突 厚度公差 0.080', 'p0:c2025-088-v2:3.3:dt', 'INDEXED', 'P0_CONFLICT', 'ACTIVE', 'system', NOW()),
  ('p0_clause_case_summary', 'p0_doc_case_2025_001', NULL, 'CASE-P0-2025-001-SUMMARY', 'CASE-2025-001', 1,
   '西南建材集团历史投诉显示，建筑围护件对轻微强度偏差的敏感度低于汽车结构件，但必须限制用途并保留批次追溯。',
   'CASE', NULL, NULL, 'P0历史投诉与让步案例', '2025-P0',
   'CUST-002', '冷轧板', 'Q235B', '厚度1.0-2.0mm', '建筑围护和普通结构件',
   NULL, NULL, '历史投诉', '2025-01-01', '9999-12-31',
   '西南建材 历史投诉 建筑围护 强度偏差 追溯', 'p0:case-2025-001:summary', 'INDEXED', 'P0_CASE', 'ACTIVE', 'system', NOW()),
  ('p0_clause_case_finding', 'p0_doc_case_2025_001', NULL, 'CASE-P0-2025-001-FINDING', 'CASE-2025-001', 1,
   '当 Q235B 冷轧板 Rm 低于合格下限不超过 10 MPa 且 A 低于合格下限不超过 2% 时，若客户用途为普通结构件且无同类投诉，可建议进入人工让步评审。',
   'CASE', NULL, NULL, 'P0历史投诉与让步案例', '2025-P0',
   'CUST-002', '冷轧板', 'Q235B', '厚度1.0-2.0mm', '建筑围护和普通结构件',
   'ind001', 'Rm', '抗拉强度', '2025-01-01', '9999-12-31',
   'Rm 低于 10 MPa A 低于 2 普通结构件 让步评审', 'p0:case-2025-001:finding', 'INDEXED', 'P0_CASE', 'ACTIVE', 'system', NOW()),
  ('p0_clause_case_condition', 'p0_doc_case_2025_001', NULL, 'CASE-P0-2025-001-CONDITION', 'CASE-2025-001', 1,
   '让步接收建议条件包括客户书面确认、用途限制、增加抽检和保留质保书备注。若存在同规格合格替代库存，应优先评估替代发货。',
   'CASE', NULL, NULL, 'P0历史投诉与让步案例', '2025-P0',
   'CUST-002', '冷轧板', 'Q235B', '厚度1.0-2.0mm', '建筑围护和普通结构件',
   NULL, NULL, '让步条件', '2025-01-01', '9999-12-31',
   '让步接收 客户确认 用途限制 抽检 质保书 替代库存', 'p0:case-2025-001:condition', 'INDEXED', 'P0_CASE', 'ACTIVE', 'system', NOW());

-- ============================================================
-- 7. AI 降级缓存
-- ============================================================
INSERT IGNORE INTO `qc_ai_cache` (
  `id`, `cache_key`, `assessment_type`, `business_type`, `business_id`,
  `prompt_version`, `input_hash`, `cached_output`, `references_json`,
  `confidence_label`, `confidence_score`, `degradation_source`, `enabled`,
  `create_user_no`, `create_date_time`
) VALUES
  ('cache_p0_exp_jud001', 'P0:JUDGMENT_EXPLANATION:jud001', 'JUDGMENT_EXPLANATION', 'JUDGMENT', 'jud001',
   'p0-cache-v1', 'p0-exp-jud001',
   '{"judgmentType":"QUALIFIED","summary":"该卷按 GB/T 912-2008 Q235B 结构化限值判定为合格。Rm=430 MPa 位于 370-510 MPa 范围内，A=30.5% 高于 26% 下限。","degradation":{"source":"CACHE","reason":"P0预生成判定解释缓存"},"confidence":{"label":"HIGH","score":0.92},"citations":["p0_clause_gb_rm","p0_clause_gb_a"]}',
   '{"citations":[{"clauseId":"p0_clause_gb_rm","standardCode":"GB/T 912-2008","clauseNo":"5.1"},{"clauseId":"p0_clause_gb_a","standardCode":"GB/T 912-2008","clauseNo":"5.2"}]}',
   'HIGH', 0.920000, 'CACHE', 1, 'system', NOW()),
  ('cache_p0_exp_jud002', 'P0:JUDGMENT_EXPLANATION:jud002', 'JUDGMENT_EXPLANATION', 'JUDGMENT', 'jud002',
   'p0-cache-v1', 'p0-exp-jud002',
   '{"judgmentType":"UNQUALIFIED","summary":"该卷命中客户协议 协议C2025-088-v1。Rm=360 MPa 低于客户协议下限 380 MPa，A=27% 低于客户协议下限 28%，因此判定为不合格。","degradation":{"source":"CACHE","reason":"P0预生成判定解释缓存"},"confidence":{"label":"HIGH","score":0.90},"citations":["p0_clause_c1_rm","p0_clause_c1_a"]}',
   '{"citations":[{"clauseId":"p0_clause_c1_rm","standardCode":"协议C2025-088-v1","clauseNo":"3.1"},{"clauseId":"p0_clause_c1_a","standardCode":"协议C2025-088-v1","clauseNo":"3.2"}]}',
   'HIGH', 0.900000, 'CACHE', 1, 'system', NOW()),
  ('cache_p0_exp_jud003', 'P0:JUDGMENT_EXPLANATION:jud003', 'JUDGMENT_EXPLANATION', 'JUDGMENT', 'jud003',
   'p0-cache-v1', 'p0-exp-jud003',
   '{"judgmentType":"CAN_CONCESSION","summary":"该卷按 GB/T 912-2008 Q235B 判定为可让步。Rm=365 MPa 低于合格下限 370 MPa 但未低于让步下限 360 MPa，A=25% 低于合格下限 26% 但未低于让步下限 24%。","degradation":{"source":"CACHE","reason":"P0预生成判定解释缓存"},"confidence":{"label":"MEDIUM","score":0.78},"citations":["p0_clause_gb_rm","p0_clause_gb_a","p0_clause_case_condition"]}',
   '{"citations":[{"clauseId":"p0_clause_gb_rm","standardCode":"GB/T 912-2008","clauseNo":"5.1"},{"clauseId":"p0_clause_gb_a","standardCode":"GB/T 912-2008","clauseNo":"5.2"},{"clauseId":"p0_clause_case_condition","standardCode":"CASE-P0-2025-001","clauseNo":"CASE-2025-001"}]}',
   'MEDIUM', 0.780000, 'CACHE', 1, 'system', NOW()),
  ('cache_p0_exp_jud004', 'P0:JUDGMENT_EXPLANATION:jud004', 'JUDGMENT_EXPLANATION', 'JUDGMENT', 'jud004',
   'p0-cache-v1', 'p0-exp-jud004',
   '{"judgmentType":"STANDARD_CONFLICT","summary":"该卷存在未裁决标准冲突。客户协议 v1 要求 Rm 下限 380 MPa，客户协议 v2 要求 Rm 下限 395 MPa，两个同优先级协议在同一客户、同一规格和同一有效期窗口内重叠，禁止直接给出放行结论。","degradation":{"source":"CACHE","reason":"P0预生成判定解释缓存"},"confidence":{"label":"LOW","score":0.35},"citations":["p0_clause_c1_rm","p0_clause_c2_rm"]}',
   '{"citations":[{"clauseId":"p0_clause_c1_rm","standardCode":"协议C2025-088-v1","clauseNo":"3.1"},{"clauseId":"p0_clause_c2_rm","standardCode":"协议C2025-088-v2","clauseNo":"3.1"}],"conflictId":"scf_p0_001"}',
   'LOW', 0.350000, 'CACHE', 1, 'system', NOW()),
  ('cache_p0_concession_jud003', 'P0:CONCESSION_RISK:jud003', 'CONCESSION_RISK', 'JUDGMENT', 'jud003',
   'p0-cache-v1', 'p0-concession-jud003',
   '{"riskLevel":"MEDIUM","mustReview":true,"missingInfo":[],"suggestedConditions":["限制为建筑围护和普通结构件用途","客户书面确认","增加同批次抽检","质保书备注让步依据","优先评估 ZALT001 替代发货"],"blockingReasons":[],"dimensionReasons":{"customerUsage":"客户画像为建筑围护和普通结构件，非安全关键用途。","deviationDegree":"Rm 低 5 MPa，A 低 1%，均在让步带内。","complaintHistory":"历史案例显示普通结构件可进入人工让步评审。","alternativeStock":"存在同规格可用替代资源 ZALT001。"},"confidence":{"label":"MEDIUM","score":0.76},"degradation":{"source":"CACHE","reason":"P0预生成让步风险缓存"},"evidenceRefs":["p0_clause_gb_a","p0_clause_case_finding","p0_clause_case_condition"]}',
   '{"citations":[{"clauseId":"p0_clause_gb_a","standardCode":"GB/T 912-2008","clauseNo":"5.2"},{"clauseId":"p0_clause_case_finding","standardCode":"CASE-P0-2025-001","clauseNo":"CASE-2025-001"},{"clauseId":"p0_clause_case_condition","standardCode":"CASE-P0-2025-001","clauseNo":"CASE-2025-001"}]}',
   'MEDIUM', 0.760000, 'CACHE', 1, 'system', NOW()),
  ('cache_p0_cert_qa_z001001', 'P0:CERT_QA:COIL:Z001001:WHY_CERT', 'CERT_QA', 'COIL', 'Z001001',
   'p0-cache-v1', 'p0-cert-z001001',
   '{"question":"这卷为什么能出证？","answer":"Z001001 当前最终判定为合格。关键性能指标 Rm=430 MPa 满足 370-510 MPa，A=30.5% 满足不低于 26% 的要求，因此可生成正式质保书数据。","finalState":"FINAL","confidence":{"label":"HIGH","score":0.91},"degradation":{"source":"CACHE","reason":"P0预生成质保书问答缓存"},"citations":["p0_clause_gb_rm","p0_clause_gb_a"]}',
   '{"citations":[{"clauseId":"p0_clause_gb_rm","standardCode":"GB/T 912-2008","clauseNo":"5.1"},{"clauseId":"p0_clause_gb_a","standardCode":"GB/T 912-2008","clauseNo":"5.2"}]}',
   'HIGH', 0.910000, 'CACHE', 1, 'system', NOW()),
  ('cache_p0_cert_qa_z003001', 'P0:CERT_QA:COIL:Z003001:CAN_CERT', 'CERT_QA', 'COIL', 'Z003001',
   'p0-cache-v1', 'p0-cert-z003001',
   '{"question":"这卷当前能否生成正式质保书？","answer":"Z003001 当前判定为可让步，不是正式放行状态。系统只能给出内部预览或让步说明，必须完成让步审批和客户确认后才能正式生成质保书。","finalState":"PREVIEW","confidence":{"label":"MEDIUM","score":0.74},"degradation":{"source":"CACHE","reason":"P0预生成质保书问答缓存"},"citations":["p0_clause_gb_a","p0_clause_case_condition"]}',
   '{"citations":[{"clauseId":"p0_clause_gb_a","standardCode":"GB/T 912-2008","clauseNo":"5.2"},{"clauseId":"p0_clause_case_condition","standardCode":"CASE-P0-2025-001","clauseNo":"CASE-2025-001"}]}',
   'MEDIUM', 0.740000, 'CACHE', 1, 'system', NOW()),
  ('cache_p0_cert_qa_z004001', 'P0:CERT_QA:COIL:Z004001:CONFLICT', 'CERT_QA', 'COIL', 'Z004001',
   'p0-cache-v1', 'p0-cert-z004001',
   '{"question":"这卷为什么不能出正式质保书？","answer":"Z004001 当前最终判定为 STANDARD_CONFLICT。两个同优先级客户协议对 Rm 下限要求不同，冲突尚未裁决，因此不能生成正式质保书，只能展示非最终预览和冲突原因。","finalState":"BLOCKED","confidence":{"label":"LOW","score":0.34},"degradation":{"source":"CACHE","reason":"P0预生成质保书问答缓存"},"citations":["p0_clause_c1_rm","p0_clause_c2_rm"],"conflictId":"scf_p0_001"}',
   '{"citations":[{"clauseId":"p0_clause_c1_rm","standardCode":"协议C2025-088-v1","clauseNo":"3.1"},{"clauseId":"p0_clause_c2_rm","standardCode":"协议C2025-088-v2","clauseNo":"3.1"}],"conflictId":"scf_p0_001"}',
   'LOW', 0.340000, 'CACHE', 1, 'system', NOW());
