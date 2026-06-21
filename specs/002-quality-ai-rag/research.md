# Research: 质量判定解释、让步与标准 RAG 系统（决赛 V2.0）

**Feature**: 002-quality-ai-rag  
**Date**: 2026-06-20  
**Base**: 001-quality-judgment-concession（已实现传统闭环）

---

## 决策记录

### D-020：LLM 接入方式

**Decision**: 后端 `LlmClient` 调用 **OpenAI 兼容 Chat Completions API**（`/v1/chat/completions`），配置驱动。

**Rationale**:
- 决赛要求 API Key 不暴露前端；服务端代理是唯一合规方案。
- OpenAI 兼容格式被 DashScope、DeepSeek、智谱、本地 vLLM 等广泛支持，便于评委环境切换。
- Spring Boot 2.7 已有 `RestTemplate`/`WebClient`，无需引入 Spring AI（避免新框架学习成本与 JDK8 兼容风险）。

**配置示例**（application.yml，密钥走环境变量）:
```yaml
app:
  ai:
    enabled: true
    base-url: ${AI_BASE_URL:https://api.openai.com/v1}
    api-key: ${AI_API_KEY:}
    model: ${AI_MODEL:gpt-4o-mini}
    timeout-ms: 5000
    max-retries-on-429: 1
    circuit-breaker-threshold: 3
    circuit-breaker-open-ms: 60000
```

**Alternatives considered**:
- Spring AI 1.x：需 Spring Boot 3+，与当前 2.7 栈冲突。
- 前端直连 LLM：违反 FR-018，否决。
- 内置本地小模型：部署复杂，作为可选 `base-url` 指向本地服务，非默认。

---

### D-021：RAG 知识库与检索

**Decision**: **MySQL 段落表 + FULLTEXT 索引 + 应用层 BM25 重排**，LLM 仅基于召回段落生成答案。

**Rationale**:
- 宪法禁止过度设计：不引入 Milvus/Pinecone/ES 等额外组件，Demo 一键启动。
- MySQL 5.7 支持 InnoDB FULLTEXT（ngram 需配置），对中文标准文档可配合二元分词或预索引关键词。
- 段落级存储（`chunk_text` + `standard_id` + `section_ref`）满足「点击溯源至原文段落」（FR-010/FR-020）。
- 无召回时直接返回拒答模板，从架构上阻断幻觉。

**入库流程**:
1. 上传 PDF/TXT/MD → Tika/PDFBox 提取纯文本
2. 按标题/条款号分段（正则 + 固定窗口 500 字 fallback）
3. 写入 `qc_standard_document` + `qc_standard_document_chunk`
4. 关联已有 `qc_quality_standard` 元数据

**Alternatives considered**:
- 纯 LLM 长上下文塞全文：成本高、溯源难、易幻觉。
- Redis Vector：需 embedding 服务 + 额外运维。
- Elasticsearch：新增组件，违反 Demo 简洁原则。

---

### D-022：AI 判定解释双轨架构

**Decision**: **规则基线轨（确定性）+ AI 增强轨（自然语言 + 置信度）**，AI 失败自动降级基线轨。

**Rationale**:
- 001 已有 `JudgmentEngine` + `JudgmentEvidence` 快照，是 SC-002「99% 一致率」的权威来源。
- AI 不得修改 `judgmentType`（防注入 FR-019）；只解释「为什么是这个结论」。
- 降级时用户仍看到完整结构化解释，满足 FR-016 与 8 分钟演示不中断。
- 评测时可对比 AI 轨 vs 基线轨（FR-021）。

**置信度启发式**（不依赖 LLM 自报）:
| 条件 | 置信度 |
|------|--------|
| 无冲突、无 StandardGap、偏差远离边界 | HIGH |
| 存在 StandardGap 或偏差在合格限 5% 以内 | MEDIUM |
| 存在未裁定标准冲突 | LOW + manualReviewRequired |

**Alternatives considered**:
- 纯 LLM 判定：不可审计，与规则引擎一致性无法保证。
- 纯模板解释：无 AI 分，不满足 FR-011。

---

### D-023：让步风险智能体

**Decision**: **轻量 ReAct 式 Agent**（Java 编排，≤3 轮），工具为内部 Service 方法。

**Rationale**:
- 赛题要求从用途、偏差、历史投诉、替代资源评估，需多数据源聚合，单轮 Prompt 不足。
- 不引入 LangChain4j 等重型框架；3 个工具 + 1 轮综合足够 Demo。
- 输出固定 JSON Schema（riskLevel、suggestedConditions、citations），便于前端展示与审计。

**降级**: LLM 不可用时，`ConcessionRuleFallback` 基于偏差百分比 + 历史让步率输出 checklist。

**Alternatives considered**:
- 纯规则评分卡：无「智能体」叙事，赛题分低。
- 全自动 MCP 外部工具：过度设计。

---

### D-024：标准冲突检测

**Decision**: **判定引擎 Step 1.5 旁路检测** + 独立冲突管理页。

**Rationale**:
- 001 已有多标准 AND 逻辑；冲突是「限值不一致」而非「不满足」。
- 写入 `qc_standard_conflict` 后，AI 模块读取冲突状态决定拒答/低置信。
- 5 个预置冲突样例在 seed 脚本中构造（客协上限 400 vs 企标 410 等）。

**裁定流程**: 质量工程师在冲突页选择「以客协为准 / 以企标为准 / 待客户确认」，记录 `resolution_note`。

---

### D-025：AI 调用审计

**Decision**: 同步写入 `qc_ai_call_audit_log`，看板 SQL 聚合 + Redis 缓存。

**Rationale**:
- 决赛硬门禁要求展示 Token、耗时、模型、Prompt 版本、调用来源。
- 同步写库保证「回放任意单次调用链」强一致；高并发非本场景瓶颈。
- 输入输出存**脱敏摘要**（MaskUtils），不存完整 Prompt 中的客户隐私。

**Alternatives considered**:
- 仅文件日志：无法前端看板回放。
- 异步 MQ：增加组件，无必要。

---

### D-026：Prompt 版本管理

**Decision**: YAML 文件定义 + DB 表记录激活版本号。

**Rationale**:
- 版本化 Prompt 是 AI 工程治理评分项；YAML 便于 Git diff 与 Agent 报告引用。
- 运行时切换版本用于演示「Prompt 版本对比」，切换后新调用写新版本号。

**目录结构**:
```text
resources/prompts/
├── v1.0.0/
│   ├── rag-qa.yaml
│   ├── judgment-explain.yaml
│   ├── concession-agent.yaml
│   └── cert-summary.yaml
└── v1.1.0/
    └── ...
```

---

### D-027：降级与熔断

**Decision**: **三层防护**：单次超时 → 429 重试 → 连续失败熔断。

**Implementation**:
- `LlmClient.call()` 使用 `ExecutorService` + `Future.get(timeout)`
- 429：`Retry-After` 或固定 1s 后重试 1 次
- 熔断：`AtomicInteger` 失败计数，≥3 次打开 60s，期间 `AiFallbackService` 接管
- 所有降级写审计 `degraded=true`, `errorType=TIMEOUT|RATE_LIMIT|UNAVAILABLE`

**Alternatives considered**:
- Resilience4j 依赖：可选引入，自研足够满足 Demo。

---

### D-028：防 Prompt 注入

**Decision**: **三层防御**：固定 System Prompt + 内容隔离标签 + 注入模式拦截。

**Rationale**:
- 赛题明确要求上传文档/对话中的恶意指令不能覆盖系统规则。
- 拦截模式列表：`ignore previous`、`disregard instructions`、`you are now` 等（可配置 regex）。
- 命中拦截：返回安全拒答 + 审计 `errorType=INJECTION_BLOCKED`；评测集 5 条验证。

**关键原则**: 用户/文档内容** never **进入 system role。

---

### D-029：评测集设计

**Decision**: JSON 文件 + `EvaluationRunner` 批量执行，结果写 `qc_evaluation_run`。

**样例分布**（FR-021）:
| 类别 | 数量 | 验证点 |
|------|------|--------|
| 正常 | 10 | 结论准确率、引用命中 |
| 边界/异常 | 10 | 低置信、StandardGap |
| 低置信/拒答 | 5 | 必须拒答或标 LOW |
| 注入/安全 | 5 | 不执行恶意指令 |

**指标**: 规则通过率、AI 准确率、引用命中率、置信度校准、平均响应时间、人工复核命中率。

---

### D-030：预置演示数据

**Decision**: Flyway/SQL seed 脚本 `V2__ai_demo_data.sql` 一次性导入。

**内容**:
- 20 份标准文档（国标 8 + 企标 7 + 客协 5）及 chunk
- 100 条检验记录（覆盖四种结论）
- 5 条标准冲突记录（PENDING）
- 4 条「一键演示」快捷入口配置（dashboard demo links）
- 30+ 评测样例 JSON

**Alternatives considered**:
- 运行时手工导入：违反 FR-003「启动即可演示」。

---

## 风险与缓解

| 风险 | 影响 | 缓解 |
|------|------|------|
| 评委现场无 API Key | AI 功能全降级 | `app.ai.enabled=false` 时纯规则模式可完整演示业务；README 说明 |
| FULLTEXT 中文召回弱 | RAG 命中率下降 | 预置 chunk 含指标代码关键词；BM25 重排 |
| LLM 输出格式不稳定 | 前端解析失败 | JSON mode / 输出解析 retry / fallback 基线 |
| 8 分钟超时 | 演示中断 | 预跑缓存热门 RAG 结果；演示账号固定样例 |

---

## 002 vs 001 复用矩阵

| 001 模块 | 002 关系 |
|---------|---------|
| JudgmentEngine | 基线，不修改结论逻辑 |
| JudgmentService.buildExplanation | 降级数据源 |
| StandardService | RAG 元数据来源 |
| StandardGap | 置信度 MEDIUM 触发 |
| ConcessionService | Agent 工具调用 |
| AuditLogAspect | 业务审计延续；AI 审计独立表 |
| DashboardService | 增加 aiRiskAlertCount |
