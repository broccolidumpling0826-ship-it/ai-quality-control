---
name: walsin-quality-mysql-db-code-gen
description: 根据数据库表设计（以表格形式描述的字段信息）自动生成符合华新质量模块项目规范的 Java Spring Boot 后端业务代码。当用户提及"华新"、"华新丽华"、"质量模块"、"质量规范"、"产品规范"、"冶金规范"、"质量设计"、"质量判定"、"质保书"、"质量监控"等关键词，或提供数据库表结构并要求生成质量模块代码时，必须使用此 skill。生成的代码包含 Entity、Mapper（含 XML）、Service、Controller 四层，遵循华新质量项目内部规范（MySQL 小写字段、QualityServiceImpl、CommonBizUtils、JhPage 分页、LockUtils 分布式锁、@Transactional 事务等）。
---

# 华新质量模块 MySQL → 后端代码生成 Skill

## 目标

用户提供数据库表设计（表格、SQL 或口述字段），生成完整的后端代码，严格遵循华新质量模块的项目规范。

---

## 第一步：询问包路径（必须，不可跳过）

**在解析表结构之前，必须先向用户提问：**

> 请提供代码的父级包路径（例如：`com.jhict.quality.qmpc`），生成的各层代码将按此路径自动分配到对应子包下。

如果用户**未提供包路径**，则立即停止执行，返回以下提示并退出：

```
❌ 未指定包路径，代码生成已终止。
请提供父级包路径（如 com.jhict.quality.qmpc），再重新发起请求。
```

**包路径生成规则（以用户提供的 `{basePackage}` 为例）：**

| 层 | 包路径 | 说明 |
|---|---|---|
| Entity | `{basePackage}.api.entity` | 实体类 |
| Query | `{basePackage}.api.query` | 查询参数对象 |
| DTO | `{basePackage}.api.dto` | 数据传输对象 |
| EO | `{basePackage}.api.eo` | 导出对象 |
| BO | `{basePackage}.api.bo` | 业务对象 |
| Service | `{basePackage}.service` | 服务层 |
| Mapper | `{basePackage}.mapper` | 数据访问层接口 |
| Controller | `{basePackage}.controller` | 控制层 |

Mapper XML 文件放在模块的 `/resources/mybatis/{module}/` 目录下，`{module}` 为 `{basePackage}` 最后一段（如 `com.jhict.quality.qmpc` → `qmpc`）。

---

## 第二步：解析表结构

用户输入可能是：
- Markdown 表格（字段英文名 / 字段中文名 / 类型 / 长度 / 是否主键 / 是否为空 / 默认值）
- CREATE TABLE SQL 语句（MySQL DDL 格式）
- 口头描述的字段列表

**MySQL 命名规范：** 表名和字段名均为**小写字母 + 下划线分隔**（如 `qmpc_alarm_content`、`alarm_content_type`）。

从中提取：
- 表名（小写下划线）
- 实体类名（大驼峰，如 `QmpcAlarmContent`）
- 每个字段的：中文名、Java 类型、是否主键、是否为空

**基础字段识别（不生成到实体类中）：**

以下字段已由 `CoreEntity` 提供，若表结构中包含，**跳过不重复声明**：
- `id` — 主键
- `company_id` / `companyId` — 公司ID
- `create_user_no` / `createUserNo` — 创建人工号
- `update_user_no` / `updateUserNo` — 修改人工号
- `create_date_time` / `createDateTime` — 创建日期
- `update_date_time` / `updateDateTime` — 修改日期

**MySQL 字段类型映射规则：**

| MySQL 类型 | Java 类型 |
|---|---|
| varchar / char / text / longtext | `String` |
| int / tinyint / smallint | `Integer` |
| bigint | `Long` |
| decimal / numeric（有小数位） | `BigDecimal` |
| date / datetime / timestamp | `String`（项目用字符串存日期） |
| tinyint(1) 或只有 0/1 两种值的字段 | `Boolean` |
| 只有两种状态的整型状态字段 | `Boolean` |

**布尔类型判断：** 只表示是/否的字段用 `Boolean`，字段名不加 `is` 前缀；多于两种状态时使用枚举。

---

## 第三步：确认生成信息

生成前输出确认摘要：

```
将为以下表生成代码：
- 表名：qmpc_alarm_content
- 实体类名：QmpcAlarmContent
- 父级包路径：com.jhict.quality.qmpc
- 业务字段（不含 CoreEntity 基础字段）：alarmContentType、ruleCode ...
```

---

## 第四步：生成六类代码文件

按顺序输出以下文件，每个文件前注明完整文件路径。

### 1. Entity 实体类

路径：`{basePackage}.api.entity/{EntityName}.java`

规范要点：
- 必须继承 `CoreEntity`
- `@TableName("表名")` 注解（表名小写下划线）
- 不重复定义 CoreEntity 中的六个基础字段
- **外键字段注释规范**：如果某个字段在表设计中标注为外键或关联其他表，`@ApiModelProperty` 的 `value` 中除字段中文名外，必须补充关联的数据库表名，格式为：`"字段中文名（关联表：{关联表名}）"`。若表设计中未明确说明关联表名，**必须先向用户提问**，确认关联的表名后再生成，并在确认摘要中列出所有外键字段及其关联表，让用户最终确认。
- 字段驼峰命名，每个字段加 `@ApiModelProperty(value = "字段中文名")`
- 使用 Lombok `@Getter @Setter`（不用 `@Data`）
- 布尔字段不加 `is` 前缀；多状态字段建议枚举并加注释提示

```java
package {basePackage}.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.common.core.entity.CoreEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author admin
 * @className {EntityName}
 * @date {today}
 * @description {表中文名}
 **/
@Getter
@Setter
@TableName("{table_name}")
public class {EntityName} extends CoreEntity {

    @ApiModelProperty(value = "{字段中文名}")
    private {JavaType} {fieldName};

    // ... 其余业务字段
}
```

### 2. Mapper 接口

路径：`{basePackage}.mapper/{EntityName}Mapper.java`

规范要点：
- 继承 `JhBaseMapper<Entity>`（包路径：`com.jhict.common.data.mapper.JhBaseMapper`），加 `@Mapper` 注解
- 必须声明 `list(JhPage page, {EntityName}Query query)` 方法，与 XML 中的分页查询对应

```java
package {basePackage}.mapper;

import com.jhict.common.data.mapper.JhBaseMapper;
import com.jhict.common.data.mybatis.entity.JhPage;
import {basePackage}.api.entity.{EntityName};
import {basePackage}.api.query.{EntityName}Query;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author admin
 * @className {EntityName}Mapper
 * @date {today}
 * @description {表中文名} Mapper
 **/
@Mapper
public interface {EntityName}Mapper extends JhBaseMapper<{EntityName}> {

    /**
     * 分页查询
     */
    void list(@Param("page") JhPage page, @Param("query") {EntityName}Query query);
}
```

### 3. Mapper XML

路径：`/resources/mybatis/{module}/{EntityName}Mapper.xml`

规范要点：
- namespace 对应 Mapper 接口全限定名
- `<resultMap>` 只映射业务字段（不含 CoreEntity 基础字段）
- `list` 方法使用 `<where>` + `<if>` 动态条件，ORDER BY create_date_time DESC

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="{basePackage}.mapper.{EntityName}Mapper">

    <resultMap id="{entityName}Map" type="{basePackage}.api.entity.{EntityName}">
        <!-- 业务字段映射，基础字段由 CoreEntity 自动处理 -->
        <result column="{db_field}" property="{javaField}"/>
        <!-- ... 其他业务字段 -->
    </resultMap>

    <!-- 分页查询 -->
    <select id="list" resultMap="{entityName}Map">
        SELECT * FROM {table_name}
        <where>
            <if test="query.{field} != null and query.{field} != ''">
                AND {db_field} = #{query.{field}}
            </if>
            <!-- 按需补充其他查询条件 -->
        </where>
        ORDER BY create_date_time DESC
    </select>

</mapper>
```

### 4. Service 类

路径：`{basePackage}.service/{EntityName}Service.java`

规范要点：
- 继承 `QualityServiceImpl<{EntityName}Mapper, {EntityName}>`（包路径：`com.jhict.quality.base.service.QualityServiceImpl`）
- 类注解：`@Log4j2 @Service @RequiredArgsConstructor`
- **不加** `@DS(...)` 数据源注解
- **两个字段常量**（均留空，由开发者根据业务需求填充）：
  - `NOT_BLANK_FIELDS`：非空校验字段集合
  - `UNIQUE_FIELDS`：唯一性校验字段集合
- **单条写操作方法**：`save` / `updateById` / `removeById` / `copy`，方法体先调用对应 validate 方法校验，再调用 super 父类方法，加 `@Transactional(rollbackFor = Exception.class)`
  - `save` 调用 `validateSave(entity)` → `super.save(entity, null)`
  - `updateById` 调用 `validateUpdate(entity)` → `super.updateById(entity, null)` （注：`QualityServiceImpl.updateById` 有带 `Consumer` 的重载，传 `null`）
  - `removeById` 直接调用 `super.removeById(id, null)`
  - `copy` 调用 `validateSave(entity)`（copy 本质是新增） → `clearBaseField(entity)` → `super.copy(entity)`
- **单条校验方法**（package-private 即可）：
  - `validateSave(Entity)` 内部调用 `this.validateSaveBatch(Collections.singletonList(entity))`
  - `validateUpdate(Entity)` 内部调用 `this.validateUpdateBatch(Collections.singletonList(entity))`
- **批量操作方法**（均加 `@Transactional(rollbackFor = Exception.class)`）：
  - `saveBatch`：先调 `validateSaveBatch`，再调 `super.saveBatch(entities, null)`（QualityServiceImpl 带 Consumer 重载，传 null）
  - `updateBatch`：先调 `validateUpdateBatch`，再调 `super.updateBatchById(entities, null)`（QualityServiceImpl 带 Consumer 重载，传 null）
  - `copyBatch`：先调 `validateSaveBatch`，对每个实体调用 `clearBaseField`，再调 `super.copyBatch(entities)`
  - `saveOrUpdateBatch`：调用 `CommonBizUtils.saveOrUpdateBatch(entities, UNIQUE_FIELDS, super.baseMapper, this::saveBatch, this::updateBatch)`
- **四个私有校验方法**（严格按 references/batch-operations.md 规范，工具类替换为 `CommonBizUtils`）：
  - `validateSaveBatch`：先调 `validateNotBlankBatch` 再调 `validateUniqueBatch`
  - `validateUpdateBatch`：先调 `validateNotBlankBatch` 再调 `validateUniqueBatch`
  - `validateNotBlankBatch`：调用 `CommonBizUtils.validateNotBlankBatch(entities, NOT_BLANK_FIELDS)`
  - `validateUniqueBatch`：调用 `CommonBizUtils.validateUniqueBatch(super.baseMapper, entities, UNIQUE_FIELDS)`
- **`clearBaseField(Entity)` 方法**：`private` 方法，调用 `BusinessUtils.cleanCoreEntity(entity)` 清空 CoreEntity 基础字段，之后留一行 TODO 注释供用户补充需要重置的其他业务字段。`copy` 和 `copyBatch` 在调用父类方法前必须先调用此方法，防止把源实体的基础字段一同复制入库。
- **静态工具方法** `getUniqueTag`：调用 `CommonBizUtils.extractUniqueTag(entity, UNIQUE_FIELDS)`
- `list(JhPage, Query)` 方法调用 `baseMapper.list(page, query)`
- 不要添加分区注释（如 `// === 查询方法 ===`）

```java
package {basePackage}.service;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.jhict.common.data.mybatis.entity.JhPage;
import com.jhict.quality.base.service.QualityServiceImpl;
import com.jhict.quality.base.utils.BusinessUtils;
import com.jhict.quality.base.utils.CommonBizUtils;
import {basePackage}.api.entity.{EntityName};
import {basePackage}.api.query.{EntityName}Query;
import {basePackage}.mapper.{EntityName}Mapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * @author admin
 * @className {EntityName}Service
 * @date {today}
 * @description {表中文名}
 **/
@Log4j2
@Service
@RequiredArgsConstructor
public class {EntityName}Service extends QualityServiceImpl<{EntityName}Mapper, {EntityName}> {

    // TODO: 根据业务需求填充需要非空校验的字段
    private static final List<SFunction<{EntityName}, ?>> NOT_BLANK_FIELDS = ListUtil.of(
        // {EntityName}::getXxx
    );

    // TODO: 根据业务需求填充需要唯一性校验的字段
    private static final List<SFunction<{EntityName}, ?>> UNIQUE_FIELDS = ListUtil.of(
        // {EntityName}::getXxx
    );

    public void list(JhPage page, {EntityName}Query query) {
        baseMapper.list(page, query);
    }

    public {EntityName} getById(String id) {
        return super.getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save({EntityName} entity) {
        validateSave(entity);
        super.save(entity, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateById({EntityName} entity) {
        validateUpdate(entity);
        super.updateById(entity, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeById(String id) {
        super.removeById(id, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void copy({EntityName} entity) {
        validateSave(entity);
        clearBaseField(entity);
        super.copy(entity);
    }

    void validateSave({EntityName} entity) {
        this.validateSaveBatch(Collections.singletonList(entity));
    }

    void validateUpdate({EntityName} entity) {
        this.validateUpdateBatch(Collections.singletonList(entity));
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveBatch(List<{EntityName}> entities) {
        this.validateSaveBatch(entities);
        super.saveBatch(entities, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateBatch(List<{EntityName}> entities) {
        this.validateUpdateBatch(entities);
        super.updateBatchById(entities, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void copyBatch(List<{EntityName}> entities) {
        this.validateSaveBatch(entities);
        entities.forEach(this::clearBaseField);
        super.copyBatch(entities);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateBatch(List<{EntityName}> entities) {
        CommonBizUtils.saveOrUpdateBatch(
            entities,
            UNIQUE_FIELDS,
            super.baseMapper,
            this::saveBatch,
            this::updateBatch
        );
    }

    private void validateSaveBatch(List<{EntityName}> entities) {
        this.validateNotBlankBatch(entities);
        this.validateUniqueBatch(entities);
    }

    private void validateUpdateBatch(List<{EntityName}> entities) {
        this.validateNotBlankBatch(entities);
        this.validateUniqueBatch(entities);
    }

    private void validateNotBlankBatch(List<{EntityName}> entities) {
        CommonBizUtils.validateNotBlankBatch(entities, NOT_BLANK_FIELDS);
    }

    private void validateUniqueBatch(List<{EntityName}> entities) {
        CommonBizUtils.validateUniqueBatch(super.baseMapper, entities, UNIQUE_FIELDS);
    }

    public static String getUniqueTag({EntityName} entity) {
        return CommonBizUtils.extractUniqueTag(entity, UNIQUE_FIELDS);
    }

    private void clearBaseField({EntityName} entity) {
        BusinessUtils.cleanCoreEntity(entity);
        // TODO: 根据业务需求清空其他需要重置的业务字段（如编码、状态等）
    }
}
```

### 5. Controller 类

路径：`{basePackage}.controller/{EntityName}Controller.java`

规范要点：
- 类注解：`@RestController @RequiredArgsConstructor @RequestMapping("/{entityCamelName}") @Api(value = "{表中文名}")`
- 接口路径只有一层，格式为 `/{实体类名首字母小写}`，例如：`QmmcProcessTechParam` → `@RequestMapping("/qmmcProcessTechParam")`
- 所有方法返回 `ApiResult`（`com.jhict.common.core.entity.ApiResult`）
- **增、改、复制** 接口：用 `LockUtils.lock({EntityName}.class.getName(), () -> service.xxx(entity))` 加分布式锁
- **删除** 接口：直接调用，不加锁
- list 接口：`@ApiIgnore JhPage page` + Query 参数，调用 `service.list(page, query)`
- 包含 `copy` 复制接口
- `@RequestBody` 参数加 `@Valid`

```java
package {basePackage}.controller;

import com.jhict.common.core.entity.ApiResult;
import com.jhict.common.data.mybatis.entity.JhPage;
import com.jhict.quality.base.utils.LockUtils;
import {basePackage}.api.entity.{EntityName};
import {basePackage}.api.query.{EntityName}Query;
import {basePackage}.service.{EntityName}Service;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;

/**
 * @author admin
 * @className {EntityName}Controller
 * @date {today}
 * @description {表中文名}
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/{entityCamelName}")
@Api(value = "{表中文名}")
public class {EntityName}Controller {

    private final {EntityName}Service service;

    @ApiOperation(value = "获取列表", notes = "获取列表")
    @ApiImplicitParams(value = {})
    @GetMapping("/list")
    public ApiResult<JhPage<List<{EntityName}>>> list(@ApiIgnore JhPage page, {EntityName}Query query) {
        service.list(page, query);
        return ApiResult.success("查询列表成功！", page);
    }

    @ApiOperation(value = "通过id查询", notes = "通过id查询")
    @ApiImplicitParams(value = {})
    @GetMapping("/getById")
    public ApiResult<{EntityName}> getById(String id) {
        return ApiResult.success("根据 ID 获取详情成功!", service.getById(id));
    }

    @ApiOperation(value = "新增", notes = "新增")
    @ApiImplicitParams(value = {})
    @PostMapping("/save")
    public ApiResult<{EntityName}> save(@Valid @RequestBody {EntityName} entity) {
        LockUtils.lock({EntityName}.class.getName(), () -> service.save(entity));
        return ApiResult.success("新增成功!", entity);
    }

    @ApiOperation(value = "复制", notes = "复制")
    @ApiImplicitParams(value = {})
    @PostMapping("/copy")
    public ApiResult<{EntityName}> copy(@Valid @RequestBody {EntityName} entity) {
        LockUtils.lock({EntityName}.class.getName(), () -> service.copy(entity));
        return ApiResult.success("复制成功!", entity);
    }

    @ApiOperation(value = "编辑", notes = "编辑")
    @ApiImplicitParams(value = {})
    @PutMapping("/update")
    public ApiResult update(@Valid @RequestBody {EntityName} entity) {
        LockUtils.lock({EntityName}.class.getName(), () -> service.updateById(entity));
        return ApiResult.success("编辑成功!");
    }

    @ApiOperation(value = "删除", notes = "删除")
    @ApiImplicitParams(value = {})
    @DeleteMapping("/remove")
    public ApiResult remove(String id) {
        service.removeById(id);
        return ApiResult.success("删除成功!");
    }
}
```

### 6. Query 查询对象

路径：`{basePackage}.api.query/{EntityName}Query.java`

```java
package {basePackage}.api.query;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author admin
 * @className {EntityName}Query
 * @date {today}
 * @description {表中文名} 查询参数
 **/
@Getter
@Setter
public class {EntityName}Query {

    @ApiModelProperty(value = "ID集合（回显用）")
    private List<String> ids;

    @ApiModelProperty(value = "{常用查询字段中文名}")
    private String {queryField};

    // 按需添加其他查询字段
}
```

---

## 第五步：生成后的提示

```
📝 代码生成完毕，请注意以下几点需要手动处理：

1. **包路径**：确认 {basePackage} 与实际项目包结构一致
2. **UNIQUE_FIELDS**：根据业务需求填充需要唯一性校验的字段
3. **XML 查询条件**：在 {EntityName}Mapper.xml 的 list 查询中补充实际的条件字段
4. **Service 业务方法**：save/updateById/copy 方法体中只有 super 调用，请根据业务需求补充初始化逻辑（如编码生成、唯一校验等）
5. **Query 字段**：在 {EntityName}Query 中补充实际需要的查询条件字段
6. **枚举字段**：多状态字段（>2种状态）请按枚举规范单独创建枚举类
```

---

## 参考规范文件

详细规范见 `references/` 目录：
- `entity-standards.md` — 实体类规范
- `controller-standards.md` — 控制层规范
- `service-standards.md` — 服务层规范
- `batch-operations.md` — 批量操作模板（工具类为 `CommonBizUtils`，批量大小为 `CommonBizConstants.DEFAULT_DB_BATCH_SIZE`）
- `object-standards.md` — BO/EO/Query 对象规范
- `enum-standards.md` — 枚举规范
