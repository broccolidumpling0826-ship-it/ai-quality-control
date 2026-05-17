USE ai_quality_control;

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
