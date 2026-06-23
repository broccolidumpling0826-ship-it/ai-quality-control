USE ai_quality_control;

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
