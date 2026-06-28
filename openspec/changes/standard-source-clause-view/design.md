## Context

标准发布或重索引时，系统已将源文件解析切片写入 `qc_standard_clause`，并在标准维护页源文件列表展示 `parseStatus`、`indexStatus`、`chunkCount`。条款查询能力已存在于 `StandardDocumentService.pageClauses` / `getClauseById`，并通过 `StandardDocumentController` 暴露，但权限为 `menu:standard-rag`，与标准维护菜单（`menu:standard`）不一致。

`StandardDocumentService.getLinkedDocument(standardId, documentId)` 已实现文档归属校验，被源文件下载、删除、重索引等流程复用。前端 `standard-lib/index.vue` 使用 Element Plus Drawer 模式，源文件表格目前无「查看拆分」入口，`parseErrorMessage` 字段也未展示。

## Goals / Non-Goals

**Goals:**

- 标准维护人员可在源文件行打开抽屉，分页查看该文件的条款拆分列表与单条详情。
- 提供 scoped REST API（`standardId + documentId`），权限 `menu:standard`，并校验文档归属。
- 抽屉支持关键词搜索（条款号/段落文本），展示向量状态便于索引排错。
- 复用现有 `qc_standard_clause` 数据与 `StandardClauseVO`，不新增表或 ES 结构。

**Non-Goals:**

- PDF 原文并排对照、切片在线编辑、Excel 导出。
- 修改 `StandardDocumentController` 现有 RAG 侧 API 的权限或路径。
- 新增 RBAC 权限码或菜单项。
- 实时 ingest 进度流式展示。

## Decisions

### 1. 新增 scoped 接口于 `StandardController`，而非放宽 RAG API 权限

**选择：**

```text
POST /api/v1/standards/{standardId}/source-files/{documentId}/clauses/page
GET  /api/v1/standards/{standardId}/source-files/{documentId}/clauses/{clauseId}
```

**理由：** 路径语义与现有 `source-files/{documentId}/reindex` 一致；天然携带 `standardId` 做归属校验；不影响 RAG 菜单独立授权模型。

**备选：** 将 `StandardDocumentController.pageClauses` 权限改为 `menu:standard` — rejected（丢失 document 归属约束，且 RAG 与维护职责混淆）。

### 2. 编排层：`StandardSourceFileService` 暴露条款查询 facade

**选择：** 在 `StandardSourceFileService` 新增：

- `pageSourceFileClauses(standardId, documentId, query)` — 先 `getLinkedDocument`，再调用 `StandardDocumentService.pageClauses`（强制 `documentId`）
- `getSourceFileClause(standardId, documentId, clauseId)` — 校验 document 归属后 `getClauseById`，并断言 `clause.documentId == documentId`

**理由：** 符合 backend `AGENTS.md` 跨域通过 Service API 访问；Controller 不直接组合多个 Service；与 download/delete/reindex 模式一致。

**备选：** Controller 直接调 `StandardDocumentService` — rejected（归属校验逻辑分散）。

### 3. 查询 DTO：复用 `StandardClausePageQuery` 子集

**选择：** 请求体沿用 `StandardClausePageQuery` 的 `keyword`、`pageNum`、`pageSize`；`documentId` 由路径注入，忽略 body 中的 `documentId`/`standardId` 以防越权。

**理由：** 零新增 DTO；与现有分页逻辑一致。

### 4. 前端：内嵌抽屉于 `standard-lib/index.vue`

**选择：** 在源文件表格操作列增加「查看拆分」；点击后打开 `el-drawer`（宽度约 720–840px），内含：

1. 文档摘要区（文件名、parse/index 状态、chunkCount、parseErrorMessage）
2. 关键词搜索 + 分页表格（条款号、页码、字符数、embeddingStatus、原文预览）
3. 行点击 → `el-dialog` 展示完整 `paragraphText` 及 `clauseKey`、`esDocumentKey`

**显示条件：** `chunkCount > 0`（含索引失败但已切片场景）。

**理由：** 改动集中、与现有 Drawer 交互一致；无需新路由与菜单 seed。

**备选：** 独立子页面 — rejected（方案 A 明确选择内嵌抽屉）。

### 5. 列表增强：`parseErrorMessage` 以 tooltip 展示

**选择：** 在「索引」列或文件名旁，当 `parseErrorMessage` 非空时显示 warning 图标 + tooltip。

**理由：** API 已有字段，UI 未展示；与拆分详情抽屉形成排错闭环，改动极小。

### 6. 响应字段：直接使用 `StandardClauseVO`

**选择：** 不新建 VO；前端 TypeScript 接口与 `StandardClauseVO` 字段对齐（至少 `id`、`clauseNo`、`pageNo`、`paragraphText`、`embeddingStatus`、`clauseKey`、`esDocumentKey`）。

**理由：** 只读查询，无敏感字段；避免重复映射。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| 大 PDF 条款数多（200+），抽屉加载慢 | 默认 `pageSize=20`；仅请求当前页；关键词搜索走后端 LIKE |
| 单条 `paragraphText` 很长 | 列表仅预览 80 字；详情用 dialog 全文展示 |
| 用户误以为切片可编辑 | UI 文案明确「只读验收」；不提供编辑控件 |
| `AUTO-x` 条款号不直观 | 保留 chunker 原样输出；详情展示完整文本供人工判断 |
| 重索引后列表 stale | 关闭抽屉再打开会重新请求；重索引成功后若抽屉已开可提示刷新 |

## Migration Plan

1. 部署后端 scoped API（向后兼容，无数据迁移）。
2. 部署前端抽屉与 API 封装。
3. 无需 RBAC seed 变更；已有 `menu:standard` 角色自动可用。
4. 回滚：移除前端入口与 Controller 两个 endpoint 即可；不影响 ingest 与 RAG。

## Open Questions

（无阻塞项；实现阶段默认：`pageSize=20`，预览截断 80 字符，抽屉宽度 800px。）
