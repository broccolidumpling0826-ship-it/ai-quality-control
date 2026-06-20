# Backend Development Standard

本文件是 `backend/` 目录下 Java 后端开发的强制规范。根目录 `AGENTS.md` 仍然适用；当两者冲突时，后端代码以本文件更具体的规则为准。

本规范依据：

- `.specify/memory/constitution.md`
- Java 8 + Spring Boot 2.7.x + MyBatis-Plus + MySQL 5.7 项目约束
- 阿里巴巴 Java 开发规约的命名、分层、异常、集合、并发、数据库、安全等通用要求
- 本项目新增强制规则：跨业务 Service 访问数据必须通过 Service API，不允许直接调用其他业务域的 Mapper

## 1. 总原则

- 后端代码必须保持 Java 8 兼容。
- 所有业务判断必须可追溯、可测试、可审计。
- Controller 只负责参数接收、权限入口、调用 Service 和返回结果，不写业务规则。
- Service 是业务边界和事务边界的主要承载层。
- Mapper 只做本业务域数据访问，不承载业务规则，不跨业务域复用。
- Entity 是数据库映射对象，DTO 是请求对象，VO 是响应对象，三者不得混用。
- AI 能解释、建议、预填，但不能绕过规则引擎、人工审批、审计和权限控制。

关键词含义：

- MUST：必须遵守，违反即为阻断问题。
- SHOULD：强烈建议，除非有明确技术理由。
- MAY：允许，但需保持一致性。

## 2. 包结构与职责

现有包结构必须保持清晰：

```text
com.jhict.quality
  common/       通用配置、异常、返回体、审计、工具
  controller/   REST API 入口
  dto/          请求命令和查询条件
  engine/       规则判定引擎、匹配模型
  entity/       数据库实体
  enums/        枚举
  mapper/       MyBatis-Plus Mapper
  scheduler/    定时任务
  service/api   Service 接口
  service/impl  Service 实现
  vo/           响应视图对象
```

新增代码必须放入匹配的包。禁止为了方便在 `common`、`controller` 或某个已有 Service 中堆放无关业务逻辑。

## 3. 分层调用规则

### 3.1 允许的调用方向

```text
Controller -> Service API -> Service Impl -> Own Mapper
                           -> Other Service API
                           -> Engine / Gateway / Component
```

允许：

- Controller 调用 Service 接口。
- Service 实现调用自己拥有的 Mapper。
- Service 实现调用其他 Service 接口。
- Service 实现调用规则引擎、网关、领域组件、工具类。
- Mapper 被其唯一归属 Service 实现调用。

禁止：

- Controller 直接调用 Mapper。
- Controller 直接调用其他 Service 的实现类。
- 一个 Service 实现直接调用其他业务域的 Mapper。
- Mapper 调用 Service。
- Mapper 中写业务状态推进、审批规则、AI 决策规则。
- Service 为了避免循环依赖而从 Spring 上下文手动取其他业务域 Mapper。

### 3.2 Mapper 所有权强制规则

每个 Mapper MUST 有且只有一个业务归属 Service。只有归属 Service 的实现类可以注入和调用该 Mapper。

跨业务读取或修改数据时，调用方 MUST 通过归属 Service 暴露的业务方法完成，不得绕过 Service 直接注入 Mapper。

错误示例：

```java
@Service
public class ReinspectionServiceImpl implements ReinspectionService {

    @Resource
    private QcJudgmentResultMapper judgmentResultMapper; // BAD: 跨域直接访问判定 Mapper

    public void createReinspection(String judgmentId) {
        QcJudgmentResult judgment = judgmentResultMapper.selectById(judgmentId);
        // ...
    }
}
```

正确示例：

```java
@Service
public class ReinspectionServiceImpl implements ReinspectionService {

    @Resource
    private JudgmentService judgmentService;

    public void createReinspection(String judgmentId) {
        QcJudgmentResult judgment = judgmentService.getRequiredJudgmentForWorkflow(judgmentId);
        // ...
    }
}
```

归属 Service 如果缺少查询或状态变更方法，调用方不得直接借用 Mapper，MUST 先在归属 Service 接口中补充语义明确的方法。

方法命名示例：

- `getRequiredJudgmentForWorkflow`
- `findFinalJudgmentByRecordId`
- `markJudgmentNonFinal`
- `listInspectionRecordsForCertificate`
- `voidApprovedConcessionsByJudgmentChange`
- `getPublishedStandardWithIndicators`

禁止为了暴露 Mapper 能力而增加无语义的方法，例如：

- `selectById`
- `queryList`
- `updateEntity`
- `getMapperData`
- `doMapperOperation`

### 3.3 Mapper 归属登记

新增 Mapper 时，必须在对应 Service 设计中明确归属。若当前没有归属 Service，先创建 Service API 和实现，再使用 Mapper。

当前项目建议归属如下，后续新增或重构时按此原则收敛：

| Mapper | 归属 Service | 说明 |
| --- | --- | --- |
| `QcAuditLogMapper` | `AuditLogService` | 审计日志查询和写入 |
| `QcQualityStandardMapper` | `StandardService` | 标准主表 |
| `QcStandardIndicatorMapper` | `StandardService` | 标准指标为标准子资源 |
| `QcIndicatorItemMapper` | `IndicatorService` | 指标项目主数据 |
| `QcInspectionRecordMapper` | `InspectionService` | 检验记录主表 |
| `QcInspectionValueMapper` | `InspectionService` | 检验值为检验记录子资源 |
| `QcJudgmentResultMapper` | `JudgmentService` | 判定结论 |
| `QcJudgmentEvidenceMapper` | `JudgmentService` | 判定依据快照 |
| `StandardGapMapper` | `StandardGapService` | 标准覆盖缺口 |
| `QcReinspectionRecordMapper` | `ReinspectionService` | 复检记录 |
| `QcRejudgmentRequestMapper` | `RejudgmentService` | 改判申请 |
| `QcRejudgmentApprovalMapper` | `RejudgmentService` | 改判审批记录 |
| `QcConcessionAcceptanceMapper` | `ConcessionService` | 让步接收 |
| `QcQualityCertDataMapper` | `CertDataService` | 质保书数据 |
| `SysDictMapper` / `SysDictItemMapper` | `SysDictService` | 字典 |
| `SysMenuMapper` | `MenuManageService` | 菜单维护和菜单基础查询；用户菜单读取必须通过 Service API |
| `SysUserMapper` | `UserManageService` | 用户维护和用户基础查询；认证读取必须通过 Service API |
| `SysRoleMapper` / `SysPermissionMapper` / relation mappers | `RoleManageService` | 角色、权限和关联关系维护；RBAC 查询 facade 必须通过 Service API |
| `SysNotificationMapper` | `NotificationService` | 通知 |

如果一个 Mapper 暂时被多个历史 Service 使用，新增代码不得扩大这种用法；触碰相关代码时 SHOULD 将跨域 Mapper 调用替换为 Service API。

`AuthService`、`MenuService`、`RbacQueryService` 这类查询或认证 facade 如果需要读取用户、菜单、角色、权限数据，必须调用归属 Service 暴露的查询方法，不得直接注入上述 Mapper。若现有 Service API 不足，先补充 Service API，再实现调用方逻辑。

### 3.4 跨 Service 事务边界

- 入口业务 Service 方法上使用 `@Transactional(rollbackFor = Exception.class)`。
- 只读查询方法 SHOULD 使用 `@Transactional(readOnly = true)`，或保持无事务但不得产生副作用。
- 跨 Service 调用时，调用方负责业务编排，被调用方负责本业务域的数据一致性和校验。
- 禁止在一个事务中混合大量无关外部调用。AI、HTTP、ES、文件 IO 等慢调用 SHOULD 在事务外执行，或先准备结果再进入短事务提交。
- 需要跨域状态推进时，必须有明确业务方法和审计记录。

## 4. 命名规范

### 4.1 Java 命名

- 类名使用大驼峰：`QualityStandardServiceImpl`。
- 方法名、参数名、成员变量、局部变量使用小驼峰。
- 常量全部大写，单词间使用下划线：`MAX_REINSPECTION_COUNT`。
- 异常类以 `Exception` 结尾。
- 抽象类以 `Abstract` 或 `Base` 开头。
- 布尔变量使用 `has`、`can`、`should`、`need`、`enabled` 等表达语义，POJO 布尔属性不以 `is` 开头。
- 数组写法使用 `String[] args`。

### 4.2 项目对象后缀

- Entity：数据库表映射，例如 `QcJudgmentResult`。
- DTO / Cmd / Query：请求对象，例如 `QcInspectionRecordAddCmd`、`QcJudgmentPageQuery`。
- VO：响应对象，例如 `QcJudgmentResultVO`。
- Mapper：MyBatis-Plus Mapper，例如 `QcJudgmentResultMapper`。
- Service：接口在 `service/api`，实现类以 `Impl` 结尾。
- Enum：业务枚举，例如 `JudgmentType`。

### 4.3 Service 方法命名

Service 方法必须使用业务语义命名，推荐结构为：

```text
动词 + 业务对象 + 条件/用途
```

推荐：

- `createInspectionRecord`
- `generateCertificateData`
- `findFinalJudgmentByRecordId`
- `validateConcessionEligibility`
- `resolveStandardConflict`

禁止：

- `process`
- `handle`
- `handleData`
- `doIt`
- `query`
- `update`

通用 CRUD 方法只允许用于非常薄的主数据服务；复杂业务服务必须使用业务语义方法。

## 5. 注释与文档

- 公共 Service 接口方法 MUST 使用 Javadoc，说明功能、参数、返回值和主要异常。
- 复杂业务规则必须通过清晰命名表达，必要时用注释解释“为什么这么做”。
- 禁止无意义注释，例如“设置名称”“循环列表”。
- 禁止注释与代码不一致。修改逻辑时同步修改注释。
- Controller 接口必须使用 Knife4j / Swagger 注解保持 API 文档可读。

## 6. Controller 规范

- Controller 只接收 DTO/Query/Cmd，不接收 Entity 作为请求体。
- Controller 不得直接访问 Mapper。
- Controller 不写业务规则、状态推进、权限细节和跨表查询。
- Controller 返回统一 `ApiResult`。
- 参数校验优先使用 Bean Validation 注解：`@NotBlank`、`@NotNull`、`@Size`、`@Valid`。
- 敏感操作必须有权限控制和审计入口。
- 不在 Controller 捕获并吞掉业务异常。

## 7. Service 规范

### 7.1 入口方法结构

复杂 Service 入口方法只保留主流程编排：

```java
@Override
@Transactional(rollbackFor = Exception.class)
public QcConcessionVO applyConcession(QcConcessionAddCmd cmd) {
    validateConcessionCommand(cmd);
    QcJudgmentResult judgment = judgmentService.getRequiredJudgmentForWorkflow(cmd.getJudgmentId());
    validateConcessionEligibility(judgment);
    QcConcessionAcceptance acceptance = buildConcessionAcceptance(cmd, judgment);
    saveConcessionAcceptance(acceptance);
    auditConcessionCreated(acceptance);
    return buildConcessionVO(acceptance);
}
```

校验、构建、查询、状态推进、审计、通知等细节必须下沉到具名私有方法或专门组件。

### 7.2 方法复杂度

- 单个方法不超过 160 行。
- 单个类不超过 1000 行。
- 嵌套深度不超过 3 层。
- 圈复杂度小于 20。
- 参数超过 4 个时必须封装为 DTO、Query 或上下文对象。
- 一个方法只做一件事。

### 7.3 业务规则

- 重要业务规则必须提取成具名函数。
- 状态判断使用枚举或常量，禁止直接散落字符串字面量。
- 不允许魔法值，必须提取常量。
- 规则引擎判定逻辑必须保持确定性。
- AI 输出不得直接改变判定、复检、改判、让步审批、冲突裁决结果。

### 7.4 异常

- 使用项目已有 `ServiceException` 表达业务异常。
- 不得吞异常。
- 不得 `catch` 后只打印日志不处理。
- 不用异常表达正常业务分支。
- 外部系统异常必须转换为明确业务错误或向上抛出。
- 日志中必须带业务主键，但不得打印密码、Token、API Key、身份证、手机号、真实客户敏感信息。

### 7.5 日志

日志只记录关键节点：

- 业务入口开始。
- 关键状态变更。
- 外部依赖调用结果。
- 审计敏感操作。
- 异常场景。

日志必须包含业务主键，例如 `recordId`、`judgmentId`、`concessionId`、`standardId`。禁止用日志替代业务状态。

## 8. Mapper 与数据库规范

- Mapper 只允许写 SQL 和 MyBatis-Plus 数据访问，不写业务规则。
- MySQL 必须兼容 5.7，不使用 CTE、窗口函数、JSON 高级函数依赖。
- 查询列表必须考虑分页。
- 新增表必须添加必要索引，覆盖列表筛选、业务查找、关联字段。
- 字符集使用 `utf8mb4`。
- 金额、指标、限值、偏差等精确数值使用 `DECIMAL` / `BigDecimal`。
- SQL 条件必须参数化，禁止字符串拼接用户输入。
- 批量操作要注意数量和事务时间，避免一次性加载过多数据。
- 数据库字段变更必须提供 migration，不直接修改历史基线而不说明影响。

## 9. Entity / DTO / VO 规范

### 9.1 Entity

- Entity 只映射数据库字段，不承载复杂业务逻辑。
- 不把 Entity 直接作为 Controller 请求体。
- Boolean 字段不以 `is` 开头。
- 包装类型优先于基本类型，避免无法表达 null。
- Entity 字段注释必须与数据库语义一致。

### 9.2 DTO / Cmd / Query

- 请求对象必须表达用途，例如 `QcConcessionAddCmd`、`QcJudgmentPageQuery`。
- 必填字段使用 Bean Validation。
- 不在 DTO 中放响应展示字段。
- 不在 Query 中放业务状态推进字段。

### 9.3 VO

- VO 只为前端展示服务。
- VO 不应泄露内部敏感字段。
- VO 可聚合多个业务域数据，但聚合必须由 Service 调用其他 Service API 完成，不得跨域直接查 Mapper。

## 10. 枚举与字典

- Java 枚举和数据库字典必须保持一致。
- 新增业务状态时同时更新：
  - Java enum
  - 字典 seed
  - 前端展示映射
  - 统计/筛选/权限逻辑
  - 测试数据
- 禁止在多个类中散落同一个状态字符串。
- `STANDARD_CONFLICT` 等高风险状态必须有明确业务流转，不得作为普通改判目标随意选择。

## 11. 阿里巴巴 Java 规约落地要求

### 11.1 集合与并发

- 集合初始化时尽量指定容量。
- 遍历中不要对集合结构做不安全修改。
- `equals` 比较使用常量或确定非空对象调用。
- 禁止使用 `new BigDecimal(double)`，使用 `BigDecimal.valueOf()` 或 `new BigDecimal(String)`。
- 线程池不使用 `Executors` 快捷方法，必须显式使用 `ThreadPoolExecutor`。
- 禁止使用 `Timer`，使用 `ScheduledExecutorService` 或 Spring Scheduler。
- `ThreadLocal` 使用后必须 `remove()`。
- `SimpleDateFormat` 不得作为共享静态变量；Java 8 优先使用 `java.time`。

### 11.2 控制结构

- `if`、`else`、`for`、`while`、`do` 必须使用大括号。
- `switch` 每个分支必须 `break`、`return` 或明确注释 fall-through。
- `finally` 中禁止 `return`、`throw`、`break`、`continue`。
- 浮点数不做直接相等判断。
- 避免复杂反逻辑表达式，必要时提取正向布尔变量。

### 11.3 安全

- 不提交密钥、密码、Token、真实客户敏感数据。
- 配置项使用环境变量占位。
- 日志和异常返回不得泄露内部堆栈、SQL、密钥、连接串。
- 文件上传必须校验路径、文件名、大小和类型。
- 下载文件必须防止路径穿越。

## 12. AI 增强后端特殊规范

- 结构化标准数据是判定唯一真相源。
- RAG 原文只用于引用、解释、核查，不覆盖结构化限值。
- AI 结果必须持久化输入快照、输出、引用、模型、Prompt 版本、置信度、降级来源。
- AI 低置信时，高风险功能必须转人工，不给明确放行建议。
- ModelGateway 和 VectorStoreGateway 是外部 AI/向量服务唯一入口。
- 业务 Service 不得直接调用 DeepSeek HTTP 细节或 Elasticsearch DSL。
- AI、ES 不可用不能影响规则判定。
- Prompt 和检索文本均视为不可信输入，禁止按模型输出执行权限、SQL、文件或流程操作。

## 13. 审计与权限

以下操作必须审计：

- 标准新增、修改、发布、废止。
- 冲突裁决。
- 改判申请和审批。
- 让步接收申请、客户确认、审批、作废。
- 置信度配置修改。
- AI 建议采纳或忽略。
- 正式质保书生成和下载。

权限校验必须在业务入口完成。仅前端隐藏按钮不是权限控制。

## 14. 测试要求

- 新增复杂 Service 必须覆盖成功、失败、边界场景。
- 核心规则方法必须可单测。
- Mapper SQL 涉及复杂条件时必须有测试或手工验证记录。
- 新增状态必须覆盖列表、详情、统计、工作台、权限、审计影响。
- 跨 Service 调用必须测试调用方行为和被调用 Service 方法的业务契约。
- AI 降级路径必须覆盖缓存、规则模板、原始检索、完全不可用。

最低验证：

```bash
cd backend && mvn test
cd backend && mvn -DskipTests package
```

如因 MySQL、Redis、ES、模型服务或凭据不可用导致无法验证，必须在交付说明中明确说明。

## 15. 代码审查清单

提交前必须自查：

- 是否新增 Controller 直接调用 Mapper。
- 是否新增 Service 跨业务域直接注入 Mapper。
- 是否为跨域访问补充了 Service API 语义方法。
- 是否存在魔法值、散落状态字符串。
- 是否存在超长方法、超深嵌套、万能 Service。
- 是否使用 Entity 作为请求体或响应体。
- 是否缺少事务边界。
- 是否吞异常。
- 是否日志缺少业务主键或泄露敏感信息。
- 是否新增表但缺少 migration、索引、字典、菜单或测试数据。
- 是否违反 MySQL 5.7 兼容性。

可辅助检查：

```bash
# 检查 Controller 是否直接引用 Mapper
rg -n "Mapper" backend/src/main/java/com/jhict/quality/controller

# 检查 Service 实现中的 Mapper 引用，需逐项对照 Mapper 归属登记
rg -n "import com\\.jhict\\.quality\\.mapper\\." backend/src/main/java/com/jhict/quality/service/impl

# 检查状态字面量，需要判断是否应替换为枚举或常量
rg -n "\"(QUALIFIED|UNQUALIFIED|NEED_REINSPECTION|CAN_CONCESSION|STANDARD_CONFLICT|PUBLISHED|DRAFT|APPROVED|REJECTED)\"" backend/src/main/java/com/jhict/quality
```

## 16. 历史代码处理原则

当前代码中可能存在历史形成的跨域 Mapper 调用、状态字符串、超长方法或重复查询逻辑。后续开发必须遵守：

- 不扩大历史违规范围。
- 新增代码必须符合本规范。
- 修改相关历史代码时，优先将跨域 Mapper 访问收敛到归属 Service API。
- 若一次性重构风险过大，必须在代码评审或任务说明中记录遗留点和后续处理方案。

## 17. 禁止事项摘要

- 禁止 Controller 调 Mapper。
- 禁止 Service 调其他业务域 Mapper。
- 禁止 Mapper 写业务规则。
- 禁止 AI 自动审批、自动改判、自动让步、自动裁决。
- 禁止 RAG 原文覆盖结构化规则。
- 禁止硬编码密钥。
- 禁止吞异常。
- 禁止无审计的高风险状态变更。
- 禁止不兼容 MySQL 5.7 的 SQL。
