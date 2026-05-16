---
description: 枚举使用规范
globs: **/*Enum*.java
alwaysApply: false
---

# 枚举使用规范

## 使用场景
目前平台有枚举从前端到服务端到数据库的自动转化。使用枚举可以有效避免频繁依靠前后端开发人员自己转换int或string等表示多种字段的繁琐过程。

## 枚举定义
枚举类必须实现MapEnum接口，使用@EnumValue注解标记key字段。

```java
public enum TenUserStatusType implements MapEnum {
    NORMAL(1, "正常"),
    DISABLED(2, "禁用"),
    AUDIT(3, "待审核");
    
    @EnumValue
    private final int key;
    private final String value;
    
    TenUserStatusType(int key, String value) {
        this.key = key;
        this.value = value;
    }
    
    @Override
    public int getKey() {
        return key;
    }
    
    @Override
    public String getValue() {
        return value;
    }
}
```

## 实体类中使用
```java
// 正确写法
public class TenUser {
    private TenUserStatusType status;
}

// 错误写法
public class TenUser {
    @ApiModelProperty(value = "1正常2停用3审核")
    private int status; // ❌ 错误
}
```

## 后端代码判断
```java
if(tenUser.getStatus() != TenUserStatusType.NORMAL) {
    // 处理逻辑
}
```

## 前端使用
后端给前端的status（通过平台定义的序列化功能自动转化）：
```json
{
    "value": "正常",
    "key": 1
}
```

### 列表显示
```vue
<!-- 使用枚举后 -->
<el-table-column label="状态" align="center" prop="status.value">
</el-table-column>

<!-- 之前的写法 -->
<el-table-column label="状态" align="center" prop="status">
    <template v-slot="{row}">
        <span>{{row.status==1?'正常':row.status==3?'待审核':'停用'}}</span>
    </template>
</el-table-column>
```

### 表单使用
```vue
<el-select v-if="form.status" v-model="form.status.key" />
```

注意：为避免存在空对象引起前端异常，须为枚举对象赋值：
```javascript
afterEdit() {
    this.form.status = this.form.status || {};
}
```

## 配置要求
1. 新建服务后端需加入相关配置类，用于feign之间的序列化转化以及前端到后端的序列化转化
2. nacos上补充mybatis-plus配置枚举类包位置：
```yaml
mybatis-plus:
  type-enums-package: com.jhict.*.api.enums
```
