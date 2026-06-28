USE ai_quality_control;

-- 可选脚本：补齐本测试包20份PDF涉及的品种、牌号和少量专用指标。
-- 核心四个Q235B-QA0622人工测试场景不依赖本脚本。

INSERT IGNORE INTO sys_dict_item
(id, dict_code, item_value, item_label, color_tag, sort_no, status, is_system, create_date_time, update_date_time)
VALUES
('td_var_001', 'PRODUCT_VARIETY', '热轧带肋钢筋', '热轧带肋钢筋', '', 101, 1, 0, NOW(), NOW()),
('td_var_002', 'PRODUCT_VARIETY', '冷轧不锈钢板', '冷轧不锈钢板', '', 102, 1, 0, NOW(), NOW()),
('td_var_003', 'PRODUCT_VARIETY', '压力容器钢板', '压力容器钢板', '', 103, 1, 0, NOW(), NOW()),
('td_var_004', 'PRODUCT_VARIETY', '管线钢板卷', '管线钢板卷', '', 104, 1, 0, NOW(), NOW()),
('td_var_005', 'PRODUCT_VARIETY', '深冲冷轧板', '深冲冷轧板', '', 105, 1, 0, NOW(), NOW()),
('td_var_006', 'PRODUCT_VARIETY', '冷轧无取向电工钢', '冷轧无取向电工钢', '', 106, 1, 0, NOW(), NOW()),
('td_var_007', 'PRODUCT_VARIETY', '热轧弹簧扁钢', '热轧弹簧扁钢', '', 107, 1, 0, NOW(), NOW()),
('td_var_008', 'PRODUCT_VARIETY', '酸洗热轧钢带', '酸洗热轧钢带', '', 108, 1, 0, NOW(), NOW()),
('td_var_009', 'PRODUCT_VARIETY', '冷轧低合金高强钢板', '冷轧低合金高强钢板', '', 109, 1, 0, NOW(), NOW()),
('td_var_010', 'PRODUCT_VARIETY', '调质耐磨钢板', '调质耐磨钢板', '', 110, 1, 0, NOW(), NOW()),
('td_var_011', 'PRODUCT_VARIETY', '风电塔筒用中厚板', '风电塔筒用中厚板', '', 111, 1, 0, NOW(), NOW()),
('td_var_012', 'PRODUCT_VARIETY', '热镀锌深冲板', '热镀锌深冲板', '', 112, 1, 0, NOW(), NOW()),
('td_var_013', 'PRODUCT_VARIETY', '船体结构钢板', '船体结构钢板', '', 113, 1, 0, NOW(), NOW()),
('td_var_014', 'PRODUCT_VARIETY', '管线钢热轧卷', '管线钢热轧卷', '', 114, 1, 0, NOW(), NOW());

INSERT IGNORE INTO sys_dict_item
(id, dict_code, item_value, item_label, color_tag, sort_no, status, is_system, create_date_time, update_date_time)
VALUES
('td_grade_001', 'PRODUCT_GRADE', 'Q355B', 'Q355B', '', 101, 1, 0, NOW(), NOW()),
('td_grade_002', 'PRODUCT_GRADE', 'Q345B', 'Q345B', '', 102, 1, 0, NOW(), NOW()),
('td_grade_003', 'PRODUCT_GRADE', 'HRB400E', 'HRB400E', '', 103, 1, 0, NOW(), NOW()),
('td_grade_004', 'PRODUCT_GRADE', 'DX51D+Z', 'DX51D+Z', '', 104, 1, 0, NOW(), NOW()),
('td_grade_005', 'PRODUCT_GRADE', 'DX56D+Z', 'DX56D+Z', '', 105, 1, 0, NOW(), NOW()),
('td_grade_006', 'PRODUCT_GRADE', '06Cr19Ni10', '06Cr19Ni10', '', 106, 1, 0, NOW(), NOW()),
('td_grade_007', 'PRODUCT_GRADE', 'Q345R', 'Q345R', '', 107, 1, 0, NOW(), NOW()),
('td_grade_008', 'PRODUCT_GRADE', 'L245M', 'L245M', '', 108, 1, 0, NOW(), NOW()),
('td_grade_009', 'PRODUCT_GRADE', 'L360M', 'L360M', '', 109, 1, 0, NOW(), NOW()),
('td_grade_010', 'PRODUCT_GRADE', 'DC04', 'DC04', '', 110, 1, 0, NOW(), NOW()),
('td_grade_011', 'PRODUCT_GRADE', '50W800', '50W800', '', 111, 1, 0, NOW(), NOW()),
('td_grade_012', 'PRODUCT_GRADE', '60Si2Mn', '60Si2Mn', '', 112, 1, 0, NOW(), NOW()),
('td_grade_013', 'PRODUCT_GRADE', 'SPFH590', 'SPFH590', '', 113, 1, 0, NOW(), NOW()),
('td_grade_014', 'PRODUCT_GRADE', 'HC340LA', 'HC340LA', '', 114, 1, 0, NOW(), NOW()),
('td_grade_015', 'PRODUCT_GRADE', 'NM400', 'NM400', '', 115, 1, 0, NOW(), NOW()),
('td_grade_016', 'PRODUCT_GRADE', 'S355J2', 'S355J2', '', 116, 1, 0, NOW(), NOW()),
('td_grade_017', 'PRODUCT_GRADE', 'AH36', 'AH36', '', 117, 1, 0, NOW(), NOW());

INSERT IGNORE INTO sys_dict_item
(id, dict_code, item_value, item_label, color_tag, sort_no, status, is_system, create_date_time, update_date_time)
VALUES
('td_grade_core_qa0622', 'PRODUCT_GRADE', 'Q235B-QA0622', 'Q235B-QA0622', '', 10, 1, 0, NOW(), NOW());

INSERT IGNORE INTO qc_indicator_item
(id, indicator_name, indicator_code, indicator_category, unit, description, is_deleted, create_date_time, update_date_time)
VALUES
('td_ind_001', '镀层重量', 'Z', 'PERFORMANCE', 'g/m2', '热镀锌双面镀层重量', 0, NOW(), NOW()),
('td_ind_002', '冲击功', 'KV2', 'PERFORMANCE', 'J', '夏比冲击吸收能量', 0, NOW(), NOW()),
('td_ind_003', '铁损', 'P15/50', 'PERFORMANCE', 'W/kg', '电工钢单位铁损', 0, NOW(), NOW()),
('td_ind_004', '磁感', 'B50', 'PERFORMANCE', 'T', '电工钢磁感应强度', 0, NOW(), NOW()),
('td_ind_005', '硬度HBW', 'HBW', 'PERFORMANCE', 'HBW', '布氏硬度', 0, NOW(), NOW()),
('td_ind_006', '扩孔率', 'HER', 'PERFORMANCE', '%', '高强钢边部成形扩孔率', 0, NOW(), NOW()),
('td_ind_007', '脱碳层', 'DECARB', 'SURFACE', 'mm', '弹簧钢表面脱碳层深度', 0, NOW(), NOW()),
('td_ind_008', '强屈比', 'Rm/ReL', 'PERFORMANCE', '-', '抗拉强度与屈服强度比值', 0, NOW(), NOW());
