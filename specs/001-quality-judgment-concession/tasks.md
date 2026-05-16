---
description: "Task list for 质量判定解释与让步管理系统"
---

# Tasks: 质量判定解释与让步管理系统

**Input**: Design documents from `specs/001-quality-judgment-concession/`
**Design Reference**: QC-DDS-001 §4（12 个子系统模块，严格按 §4 顺序）
**Status**: Session 2026-05-16 澄清新增 5 个待实现任务（T106–T110）

## Format: `[ID] [P?] [Story?] Description — file path`

- **[P]**: 可并行（不同文件）
- **[X]**: 已完成；**[ ]**: 待完成

---

## Phase 1: Setup（项目初始化）

- [X] T001 创建 Spring Boot 2.7.x Maven 单模块项目 — `backend/pom.xml`
- [X] T002 [P] 配置 pom.xml 所有依赖 — `backend/pom.xml`
- [X] T003 [P] 创建 application.yml — `backend/src/main/resources/application.yml`
- [X] T004 [P] 创建 application-dev.yml — `backend/src/main/resources/application-dev.yml`
- [X] T005 [P] 创建 application-prod.yml — `backend/src/main/resources/application-prod.yml`
- [X] T006 用 Vite 创建 Vue 3 + TypeScript 前端项目 — `frontend/package.json` + `vite.config.ts`
- [X] T007 创建数据库 DDL（16 张表） — `backend/scripts/init-schema.sql`
- [X] T008 [P] 字典初始化数据 — `backend/scripts/init-dict-data.sql`

---

## Phase 2: Foundational（基础设施）

### 后端基础设施

- [X] T009 [P] QualityApplication.java + CoreEntity.java + ApiResult.java — `backend/src/main/java/com/jhict/quality/`
- [X] T010 [P] ServiceException.java + GlobalExceptionHandler.java — `backend/.../common/exception/`
- [X] T011 [P] MybatisPlusConfig.java — `backend/.../common/config/`
- [X] T012 SaTokenConfig.java — `backend/.../common/config/SaTokenConfig.java`
- [X] T013 [P] RedisConfig.java + CorsConfig.java — `backend/.../common/config/`
- [X] T014 [P] Knife4jConfig.java — `backend/.../common/config/Knife4jConfig.java`
- [X] T015 [P] FileStorageUtil.java — `backend/.../common/util/FileStorageUtil.java`
- [X] T016 [P] 所有枚举类（11 个，含 ConcessionStatus.SALES_APPROVED） — `backend/.../enums/`
- [X] T017 [P] SysUser entity + SysUserMapper — `backend/.../entity/`
- [X] T018 AuthService + AuthServiceImpl — `backend/.../service/`
- [X] T019 AuthController — `backend/.../controller/AuthController.java`
- [X] T020 [P] SysDict + SysDictItem entity + Mapper — `backend/.../entity/`
- [X] T021 SysDictService + SysDictServiceImpl — `backend/.../service/`
- [X] T022 DictController（含 ADMIN 写接口） — `backend/.../controller/DictController.java`
- [X] T023 [P] @AuditLog 注解 + AuditLogAspect — `backend/.../common/annotation/` + `common/aspect/`
- [X] T024 [P] QcAuditLog entity + QcAuditLogMapper — `backend/.../entity/`
- [X] T025 [P] SysNotification entity + SysNotificationMapper — `backend/.../entity/SysNotification.java`

### 前端基础设施

- [X] T026 [P] frontend/src/utils/request.ts
- [X] T027 [P] frontend/src/router/index.ts（19 路由，§4 顺序，requiresAdmin 守卫）
- [X] T028 [P] frontend/src/store/auth.ts + `frontend/src/store/dict.ts`
- [X] T029 [P] frontend/src/types/index.ts
- [X] T030 [P] frontend/src/components/base-table/BaseTable.vue
- [X] T031 [P] frontend/src/api/auth.ts + `frontend/src/api/dict.ts`
- [X] T032 frontend/src/views/login/index.vue + `frontend/src/layouts/MainLayout.vue`（§4 顺序，nav-item-sub 缩进）

**Checkpoint**: 基础设施就绪

---

## Phase 3: US1 — 标准库维护与自动判定（P1）

**Goal**: 维护标准后录入检验值，系统自动输出带依据的判定结论（四段优先级）

**Independent Test**: quickstart.md 场景 1

### 数据层

- [X] T033 [P] [US1] QcIndicatorItem entity + Mapper — `backend/.../entity/QcIndicatorItem.java`
- [X] T034 [P] [US1] QcQualityStandard entity + Mapper — `backend/.../entity/QcQualityStandard.java`
- [X] T035 [P] [US1] QcStandardIndicator entity + Mapper — `backend/.../entity/QcStandardIndicator.java`
- [X] T036 [P] [US1] QcInspectionRecord entity + Mapper — `backend/.../entity/QcInspectionRecord.java`
- [X] T037 [P] [US1] QcInspectionValue entity + Mapper — `backend/.../entity/QcInspectionValue.java`
- [X] T038 [P] [US1] QcJudgmentResult entity + Mapper — `backend/.../entity/QcJudgmentResult.java`
- [X] T039 [P] [US1] QcJudgmentEvidence entity + Mapper（快照字段冗余存储） — `backend/.../entity/QcJudgmentEvidence.java`
- [X] T040 [P] [US1] StandardGap entity + Mapper — `backend/.../entity/StandardGap.java`

### 判定引擎

- [X] T041 [US1] JudgmentEngine.java（三级 Matcher 责任链，纯计算） — `backend/.../engine/JudgmentEngine.java`
- [X] T042 [P] [US1] AbstractStandardMatcher.java + 3 Matchers — `backend/.../engine/matcher/`

### §4.2 标准库模块

- [X] T043 [P] [US1] IndicatorService + IndicatorServiceImpl — `backend/.../service/`
- [X] T044 [US1] IndicatorController — `backend/.../controller/IndicatorController.java`
- [X] T045 [US1] StandardService + StandardServiceImpl — `backend/.../service/`
- [X] T046 [US1] StandardController — `backend/.../controller/StandardController.java`
- [X] T047 [US1] StandardGapController（POST /page，PUT /{id}/resolve） — `backend/.../controller/StandardGapController.java`

### §4.3 检验录入模块

- [X] T048 [US1] InspectionService + InspectionServiceImpl（batch_no=heat_no，判定触发，StandardGap 旁路） — `backend/.../service/`
- [X] T049 [US1] InspectionController — `backend/.../controller/InspectionController.java`

### §4.4 判定解释 + §4.1 工作台

- [X] T050 [US1] JudgmentService + JudgmentServiceImpl（判定解释报告 + Dashboard 统计 Redis TTL 60s） — `backend/.../service/`
- [X] T051 [US1] JudgmentController — `backend/.../controller/JudgmentController.java`
- [X] T052 [US1] DashboardController — `backend/.../controller/DashboardController.java`
- [X] T053 [P] [US1] FileController — `backend/.../controller/FileController.java`

### §4.2 标准库前端

- [X] T054 [P] [US1] frontend/src/api/standard.ts + `indicator.ts` + `standard-gap.ts`
- [X] T055 [P] [US1] frontend/src/views/standard-lib/index.vue（§4.2.1）
- [X] T056 [P] [US1] frontend/src/views/indicator/index.vue（§4.2.2，路由 /standard-lib/indicators）
- [X] T057 [P] [US1] frontend/src/views/standard-lib/gaps.vue（§4.2.3，FR-015，路由 /standard-lib/gaps）

### §4.1 工作台前端

- [X] T058 [US1] frontend/src/views/dashboard/index.vue

### §4.3–4.4 检验录入 + 判定解释前端

- [X] T059 [P] [US1] frontend/src/api/inspection.ts + `judgment.ts`
- [X] T060 [US1] frontend/src/views/inspection/index.vue（§4.3）
- [X] T061 [US1] frontend/src/views/inspection/form.vue（§4.3.1 新增，含 product_spec 字段）
- [X] T062 [US1] frontend/src/views/judgment/index.vue（§4.4）
- [X] T063 [US1] frontend/src/views/judgment/explanation.vue（§4.4.1）

**Checkpoint**: US1 完成 — 维护标准 → 录入检验 → 查看判定解释

---

## Phase 4: US2 — 复检与改判流程（P2）

**Goal**: 复检全流程 + 改判双轨审批

**Independent Test**: quickstart.md 场景 2/3

### 数据层

- [X] T064 [P] [US2] QcReinspectionRecord entity + Mapper — `backend/.../entity/`
- [X] T065 [P] [US2] QcRejudgmentRequest entity + Mapper — `backend/.../entity/`
- [X] T066 [P] [US2] QcRejudgmentApproval entity + Mapper — `backend/.../entity/`

### §4.5 复检 + §4.6 改判

- [X] T067 [US2] ReinspectionService + ReinspectionServiceImpl（复检次数≤2） — `backend/.../service/`
- [X] T068 [US2] ReinspectionController — `backend/.../controller/ReinspectionController.java`
- [X] T069 [US2] RejudgmentService + RejudgmentServiceImpl（is_reverse 矩阵，4 具名私有方法） — `backend/.../service/`
- [X] T070 [US2] RejudgmentController — `backend/.../controller/RejudgmentController.java`

### 前端

- [X] T071 [P] [US2] frontend/src/api/reinspection.ts + `rejudgment.ts`
- [X] T072 [US2] frontend/src/views/reinspection/index.vue（§4.5）
- [X] T073 [US2] frontend/src/views/re-judgment/index.vue（§4.6）
- [X] T074 [US2] frontend/src/views/re-judgment/form.vue（逆向改判警告横幅）
- [X] T075 [US2] frontend/src/views/re-judgment/detail.vue

**Checkpoint**: US2 完成

---

## Phase 5: US3 — 让步接收管理（P3）

**Goal**: 让步双签 + 附件不可替换约束

**Independent Test**: quickstart.md 场景 4

### 数据层

- [X] T076 [P] [US3] QcConcessionAcceptance entity + Mapper（含 SALES_APPROVED 中间态） — `backend/.../entity/`

### §4.7 让步

- [X] T077 [US3] ConcessionService + ConcessionServiceImpl（双签：SALES_MANAGER→SALES_APPROVED，QUALITY_MANAGER→APPROVED） — `backend/.../service/`
- [X] T078 [US3] ConcessionController（@AuditLog：APPROVE/CONFIRM/REJECT） — `backend/.../controller/ConcessionController.java`
- [X] T079 [US3] ConcessionScheduler.java（到期失效 + 催确认，Redis SET NX 锁） — `backend/.../scheduler/`

### 前端

- [X] T080 [P] [US3] frontend/src/api/concession.ts
- [X] T081 [US3] frontend/src/views/concession/index.vue（§4.7）
- [X] T082 [US3] frontend/src/views/concession/detail.vue（4 步状态机进度条）

**Checkpoint**: US3 完成

---

## Phase 6: US4 — 质保书数据汇总与质量统计（P4）

**Goal**: 质保书汇总（关键指标：COMPOSITION+PERFORMANCE+DIMENSION） + 统计报表

**Independent Test**: quickstart.md 场景 5

### 数据层

- [X] T083 [P] [US4] QcQualityCertData entity + Mapper — `backend/.../entity/`

### §4.8 质保书 + §4.9 质量统计

- [X] T084 [US4] CertDataService + CertDataServiceImpl（汇总逻辑，≤10s） — `backend/.../service/`
- [X] T085 [US4] CertDataController — `backend/.../controller/CertDataController.java`
- [X] T086 [US4] StatisticsService + StatisticsServiceImpl（MySQL 5.7 GROUP BY 聚合） — `backend/.../service/`
- [X] T087 [US4] StatisticsController — `backend/.../controller/StatisticsController.java`

### 前端

- [X] T088 [P] [US4] frontend/src/api/cert-data.ts + `statistics.ts`
- [X] T089 [US4] frontend/src/views/cert-data/index.vue（§4.8）
- [X] T090 [US4] frontend/src/views/statistics/index.vue（§4.9，ECharts 柱状图）

**Checkpoint**: US4 完成

---

## Phase 7: Polish — §4.10–4.12 管理后台 + 系统完整性

### §4.10–4.12 管理后台

- [X] T091 AuditService + AuditController — `backend/.../controller/AuditController.java`
- [X] T092 frontend/src/views/audit/index.vue（§4.10）
- [X] T093 UserManageController（BCrypt，ADMIN 守卫） — `backend/.../controller/UserManageController.java`
- [X] T094 frontend/src/views/admin/users/index.vue（§4.11）
- [X] T095 DictController 写接口扩展 — `backend/.../controller/DictController.java`
- [X] T096 frontend/src/views/admin/dict/index.vue（§4.12）

### FR-016 站内消息

- [X] T097 NotificationService + NotificationController — `backend/.../controller/NotificationController.java`
- [X] T098 [P] frontend/src/api/notification.ts + `components/notification-center/NotificationBell.vue`

### §4 菜单结构最终对齐

- [X] T099 frontend/src/layouts/MainLayout.vue 重组（§4 顺序，nav-item-sub 缩进，覆盖缺口菜单子项）
- [X] T100 frontend/src/router/index.ts（19 路由，standard-lib/indicators + standard-lib/gaps）
- [X] T101 [P] nginx/nginx.conf（前后端反向代理）

### 前端列筛选（FR-018）

- [X] T102 frontend/src/composables/use-table-filter.ts（useTableFilter composable）
- [X] T103 frontend/src/styles/global.css — El Plus 列筛选面板深色主题 + 13 个视图添加 :filters/:filter-method

### UI 可读性

- [X] T104 [P] frontend/src/styles/global.css — 表格表头与表单标签 9px→11px，#3A5B7A→#7A9BBE

---

## Phase 8: Session 2026-05-16 新增需求（D-015 / D-016 / D-017 / D-019）

> 以下 5 个任务来自 speckit-clarify Session 2026-05-16 的澄清，是对现有实现的补充/修正。

### D-015：判定引擎四段优先级修正

- [ ] T106 [US1] 修复 JudgmentEngine.java 判定逻辑，添加 NEED_REINSPECTION 触发：超出合格限且 concessionUpper/concessionLower 均为 NULL 时设 `hasReinspection=true`；更新 Step 5 优先级为 UNQUALIFIED > NEED_REINSPECTION > CAN_CONCESSION > QUALIFIED；相应更新 `buildReinspectionRule()` 触发规则描述 — `backend/src/main/java/com/jhict/quality/engine/JudgmentEngine.java`

### D-016：检验录入 product_spec 下拉 UI 约束

- [ ] T107 [US1] 在 StandardController 新增接口 `GET /api/v1/standards/spec-ranges?variety=&grade=&customerId=`，返回标准库中按 variety+grade+customerId 过滤的有效 spec_range 去重列表（含版本号），供检验录入规格下拉使用 — `backend/src/main/java/com/jhict/quality/controller/StandardController.java`
- [ ] T108 [P] [US1] 在 frontend/src/api/standard.ts 添加 `getSpecRanges(params)` 方法；更新 frontend/src/views/inspection/form.vue 将 product_spec 字段从文本输入改为 `el-select`，数据源调用 `getSpecRanges`，随 customer/variety/grade 变化动态加载，显示格式 "厚度 X.Xmm / 宽度 XXXXmm (版本号)" — `frontend/src/api/standard.ts` + `frontend/src/views/inspection/form.vue`

### D-017：质保书关键指标按类别过滤

- [ ] T109 [US4] 更新 CertDataServiceImpl 汇总逻辑：在聚合 InspectionValue 时按 indicator_category 过滤，默认仅纳入 COMPOSITION/PERFORMANCE/DIMENSION 三类；从系统配置（application.yml `app.cert.included-categories`，默认 `COMPOSITION,PERFORMANCE,DIMENSION`）读取配置，支持 ADMIN 通过配置调整 — `backend/src/main/java/com/jhict/quality/service/impl/CertDataServiceImpl.java` + `backend/src/main/resources/application.yml`

### D-019：结构化 JSON 日志（FR-019）

- [ ] T110 [P] 创建 logback-spring.xml：配置 LogstashEncoder 输出结构化 JSON；添加 `FILE_ERROR` Appender（WARN 及以上写入 `/var/log/qc/application-error.log`，按日滚动，保留 30 天）；在 pom.xml 添加 `logstash-logback-encoder` 依赖（版本 7.x）；创建 MdcTraceFilter.java（Servlet Filter，每请求生成 UUID 注入 MDC traceId，请求结束后 MDC.remove） — `backend/src/main/resources/logback-spring.xml` + `backend/pom.xml` + `backend/src/main/java/com/jhict/quality/common/filter/MdcTraceFilter.java`

---

### 最终验收

- [ ] T111 quickstart.md 5 个场景端到端验证（场景 1–5 逐一验证，确认 SC-001~SC-008 成功标准达成；需要连接运行中的 MySQL + Redis 环境）

---

## Dependencies & Execution Order

- **Phase 8 任务** 均可在 Phase 7 完成后并行执行：
  - T106（JudgmentEngine）← 独立，无依赖
  - T107（spec-ranges API）← StandardController 已存在
  - T108（inspection form 下拉）← 依赖 T107
  - T109（CertData 类别过滤）← CertDataServiceImpl 已存在
  - T110（logback）← 独立

- **T111**（quickstart 验证）← 依赖 T106–T110 全部完成

### 当前状态（2026-05-16）

- Phase 1–7: **全部完成** ✅（T001–T104）
- Phase 8: **5 个新任务待实现**（T106–T110）
- 最终验收: T111 待完成

---

## Notes

- **D-015 核心修改**：JudgmentEngine.java Step 5 需新增 `boolean hasReinspection` flag；当 else 分支中 concessionUpper/Lower 均为 NULL 时设 `hasReinspection=true`；当有 concession range 但实测值超出时才设 `hasUnqualified=true`
- **D-016 检验表单**：product_spec el-select，watchEffect 监听 customer/variety/grade 变化重新加载选项
- **D-017 质保书**：从 application.yml 读 `app.cert.included-categories`，@Value 注入，逻辑为 `indicatorCategory IN (配置列表)`
- **D-019 logback**：需在 pom.xml 添加 `net.logstash.logback:logstash-logback-encoder:7.4`；MdcTraceFilter 注册为 `@Component`（Spring Boot 自动注入 Servlet Filter）
