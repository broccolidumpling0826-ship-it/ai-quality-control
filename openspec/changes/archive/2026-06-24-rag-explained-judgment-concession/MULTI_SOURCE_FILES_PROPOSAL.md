# 一标准多源文件扩展提案

**Change**: `rag-explained-judgment-concession`（增量）  
**状态**: **已确认并合入 OpenSpec**（2026-06-22）  
**方案**: A — 复用 `qc_standard_document` 一对多  

---

本提案内容已同步至正式 OpenSpec artifact：

| Artifact | 路径 |
| --- | --- |
| Spec delta | [`specs/standard-source-file-management/spec.md`](specs/standard-source-file-management/spec.md) |
| RAG spec | [`specs/standard-rag-retrieval/spec.md`](specs/standard-rag-retrieval/spec.md) |
| Design §3B/§3C/§3D | [`design.md`](design.md) |
| Proposal | [`proposal.md`](proposal.md) |
| Tasks §15 | [`tasks.md`](tasks.md) |
| Handoff | [`handoff.md`](handoff.md) |
| Flowcharts | [`flowcharts.md`](flowcharts.md) |

## 已确认决策

1. **每标准最多 10 个源文件**（`STANDARD_DOC_MAX_FILES_PER_STANDARD`）
2. **允许同格式多文件**（如 2 个 PDF）
3. **发布时 ingest 所有已上传文件**；单文件失败不 rollback 发布
4. **删除/重索引按 documentId 粒度**；不 wipe 整个标准目录
5. **首期不做 PRIMARY 主文件标记**；legacy `/source-file` 保留一个版本周期

## 实现顺序（规范先行）

1. ✅ OpenSpec 更新 + `openspec validate --strict`
2. ⏳ 后端 storage / API / publish 多文件
3. ⏳ 前端源文件列表 UI
4. ⏳ E2E + 验证报告

实现代码须以 OpenSpec §15 任务清单为准，不得偏离 spec 行为。
