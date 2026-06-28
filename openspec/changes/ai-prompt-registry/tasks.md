## 1. Prompt 资产盘点与资源骨架

- [x] 1.1 从 `JudgmentServiceImpl`、`StandardRagServiceImpl`、`CertQaServiceImpl`、`ConcessionRiskServiceImpl`、`ImageVisionSourceDocumentTextExtractor` 导出当前 Prompt 原文对照表（作为 golden 基准）
- [x] 1.2 创建 `backend/src/main/resources/ai-prompts/` 目录结构与 `manifest.yaml` 骨架
- [x] 1.3 创建 `shared/citation-judgment-explanation.txt`、`shared/citation-cert-qa.txt`、`shared/citation-standard-rag.txt`（内容与现有 `CitationReferenceSupport` switch 分支一致）
- [x] 1.4 创建五场景 active 版本资源文件（system.txt、user-skeleton.txt、*.meta.yaml；cert-qa 含 append 片段；vision-ocr 含 system 与 user-prompt 默认）
- [x] 1.5 编写 `backend/src/main/resources/ai-prompts/README.md`（版本 bump、manifest 切换、Review 检查项）

## 2. Prompt Registry 基础设施

- [x] 2.1 新增 `PromptScene` 枚举（JUDGMENT_EXPLANATION、STANDARD_RAG、CERT_QA、CONCESSION_RISK、VISION_OCR）
- [x] 2.2 新增 `PromptTemplate`、`PromptManifest`、`PromptRenderResult` 数据类
- [x] 2.3 实现 `PromptRegistry`（classpath 加载 manifest 与模板；缺文件 fail-fast；`resolve(scene)` / `resolveVersion(scene, version)`）
- [x] 2.4 新增 `PromptRegistryTest`（加载成功、缺文件启动失败、activeVersion 解析正确）
- [x] 2.5 可选：新增 `app.ai.prompts.base-path` 配置项（默认 `classpath:ai-prompts/`）

## 3. Citation 规则外置

- [x] 3.1 改造 `CitationReferenceSupport.appendCitationAnswerRules`：从 `PromptRegistry` 读取 shared 规则文本并 append（保留 `CitationPromptStyle` 枚举）
- [x] 3.2 更新 `CitationReferenceSupportTest` 以适配 Registry 加载（Spring 测试或 test fixture Registry）
- [x] 3.3 确认 grounded 校验方法（`acceptTrustedCitedOutput` 等）未改动

## 4. 场景 PromptBuilder 实现（按迁移顺序）

- [x] 4.1 实现 `ConcessionRiskPromptBuilder` 并重构 `ConcessionRiskServiceImpl.fillAiWordingIfAvailable`
- [x] 4.2 新增 `ConcessionRiskPromptBuilderTest` golden 等价测试
- [x] 4.3 实现 `JudgmentExplanationPromptBuilder` 并重构 `JudgmentServiceImpl.buildExplanationChatRequest`
- [x] 4.4 新增 `JudgmentExplanationPromptBuilderTest` golden 等价测试
- [x] 4.5 实现 `StandardRagPromptBuilder` 并重构 `StandardRagServiceImpl.buildChatRequest`（保留 sourceCount 日志）
- [x] 4.6 新增 `StandardRagPromptBuilderTest` golden 等价测试
- [x] 4.7 实现 `CertQaPromptBuilder`（含条件 append 逻辑）并重构 `CertQaServiceImpl.buildCertQaChatRequest`
- [x] 4.8 新增 `CertQaPromptBuilderTest`（覆盖非最终态、让步已批缺快照两分支）
- [x] 4.9 实现 `VisionOcrPromptProvider` 并重构 `ImageVisionSourceDocumentTextExtractor`（删除 `DEFAULT_SYSTEM_PROMPT` 常量）
- [x] 4.10 对齐 `application.yml` 中 `AI_VISION_OCR_PROMPT` 与 manifest 默认值；确认 env override 优先级正确

## 5. Service 清理与审计一致性

- [x] 5.1 删除各 Service 内 `PROMPT_VERSION` / `EXPLANATION_PROMPT_VERSION` 硬编码常量
- [x] 5.2 确认 `ModelChatRequest.promptVersion` 与 `AiAssessmentCreateCmd.promptVersion` 均来自 Registry active version
- [x] 5.3 确认 `businessType` 字段与迁移前一致

## 6. 验证与收尾

- [x] 6.1 运行 `cd backend && mvn test`，修复失败用例
- [ ] 6.2 手动冒烟：判定解释、标准 RAG、质保书问答、让步润色、Vision OCR 上传（若环境可用）
- [ ] 6.3 可选：手动运行 `openspec/changes/rag-explained-judgment-concession/evaluation/run-evaluation.mjs` 并记录结果
- [x] 6.4 运行 `openspec validate ai-prompt-registry --strict` 确认 change 合法
