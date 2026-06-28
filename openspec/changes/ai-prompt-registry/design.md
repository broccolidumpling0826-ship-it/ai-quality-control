## Context

当前 AI 相关 Prompt 分散在以下位置：

| 场景 | 类 | 当前 version |
|------|-----|--------------|
| 判定解释 | `JudgmentServiceImpl` | `judgment-explanation-v3` |
| 标准 RAG | `StandardRagServiceImpl` | `standard-rag-p0-v3` |
| 质保书问答 | `CertQaServiceImpl` | `cert-qa-v4` |
| 让步风险润色 | `ConcessionRiskServiceImpl` | `concession-risk-v1` |
| Vision OCR | `ImageVisionSourceDocumentTextExtractor` + `application.yml` | 无统一 version |

共享引用规则硬编码在 `CitationReferenceSupport.appendCitationAnswerRules`（三类 `CitationPromptStyle`）。grounded 校验（`acceptTrustedCitedOutput`）与 Prompt 规则配套，须保留在 Java。

`ModelChatRequest` 已具备 `promptVersion`、`systemPrompt`、`temperature`、`maxTokens` 字段；`qc_ai_assessment.prompt_version` 已用于审计，但无法回放 Prompt 正文。

## Goals / Non-Goals

**Goals:**

- 将固定 Prompt 策略（system、user skeleton、引用规则文案、模型参数、version）外置到 Git 资源文件，由 `PromptRegistry` 统一加载。
- 引入按场景的 `PromptBuilder`，运行时拼装动态业务事实（判定结论、指标依据、citations、用户问题等）。
- 迁移后 **行为与现有硬编码等价**（字节级 Prompt 一致），`prompt_version` 语义不变。
- 启动时 manifest 解析 fail-fast；旧版本文件保留在目录中便于 diff 与 manifest 回滚。
- 为路径 B（DB override + Admin 页）预留 Registry 接口，无需推倒重来。

**Non-Goals:**

- DB 在线编辑、运行时热更新、A/B 灰度。
- 将 grounded 校验逻辑或业务条件判断配置化。
- 将 evidences/citations 循环逻辑迁入模板引擎（循环留 Java Builder）。
- `prompt_snapshot_hash` 持久化（可选后续 change）。
- 修改对外 REST API 或前端页面。

## Decisions

### 1. 路径 A：classpath 资源文件 + manifest，而非 application.yml 大段文本

**选择：** 独立目录 `backend/src/main/resources/ai-prompts/`，manifest 指定 scene → activeVersion。

**理由：** 长文本在 yml 中可读性差、Diff 不友好；manifest 支持多版本并存与指针切换。

**备选：** 全放 `application.yml` —  rejected（不可维护）。

### 2. 三层 Prompt 分离

```text
Layer 1  策略（外置）     system、skeleton 标题、引用规则文本、参数
Layer 2  编排（Builder）  占位符替换、条件分支选择、循环拼 list
Layer 3  事实（运行时）   VO/Entity 字段、citation 段落正文
```

**理由：** 与「结构化数据为判定真相源、AI 仅解释」原则一致；避免限值/结论被配置篡改。

### 3. 包结构与核心类型

包名：`com.jhict.quality.service.support.prompt`

| 类型 | 职责 |
|------|------|
| `PromptScene` | 枚举：`JUDGMENT_EXPLANATION`, `STANDARD_RAG`, `CERT_QA`, `CONCESSION_RISK`, `VISION_OCR` |
| `PromptTemplate` | version, systemPrompt, userSkeleton, conditionalSnippets, citationStyle, temperature, maxTokens, businessType |
| `PromptManifest` | 解析 `manifest.yaml` |
| `PromptRegistry` | `@Component`，启动加载；`resolve(PromptScene)` → `PromptTemplate` |
| `PromptAssemblyContext` | 各 Builder 输入（按场景分子类或统一 Map + typed getters） |
| `*PromptBuilder` | 每场景一个，输出 `ModelChatRequest` 或 `PromptRenderResult` |

**理由：** 与现有 `service.support.rag` 并列，不污染 business Service。

### 4. 引用规则：文本外置，校验留代码

`CitationReferenceSupport.appendCitationAnswerRules` 改为从 Registry 读取 `shared/citation-*.txt` 并 append；`appendNumberedCitationBlock` / `appendNumberedRagCitationBlock` 保持现有实现。

**理由：** 规则文案可调优；grounded 算法与安全边界不受配置误改影响。

**实现注意：** `CitationReferenceSupport` 当前为 static 方法；可通过注入 `PromptRegistry` 的实例方法包装，或 Registry 在 `@PostConstruct` 时预加载 citation 文本到 static map（优先实例化 + 委托，减少 static 状态）。

### 5. 模板占位符：轻量 replace，不用完整 Mustache

user-skeleton 使用 `{{judgmentType}}` 等简单占位符；列表段（evidences、citations）由 Builder 代码循环生成后插入固定 section 标题之后。

**理由：** Java 8 项目，避免引入重型模板依赖；现有逻辑已是 StringBuilder 风格。

### 6. cert-qa 条件分支

条件判断（`answerNonFinal`、`needsCertSnapshot`、`concessionApproved`）留 `CertQaPromptBuilder`；追加文案外置为 `cert-qa-v4.append-*.txt`。

### 7. Vision OCR 配置优先级

```text
application.yml / env AI_VISION_OCR_PROMPT  >  manifest vision-ocr activeVersion user-prompt
system prompt 仅从 manifest 读取（删除 Java DEFAULT_SYSTEM_PROMPT 常量）
```

新增 `vision-ocr-v1` version；OCR 暂不强制写 `qc_ai_assessment`（无现有审计点）。

### 8. 迁移顺序（由简到难）

1. `concession-risk` — 无 citation
2. `judgment-explanation`
3. `standard-rag`
4. `cert-qa` — 条件分支
5. `vision-ocr`

每步完成后跑 golden 等价测试再进入下一步。

### 9. 测试策略

- `PromptRegistryTest`：manifest 加载、缺文件 fail-fast、resolveVersion
- `*PromptBuilderTest`：fixture Context → 与迁移前 golden 文件逐字比对
- 现有 `CitationReferenceSupportTest` 更新以覆盖 Registry 加载的 citation 规则
- 迁移 PR 前建议手动跑 `evaluation/run-evaluation.mjs`（不强制 CI 接入）

### 10. 资源目录布局

```text
backend/src/main/resources/ai-prompts/
├── manifest.yaml
├── README.md
├── shared/
│   ├── citation-judgment-explanation.txt
│   ├── citation-cert-qa.txt
│   └── citation-standard-rag.txt
├── judgment-explanation/
│   └── judgment-explanation-v3.{meta.yaml,system.txt,user-skeleton.txt}
├── standard-rag/
│   └── standard-rag-p0-v3.*
├── cert-qa/
│   └── cert-qa-v4.* + append-*.txt
├── concession-risk/
│   └── concession-risk-v1.*
└── vision-ocr/
    └── vision-ocr-v1.*
```

`*.meta.yaml` 含 changelog、scene、version（与文件名一致）。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| 「等价迁移」时顺手改文案导致行为漂移 | golden 测试 + Code Review 禁止同 PR 改文案与逻辑 |
| Registry 缺文件 silent fallback | fail-fast 启动失败 |
| Citation 规则文本与 grounded 校验不同步 | 规则变更必须跑 `CitationReferenceSupportTest` + evaluation |
| static `CitationReferenceSupport` 与 Spring 注入冲突 | Builder 层注入 Registry，Support 增加实例方法或初始化钩子 |
| 改 Prompt 仍需发版 | 路径 A 接受；路径 B 后续 change |
| cert-qa 条件误迁入 txt | 设计明确：条件在 Java，文案在文件 |

## Migration Plan

1. **Phase 0（本 change）**：新增 Registry + 资源文件 + Builder；Service 委托 Builder；删除硬编码常量；测试绿灯。
2. **部署**：普通后端发版，无 DB migration，无配置强制变更（Vision OCR env override 仍有效）。
3. **回滚**：manifest 指针指回旧 version 文件，或 revert PR；无数据迁移。
4. **Prompt 调优流程（发版后）**：复制 vN → vN+1 文件 → 改 manifest activeVersion → changelog → golden 测试 → PR。

## Open Questions

- `CitationReferenceSupport` 是否在本 change 中改为 Spring `@Component`（实例方法），还是 Registry 预加载 + static 委托？**建议：** 本 change 用 `@Component` 包装 citation rules 加载，尽量减少 static 全局状态。
- Vision OCR 是否在本 change 写入 `prompt_version` 到某审计表？**建议：** 暂不写，仅统一加载路径；若标准文档 ingestion 已有 trace 日志即可。
