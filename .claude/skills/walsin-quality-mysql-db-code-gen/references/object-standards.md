---
description: BO、EO、Query对象使用规范
alwaysApply: true
---

# 对象使用规范

## BO（Business Object）业务对象
开发场景中经常会遇到需要把业务逻辑封装为一个对象，这个对象可以包括一个或多个其他的对象。此时不要在原实体类中加入其他对象，而应该新建单独的BO业务对象。

### 使用场景
比如一份简历，有教育经历、工作经历、社会关系等。可以把教育经历对应一个entity，工作经历对应一个entity，社会关系对应一个entity，然后建立一个对应的BO来处理简历，每个BO包含这些entity，这样就可以针对BO去处理业务逻辑。

### 示例
```java
@Getter
@Setter
public class HrUserBO {
    private HrUser user;
    private HrDepartment dept;
}
```

## EO（Export Object）导出对象
导入导出Excel相关业务请新建立EO对象。

### 示例
例如HrUser，请新建HrUserEO，仅包含需要导出的字段。

## Query查询对象
查询类型的参数使用query，如ProjectQuery。

## 工具类
- 工具类优先使用`com.jhict.common.core.util`包下的util
- 不要使用三方包的util，方便定位错误和升级
- 如不满足，可以自行封装或使用三方包的util
