## Why

当前系统在判定解释、标准 RAG、质保书问答、让步风险润色、Vision OCR 等场景中，Prompt 文案与模型参数分散在多个 Java Service 与少量 yml 配置中硬编码。`prompt_version` 虽已写入 `qc_ai_assessment` 审计，但版本号与 Prompt 内容无法一一对应，调优需改代码并发版，Review 成本高，也不利于后续评测回归与版本 diff。

OpenSpec 设计（`ai-gateway-degradation`）已要求 Gateway 层隔离 prompt policy，但 P0 实现尚未落地。现在 AI 四场景 Prompt 形态已稳定，适合采用路径 A（Git 资源文件 + PromptRegistry + PromptBuilder）做等价收敛，为后续可选的 DB 在线管理（路径 B）打基础。

## What Changes

- 新增 `backend/src/main/resources/ai-prompts/` 资源目录与 `manifest.yaml`，集中管理各场景 active 版本、system 文案、user skeleton、共享引用规则、temperature/maxTokens。
- 新增 `PromptRegistry`、`PromptTemplate`、`PromptScene` 枚举及按场景的 `PromptBuilder`，启动时加载资源，缺失文件 fail-fast。
- 重构 `JudgmentServiceImpl`、`StandardRagServiceImpl`、`CertQaServiceImpl`、`ConcessionRiskServiceImpl` 的 Prompt 组装逻辑，改为委托 Builder；**运行时行为与现有硬编码等价**。
- 将 `CitationReferenceSupport.appendCitationAnswerRules` 的三类规则文本外置到 shared 资源文件，校验逻辑（`acceptTrustedCitedOutput` / grounded 规则）保留在 Java 代码。
- 统一 Vision OCR 的 system prompt 加载路径；`application.yml` 的 `AI_VISION_OCR_PROMPT` 仍可环境变量 override。
- 新增 Registry/Builder 单元测试与 golden 等价性测试，确保迁移前后 Prompt 字节级一致。
- 新增 `ai-prompts/README.md` 说明版本 bump、占位符与 Review 约定。

**不在本次范围（路径 B 留待后续 change）：**

- DB 表与管理页在线编辑 Prompt
- 运行时热更新、A/B 灰度
- `prompt_snapshot_hash` 持久化到 `qc_ai_assessment`
- 将 grounded 校验逻辑配置化

## Capabilities

### New Capabilities

- `ai-prompt-registry`: 基于 Git 资源文件的 AI Prompt 集中注册、版本管理与运行时组装能力，覆盖 Chat 四场景与 Vision OCR。

### Modified Capabilities

- `ai-gateway-degradation`: 补充 Prompt 策略须通过 Prompt Registry 加载、业务 Service 不得硬编码 system/skeleton 的要求（实现层面收敛，不改变对外降级行为）。

## Impact

- **Backend 新增包**：`com.jhict.quality.service.support.prompt`（或等价命名）— Registry、Template、Builder、Context。
- **Backend 改动**：`JudgmentServiceImpl`、`StandardRagServiceImpl`、`CertQaServiceImpl`、`ConcessionRiskServiceImpl`、`CitationReferenceSupport`、`ImageVisionSourceDocumentTextExtractor`。
- **资源文件**：`backend/src/main/resources/ai-prompts/**`。
- **测试**：新增 Registry/Builder 测试；更新 `CitationReferenceSupportTest`（若引用规则改从 Registry 加载）。
- **API / 数据库**：无对外 API 变更；`qc_ai_assessment.prompt_version` 字段语义不变，值仍来自 Registry active version。
- **前端**：无变更。
- **运维**：改 Prompt 仍需发版；可通过 manifest 切换 activeVersion 指针。
