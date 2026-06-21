-- ============================================================
-- 002 AI 演示数据 seed — MySQL 5.7.43
-- 依赖：init-schema.sql、init-test-data.sql、V2__ai_schema.sql
-- 执行：mysql -h 127.0.0.1 -P 3307 -u user -p ai_quality_control < backend/scripts/V2__ai_demo_data.sql
-- ============================================================
USE ai_quality_control;

SET @ts = '2026-06-20 10:00:00';

-- ── 四场景 Demo 批次（T024）──────────────────────────────────

-- 1. DEMO-QUALIFIED-001 — 合格
INSERT IGNORE INTO qc_inspection_record
(id, heat_no, coil_no, batch_no, sample_type, test_time, tester_no, customer_id, product_variety, product_grade, product_spec, status, create_date_time, update_date_time)
VALUES
('demo-rec-qualified', 'H-DEMO-QUAL-001', 'Z-DEMO-QUAL-001', 'DEMO-QUALIFIED-001', 'MIDDLE', '2026-06-20 09:00:00', '021001', NULL, '冷轧板', 'Q235B', '厚度1.5mm×宽度1000mm', 'NORMAL', @ts, @ts);

INSERT IGNORE INTO qc_inspection_value (id, record_id, indicator_id, test_value, create_date_time, update_date_time) VALUES
('demo-iv-q1', 'demo-rec-qualified', 'ind001', 430.000000, @ts, @ts),
('demo-iv-q2', 'demo-rec-qualified', 'ind002', 30.500000, @ts, @ts),
('demo-iv-q3', 'demo-rec-qualified', 'ind003', 280.000000, @ts, @ts),
('demo-iv-q4', 'demo-rec-qualified', 'ind004', 0.050000, @ts, @ts);

INSERT IGNORE INTO qc_judgment_result
(id, record_id, judgment_type, judgment_time, is_final, matched_standard_ids, create_date_time, update_date_time)
VALUES
('demo-jud-qualified', 'demo-rec-qualified', 'QUALIFIED', '2026-06-20 09:00:05', 1, '["std001"]', @ts, @ts);

INSERT IGNORE INTO qc_judgment_evidence
(id, judgment_id, standard_id, indicator_id, test_value, upper_limit, lower_limit, deviation, trigger_rule, is_passed, create_date_time, update_date_time) VALUES
('demo-je-q1', 'demo-jud-qualified', 'std001', 'ind001', 430.0, 510.0, 370.0, 60.0, '实测值 430.0 MPa 在国标范围 [370.0, 510.0] 内', 1, @ts, @ts),
('demo-je-q2', 'demo-jud-qualified', 'std001', 'ind002', 30.5, NULL, 26.0, 4.5, '实测值 30.5% ≥ 国标下限 26.0%', 1, @ts, @ts);

-- 2. DEMO-UNQUALIFIED-001 — 不合格
INSERT IGNORE INTO qc_inspection_record
(id, heat_no, coil_no, batch_no, sample_type, test_time, tester_no, customer_id, product_variety, product_grade, product_spec, status, create_date_time, update_date_time)
VALUES
('demo-rec-unqualified', 'H-DEMO-UNQ-001', 'Z-DEMO-UNQ-001', 'DEMO-UNQUALIFIED-001', 'HEAD', '2026-06-20 09:30:00', '021001', 'CUST-001', '冷轧板', 'Q235B', '厚度1.5mm×宽度1000mm', 'NORMAL', @ts, @ts);

INSERT IGNORE INTO qc_inspection_value (id, record_id, indicator_id, test_value, create_date_time, update_date_time) VALUES
('demo-iv-u1', 'demo-rec-unqualified', 'ind001', 360.000000, @ts, @ts),
('demo-iv-u2', 'demo-rec-unqualified', 'ind002', 27.000000, @ts, @ts);

INSERT IGNORE INTO qc_judgment_result
(id, record_id, judgment_type, judgment_time, is_final, matched_standard_ids, create_date_time, update_date_time)
VALUES
('demo-jud-unqualified', 'demo-rec-unqualified', 'UNQUALIFIED', '2026-06-20 09:30:05', 1, '["std002","std001"]', @ts, @ts);

INSERT IGNORE INTO qc_judgment_evidence
(id, judgment_id, standard_id, indicator_id, test_value, upper_limit, lower_limit, deviation, trigger_rule, is_passed, create_date_time, update_date_time) VALUES
('demo-je-u1', 'demo-jud-unqualified', 'std002', 'ind001', 360.0, 500.0, 380.0, -20.0, '实测值 360.0 MPa 低于客户协议下限 380.0 MPa', 0, @ts, @ts),
('demo-je-u2', 'demo-jud-unqualified', 'std002', 'ind002', 27.0, NULL, 28.0, -1.0, '实测值 27.0% 低于客户协议下限 28.0%', 0, @ts, @ts);

-- 3. DEMO-CONCESSION-001 — 可让步
INSERT IGNORE INTO qc_inspection_record
(id, heat_no, coil_no, batch_no, sample_type, test_time, tester_no, customer_id, product_variety, product_grade, product_spec, status, create_date_time, update_date_time)
VALUES
('demo-rec-concession', 'H-DEMO-CON-001', 'Z-DEMO-CON-001', 'DEMO-CONCESSION-001', 'MIDDLE', '2026-06-20 10:00:00', '021001', NULL, '冷轧板', 'Q235B', '厚度1.5mm×宽度1000mm', 'NORMAL', @ts, @ts);

INSERT IGNORE INTO qc_inspection_value (id, record_id, indicator_id, test_value, create_date_time, update_date_time) VALUES
('demo-iv-c1', 'demo-rec-concession', 'ind001', 365.000000, @ts, @ts),
('demo-iv-c2', 'demo-rec-concession', 'ind002', 25.000000, @ts, @ts);

INSERT IGNORE INTO qc_judgment_result
(id, record_id, judgment_type, judgment_time, is_final, matched_standard_ids, create_date_time, update_date_time)
VALUES
('demo-jud-concession', 'demo-rec-concession', 'CAN_CONCESSION', '2026-06-20 10:00:05', 1, '["std001"]', @ts, @ts);

INSERT IGNORE INTO qc_judgment_evidence
(id, judgment_id, standard_id, indicator_id, test_value, upper_limit, lower_limit, deviation, trigger_rule, is_passed, create_date_time, update_date_time) VALUES
('demo-je-c1', 'demo-jud-concession', 'std001', 'ind001', 365.0, 510.0, 370.0, -5.0, '实测值 365.0 MPa 略低于国标下限 370.0 MPa，在让步范围内', 0, @ts, @ts);

-- 4. DEMO-CONFLICT-001 — 标准冲突（双标准限值不一致）
INSERT IGNORE INTO qc_inspection_record
(id, heat_no, coil_no, batch_no, sample_type, test_time, tester_no, customer_id, product_variety, product_grade, product_spec, status, create_date_time, update_date_time)
VALUES
('demo-rec-conflict', 'H-DEMO-CONF-001', 'Z-DEMO-CONF-001', 'DEMO-CONFLICT-001', 'TAIL', '2026-06-20 10:30:00', '021001', 'CUST-001', '冷轧板', 'Q235B', '厚度1.5mm×宽度1000mm', 'NORMAL', @ts, @ts);

INSERT IGNORE INTO qc_inspection_value (id, record_id, indicator_id, test_value, create_date_time, update_date_time) VALUES
('demo-iv-f1', 'demo-rec-conflict', 'ind001', 375.000000, @ts, @ts),
('demo-iv-f2', 'demo-rec-conflict', 'ind002', 27.500000, @ts, @ts);

INSERT IGNORE INTO qc_judgment_result
(id, record_id, judgment_type, judgment_time, is_final, matched_standard_ids, remark, create_date_time, update_date_time)
VALUES
('demo-jud-conflict', 'demo-rec-conflict', 'UNQUALIFIED', '2026-06-20 10:30:05', 1, '["std002","std001"]', '国标与客户协议抗拉强度下限冲突，需人工裁定', @ts, @ts);

INSERT IGNORE INTO qc_judgment_evidence
(id, judgment_id, standard_id, indicator_id, test_value, upper_limit, lower_limit, deviation, trigger_rule, is_passed, create_date_time, update_date_time) VALUES
('demo-je-f1', 'demo-jud-conflict', 'std001', 'ind001', 375.0, 510.0, 370.0, 5.0, '国标判定：375.0 MPa 在 [370.0, 510.0] 内', 1, @ts, @ts),
('demo-je-f2', 'demo-jud-conflict', 'std002', 'ind001', 375.0, 500.0, 380.0, -5.0, '客协判定：375.0 MPa 低于下限 380.0 MPa', 0, @ts, @ts);

-- 质保书演示批次 BATCH-DEMO-001（US6 预置）
INSERT IGNORE INTO qc_quality_cert_data
(id, batch_no, coil_no, snapshot_data, generate_time, generated_by, create_date_time, update_date_time)
VALUES
('cert-demo-001', 'BATCH-DEMO-001', 'Z-DEMO-QUAL-001',
 '{"batchNo":"BATCH-DEMO-001","coilCount":1,"indicators":[{"code":"Rm","name":"抗拉强度","value":430,"unit":"MPa"},{"code":"A","name":"延伸率","value":30.5,"unit":"%"}]}',
 '2026-06-20 11:00:00', '021001', @ts, @ts);

-- ── 批量检验记录（96 条，合计 100 条含四场景）────────────────
DROP PROCEDURE IF EXISTS seed_bulk_inspection_records;
DELIMITER $$
CREATE PROCEDURE seed_bulk_inspection_records()
BEGIN
  DECLARE i INT DEFAULT 1;
  DECLARE rec_id VARCHAR(64);
  DECLARE batch_no VARCHAR(64);
  DECLARE coil_no VARCHAR(64);
  DECLARE jtype VARCHAR(32);
  WHILE i <= 96 DO
    SET rec_id = CONCAT('bulk-rec-', LPAD(i, 3, '0'));
    SET batch_no = CONCAT('H20250620', LPAD(i, 3, '0'));
    SET coil_no = CONCAT('Z-BULK-', LPAD(i, 3, '0'));
    SET jtype = CASE MOD(i, 4)
      WHEN 0 THEN 'QUALIFIED'
      WHEN 1 THEN 'UNQUALIFIED'
      WHEN 2 THEN 'CAN_CONCESSION'
      ELSE 'QUALIFIED'
    END;

    INSERT IGNORE INTO qc_inspection_record
    (id, heat_no, coil_no, batch_no, sample_type, test_time, tester_no, customer_id, product_variety, product_grade, product_spec, status, create_date_time, update_date_time)
    VALUES
    (rec_id, batch_no, coil_no, batch_no, 'MIDDLE', DATE_ADD('2026-06-01 08:00:00', INTERVAL i HOUR), '021001', NULL, '冷轧板', 'Q235B', '厚度1.5mm×宽度1000mm', 'NORMAL', @ts, @ts);

    INSERT IGNORE INTO qc_inspection_value (id, record_id, indicator_id, test_value, create_date_time, update_date_time) VALUES
    (CONCAT('bulk-iv-', i, '-1'), rec_id, 'ind001', 400.000000 + MOD(i, 30), @ts, @ts),
    (CONCAT('bulk-iv-', i, '-2'), rec_id, 'ind002', 25.000000 + MOD(i, 10) * 0.5, @ts, @ts);

    INSERT IGNORE INTO qc_judgment_result
    (id, record_id, judgment_type, judgment_time, is_final, matched_standard_ids, create_date_time, update_date_time)
    VALUES
    (CONCAT('bulk-jud-', LPAD(i, 3, '0')), rec_id, jtype, DATE_ADD('2026-06-01 08:00:05', INTERVAL i HOUR), 1, '["std001"]', @ts, @ts);

    SET i = i + 1;
  END WHILE;
END$$
DELIMITER ;

CALL seed_bulk_inspection_records();
DROP PROCEDURE IF EXISTS seed_bulk_inspection_records;
