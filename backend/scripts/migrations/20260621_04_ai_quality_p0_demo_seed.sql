-- ============================================================
-- Deterministic P0 demo seed for AI quality enhancement.
-- Depends on init-test-data.sql and 20260621_ai_quality_p0_schema.sql.
-- MySQL 5.7 compatible and rerunnable with INSERT IGNORE.
-- ============================================================

USE ai_quality_control;

-- Complete the existing concession demo scenario with evidence and customer usage.
UPDATE `qc_inspection_record`
SET `customer_id` = 'CUST-002',
    `update_date_time` = NOW()
WHERE `id` = 'rec003'
  AND (`customer_id` IS NULL OR `customer_id` = '');

INSERT IGNORE INTO `qc_judgment_evidence` (
  `id`, `judgment_id`, `standard_id`, `indicator_id`, `test_value`,
  `upper_limit`, `lower_limit`, `deviation`, `trigger_rule`, `is_passed`,
  `create_date_time`, `update_date_time`
) VALUES
  ('je021', 'jud003', 'std001', 'ind001', 365.000000, 510.000000, 370.000000, -5.000000,
   '实测值 365.0 MPa 低于国标下限 370.0 MPa，但在让步下限 360.0 MPa 范围内，触发 CAN_CONCESSION', 0, NOW(), NOW()),
  ('je022', 'jud003', 'std001', 'ind002', 25.000000, NULL, 26.000000, -1.000000,
   '实测值 25.0% 低于国标下限 26.0%，但在让步下限 24.0% 范围内，触发 CAN_CONCESSION', 0, NOW(), NOW());

INSERT IGNORE INTO `qc_customer_usage_profile` (
  `id`, `customer_id`, `customer_name`, `default_usage`, `risk_category`,
  `variety`, `grade`, `status`, `remark`, `create_user_no`, `create_date_time`
) VALUES
  ('usage_p0_001', 'CUST-002', '西南建材集团', '建筑围护和普通结构件', 'NORMAL',
   '冷轧板', 'Q235B', 'ACTIVE', 'P0 可让步演示默认用途', 'system', NOW());

INSERT IGNORE INTO `alternative_stock` (
  `id`, `variety`, `grade`, `spec_range`, `coil_no`, `batch_no`,
  `available_weight`, `location`, `status`, `earliest_ship_date`,
  `remark`, `create_user_no`, `create_date_time`
) VALUES
  ('alt_p0_001', '冷轧板', 'Q235B', '厚度1.0-2.0mm，宽度600-1500mm',
   'ZALT001', 'HALT20250515', 18.500000, 'A-01-03', 'AVAILABLE', '2025-05-17',
   'P0 可让步演示同规格合格替代资源', 'system', NOW());

-- Add a same-priority customer agreement conflict for the standard-conflict demo.
INSERT IGNORE INTO `qc_quality_standard` (
  `id`, `standard_type`, `standard_code`, `standard_name`, `variety`, `grade`,
  `spec_range`, `version_no`, `effective_date`, `expiry_date`, `status`,
  `customer_id`, `remark`, `create_date_time`, `update_date_time`
) VALUES
  ('std003', 'CUSTOMER', '协议C2025-088-v2-冲突样例', '华东汽车配件客户协议冲突样例',
   '冷轧板', 'Q235B', '厚度1.0-2.0mm', '协议C2025-088-v2', '2025-04-01', '2025-12-31',
   'PUBLISHED', 'CUST-001', 'P0 标准冲突演示：与 std002 同优先级同范围但限值不一致', NOW(), NOW());

INSERT IGNORE INTO `qc_standard_indicator` (
  `id`, `standard_id`, `indicator_id`, `upper_limit`, `lower_limit`, `is_required`,
  `concession_upper`, `concession_lower`, `create_date_time`, `update_date_time`
) VALUES
  ('si021', 'std003', 'ind001', 500.000000, 395.000000, 1, 510.000000, 385.000000, NOW(), NOW()),
  ('si022', 'std003', 'ind002', NULL, 29.000000, 1, NULL, 27.000000, NOW(), NOW()),
  ('si023', 'std003', 'ind004', 0.080000, -0.080000, 1, 0.100000, -0.100000, NOW(), NOW());

INSERT IGNORE INTO `qc_inspection_record` (
  `id`, `heat_no`, `coil_no`, `batch_no`, `sample_type`, `test_time`,
  `tester_no`, `customer_id`, `product_variety`, `product_grade`,
  `product_spec`, `status`, `create_date_time`, `update_date_time`
) VALUES
  ('rec004', 'H20250516001', 'Z004001', 'H20250516001', 'MIDDLE', '2025-05-16 09:30:00',
   '021001', 'CUST-001', '冷轧板', 'Q235B', '厚度1.5mm×宽度1000mm', 'NORMAL', NOW(), NOW());

INSERT IGNORE INTO `qc_inspection_value` (
  `id`, `record_id`, `indicator_id`, `test_value`, `create_date_time`, `update_date_time`
) VALUES
  ('iv031', 'rec004', 'ind001', 390.000000, NOW(), NOW()),
  ('iv032', 'rec004', 'ind002', 29.500000, NOW(), NOW()),
  ('iv033', 'rec004', 'ind004', 0.060000, NOW(), NOW());

INSERT IGNORE INTO `qc_judgment_result` (
  `id`, `record_id`, `judgment_type`, `judgment_time`, `is_final`,
  `matched_standard_ids`, `remark`, `create_date_time`, `update_date_time`
) VALUES
  ('jud004', 'rec004', 'STANDARD_CONFLICT', '2025-05-16 09:30:05', 1,
   '["std002","std003","std001"]', 'P0 演示：客户协议同优先级重叠冲突，需人工裁决', NOW(), NOW());

INSERT IGNORE INTO `qc_judgment_evidence` (
  `id`, `judgment_id`, `standard_id`, `indicator_id`, `test_value`,
  `upper_limit`, `lower_limit`, `deviation`, `trigger_rule`, `is_passed`,
  `create_date_time`, `update_date_time`
) VALUES
  ('je031', 'jud004', 'std002', 'ind001', 390.000000, 500.000000, 380.000000, 10.000000,
   '同一客户协议优先级下，std002 要求 Rm ≥ 380 MPa', 0, NOW(), NOW()),
  ('je032', 'jud004', 'std003', 'ind001', 390.000000, 500.000000, 395.000000, -5.000000,
   '同一客户协议优先级下，std003 要求 Rm ≥ 395 MPa，与 std002 冲突，触发 STANDARD_CONFLICT', 0, NOW(), NOW());

INSERT IGNORE INTO `standard_conflict` (
  `id`, `conflict_no`, `judgment_id`, `record_id`, `conflict_type`,
  `conflict_level`, `status`, `indicator_id`, `indicator_name`, `unit`,
  `customer_id`, `variety`, `grade`, `product_spec`, `inspection_date`,
  `selected_standard_id`, `involved_standard_ids`, `conflict_detail`,
  `selected_priority`, `create_user_no`, `create_date_time`
) VALUES
  ('scf_p0_001', 'SCF-P0-001', 'jud004', 'rec004', 'NUMERIC_LIMIT',
   'BLOCKING', 'PENDING', 'ind001', '抗拉强度', 'MPa',
   'CUST-001', '冷轧板', 'Q235B', '厚度1.5mm×宽度1000mm', '2025-05-16',
   NULL, '["std002","std003"]',
   '{"reason":"同一客户、同一品种牌号、同一规格窗口内存在两个生效客户协议，Rm 下限分别为 380 MPa 和 395 MPa。","standards":[{"standardId":"std002","lowerLimit":380,"upperLimit":500,"effectiveDate":"2025-01-01","expiryDate":"2025-12-31"},{"standardId":"std003","lowerLimit":395,"upperLimit":500,"effectiveDate":"2025-04-01","expiryDate":"2025-12-31"}],"blocking":true}',
   'CUSTOMER', 'system', NOW());
