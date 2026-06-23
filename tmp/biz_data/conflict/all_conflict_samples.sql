USE ai_quality_control;


-- ===== conflict_sample_001_HC340LA_customer.sql =====
-- ============================================================
-- Conflict sample 001: HC340LA_customer
-- Source PDF: /Users/weixuelei/workspace/aiAgent/ai-project/for-test/std-doc/CUST-HDQC-SIM-2026-HC340LA_customer_agreement.pdf
-- Rerunnable: INSERT IGNORE only.
-- ============================================================

INSERT IGNORE INTO sys_dict_item
(id, dict_code, item_value, item_label, color_tag, sort_no, status, is_system, remark, create_date_time, update_date_time)
VALUES
('td_cf_var_01', 'PRODUCT_VARIETY', '冷轧低合金高强钢板', '冷轧低合金高强钢板', '', 711, 1, 0, '测试包：冲突标准样例', NOW(), NOW()),
('td_cf_grade_01', 'PRODUCT_GRADE', 'HC340LA-QA0623', 'HC340LA-QA0623', '', 712, 1, 0, '测试包：冲突标准结构化牌号使用QA0623后缀', NOW(), NOW()),
('td_cf_cust_01', 'QC_CUSTOMER', 'TD-CUST-HDQC-CF', '华东汽车配件有限公司冲突测试客户', '', 713, 1, 0, '测试包：冲突标准独立客户，避免影响普通检验记录', NOW(), NOW());

INSERT IGNORE INTO qc_indicator_item
(id, indicator_name, indicator_code, indicator_category, unit, test_method, description, status, is_deleted, create_date_time, update_date_time)
VALUES
('ind001', '抗拉强度', 'Rm', 'PERFORMANCE', 'MPa', 'GB/T 228.1', '最大拉伸力/原始横截面积', 'ACTIVE', 0, NOW(), NOW()),
('ind002', '延伸率', 'A', 'PERFORMANCE', '%', 'GB/T 228.1', '断后伸长量/原始标距长度', 'ACTIVE', 0, NOW(), NOW()),
('ind003', '屈服强度', 'ReL', 'PERFORMANCE', 'MPa', 'GB/T 228.1', '屈服点的应力值', 'ACTIVE', 0, NOW(), NOW()),
('ind004', '厚度公差', 'Δt', 'DIMENSION', 'mm', '千分尺测厚', '实测厚度与名义厚度之差', 'ACTIVE', 0, NOW(), NOW());

INSERT IGNORE INTO qc_quality_standard
(id, standard_type, standard_code, standard_name, variety, grade, spec_range, version_no, effective_date, expiry_date, status, customer_id, remark, create_user_no, create_date_time, update_date_time)
VALUES
('td_conf_01_v1', 'CUSTOMER', 'AGREEMENT HDQC-SIM-2026-002-QA0623', '华东汽车配件有限公司HC340LA冷轧结构板供货质量协议（模拟客户协议）基准版', '冷轧低合金高强钢板', 'HC340LA-QA0623', '厚度0.90mm-2.00mm，宽度900mm-1450mm', '协议V1.0-CF-QA0623', '2026-03-01', '2026-12-31', 'PUBLISHED', 'TD-CUST-HDQC-CF', '冲突样例V1：客户协议基准版；来源PDF：CUST-HDQC-SIM-2026-HC340LA_customer_agreement.pdf', 'system', NOW(), NOW()),
('td_conf_01_v2', 'CUSTOMER', 'AGREEMENT HDQC-SIM-2026-002-REV-CF-QA0623', '华东汽车配件有限公司HC340LA冷轧结构板供货质量协议（模拟客户协议）冲突版', '冷轧低合金高强钢板', 'HC340LA-QA0623', '厚度0.90mm-2.00mm，宽度900mm-1450mm', '协议V1.1-CF-QA0623', '2026-04-15', '2026-12-31', 'PUBLISHED', 'TD-CUST-HDQC-CF', '冲突样例V2：同优先级同范围限值收严；来源PDF：CUST-HDQC-SIM-2026-HC340LA_customer_agreement.pdf', 'system', NOW(), NOW());

INSERT IGNORE INTO qc_standard_indicator
(id, standard_id, indicator_id, upper_limit, lower_limit, is_required, concession_upper, concession_lower, create_date_time, update_date_time)
VALUES
('td_csi_01_1_1', 'td_conf_01_v1', 'ind003', 410, 340, 1, 420, 330, NOW(), NOW()),
('td_csi_01_1_2', 'td_conf_01_v1', 'ind001', 500, 420, 1, NULL, NULL, NOW(), NOW()),
('td_csi_01_1_3', 'td_conf_01_v1', 'ind002', NULL, 23, 1, NULL, 22, NOW(), NOW()),
('td_csi_01_1_4', 'td_conf_01_v1', 'ind004', 0.05, -0.05, 1, 0.06, -0.06, NOW(), NOW()),
('td_csi_01_2_1', 'td_conf_01_v2', 'ind003', 405, 350, 1, NULL, NULL, NOW(), NOW()),
('td_csi_01_2_2', 'td_conf_01_v2', 'ind001', 495, 430, 1, NULL, NULL, NOW(), NOW()),
('td_csi_01_2_3', 'td_conf_01_v2', 'ind002', NULL, 24, 1, NULL, NULL, NOW(), NOW()),
('td_csi_01_2_4', 'td_conf_01_v2', 'ind004', 0.045, -0.04, 1, NULL, NULL, NOW(), NOW());

INSERT IGNORE INTO qc_inspection_record
(id, heat_no, coil_no, batch_no, sample_type, test_time, tester_no, customer_id, product_variety, product_grade, product_spec, status, create_user_no, create_date_time, update_date_time)
VALUES
('td_conf_ins_01', 'HT-TD-CF-01-001', 'COIL-TD-CF-01-001', 'HT-TD-CF-01-001', 'MIDDLE', '2026-06-28 10:07:00', '021001', 'TD-CUST-HDQC-CF', '冷轧低合金高强钢板', 'HC340LA-QA0623', '厚度1.20mm，宽度1200mm', 'NORMAL', 'system', NOW(), NOW());

INSERT IGNORE INTO qc_inspection_value
(id, record_id, indicator_id, test_value, value_text, create_user_no, create_date_time, update_date_time)
VALUES
('td_civ_01_1', 'td_conf_ins_01', 'ind003', 345, NULL, 'system', NOW(), NOW()),
('td_civ_01_2', 'td_conf_ins_01', 'ind001', 460, NULL, 'system', NOW(), NOW()),
('td_civ_01_3', 'td_conf_ins_01', 'ind002', 25, NULL, 'system', NOW(), NOW()),
('td_civ_01_4', 'td_conf_ins_01', 'ind004', 0.00, NULL, 'system', NOW(), NOW());

INSERT IGNORE INTO qc_judgment_result
(id, record_id, judgment_type, judgment_time, is_final, matched_standard_ids, remark, create_user_no, create_date_time, update_date_time)
VALUES
('td_conf_jud_01', 'td_conf_ins_01', 'STANDARD_CONFLICT', '2026-06-28 10:07:05', 1, '["td_conf_01_v1", "td_conf_01_v2"]', '测试包冲突样例：同优先级客户协议结构化限值冲突，需人工裁决', 'system', NOW(), NOW());

INSERT IGNORE INTO standard_conflict
(id, conflict_no, judgment_id, record_id, conflict_type, conflict_level, status, indicator_id, indicator_name, unit, customer_id, variety, grade, product_spec, inspection_date, selected_standard_id, involved_standard_ids, conflict_detail, selected_priority, create_user_no, create_date_time, update_date_time)
VALUES
('td_scf_01', 'TD-SCF-001', 'td_conf_jud_01', 'td_conf_ins_01', 'NUMERIC_LIMIT', 'BLOCKING', 'PENDING', 'ind003', '屈服强度', 'MPa', 'TD-CUST-HDQC-CF', '冷轧低合金高强钢板', 'HC340LA-QA0623', '厚度1.20mm，宽度1200mm', '2026-06-28', NULL, '["td_conf_01_v1", "td_conf_01_v2"]', '{"reason": "同一客户、同一品种牌号、同一规格窗口内存在两份已发布客户协议，且有效期重叠；至少一个关键指标结构化限值不一致。", "standards": [{"standardId": "td_conf_01_v1", "standardCode": "AGREEMENT HDQC-SIM-2026-002-QA0623", "indicator": "ind003", "lowerLimit": 340.0, "upperLimit": 410.0}, {"standardId": "td_conf_01_v2", "standardCode": "AGREEMENT HDQC-SIM-2026-002-REV-CF-QA0623", "indicator": "ind003", "lowerLimit": 350.0, "upperLimit": 405.0}], "blocking": true}', 'CUSTOMER', 'system', NOW(), NOW());


-- ===== conflict_sample_002_Q235B_customer.sql =====
-- ============================================================
-- Conflict sample 002: Q235B_customer
-- Source PDF: /Users/weixuelei/workspace/aiAgent/ai-project/for-test/std-doc/CUST-XNJC-SIM-2026-Q235B_customer_agreement.pdf
-- Rerunnable: INSERT IGNORE only.
-- ============================================================

INSERT IGNORE INTO sys_dict_item
(id, dict_code, item_value, item_label, color_tag, sort_no, status, is_system, remark, create_date_time, update_date_time)
VALUES
('td_cf_var_02', 'PRODUCT_VARIETY', '冷轧板', '冷轧板', '', 721, 1, 0, '测试包：冲突标准样例', NOW(), NOW()),
('td_cf_grade_02', 'PRODUCT_GRADE', 'Q235B-QA0623', 'Q235B-QA0623', '', 722, 1, 0, '测试包：冲突标准结构化牌号使用QA0623后缀', NOW(), NOW()),
('td_cf_cust_02', 'QC_CUSTOMER', 'TD-CUST-XNJC-CF', '西南建材集团冲突测试客户', '', 723, 1, 0, '测试包：冲突标准独立客户，避免影响普通检验记录', NOW(), NOW());

INSERT IGNORE INTO qc_indicator_item
(id, indicator_name, indicator_code, indicator_category, unit, test_method, description, status, is_deleted, create_date_time, update_date_time)
VALUES
('ind001', '抗拉强度', 'Rm', 'PERFORMANCE', 'MPa', 'GB/T 228.1', '最大拉伸力/原始横截面积', 'ACTIVE', 0, NOW(), NOW()),
('ind002', '延伸率', 'A', 'PERFORMANCE', '%', 'GB/T 228.1', '断后伸长量/原始标距长度', 'ACTIVE', 0, NOW(), NOW()),
('ind003', '屈服强度', 'ReL', 'PERFORMANCE', 'MPa', 'GB/T 228.1', '屈服点的应力值', 'ACTIVE', 0, NOW(), NOW()),
('ind004', '厚度公差', 'Δt', 'DIMENSION', 'mm', '千分尺测厚', '实测厚度与名义厚度之差', 'ACTIVE', 0, NOW(), NOW());

INSERT IGNORE INTO qc_quality_standard
(id, standard_type, standard_code, standard_name, variety, grade, spec_range, version_no, effective_date, expiry_date, status, customer_id, remark, create_user_no, create_date_time, update_date_time)
VALUES
('td_conf_02_v1', 'CUSTOMER', 'AGREEMENT XNJC-SIM-2026-001-QA0623', '西南建材集团Q235B冷轧板供货质量协议（模拟客户协议）基准版', '冷轧板', 'Q235B-QA0623', '厚度0.80mm-2.50mm，宽度800mm-1250mm', '协议V1.0-CF-QA0623', '2026-03-01', '2026-12-31', 'PUBLISHED', 'TD-CUST-XNJC-CF', '冲突样例V1：客户协议基准版；来源PDF：CUST-XNJC-SIM-2026-Q235B_customer_agreement.pdf', 'system', NOW(), NOW()),
('td_conf_02_v2', 'CUSTOMER', 'AGREEMENT XNJC-SIM-2026-001-REV-CF-QA0623', '西南建材集团Q235B冷轧板供货质量协议（模拟客户协议）冲突版', '冷轧板', 'Q235B-QA0623', '厚度0.80mm-2.50mm，宽度800mm-1250mm', '协议V1.1-CF-QA0623', '2026-04-15', '2026-12-31', 'PUBLISHED', 'TD-CUST-XNJC-CF', '冲突样例V2：同优先级同范围限值收严；来源PDF：CUST-XNJC-SIM-2026-Q235B_customer_agreement.pdf', 'system', NOW(), NOW());

INSERT IGNORE INTO qc_standard_indicator
(id, standard_id, indicator_id, upper_limit, lower_limit, is_required, concession_upper, concession_lower, create_date_time, update_date_time)
VALUES
('td_csi_02_1_1', 'td_conf_02_v1', 'ind001', 505, 375, 1, 510, 370, NOW(), NOW()),
('td_csi_02_1_2', 'td_conf_02_v1', 'ind002', NULL, 27, 1, NULL, NULL, NOW(), NOW()),
('td_csi_02_1_3', 'td_conf_02_v1', 'ind003', 360, 235, 1, 370, 225, NOW(), NOW()),
('td_csi_02_1_4', 'td_conf_02_v1', 'ind004', 0.08, -0.08, 1, 0.1, -0.1, NOW(), NOW()),
('td_csi_02_2_1', 'td_conf_02_v2', 'ind001', 500, 385, 1, NULL, NULL, NOW(), NOW()),
('td_csi_02_2_2', 'td_conf_02_v2', 'ind002', NULL, 28, 1, NULL, NULL, NOW(), NOW()),
('td_csi_02_2_3', 'td_conf_02_v2', 'ind003', 355, 245, 1, NULL, NULL, NOW(), NOW()),
('td_csi_02_2_4', 'td_conf_02_v2', 'ind004', 0.075, -0.07, 1, NULL, NULL, NOW(), NOW());

INSERT IGNORE INTO qc_inspection_record
(id, heat_no, coil_no, batch_no, sample_type, test_time, tester_no, customer_id, product_variety, product_grade, product_spec, status, create_user_no, create_date_time, update_date_time)
VALUES
('td_conf_ins_02', 'HT-TD-CF-02-001', 'COIL-TD-CF-02-001', 'HT-TD-CF-02-001', 'MIDDLE', '2026-06-28 10:14:00', '021001', 'TD-CUST-XNJC-CF', '冷轧板', 'Q235B-QA0623', '厚度1.20mm，宽度1000mm', 'NORMAL', 'system', NOW(), NOW());

INSERT IGNORE INTO qc_inspection_value
(id, record_id, indicator_id, test_value, value_text, create_user_no, create_date_time, update_date_time)
VALUES
('td_civ_02_1', 'td_conf_ins_02', 'ind001', 380, NULL, 'system', NOW(), NOW()),
('td_civ_02_2', 'td_conf_ins_02', 'ind002', 29, NULL, 'system', NOW(), NOW()),
('td_civ_02_3', 'td_conf_ins_02', 'ind003', 297.5, NULL, 'system', NOW(), NOW()),
('td_civ_02_4', 'td_conf_ins_02', 'ind004', 0.00, NULL, 'system', NOW(), NOW());

INSERT IGNORE INTO qc_judgment_result
(id, record_id, judgment_type, judgment_time, is_final, matched_standard_ids, remark, create_user_no, create_date_time, update_date_time)
VALUES
('td_conf_jud_02', 'td_conf_ins_02', 'STANDARD_CONFLICT', '2026-06-28 10:14:05', 1, '["td_conf_02_v1", "td_conf_02_v2"]', '测试包冲突样例：同优先级客户协议结构化限值冲突，需人工裁决', 'system', NOW(), NOW());

INSERT IGNORE INTO standard_conflict
(id, conflict_no, judgment_id, record_id, conflict_type, conflict_level, status, indicator_id, indicator_name, unit, customer_id, variety, grade, product_spec, inspection_date, selected_standard_id, involved_standard_ids, conflict_detail, selected_priority, create_user_no, create_date_time, update_date_time)
VALUES
('td_scf_02', 'TD-SCF-002', 'td_conf_jud_02', 'td_conf_ins_02', 'NUMERIC_LIMIT', 'BLOCKING', 'PENDING', 'ind001', '抗拉强度', 'MPa', 'TD-CUST-XNJC-CF', '冷轧板', 'Q235B-QA0623', '厚度1.20mm，宽度1000mm', '2026-06-28', NULL, '["td_conf_02_v1", "td_conf_02_v2"]', '{"reason": "同一客户、同一品种牌号、同一规格窗口内存在两份已发布客户协议，且有效期重叠；至少一个关键指标结构化限值不一致。", "standards": [{"standardId": "td_conf_02_v1", "standardCode": "AGREEMENT XNJC-SIM-2026-001-QA0623", "indicator": "ind001", "lowerLimit": 375.0, "upperLimit": 505.0}, {"standardId": "td_conf_02_v2", "standardCode": "AGREEMENT XNJC-SIM-2026-001-REV-CF-QA0623", "indicator": "ind001", "lowerLimit": 385.0, "upperLimit": 500.0}], "blocking": true}', 'CUSTOMER', 'system', NOW(), NOW());


-- ===== conflict_sample_003_DX56D_Z_customer.sql =====
-- ============================================================
-- Conflict sample 003: DX56D_Z_customer
-- Source PDF: /Users/weixuelei/workspace/aiAgent/ai-project/for-test/std-doc/CUST-BYEV-SIM-2026-DX56D-Z_customer_agreement.pdf
-- Rerunnable: INSERT IGNORE only.
-- ============================================================

INSERT IGNORE INTO sys_dict_item
(id, dict_code, item_value, item_label, color_tag, sort_no, status, is_system, remark, create_date_time, update_date_time)
VALUES
('td_cf_var_03', 'PRODUCT_VARIETY', '热镀锌深冲板', '热镀锌深冲板', '', 731, 1, 0, '测试包：冲突标准样例', NOW(), NOW()),
('td_cf_grade_03', 'PRODUCT_GRADE', 'DX56D+Z-QA0623', 'DX56D+Z-QA0623', '', 732, 1, 0, '测试包：冲突标准结构化牌号使用QA0623后缀', NOW(), NOW()),
('td_cf_cust_03', 'QC_CUSTOMER', 'TD-CUST-BYEV-CF', '北源新能源冲突测试客户', '', 733, 1, 0, '测试包：冲突标准独立客户，避免影响普通检验记录', NOW(), NOW());

INSERT IGNORE INTO qc_indicator_item
(id, indicator_name, indicator_code, indicator_category, unit, test_method, description, status, is_deleted, create_date_time, update_date_time)
VALUES
('ind002', '延伸率', 'A', 'PERFORMANCE', '%', 'GB/T 228.1', '断后伸长量/原始标距长度', 'ACTIVE', 0, NOW(), NOW()),
('ind003', '屈服强度', 'ReL', 'PERFORMANCE', 'MPa', 'GB/T 228.1', '屈服点的应力值', 'ACTIVE', 0, NOW(), NOW()),
('ind004', '厚度公差', 'Δt', 'DIMENSION', 'mm', '千分尺测厚', '实测厚度与名义厚度之差', 'ACTIVE', 0, NOW(), NOW()),
('td_ind_coat_mass', '镀层重量', 'TD_COAT', 'PERFORMANCE', 'g/m2', '三点称量法', '双面镀层重量', 'ACTIVE', 0, NOW(), NOW());

INSERT IGNORE INTO qc_quality_standard
(id, standard_type, standard_code, standard_name, variety, grade, spec_range, version_no, effective_date, expiry_date, status, customer_id, remark, create_user_no, create_date_time, update_date_time)
VALUES
('td_conf_03_v1', 'CUSTOMER', 'AGREEMENT BYEV-SIM-2026-003-QA0623', '北源新能源DX56D+Z热镀锌深冲板供货质量协议（模拟客户协议）基准版', '热镀锌深冲板', 'DX56D+Z-QA0623', '厚度0.60mm-1.60mm，宽度900mm-1450mm', '协议V1.0-CF-QA0623', '2026-03-01', '2026-12-31', 'PUBLISHED', 'TD-CUST-BYEV-CF', '冲突样例V1：客户协议基准版；来源PDF：CUST-BYEV-SIM-2026-DX56D-Z_customer_agreement.pdf', 'system', NOW(), NOW()),
('td_conf_03_v2', 'CUSTOMER', 'AGREEMENT BYEV-SIM-2026-003-REV-CF-QA0623', '北源新能源DX56D+Z热镀锌深冲板供货质量协议（模拟客户协议）冲突版', '热镀锌深冲板', 'DX56D+Z-QA0623', '厚度0.60mm-1.60mm，宽度900mm-1450mm', '协议V1.1-CF-QA0623', '2026-04-15', '2026-12-31', 'PUBLISHED', 'TD-CUST-BYEV-CF', '冲突样例V2：同优先级同范围限值收严；来源PDF：CUST-BYEV-SIM-2026-DX56D-Z_customer_agreement.pdf', 'system', NOW(), NOW());

INSERT IGNORE INTO qc_standard_indicator
(id, standard_id, indicator_id, upper_limit, lower_limit, is_required, concession_upper, concession_lower, create_date_time, update_date_time)
VALUES
('td_csi_03_1_1', 'td_conf_03_v1', 'ind002', NULL, 39, 1, NULL, 37, NOW(), NOW()),
('td_csi_03_1_2', 'td_conf_03_v1', 'td_ind_coat_mass', 140, 70, 1, NULL, NULL, NOW(), NOW()),
('td_csi_03_1_3', 'td_conf_03_v1', 'ind003', 180, 120, 1, 190, 110, NOW(), NOW()),
('td_csi_03_1_4', 'td_conf_03_v1', 'ind004', 0.05, -0.05, 1, 0.06, -0.06, NOW(), NOW()),
('td_csi_03_2_1', 'td_conf_03_v2', 'ind002', NULL, 40, 1, NULL, NULL, NOW(), NOW()),
('td_csi_03_2_2', 'td_conf_03_v2', 'td_ind_coat_mass', 135, 71, 1, NULL, NULL, NOW(), NOW()),
('td_csi_03_2_3', 'td_conf_03_v2', 'ind003', 175, 130, 1, NULL, NULL, NOW(), NOW()),
('td_csi_03_2_4', 'td_conf_03_v2', 'ind004', 0.045, -0.04, 1, NULL, NULL, NOW(), NOW());

INSERT IGNORE INTO qc_inspection_record
(id, heat_no, coil_no, batch_no, sample_type, test_time, tester_no, customer_id, product_variety, product_grade, product_spec, status, create_user_no, create_date_time, update_date_time)
VALUES
('td_conf_ins_03', 'HT-TD-CF-03-001', 'COIL-TD-CF-03-001', 'HT-TD-CF-03-001', 'MIDDLE', '2026-06-28 10:21:00', '021001', 'TD-CUST-BYEV-CF', '热镀锌深冲板', 'DX56D+Z-QA0623', '厚度0.80mm，宽度1250mm', 'NORMAL', 'system', NOW(), NOW());

INSERT IGNORE INTO qc_inspection_value
(id, record_id, indicator_id, test_value, value_text, create_user_no, create_date_time, update_date_time)
VALUES
('td_civ_03_1', 'td_conf_ins_03', 'ind002', 39.5, NULL, 'system', NOW(), NOW()),
('td_civ_03_2', 'td_conf_ins_03', 'td_ind_coat_mass', 105, NULL, 'system', NOW(), NOW()),
('td_civ_03_3', 'td_conf_ins_03', 'ind003', 150, NULL, 'system', NOW(), NOW()),
('td_civ_03_4', 'td_conf_ins_03', 'ind004', 0.00, NULL, 'system', NOW(), NOW());

INSERT IGNORE INTO qc_judgment_result
(id, record_id, judgment_type, judgment_time, is_final, matched_standard_ids, remark, create_user_no, create_date_time, update_date_time)
VALUES
('td_conf_jud_03', 'td_conf_ins_03', 'STANDARD_CONFLICT', '2026-06-28 10:21:05', 1, '["td_conf_03_v1", "td_conf_03_v2"]', '测试包冲突样例：同优先级客户协议结构化限值冲突，需人工裁决', 'system', NOW(), NOW());

INSERT IGNORE INTO standard_conflict
(id, conflict_no, judgment_id, record_id, conflict_type, conflict_level, status, indicator_id, indicator_name, unit, customer_id, variety, grade, product_spec, inspection_date, selected_standard_id, involved_standard_ids, conflict_detail, selected_priority, create_user_no, create_date_time, update_date_time)
VALUES
('td_scf_03', 'TD-SCF-003', 'td_conf_jud_03', 'td_conf_ins_03', 'NUMERIC_LIMIT', 'BLOCKING', 'PENDING', 'ind002', '延伸率', '%', 'TD-CUST-BYEV-CF', '热镀锌深冲板', 'DX56D+Z-QA0623', '厚度0.80mm，宽度1250mm', '2026-06-28', NULL, '["td_conf_03_v1", "td_conf_03_v2"]', '{"reason": "同一客户、同一品种牌号、同一规格窗口内存在两份已发布客户协议，且有效期重叠；至少一个关键指标结构化限值不一致。", "standards": [{"standardId": "td_conf_03_v1", "standardCode": "AGREEMENT BYEV-SIM-2026-003-QA0623", "indicator": "ind002", "lowerLimit": 39.0, "upperLimit": null}, {"standardId": "td_conf_03_v2", "standardCode": "AGREEMENT BYEV-SIM-2026-003-REV-CF-QA0623", "indicator": "ind002", "lowerLimit": 40.0, "upperLimit": null}], "blocking": true}', 'CUSTOMER', 'system', NOW(), NOW());


-- ===== conflict_sample_004_AH36_customer.sql =====
-- ============================================================
-- Conflict sample 004: AH36_customer
-- Source PDF: /Users/weixuelei/workspace/aiAgent/ai-project/for-test/std-doc/CUST-ZYSHIP-SIM-2026-AH36_customer_agreement.pdf
-- Rerunnable: INSERT IGNORE only.
-- ============================================================

INSERT IGNORE INTO sys_dict_item
(id, dict_code, item_value, item_label, color_tag, sort_no, status, is_system, remark, create_date_time, update_date_time)
VALUES
('td_cf_var_04', 'PRODUCT_VARIETY', '船体结构钢板', '船体结构钢板', '', 741, 1, 0, '测试包：冲突标准样例', NOW(), NOW()),
('td_cf_grade_04', 'PRODUCT_GRADE', 'AH36-QA0623', 'AH36-QA0623', '', 742, 1, 0, '测试包：冲突标准结构化牌号使用QA0623后缀', NOW(), NOW()),
('td_cf_cust_04', 'QC_CUSTOMER', 'TD-CUST-ZYSHIP-CF', '中远船务冲突测试客户', '', 743, 1, 0, '测试包：冲突标准独立客户，避免影响普通检验记录', NOW(), NOW());

INSERT IGNORE INTO qc_indicator_item
(id, indicator_name, indicator_code, indicator_category, unit, test_method, description, status, is_deleted, create_date_time, update_date_time)
VALUES
('ind001', '抗拉强度', 'Rm', 'PERFORMANCE', 'MPa', 'GB/T 228.1', '最大拉伸力/原始横截面积', 'ACTIVE', 0, NOW(), NOW()),
('ind002', '延伸率', 'A', 'PERFORMANCE', '%', 'GB/T 228.1', '断后伸长量/原始标距长度', 'ACTIVE', 0, NOW(), NOW()),
('ind003', '屈服强度', 'ReL', 'PERFORMANCE', 'MPa', 'GB/T 228.1', '屈服点的应力值', 'ACTIVE', 0, NOW(), NOW()),
('td_ind_kv2', '冲击功KV2', 'TD_KV2', 'PERFORMANCE', 'J', '夏比V型缺口冲击', '低温冲击吸收能量', 'ACTIVE', 0, NOW(), NOW());

INSERT IGNORE INTO qc_quality_standard
(id, standard_type, standard_code, standard_name, variety, grade, spec_range, version_no, effective_date, expiry_date, status, customer_id, remark, create_user_no, create_date_time, update_date_time)
VALUES
('td_conf_04_v1', 'CUSTOMER', 'AGREEMENT ZYSHIP-SIM-2026-004-QA0623', '中远船务AH36船体结构钢板供货质量协议（模拟客户协议）基准版', '船体结构钢板', 'AH36-QA0623', '厚度8.00mm-40.00mm，宽度1500mm-3000mm', '协议V1.0-CF-QA0623', '2026-03-01', '2026-12-31', 'PUBLISHED', 'TD-CUST-ZYSHIP-CF', '冲突样例V1：客户协议基准版；来源PDF：CUST-ZYSHIP-SIM-2026-AH36_customer_agreement.pdf', 'system', NOW(), NOW()),
('td_conf_04_v2', 'CUSTOMER', 'AGREEMENT ZYSHIP-SIM-2026-004-REV-CF-QA0623', '中远船务AH36船体结构钢板供货质量协议（模拟客户协议）冲突版', '船体结构钢板', 'AH36-QA0623', '厚度8.00mm-40.00mm，宽度1500mm-3000mm', '协议V1.1-CF-QA0623', '2026-04-15', '2026-12-31', 'PUBLISHED', 'TD-CUST-ZYSHIP-CF', '冲突样例V2：同优先级同范围限值收严；来源PDF：CUST-ZYSHIP-SIM-2026-AH36_customer_agreement.pdf', 'system', NOW(), NOW());

INSERT IGNORE INTO qc_standard_indicator
(id, standard_id, indicator_id, upper_limit, lower_limit, is_required, concession_upper, concession_lower, create_date_time, update_date_time)
VALUES
('td_csi_04_1_1', 'td_conf_04_v1', 'ind003', NULL, 355, 1, NULL, 345, NOW(), NOW()),
('td_csi_04_1_2', 'td_conf_04_v1', 'ind001', 620, 490, 1, NULL, NULL, NOW(), NOW()),
('td_csi_04_1_3', 'td_conf_04_v1', 'ind002', NULL, 21, 1, NULL, 20, NOW(), NOW()),
('td_csi_04_1_4', 'td_conf_04_v1', 'td_ind_kv2', NULL, 34, 1, NULL, 30, NOW(), NOW()),
('td_csi_04_2_1', 'td_conf_04_v2', 'ind003', NULL, 365, 1, NULL, NULL, NOW(), NOW()),
('td_csi_04_2_2', 'td_conf_04_v2', 'ind001', 615, 500, 1, NULL, NULL, NOW(), NOW()),
('td_csi_04_2_3', 'td_conf_04_v2', 'ind002', NULL, 22, 1, NULL, NULL, NOW(), NOW()),
('td_csi_04_2_4', 'td_conf_04_v2', 'td_ind_kv2', NULL, 35, 1, NULL, NULL, NOW(), NOW());

INSERT IGNORE INTO qc_inspection_record
(id, heat_no, coil_no, batch_no, sample_type, test_time, tester_no, customer_id, product_variety, product_grade, product_spec, status, create_user_no, create_date_time, update_date_time)
VALUES
('td_conf_ins_04', 'HT-TD-CF-04-001', 'COIL-TD-CF-04-001', 'HT-TD-CF-04-001', 'MIDDLE', '2026-06-28 10:28:00', '021001', 'TD-CUST-ZYSHIP-CF', '船体结构钢板', 'AH36-QA0623', '厚度18.00mm，宽度2200mm', 'NORMAL', 'system', NOW(), NOW());

INSERT IGNORE INTO qc_inspection_value
(id, record_id, indicator_id, test_value, value_text, create_user_no, create_date_time, update_date_time)
VALUES
('td_civ_04_1', 'td_conf_ins_04', 'ind003', 360, NULL, 'system', NOW(), NOW()),
('td_civ_04_2', 'td_conf_ins_04', 'ind001', 555, NULL, 'system', NOW(), NOW()),
('td_civ_04_3', 'td_conf_ins_04', 'ind002', 23, NULL, 'system', NOW(), NOW()),
('td_civ_04_4', 'td_conf_ins_04', 'td_ind_kv2', 36, NULL, 'system', NOW(), NOW());

INSERT IGNORE INTO qc_judgment_result
(id, record_id, judgment_type, judgment_time, is_final, matched_standard_ids, remark, create_user_no, create_date_time, update_date_time)
VALUES
('td_conf_jud_04', 'td_conf_ins_04', 'STANDARD_CONFLICT', '2026-06-28 10:28:05', 1, '["td_conf_04_v1", "td_conf_04_v2"]', '测试包冲突样例：同优先级客户协议结构化限值冲突，需人工裁决', 'system', NOW(), NOW());

INSERT IGNORE INTO standard_conflict
(id, conflict_no, judgment_id, record_id, conflict_type, conflict_level, status, indicator_id, indicator_name, unit, customer_id, variety, grade, product_spec, inspection_date, selected_standard_id, involved_standard_ids, conflict_detail, selected_priority, create_user_no, create_date_time, update_date_time)
VALUES
('td_scf_04', 'TD-SCF-004', 'td_conf_jud_04', 'td_conf_ins_04', 'NUMERIC_LIMIT', 'BLOCKING', 'PENDING', 'ind003', '屈服强度', 'MPa', 'TD-CUST-ZYSHIP-CF', '船体结构钢板', 'AH36-QA0623', '厚度18.00mm，宽度2200mm', '2026-06-28', NULL, '["td_conf_04_v1", "td_conf_04_v2"]', '{"reason": "同一客户、同一品种牌号、同一规格窗口内存在两份已发布客户协议，且有效期重叠；至少一个关键指标结构化限值不一致。", "standards": [{"standardId": "td_conf_04_v1", "standardCode": "AGREEMENT ZYSHIP-SIM-2026-004-QA0623", "indicator": "ind003", "lowerLimit": 355.0, "upperLimit": null}, {"standardId": "td_conf_04_v2", "standardCode": "AGREEMENT ZYSHIP-SIM-2026-004-REV-CF-QA0623", "indicator": "ind003", "lowerLimit": 365.0, "upperLimit": null}], "blocking": true}', 'CUSTOMER', 'system', NOW(), NOW());


-- ===== conflict_sample_005_L360M_customer.sql =====
-- ============================================================
-- Conflict sample 005: L360M_customer
-- Source PDF: /Users/weixuelei/workspace/aiAgent/ai-project/for-test/std-doc/CUST-HXPIPE-SIM-2026-L360M_customer_agreement.pdf
-- Rerunnable: INSERT IGNORE only.
-- ============================================================

INSERT IGNORE INTO sys_dict_item
(id, dict_code, item_value, item_label, color_tag, sort_no, status, is_system, remark, create_date_time, update_date_time)
VALUES
('td_cf_var_05', 'PRODUCT_VARIETY', '管线钢热轧卷', '管线钢热轧卷', '', 751, 1, 0, '测试包：冲突标准样例', NOW(), NOW()),
('td_cf_grade_05', 'PRODUCT_GRADE', 'L360M-QA0623', 'L360M-QA0623', '', 752, 1, 0, '测试包：冲突标准结构化牌号使用QA0623后缀', NOW(), NOW()),
('td_cf_cust_05', 'QC_CUSTOMER', 'TD-CUST-HXPIPE-CF', '华信管业冲突测试客户', '', 753, 1, 0, '测试包：冲突标准独立客户，避免影响普通检验记录', NOW(), NOW());

INSERT IGNORE INTO qc_indicator_item
(id, indicator_name, indicator_code, indicator_category, unit, test_method, description, status, is_deleted, create_date_time, update_date_time)
VALUES
('ind001', '抗拉强度', 'Rm', 'PERFORMANCE', 'MPa', 'GB/T 228.1', '最大拉伸力/原始横截面积', 'ACTIVE', 0, NOW(), NOW()),
('ind002', '延伸率', 'A', 'PERFORMANCE', '%', 'GB/T 228.1', '断后伸长量/原始标距长度', 'ACTIVE', 0, NOW(), NOW()),
('ind003', '屈服强度', 'ReL', 'PERFORMANCE', 'MPa', 'GB/T 228.1', '屈服点的应力值', 'ACTIVE', 0, NOW(), NOW()),
('td_ind_ceq', '碳当量', 'TD_CEQ', 'COMPOSITION', '%', '熔炼分析', '焊接性控制指标', 'ACTIVE', 0, NOW(), NOW());

INSERT IGNORE INTO qc_quality_standard
(id, standard_type, standard_code, standard_name, variety, grade, spec_range, version_no, effective_date, expiry_date, status, customer_id, remark, create_user_no, create_date_time, update_date_time)
VALUES
('td_conf_05_v1', 'CUSTOMER', 'AGREEMENT HXPIPE-SIM-2026-005-QA0623', '华信管业L360M管线钢热轧卷供货质量协议（模拟客户协议）基准版', '管线钢热轧卷', 'L360M-QA0623', '厚度5.00mm-18.00mm，宽度1000mm-1800mm', '协议V1.0-CF-QA0623', '2026-03-01', '2026-12-31', 'PUBLISHED', 'TD-CUST-HXPIPE-CF', '冲突样例V1：客户协议基准版；来源PDF：CUST-HXPIPE-SIM-2026-L360M_customer_agreement.pdf', 'system', NOW(), NOW()),
('td_conf_05_v2', 'CUSTOMER', 'AGREEMENT HXPIPE-SIM-2026-005-REV-CF-QA0623', '华信管业L360M管线钢热轧卷供货质量协议（模拟客户协议）冲突版', '管线钢热轧卷', 'L360M-QA0623', '厚度5.00mm-18.00mm，宽度1000mm-1800mm', '协议V1.1-CF-QA0623', '2026-04-15', '2026-12-31', 'PUBLISHED', 'TD-CUST-HXPIPE-CF', '冲突样例V2：同优先级同范围限值收严；来源PDF：CUST-HXPIPE-SIM-2026-L360M_customer_agreement.pdf', 'system', NOW(), NOW());

INSERT IGNORE INTO qc_standard_indicator
(id, standard_id, indicator_id, upper_limit, lower_limit, is_required, concession_upper, concession_lower, create_date_time, update_date_time)
VALUES
('td_csi_05_1_1', 'td_conf_05_v1', 'ind003', 510, 360, 1, 520, 350, NOW(), NOW()),
('td_csi_05_1_2', 'td_conf_05_v1', 'ind001', 650, 460, 1, NULL, NULL, NOW(), NOW()),
('td_csi_05_1_3', 'td_conf_05_v1', 'ind002', NULL, 20, 1, NULL, 19, NOW(), NOW()),
('td_csi_05_1_4', 'td_conf_05_v1', 'td_ind_ceq', 0.42, NULL, 1, 0.44, NULL, NOW(), NOW()),
('td_csi_05_2_1', 'td_conf_05_v2', 'ind003', 505, 370, 1, NULL, NULL, NOW(), NOW()),
('td_csi_05_2_2', 'td_conf_05_v2', 'ind001', 645, 470, 1, NULL, NULL, NOW(), NOW()),
('td_csi_05_2_3', 'td_conf_05_v2', 'ind002', NULL, 21, 1, NULL, NULL, NOW(), NOW()),
('td_csi_05_2_4', 'td_conf_05_v2', 'td_ind_ceq', 0.415, NULL, 1, NULL, NULL, NOW(), NOW());

INSERT IGNORE INTO qc_inspection_record
(id, heat_no, coil_no, batch_no, sample_type, test_time, tester_no, customer_id, product_variety, product_grade, product_spec, status, create_user_no, create_date_time, update_date_time)
VALUES
('td_conf_ins_05', 'HT-TD-CF-05-001', 'COIL-TD-CF-05-001', 'HT-TD-CF-05-001', 'MIDDLE', '2026-06-28 10:35:00', '021001', 'TD-CUST-HXPIPE-CF', '管线钢热轧卷', 'L360M-QA0623', '厚度10.00mm，宽度1500mm', 'NORMAL', 'system', NOW(), NOW());

INSERT IGNORE INTO qc_inspection_value
(id, record_id, indicator_id, test_value, value_text, create_user_no, create_date_time, update_date_time)
VALUES
('td_civ_05_1', 'td_conf_ins_05', 'ind003', 365, NULL, 'system', NOW(), NOW()),
('td_civ_05_2', 'td_conf_ins_05', 'ind001', 555, NULL, 'system', NOW(), NOW()),
('td_civ_05_3', 'td_conf_ins_05', 'ind002', 22, NULL, 'system', NOW(), NOW()),
('td_civ_05_4', 'td_conf_ins_05', 'td_ind_ceq', 0.40, NULL, 'system', NOW(), NOW());

INSERT IGNORE INTO qc_judgment_result
(id, record_id, judgment_type, judgment_time, is_final, matched_standard_ids, remark, create_user_no, create_date_time, update_date_time)
VALUES
('td_conf_jud_05', 'td_conf_ins_05', 'STANDARD_CONFLICT', '2026-06-28 10:35:05', 1, '["td_conf_05_v1", "td_conf_05_v2"]', '测试包冲突样例：同优先级客户协议结构化限值冲突，需人工裁决', 'system', NOW(), NOW());

INSERT IGNORE INTO standard_conflict
(id, conflict_no, judgment_id, record_id, conflict_type, conflict_level, status, indicator_id, indicator_name, unit, customer_id, variety, grade, product_spec, inspection_date, selected_standard_id, involved_standard_ids, conflict_detail, selected_priority, create_user_no, create_date_time, update_date_time)
VALUES
('td_scf_05', 'TD-SCF-005', 'td_conf_jud_05', 'td_conf_ins_05', 'NUMERIC_LIMIT', 'BLOCKING', 'PENDING', 'ind003', '屈服强度', 'MPa', 'TD-CUST-HXPIPE-CF', '管线钢热轧卷', 'L360M-QA0623', '厚度10.00mm，宽度1500mm', '2026-06-28', NULL, '["td_conf_05_v1", "td_conf_05_v2"]', '{"reason": "同一客户、同一品种牌号、同一规格窗口内存在两份已发布客户协议，且有效期重叠；至少一个关键指标结构化限值不一致。", "standards": [{"standardId": "td_conf_05_v1", "standardCode": "AGREEMENT HXPIPE-SIM-2026-005-QA0623", "indicator": "ind003", "lowerLimit": 360.0, "upperLimit": 510.0}, {"standardId": "td_conf_05_v2", "standardCode": "AGREEMENT HXPIPE-SIM-2026-005-REV-CF-QA0623", "indicator": "ind003", "lowerLimit": 370.0, "upperLimit": 505.0}], "blocking": true}', 'CUSTOMER', 'system', NOW(), NOW());
