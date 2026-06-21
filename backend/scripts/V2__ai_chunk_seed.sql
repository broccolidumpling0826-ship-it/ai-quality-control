-- ============================================================
-- RAG Chunk 演示种子数据（使用真实 standard ID）
-- 真实 ID：STD001=2055929409755439106  STD003=2056005473819181057
-- ============================================================
USE ai_quality_control;
SET @ts = '2026-06-22 00:00:00';

-- ── 文档元数据 ────────────────────────────────────────────────
INSERT IGNORE INTO qc_standard_document
  (id, standard_id, file_name, file_path, file_type, ingest_status, ingest_time, chunk_count, create_date_time, update_date_time)
VALUES
('doc-real-gbt700',    '2055929409755439106', 'GB-T 700-2006 Q235B.md',       'seed-documents/gbt700-q235b.md',          'MD', 'INDEXED', @ts, 6, @ts, @ts),
('doc-real-cust001',   '2056005473819181057', 'CUST-001 质量协议 Q235B.md',   'seed-documents/customer-spec-cust001.md', 'MD', 'INDEXED', @ts, 5, @ts, @ts);

-- ── 国标 GB/T 700-2006 Chunks ─────────────────────────────────
INSERT IGNORE INTO qc_standard_document_chunk
  (id, document_id, standard_id, chunk_index, section_ref, chunk_text, keyword_tags, create_date_time, update_date_time)
VALUES
('ck-r-gbt700-0', 'doc-real-gbt700', '2055929409755439106', 0, '§1 适用范围',
 '本标准规定了碳素结构钢Q235B的牌号、技术要求、试验方法、检验规则。适用于热轧钢板、钢带、型钢及棒材。',
 'GB/T,碳素结构钢,Q235B,适用范围', @ts, @ts),

('ck-r-gbt700-1', 'doc-real-gbt700', '2055929409755439106', 1, '§2 化学成分',
 'Q235B化学成分（质量分数）：C≤0.20%，Si≤0.35%，Mn≤1.40%，P≤0.045%，S≤0.045%。冷弯成型用途碳当量Ceq≤0.38%。',
 'Q235B,化学成分,碳含量,磷含量,硫含量', @ts, @ts),

('ck-r-gbt700-2', 'doc-real-gbt700', '2055929409755439106', 2, '§3 力学性能',
 'Q235B力学性能（纵向，板厚≤16mm）：屈服强度ReL≥235MPa；抗拉强度Rm=370～500MPa；断后伸长率A≥26%。板厚>16mm时ReL≥225MPa。',
 'Q235B,抗拉强度,Rm,屈服强度,ReL,延伸率,A,力学性能', @ts, @ts),

('ck-r-gbt700-3', 'doc-real-gbt700', '2055929409755439106', 3, '§3.3 冲击韧性',
 'Q235B冲击韧性：V型缺口夏比冲击功KV2在20°C时纵向≥27J。低温使用需另行注明要求。',
 'Q235B,冲击韧性,夏比冲击,KV2', @ts, @ts),

('ck-r-gbt700-4', 'doc-real-gbt700', '2055929409755439106', 4, '§4 交货与尺寸',
 '钢材一般以热轧状态交货。冷轧薄板厚度偏差PT.A级：厚1.5mm允许偏差±0.12mm。表面不允许有结疤、裂纹、折叠、夹层。',
 'Q235B,交货状态,热轧,冷轧,尺寸偏差', @ts, @ts),

('ck-r-gbt700-5', 'doc-real-gbt700', '2055929409755439106', 5, '§6 弯曲试验',
 'Q235B弯曲试验（180°冷弯）：板厚≤6mm时弯曲半径r=0.5a；板厚>6mm时r=1.5a。弯曲后外表面不允许开裂。',
 'Q235B,弯曲试验,冷弯,弯曲半径', @ts, @ts),

-- ── 客户协议 CUST-001 Chunks ──────────────────────────────────
('ck-r-cust001-0', 'doc-real-cust001', '2056005473819181057', 0, '§C1 适用范围',
 'CUST-001质量协议适用于Q235B冷轧薄板，厚度0.8～3.0mm，宽度900～1250mm。本协议与国标差异时以本协议为准。',
 'CUST-001,客户协议,Q235B,适用范围', @ts, @ts),

('ck-r-cust001-1', 'doc-real-cust001', '2056005473819181057', 1, '§C2 抗拉强度',
 'CUST-001要求Q235B抗拉强度Rm下限380MPa（高于国标370MPa），上限500MPa。Rm在370～380MPa时须书面批准让步接收。',
 'CUST-001,抗拉强度,Rm,Q235B,让步接收', @ts, @ts),

('ck-r-cust001-2', 'doc-real-cust001', '2056005473819181057', 2, '§C3 延伸率与屈服强度',
 'CUST-001要求：断后伸长率A≥28%（严于国标26%）；屈服强度ReL≥245MPa（高于国标235MPa）；屈强比ReL/Rm≤0.78。',
 'CUST-001,延伸率,A,屈服强度,ReL,Q235B,屈强比', @ts, @ts),

('ck-r-cust001-3', 'doc-real-cust001', '2056005473819181057', 3, '§C4 表面质量',
 'CUST-001要求表面FB级：不允许目视可见麻点、划伤、辊印。单卷累计缺陷面积>2%或单处>0.05m²判为不合格。',
 'CUST-001,表面质量,FB级,缺陷', @ts, @ts),

('ck-r-cust001-4', 'doc-real-cust001', '2056005473819181057', 4, '§C5 不合格处理',
 '不合格须出具NCR，需方可选择退货、让步接收（附加折扣）或复检。让步接收需质量工程师书面批准。',
 'CUST-001,不合格,NCR,让步接收,复检', @ts, @ts);

-- ── 标准冲突演示样例（5 条）───────────────────────────────────
INSERT IGNORE INTO qc_standard_conflict
  (id, variety, grade, indicator_id, standard_id_a, standard_id_b,
   limit_a_upper, limit_a_lower, limit_b_upper, limit_b_lower,
   conflict_status, resolution_note, resolved_by, resolved_time,
   demo_flag, create_date_time, update_date_time)
VALUES
-- 冲突1：国标 vs 客协 抗拉强度下限 370 vs 380
('demo-cf-rm',     '冷轧板', 'Q235B', 'ind001', '2055929409755439106', '2056005473819181057',
 500.000000, 370.000000, 500.000000, 380.000000,
 'PENDING', NULL, NULL, NULL, 1, @ts, @ts),
-- 冲突2：国标 vs 客协 延伸率 26% vs 28%
('demo-cf-a',      '冷轧板', 'Q235B', 'ind002', '2055929409755439106', '2056005473819181057',
 NULL, 26.000000, NULL, 28.000000,
 'PENDING', NULL, NULL, NULL, 1, @ts, @ts),
-- 冲突3：国标 vs 客协 屈服强度 235 vs 245
('demo-cf-rel',    '冷轧板', 'Q235B', 'ind003', '2055929409755439106', '2056005473819181057',
 NULL, 235.000000, NULL, 245.000000,
 'PENDING', NULL, NULL, NULL, 1, @ts, @ts),
-- 冲突4：已裁定（国标抗拉强度范围差异，以客协为准）
('demo-cf-resolved','冷轧板', 'Q235B', 'ind001', '2055929409755439106', '2056005473819181057',
 500.000000, 370.000000, 500.000000, 380.000000,
 'RESOLVED', '经协商，本批次以客协下限 380 MPa 为准，国标为兜底。', '021001', '2026-06-20 14:00:00',
 1, @ts, @ts),
-- 冲突5：SPHC 国标 vs 客协 抗拉强度（演示跨牌号）
('demo-cf-sphc',   '热轧板', 'SPHC', 'ind001', '2055932444430827522', '2056005473819181057',
 440.000000, 270.000000, 500.000000, 380.000000,
 'PENDING', NULL, NULL, NULL, 1, @ts, @ts);

SELECT '✓ chunk_count' AS label, COUNT(*) AS n FROM qc_standard_document_chunk;
SELECT '✓ conflict_demo' AS label, COUNT(*) AS n FROM qc_standard_conflict WHERE demo_flag=1;
