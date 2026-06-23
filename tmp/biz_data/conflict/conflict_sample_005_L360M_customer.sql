USE ai_quality_control;

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
