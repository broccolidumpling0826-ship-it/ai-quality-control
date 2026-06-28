## Why

标准发布或重索引后，维护人员目前只能在标准维护页看到源文件的解析/索引状态和条款总数，无法核对具体切片内容是否正确。条款数据已持久化在 `qc_standard_clause`，后端也有通用查询 API，但权限绑定在 `menu:standard-rag`，标准维护角色往往无法访问，前端也缺少查看入口。发布验收与索引排错因此只能依赖 RAG 检索间接验证，效率低且不可审计。

## What Changes

- 在标准维护页源文件表格增加 **「查看拆分」** 操作，打开抽屉展示该源文件的条款拆分列表（条款号、页码、原文预览、向量状态）。
- 抽屉支持关键词搜索、分页浏览，点击行可查看完整切片原文及排错字段（`clauseKey`、`esDocumentKey`）。
- 新增 scoped 后端接口：按 `standardId + documentId` 查询条款，权限对齐 `menu:standard`，并校验文档归属，避免跨标准越权。
- 源文件列表补充展示 `parseErrorMessage`（tooltip 或等价方式），与索引失败场景联动。
- 保留现有 `/standard-documents/clauses/*` RAG 侧 API 不变，供标准 RAG 检索菜单继续使用。

**不在本次范围：**

- PDF 原文与切片并排对照预览
- 在线编辑、合并或删除切片
- 条款导出 Excel
- 新的数据库表或迁移

## Capabilities

### New Capabilities

（无）

### Modified Capabilities

- `standard-source-file-management`：扩展要求——已解析的源文件须支持从标准维护页查看条款拆分详情（分页、搜索、单条详情），且权限与标准维护菜单一致。

## Impact

- **Backend**：`StandardController` 新增 2 个 scoped 条款查询接口；`StandardDocumentService` 或 `StandardService` 增加文档归属校验；可选小幅调整 `StandardSourceFileService` 测试。
- **Frontend**：`standard-lib/index.vue` 源文件表格与抽屉组件；`frontend/src/api/standard.ts` 新增 API 封装与类型。
- **权限 / RBAC**：不新增权限码；复用 `menu:standard`（查看）与既有 `standard:manage`（维护操作）。
- **数据库 / ES**：无 schema 变更；读取已有 `qc_standard_clause` 与 `qc_standard_document`。
- **OpenSpec**：`standard-source-file-management` spec delta。
