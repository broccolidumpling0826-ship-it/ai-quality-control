# Research: 质量判定解释与让步管理系统

**Feature**: 001-quality-judgment-concession
**Date**: 2026-05-15

## 决策记录

### D-000：后端架构选型 — Spring Boot 单体（非 Spring Cloud）

**Decision**: Spring Boot 2.7.x 单体应用

**Rationale**:

| 维度 | Spring Cloud | Spring Boot 单体 | 本项目选择 |
|------|-------------|-----------------|-----------|
| 用户规模 | 万级+ | 百级-千级 | 50–200 人 → **单体** |
| 服务间 RPC | 多服务 Feign 调用 | 无 | 无跨服务调用 → **单体** |
| 注册中心 | Nacos/Eureka 必须 | 不需要 | 无必要 → **单体** |
| 网关 | Spring Cloud Gateway | Nginx 足够 | Nginx 直接代理 → **单体** |
| 部署复杂度 | 高（多进程） | 低（一个 jar） | 降低运维成本 → **单体** |
| 开发效率 | 低（多模块联调） | 高（本地一键启动） | 快速迭代 → **单体** |

**认证框架**：使用 Sa-Token 替代 Spring Security + JWT 手写配置：
- Sa-Token 开箱即用，10 行配置实现登录、权限校验、Token 管理、踢人下线
- 与 Spring Boot 集成简单，不需要 WebSecurityConfigurerAdapter 继承链
- 内置 Redis 集成（Sa-Token-Dao-Redis），Token 存 Redis 天然支持分布式会话

**未来扩展路径**（如业务增长）：
- 单体 → 拆出独立的 Auth 服务 → 接入 Spring Cloud Gateway → 完整微服务
- 包名设计（`com.jhict.quality.*`）与 Spring Cloud 兼容，拆分时改动最小

**Alternatives considered**:
- Spring Cloud Alibaba：功能完备但 Nacos + Gateway + Auth 服务运维成本高，当前规模不合算。

---

### D-001：ORM 框架选型

**Decision**: MyBatis-Plus 3.x

**Rationale**:
- 与用户提供的 `CoreEntity`（`@TableId`、`@TableField(fill=...)` 注解）天然兼容。
- `BaseMapper<T>` 提供标准 CRUD，减少重复代码。
- `MetaObjectHandler` 实现 `createUserNo`/`updateUserNo`/`createDateTime`/`updateDateTime`
  自动填充，无需在业务代码中手动赋值。
- 分页插件（`MybatisPlusInterceptor`）开箱即用。

**Alternatives considered**:
- JPA/Hibernate：注解冲突（`@TableId` vs `@Id`），与现有 CoreEntity 不兼容。
- 原生 MyBatis：需手写大量 XML，维护成本高。

---

### D-002：微服务注册/配置中心

**Decision**: Spring Cloud Alibaba Nacos（注册中心 + 配置中心二合一）

**Rationale**:
- 与 Spring Cloud 生态无缝集成，学习曲线低。
- 配置热更新：标准有效日期窗口校验规则等可通过 Nacos 动态调整，无需重启服务。
- 初期单节点部署，后续可升级为集群。

**Alternatives considered**:
- Consul：配置热更新需额外集成，复杂度高。
- Eureka + Config Server：两套系统，运维负担重。

---

### D-003：判定引擎架构

**Decision**: 独立包 `engine/`，策略模式 + 责任链

**Rationale**:
- 三级优先级匹配（客协 > 企标 > 国标）天然适合责任链模式：
  `CustomerStandardMatcher → EnterpriseStandardMatcher → NationalStandardMatcher`。
- 每个 Matcher 独立可单测，符合宪法 Principle V。
- 结论枚举（`JudgmentType`）驱动状态流转，禁止字面量比较（宪法 Principle III）。
- 引擎只返回判定结论 + 依据列表（纯计算），不直接调用 DB，依赖由 Service 注入（便于 mock）。

**Alternatives considered**:
- 规则引擎（Drools）：学习成本高，过度设计（宪法 Principle V 禁止过度设计）。
- 存储过程：逻辑与 DB 耦合，不可单测。

---

### D-004：文件存储（让步接收附件 / 改判证据附件）

**Decision**: 本地文件系统存储，Spring Boot 提供下载接口，**不引入 MinIO / OSS**

**项目定位**: 本系统为 Demo 雏形，文件存储采用最简方案，不做生产级容灾设计。

**具体方案**:
```
# 文件存储目录（application.yml 配置）
app:
  upload:
    base-path: /opt/quality-control/uploads   # 服务器本地路径
    url-prefix: /api/v1/files                 # 访问前缀（通过 Controller 下载）
```

**目录结构**:
```
/opt/quality-control/uploads/
├── concession/          # 让步接收客户确认附件
│   └── {year}/{month}/  # 按年月分目录，防止单目录文件过多
├── rejudgment/          # 逆向改判证据附件
│   └── {year}/{month}/
└── temp/                # 临时文件（上传中转）
```

**文件名策略**: `{UUID}_{原始文件名}`，DB 存相对路径（如 `concession/2026/05/abc123_客户确认.pdf`）

**下载接口**: `GET /api/v1/files/{category}/{year}/{month}/{filename}`，Sa-Token 鉴权后
通过 `response.getOutputStream()` 流式输出，不暴露服务器真实路径。

**附件不可替换约束**: 应用层 `ServiceAssert` 拦截，DB 字段写入后拒绝更新请求。

**不做的事（Demo 阶段）**: 不做文件容灾备份、不做防病毒扫描、不限制文件类型（仅限大小 ≤ 20MB）。

---

### D-005：让步到期与超时提醒

**Decision**: Spring `@Scheduled` + Redis 分布式锁

**Rationale**:
- 每天定时（如凌晨 01:00）扫描 `qc_concession_acceptance`，检查有效期到期和
  `confirm_status = PENDING` 超过阈值的记录。
- Redis SET NX 保证多节点部署时定时任务只执行一次。
- 提醒通知写入系统消息表（初期），后续可对接邮件/企微。

**Alternatives considered**:
- Quartz 分布式调度：功能重，初期 50–200 用户不必要。
- 数据库触发器：难以单测，与宪法 Principle V 冲突。

---

### D-006：Dashboard 统计缓存

**Decision**: Redis Hash 缓存，TTL 60 秒，手动失效策略

**Rationale**:
- Dashboard 4 个指标（待判/不合格/复检/让步）每次查询走全表聚合，高并发下有性能风险。
- TTL 60 秒满足"实时性"要求（用户感知不到 1 分钟延迟），大幅减少 DB 压力。
- 当相关业务操作（检验录入、判定、让步状态变更）发生时，主动删除对应 Redis key，
  下次查询重建缓存。

**Alternatives considered**:
- 实时查询：并发高时 DB 压力大。
- MQ 异步更新：复杂度过高。

---

### D-007：审计日志实现

**Decision**: AOP（`@Aspect`）+ 自定义注解 `@AuditLog` + 异步写入

**Rationale**:
- 业务代码与审计日志解耦，Service 方法无需手动调用日志记录。
- `@AuditLog(operationType = "VOID_INSPECTION", targetEntity = "InspectionRecord")`
  注解标记需要审计的方法。
- AOP 切面获取方法参数（操作前/后值通过 JSON 序列化）并异步写入 `qc_audit_log`。
- 异步写入（`@Async`）不阻塞主业务流程，失败时写降级日志（本地日志文件）。

**Alternatives considered**:
- 在每个 Service 方法手动调用：代码重复，容易遗漏（宪法 Principle V 禁止）。
- 数据库 binlog：与应用层解耦过度，运维复杂。

---

### D-008：前端状态管理

**Decision**: Pinia（Vue 3 官方推荐）

**Rationale**:
- 比 Vuex 4 更轻量，TypeScript 支持更好（无需 `commit`/`dispatch` 字符串）。
- 按模块拆 store（`authStore`、`dictStore`），避免全局 store 过重。
- `authStore` 管理 JWT Token 和用户角色权限，`dictStore` 缓存枚举字典数据。

**Alternatives considered**:
- Vuex 4：TypeScript 类型推导体验差，模板代码多。
- 组件内 `ref`/`reactive`：跨组件状态共享不方便。

---

### D-009：逆向改判附件不可替换约束实现

**Decision**: 应用层拦截 + DB 字段锁定

**Rationale**:
- `qc_rejudgment_request.evidence_attachment_url` 写入后，Service 层在更新接口中
  检查 `is_reverse = true && evidence_attachment_url IS NOT NULL`，拒绝替换请求。
- 同样策略用于 `qc_concession_acceptance.confirm_attachment_url`。
- 无需 DB 层约束（会降低灵活性），通过 `ServiceAssert` 统一抛出业务异常。

---

### D-010：标准版本时间窗口唯一性约束

**Decision**: 数据库唯一索引 + 应用层校验

**Rationale**:
- `qc_quality_standard` 增加唯一约束：
  `UNIQUE INDEX uk_standard_window (standard_type, variety, grade, customer_id, effective_date)`
  — 防止同一标准体系下相同生效日期的重复版本。
- 应用层在发布新版本前校验：新版本的 `effective_date` 不得落在同体系其他版本的
  `[effective_date, expiry_date]` 区间内（两端包含）。
- 若违反，返回 `ApiResult.failure("标准版本时间窗口与已有版本重叠，请调整生效日期")`.

---

### D-011：数据字典设计（FR-017）

**Decision**: 两张表（`sys_dict` + `sys_dict_item`）+ Pinia 全局缓存 + Redis 二级缓存

**Rationale**:
- 两张表满足字典分类管理和字典项管理，结构简单、运维成本低。
- 字典项携带 `color_tag` 字段，前端 Element Plus 的 `<el-tag type="">` 可直接消费，
  无需前端维护颜色映射 Map。
- 前端启动时通过 `GET /api/v1/dict/all` 批量加载所有启用字典并写入 Pinia `dictStore`；
  运行时直接从 store 读取，不发起额外请求（减少 N+1 问题）。
- 后端 `SysDictService.getItems(dictCode)` 先查 Redis（TTL 10 分钟），未命中再查 DB；
  字典项变更时主动删除 Redis key，保证数据一致性。
- 系统内置字典（`is_system=1`）不可被 ADMIN 删除，但可修改 label 和 sort_no；
  业务枚举值（item_value）不可修改（防止破坏已存储数据的含义）。

**Alternatives considered**:
- 前端硬编码枚举：字典变更需重新部署前端，灵活性差，拒绝。
- 单表设计（自关联）：查询 SQL 复杂，拒绝。

---

### D-012：表头筛选实现方案（FR-018）

**Decision**: 后端动态 WHERE 子句 + 前端 Element Plus 列筛选 + URL query 参数持久化

**Rationale**:
- 后端使用 MyBatis-Plus `QueryWrapper` 动态拼接 WHERE 条件，每个可筛选字段对应一个
  可选查询参数；所有分页查询接口（`POST /page`）的 Request DTO 包含对应筛选字段。
- 前端使用 Element Plus `el-table-column` 的 `:filters` 和 `filter-method` 属性，
  枚举类型的筛选项从 Pinia dictStore 读取，不走网络请求。
- 日期范围筛选使用 `el-date-picker type="daterange"`。
- 文本筛选使用表头内嵌 `el-input`（即 column 的 filterPanel slot）+ 防抖 300ms。
- 筛选状态通过 Vue Router query 参数持久化（`router.replace({ query: filters })`），
  刷新后可恢复筛选条件，满足 FR-018 中的 SHOULD 要求。
- 筛选条件变更时，分页页码自动重置为第 1 页。

**Alternatives considered**:
- 前端本地过滤（Element Plus 内置）：不支持跨页过滤，数据不准确，拒绝。
- 全文搜索（Elasticsearch）：过度设计，初期规模不需要，拒绝。

---

### D-013：MySQL 5.7.43 版本约束

**Decision**: 针对 MySQL 5.7 特性限制调整 SQL 编写规范

**Rationale 与具体约束**:

| 特性 | MySQL 5.7 行为 | 应对方案 |
|------|---------------|---------|
| CTE（WITH 子句） | ❌ 不支持 | 使用子查询或临时表代替 |
| 窗口函数（ROW_NUMBER、RANK 等） | ❌ 不支持 | 使用自增变量或应用层排序代替 |
| JSON_TABLE | ❌ 不支持 | 用 `JSON_EXTRACT`、`JSON_UNQUOTE` 代替 |
| JSON 字段类型 | ✅ 5.7.8+ 支持 | 可用 `JSON` 列类型；存快照数据时也可用 `LONGTEXT` |
| 降序索引 | ❌ 不支持 | 降序查询通过应用层排序或调整索引方向 |
| ONLY_FULL_GROUP_BY | ✅ 5.7 默认开启 | GROUP BY 必须包含 SELECT 中所有非聚合列，或用 `ANY_VALUE()` |
| 字符集 | utf8mb4 ✅ | 统一使用 `utf8mb4_unicode_ci`，支持 emoji |
| Generated Columns | ✅ 支持 | 可用于批次号衍生字段（如需要） |
| innodb_strict_mode | ✅ 默认开启 | 行格式设置 `ROW_FORMAT=DYNAMIC`，支持大索引前缀 |

**关键 SQL 改写示例**:
```sql
-- ❌ MySQL 8.0 写法（CTE）
WITH recent AS (SELECT * FROM qc_inspection_record WHERE ...)
SELECT * FROM recent;

-- ✅ MySQL 5.7 写法（子查询）
SELECT * FROM (SELECT * FROM qc_inspection_record WHERE ...) recent;

-- ❌ MySQL 8.0 写法（窗口函数）
SELECT *, ROW_NUMBER() OVER (PARTITION BY coil_no ORDER BY test_time DESC) rn FROM ...;

-- ✅ MySQL 5.7 写法（自增变量）
SELECT * FROM (
  SELECT t.*, @rn := IF(@prev = coil_no, @rn + 1, 1) AS rn,
  @prev := coil_no FROM qc_inspection_record t, (SELECT @rn:=0, @prev:='') vars
  ORDER BY coil_no, test_time DESC
) ranked WHERE rn = 1;
```

**建表规范**（统一 DDL 模板）:
```sql
CREATE TABLE `table_name` (
  ...
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  ROW_FORMAT=DYNAMIC COMMENT='表注释';
```

---

### D-014：Redis 8.0.5 版本特性利用

**Decision**: 使用 Redis 8.0 新特性优化关键场景

**可用新特性**:

| 特性 | 用途 |
|------|------|
| **RedisJSON（内置）** | 可直接存储 JSON 格式的 Dashboard 统计数据，无需序列化 |
| **Key Expiry 通知增强** | 监听让步有效期到期的 keyspace 通知，触发自动失效 |
| **ACL v2** | 按服务账号精细化控制读写权限 |
| **I/O threads** | 高并发时读写性能更好，初期用默认配置即可 |

**具体使用方案**:
- **JWT 黑名单**: `SET token:{jti} 1 EX {remaining_seconds}`，Token 过期时自动清理
- **Dashboard 统计缓存**: `SET dashboard:summary:{companyId} {json} EX 60`，手动失效用 `DEL`
- **字典缓存**: `SET dict:items:{dictCode} {json} EX 600`，字典变更时 `DEL`
- **幂等控制**: `SET idempotent:{key} 1 NX EX 30`，防止重复提交
- **定时任务分布式锁**: `SET lock:scheduler:{task} 1 NX EX 60`，保证多节点只执行一次

**注意**: Redis 8.0.5 的 RedisJSON 模块默认未启动，如需使用须在配置中加载
`loadmodule /usr/lib/redis/modules/librejson.so`；初期可使用普通 String 存 JSON 字符串。

---

### D-015：判定结论四段优先级（Session 2026-05-16 澄清）

**Decision**: NEED_REINSPECTION 在让步范围**未配置**时触发，CAN_CONCESSION 在让步范围**已配置且实测值在范围内**时触发

**四段互斥判断顺序**:
1. 实测值在合格限内 → QUALIFIED
2. 超出合格限 + concession_upper/lower 非 NULL 且实测值在让步范围内 → CAN_CONCESSION
3. 超出合格限 + concession_upper/lower 均为 NULL → NEED_REINSPECTION
4. 超出合格限 + 超出让步范围 → UNQUALIFIED

多指标取最严重结论（UNQUALIFIED > NEED_REINSPECTION > CAN_CONCESSION > QUALIFIED）

**Rationale**: 让步范围是标准设计决策，无让步范围配置意味着标准制定者认为该偏差必须复检而非直接让步。判断规则与现有 StandardIndicator 数据模型完全兼容，无需新增字段。

**Alternatives considered**:
- 复检标志位（is_reinspect）：需新增字段，且与让步范围语义重叠，拒绝。
- 由客户协议约定：增加业务复杂度，拒绝。

---

### D-016：product_spec 规格匹配方式（Session 2026-05-16 澄清）

**Decision**: product_spec 字段强制下拉选择，数据源来自标准库动态过滤，禁止自由文本输入

**Rationale**: 避免区间数值解析的复杂性（MySQL 5.7 字符串解析成本高），通过 UI 设计保证语义正确性。下拉数据源按已选客户+品种+牌号动态过滤有效 spec_range 去重列表，显示格式含版本号信息（如"厚度 1.2–2.0mm / 宽度 900–1250mm (GB/T 700-2019)"）。选定后快照存入检验记录，判定快照原则保障历史不变性。

**Alternatives considered**:
- 区间数值解析：实现复杂，需解析 spec_range 格式，且格式不标准化，拒绝。
- 规格不参与匹配：丢失重要业务区分信息，拒绝。

---

### D-017：质保书关键指标范围（Session 2026-05-16 澄清）

**Decision**: 成分（COMPOSITION）+ 性能（PERFORMANCE）+ 尺寸（DIMENSION）三类默认纳入；表面/外形默认排除；ADMIN 可配置

**Rationale**: 钢铁行业质保书通常只包含可量化的机械性能和成分数据，表面/外形以目检为主难以数字化汇总。三类固定纳入符合行业惯例，同时保留 ADMIN 配置灵活性以适应不同客户需求。

---

### D-020：质保书 PDF 导出（Session 2026-06-29 实现收敛）

**Decision**: 本系统内置正式质保书 PDF 导出；Apache PDFBox 2.x + `QualityCertPdfBuilder`；中文字体资源置于 `backend/src/main/resources/fonts/`

**Rationale**: 原假设「PDF 由下游系统生成」已不满足演示与出证需求。PDFBox 已在 `pom.xml` 引入；集中 Builder 可避免手写 ASCII PDF 导致中文乱码；导出门禁与数据生成门禁保持一致，降低误出证风险。

**版式要点**: A4 中文表格化；标题「质量证明书」；基本信息双列表 + 检测指标表；`EXPORT_CERT_PDF` 审计。

---

### D-018：系统可用性目标（Session 2026-05-16 澄清）

**Decision**: 软可用目标：单点部署，RTO ≤ 30 分钟，RPO = 0

**Rationale**: 50–200 人的内部工业系统不需要 HA 集群（成本过高）。Spring Boot 单体 + MySQL 事务保证 RPO=0，Spring Boot Actuator 健康端点支持自动重启检测。故障时展示维护页而非数据损坏比无 SLA 更可接受。

---

### D-019：可观测性方案（Session 2026-05-16 澄清）

**Decision**: SLF4J + Logback 结构化 JSON 日志 + MDC traceId + File Appender

**关键配置**:
```xml
<!-- logback-spring.xml 核心结构 -->
<appender name="FILE_ERROR" class="ch.qos.logback.core.rolling.RollingFileAppender">
  <file>/var/log/qc/application-error.log</file>
  <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
    <fileNamePattern>/var/log/qc/application-error.%d{yyyy-MM-dd}.log</fileNamePattern>
    <maxHistory>30</maxHistory>
  </rollingPolicy>
  <encoder class="net.logstash.logback.encoder.LogstashEncoder" />
  <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
    <level>WARN</level>
  </filter>
</appender>
```

**MDC 使用**: 每个 HTTP 请求通过 Filter 自动生成 UUID traceId 注入 `MDC.put("traceId", uuid)`，请求结束时 `MDC.remove`。

**Rationale**: Spring Boot 已包含 SLF4J + Logback，logstash-logback-encoder 是 Spring Boot 生态标配的 JSON 结构化日志方案。Actuator 已在 pom.xml 中，零额外基础设施成本。

**Alternatives considered**:
- Prometheus + Grafana：成本高，50-200 人内部系统过度工程，拒绝。
- 写入 qc_audit_log：混用业务审计和系统运维日志，职责不清晰，拒绝。
