---
description: "Task list for 质量判定解释与让步管理系统"
---

# Tasks: 质量判定解释与让步管理系统

**Input**: Design documents from `specs/001-quality-judgment-concession/`
**Design Reference**: QC-DDS-001 §4（12 个子系统模块，严格按 §4 顺序）
**Status**: 实现基本完成 — [X] 已完成，[ ] 待完成

## Format: `[ID] [P?] [Story?] Description — file path`

- **[P]**: 可并行（不同文件，无未完成依赖）
- **[US1/2/3/4]**: 对应哪个用户故事

---

## Phase 1: Setup（项目初始化）

- [X] T001 创建 Spring Boot 2.7.x Maven 单模块项目 — `backend/pom.xml`
- [X] T002 [P] 配置 pom.xml 所有依赖（MyBatis-Plus/Sa-Token/Knife4j/Redis/Hutool 等）— `backend/pom.xml`
- [X] T003 [P] 创建 application.yml（公共配置）— `backend/src/main/resources/application.yml`
- [X] T004 [P] 创建 application-dev.yml（SSH 隧道本地开发：3307/6380 端口映射）— `backend/src/main/resources/application-dev.yml`
- [X] T005 [P] 创建 application-prod.yml（生产配置）— `backend/src/main/resources/application-prod.yml`
- [X] T006 用 Vite 创建 Vue 3 + TypeScript 前端项目（Element Plus/Pinia/Vue Router/Axios/ECharts）— `frontend/package.json` + `vite.config.ts`
- [X] T007 创建数据库 DDL（16 张表）— `backend/scripts/init-schema.sql`
- [X] T008 [P] 字典初始化数据（16 个字典分类 + 所有字典项，含 color_tag）— `backend/scripts/init-dict-data.sql`

---

## Phase 2: Foundational（基础设施）

### 后端基础设施

- [X] T009 [P] QualityApplication.java + CoreEntity.java + ApiResult.java — `backend/src/main/java/com/jhict/quality/`
- [X] T010 [P] ServiceException.java + GlobalExceptionHandler.java — `backend/.../common/exception/`
- [X] T011 [P] MybatisPlusConfig.java（分页插件 + MetaObjectHandler 自动填充）— `backend/.../common/config/`
- [X] T012 SaTokenConfig.java（鉴权拦截器，排除白名单）— `backend/.../common/config/SaTokenConfig.java`
- [X] T013 [P] RedisConfig.java + CorsConfig.java — `backend/.../common/config/`
- [X] T014 [P] Knife4jConfig.java — `backend/.../common/config/Knife4jConfig.java`
- [X] T015 [P] FileStorageUtil.java（本地文件存储，按年月分目录）— `backend/.../common/util/FileStorageUtil.java`
- [X] T016 [P] 所有枚举类（StandardType/StandardStatus/IndicatorCategory/InspectionStatus/JudgmentType/ReinspectionStatus/ApprovalLevel/ApprovalStatus/ConfirmStatus/ConcessionStatus[含SALES_APPROVED]/AuditOperationType 共 11 个）— `backend/.../enums/`
- [X] T017 [P] SysUser entity + SysUserMapper — `backend/.../entity/SysUser.java` + `mapper/`
- [X] T018 AuthService + AuthServiceImpl（Sa-Token login/logout，BCrypt，Redis Token 黑名单）— `backend/.../service/`
- [X] T019 AuthController（POST /api/v1/auth/login｜logout｜/user/info，预留 /sso 返回 501）— `backend/.../controller/AuthController.java`
- [X] T020 [P] SysDict + SysDictItem entity + Mapper — `backend/.../entity/` + `mapper/`
- [X] T021 SysDictService + SysDictServiceImpl（Redis 二级缓存 TTL 10min，is_system 约束）— `backend/.../service/`
- [X] T022 DictController（GET /all｜/items/{dictCode}｜POST /page + ADMIN 写接口：POST/PUT/DELETE）— `backend/.../controller/DictController.java`
- [X] T023 [P] @AuditLog 注解 + AuditLogAspect（@Async 异步写入，失败降级本地日志）— `backend/.../common/annotation/` + `common/aspect/`
- [X] T024 [P] QcAuditLog entity + QcAuditLogMapper（仅 INSERT，Mapper 层禁 UPDATE/DELETE）— `backend/.../entity/` + `mapper/`
- [X] T025 [P] SysNotification entity + SysNotificationMapper（站内消息，FR-016）— `backend/.../entity/SysNotification.java`

### 前端基础设施

- [X] T026 [P] frontend/src/utils/request.ts（Axios 封装：Bearer Token 注入、统一响应处理、4001 跳转登录）
- [X] T027 [P] frontend/src/router/index.ts（懒加载路由，19 条路由按 §4 顺序，beforeEach 鉴权守卫；requiresAdmin 守卫）
- [X] T028 [P] frontend/src/store/auth.ts + `frontend/src/store/dict.ts`（Pinia：authStore/dictStore）
- [X] T029 [P] frontend/src/types/index.ts（全局 TypeScript 类型定义）
- [X] T030 [P] frontend/src/components/base-table/BaseTable.vue（封装 el-table，含字典翻译 + 表头筛选）
- [X] T031 [P] frontend/src/api/auth.ts + `frontend/src/api/dict.ts`
- [X] T032 frontend/src/views/login/index.vue + `frontend/src/layouts/MainLayout.vue`（侧边栏按 §4 顺序 12 菜单，工业精密控制室风格，扫描线纹理，nav-item-sub 缩进子项）

**Checkpoint**: 基础设施就绪，US1–US4 可并行启动

---

## Phase 3: US1 — 标准库维护与自动判定（P1）🎯 MVP

**Goal**: 质量工程师维护标准后录入检验值，系统自动输出带依据的判定结论。覆盖 §4.1 工作台、§4.2 标准库、§4.3 检验录入、§4.4 判定解释。

**Independent Test**: quickstart.md 场景 1 — 维护标准 → 录入检验 → 查看带依据判定结论

### 数据层

- [X] T033 [P] [US1] QcIndicatorItem entity + QcIndicatorItemMapper — `backend/.../entity/QcIndicatorItem.java`
- [X] T034 [P] [US1] QcQualityStandard entity + QcQualityStandardMapper（含三级优先级查询）— `backend/.../entity/QcQualityStandard.java`
- [X] T035 [P] [US1] QcStandardIndicator entity + QcStandardIndicatorMapper — `backend/.../entity/QcStandardIndicator.java`
- [X] T036 [P] [US1] QcInspectionRecord entity + QcInspectionRecordMapper（含 Dashboard 四指标统计查询）— `backend/.../entity/QcInspectionRecord.java`
- [X] T037 [P] [US1] QcInspectionValue entity + QcInspectionValueMapper — `backend/.../entity/QcInspectionValue.java`
- [X] T038 [P] [US1] QcJudgmentResult entity + QcJudgmentResultMapper — `backend/.../entity/QcJudgmentResult.java`
- [X] T039 [P] [US1] QcJudgmentEvidence entity + QcJudgmentEvidenceMapper（快照字段：upper_limit/lower_limit/concession_upper/concession_lower/version_no 冗余存储）— `backend/.../entity/QcJudgmentEvidence.java`
- [X] T040 [P] [US1] StandardGap entity + StandardGapMapper（含 findUnresolvedGap 查询方法）— `backend/.../entity/StandardGap.java`

### 判定引擎

- [X] T041 [US1] JudgmentEngine.java（编排三级 Matcher 责任链，纯计算不调 DB）— `backend/.../engine/JudgmentEngine.java`
- [X] T042 [P] [US1] AbstractStandardMatcher.java（含 CustomerMatcher/EnterpriseMatcher/NationalMatcher）— `backend/.../engine/matcher/`

### §4.2 标准库模块后端

- [X] T043 [P] [US1] IndicatorService + IndicatorServiceImpl（indicator_code 唯一校验，停用前校验引用）— `backend/.../service/`
- [X] T044 [US1] IndicatorController（POST /api/v1/indicators/page，POST，PUT，DELETE 停用，GET /{id}）— `backend/.../controller/IndicatorController.java`
- [X] T045 [US1] StandardService + StandardServiceImpl（时间窗口不重叠校验，发布 needSetExpiryFor 提示，作废不可恢复）— `backend/.../service/`
- [X] T046 [US1] StandardController（POST /api/v1/standards，PUT /{id}，PUT /{id}/publish，PUT /{id}/deprecate，GET /{id}，POST /page）— `backend/.../controller/StandardController.java`
- [X] T047 [US1] StandardGapController（POST /api/v1/standard-gaps/page，PUT /{id}/resolve；联查指标名称）— `backend/.../controller/StandardGapController.java`

### §4.3 检验录入模块后端

- [X] T048 [US1] InspectionService + InspectionServiceImpl（batch_no 服务端推导 batch_no=heat_no；录入触发 JudgmentEngine；软作废校验权限；StandardGap 旁路写入+消息通知）— `backend/.../service/`
- [X] T049 [US1] InspectionController（POST /api/v1/inspections，POST /batch，GET /{id}，POST /page，PUT /{id}/void @AuditLog(VOID_INSPECTION)）— `backend/.../controller/InspectionController.java`

### §4.4 判定解释 + §4.1 工作台后端

- [X] T050 [US1] JudgmentService + JudgmentServiceImpl（判定解释报告聚合；Dashboard 四指标统计 Redis TTL 60s）— `backend/.../service/`
- [X] T051 [US1] JudgmentController（GET /api/v1/judgments/{id}/explanation，POST /page，GET /dashboard/summary）— `backend/.../controller/JudgmentController.java`
- [X] T052 [US1] DashboardController（GET /api/v1/judgments/dashboard/summary）— `backend/.../controller/DashboardController.java`
- [X] T053 [P] [US1] FileController（GET /api/v1/files/**，Sa-Token 鉴权，流式输出）— `backend/.../controller/FileController.java`

### §4.2 标准库前端（路由按 §4 顺序）

- [X] T054 [P] [US1] frontend/src/api/standard.ts + `frontend/src/api/indicator.ts` + `frontend/src/api/standard-gap.ts`（含 PageResult<StandardGapVO> 类型安全）
- [X] T055 [P] [US1] frontend/src/views/standard-lib/index.vue（§4.2.1 标准维护，路由 /standard-lib）
- [X] T056 [P] [US1] frontend/src/views/indicator/index.vue（§4.2.2 指标项目，路由 /standard-lib/indicators，面包屑：标准库›指标项目）
- [X] T057 [P] [US1] frontend/src/views/standard-lib/gaps.vue（§4.2 StandardGap 覆盖缺口列表，路由 /standard-lib/gaps；品种/牌号/状态筛选；指标名称琥珀金徽章；FR-015）

### §4.1 工作台前端

- [X] T058 [US1] frontend/src/views/dashboard/index.vue（§4.1 质量工作台，4 指标卡，路由 /dashboard）

### §4.3–4.4 检验录入 + 判定解释前端

- [X] T059 [P] [US1] frontend/src/api/inspection.ts + `frontend/src/api/judgment.ts`
- [X] T060 [US1] frontend/src/views/inspection/index.vue（§4.3 检验列表，行内作废操作，路由 /inspection）
- [X] T061 [US1] frontend/src/views/inspection/form.vue（§4.3.1 新增/批量录入，路由 /inspection/form）
- [X] T062 [US1] frontend/src/views/judgment/index.vue（§4.4 判定列表，判定结论徽章，路由 /judgment）
- [X] T063 [US1] frontend/src/views/judgment/explanation.vue（§4.4.1 判定解释详情，标准匹配链三步可视化，偏差条，路由 /judgment/explanation）

**Checkpoint**: US1 完成 — 维护标准 → 录入检验 → 查看判定解释全流程可用

---

## Phase 4: US2 — 复检与改判流程（P2）

**Goal**: 发起复检、录入复检结果、改判申请、审批后更新结论。覆盖 §4.5 复检管理、§4.6 改判管理。

**Independent Test**: quickstart.md 场景 2/3 — 复检全流程 + 常规/逆向改判双轨审批

### 数据层

- [X] T064 [P] [US2] QcReinspectionRecord entity + Mapper — `backend/.../entity/QcReinspectionRecord.java`
- [X] T065 [P] [US2] QcRejudgmentRequest entity + Mapper（含 is_reverse 字段）— `backend/.../entity/QcRejudgmentRequest.java`
- [X] T066 [P] [US2] QcRejudgmentApproval entity + Mapper — `backend/.../entity/QcRejudgmentApproval.java`

### §4.5 复检模块

- [X] T067 [US2] ReinspectionService + ReinspectionServiceImpl（前置校验复检次数≤2；完成复检触发重新判定；工作台缓存失效）— `backend/.../service/`
- [X] T068 [US2] ReinspectionController（POST /api/v1/reinspections，PUT /{id}/complete，GET /{id}，POST /page）— `backend/.../controller/ReinspectionController.java`

### §4.6 改判模块

- [X] T069 [US2] RejudgmentService + RejudgmentServiceImpl（is_reverse 矩阵：QUALIFIED/CAN_CONCESSION→任意=逆向，其他=常规；approve() 编排 4 具名私有方法：expireOriginalJudgment/createNewJudgment/invalidateLinkedConcessions/notifySalesIfConcessionConfirmed）— `backend/.../service/`
- [X] T070 [US2] RejudgmentController（POST /api/v1/rejudgments，PUT /{id}/approve @AuditLog(APPROVE_REJUDGMENT)，GET /{id}，POST /page）— `backend/.../controller/RejudgmentController.java`

### §4.5–4.6 前端

- [X] T071 [P] [US2] frontend/src/api/reinspection.ts + `frontend/src/api/rejudgment.ts`
- [X] T072 [US2] frontend/src/views/reinspection/index.vue（§4.5 复检管理，路由 /reinspection）
- [X] T073 [US2] frontend/src/views/re-judgment/index.vue（§4.6 改判列表，逆向标识，路由 /re-judgment）
- [X] T074 [US2] frontend/src/views/re-judgment/form.vue（§4.6.1 新增改判，逆向警告横幅+强制字段，路由 /re-judgment/form）
- [X] T075 [US2] frontend/src/views/re-judgment/detail.vue（§4.6.1 改判详情，路由 /re-judgment/detail）

**Checkpoint**: US2 完成 — 复检 + 改判双轨审批全流程可验证

---

## Phase 5: US3 — 让步接收管理（P3）

**Goal**: 销售发起让步申请→双签审批（SALES_MANAGER 第一签，QUALITY_MANAGER 第二签）→客户书面确认→有效期管理。覆盖 §4.7 让步接收。

**Independent Test**: quickstart.md 场景 4 — 让步全流程 + 附件不可替换约束

### 数据层

- [X] T076 [P] [US3] QcConcessionAcceptance entity + Mapper（双状态机：confirm_status + approval_status 含 SALES_APPROVED 中间态）— `backend/.../entity/QcConcessionAcceptance.java`

### §4.7 让步模块

- [X] T077 [US3] ConcessionService + ConcessionServiceImpl（双签逻辑：SALES_MANAGER 第一签→SALES_APPROVED，QUALITY_MANAGER 第二签→APPROVED；附件不可替换 ServiceAssert；催促阈值 app.concession.remind-days 默认 7 天）— `backend/.../service/`
- [X] T078 [US3] ConcessionController（POST /api/v1/concessions，PUT /{id}/approve（角色感知双签），PUT /{id}/confirm（multipart 附件），PUT /{id}/reject，GET /{id}，POST /page @AuditLog(APPROVE_CONCESSION/CONFIRM_CONCESSION/REJECT_CONCESSION)）— `backend/.../controller/ConcessionController.java`
- [X] T079 [US3] ConcessionScheduler.java（Spring @Scheduled 凌晨 01:00；Redis SET NX 分布式锁；到期 APPROVED→INVALID；PENDING 超阈值催确认）— `backend/.../scheduler/ConcessionScheduler.java`

### §4.7 前端

- [X] T080 [P] [US3] frontend/src/api/concession.ts
- [X] T081 [US3] frontend/src/views/concession/index.vue（§4.7 让步列表，双状态列，路由 /concession）
- [X] T082 [US3] frontend/src/views/concession/detail.vue（§4.7.1 让步详情，4步状态机进度条，有效期倒计时，附件上传区，路由 /concession/detail）

**Checkpoint**: US3 完成 — 让步全流程 + 附件不可替换 + 定时任务到期失效

---

## Phase 6: US4 — 质保书数据汇总与质量统计（P4）

**Goal**: 按批次汇总检验结果用于质保书，统计报表了解不合格率/复检率/让步率/指标异常分布。覆盖 §4.8 质保书数据、§4.9 质量统计。

**Independent Test**: quickstart.md 场景 5 — 质保书汇总 + 统计报表数据合理

### 数据层

- [X] T083 [P] [US4] QcQualityCertData entity + Mapper（snapshot_data LONGTEXT JSON）— `backend/.../entity/QcQualityCertData.java`

### §4.8 质保书模块

- [X] T084 [US4] CertDataService + CertDataServiceImpl（汇总：status=NORMAL → 同指标取最新值 → is_final=1 判定 → JSON 快照；≤10s；APPROVED 让步附加说明）— `backend/.../service/`
- [X] T085 [US4] CertDataController（POST /api/v1/cert-data/generate，GET /{id}，POST /page，GET /{id}/export）— `backend/.../controller/CertDataController.java`

### §4.9 质量统计模块

- [X] T086 [US4] StatisticsService + StatisticsServiceImpl（MySQL 5.7 GROUP BY 聚合；不合格率/复检率/让步率；指标异常分布 COUNT by indicator；ONLY_FULL_GROUP_BY 兼容）— `backend/.../service/`
- [X] T087 [US4] StatisticsController（GET /api/v1/statistics/overview，GET /api/v1/statistics/indicator-distribution）— `backend/.../controller/StatisticsController.java`

### §4.8–4.9 前端（§4.8 在 §4.9 之前，与 §4 文档顺序一致）

- [X] T088 [P] [US4] frontend/src/api/cert-data.ts + `frontend/src/api/statistics.ts`
- [X] T089 [US4] frontend/src/views/cert-data/index.vue（§4.8 质保书数据，路由 /cert-data）
- [X] T090 [US4] frontend/src/views/statistics/index.vue（§4.9 质量统计，ECharts 柱状图，路由 /statistics）

**Checkpoint**: US4 完成 — 质保书汇总 + 统计报表数据合理

---

## Phase 7: Polish — §4.10–4.12 管理后台 + 系统完整性 + UI 优化

### §4.10 权限审计

- [X] T091 AuditService + AuditServiceImpl（分页查询 qc_audit_log）— `backend/.../service/`
- [X] T092 AuditController（POST /api/v1/audit/page，GET /export；无 UPDATE/DELETE）— `backend/.../controller/AuditController.java`
- [X] T093 frontend/src/api/audit.ts + `frontend/src/views/audit/index.vue`（§4.10 审计日志，路由 /audit）

### §4.11 账号管理（ADMIN）

- [X] T094 UserManageController（POST /api/v1/admin/users/page｜POST｜PUT /{id}｜PUT /{id}/status；BCrypt 初始密码；ADMIN 角色守卫）— `backend/.../controller/UserManageController.java`
- [X] T095 frontend/src/api/user-manage.ts（PageResult<UserVO> 类型安全）+ `frontend/src/views/admin/users/index.vue`（§4.11 账号管理，路由 /admin/users）

### §4.12 数据字典管理（ADMIN）

- [X] T096 DictController 写接口扩展（POST /dict，PUT /{id}，DELETE /{id}，POST /items，PUT /items/{id}；is_system=1 约束；Redis 缓存失效）— `backend/.../controller/DictController.java`
- [X] T097 frontend/src/api/dict-manage.ts（PageResult<DictCategoryVO> 类型安全）+ `frontend/src/views/admin/dict/index.vue`（§4.12 数据字典，分栏布局，路由 /admin/dict）

### FR-016 站内消息

- [X] T098 NotificationService + NotificationServiceImpl（未读数查询、分页、标记已读）— `backend/.../service/`
- [X] T099 NotificationController（GET /api/v1/notifications/unread-count，POST /page，PUT /{id}/read，PUT /read-all）— `backend/.../controller/NotificationController.java`
- [X] T100 [P] frontend/src/api/notification.ts + `frontend/src/components/notification-center/NotificationBell.vue`（工作台未读角标 + 消息抽屉）

### §4 菜单结构最终对齐

- [X] T101 frontend/src/layouts/MainLayout.vue 重组（严格按 §4 顺序：工作台→标准库[标准维护+指标项目+覆盖缺口，后两者 nav-item-sub 缩进]→检验与判定→质量流程→数据汇总[4.8先4.9后]→系统管理→管理员）
- [X] T102 frontend/src/router/index.ts（19 条路由按 §4 顺序；standard-lib/indicators、standard-lib/gaps 均已注册；requiresAdmin 守卫）
- [X] T103 [P] nginx/nginx.conf（前后端反向代理：/api/ → backend:8080，/ → frontend build）

### UI 可读性优化

- [X] T104 [P] frontend/src/styles/global.css — 表格表头与表单标签字体调整：`font-size: 9px → 11px`；`color: #3A5B7A → #7A9BBE`（--text-secondary）；`letter-spacing: 0.12em → 0.08em`；表单标签 `font-weight: 400 → 500`；影响全局所有 el-table 表头和 el-form-item 标签

### 待完成

- [ ] T105 quickstart.md 5 个场景端到端验证（按 quickstart.md 场景 1–5 逐一验证，确认 SC-001~SC-006 成功标准达成；需要连接运行中的 MySQL + Redis 环境）

---

## Dependencies & Execution Order

### Phase 依赖关系

- **Setup（Phase 1）**: 无依赖，立即启动
- **Foundational（Phase 2）**: 依赖 Phase 1，BLOCKS 所有用户故事
- **US1（Phase 3）**: 依赖 Phase 2，独立可测试 ✅
- **US2（Phase 4）**: 依赖 Phase 2 + US1 判定结论数据
- **US3（Phase 5）**: 依赖 Phase 2 + US1 CAN_CONCESSION 判定
- **US4（Phase 6）**: 依赖 Phase 2 + US1–US3 数据沉淀
- **Polish（Phase 7）**: 依赖所有故事完成

### 当前状态（2026-05-16）

- **Phase 1–6**: 全部完成 ✅
- **Phase 7 T091–T104**: 全部完成 ✅
- **待完成**: T105（quickstart 端到端验证，需运行环境）

### Parallel Opportunities

```bash
# Phase 2 — 后端/前端基础设施可并行
T009–T015, T017, T020, T023–T025   ← 后端基础设施
T026–T031                           ← 前端基础设施

# Phase 3 US1 数据层全部并行
T033–T040                           ← 8 个 entity+mapper

# Phase 7 各子任务并行
T091–T100                           ← 审计、账号、字典、消息各模块
T101–T104                           ← 菜单/路由/nginx/CSS 独立文件
```

---

## Implementation Strategy

### MVP 路径（US1 only）

1. Phase 1: Setup ✅
2. Phase 2: Foundational ✅
3. Phase 3: US1 ✅（含 StandardGap 缺口视图）
4. 验证: quickstart.md 场景 1

### 完整交付路径

1. US1 → 核心判定能力 ✅
2. US2 → 复检/改判双轨 ✅
3. US3 → 让步双签闭环 ✅
4. US4 → 质保书+统计 ✅
5. Polish → 管理后台+消息+菜单+UI优化 ✅
6. **T105** → quickstart 端到端验证（最终验收）

---

## Notes

- §4 菜单顺序已对齐：工作台→标准库[子项缩进+覆盖缺口]→检验判定→质量流程→数据汇总[4.8先]→系统管理→管理员
- is_reverse 矩阵：原结论为 QUALIFIED/CAN_CONCESSION → 无论目标 → 逆向；其余 → 常规
- 让步双签：SALES_APPROVED 中间态；ServiceAssert 拦截越权调用
- RejudgmentService.approve()：4 具名私有方法编排（Principle III）
- batch_no 服务端推导：`record.setBatchNo(record.getHeatNo())`
- 催促提醒阈值：`app.concession.remind-days`，默认 7 天
- 全局 CSS 已修复：表格表头和表单标签 9px→11px，--text-muted→--text-secondary
