USE ai_quality_control;

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
('di034', 'JUDGMENT_TYPE', 'CAN_CONCESSION',    '可让步', 'primary', 4, 1, 1, NOW(), NOW());

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
