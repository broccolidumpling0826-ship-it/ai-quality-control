<!--
SYNC IMPACT REPORT
==================
Version change: N/A (initial template) → 1.0.0
Bump type: MINOR — initial fill of all principles and sections from template placeholders.

Modified principles: N/A (initial population)

Added sections:
  - I.  Java后端命名与注释规范
  - II. Java后端OOP与性能规范
  - III. 复杂Service设计规范
  - IV. 前端代码规范（Vue + TypeScript）
  - V.  代码质量与可测试性
  - 技术栈约束
  - 开发工作流
  - Governance

Templates updated:
  ✅ .specify/templates/plan-template.md — Constitution Check gates updated to reference Principles I–V
  ✅ .specify/templates/spec-template.md — no structural changes required
  ✅ .specify/templates/tasks-template.md — no structural changes required

Deferred TODOs:
  - TODO(PROJECT_NAME): RESOLVED — project name set to "ai-quality-control".
-->

# ai-quality-control Constitution

## Core Principles

### I. Java后端命名与注释规范

命名和注释是代码可读性与可维护性的基石。所有 Java 代码 MUST 遵守：

- 类名使用大驼峰命名（UpperCamelCase）；抽象类以 `Abstract`/`Base` 开头；异常类以
  `Exception` 结尾；接口名以 `I` 开头。
- 方法名、参数名、成员变量、局部变量使用小驼峰（lowerCamelCase）；函数成员变量以 `m`
  开头。
- 常量全部大写，单词间下划线分隔（如 `MAX_STOCK_COUNT`）；`final` 成员同规。
- POJO 类中布尔变量不以 `is` 开头；包装类型代替基本类型；不加属性默认值；MUST 写
  `toString`。
- 所有抽象方法 MUST 用 javadoc 注释（功能、参数、返回值、异常）；类/属性/方法使用
  `/** */` 格式。
- 注释只解释"为什么"，不解释"是什么"；优先用清晰命名代替注释；禁止无意义注释。
- 不允许魔法值直接出现在代码中，MUST 提取为具名常量。
- 数组类型写法：`String[] args`，而非 `String args[]`。

### II. Java后端OOP与性能规范

面向对象设计和性能规约保障代码健壮性与可扩展性。以下约束均为 MUST：

- 单个方法代码行数不得超过 160 行；单个类文件不得超过 1000 行；超过必须拆分。
- 函数嵌套深度不超过 3 层；函数圈复杂度必须小于 20。
- 方法参数个数不超过 4 个，超过时封装请求对象或上下文对象。
- 一个函数只做一件事；禁止"万能类"。
- 线程池不允许使用 `Executors` 创建，MUST 使用 `ThreadPoolExecutor`；禁止使用
  `Timer`，改用 `ScheduledExecutorService`。
- `SimpleDateFormat` 禁止作为 `static` 变量；`ThreadLocal` 使用后必须调用 `remove()`。
- 禁止使用 `new BigDecimal(double)`，MUST 使用 `BigDecimal.valueOf()` 或
  `new BigDecimal(String)`。
- `equals` 比较 MUST 使用常量或确定有值的对象调用（`"hello".equals(str)`）。
- 集合初始化时指定初始值大小；循环体内字符串拼接使用 `StringBuilder`。
- 禁止浮点数直接逻辑判断；避免采用反逻辑运算符。
- 变量中不允许有敏感信息（邮箱、身份证、IP、电话号码必须脱敏）。
- 资源对象（数据库、IO 流）MUST 关闭，推荐 `try-with-resources`。
- `if/else/for/while/do` 必须使用大括号，即使只有一行代码。
- `switch case` 必须在结尾处加 `break`/`return`；禁止 `finally` 内使用
  `break`/`return`/`throw`。

### III. 复杂Service设计规范

复杂流程型 Service（同时包含大量校验、多业务步骤、状态变更以及外部依赖调用）MUST 遵守：

- Controller 调用的 Service 入口方法只保留主干步骤编排，细节MUST下沉到具名私有方法或专门
  组件；禁止将校验、分支、远程调用全堆在入口函数。
- 方法命名 MUST 使用"动词 + 业务对象/业务动作"结构；禁止模糊命名（`process`、
  `handleData`、`doIt` 等）；布尔变量 MUST 以 `has`、`can`、`should` 开头。
- 每条重要业务规则 MUST 提取成具名函数，函数名直接表达规则含义。
- 外部依赖（数据库、RPC、HTTP、MQ、缓存）MUST 收敛到专门方法或组件；禁止在复杂业务分支
  中 scattered 地直接调用。
- 每次外部调用都要明确失败策略、超时、重试、幂等和空值校验；调用结果 MUST 做空值和异常校验。
- 禁止吞异常；禁止 `catch` 后什么也不做；异常 MUST 转换为明确业务异常或记录日志后上抛；
  禁止用异常表达正常业务分支。
- 参数校验统一使用 `ServiceAssert`，不手写重复的 `if + throw` 样板。
- 返回值风格必须稳定；`void` 方法只承担明确副作用，调用方需要结果判断时 MUST 返回结果对象。
- 能用 `final` 尽量用 `final`；一个变量只承载一个含义，禁止跨代码段复用变量。
- 状态推进、状态判断 MUST 通过枚举类取值，禁止直接写状态字面量。
- 日志只在关键节点打印（请求开始、关键外部调用结果、流程结束、异常场景），MUST 带业务主键；
  不打印重复日志；不打印敏感信息；不允许把日志当业务逻辑的一部分。

### IV. 前端代码规范（Vue + TypeScript）

前端规范确保多人协作下的代码一致性和可维护性。以下约束均为 MUST：

- 项目/目录命名全部小写，以中划线分隔（kebab-case）；有复数结构时采用复数命名法。
- JS/Vue/TS/TSX/JSX/CSS/SCSS/HTML/PNG 文件全部小写，中划线分隔。
- TypeScript 使用双引号；JavaScript 使用单引号。
- 缩进统一 2 个空格（模板和脚本一致）。
- 代码块 MUST 使用大括号包裹，即使只有一行；条件判断和循环最多三层，超过必须抽成函数。
- Vue 组件名始终是多个单词（≥2），组件名 KebabCase；组件文件名 kebab-case。
- 紧密耦合的子组件以父组件名作为前缀；禁止组件名缩写。
- Props MUST 指定类型、加注释、加 `required` 或 `default`；如有业务需要 MUST 加
  `validator`。
- 组件样式 MUST 使用 `scoped`；模板中只包含简单表达式，复杂表达式 MUST 重构为计算属性或方法。
- `v-for` MUST 设置 `key`；指令统一使用缩写（`:` / `@` / `#`）。
- 路由使用懒加载；`path` MUST 以 `/` 开头；router `name` 采用 KebabCase 且与组件名一致。
- 禁止使用 `for...in` 做对象属性遍历。
- 禁止直接使用 `undefined` 进行变量判断，MUST 使用 `typeof` 判断。
- 谨慎使用 `console.log`，非 webpack 项目中应避免。
- 对上下文 `this` 的引用只能使用 `self` 命名。

### V. 代码质量与可测试性

高质量代码保障系统长期健康演进：

- 业务核心方法 MUST 可单测；复杂规则 MUST 可单独测试。
- 复杂 Service 至少能按"成功 / 失败 / 边界"拆出测试点。
- 避免大量 `static` 依赖导致难以 mock。
- 代码优先生成简单、直白、低心智负担的实现；当"更短"与"更容易读懂"冲突时，MUST 优先选
  更容易读懂的实现；禁止炫技式写法。
- 禁止过度设计：不为假设中的未来需求做预防性抽象；三条相似的代码优于过早的抽象。
- 安全性：`ContentProvider` 操作 MUST 采用参数化查询，防止 SQL 注入；SSL 传输 MUST 校验
  主机和客户端可信性。

## 技术栈约束

本项目为前后端分离架构，所有设计决策 MUST 在以下技术边界内进行：

**后端**：
- Java（JDK 1.8+）+ **Spring Boot 2.7.x 单体应用**（不使用 Spring Cloud）
- 认证授权：Sa-Token（轻量级，替代 Spring Security + JWT 复杂配置）
- ORM：MyBatis-Plus 3.x；API 文档：Knife4j
- 持久化：MySQL 5.7.43（utf8mb4，无 CTE / 窗口函数）
- 缓存：Redis 8.0.5（JWT 黑名单、Dashboard 缓存、字典缓存、分布式锁）
- 包结构遵循金恒Java语言通用编程规范V2.0

**前端**：
- Vue 3 + Element Plus 组件库
- TypeScript（严格类型）
- 构建工具：Webpack / Vite（以项目实际配置为准）

**接口规范**：
- 前后端通过 RESTful API 通信，统一 JSON 格式
- API 统一版本化管理（路径前缀 `/api/v{n}/`）

## 开发工作流

开发过程中的质量门控，MUST 在合并前通过：

- 每次提交前完成代码自审：命名、注释、方法长度（≤160 行）、嵌套深度（≤3 层）、圈复杂度（<20）。
- 新增复杂 Service MUST 遵守原则 III（入口方法编排、外部依赖收敛、ServiceAssert 使用）。
- 前端组件 MUST 遵守原则 IV（Props 定义完整、scoped 样式、模板简洁性）。
- 合并请求前 MUST 通过代码审查，审查人确认遵守核心原则 I–V。
- 安全规范（敏感信息脱敏、SQL 注入防护）MUST 在开发阶段落实，不得遗留到测试阶段。

## Governance

本宪法是项目最高行为准则，高于个人习惯和局部约定。任何与本宪法冲突的实践以本宪法为准。

**修订程序**：
1. 任何人可提出修订建议，需说明修订原因和影响范围。
2. 修订需团队讨论确认；涉及原则 I–V 的重大变更需技术负责人批准。
3. 批准后更新 `constitution.md`，`CONSTITUTION_VERSION` 按语义化版本规则递增，
   并同步传播至相关模板文件。

**版本策略**：
- MAJOR：向后不兼容的原则删除或重定义。
- MINOR：新增原则/章节或实质性扩展指导。
- PATCH：澄清、措辞、错别字修正等非语义性修订。

**合规审查**：
- 每次代码审查时验证本宪法遵守情况。
- 复杂度违规（方法长度、嵌套深度、圈复杂度）MUST 在合并前完成解释或重构。
- 安全违规（敏感信息泄露、SQL 注入等）为阻断性问题，MUST 修复后才能合并。

**Version**: 1.0.0 | **Ratified**: 2026-05-14 | **Last Amended**: 2026-05-14
