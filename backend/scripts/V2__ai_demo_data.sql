-- ============================================================
-- 002 AI 演示数据 seed — MySQL 5.7.43
-- 依赖：init-schema.sql、init-test-data.sql、V2__ai_schema.sql
-- 执行：mysql -h 127.0.0.1 -P 3307 -u user -p --default-character-set=utf8mb4 ai_quality_control < backend/scripts/V2__ai_demo_data.sql
-- ============================================================
USE ai_quality_control;
SET NAMES utf8mb4;

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

-- ══════════════════════════════════════════════════════════════
-- T046  标准/协议文档元数据 + Chunk 预置（20 份文档，共 60 个片段）
-- ══════════════════════════════════════════════════════════════

-- ── 文档元数据 ────────────────────────────────────────────────
INSERT IGNORE INTO qc_standard_document
  (id, standard_id, file_name, file_path, file_type, ingest_status, ingest_time, chunk_count, create_date_time, update_date_time)
VALUES
-- GB/T 700-2006 国标（6 份，按章节拆分）
('doc-gbt700-01','std001','GB-T 700-2006 总则与适用范围.md','seed-documents/gbt700-ch1-scope.md','MD','INDEXED','2026-06-20 08:00:00',3,@ts,@ts),
('doc-gbt700-02','std001','GB-T 700-2006 化学成分要求.md','seed-documents/gbt700-ch2-chemistry.md','MD','INDEXED','2026-06-20 08:01:00',3,@ts,@ts),
('doc-gbt700-03','std001','GB-T 700-2006 力学性能要求.md','seed-documents/gbt700-ch3-mechanical.md','MD','INDEXED','2026-06-20 08:02:00',3,@ts,@ts),
('doc-gbt700-04','std001','GB-T 700-2006 交货状态与尺寸.md','seed-documents/gbt700-ch4-delivery.md','MD','INDEXED','2026-06-20 08:03:00',3,@ts,@ts),
('doc-gbt700-05','std001','GB-T 700-2006 检验方法.md','seed-documents/gbt700-ch5-test.md','MD','INDEXED','2026-06-20 08:04:00',3,@ts,@ts),
('doc-gbt700-06','std001','GB-T 700-2006 附录A弯曲试验.md','seed-documents/gbt700-appendixA.md','MD','INDEXED','2026-06-20 08:05:00',3,@ts,@ts),
-- 客户协议（CUST-001，4 份）
('doc-cust001-01','std002','CUST-001 质量协议-总体要求.md','seed-documents/cust001-general.md','MD','INDEXED','2026-06-20 08:10:00',3,@ts,@ts),
('doc-cust001-02','std002','CUST-001 质量协议-抗拉强度.md','seed-documents/cust001-tensile.md','MD','INDEXED','2026-06-20 08:11:00',3,@ts,@ts),
('doc-cust001-03','std002','CUST-001 质量协议-延伸率与屈服强度.md','seed-documents/cust001-elongation.md','MD','INDEXED','2026-06-20 08:12:00',3,@ts,@ts),
('doc-cust001-04','std002','CUST-001 质量协议-表面质量要求.md','seed-documents/cust001-surface.md','MD','INDEXED','2026-06-20 08:13:00',3,@ts,@ts),
-- 企业标准 QB/JH-001-2024（4 份）
('doc-ent001-01','std003','QB-JH-001-2024 企标范围.md','seed-documents/ent001-scope.md','MD','INDEXED','2026-06-20 08:20:00',3,@ts,@ts),
('doc-ent001-02','std003','QB-JH-001-2024 力学性能.md','seed-documents/ent001-mechanical.md','MD','INDEXED','2026-06-20 08:21:00',3,@ts,@ts),
('doc-ent001-03','std003','QB-JH-001-2024 化学成分控制.md','seed-documents/ent001-chemistry.md','MD','INDEXED','2026-06-20 08:22:00',3,@ts,@ts),
('doc-ent001-04','std003','QB-JH-001-2024 让步判定条款.md','seed-documents/ent001-concession.md','MD','INDEXED','2026-06-20 08:23:00',3,@ts,@ts),
-- 行业标准 YB/T 5133（3 份）
('doc-ybt001-01','std004','YB-T 5133 冷轧薄钢板通则.md','seed-documents/ybt5133-general.md','MD','INDEXED','2026-06-20 08:30:00',3,@ts,@ts),
('doc-ybt001-02','std004','YB-T 5133 力学性能限值.md','seed-documents/ybt5133-mechanical.md','MD','INDEXED','2026-06-20 08:31:00',3,@ts,@ts),
('doc-ybt001-03','std004','YB-T 5133 抽样与检验频次.md','seed-documents/ybt5133-sampling.md','MD','INDEXED','2026-06-20 08:32:00',3,@ts,@ts),
-- 协议补充条款（std005，3 份）
('doc-supp001-01','std005','补充协议-抗拉强度上浮修正.md','seed-documents/supp001-tensile-adj.md','MD','INDEXED','2026-06-20 08:40:00',3,@ts,@ts),
('doc-supp001-02','std005','补充协议-延伸率下限豁免条款.md','seed-documents/supp001-elongation-exc.md','MD','INDEXED','2026-06-20 08:41:00',3,@ts,@ts),
('doc-supp001-03','std005','补充协议-硫磷含量特别规定.md','seed-documents/supp001-sp-limits.md','MD','INDEXED','2026-06-20 08:42:00',3,@ts,@ts);

-- ── Chunk 片段（每份文档 3 个 chunk，共 60 条）────────────────

-- doc-gbt700-01 总则
INSERT IGNORE INTO qc_standard_document_chunk (id,document_id,standard_id,chunk_index,section_ref,chunk_text,keyword_tags,create_date_time,update_date_time) VALUES
('ck-gbt700-01-0','doc-gbt700-01','std001',0,'§1.1 范围','本标准规定了碳素结构钢（Q195、Q215、Q235、Q255、Q275）的牌号、订货内容、尺寸、外形、重量及允许偏差、技术要求、试验方法、检验规则及包装、标志和质量证明书等。本标准适用于热轧钢板、钢带、型钢及棒材。','GB/T,碳素结构钢,Q235B',@ts,@ts),
('ck-gbt700-01-1','doc-gbt700-01','std001',1,'§1.2 规范性引用','下列文件中的条款通过本标准的引用而成为本标准的条款：GB/T 228.1 金属材料 拉伸试验；GB/T 232 金属材料 弯曲试验；GB/T 20066 钢和铁 化学成分测定样品的取样和制样。','GB/T,规范引用',@ts,@ts),
('ck-gbt700-01-2','doc-gbt700-01','std001',2,'§1.3 术语','屈服强度（ReL）：拉伸试验时，试样产生屈服现象所对应的应力值，单位 MPa。抗拉强度（Rm）：试样断裂前承受的最大力所对应的应力值，单位 MPa。','屈服强度,抗拉强度,ReL,Rm',@ts,@ts),

-- doc-gbt700-02 化学成分
('ck-gbt700-02-0','doc-gbt700-02','std001',0,'§2.1 化学成分','Q235B 牌号的化学成分（熔炼分析，质量分数，%）：C ≤ 0.20，Si ≤ 0.35，Mn ≤ 1.40，P ≤ 0.045，S ≤ 0.045。允许偏差按 GB/T 222 规定。','Q235B,化学成分,碳含量',@ts,@ts),
('ck-gbt700-02-1','doc-gbt700-02','std001',1,'§2.2 特殊成分控制','对于需要进行冷弯成型的钢材，碳当量 Ceq = C + Mn/6 ≤ 0.38%。磷、硫含量应满足 P ≤ 0.040，S ≤ 0.040 的严格要求。','磷含量,碳当量,冷弯成型',@ts,@ts),
('ck-gbt700-02-2','doc-gbt700-02','std001',2,'§2.3 残余元素','当需方在合同中要求时，钢中残余元素铬（Cr）、镍（Ni）、铜（Cu）的含量各不应超过 0.30%，且三者之和不应超过 0.60%。','残余元素,铬,镍,铜',@ts,@ts),

-- doc-gbt700-03 力学性能
('ck-gbt700-03-0','doc-gbt700-03','std001',0,'§3.1 拉伸性能','Q235B 力学性能（纵向）：屈服强度 ReL ≥ 235 MPa（板厚 ≤16 mm）；抗拉强度 Rm = 370～500 MPa；断后伸长率 A ≥ 26%（板厚 ≤40 mm）。','Q235B,Rm,ReL,抗拉强度,延伸率',@ts,@ts),
('ck-gbt700-03-1','doc-gbt700-03','std001',1,'§3.2 厚度分级','当板厚 >16 mm 且 ≤40 mm 时，屈服强度 ReL ≥ 225 MPa；当板厚 >40 mm 且 ≤60 mm 时，ReL ≥ 215 MPa。厚度越大，屈服强度允许值越低。','屈服强度,厚度分级,Q235B',@ts,@ts),
('ck-gbt700-03-2','doc-gbt700-03','std001',2,'§3.3 冲击韧性','Q235B 应进行冲击试验，V 型缺口夏比冲击功（KV2）在 20°C 时纵向 ≥ 27 J。当设计温度低于 0°C 时，需方应在订货时另行注明低温冲击要求。','冲击韧性,夏比冲击,Q235B',@ts,@ts),

-- doc-gbt700-04 交货状态
('ck-gbt700-04-0','doc-gbt700-04','std001',0,'§4.1 交货状态','钢材一般以热轧状态交货。需方有特殊要求时，可订购正火或退火状态的钢材，并在合同中注明。','热轧,交货状态,正火',@ts,@ts),
('ck-gbt700-04-1','doc-gbt700-04','std001',1,'§4.2 尺寸与偏差','冷轧薄钢板厚度偏差按精度等级分 PT.A（普通）和 PT.B（高精度）。PT.A 级：厚度 1.5 mm 时允许偏差 ±0.12 mm；PT.B 级：±0.07 mm。','尺寸,厚度偏差,冷轧',@ts,@ts),
('ck-gbt700-04-2','doc-gbt700-04','std001',2,'§4.3 表面质量','钢板表面不允许有结疤、裂纹、折叠、夹层等缺陷。表面允许有深度不超过负偏差之半的轻微麻点、氧化铁皮压入及划伤。','表面质量,缺陷',@ts,@ts),

-- doc-gbt700-05 检验方法
('ck-gbt700-05-0','doc-gbt700-05','std001',0,'§5.1 拉伸试验','拉伸试样按 GB/T 228.1 规定制备，取样方向为纵向。试验速率在测量屈服强度时应 ≤ 30 MPa/s；测量抗拉强度时应使横梁速率 ≤ 0.008/s。','拉伸试验,GB/T 228.1,试验方法',@ts,@ts),
('ck-gbt700-05-1','doc-gbt700-05','std001',1,'§5.2 化学分析','化学成分分析按 GB/T 223 系列标准规定的方法执行，仲裁分析方法以 GB/T 223 为准。','化学分析,仲裁',@ts,@ts),
('ck-gbt700-05-2','doc-gbt700-05','std001',2,'§5.3 检验频次','力学性能检验：每批次（≤60 t）取样 1 组；厚度 ≤4 mm 每批取 2 组。化学分析每炉取 1 个试样。','检验频次,批次',@ts,@ts),

-- doc-gbt700-06 附录A
('ck-gbt700-06-0','doc-gbt700-06','std001',0,'§A.1 弯曲试验条件','Q235B 弯曲试验（180°冷弯）：板厚 ≤ 6 mm 时，弯曲半径 r = 0.5a（a 为板厚）；板厚 > 6 mm 时 r = 1.5a。弯曲后外表面不允许开裂。','弯曲试验,Q235B,冷弯',@ts,@ts),
('ck-gbt700-06-1','doc-gbt700-06','std001',1,'§A.2 取样位置','弯曲试样应取自距板边 > 2 mm 处，试样纵轴应与轧制方向垂直（横向弯曲）。','弯曲取样,轧制方向',@ts,@ts),
('ck-gbt700-06-2','doc-gbt700-06','std001',2,'§A.3 合格判定','若弯曲后试样外表面及侧面无肉眼可见裂纹，则判为弯曲合格。','弯曲合格,判定',@ts,@ts),

-- doc-cust001-01 客协总则
('ck-cust001-01-0','doc-cust001-01','std002',0,'§C1.1 适用范围','本协议适用于供应商向 CUST-001 交付的所有 Q235B 冷轧薄板产品，厚度范围 0.8～3.0 mm，宽度范围 900～1250 mm。','客户协议,CUST-001,Q235B',@ts,@ts),
('ck-cust001-01-1','doc-cust001-01','std002',1,'§C1.2 优先级','本协议与国家标准存在差异时，以本协议为准；本协议未规定的项目，按 GB/T 700 执行。','优先级,客协',@ts,@ts),
('ck-cust001-01-2','doc-cust001-01','std002',2,'§C1.3 不合格处理','任何单项不合格均须出具不合格通知单（NCR），需方可选择退货、让步接收（附加折扣）或复检。','不合格,NCR,让步接收',@ts,@ts),

-- doc-cust001-02 客协抗拉强度
('ck-cust001-02-0','doc-cust001-02','std002',0,'§C2.1 抗拉强度限值','Q235B 成品抗拉强度 Rm 要求：下限 380 MPa，上限 500 MPa。本协议抗拉强度下限高于国标（370 MPa），以确保足够安全裕度。','抗拉强度,Rm,Q235B,客户协议',@ts,@ts),
('ck-cust001-02-1','doc-cust001-02','std002',1,'§C2.2 让步范围','当 Rm 在 370～380 MPa 之间时，供应商须提供同批次至少 3 组实测数据及历史记录，经需方质量工程师书面批准后方可让步接收。','让步,抗拉强度,Q235B',@ts,@ts),
('ck-cust001-02-2','doc-cust001-02','std002',2,'§C2.3 抽检频次','每批次（≤50 t）须提供至少 2 组拉伸试验数据，取样位置分别为卷头和卷中。','抽检频次,拉伸试验',@ts,@ts),

-- doc-cust001-03 客协延伸率
('ck-cust001-03-0','doc-cust001-03','std002',0,'§C3.1 断后伸长率','Q235B 断后伸长率 A（标距 80 mm，纵向）须 ≥ 28%，严于国标 26% 要求。此要求针对厚度 ≤ 2.0 mm 的薄板。','延伸率,A,Q235B,客户协议',@ts,@ts),
('ck-cust001-03-1','doc-cust001-03','std002',1,'§C3.2 屈服强度','下屈服强度 ReL ≥ 245 MPa（厚度 ≤ 16 mm），高于国标 235 MPa，以保证冲压成型稳定性。','屈服强度,ReL,Q235B',@ts,@ts),
('ck-cust001-03-2','doc-cust001-03','std002',2,'§C3.3 屈强比','要求屈强比 ReL/Rm ≤ 0.78，防止钢材在成型中发生脆性断裂。','屈强比,成型性',@ts,@ts),

-- doc-cust001-04 客协表面质量
('ck-cust001-04-0','doc-cust001-04','std002',0,'§C4.1 表面等级','表面质量按 FB 级（较高级精整面）供货。不允许有目视可见的麻点、划伤、辊印及氧化色斑。','表面质量,FB级,客户协议',@ts,@ts),
('ck-cust001-04-1','doc-cust001-04','std002',1,'§C4.2 镀层（如适用）','有涂油要求的钢板须均匀涂防锈油，油量 0.5～1.5 g/m²；无油产品须在发货前完成脱脂处理。','涂油,防锈,表面处理',@ts,@ts),
('ck-cust001-04-2','doc-cust001-04','std002',2,'§C4.3 不合格判定','单卷内出现累计面积 > 2% 的表面缺陷，或单处缺陷面积 > 0.05 m²，则判定为表面不合格。','表面不合格,缺陷面积',@ts,@ts),

-- doc-ent001-01 企标范围
('ck-ent001-01-0','doc-ent001-01','std003',0,'§E1.1 适用范围','本标准规定了本公司生产的冷轧薄板（Q195B、Q235B、Q275B）的技术要求、检验方法、检验规则及出厂检验。','企业标准,冷轧薄板,Q235B',@ts,@ts),
('ck-ent001-01-1','doc-ent001-01','std003',1,'§E1.2 与国标关系','本标准在满足 GB/T 700 的基础上，对力学性能和化学成分进行了更严格的控制。凡国标有规定而本标准未提及处，均按国标执行。','企业标准,国标,Q235B',@ts,@ts),
('ck-ent001-01-2','doc-ent001-01','std003',2,'§E1.3 版本修订','本标准 2024 年第一次修订，主要调整了 Q235B 的抗拉强度上限（由 500 MPa 调整为 490 MPa）及碳含量控制上限（由 0.20% 降至 0.19%）。','版本修订,Q235B,企标',@ts,@ts),

-- doc-ent001-02 企标力学性能
('ck-ent001-02-0','doc-ent001-02','std003',0,'§E2.1 Q235B 力学性能','按本企业标准，Q235B 冷轧薄板力学性能要求：Rm = 380～490 MPa；ReL ≥ 240 MPa；A ≥ 27%（标距 80 mm）。','Rm,ReL,A,Q235B,企标',@ts,@ts),
('ck-ent001-02-1','doc-ent001-02','std003',1,'§E2.2 严格模式','对于 I 类（关键零件）用途，须满足严格模式：Rm ≥ 395 MPa，A ≥ 29%，且同批次极差 ΔRm ≤ 30 MPa。','严格模式,Rm,A',@ts,@ts),
('ck-ent001-02-2','doc-ent001-02','std003',2,'§E2.3 抗时效性','本标准要求钢材应进行时效处理，时效后 A 值降低不应超过初始值的 3 个百分点。','时效性,延伸率',@ts,@ts),

-- doc-ent001-03 企标化学成分
('ck-ent001-03-0','doc-ent001-03','std003',0,'§E3.1 化学成分（熔炼）','Q235B 企标化学成分（质量分数，%）：C ≤ 0.19，Si ≤ 0.30，Mn ≤ 1.30，P ≤ 0.035，S ≤ 0.035。各元素控制严于国标。','化学成分,Q235B,企标',@ts,@ts),
('ck-ent001-03-1','doc-ent001-03','std003',1,'§E3.2 碳含量控制','碳含量对焊接性能影响显著。企标将 C 上限从 0.20% 降至 0.19%，可有效改善焊接热影响区韧性，减少冷裂纹风险。','碳含量,焊接性,Q235B',@ts,@ts),
('ck-ent001-03-2','doc-ent001-03','std003',2,'§E3.3 成品分析偏差','成品化学成分允许偏差按 GB/T 222-2006 附录 A 规定执行，P、S 成品偏差上限各加 0.005%。','成品分析,化学成分偏差',@ts,@ts),

-- doc-ent001-04 企标让步条款
('ck-ent001-04-0','doc-ent001-04','std003',0,'§E4.1 让步判定条件','当单项指标偏出规格值不超过 5%，且其他所有指标均在规格内时，允许申请让步接收。让步接收须完成书面审批流程。','让步判定,Q235B,企标',@ts,@ts),
('ck-ent001-04-1','doc-ent001-04','std003',1,'§E4.2 让步记录','每次让步接收须在 ERP 系统中记录：批次号、偏出项目、偏出量、审批人、使用方向（降级使用 / 原用途使用）。','让步记录,ERP,批次',@ts,@ts),
('ck-ent001-04-2','doc-ent001-04','std003',2,'§E4.3 让步频次控制','同一牌号同一指标在连续 3 个月内让步次数不得超过 2 次，否则须触发工艺改进流程。','让步频次,工艺改进',@ts,@ts),

-- doc-ybt001-01 行标通则
('ck-ybt001-01-0','doc-ybt001-01','std004',0,'§Y1.1 行业标准范围','YB/T 5133 规定了普通冷轧薄钢板及钢带的技术条件，适用于非合金钢冷轧薄板，厚度 0.5～3.0 mm。','行业标准,YB/T,冷轧薄板',@ts,@ts),
('ck-ybt001-01-1','doc-ybt001-01','std004',1,'§Y1.2 与 GB/T 700 的关系','对于已在 GB/T 700 中规定的牌号，YB/T 5133 提供补充性技术要求，两者不矛盾时并行适用。','行业标准,国标,Q235B',@ts,@ts),
('ck-ybt001-01-2','doc-ybt001-01','std004',2,'§Y1.3 品种规格','包括：热轧板（HR）、冷轧板（CR）、镀锌板（GI/GA）。本文件重点覆盖 CR 类产品。','品种规格,冷轧,热轧',@ts,@ts),

-- doc-ybt001-02 行标力学性能
('ck-ybt001-02-0','doc-ybt001-02','std004',0,'§Y2.1 力学性能基础值','冷轧薄板（Q235）抗拉强度 Rm ≥ 365 MPa（仅适用厚度 > 2.5 mm 时的特别豁免），一般要求 ≥ 370 MPa，与 GB/T 700 一致。','Rm,行标,Q235,冷轧',@ts,@ts),
('ck-ybt001-02-1','doc-ybt001-02','std004',1,'§Y2.2 特殊厚度豁免','当板厚 > 2.5 mm 时，断后伸长率 A 可放宽至 ≥ 24%（代替通用的 26%）。需方需在订货时注明豁免要求。','延伸率,豁免,行标',@ts,@ts),
('ck-ybt001-02-2','doc-ybt001-02','std004',2,'§Y2.3 硬度要求（参考值）','洛氏硬度 HRB ≤ 80（参考值，非强制指标），用于评估成型加工适应性。','硬度,HRB,成型',@ts,@ts),

-- doc-ybt001-03 行标抽样
('ck-ybt001-03-0','doc-ybt001-03','std004',0,'§Y3.1 组批规则','同一炉号、同一牌号、同一规格的钢材组成一个检验批，每批重量 ≤ 60 t。超重时须分批检验。','组批,检验批,行标',@ts,@ts),
('ck-ybt001-03-1','doc-ybt001-03','std004',1,'§Y3.2 取样数量','每批取 2 个拉伸试样、1 个弯曲试样、1 个化学分析试样。取样位置为卷头 500 mm 之后。','取样,拉伸,化学分析',@ts,@ts),
('ck-ybt001-03-2','doc-ybt001-03','std004',2,'§Y3.3 复检规定','若首次检验有 1 项不合格，允许加倍取样复检。复检后仍不合格则判该批次不合格，不得再次复检。','复检,不合格,行标',@ts,@ts),

-- doc-supp001-01 补充协议-抗拉强度修正
('ck-supp001-01-0','doc-supp001-01','std005',0,'§S1.1 背景','鉴于连续三批次（2026-Q1）Q235B 抗拉强度实测值集中在 372～378 MPa（高于国标但低于客协下限 380 MPa），双方经协商，达成如下修正条款。','补充协议,抗拉强度,Q235B',@ts,@ts),
('ck-supp001-01-1','doc-supp001-01','std005',1,'§S1.2 修正范围','本次修正有效期 2026-04-01 至 2026-09-30。在此期间，Rm 下限临时调整为 372 MPa，但每批须提交完整实测分布报告。','修正范围,Rm,补充协议',@ts,@ts),
('ck-supp001-01-2','doc-supp001-01','std005',2,'§S1.3 后续处理','供应商应在 2026-10-01 前提交工艺改进报告，确保后续批次 Rm ≥ 380 MPa。否则恢复原协议并附加质量扣款。','工艺改进,后续处理,抗拉强度',@ts,@ts),

-- doc-supp001-02 补充协议-延伸率豁免
('ck-supp001-02-0','doc-supp001-02','std005',0,'§S2.1 豁免条件','当产品设计为非冲压用途（如结构件）时，A 可由 ≥ 28% 豁免至 ≥ 26%，须在订货合同中明确注明用途。','延伸率,豁免,补充协议',@ts,@ts),
('ck-supp001-02-1','doc-supp001-02','std005',1,'§S2.2 豁免限制','每年豁免批次不超过总交货量的 15%。超出比例时，客户有权拒绝豁免并要求退货。','豁免限制,延伸率,A',@ts,@ts),
('ck-supp001-02-2','doc-supp001-02','std005',2,'§S2.3 记录要求','所有豁免批次须在质量证明书上标注"Non-Stamping Use"，并在系统中创建让步接收记录。','豁免记录,质量证明书,让步',@ts,@ts),

-- doc-supp001-03 补充协议-硫磷含量
('ck-supp001-03-0','doc-supp001-03','std005',0,'§S3.1 硫磷限值','补充协议要求 P ≤ 0.030%、S ≤ 0.025%，严于国标（P ≤ 0.045，S ≤ 0.045）和企标（P/S ≤ 0.035）。','磷含量,硫含量,P,S,补充协议',@ts,@ts),
('ck-supp001-03-1','doc-supp001-03','std005',1,'§S3.2 适用场景','本条款适用于焊接结构件用途，S 含量过高会增加焊接热裂纹风险。低 S、P 改善焊缝低温韧性。','硫磷,焊接,热裂纹',@ts,@ts),
('ck-supp001-03-2','doc-supp001-03','std005',2,'§S3.3 检验方式','每炉须提供 1 次熔炼分析报告，发货前须通过成品分析验证。P/S 仲裁方法采用 ICP 光谱法。','硫磷检验,ICP,仲裁',@ts,@ts);

-- ══════════════════════════════════════════════════════════════
-- T047  标准冲突演示样例（5 条，demo_flag=1）
-- ══════════════════════════════════════════════════════════════
INSERT IGNORE INTO qc_standard_conflict
  (id, variety, grade, indicator_id, standard_id_a, standard_id_b,
   limit_a_upper, limit_a_lower, limit_b_upper, limit_b_lower,
   conflict_status, resolution_note, resolved_by, resolved_time,
   demo_flag, create_date_time, update_date_time)
VALUES
-- 冲突 1：国标 vs 客协 — 抗拉强度下限（std001 lower=370 vs std002 lower=380）
('demo-conflict-rm',
 '冷轧板', 'Q235B', 'ind001', 'std001', 'std002',
 500.000000, 370.000000,   -- 国标 Rm [370, 500]
 500.000000, 380.000000,   -- 客协 Rm [380, 500]
 'PENDING', NULL, NULL, NULL, 1, @ts, @ts),

-- 冲突 2：国标 vs 客协 — 延伸率下限（std001 lower=26 vs std002 lower=28）
('demo-conflict-a',
 '冷轧板', 'Q235B', 'ind002', 'std001', 'std002',
 NULL, 26.000000,   -- 国标 A ≥ 26%
 NULL, 28.000000,   -- 客协 A ≥ 28%
 'PENDING', NULL, NULL, NULL, 1, @ts, @ts),

-- 冲突 3：国标 vs 企标 — 屈服强度下限（std001 lower=235 vs std003 lower=240）
('demo-conflict-rel',
 '冷轧板', 'Q235B', 'ind003', 'std001', 'std003',
 NULL, 235.000000,   -- 国标 ReL ≥ 235 MPa
 NULL, 240.000000,   -- 企标 ReL ≥ 240 MPa
 'PENDING', NULL, NULL, NULL, 1, @ts, @ts),

-- 冲突 4：企标 vs 补充协议 — 抗拉强度下限（std003 lower=380 vs std005 lower=372，临时修正）
('demo-conflict-rm-supp',
 '冷轧板', 'Q235B', 'ind001', 'std003', 'std005',
 490.000000, 380.000000,   -- 企标 Rm [380, 490]
 490.000000, 372.000000,   -- 补充协议临时下限 372
 'RESOLVED', '经双方协商，2026-04-01 至 2026-09-30 期间以补充协议 372 MPa 为准。',
 '021001', '2026-06-18 14:00:00', 1, @ts, @ts),

-- 冲突 5：客协 vs 企标 — 延伸率下限（std002 lower=28 vs std003 lower=27）
('demo-conflict-a-ent',
 '冷轧板', 'Q235B', 'ind002', 'std002', 'std003',
 NULL, 28.000000,   -- 客协 A ≥ 28%
 NULL, 27.000000,   -- 企标 A ≥ 27%
 'PENDING', NULL, NULL, NULL, 1, @ts, @ts);
