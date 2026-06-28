## 1. Backend — Scoped clause query API

- [x] 1.1 在 `StandardSourceFileService` 接口新增 `pageSourceFileClauses` 与 `getSourceFileClause` 方法签名
- [x] 1.2 在 `StandardSourceFileServiceImpl` 实现：先 `getLinkedDocument(standardId, documentId)` 校验归属，再委托 `StandardDocumentService.pageClauses` / `getClauseById`（分页强制 `documentId`；详情断言 `clause.documentId` 一致）
- [x] 1.3 在 `StandardController` 新增 `POST /{id}/source-files/{documentId}/clauses/page` 与 `GET /{id}/source-files/{documentId}/clauses/{clauseId}`，权限 `@SaCheckPermission("menu:standard")`
- [x] 1.4 新增 `StandardSourceFileServiceImplTest`（或等价测试）：归属校验失败 404、分页仅返回该 document、跨 document 查 clause 拒绝

## 2. Frontend — API 封装

- [x] 2.1 在 `frontend/src/api/standard.ts` 新增 `StandardClauseSummary` 类型与 `pageStandardSourceClauses`、`getStandardSourceClause` 方法

## 3. Frontend — 标准维护页抽屉 UI

- [x] 3.1 源文件表格操作列增加「查看拆分」按钮（`chunkCount > 0` 时显示；view/edit 模式均可）
- [x] 3.2 实现条款拆分抽屉：文档摘要区（文件名、parse/index 状态、chunkCount、parseErrorMessage）
- [x] 3.3 抽屉内实现关键词搜索、分页表格（条款号、页码、字符数、embeddingStatus、原文预览 80 字）
- [x] 3.4 行点击打开详情 dialog：完整 `paragraphText`、`clauseKey`、`esDocumentKey`
- [x] 3.5 源文件列表「索引」列或文件名旁补充 `parseErrorMessage` tooltip（有值时显示 warning 图标）

## 4. 验证与 OpenSpec

- [x] 4.1 运行 `cd backend && mvn test`，修复失败用例
- [x] 4.2 运行 `cd frontend && npm run build`，确认编译通过（项目存在既有 TS 错误，与本 change 无关；本次改动文件无新增 lint 问题）
- [x] 4.3 运行 `openspec validate standard-source-clause-view --strict`
- [x] 4.4 手动冒烟：发布/重索引后在标准维护页打开抽屉，验证搜索、分页、详情与越权拦截（若环境可用）（未执行：本地无运行中后端/前端服务）
