-- ============================================================
-- P0 pre-generated AI cache for offline demos.
-- Depends on 20260621_01_ai_quality_p0_schema.sql and clause seeds.
-- MySQL 5.7 compatible and rerunnable with INSERT IGNORE.
-- ============================================================

USE ai_quality_control;

INSERT IGNORE INTO `qc_ai_cache` (
  `id`, `cache_key`, `assessment_type`, `business_type`, `business_id`,
  `prompt_version`, `input_hash`, `cached_output`, `references_json`,
  `confidence_label`, `confidence_score`, `degradation_source`, `enabled`,
  `create_user_no`, `create_date_time`
) VALUES
  ('cache_p0_exp_jud001', 'P0:JUDGMENT_EXPLANATION:jud001', 'JUDGMENT_EXPLANATION', 'JUDGMENT', 'jud001',
   'p0-cache-v1', 'p0-exp-jud001',
   '{"judgmentType":"QUALIFIED","summary":"该卷按 GB/T 912-2008 Q235B 结构化限值判定为合格。Rm=430 MPa 位于 370-510 MPa 范围内，A=30.5% 高于 26% 下限。","degradation":{"source":"CACHE","reason":"P0预生成判定解释缓存"},"confidence":{"label":"HIGH","score":0.92},"citations":["p0_clause_gb_rm","p0_clause_gb_a"]}',
   '{"citations":[{"clauseId":"p0_clause_gb_rm","standardCode":"GB/T 912-2008","clauseNo":"5.1"},{"clauseId":"p0_clause_gb_a","standardCode":"GB/T 912-2008","clauseNo":"5.2"}]}',
   'HIGH', 0.920000, 'CACHE', 1, 'system', NOW()),
  ('cache_p0_exp_jud002', 'P0:JUDGMENT_EXPLANATION:jud002', 'JUDGMENT_EXPLANATION', 'JUDGMENT', 'jud002',
   'p0-cache-v1', 'p0-exp-jud002',
   '{"judgmentType":"UNQUALIFIED","summary":"该卷命中客户协议 协议C2025-088-v1。Rm=360 MPa 低于客户协议下限 380 MPa，A=27% 低于客户协议下限 28%，因此判定为不合格。","degradation":{"source":"CACHE","reason":"P0预生成判定解释缓存"},"confidence":{"label":"HIGH","score":0.90},"citations":["p0_clause_c1_rm","p0_clause_c1_a"]}',
   '{"citations":[{"clauseId":"p0_clause_c1_rm","standardCode":"协议C2025-088-v1","clauseNo":"3.1"},{"clauseId":"p0_clause_c1_a","standardCode":"协议C2025-088-v1","clauseNo":"3.2"}]}',
   'HIGH', 0.900000, 'CACHE', 1, 'system', NOW()),
  ('cache_p0_exp_jud003', 'P0:JUDGMENT_EXPLANATION:jud003', 'JUDGMENT_EXPLANATION', 'JUDGMENT', 'jud003',
   'p0-cache-v1', 'p0-exp-jud003',
   '{"judgmentType":"CAN_CONCESSION","summary":"该卷按 GB/T 912-2008 Q235B 判定为可让步。Rm=365 MPa 低于合格下限 370 MPa 但未低于让步下限 360 MPa，A=25% 低于合格下限 26% 但未低于让步下限 24%。","degradation":{"source":"CACHE","reason":"P0预生成判定解释缓存"},"confidence":{"label":"MEDIUM","score":0.78},"citations":["p0_clause_gb_rm","p0_clause_gb_a","p0_clause_case_condition"]}',
   '{"citations":[{"clauseId":"p0_clause_gb_rm","standardCode":"GB/T 912-2008","clauseNo":"5.1"},{"clauseId":"p0_clause_gb_a","standardCode":"GB/T 912-2008","clauseNo":"5.2"},{"clauseId":"p0_clause_case_condition","standardCode":"CASE-P0-2025-001","clauseNo":"CASE-2025-001"}]}',
   'MEDIUM', 0.780000, 'CACHE', 1, 'system', NOW()),
  ('cache_p0_exp_jud004', 'P0:JUDGMENT_EXPLANATION:jud004', 'JUDGMENT_EXPLANATION', 'JUDGMENT', 'jud004',
   'p0-cache-v1', 'p0-exp-jud004',
   '{"judgmentType":"STANDARD_CONFLICT","summary":"该卷存在未裁决标准冲突。客户协议 v1 要求 Rm 下限 380 MPa，客户协议 v2 要求 Rm 下限 395 MPa，两个同优先级协议在同一客户、同一规格和同一有效期窗口内重叠，禁止直接给出放行结论。","degradation":{"source":"CACHE","reason":"P0预生成判定解释缓存"},"confidence":{"label":"LOW","score":0.35},"citations":["p0_clause_c1_rm","p0_clause_c2_rm"]}',
   '{"citations":[{"clauseId":"p0_clause_c1_rm","standardCode":"协议C2025-088-v1","clauseNo":"3.1"},{"clauseId":"p0_clause_c2_rm","standardCode":"协议C2025-088-v2","clauseNo":"3.1"}],"conflictId":"scf_p0_001"}',
   'LOW', 0.350000, 'CACHE', 1, 'system', NOW()),
  ('cache_p0_concession_jud003', 'P0:CONCESSION_RISK:jud003', 'CONCESSION_RISK', 'JUDGMENT', 'jud003',
   'p0-cache-v1', 'p0-concession-jud003',
   '{"riskLevel":"MEDIUM","mustReview":true,"missingInfo":[],"suggestedConditions":["限制为建筑围护和普通结构件用途","客户书面确认","增加同批次抽检","质保书备注让步依据","优先评估 ZALT001 替代发货"],"blockingReasons":[],"dimensionReasons":{"customerUsage":"客户画像为建筑围护和普通结构件，非安全关键用途。","deviationDegree":"Rm 低 5 MPa，A 低 1%，均在让步带内。","complaintHistory":"历史案例显示普通结构件可进入人工让步评审。","alternativeStock":"存在同规格可用替代资源 ZALT001。"},"confidence":{"label":"MEDIUM","score":0.76},"degradation":{"source":"CACHE","reason":"P0预生成让步风险缓存"},"evidenceRefs":["p0_clause_gb_a","p0_clause_case_finding","p0_clause_case_condition"]}',
   '{"citations":[{"clauseId":"p0_clause_gb_a","standardCode":"GB/T 912-2008","clauseNo":"5.2"},{"clauseId":"p0_clause_case_finding","standardCode":"CASE-P0-2025-001","clauseNo":"CASE-2025-001"},{"clauseId":"p0_clause_case_condition","standardCode":"CASE-P0-2025-001","clauseNo":"CASE-2025-001"}]}',
   'MEDIUM', 0.760000, 'CACHE', 1, 'system', NOW()),
  ('cache_p0_cert_qa_z001001', 'P0:CERT_QA:COIL:Z001001:WHY_CERT', 'CERT_QA', 'COIL', 'Z001001',
   'p0-cache-v1', 'p0-cert-z001001',
   '{"question":"这卷为什么能出证？","answer":"Z001001 当前最终判定为合格。关键性能指标 Rm=430 MPa 满足 370-510 MPa，A=30.5% 满足不低于 26% 的要求，因此可生成正式质保书数据。","finalState":"FINAL","confidence":{"label":"HIGH","score":0.91},"degradation":{"source":"CACHE","reason":"P0预生成质保书问答缓存"},"citations":["p0_clause_gb_rm","p0_clause_gb_a"]}',
   '{"citations":[{"clauseId":"p0_clause_gb_rm","standardCode":"GB/T 912-2008","clauseNo":"5.1"},{"clauseId":"p0_clause_gb_a","standardCode":"GB/T 912-2008","clauseNo":"5.2"}]}',
   'HIGH', 0.910000, 'CACHE', 1, 'system', NOW()),
  ('cache_p0_cert_qa_z003001', 'P0:CERT_QA:COIL:Z003001:CAN_CERT', 'CERT_QA', 'COIL', 'Z003001',
   'p0-cache-v1', 'p0-cert-z003001',
   '{"question":"这卷当前能否生成正式质保书？","answer":"Z003001 当前判定为可让步，不是正式放行状态。系统只能给出内部预览或让步说明，必须完成让步审批和客户确认后才能正式生成质保书。","finalState":"PREVIEW","confidence":{"label":"MEDIUM","score":0.74},"degradation":{"source":"CACHE","reason":"P0预生成质保书问答缓存"},"citations":["p0_clause_gb_a","p0_clause_case_condition"]}',
   '{"citations":[{"clauseId":"p0_clause_gb_a","standardCode":"GB/T 912-2008","clauseNo":"5.2"},{"clauseId":"p0_clause_case_condition","standardCode":"CASE-P0-2025-001","clauseNo":"CASE-2025-001"}]}',
   'MEDIUM', 0.740000, 'CACHE', 1, 'system', NOW()),
  ('cache_p0_cert_qa_z004001', 'P0:CERT_QA:COIL:Z004001:CONFLICT', 'CERT_QA', 'COIL', 'Z004001',
   'p0-cache-v1', 'p0-cert-z004001',
   '{"question":"这卷为什么不能出正式质保书？","answer":"Z004001 当前最终判定为 STANDARD_CONFLICT。两个同优先级客户协议对 Rm 下限要求不同，冲突尚未裁决，因此不能生成正式质保书，只能展示非最终预览和冲突原因。","finalState":"BLOCKED","confidence":{"label":"LOW","score":0.34},"degradation":{"source":"CACHE","reason":"P0预生成质保书问答缓存"},"citations":["p0_clause_c1_rm","p0_clause_c2_rm"],"conflictId":"scf_p0_001"}',
   '{"citations":[{"clauseId":"p0_clause_c1_rm","standardCode":"协议C2025-088-v1","clauseNo":"3.1"},{"clauseId":"p0_clause_c2_rm","standardCode":"协议C2025-088-v2","clauseNo":"3.1"}],"conflictId":"scf_p0_001"}',
   'LOW', 0.340000, 'CACHE', 1, 'system', NOW());
