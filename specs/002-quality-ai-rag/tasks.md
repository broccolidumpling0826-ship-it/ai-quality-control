---
description: "Task list for 质量判定解释、让步与标准 RAG 系统（决赛 V2.0）"
---

# Tasks: 质量判定解释、让步与标准 RAG 系统（决赛 V2.0）

**Input**: Design documents from `specs/002-quality-ai-rag/`  
**Base**: `001-quality-judgment-concession` 已实现（传统业务闭环）  
**Prerequisites**: plan.md ✅ | spec.md ✅ | research.md ✅ | data-model.md ✅ | quickstart.md ✅  
**Naming**: 延续 001 约定 — `QcXxx` 实体、`XxxService`/`XxxServiceImpl`（无 `I` 前缀）、`/api/v1/` 路径

**Tests**: 规格未要求 TDD；验证以 quickstart.md 场景为准（Polish 阶段统一执行）

## Format: `[ID] [P?] [Story] Description — file path`

- **[P]**: 可并行（不同文件、无未完成依赖）
- **[Story]**: 映射 spec.md 用户故事 US1–US8

---

## Phase 1: Setup（002 增量初始化）

**Purpose**: AI 模块依赖、配置、目录与数据库迁移脚本

- [X] T001 在 pom.xml 添加 AI 相关依赖（Apache Tika、HTTP Client） — `backend/pom.xml`
- [X] T002 [P] 在 application.yml 添加 app.ai 配置块（base-url/api-key/model/timeout/enabled） — `backend/src/main/resources/application.yml`
- [X] T003 [P] 在 application-dev.yml 添加 AI 开发配置与环境变量占位 — `backend/src/main/resources/application-dev.yml`
- [X] T004 创建 002 DDL 迁移脚本（10 张新表 + qc_quality_cert_data.ai_summary_text + FULLTEXT 索引） — `backend/scripts/V2__ai_schema.sql`
- [X] T005 [P] 创建 Prompt 资源目录与 v1.0.0 占位 YAML — `backend/src/main/resources/prompts/v1.0.0/`
- [X] T006 [P] 创建评测样例资源目录 — `backend/src/main/resources/evaluation/`
- [X] T007 [P] 创建标准文档样例目录 — `backend/scripts/seed-documents/`

---

## Phase 2: Foundational（AI 基础设施 — 阻塞所有用户故事）

**Purpose**: LlmClient、审计、Prompt、降级、脱敏；所有 AI 功能依赖本阶段

**⚠️ CRITICAL**: Phase 2 完成前不得开始 US2–US8 的 LLM 调用实现

- [X] T008 [P] 新增 AI 相关枚举（AiCallSource/AiErrorType/ConfidenceLevel/ConflictStatus/EvaluationCategory/EvaluationRunStatus） — `backend/src/main/java/com/jhict/quality/enums/`
- [X] T009 [P] 创建 AiProperties 配置类 — `backend/src/main/java/com/jhict/quality/ai/config/AiProperties.java`
- [X] T010 [P] 创建 LlmRequest/LlmResponse DTO — `backend/src/main/java/com/jhict/quality/ai/client/`
- [X] T011 实现 LlmClient（5s 超时、429 重试 1 次、熔断计数） — `backend/src/main/java/com/jhict/quality/ai/client/LlmClient.java`
- [X] T012 [P] 实现 MaskUtils 敏感信息脱敏工具 — `backend/src/main/java/com/jhict/quality/common/util/MaskUtils.java`
- [X] T013 [P] 实现 PromptSanitizer（注入模式检测与拦截） — `backend/src/main/java/com/jhict/quality/ai/prompt/PromptSanitizer.java`
- [X] T014 实现 PromptRegistry（加载 YAML + 读 qc_ai_prompt_version 激活版本） — `backend/src/main/java/com/jhict/quality/ai/prompt/PromptRegistry.java`
- [X] T015 [P] 创建 QcAiPromptVersion entity + Mapper — `backend/src/main/java/com/jhict/quality/entity/QcAiPromptVersion.java`
- [X] T016 [P] 创建 QcAiCallAuditLog entity + Mapper — `backend/src/main/java/com/jhict/quality/entity/QcAiCallAuditLog.java`
- [X] T017 实现 AiAuditService（同步写审计、脱敏摘要、关联 traceId） — `backend/src/main/java/com/jhict/quality/ai/audit/AiAuditService.java`
- [X] T018 实现 AiFallbackService（超时/429/不可用降级策略矩阵 D-027） — `backend/src/main/java/com/jhict/quality/ai/fallback/AiFallbackService.java`
- [X] T019 编写 v1.0.0 Prompt YAML（rag-qa/judgment-explain/concession-agent/cert-summary） — `backend/src/main/resources/prompts/v1.0.0/`
- [X] T020 编写 Prompt 版本与字典 seed 数据 — `backend/scripts/V2__ai_prompt_seed.sql`
- [X] T021 在 LlmClient 集成 AiAuditService 与 AiFallbackService 回调 — `backend/src/main/java/com/jhict/quality/ai/client/LlmClient.java`
- [X] T022 [P] 新增 AI 相关 VO/DTO 基类（CitationVO/AiExplanationVO 等） — `backend/src/main/java/com/jhict/quality/vo/`
- [X] T023 [P] 创建 ai 包 Spring 配置（RestTemplate Bean、AiProperties Enable） — `backend/src/main/java/com/jhict/quality/ai/config/AiConfig.java`

**Checkpoint**: LlmClient 可 mock 调用、审计可写库、降级路径可触发

---

## Phase 3: User Story 1 — 检验录入与自动判定闭环（Priority: P1）🎯 MVP 底座

**Goal**: 001 主流程可演示 + 002 预置数据（100 条检验、四场景 Demo 入口）  
**Independent Test**: quickstart.md 场景 1 步骤 1–2（无 AI Key 亦可）

- [X] T024 [US1] 编写 V2 演示数据 seed（100 条检验记录、四场景 DEMO-* 批次） — `backend/scripts/V2__ai_demo_data.sql`
- [X] T025 [US1] 在 DashboardService 增加 demoScenarios 快捷入口 API — `backend/src/main/java/com/jhict/quality/service/impl/DashboardServiceImpl.java`
- [X] T026 [US1] 扩展 DashboardSummaryVO 增加 demoLinks 字段 — `backend/src/main/java/com/jhict/quality/vo/DashboardSummaryVO.java`
- [X] T027 [US1] 更新 DashboardController 返回 Demo 入口 — `backend/src/main/java/com/jhict/quality/controller/DashboardController.java`
- [X] T028 [US1] 质量工作台增加四场景 Demo 快捷卡片 — `frontend/src/views/dashboard/index.vue`
- [X] T029 [P] [US1] 扩展 frontend/src/api/dashboard.ts 增加 demoLinks 类型 — `frontend/src/api/dashboard.ts`
- [X] T030 [US1] 验证检验录入→自动判定→规则解释链路（001 回归） — `specs/002-quality-ai-rag/quickstart.md` 场景 1

**Checkpoint**: 登录后可一键进入合格/不合格/可让步/冲突演示批次（检验与规则判定）

---

## Phase 4: User Story 2 — 标准 RAG 检索与冲突检测（Priority: P1）

**Goal**: 自然语言检索标准条款并溯源；冲突标注与拒答  
**Independent Test**: quickstart.md 场景 2 + 场景 4

### 数据层

- [X] T031 [P] [US2] 创建 QcStandardDocument entity + Mapper — `backend/src/main/java/com/jhict/quality/entity/QcStandardDocument.java`
- [X] T032 [P] [US2] 创建 QcStandardDocumentChunk entity + Mapper（FULLTEXT 查询方法） — `backend/src/main/java/com/jhict/quality/entity/QcStandardDocumentChunk.java`
- [X] T033 [P] [US2] 创建 QcStandardConflict entity + Mapper — `backend/src/main/java/com/jhict/quality/entity/QcStandardConflict.java`

### RAG 后端

- [X] T034 [US2] 实现 DocumentIngestService（Tika 解析 PDF/TXT/MD + 分段入库） — `backend/src/main/java/com/jhict/quality/ai/rag/DocumentIngestService.java`
- [X] T035 [US2] 实现 RagRetriever（FULLTEXT 召回 + BM25 重排） — `backend/src/main/java/com/jhict/quality/ai/rag/RagRetriever.java`
- [X] T036 [US2] 实现 RagService（检索→LlmClient→强制 citations / 无命中拒答） — `backend/src/main/java/com/jhict/quality/ai/rag/RagService.java`
- [X] T037 [US2] 实现 StandardConflictDetector（多标准同指标限值比对） — `backend/src/main/java/com/jhict/quality/ai/conflict/StandardConflictDetector.java`
- [X] T038 [US2] 在 JudgmentEngine 或 JudgmentService 集成冲突旁路写入 — `backend/src/main/java/com/jhict/quality/service/impl/JudgmentServiceImpl.java`
- [X] T039 [US2] 实现 StandardConflictService + StandardConflictServiceImpl — `backend/src/main/java/com/jhict/quality/service/impl/StandardConflictServiceImpl.java`
- [X] T040 [US2] 实现 RagController（POST /api/v1/rag/query、POST /api/v1/rag/ingest） — `backend/src/main/java/com/jhict/quality/controller/RagController.java`
- [X] T041 [US2] 实现 StandardConflictController（page/get/resolve） — `backend/src/main/java/com/jhict/quality/controller/StandardConflictController.java`

### RAG 前端

- [X] T042 [P] [US2] 创建 frontend/src/api/rag.ts — `frontend/src/api/rag.ts`
- [X] T043 [P] [US2] 创建 frontend/src/api/standard-conflict.ts — `frontend/src/api/standard-conflict.ts`
- [X] T044 [US2] 实现标准 RAG 检索页（问答 + 引用高亮侧栏） — `frontend/src/views/standard-rag/index.vue`
- [X] T045 [US2] 实现标准冲突检测页（对比 + 裁定） — `frontend/src/views/standard-lib/conflicts.vue`

### 种子数据

- [X] T046 [US2] 入库 20 份标准/协议文档并生成 chunk — `backend/scripts/V2__ai_demo_data.sql`
- [X] T047 [US2] 预置 5 条标准冲突样例（demo_flag=1） — `backend/scripts/V2__ai_demo_data.sql`

**Checkpoint**: RAG 问答可溯源；冲突 5 条可检出；超范围问题拒答

---

## Phase 5: User Story 3 — AI 判定解释与置信度展示（Priority: P1）

**Goal**: 判定完成后 AI 自然语言解释 + 置信度 + 降级基线  
**Independent Test**: quickstart.md 场景 3（四场景 DEMO-*）

- [X] T048 [P] [US3] 创建 QcAiJudgmentExplanation entity + Mapper — `backend/src/main/java/com/jhict/quality/entity/QcAiJudgmentExplanation.java`
- [X] T049 [US3] 实现 ConfidenceCalculator（HIGH/MEDIUM/LOW 启发式 D-022） — `backend/src/main/java/com/jhict/quality/ai/judgment/ConfidenceCalculator.java`
- [X] T050 [US3] 实现 AiJudgmentExplainService（基线 JSON + RAG + LlmClient 双轨） — `backend/src/main/java/com/jhict/quality/service/impl/AiJudgmentExplainServiceImpl.java`
- [X] T051 [US3] 定义 AiJudgmentExplainService 接口 — `backend/src/main/java/com/jhict/quality/service/api/AiJudgmentExplainService.java`
- [X] T052 [US3] 实现 AiJudgmentController（GET /api/v1/judgments/{id}/ai-explanation、GET /demo/{code}/ai-explanation） — `backend/src/main/java/com/jhict/quality/controller/AiJudgmentController.java`
- [X] T053 [P] [US3] 创建 AiJudgmentExplanationVO — `backend/src/main/java/com/jhict/quality/vo/AiJudgmentExplanationVO.java`
- [X] T054 [P] [US3] 创建 frontend/src/api/ai-judgment.ts — `frontend/src/api/ai-judgment.ts`
- [X] T055 [US3] 实现 AI 判定解释页（置信度徽章、引用跳转、需复核提示、degraded 标记） — `frontend/src/views/judgment/ai-explanation.vue`
- [X] T056 [US3] 在 judgment/explanation.vue 增加「AI 解释」Tab 入口 — `frontend/src/views/judgment/explanation.vue`

**Checkpoint**: 四场景均可展示 AI 解释链；冲突场景 LOW + 需复核；LLM 失败显示基线解释

---

## Phase 6: User Story 4 — 让步风险智能评估（Priority: P2）

**Goal**: 可让步批次 AI 风险评估报告  
**Independent Test**: quickstart.md 场景 1 步骤 4

- [ ] T057 [P] [US4] 创建 QcConcessionAssessment entity + Mapper — `backend/src/main/java/com/jhict/quality/entity/QcConcessionAssessment.java`
- [ ] T058 [US4] 实现 ConcessionAgentToolKit（4 个内部工具方法） — `backend/src/main/java/com/jhict/quality/ai/agent/ConcessionAgentToolKit.java`
- [ ] T059 [US4] 实现 ConcessionAgent（≤3 轮 Agent 循环 + 结构化输出） — `backend/src/main/java/com/jhict/quality/ai/agent/ConcessionAgent.java`
- [ ] T060 [US4] 实现 AiConcessionService + AiConcessionServiceImpl — `backend/src/main/java/com/jhict/quality/service/impl/AiConcessionServiceImpl.java`
- [ ] T061 [US4] 实现 AiConcessionController（POST /api/v1/ai/concession/assess） — `backend/src/main/java/com/jhict/quality/controller/AiConcessionController.java`
- [ ] T062 [P] [US4] 创建 ConcessionAssessmentVO — `backend/src/main/java/com/jhict/quality/vo/ConcessionAssessmentVO.java`
- [ ] T063 [P] [US4] 创建 frontend/src/api/ai-concession.ts — `frontend/src/api/ai-concession.ts`
- [ ] T064 [US4] 实现 AI 让步评估页 — `frontend/src/views/concession/ai-assessment.vue`
- [ ] T065 [US4] 在 concession/detail.vue 增加「AI 评估」入口 — `frontend/src/views/concession/detail.vue`

**Checkpoint**: DEMO-CONCESSION-001 可生成风险等级与建议条件；429 时降级 checklist

---

## Phase 7: User Story 5 — 复检/改判建议与流程管理（Priority: P2）

**Goal**: 需复检/存疑结论的 AI 建议（不替代审批）  
**Independent Test**: 对 NEED_REINSPECTION 批次查看建议并完成复检流程

- [ ] T066 [P] [US5] 创建 QcAiSuggestion entity + Mapper — `backend/src/main/java/com/jhict/quality/entity/QcAiSuggestion.java`
- [ ] T067 [US5] 实现 ReinspectionAdvisor（复检/改判建议生成） — `backend/src/main/java/com/jhict/quality/ai/agent/ReinspectionAdvisor.java`
- [ ] T068 [US5] 实现 AiSuggestionService + AiSuggestionServiceImpl — `backend/src/main/java/com/jhict/quality/service/impl/AiSuggestionServiceImpl.java`
- [ ] T069 [US5] 在 ReinspectionController 增加 GET /{judgmentId}/ai-suggestion — `backend/src/main/java/com/jhict/quality/controller/ReinspectionController.java`
- [ ] T070 [US5] 在 RejudgmentController 增加 GET /{judgmentId}/ai-suggestion — `backend/src/main/java/com/jhict/quality/controller/RejudgmentController.java`
- [ ] T071 [P] [US5] 扩展 frontend/src/api/reinspection.ts 与 re-judgment.ts — `frontend/src/api/`
- [ ] T072 [US5] 在 reinspection/index.vue 增加 AI 建议侧栏 — `frontend/src/views/reinspection/index.vue`
- [ ] T073 [US5] 在 re-judgment/index.vue 增加 AI 建议侧栏 — `frontend/src/views/re-judgment/index.vue`

**Checkpoint**: 需复检批次展示建议与置信度；复检/改判流程仍走 001 审批链

---

## Phase 8: User Story 6 — 质保书数据汇总与 AI 辅助说明（Priority: P3）

**Goal**: 批次汇总 + AI 生成客户向指标说明  
**Independent Test**: quickstart.md 场景 1 步骤 5

- [ ] T074 [US6] 扩展 QcQualityCertData 实体与 Mapper 增加 aiSummaryText — `backend/src/main/java/com/jhict/quality/entity/QcQualityCertData.java`
- [ ] T075 [US6] 在 CertDataServiceImpl 增加 generateAiSummary（汇总数据一致性校验 + LlmClient） — `backend/src/main/java/com/jhict/quality/service/impl/CertDataServiceImpl.java`
- [ ] T076 [US6] 在 CertDataController 增加 POST /{batchNo}/ai-summary — `backend/src/main/java/com/jhict/quality/controller/CertDataController.java`
- [ ] T077 [P] [US6] 扩展 frontend/src/api/cert-data.ts — `frontend/src/api/cert-data.ts`
- [ ] T078 [US6] 在 cert-data/index.vue 增加「生成 AI 说明」按钮与展示 — `frontend/src/views/cert-data/index.vue`

**Checkpoint**: BATCH-DEMO-001 可生成与汇总数据一致的 AI 说明

---

## Phase 9: User Story 7 — AI 调用审计、评测与工程治理（Priority: P2）

**Goal**: 审计看板、调用链回放、Prompt 切换、30+ 评测集一键评分  
**Independent Test**: quickstart.md 场景 5 + 场景 6 + 场景 7 + 场景 8

- [ ] T079 [P] [US7] 创建 QcEvaluationSample entity + Mapper — `backend/src/main/java/com/jhict/quality/entity/QcEvaluationSample.java`
- [ ] T080 [P] [US7] 创建 QcEvaluationRun entity + Mapper — `backend/src/main/java/com/jhict/quality/entity/QcEvaluationRun.java`
- [ ] T081 [US7] 实现 EvaluationScorer（规则通过率/准确率/引用命中率/置信度校准） — `backend/src/main/java/com/jhict/quality/ai/evaluation/EvaluationScorer.java`
- [ ] T082 [US7] 实现 EvaluationRunner（批量执行 + 写 qc_evaluation_run） — `backend/src/main/java/com/jhict/quality/ai/evaluation/EvaluationRunner.java`
- [ ] T083 [US7] 实现 AiAuditDashboardService（聚合统计 + Redis 缓存 60s） — `backend/src/main/java/com/jhict/quality/service/impl/AiAuditDashboardServiceImpl.java`
- [ ] T084 [US7] 实现 AiAuditController（dashboard/logs/replay/prompts/activate） — `backend/src/main/java/com/jhict/quality/controller/AiAuditController.java`
- [ ] T085 [US7] 实现 EvaluationController（POST /run、GET /runs/{runNo}） — `backend/src/main/java/com/jhict/quality/controller/EvaluationController.java`
- [ ] T086 [US7] 编写 30+ 评测样例 JSON（10 正常/10 边界/5 拒答/5 注入） — `backend/src/main/resources/evaluation/samples.json`
- [ ] T087 [US7] 编写评测样例入库 seed — `backend/scripts/V2__ai_evaluation_seed.sql`
- [ ] T088 [US7] 在 LlmClient 增加 AI_MOCK_FAILURE 测试开关（TIMEOUT/RATE_LIMIT/UNAVAILABLE） — `backend/src/main/java/com/jhict/quality/ai/client/LlmClient.java`
- [ ] T089 [P] [US7] 创建 frontend/src/api/ai-audit.ts — `frontend/src/api/ai-audit.ts`
- [ ] T090 [P] [US7] 创建 frontend/src/api/evaluation.ts — `frontend/src/api/evaluation.ts`
- [ ] T091 [US7] 实现 AI 调用审计看板页（汇总 + 明细 + 回放抽屉） — `frontend/src/views/ai-audit/index.vue`
- [ ] T092 [US7] 实现 AI 评测中心页（一键运行 + 报告图表） — `frontend/src/views/ai-evaluation/index.vue`
- [ ] T093 [US7] 实现 Prompt 版本切换 UI（管理员） — `frontend/src/views/ai-audit/prompt-versions.vue`

**Checkpoint**: 审计看板可回放；评测 30 条 ≤5min；三故障注入可验证降级

---

## Phase 10: User Story 8 — 质量工作台与权限审计（Priority: P3）

**Goal**: AI 风险预警、新菜单 RBAC、前端无密钥泄露  
**Independent Test**: quickstart.md 演示账号权限 + F12 网络检查

- [ ] T094 [US8] 扩展 DashboardSummaryVO 增加 aiRiskAlertCount — `backend/src/main/java/com/jhict/quality/vo/DashboardSummaryVO.java`
- [ ] T095 [US8] 在 DashboardServiceImpl 聚合 AI 风险预警（高险让步/未裁定冲突/LOW 置信） — `backend/src/main/java/com/jhict/quality/service/impl/DashboardServiceImpl.java`
- [ ] T096 [US8] 编写 sys_menu seed（standard-rag/conflicts/ai-explanation/ai-assessment/ai-audit/ai-evaluation） — `backend/scripts/V2__ai_menu_seed.sql`
- [ ] T097 [US8] 配置新菜单角色权限（质量工程师/销售/admin） — `backend/scripts/V2__ai_menu_seed.sql`
- [ ] T098 [US8] 更新动态路由加载（确保新菜单可见） — `frontend/src/router/dynamic.ts`
- [ ] T099 [US8] 工作台展示 AI 风险预警卡片 — `frontend/src/views/dashboard/index.vue`
- [ ] T100 [US8] 确认 frontend/src/utils/request.ts 不含 AI Key 或模型凭证 — `frontend/src/utils/request.ts`
- [ ] T101 [US8] 确认 AiAuditService 审计展示字段经 MaskUtils 脱敏 — `backend/src/main/java/com/jhict/quality/ai/audit/AiAuditService.java`

**Checkpoint**: 三角色菜单正确；F12 无 API Key；审计日志无敏感明文

---

## Phase 11: Polish & Cross-Cutting（决赛交付）

**Purpose**: 文档、README、全链路演示验证

- [ ] T102 [P] 更新 README.md（运行方式、演示账号、AI 模块、模型配置、已知限制） — `README.md`
- [ ] T103 [P] 编写 AI 能力说明文档 — `docs/AI-CAPABILITIES.md`
- [ ] T104 [P] 编写 Agent 使用深度报告模板 — `docs/AGENT-USAGE-REPORT.md`
- [ ] T105 修正 plan.md 命名说明（Service 接口延续 001，无 I 前缀） — `specs/002-quality-ai-rag/plan.md`
- [ ] T106 按 quickstart.md 执行场景 1–8 全链路验证并记录结果 — `specs/002-quality-ai-rag/test-case/ai-demo-results.md`
- [ ] T107 [P] 补充 contracts/ API 合同文档（6 组） — `specs/002-quality-ai-rag/contracts/`
- [ ] T108 更新 CLAUDE.md 指向 002 plan — `CLAUDE.md`

---

## Dependencies & Execution Order

### Phase Dependencies

```text
Phase 1 Setup
    ↓
Phase 2 Foundational（阻塞所有 AI 用户故事）
    ↓
Phase 3 US1（可与 Phase 4 数据准备并行，但 Demo 依赖 seed）
    ↓
Phase 4 US2（RAG + 冲突）──→ Phase 5 US3（AI 解释依赖 RAG 引用）
    ↓
Phase 6 US4 / Phase 7 US5（可并行，均依赖 US3 判定数据）
    ↓
Phase 8 US6（依赖 cert 汇总）
    ↓
Phase 9 US7（依赖各 AI 模块有调用记录）
    ↓
Phase 10 US8（菜单 + 预警，依赖前述页面）
    ↓
Phase 11 Polish
```

### User Story Dependencies

| Story | 依赖 | 可独立测试 |
|-------|------|-----------|
| US1 | Phase 2 | ✅ 规则判定 + Demo 入口 |
| US2 | Phase 2 | ✅ RAG 问答 + 冲突页 |
| US3 | US2（引用）、US1（判定数据） | ✅ 四场景 AI 解释 |
| US4 | US1（可让步数据） | ✅ 让步评估报告 |
| US5 | US1 | ✅ 建议侧栏 + 001 流程 |
| US6 | US1 | ✅ 质保书 AI 说明 |
| US7 | US2–US6 至少各 1 次调用 | ✅ 审计 + 评测 |
| US8 | US2–US7 页面就绪 | ✅ 权限 + 安全 |

### Parallel Opportunities

**Phase 2**（可同时进行）:
- T008 枚举 + T009 AiProperties + T010 DTO + T012 MaskUtils + T013 PromptSanitizer + T015/T016 Entity

**Phase 4 US2**（可同时进行）:
- T031/T032/T033 三个 Entity + T042/T043 前端 API

**Phase 6 + Phase 7**（不同开发者）:
- US4 ConcessionAgent 与 US5 ReinspectionAdvisor 并行

**Phase 9 前端**:
- T089 ai-audit.ts + T090 evaluation.ts + T091/T092 页面可拆分

---

## Parallel Example: User Story 2

```bash
# 并行启动 US2 数据层：
T031 QcStandardDocument entity
T032 QcStandardDocumentChunk entity
T033 QcStandardConflict entity

# 并行启动 US2 前端 API：
T042 frontend/src/api/rag.ts
T043 frontend/src/api/standard-conflict.ts
```

---

## Implementation Strategy

### MVP First（推荐）

1. Phase 1 + Phase 2 → AI 基础设施就绪
2. Phase 3 US1 → Demo 数据 + 规则闭环可演示
3. Phase 4 US2 + Phase 5 US3 → **核心 AI 能力**（RAG + AI 解释）
4. **STOP & VALIDATE** → quickstart 场景 1–3
5. 依次 US4 → US7 → US8 → Polish

### 决赛演示最小集（8 分钟）

- 必须完成：Phase 1–5 + Phase 6 + Phase 8 US6 + Phase 9 审计/降级 + Phase 10 菜单
- 可后补：US5 建议侧栏、US7 全量评测 UI、加分项 FR-023–025

### Incremental Delivery

| 增量 | 包含 Phase | 演示能力 |
|------|-----------|---------|
| 增量 1 | 1–3 | 传统闭环 + Demo 入口 |
| 增量 2 | 4–5 | RAG + AI 判定解释 |
| 增量 3 | 6–8 | 让步 + 质保书 AI |
| 增量 4 | 9–11 | 审计 + 评测 + 交付文档 |

---

## Notes

- 所有 Service 接口命名延续 001：`XxxService` + `XxxServiceImpl`，包路径 `service/api/` + `service/impl/`
- AI 专属逻辑放 `ai/` 包；业务编排放 `service/impl/`；Controller 保持薄层
- 每个 Phase Checkpoint 通过后再进入下一 Phase，避免 AI 模块半成品堆积
- 无 `AI_API_KEY` 时 `app.ai.enabled=false`，全链路降级可演示
