---
name: db-code-gen
description: 根据数据库表设计（以表格形式描述的字段信息）自动生成符合项目规范的 Java Spring Boot 后端业务代码。当用户提供数据库表结构、字段信息、建表语句，或者说"帮我生成代码"、"根据这张表生成业务代码"、"生成 Entity/Service/Controller/Mapper"等需求时，必须使用此 skill。生成的代码包含 Entity、Mapper、Service、Controller 四层，并完全遵循项目内部规范（MyBatis-Plus、ApiResult、ServiceAssert、LambdaWrapper、批量操作模板等）。
---

# DB → 后端代码生成 Skill

## 目标

用户提供数据库表设计（表格、SQL、或口述字段），你负责生成完整的四层后端代码，严格遵循下方规范。

---

## 第一步：解析表结构

用户输入可能是：
- Markdown 表格（字段英文名 / 字段中文名 / 类型 / 长度 / 是否主键 / 是否为空 / 默认值）
- CREATE TABLE SQL 语句（Oracle DDL 格式）
- 口头描述的字段列表

从中提取以下信息：
- 表名（下划线命名，如 `METER_ACCT_DTL`，Oracle 惯用大写）
- 实体类名（大驼峰，如 `MeterAcctDtl`）
- 模块前缀（如 `prf`、`oa`，从表名推断；如无法推断，询问用户）
- 每个字段的：中文名、Java 类型、是否主键、是否为空

**基础字段识别（不生成到实体类中）：**

以下字段已由 `CoreEntity` 提供，如果用户的表结构中包含它们，**跳过，不在 Entity 中重复声明**：
- `ID` / `id` — 主键
- `COMPANY_ID` / `companyId` — 公司ID
- `CREATE_USER_NO` / `createUserNo` — 创建人工号
- `UPDATE_USER_NO` / `updateUserNo` — 修改人工号
- `CREATE_DATE_TIME` / `createDateTime` — 创建日期
- `UPDATE_DATE_TIME` / `updateDateTime` — 修改日期

**Oracle 字段类型映射规则：**
| Oracle 类型 | Java 类型 |
|---|---|
| VARCHAR2 / VARCHAR / CHAR / CLOB | `String` |
| NUMBER(n) / INT / INTEGER（整型，无小数） | `Integer`（n≤9）或 `Long`（n>9） |
| NUMBER(m,n) / DECIMAL（有小数位） | `BigDecimal` |
| DATE / TIMESTAMP | `String`（项目用字符串存日期） |
| CHAR(1) 或只有 0/1 两种值的 NUMBER(1) | `Boolean` |
| 只有两种状态的整型状态字段 | `Boolean`（见实体规范） |

**布尔类型判断：** 如果字段只表示是/否（如是否启用、是否删除），用 `Boolean`；多于两种状态时使用枚举（见枚举规范）。

---

## 第二步：确认生成信息

在生成代码前，先输出一个简短的确认摘要，例如：

```
将为以下表生成代码：
- 表名：meter_acct_dtl
- 实体类名：MeterAcctDtl
- 包路径前缀：com.jhict.performance（请确认或修改）
- 业务字段（不含 CoreEntity 基础字段）：employeeNo、meterStdId、acctDate ...
```

如果包路径无法从表名推断，询问用户。

---

## 第三步：生成四层代码

按顺序输出以下四个文件，每个文件前注明文件路径。

### 1. Entity 实体类

规范要点（详见 `references/entity-standards.md`）：
- 必须继承 `CoreEntity`
- 使用 `@TableName("表名")` 注解
- 不要重复定义 `id`、`companyId`、`createUserNo`、`updateUserNo`、`createDateTime`、`updateDateTime`（这六个字段由 CoreEntity 提供）
- 字段名使用驼峰命名，对应数据库下划线字段名
- 使用 `@ApiModelProperty(value = "字段中文名")` 注解每个字段
- 使用 Lombok `@Getter @Setter`（不用 `@Data`，避免 equals/hashCode 问题）
- 不要在实体类中用 `int` 表示状态字段；布尔字段不要以 `isXxx` 命名
- 状态枚举字段使用 `XxxStatusType` 枚举类型（生成代码时注明：需按枚举规范单独创建枚举类）

模板：
```java
package com.jhict.{模块}.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.common.core.entity.CoreEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * {表中文名}
 */
@Getter
@Setter
@TableName("{表名}")
public class {EntityName} extends CoreEntity {

    @ApiModelProperty(value = "{字段中文名}")
    private {JavaType} {fieldName};

    // ... 其余业务字段
}
```

### 2. Mapper 接口

规范要点：
- 继承 `BaseMapper<Entity>`
- 接口上加 `@Mapper` 注解
- 基础 CRUD 无需自定义方法，MyBatis-Plus 已提供
- 只在有自定义复杂 SQL 需求时添加方法（此时生成空的方法签名，注释说明需补充 SQL）

模板：
```java
package com.jhict.{模块}.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jhict.{模块}.api.entity.{EntityName};
import org.apache.ibatis.annotations.Mapper;

/**
 * {表中文名} Mapper
 */
@Mapper
public interface {EntityName}Mapper extends BaseMapper<{EntityName}> {
}
```

### 3. Service 类

规范要点（详见 `references/service-standards.md`）：
- 继承 `JhServiceImpl<{EntityName}Mapper, {EntityName}>`
- 类注解：`@Log4j2 @Service @RequiredArgsConstructor`
- 如有多数据源，加 `@DS(XxxMicroServiceName.XXX)`（从表名模块推断，不确定时注释说明）
- **必须包含批量操作模板**（见 service-batch-operations 规范）：
  - `NOT_BLANK_FIELDS`、`UNIQUE_FIELDS` 两个常量（**留空列表，让开发者填充**）
  - `saveBatch`、`updateBatch`、`saveOrUpdateBatch` 方法
  - `validateSaveBatch`、`validateUpdateBatch`、`validateNotBlankBatch`、`validateUniqueBatch` 四个私有校验方法
  - `getUniqueTag` 静态工具方法
- 优先使用 `lambdaQuery()` / `lambdaUpdate()` 写法，不用 `new LambdaQueryWrapper<>()`
- 校验逻辑用 `ServiceAssert`，不用 `if-return` 返回错误
- 基础 CRUD 方法：`getById`、`list`（带条件查询）、`page`（分页）、`save`（新增）、`updateById`（更新）、`removeById`（删除）

模板：
```java
package com.jhict.{模块}.service;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhict.common.core.constant.BusConstant;
import com.jhict.common.core.util.BusinessUtil;
import com.jhict.{模块}.api.entity.{EntityName};
import com.jhict.{模块}.api.query.{EntityName}Query;
import com.jhict.{模块}.mapper.{EntityName}Mapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * {表中文名} Service
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class {EntityName}Service extends JhServiceImpl<{EntityName}Mapper, {EntityName}> {

    // TODO: 根据业务需求填充需要非空校验和唯一性校验的字段
    private static final List<SFunction<{EntityName}, ?>> NOT_BLANK_FIELDS = ListUtil.of(
        // {EntityName}::getXxx
    );

    private static final List<SFunction<{EntityName}, ?>> UNIQUE_FIELDS = ListUtil.of(
        // {EntityName}::getXxx
    );

    // ==================== 查询方法 ====================

    public {EntityName} getById(String id) {
        return super.getById(id);
    }

    public List<{EntityName}> list({EntityName}Query query) {
        return lambdaQuery()
            // .eq({EntityName}::getXxx, query.getXxx())  // 按需添加查询条件
            .list();
    }

    public Page<{EntityName}> page({EntityName}Query query) {
        return lambdaQuery()
            // .eq({EntityName}::getXxx, query.getXxx())  // 按需添加查询条件
            .page(new Page<>(query.getCurrent(), query.getSize()));
    }

    // ==================== 写操作 ====================

    public void save({EntityName} entity) {
        super.save(entity);
    }

    public void updateById({EntityName} entity) {
        super.updateById(entity);
    }

    public void removeById(String id) {
        super.removeById(id);
    }

    // ==================== 批量操作 ====================

    public void saveBatch(List<{EntityName}> entities) {
        this.validateSaveBatch(entities);
        super.saveBatch(entities, BusConstant.DEFAULT_DB_BATCH_SIZE);
    }

    public void updateBatch(List<{EntityName}> entities) {
        this.validateUpdateBatch(entities);
        super.updateBatchById(entities, BusConstant.DEFAULT_DB_BATCH_SIZE);
    }

    public void saveOrUpdateBatch(List<{EntityName}> entities) {
        BusinessUtil.saveOrUpdateBatch(
            entities,
            UNIQUE_FIELDS,
            super.baseMapper,
            this::saveBatch,
            this::updateBatch
        );
    }

    // ==================== 校验方法 ====================

    private void validateSaveBatch(List<{EntityName}> entities) {
        this.validateNotBlankBatch(entities);
        this.validateUniqueBatch(entities);
    }

    private void validateUpdateBatch(List<{EntityName}> entities) {
        this.validateNotBlankBatch(entities);
        this.validateUniqueBatch(entities);
    }

    private void validateNotBlankBatch(List<{EntityName}> entities) {
        BusinessUtil.validateNotBlankBatch(entities, NOT_BLANK_FIELDS);
    }

    private void validateUniqueBatch(List<{EntityName}> entities) {
        BusinessUtil.validateUniqueBatch(super.baseMapper, entities, UNIQUE_FIELDS);
    }

    public static String getUniqueTag({EntityName} entity) {
        return BusinessUtil.extractUniqueTag(entity, UNIQUE_FIELDS);
    }
}
```

同时生成对应的 Query 查询对象：

```java
package com.jhict.{模块}.api.query;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * {表中文名} 查询参数
 */
@Getter
@Setter
public class {EntityName}Query extends PageQuery {

    @ApiModelProperty(value = "{常用查询字段中文名}")
    private String {queryField};

    // 按需添加其他查询字段
}
```

### 4. Controller 类

规范要点（详见 `references/controller-standards.md`）：
- 类注解：`@RestController @RequestMapping("/模块标识/{entityKebabName}") @Api(tags = "{表中文名}管理")`
- 所有方法返回 `ApiResult`，不返回其他类型
- 不做任何异常处理（框架自动处理）
- 只调用 Service，不调用 Mapper 或其他 Controller
- 接口路径命名规范：list* / get* / save* / update* / remove*
- 查询用 GET，新增用 POST，更新用 PUT，删除用 DELETE
- 代码量尽量少，业务逻辑不写在 Controller

模板：
```java
package com.jhict.{模块}.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhict.common.core.result.ApiResult;
import com.jhict.{模块}.api.entity.{EntityName};
import com.jhict.{模块}.api.query.{EntityName}Query;
import com.jhict.{模块}.service.{EntityName}Service;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * {表中文名} Controller
 */
@RestController
@RequestMapping("/{模块路由标识}/{entity-kebab-name}")
@Api(tags = "{表中文名}管理")
@RequiredArgsConstructor
public class {EntityName}Controller {

    private final {EntityName}Service {entityName}Service;

    @ApiOperation("分页查询{表中文名}")
    @GetMapping("page")
    public ApiResult page({EntityName}Query query) {
        Page<{EntityName}> page = {entityName}Service.page(query);
        return ApiResult.success(page);
    }

    @ApiOperation("查询{表中文名}列表")
    @GetMapping("list")
    public ApiResult list({EntityName}Query query) {
        return ApiResult.success({entityName}Service.list(query));
    }

    @ApiOperation("获取{表中文名}详情")
    @GetMapping("get")
    public ApiResult get(String id) {
        return ApiResult.success({entityName}Service.getById(id));
    }

    @ApiOperation("新增{表中文名}")
    @PostMapping("save")
    public ApiResult save(@RequestBody {EntityName} entity) {
        {entityName}Service.save(entity);
        return ApiResult.success("新增成功");
    }

    @ApiOperation("更新{表中文名}")
    @PutMapping("update")
    public ApiResult update(@RequestBody {EntityName} entity) {
        {entityName}Service.updateById(entity);
        return ApiResult.success("更新成功");
    }

    @ApiOperation("删除{表中文名}")
    @DeleteMapping("remove")
    public ApiResult remove(String id) {
        {entityName}Service.removeById(id);
        return ApiResult.success("删除成功");
    }
}
```

---

## 第四步：生成后的提示

输出完所有代码后，给用户以下提示：

```
📝 代码生成完毕，请注意以下几点需要手动处理：

1. **包路径**：请确认 `com.jhict.{模块}` 是否与你的项目实际包路径一致
2. **NOT_BLANK_FIELDS 和 UNIQUE_FIELDS**：根据业务需求填充需要非空/唯一校验的字段
3. **查询条件**：在 Service 的 list() 和 page() 方法中，取消注释并补充实际的查询条件
4. **多数据源注解**：如果该模块需要指定数据源，在 Service 类上加 @DS(...)
5. **枚举字段**：如果表中有多状态字段（>2种状态），请按枚举规范单独创建枚举类
6. **Query 基类**：确认 PageQuery 基类路径是否正确
```

---

## 参考规范文件

详细规范见 `references/` 目录：
- `entity-standards.md` — 实体类规范
- `controller-standards.md` — 控制层规范
- `service-standards.md` — 服务层规范
- `batch-operations.md` — 批量操作模板
- `object-standards.md` — BO/EO/Query 对象规范
- `enum-standards.md` — 枚举规范
