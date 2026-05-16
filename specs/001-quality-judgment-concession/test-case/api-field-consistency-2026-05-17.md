# 前后端接口字段一致性检查报告

**检查时间**: 2026-05-17  
**检查范围**: 质量判定解释与让步管理系统全部业务模块 REST API  
**检查方式**: 对照 `backend/.../controller`、DTO/VO/Entity 与 `frontend/src/api`、`frontend/src/views`  

---

## 一、检查结论摘要

| 类别 | 数量 | 说明 |
|------|------|------|
| 已修复 | 28 | 传参方式、HTTP 方法、字段名不一致等 |
| 部分对齐（后端别名） | 6 | 通过 VO/DTO 别名字段兼容前端历史命名 |
| 待后续增强 | 5 | 列表需关联查询、判定解释页结构差异等 |

整体：**分页类 POST 接口若后端使用 `@RequestParam`，前端不得将条件放在 JSON Body 中**；本次已统一修复复检、改判、让步、质保书等模块。

---

## 二、按模块检查结果

### 1. 认证（Auth）

| 项目 | 后端 | 前端（修复前） | 处理 |
|------|------|----------------|------|
| 登录工号 | `LoginCmd.userNo` | `LoginForm.username` | `auth store` 已映射；补充 `LoginCmd` 类型与 API 注释 |
| 登录响应 | `LoginVO` 字段一致 | `UserInfo` 一致 | 无变更 |

### 2. 检验录入（Inspection）

| 项目 | 后端 | 前端（修复前） | 处理 |
|------|------|----------------|------|
| 新增字段 | `productSpec`, `testTime`, `testerNo`, `values[]` | `specification`, `inspectionTime`, `inspector`, `indicators` | `mapInspectionAddPayload()` 统一映射 |
| 分页时间 | `testTimeStart` / `testTimeEnd` | `startTime` / `endTime` | 后端 `QcInspectionRecordPageQuery` 增加 setter 兼容 |
| 列表列 | `testTime`, `testerNo`, `status=VOID` | `inspectionTime`, `inspector`, `VOIDED` | 前端列表列与状态判断已改 |

### 3. 判定解释（Judgment）

| 项目 | 后端 | 前端（修复前） | 处理 |
|------|------|----------------|------|
| 分页时间 | `timeStart` / `timeEnd` | `startTime` / `endTime` 或仅日期 | 前端改传 `timeStart/timeEnd`；DTO 增加 setter 兼容 |
| 列表时间列 | `judgmentTime` | `judgeTime` | 列表 `prop` 已改 |
| 列表关联字段 | 实体无 `coilNo`/`batchNo` | 表格展示卷号/批次 | **待增强**：需列表 VO 关联检验记录（未在本次实现） |
| 发起复检 | `originalJudgmentId`, `reinspectionReason` | `judgmentId`, `reason` | `explanation.vue` 与 `reinspection` API 已改 |

### 4. 复检（Reinspection）

| 项目 | 后端 | 前端（修复前） | 处理 |
|------|------|----------------|------|
| 分页传参 | `@RequestParam` | POST Body | `pageReinspections` 改为 query params |
| 责任人筛选 | `responsibleNo` | `responsiblePerson` | API 与页面已改 |
| 列表字段 | `reinspectionReason`, `createDateTime` | `reason`, `createTime` | 表格列已改 |
| 卷号/原判定类型 | 需关联查询 | `coilNo`, `originalJudgmentType` | **待增强** |

### 5. 改判（Rejudgment）

| 项目 | 后端 | 前端（修复前） | 处理 |
|------|------|----------------|------|
| 分页传参 | `@RequestParam` | POST Body | `pageRejudgments` 改为 query params |
| 申请体 | `originalJudgmentId`, `rejudgmentReason`, `affectScope` | `judgmentId`, `reason`, `impactScope` | `applyRejudgment` 与表单提交已改 |
| 审批体 | `action`, `comment` | `decision`, `comment` | 审批弹窗已改 |
| 详情/审批历史 | `approvalRecords`, `approvalAction` | `approvalHistory`, `decision` | 后端 VO 增加别名；详情页兼容读取 |

### 6. 让步接收（Concession）

| 项目 | 后端 | 前端（修复前） | 处理 |
|------|------|----------------|------|
| 分页传参 | `@RequestParam` | POST Body | `pageConcessions` 改为 query params |
| 筛选总状态 | `approvalStatus` | `concessionStatus` | API 映射 `approvalStatus` |
| 客户确认 | `PUT` + `file` + `cmd.confirmNote` | `POST` + `confirmFile` + `summary` | `confirmConcession` 已改 |
| 拒绝 | `rejectNote` query | `rejectReason` body | `rejectConcession` 已改 |
| 有效期展示 | `effectiveDate`/`expiryDate` | `validFrom`/`validTo` | 后端 VO 增加别名字段 |
| 卷号/批次/申请人 | VO 未含 | 列表/详情展示 | **待增强**（需关联判定/检验） |

### 7. 质量标准（Standard）

| 项目 | 后端 | 前端（修复前） | 处理 |
|------|------|----------------|------|
| 分页查询 | `variety`, `grade` | `productVariety`, `productGrade` | `QcQualityStandardPageQuery` setter 兼容 |
| 列表返回 | `variety`, `grade`, `versionNo`, `specRange` | `productVariety`, `version`, `standardCode` | VO 增加别名；列表 `loadData` 映射 |
| 提交体 | `QcQualityStandardAddCmd` | 含 `standardName` 无 `specRange` | `mapStandardPayload()` 映射 |

### 8. 标准覆盖缺口（Standard Gap）

| 项目 | 后端 | 前端 | 处理 |
|------|------|------|------|
| 分页 | `@ModelAttribute` + query | `post(..., null, { params })` | 原本一致，无需修改 |

### 9. 指标项目（Indicator）

| 项目 | 后端 | 前端 | 处理 |
|------|------|------|------|
| 类别字段 | `category`（VO 含 `indicatorCategory` 别名） | `category` | 已一致 |
| 分页/CRUD | `@RequestBody` | POST Body | 已一致 |

### 10. 质保书数据（Cert Data）

| 项目 | 后端 | 前端（修复前） | 处理 |
|------|------|----------------|------|
| 分页传参 | `@RequestParam` | POST Body | `pageCertData` 改为 query params |

### 11. 质量统计（Statistics）

| 项目 | 后端 | 前端 | 处理 |
|------|------|------|------|
| 时间参数 | `timeStart`, `timeEnd` | `timeStart`, `timeEnd` | 已一致 |

### 12. 工作台（Dashboard）

| 项目 | 后端 | 前端 | 处理 |
|------|------|------|------|
| 汇总字段 | `pendingJudgmentCount` 等 | 曾用短名 | 页面已有 fallback 映射，保持 |

### 13. 审计日志（Audit）

| 项目 | 后端 | 前端（修复前） | 处理 |
|------|------|----------------|------|
| 时间查询 | `timeStart`/`timeEnd` 或 `startTime`/`endTime` | `startTime`/`endTime` | 后端已兼容 |
| 列表列 | `targetEntity`, `targetId`, `ipAddress` | `operationModule`, `ip` | 表格列已改 |

### 14. 账号管理（User Manage）

| 项目 | 后端 | 前端 | 处理 |
|------|------|------|------|
| 分页/CRUD | `@RequestBody` | POST Body | 已一致 |

### 15. 数据字典（Dict）

| 项目 | 后端 | 前端 | 处理 |
|------|------|------|------|
| 下拉项 | `value`, `label`, `colorTag` | 同左 | 已一致 |
| 管理端项 | `itemValue`, `itemLabel` | `DictItemVO` 定义 | 已一致 |

---

## 三、本次代码修改清单

### 前端 API（`frontend/src/api/`）

- `reinspection.ts` — 分页改 query；类型化请求体
- `rejudgment.ts` — 分页改 query；`action`/`rejudgmentReason` 等
- `concession.ts` — 分页改 query；确认/拒绝/审批方法与字段
- `cert-data.ts` — 分页改 query
- `inspection.ts` — `mapInspectionAddPayload`
- `standard.ts` — `mapStandardPayload`
- `auth.ts` — `LoginCmd` 类型说明

### 前端页面（`frontend/src/views/`）

- `inspection/form.vue`, `inspection/index.vue`
- `reinspection/index.vue`
- `re-judgment/index.vue`, `re-judgment/form.vue`, `re-judgment/detail.vue`
- `concession/index.vue`, `concession/detail.vue`
- `judgment/index.vue`, `judgment/explanation.vue`
- `standard-lib/index.vue`
- `audit/index.vue`

### 后端

- `QcQualityStandardPageQuery.java` — `productVariety`/`productGrade` 兼容
- `QcQualityStandardVO.java` + `StandardServiceImpl.java` — 列表别名与 `specRange`
- `QcInspectionRecordPageQuery.java` — `startTime`/`endTime` 兼容
- `QcJudgmentPageQuery.java` — `startTime`/`endTime` 兼容
- `QcConcessionVO.java` + `ConcessionServiceImpl.java` — `validFrom`/`validTo`/`confirmFileUrl` 别名
- `QcRejudgmentRequestVO.java` + `RejudgmentServiceImpl.java` — 详情与审批历史别名

### 类型

- `frontend/src/types/index.ts` — 补充 `LoginCmd`

---

## 四、验证说明

- 后端：`mvn compile -DskipTests` 通过
- 前端：`npm run build` 仍存在项目原有问题（`BaseTable.vue`、`vite.config.ts` 等），与本次字段修复无直接关系；本次修改的 API/页面无新增 TS 错误

建议在本地启动前后端后，按模块验证：

1. 登录（工号 + 密码）
2. 检验录入提交与列表查询
3. 复检/改判/让步列表分页与筛选
4. 改判申请与审批
5. 让步客户确认（上传附件）
6. 标准库分页与保存
7. 审计日志查询

---

## 五、后续建议项实施记录（2026-05-17 补充）

### 1. 判定 / 复检 / 让步列表 VO（已完成）

| 模块 | 新增/调整 | 关联字段 |
|------|-----------|----------|
| 判定 | `QcJudgmentListVO`，`JudgmentService.page` 返回列表 VO | `coilNo`, `batchNo`, `heatNo`, `productVariety`, `productGrade`, `testerNo`, `inspector` |
| 复检 | `QcReinspectionListVO`，`ReinspectionService.page` | `coilNo`, `originalJudgmentType`, `reinspectionReason`, `createDateTime` |
| 让步 | `QcConcessionVO` 增强 `toVO` | `coilNo`, `batchNo`, `applyBy`, `applyByName`, `applyTime`, `reason`；分页支持 `coilNo` 筛选 |

判定分页查询新增 `coilNo`、`batchNo` 条件（关联检验记录过滤）。

### 2. 判定解释页与 `QcJudgmentResultVO` 对齐（已完成）

后端 `buildJudgmentResultVO` 补充：

- 检验信息：`batchNo`, `heatNo`, `productVariety`, `productGrade`, `productSpec`/`specification`, `testerNo`, `inspector`, `judgeTime`
- `standardMatches`：按 CUSTOMER → ENTERPRISE → NATIONAL 构建优先级卡片
- `indicatorDetails`：由 `evidences` 转换，含 `measuredValue`、`triggeredRule`、`indicatorResult`、`noStandard`

前端 `explanation.vue` 可直接使用上述字段；路由跳转统一使用 `judgmentId`。

### 3. 改判逆向附件上传（已完成）

- 新增 `frontend/src/api/file.ts` → `POST /files/upload?category=rejudgment-evidence`
- `re-judgment/form.vue` 提交前先上传文件，将返回的相对路径写入 `evidenceAttachmentUrl`

### 4. 标准库规格范围字段（已完成）

- 表单增加必填项「规格范围」`specRange`
- `mapStandardPayload` 优先使用 `specRange`，不再用 `standardName` 兜底
- 详情/列表 VO 返回 `specRange`、`description`（备注别名）

---

## 六、宪法合规复核调整（2026-05-17）

对照 `.specify/memory/constitution.md` 对本次新增/修改代码做了如下整改：

| 规范条款 | 原问题 | 调整 |
|----------|--------|------|
| II. 方法参数 ≤4 | `ConcessionService.page` 5 个参数 | 新增 `QcConcessionPageQuery` |
| II. 方法参数 ≤4 | `ReinspectionService.page` 4 个参数边界 | 新增 `QcReinspectionPageQuery` 统一封装 |
| II. 禁止魔法值 | 标准优先级、PASS/FAIL 等字面量 | 新增 `JudgmentExplainConstants`，使用 `StandardType` 枚举 |
| III. 状态字面量 | 复检 PENDING/COMPLETED 字符串 | 改用 `ReinspectionStatus` 枚举 |
| III. 状态字面量 | 让步 CAN_CONCESSION 判断 | 改用 `JudgmentType.CAN_CONCESSION` |
| III. Service 入口编排 | `buildJudgmentResultVO` 过长 | 拆分为 `fillInspectionFieldsOnVo` 等具名私有方法 |
| III. 外部依赖收敛 | 分页/解释逻辑堆叠 | 抽出 `buildJudgmentPageWrapper`、`convertToJudgmentListPage` 等 |
| I. VO 字段注释 | `IndicatorDetailVO` 缺 `@ApiModelProperty` | 已补全 |
| IV. TypeScript 双引号 | `file.ts` 使用单引号 | 已改为双引号 |
