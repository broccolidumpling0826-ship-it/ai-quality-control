-- ============================================================
-- 标准库初始化 — 指标项目 + 质量标准 + 标准指标限值
-- MySQL 5.7+ | 幂等：INSERT IGNORE
-- ============================================================
--
-- 【用途】
--   初始化「标准库 → 指标项目」「标准库 → 标准维护」所需主数据，
--   不含检验记录、判定、RAG 文档等下游业务数据。
--
-- 【涉及表】
--   qc_indicator_item      指标项目主数据
--   qc_quality_standard    质量标准（国标/企标/客协）
--   qc_standard_indicator  标准下的指标限值（含让步上下限）
--
-- 【前置条件】
--   1. 已执行 init-schema.sql（表结构存在）
--   2. 建议已执行 init-dict-data.sql（字典项）；本脚本会补写客协依赖的最小字典
--
-- 【用法】
--   mysql -u root -p -h 127.0.0.1 -P 3307 ai_quality_control < init-standard-indicator-data.sql
--
-- 【可选扩展】
--   PDF 上传联调草稿标准：init-mock-standards-for-pdf-test.sql
--
-- ============================================================

USE ai_quality_control;

-- ────────────────────────────────────────────────────────────
-- 0. 最小字典依赖（客协标准绑定客户；若 init-dict-data 已执行可跳过）
-- ────────────────────────────────────────────────────────────
INSERT IGNORE INTO sys_dict_item (id, dict_code, item_value, item_label, sort_no, status, is_system, create_date_time, update_date_time) VALUES
('di161', 'QC_CUSTOMER', 'CUST-001', '华东汽车配件有限公司', 1, 1, 0, NOW(), NOW()),
('di162', 'QC_CUSTOMER', 'CUST-002', '西南建材集团',         2, 1, 0, NOW(), NOW());

INSERT IGNORE INTO sys_dict_item (id, dict_code, item_value, item_label, sort_no, status, is_system, create_date_time, update_date_time) VALUES
('di131', 'PRODUCT_VARIETY', '冷轧板', '冷轧板', 1, 1, 0, NOW(), NOW()),
('di132', 'PRODUCT_VARIETY', '热轧板', '热轧板', 2, 1, 0, NOW(), NOW()),
('di141', 'PRODUCT_GRADE', 'Q235B', 'Q235B', 1, 1, 0, NOW(), NOW()),
('di142', 'PRODUCT_GRADE', 'SPHC',  'SPHC',  2, 1, 0, NOW(), NOW());

-- ────────────────────────────────────────────────────────────
-- 1. 指标项目（qc_indicator_item）
--    菜单：标准库 → 指标项目
-- ────────────────────────────────────────────────────────────
INSERT IGNORE INTO qc_indicator_item (
  id, indicator_name, indicator_code, indicator_category, unit,
  test_method, description, status, is_deleted,
  create_date_time, update_date_time
) VALUES
('ind001', '抗拉强度', 'Rm',  'PERFORMANCE', 'MPa', 'GB/T 228.1', '最大拉伸力/原始横截面积',           'ACTIVE', 0, NOW(), NOW()),
('ind002', '延伸率',   'A',   'PERFORMANCE', '%',   'GB/T 228.1', '断后伸长量/原始标距长度',            'ACTIVE', 0, NOW(), NOW()),
('ind003', '屈服强度', 'ReL', 'PERFORMANCE', 'MPa', 'GB/T 228.1', '屈服点的应力值',                     'ACTIVE', 0, NOW(), NOW()),
('ind004', '厚度公差', 'Δt',  'DIMENSION',   'mm',  NULL,         '实测厚度与名义厚度之差',             'ACTIVE', 0, NOW(), NOW()),
('ind005', '宽度公差', 'Δw',  'DIMENSION',   'mm',  NULL,         '实测宽度与名义宽度之差',             'ACTIVE', 0, NOW(), NOW()),
('ind006', '表面等级', 'FS',  'SURFACE',     '-',   NULL,         'FB/FC/FD 表面质量等级',              'ACTIVE', 0, NOW(), NOW()),
('ind007', '硬度',     'HV',  'PERFORMANCE', 'HV',  'GB/T 4340.1','Vickers 硬度',                       'ACTIVE', 0, NOW(), NOW()),
('ind008', 'n值',      'n',   'PERFORMANCE', '-',   'GB/T 5027',  '应变硬化指数',                       'ACTIVE', 0, NOW(), NOW());

-- ────────────────────────────────────────────────────────────
-- 2. 质量标准（qc_quality_standard）
--    菜单：标准库 → 标准维护
-- ────────────────────────────────────────────────────────────

-- 2.1 国标 Q235B 冷轧板（已发布）
INSERT IGNORE INTO qc_quality_standard (
  id, standard_type, standard_code, standard_name, variety, grade, spec_range,
  version_no, effective_date, expiry_date, status, customer_id, remark,
  create_date_time, update_date_time
) VALUES (
  'std001', 'NATIONAL', 'GB/T 912-2008', '冷轧碳素钢板国家标准',
  '冷轧板', 'Q235B', '厚度0.5-3.0mm，宽度600-1500mm',
  'GB/T 912-2008', '2009-06-01', '9999-12-31', 'PUBLISHED', NULL,
  '冷轧碳素钢板国家标准', NOW(), NOW()
);

-- 2.2 客协 v1 — 华东汽车 CUST-001（已发布，限值严于国标）
INSERT IGNORE INTO qc_quality_standard (
  id, standard_type, standard_code, standard_name, variety, grade, spec_range,
  version_no, effective_date, expiry_date, status, customer_id, remark,
  create_date_time, update_date_time
) VALUES (
  'std002', 'CUSTOMER', '协议C2025-088-v1', '华南汽车零配件厂客户协议',
  '冷轧板', 'Q235B', '厚度1.0-2.0mm',
  '协议C2025-088-v1', '2025-01-01', '2025-12-31', 'PUBLISHED', 'CUST-001',
  '华东汽车配件客户协议 v1', NOW(), NOW()
);

-- 2.3 客协 v2 — 同客户冲突样例（已发布，与 std002 限值不一致，用于标准冲突演示）
INSERT IGNORE INTO qc_quality_standard (
  id, standard_type, standard_code, standard_name, variety, grade, spec_range,
  version_no, effective_date, expiry_date, status, customer_id, remark,
  create_date_time, update_date_time
) VALUES (
  'std003', 'CUSTOMER', '协议C2025-088-v2-冲突样例', '华东汽车配件客户协议冲突样例',
  '冷轧板', 'Q235B', '厚度1.0-2.0mm',
  '协议C2025-088-v2', '2025-04-01', '2025-12-31', 'PUBLISHED', 'CUST-001',
  'P0 标准冲突演示：与 std002 同优先级同范围但限值不一致', NOW(), NOW()
);

-- ────────────────────────────────────────────────────────────
-- 3. 标准指标限值（qc_standard_indicator）
--    关联：standard_id → qc_quality_standard.id
--          indicator_id → qc_indicator_item.id
-- ────────────────────────────────────────────────────────────

-- 3.1 国标 std001
INSERT IGNORE INTO qc_standard_indicator (
  id, standard_id, indicator_id, upper_limit, lower_limit, is_required,
  concession_upper, concession_lower, create_date_time, update_date_time
) VALUES
('si001', 'std001', 'ind001', 510.000000, 370.000000, 1, 520.000000, 360.000000, NOW(), NOW()),
('si002', 'std001', 'ind002',       NULL,  26.000000, 1,       NULL,  24.000000, NOW(), NOW()),
('si003', 'std001', 'ind003',       NULL, 235.000000, 1,       NULL,        NULL, NOW(), NOW()),
('si004', 'std001', 'ind004',   0.120000,  -0.120000, 1,   0.150000,  -0.150000, NOW(), NOW());

-- 3.2 客协 std002
INSERT IGNORE INTO qc_standard_indicator (
  id, standard_id, indicator_id, upper_limit, lower_limit, is_required,
  concession_upper, concession_lower, create_date_time, update_date_time
) VALUES
('si011', 'std002', 'ind001', 500.000000, 380.000000, 1, 510.000000, 370.000000, NOW(), NOW()),
('si012', 'std002', 'ind002',       NULL,  28.000000, 1,       NULL,  26.000000, NOW(), NOW()),
('si013', 'std002', 'ind004',   0.100000,  -0.100000, 1,   0.120000,  -0.120000, NOW(), NOW());

-- 3.3 客协 std003（冲突样例）
INSERT IGNORE INTO qc_standard_indicator (
  id, standard_id, indicator_id, upper_limit, lower_limit, is_required,
  concession_upper, concession_lower, create_date_time, update_date_time
) VALUES
('si021', 'std003', 'ind001', 500.000000, 395.000000, 1, 510.000000, 385.000000, NOW(), NOW()),
('si022', 'std003', 'ind002',       NULL,  29.000000, 1,       NULL,  27.000000, NOW(), NOW()),
('si023', 'std003', 'ind004',   0.080000,  -0.080000, 1,   0.100000,  -0.100000, NOW(), NOW());

-- ────────────────────────────────────────────────────────────
-- 初始化结果校验
-- ────────────────────────────────────────────────────────────
SELECT 'init-standard-indicator-data completed' AS status,
       (SELECT COUNT(*) FROM qc_indicator_item WHERE is_deleted = 0)     AS indicator_count,
       (SELECT COUNT(*) FROM qc_quality_standard)                        AS standard_count,
       (SELECT COUNT(*) FROM qc_standard_indicator)                       AS standard_indicator_count;

-- 预期：indicator_count=8, standard_count=3, standard_indicator_count=10

-- ────────────────────────────────────────────────────────────
-- 数据关系速查
-- ────────────────────────────────────────────────────────────
-- | 标准 ID | 类型     | 编号              | 牌号  | 指标数 |
-- |---------|----------|-------------------|-------|--------|
-- | std001  | NATIONAL | GB/T 912-2008     | Q235B | 4      |
-- | std002  | CUSTOMER | 协议C2025-088-v1  | Q235B | 3      |
-- | std003  | CUSTOMER | 协议C2025-088-v2  | Q235B | 3      |
--
-- 判定引擎读取路径：检验值 → 按品种/牌号/客户/日期匹配标准 → qc_standard_indicator 限值
