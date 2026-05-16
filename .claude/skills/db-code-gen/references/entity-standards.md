---
description: 实体类开发规范
globs: **/*Entity.java
alwaysApply: false
---

# 实体类开发规范

## 继承CoreEntity
所有业务实体类必须继承CoreEntity，包含五个基础字段：

```java
public class OaMessage extends CoreEntity {
    // 业务字段...
}
```

## 基础字段
所有业务表要求包含五个基础字段，添加这些字段后新增、更新时的自动填充以及AuthUtil可以调用到。

### 实体类字段定义
```java
/** 主键，采用雪花算法生成策略 */
@ApiModelProperty(value = "id")
protected String id;

@TableField(fill = FieldFill.INSERT)
@ApiModelProperty(value = "公司ID", hidden = true)
protected String companyId;

@TableField(fill = FieldFill.INSERT)
@ApiModelProperty(value = "创建人工号:strAccount", hidden = true)
protected String createUserNo;

@TableField(fill = FieldFill.INSERT_UPDATE)
@ApiModelProperty(value = "修改人工号:strAccount", hidden = true)
protected String updateUserNo;

@TableField(fill = FieldFill.INSERT)
@ApiModelProperty(value = "创建日期", hidden = true)
protected String createDateTime;

@TableField(fill = FieldFill.INSERT_UPDATE)
@ApiModelProperty(value = "修改日期", hidden = true)
protected String updateDateTime;
```

## 字段命名规范
- 数据库建立字段请规范使用下划线形式（如：USER_NO）
- 实体类中字段请使用驼峰命名（如：userNo）
- 例如：实体类中 `userNo` 对应数据库 `USER_NO`

## 布尔类型
- 对于一般只有两种状态的字段（例如正常状态和异常状态），请使用Boolean，避免使用Int
- 字段不要以`isXxx`命名，会遇到很多问题，例如一些反射场景
- 状态多余是/否两种时，使用枚举

## 自动填充
实体类继承CoreEntity后，可在新增或修改时自动填充数据：
- 新增：自动填充 创建人、创建时间、更新人、更新时间、公司别
- 更新：自动填充 更新人、更新时间
