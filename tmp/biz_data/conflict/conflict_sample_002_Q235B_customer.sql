USE ai_quality_control;

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
