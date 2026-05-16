# Implementation Plan: 质量判定解释与让步管理系统

**Branch**: `001-quality-judgment-concession` | **Date**: 2026-05-16 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/001-quality-judgment-concession/spec.md`
**Detailed Design Reference**: QC-DDS-001 §4
**Clarifications Applied**: Session 2026-05-14, 2026-05-15, 2026-05-16 (5 new questions resolved)

## Summary

构建一套前后端分离的钢铁产品质量判定解释与让步管理系统（ai-quality-control）。
核心能力：按客户协议>企标>国标三级优先级匹配标准，自动输出带依据的判定结论（四段优先级：
QUALIFIED > CAN_CONCESSION > NEED_REINSPECTION > UNQUALIFIED），支持复检/改判（含逆向改判）/
让步接收（双签状态机）全生命周期管理，SLF4J + Logback 结构化 JSON 日志实现可观测性（FR-019）。

---

## Technical Context

**Language/Version**:
- 后端：Java 8 + Spring Boot 2.7.x（单体应用，不使用 Spring Cloud）
- 前端：TypeScript 4.x + Vue 3 + Element Plus

**Primary Dependencies**:
- 后端：MyBatis-Plus 3.x、Knife4j、Lombok、Sa-Token、Spring Boot Actuator（`/actuator/health`）、Spring Scheduler
- 前端：Vue Router 4、Pinia、Axios、Element Plus、dayjs、ECharts
- 日志：SLF4J + Logback（FR-019：结构化 JSON + MDC traceId + File Appender）

**Storage**:
- MySQL 5.7.43（utf8mb4_unicode_ci；无 CTE / Window Functions）
- Redis 8.0.5（JWT 黑名单、Dashboard 缓存 TTL 60s、字典缓存 TTL 10min、分布式锁）

**Performance Goals**:
- 检验录入到判定结论 ≤ 3 分钟（SC-001）
- 质量工作台首页加载 ≤ 3 秒（SC-004）
- 质保书汇总 ≤ 10 秒（SC-005）

**Constraints**:
- MySQL 5.7.43：不支持 CTE / 窗口函数
- 初期用户规模 50–200 人，并发判定请求峰值 ≤ 100 次/分钟
- 可用性：RTO ≤ 30 分钟，RPO = 0（SC-008）
- product_spec 字段：**强制下拉选择，禁止自由文本输入**（FR-004 UI 约束，Session 2026-05-16）

---

## Constitution Check

| # | Gate | Status |
|---|------|--------|
| 1 | **命名规范 (Principle I)**: 大驼峰类名、小驼峰方法/变量、全大写常量、无魔法值 | ✅ |
| 2 | **方法规模 (Principle II)**: 方法 ≤160 行、嵌套 ≤3 层 | ✅ |
| 3 | **Service入口编排 (Principle III)**: JudgmentEngine.judge() 只编排，approve() 下沉 4 具名私有方法 | ✅ |
| 4 | **外部依赖收敛 (Principle III)**: DB/Redis 收敛专门方法；ServiceAssert 统一校验 | ✅ |
| 5 | **前端规范 (Principle IV)**: Vue 多词命名、scoped 样式、Props 完整类型 | ✅ |
| 6 | **可测试性 (Principle V)**: 判定引擎纯计算可单测；Matcher 独立可 mock | ✅ |
| 7 | **安全 (Principle V)**: BCrypt 密码哈希、敏感字段过滤、SQL 参数化 | ✅ |
| 8 | **技术栈合规**: Spring Boot 2.7.x + MySQL 5.7.43 + Redis 8.0.5 + Vue3/Element Plus | ✅ |

---

## 关键设计决策（Session 2026-05-16 新增）

### D-015：判定结论四段优先级（FR-005 权威定义）

判定引擎对每条指标按以下互斥顺序判断：

| 优先级 | 条件 | 结论 |
|--------|------|------|
| 1 | 实测值在合格限内 | QUALIFIED |
| 2 | 超出合格限 + 在让步范围内（concession_upper/lower 非 NULL） | CAN_CONCESSION |
| 3 | 超出合格限 + **未配置让步范围**（concession_upper/lower 均为 NULL） | NEED_REINSPECTION |
| 4 | 超出合格限 + 超出让步范围 | UNQUALIFIED |

多指标并存时，取最严重结论（UNQUALIFIED > NEED_REINSPECTION > CAN_CONCESSION > QUALIFIED）。

### D-016：product_spec 规格选择方式（FR-004 UI 约束）

- 检验录入界面 product_spec **强制下拉选择框**，禁止自由文本输入
- 数据源：标准库按已选客户+品种+牌号动态过滤的有效 spec_range 去重列表
- 显示格式："厚度 1.2–2.0mm / 宽度 900–1250mm (GB/T 700-2019)"
- 选定后快照存入检验记录，历史判定不受标准库后续变更影响

### D-017：质保书关键指标范围（FR-010 权威定义）

- **默认纳入**：COMPOSITION（成分）、PERFORMANCE（性能）、DIMENSION（尺寸）
- **默认排除**：SURFACE（表面）、SHAPE（外形）
- ADMIN 可通过系统配置动态调整

### D-018：可用性目标（SC-008）

- 单点部署，故障时展示维护提示页
- RTO ≤ 30 分钟，RPO = 0（MySQL 事务保证）
- 恢复后自动恢复，无需人工干预

### D-019：可观测性规范（FR-019）

- **SLF4J + Logback 结构化 JSON 日志**
- WARN：可恢复边界；ERROR：需人工干预
- MDC 每请求自动注入 traceId，同一请求日志可串联
- File Appender：`/var/log/qc/application-error.log`，按日滚动 30 天
- 定时任务：开始/结束 INFO + 异常 ERROR（含执行时长）
- `/actuator/health` 端点暴露

---

## 前端菜单层级结构（严格按 §4 顺序）

```text
├── §4.1  质量工作台            → /dashboard
├── [标准库]
│   ├── §4.2.1 标准维护         → /standard-lib
│   ├── §4.2.2 指标项目         → /standard-lib/indicators  ← 缩进子项
│   └── §4.2.3 覆盖缺口         → /standard-lib/gaps        ← 缩进子项（FR-015）
├── [检验与判定]
│   ├── §4.3   检验录入         → /inspection
│   └── §4.4   判定解释         → /judgment
├── [质量流程]
│   ├── §4.5   复检管理         → /reinspection
│   ├── §4.6   改判管理         → /re-judgment
│   └── §4.7   让步接收         → /concession
├── [数据汇总]
│   ├── §4.8   质保书数据       → /cert-data   ← 4.8 在 4.9 之前
│   └── §4.9   质量统计         → /statistics
├── [系统管理]
│   └── §4.10  权限审计         → /audit
└── [管理员（ADMIN only）]
    ├── §4.11  账号管理         → /admin/users
    └── §4.12  数据字典         → /admin/dict
```

---

## 判定引擎（§3.5）

```
JudgmentEngine.judge(inspectionRecord, inspectionValues)
  ├── Step 1: 三级优先级查询命中标准
  │   CustomerStandardMatcher → EnterpriseStandardMatcher → NationalStandardMatcher
  ├── Step 2: 逐指标四段互斥判断（D-015）
  │   ① 合格限内 → QUALIFIED
  │   ② 超合格限 + 在让步范围内 → CAN_CONCESSION
  │   ③ 超合格限 + 无让步范围 → NEED_REINSPECTION
  │   ④ 超合格限 + 超让步范围 → UNQUALIFIED
  ├── Step 3: 多标准合并（AND 逻辑，任一不满足则不合格）
  ├── Step 4: 多指标取最严重结论
  └── Step 5: 快照 JudgmentEvidence（冗余存储）
```

---

## Project Structure

```text
backend/
└── src/main/
    ├── java/com/jhict/quality/
    │   ├── common/        # CoreEntity, ApiResult, AuditLog AOP
    │   ├── config/        # MybatisPlus, SaToken, Redis, CORS, Knife4j
    │   ├── engine/        # JudgmentEngine + 3 Matchers（四段优先级实现）
    │   ├── controller/    # 18 Controllers
    │   ├── service/       # api/ + impl/
    │   ├── entity/        # 16 entities (含 StandardGap)
    │   ├── enums/         # 11 enums (含 ConcessionStatus.SALES_APPROVED)
    │   └── scheduler/     # ConcessionScheduler
    └── resources/
        ├── application*.yml
        └── logback-spring.xml  # 结构化 JSON + File Appender (FR-019)

frontend/src/
    ├── api/               # 16 API 模块
    ├── composables/       # use-table-filter.ts（前端列筛选）
    ├── styles/            # global.css（深色主题 + 列筛选面板）
    └── views/
        ├── standard-lib/  # §4.2.1 + gaps.vue（§4.2.3，FR-015）
        ├── indicator/     # §4.2.2（路由 /standard-lib/indicators）
        ├── inspection/    # §4.3（product_spec 下拉，D-016）
        └── admin/         # users/ + dict/（ADMIN only）
```

---

## Complexity Tracking

| 模块 | 复杂度 | 应对措施 |
|------|--------|---------|
| 判定四段优先级 | NEED_REINSPECTION vs CAN_CONCESSION 依赖让步范围配置 | D-015 四段互斥顺序，快照让步范围 |
| 逆向改判 | UI 前置校验 + 双级审批 + 联动让步失效 | is_reverse 矩阵 + 4 具名私有方法 |
| 让步双签 | SALES_APPROVED 中间态 | ServiceAssert 按角色拦截越权 |
| product_spec 匹配 | 下拉选择，无需区间解析 | 数据源动态过滤自标准库（D-016） |
| 质保书指标范围 | 按 indicator_category 过滤 | ADMIN 可配置，默认 3 类纳入（D-017） |
| 可观测性 | 结构化 JSON + MDC | logback-spring.xml 配置（D-019） |
| MySQL 5.7 约束 | 无 CTE/窗口函数 | 子查询替代，严格 GROUP BY |

---

## 设计产出物清单

| 文件 | 状态 | 说明 |
|------|------|------|
| spec.md | ✅ | FR-001~FR-019；三轮澄清编码（含 Session 2026-05-16 5 条） |
| research.md | ✅ | D-000~D-014 技术决策 |
| data-model.md | ✅ | 16 张表，枚举（SALES_APPROVED），状态机 |
| contracts/ | ✅ | 10 个 API 分组合同（含 message.md） |
| quickstart.md | ✅ | 5 个集成测试场景 |
| tasks.md | ✅ | 105 个任务（104 完成，T105 待验证） |
| checklists/business.md | ✅ | 40 条业务需求质量检查单 |
