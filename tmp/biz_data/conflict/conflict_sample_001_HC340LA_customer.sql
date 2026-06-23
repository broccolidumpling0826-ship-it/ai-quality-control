USE ai_quality_control;

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
