-- 两份模拟标准（草稿），用于标准维护页 PDF 上传/发布入库联调
-- 对应 PDF：backend/scripts/demo-documents/mock-standard-A-q345b-enterprise.pdf
--           backend/scripts/demo-documents/mock-standard-B-q235b-customer.pdf
-- 使用方式：mysql ... < backend/scripts/init-mock-standards-for-pdf-test.sql

USE ai_quality_control;

INSERT IGNORE INTO qc_quality_standard (
    id, standard_type, standard_code, standard_name, variety, grade, spec_range,
    version_no, effective_date, expiry_date, status, customer_id, remark,
    create_date_time, update_date_time
) VALUES
(
    'stdmock001', 'ENTERPRISE', 'Q/ZX-STEEL-2026-Q345B', 'Q345B低合金高强度结构钢企业标准',
    '热轧板', 'Q345B', '厚度2.0-12.0mm，宽度900-1800mm',
    'V2026.1', '2026-01-01', '9999-12-31', 'DRAFT', NULL,
    '模拟企业标准，配套 PDF：mock-standard-A-q345b-enterprise.pdf', NOW(), NOW()
),
(
    'stdmock002', 'CUSTOMER', '协议D2026-001-v1', '西南建材集团Q235B冷轧板供货协议',
    '冷轧板', 'Q235B', '厚度0.8-2.5mm，宽度800-1250mm',
    '协议D2026-001-v1', '2026-01-01', '2026-12-31', 'DRAFT', 'CUST-002',
    '模拟客户协议，配套 PDF：mock-standard-B-q235b-customer.pdf', NOW(), NOW()
);

INSERT IGNORE INTO qc_standard_indicator (
    id, standard_id, indicator_id, upper_limit, lower_limit, is_required,
    concession_upper, concession_lower, create_date_time, update_date_time
) VALUES
-- stdmock001 Q345B 企业标准
('sim001', 'stdmock001', 'ind001', 630.000000, 470.000000, 1, 640.000000, 460.000000, NOW(), NOW()),
('sim002', 'stdmock001', 'ind003', 460.000000, 345.000000, 1, NULL, NULL, NOW(), NOW()),
('sim003', 'stdmock001', 'ind002', NULL, 20.000000, 1, NULL, 18.000000, NOW(), NOW()),
('sim004', 'stdmock001', 'ind004', 0.200000, -0.200000, 1, 0.250000, -0.250000, NOW(), NOW()),
-- stdmock002 Q235B 客户协议
('sim011', 'stdmock002', 'ind001', 505.000000, 375.000000, 1, 510.000000, 370.000000, NOW(), NOW()),
('sim012', 'stdmock002', 'ind002', NULL, 27.000000, 1, NULL, 25.000000, NOW(), NOW()),
('sim013', 'stdmock002', 'ind003', 360.000000, 235.000000, 1, NULL, NULL, NOW(), NOW()),
('sim014', 'stdmock002', 'ind004', 0.100000, -0.100000, 1, 0.130000, -0.130000, NOW(), NOW());
