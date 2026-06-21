# Business Flowcharts

Status: Draft
Last updated: 2026-06-21
Change: `rag-explained-judgment-concession`

## 1. Purpose

This document is the shared process reference for frontend and backend implementation. It prevents the UI flow, API flow, business rules, AI degradation behavior, and audit behavior from drifting apart.

Mandatory implementation references:

- Frontend implementation MUST follow [frontend/DESIGN.md](../../../frontend/DESIGN.md).
- Backend implementation MUST follow [backend/AGENTS.md](../../../backend/AGENTS.md).
- Backend cross-Service access MUST go through `service/api` methods. A Service MUST NOT call another business domain's Mapper directly.
- Structured standards remain the only source of truth for deterministic judgment.
- RAG clauses are used for retrieval, citation, explanation, and review only.
- AI may explain, suggest, prefill, and warn. AI MUST NOT auto-create or auto-approve reinspection, rejudgment, concession approval, certificate release, or conflict裁决.

## 2. Legend

```text
FE  = Vue page/component following frontend/DESIGN.md
BE  = Spring Controller/Service following backend/AGENTS.md
DB  = MySQL authoritative business persistence
ES  = Elasticsearch retrieval/vector index
LLM = ModelGateway provider, for example an OpenAI-compatible API
AUD = Audit log or immutable AI assessment record
```

## 3. P0 End-To-End Demo Flow

This is the primary final competition flow.

```mermaid
flowchart TD
    A[FE: 检验录入页面提交炉号/卷号/样品类型/检验值] --> B[BE: InspectionService 创建检验记录和值]
    B --> C[BE: JudgmentEngine 匹配结构化标准]
    C --> D{BE: 是否存在阻断标准冲突}
    D -- 是 --> E[BE: 保存 STANDARD_CONFLICT 判定和 conflict 记录]
    E --> F[FE: 判定解释页显示冲突阻断和裁决入口]
    F --> G[FE/BE: 质量经理人工裁决]
    G --> H[BE: 重新触发规则判定并保留历史冲突判定]
    D -- 否 --> I[BE: 生成规则判定 QUALIFIED/UNQUALIFIED/NEED_REINSPECTION/CAN_CONCESSION]
    H --> J[BE: 生成判定依据快照]
    I --> J
    J --> K[BE: RAG 检索来源条款]
    K --> L[BE: 生成或降级 AI 判定解释]
    L --> M[DB/AUD: 持久化 AI assessment 和引用]
    M --> N[FE: 判定解释页展示结构化规则/AI解释/来源/置信度]
    N --> O{判定是否 CAN_CONCESSION}
    O -- 是 --> P[FE/BE: 让步风险评估]
    P --> Q[FE: 用户手动发起或放弃让步流程]
    O -- 否 --> R[FE: 展示对应操作入口]
    Q --> S[FE/BE: 质保书数据生成或质保书问答]
    R --> S
    S --> T[FE: 展示可出证/不可出证/预览/拒答原因]
```

Implementation notes:

- FE must show all conflict, low-confidence, and degradation states. It must not hide them behind a generic error.
- BE must persist the deterministic judgment and evidence before AI explanation is shown.
- Certificate Q&A must answer from certificate snapshot, inspection record, judgment evidence, and cited standard clauses. It must refuse when evidence is missing.

## 4. Inspection Entry And Deterministic Judgment Flow

```mermaid
flowchart TD
    A[FE: inspection/form.vue] --> B[POST /inspections]
    B --> C[BE: InspectionController 接收 QcInspectionRecordAddCmd]
    C --> D[BE: InspectionService 校验必填字段和检验值]
    D --> E[DB: 保存 qc_inspection_record]
    E --> F[DB: 保存 qc_inspection_value]
    F --> G[BE: 构建 JudgmentInput]
    G --> H[BE: JudgmentEngine.judge]
    H --> I[BE: 查询候选标准集]
    I --> J{同优先级是否冲突}
    J -- 是 --> K[BE: 返回 STANDARD_CONFLICT 输出]
    J -- 否 --> L[BE: 按客户协议 > 企标 > 国标选择结构化标准]
    L --> M[BE: 按指标上下限/让步限计算结论]
    K --> N[BE: JudgmentService 保存最终或冲突判定]
    M --> N
    N --> O[DB: 保存 qc_judgment_result/qc_judgment_evidence/standard_conflict]
    O --> P[FE: 显示提交成功和判定结果入口]
```

Backend boundaries:

- `InspectionService` owns inspection record/value persistence.
- `JudgmentService` owns judgment result/evidence persistence.
- If `InspectionService` needs to invalidate or query judgment data, it MUST call `JudgmentService`; it MUST NOT use `QcJudgmentResultMapper` directly.

Frontend boundaries:

- FE submits DTO fields only.
- FE does not calculate final judgment.
- FE may preview entered values but must label any local calculation as non-authoritative if added later.

## 5. Standard Candidate Matching And Conflict Detection Flow

```mermaid
flowchart TD
    A[BE: 输入 customerId/variety/grade/spec/testDate/indicators] --> B[BE: 查询所有匹配候选标准]
    B --> C[BE: 按优先级分组 CUSTOMER/ENTERPRISE/NATIONAL]
    C --> D[BE: 对每个指标比较同优先级候选规则]
    D --> E{同优先级限值/单位/口径/规格范围是否冲突}
    E -- 是 --> F[BE: 创建 blocking conflict]
    F --> G[BE: 输出 STANDARD_CONFLICT]
    E -- 否 --> H[BE: 选择最高优先级候选标准]
    H --> I[BE: 比较低优先级适用标准]
    I --> J{是否存在优先级可解差异}
    J -- 是 --> K[BE: 创建 priority-resolvable conflict warning]
    K --> L[BE: 继续使用最高优先级标准判定]
    J -- 否 --> L
    L --> M[BE: 输出 selectedStandard + suppressedStandards + warnings]
```

Conflict rules:

- Same-priority numeric differences for the same indicator and overlapping effective/spec range are blocking conflicts.
- Unit mismatch or indicator口径 mismatch is blocking unless a configured conversion/alias rule exists.
- Cross-priority differences are priority-resolvable but must be surfaced in explanation.
- Customer agreement wider than enterprise/national standard is priority-resolvable but must be risk-highlighted.

Data contract alignment:

- BE response should include `selectedStandard`, `candidateStandards`, `suppressedStandards`, `conflicts`, and `warnings`.
- FE must render selected, skipped, warning, and blocking states distinctly.

## 6. Standard Document Ingestion And RAG Retrieval Flow

### 6.1 Document Ingestion And Indexing Flow

```mermaid
flowchart TD
    A[FE: 标准库上传/登记 PDF、Office、Markdown 或文本文件] --> B[BE: StandardDocumentController]
    B --> C[BE: StandardDocumentService 保存文档元数据和原始文件信息]
    C --> D{文件类型}
    D -- PDF --> E[BE: Apache PDFBox 提取文本和页码]
    D -- Word/Excel --> F[BE: Apache POI 提取文本/表格行文本]
    D -- Markdown/Text --> G[BE: 直接读取文本]
    E --> H[BE: 保存 parseStatus、解析错误和引用锚点]
    F --> H
    G --> H
    H --> I[BE: ClauseChunker 按章节/条款/段落边界切片]
    I --> J{切片是否过长或边界不清}
    J -- 是 --> K[BE: 按段落/句子边界拆分，可选 jieba/spaCy 分句]
    J -- 否 --> L[BE: 保持条款标题和完整段落为一片]
    K --> M[BE: 生成 chunk metadata: documentId/页码/条款号/适用范围]
    L --> M
    M --> N[BE: ModelGateway.embed 对每个 chunk 向量化]
    N --> O{embedding 是否成功}
    O -- 否 --> P[DB: 标记 EMBEDDING_FAILED 并记录错误]
    O -- 是 --> Q[BE: VectorStoreGateway 写入 ES 文本、向量和来源字段]
    Q --> R{ES 写入是否成功}
    R -- 否 --> S[DB: 标记 INDEX_FAILED 并记录错误]
    R -- 是 --> T[DB: 标记 INDEXED，保存 clause 与 ES document key]
    T --> U[FE: 标准库显示解析/切片/向量化/索引状态]
    P --> U
    S --> U
```

Ingestion rules:

- Chunking is deterministic code behavior. The embedding model must not decide where the document is cut.
- Clause headings and paragraph boundaries are preferred. Lightweight NLP sentence segmentation is only a fallback for unclear text boundaries.
- PDF/Office/table extraction output is retrieval evidence only. It must not become structured judgment truth unless a human维护标准指标后写入结构化标准表.
- Every indexed ES chunk must contain source text, embedding vector, document metadata, citation anchors, applicability metadata, and index status traceability.

### 6.2 Standard RAG Retrieval Flow

```mermaid
flowchart TD
    A[FE: 标准 RAG 检索页输入自然语言问题和过滤条件] --> B[BE: StandardRagController]
    B --> C[BE: StandardRagService 校验 query 和过滤条件]
    C --> D[BE: ModelGateway.embed 对 query 向量化]
    D --> E{query embedding 是否可用}
    E -- 否 --> F[BE: 降级为 keyword retrieval 或返回 embedding unavailable]
    E -- 是 --> G[BE: VectorStoreGateway 按 queryVector + filters 检索 ES chunks]
    F --> G
    G --> H{是否有达到回答阈值的条款}
    H -- 否 --> I[BE: 返回 no-evidence refusal，禁止模型凭记忆回答]
    H -- 是 --> J[BE: PromptBuilder 仅注入检索 chunks、来源编号和系统规则]
    J --> K{Chat LLM 是否可用}
    K -- 是 --> L[BE: ModelGateway.chat 生成引用式回答]
    K -- 否 --> M[BE: 返回 raw retrieval fallback]
    L --> N[BE: 校验回答是否只引用已检索来源]
    N --> O{校验通过}
    O -- 是 --> P[DB/AUD: 可选持久化 AI assessment 和 prompt/source 快照]
    O -- 否 --> Q[BE: 降级为拒答或原始条款]
    I --> R[FE: 展示未找到依据]
    M --> S[FE: 展示 AI 不可用和原始条款]
    P --> T[FE: 展示答案/来源/分数/置信度/检索模式]
    Q --> U[FE: 展示拒答或降级原因]
```

Security rules:

- FE renders model output and source paragraphs as escaped text.
- BE treats user query and retrieved clauses as untrusted input.
- LLM must not answer from memory when retrieval evidence is missing.
- Chat prompt must include only retrieved chunks plus grounding rules; it must not include hidden unverified standard limits.
- Retrieval responses should expose whether embedding retrieval was used, whether keyword fallback was used, and how many chunks were sent to chat.

## 7. AI Judgment Explanation And Degradation Flow

```mermaid
flowchart TD
    A[FE: 打开判定解释详情] --> B[GET /judgments/{id}/explanation]
    B --> C[BE: JudgmentService 读取判定和依据快照]
    C --> D[BE: 检查冲突/标准缺口/结构化文档不一致]
    D --> E[BE: RAG 检索匹配条款]
    E --> F[BE: 计算规则化置信度 band]
    F --> G{是否命中预生成缓存}
    G -- 是 --> H[BE: 返回 cache-backed explanation]
    G -- 否 --> I{LLM 是否可用}
    I -- 是 --> J[BE: 生成引用式解释]
    I -- 否 --> K{结构化依据是否完整}
    K -- 是 --> L[BE: 返回 rule-template explanation]
    K -- 否 --> M{ES 检索是否可用}
    M -- 是 --> N[BE: 返回 raw-clause retrieval]
    M -- 否 --> O[BE: 返回 unavailable state]
    H --> P[DB/AUD: 保存或复用 AI assessment]
    J --> P
    L --> P
    N --> P
    O --> Q[FE: 展示不可用且保留规则判定]
    P --> R[FE: 展示结构化规则/AI文本/引用/置信度/降级标签]
```

Frontend requirements:

- Always show structured rule details before generated prose.
- Show confidence and degradation tags using the mapping in `frontend/DESIGN.md`.
- `STANDARD_CONFLICT` must not show a final release conclusion.

Backend requirements:

- Use `ModelGateway` and `VectorStoreGateway`.
- Persist AI assessment input snapshot, citations, model metadata, output, confidence, and degradation source.
- Do not let AI change `qc_judgment_result`.

## 8. Concession Risk Assessment Flow

```mermaid
flowchart TD
    A[FE: 用户在 CAN_CONCESSION 判定页请求让步风险评估] --> B[BE: ConcessionRiskController]
    B --> C[BE: ConcessionRiskService 获取判定快照]
    C --> D{判定是否 CAN_CONCESSION}
    D -- 否 --> E[BE: 拒绝评估并返回业务错误]
    D -- 是 --> F[BE: 获取客户用途]
    F --> G{客户用途是否存在}
    G -- 否 --> H[FE: 要求人工填写用途]
    G -- 是 --> I[BE: 计算偏差程度]
    H --> I
    I --> J[BE: RAG 检索让步条款和历史投诉/案例]
    J --> K[BE: 查询替代资源]
    K --> L[BE: 规则化风险和置信度预判]
    L --> M{低置信或信息不足}
    M -- 是 --> N[BE: 返回 BLOCKED/MANUAL_REVIEW]
    M -- 否 --> O[BE: ModelGateway 生成维度化建议]
    O --> P[DB/AUD: 保存 immutable AI assessment]
    N --> P
    P --> Q[FE: 展示风险等级/维度理由/缺失信息/引用/建议条件]
    Q --> R{用户采纳或忽略}
    R -- 采纳 --> S[BE: 更新 assessment adoption，不自动审批让步]
    R -- 忽略 --> T[BE: 记录 ignore 和人工意见]
```

Hard gates:

- Low-confidence concession assessment must not provide definitive release advice.
- Adoption only marks AI assessment state. It does not approve concession.
- Formal concession approval remains the existing human workflow.

## 9. Reinspection Advice Flow

```mermaid
flowchart TD
    A[FE: 判定详情或复检页请求复检建议] --> B[BE: ReinspectionAdviceService]
    B --> C[BE: 读取判定依据和异常指标]
    C --> D[BE: 获取样品类型和历史复检结果]
    D --> E[BE: 计算置信度和触发原因]
    E --> F{证据是否足够}
    F -- 否 --> G[BE: 返回 withheld advice]
    F -- 是 --> H[BE: 生成建议复检指标和原因]
    G --> I[DB/AUD: 保存 AI assessment]
    H --> I
    I --> J[FE: 展示建议或不足原因]
    J --> K{用户点击采纳}
    K -- 是 --> L[FE: 跳转复检申请并预填原因]
    L --> M[FE: 用户手动提交]
    M --> N[BE: ReinspectionService 创建复检记录]
    K -- 否 --> O[BE: 记录 ignore]
```

Boundary:

- AI advice never creates `qc_reinspection_record`.
- Creating the reinspection record remains a user-submitted workflow action.

## 10. Rejudgment Advice Flow

```mermaid
flowchart TD
    A[FE: 判定详情/新证据入口请求改判建议] --> B[BE: RejudgmentAdviceService]
    B --> C[BE: 获取原判定/依据/新证据/投诉或后工序缺陷]
    C --> D[BE: 判断是否属于标准冲突]
    D -- 是 --> E[BE: 返回必须走标准冲突裁决]
    D -- 否 --> F[BE: 生成目标结论/理由/影响范围建议]
    E --> G[DB/AUD: 保存 AI assessment]
    F --> G
    G --> H[FE: 展示建议或冲突裁决提示]
    H --> I{用户采纳}
    I -- 是 --> J[FE: 跳转改判申请并预填目标结论/理由/影响范围]
    J --> K[FE: 用户手动提交]
    K --> L[BE: RejudgmentService 创建改判申请]
    I -- 否 --> M[BE: 记录 ignore]
```

Hard gate:

- `STANDARD_CONFLICT` is not a normal rejudgment target.
- Conflict must be resolved by conflict裁决 flow.

## 11. Standard Conflict 裁决 Flow

```mermaid
flowchart TD
    A[FE: 标准冲突列表打开冲突详情] --> B[GET /standard-conflicts/{id}]
    B --> C[BE: StandardConflictService 返回冲突详情]
    C --> D[FE: 展示标准对比/指标限值/来源条款/相关判定]
    D --> E{用户是否有裁决权限}
    E -- 否 --> F[FE: 只读展示]
    E -- 是 --> G[FE: 选择控制标准并填写裁决理由]
    G --> H[POST /standard-conflicts/{id}/resolve]
    H --> I[BE: 校验权限和冲突状态]
    I --> J[BE: 保存裁决标准/理由/操作者/时间]
    J --> K[BE: 将原 STANDARD_CONFLICT 判定标记历史或非最终]
    K --> L[BE: 按裁决结果重新执行规则判定]
    L --> M[DB: 保存新的最终 judgment/evidence]
    M --> N[AUD: 写审计日志]
    N --> O[FE: 展示裁决完成和新判定链接]
```

Backend boundaries:

- Conflict Service owns conflict record and裁决.
- Rejudgment after裁决 must call JudgmentService or JudgmentEngine through Service-level APIs, not cross-domain Mapper access.

Frontend boundaries:

- Read-only users must see why they cannot裁决.
- Resolved conflicts must not show editable裁决 form.

## 12. Quality Certificate Data And Q&A Flow

```mermaid
flowchart TD
    A[FE: 选择卷号或批次生成质保书数据] --> B[BE: CertDataService 查询检验记录]
    B --> C[BE: 获取最终判定和判定依据]
    C --> D{是否存在 unresolved STANDARD_CONFLICT}
    D -- 是 --> E[BE: 阻断正式生成，仅允许非最终预览]
    D -- 否 --> F{是否可正式生成}
    F -- 否 --> G[BE: 返回不可生成原因: 未判定/需复检/让步未审批/标准缺口]
    F -- 是 --> H[BE: 汇总关键指标并保存 snapshot]
    E --> I[FE: 展示预览或阻断原因]
    G --> I
    H --> J[FE: 展示正式快照和下载/问答入口]
    J --> K[FE: 用户按卷号/批次提问]
    K --> L[BE: CertificateQaService 读取 snapshot/判定依据/标准条款]
    L --> M{证据是否足够}
    M -- 否 --> N[BE: 拒答并说明缺失依据]
    M -- 是 --> O[BE: 生成引用式回答或降级回答]
    N --> P[FE: 展示拒答/缺失依据]
    O --> Q[FE: 展示回答/引用/置信度/降级标签]
```

Certificate Q&A evidence order:

1. Certificate snapshot.
2. Inspection record and values.
3. Final judgment and evidence snapshots.
4. Standard conflict state.
5. Standard clauses from RAG.

Formal certificate generation must remain blocked when unresolved conflict exists.

## 13. AI Assessment Lifecycle Flow

```mermaid
stateDiagram-v2
    [*] --> Generated
    Generated --> Cached: later request reuses output
    Generated --> Adopted: user adopts suggestion
    Generated --> Ignored: user ignores suggestion
    Cached --> Adopted: user adopts cached suggestion
    Cached --> Ignored: user ignores cached suggestion
    Generated --> Superseded: new assessment generated for same object
    Cached --> Superseded: new assessment generated for same object
    Adopted --> [*]
    Ignored --> [*]
    Superseded --> [*]
```

Immutability rules:

- Raw AI output, input snapshot, citations, model metadata, prompt version, confidence, and degradation source are immutable.
- Adoption status and human opinion may be appended or updated through controlled APIs.
- Every adoption or ignore action must be auditable.

## 14. Frontend And Backend Consistency Checklist

Before implementing or modifying a flow:

- Confirm the target page behavior matches `frontend/DESIGN.md`.
- Confirm backend package, Service, Mapper, and transaction boundaries match `backend/AGENTS.md`.
- Confirm any cross-domain data access uses Service API methods.
- Confirm all AI outputs include confidence, degradation source, citations or explicit no-evidence reason.
- Confirm low-confidence and conflict states have visible FE states and enforceable BE gates.
- Confirm formal business actions are submitted by users, not automatically by AI.
- Confirm every high-risk operation writes audit or AI assessment records.
- Confirm new statuses are represented in backend enums, dictionaries, frontend tags, filters, and tests.

## 15. Change Control

If implementation changes any of these flows, this document MUST be updated in the same change before frontend and backend code diverge.

Flow changes that require updates:

- New judgment state.
- New AI degradation source.
- New certificate blocking rule.
- New conflict type or裁决 path.
- New concession risk gate.
- New Service ownership or cross-Service API method.
- Frontend route/page behavior that changes user decision points.
