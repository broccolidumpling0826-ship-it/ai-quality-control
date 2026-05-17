USE ai_quality_control;

INSERT IGNORE INTO sys_dict (id, dict_code, dict_name, is_system, sort_no, status, create_date_time, update_date_time) VALUES
('d017', 'QC_CUSTOMER', '关联客户', 0, 17, 1, NOW(), NOW());

INSERT IGNORE INTO sys_dict_item (id, dict_code, item_value, item_label, color_tag, sort_no, status, is_system, create_date_time, update_date_time) VALUES
('di161', 'QC_CUSTOMER', 'CUST-001', '华东汽车配件有限公司', '', 1, 1, 0, NOW(), NOW()),
('di162', 'QC_CUSTOMER', 'CUST-002', '西南建材集团',         '', 2, 1, 0, NOW(), NOW());
