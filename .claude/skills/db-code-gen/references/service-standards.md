---
description: 服务层开发规范
globs: **/*Service.java
alwaysApply: false
---

# 服务层开发规范

## 代码原则
服务层是绝大多数复用逻辑的代码，这里代码体量应该是项目中最大的，注意解耦和SRP（单一职责）原则。

## 断言使用
使用ServiceAssert进行业务断言，不要使用if-return的方式返回错误信息。

### 错误写法
```java
public ApiResult validate(PurReimbursementBill bill, ValidatePattern validatePattern) {
    if(bill.getInvoiceNo() == null || "".equals(bill.getInvoiceNo())){
        return ApiResult.failure("发票代码不可为空");
    }
    // ...
}
```

### 正确写法
```java
public void validate(PurReimbursementBill bill, ValidatePattern validatePattern) {
    ServiceAssert.isTrue(StringUtils.isNotEmpty(bill.getInvoiceNo()), "发票代码不可为空");
    ServiceAssert.isTrue(StringUtils.isNotEmpty(bill.getInvoiceDay()), "发票日期不可为空");
    ServiceAssert.isNotNull(bill.getTaxRate(), "税率不可为空");
    ServiceAssert.isNotNull(bill.getTotalAmount(), "总额不可为空");
    ServiceAssert.isNotNull(bill.getAmount(), "金额不可为空");
    ServiceAssert.isNotNull(bill.getTaxAmount(), "税额不可为空");
}
```

### 常用断言方法
- `ServiceAssert.isTrue(expression, message)` - 判断表达式是否为true
- `ServiceAssert.isNotNull(object, message)` - 判断对象不为null
- `ServiceAssert.isNull(object, message)` - 判断对象为null
- `ServiceAssert.hasText(text, message)` - 判断字符串不为空
- `ServiceAssert.arrayHasValue(list, message)` - 判断集合不为空
- `ServiceAssert.arrayContains(target, resource, message)` - 判断集合包含某元素

## Service与Mapper关系
- service和mapper是一一对应的关系，比如projectService只能调用projectMapper
- 不能跨service调用mapper，如projectService不能调用presaleMapper
- 可以调用其他service，如projectService可以调用presaleService
- mapper的接口都必须被service封装一层后再被其他接口调用

示例：
```java
// 错误：直接跨service调用mapper
projectService.deleteBOById() {
    presaleMapper.getBOById(); // ❌ 错误
}

// 正确：通过service调用
projectService.deleteBOById() {
    presaleService.getBOById(); // ✅ 正确
}
```

## LambdaWrapper使用
优先使用lambda写法，可以省去泛型代码的编写。

```java
// 推荐写法
public List<FlowTemplateCc> listByTempId(String templateId) {
    return lambdaQuery().eq(FlowTemplateCc::getTempId, templateId).list();
}

// 不推荐写法
LambdaQueryWrapper<CodeModel> queryWrapper = new LambdaQueryWrapper<>();
queryWrapper.eq(CodeModel::getDsId, dsId);
return modelService.list(queryWrapper);
```

## 事务
- 单机事务不需要加事务注解，resource下有事务全局配置文件
- 全局事务用seata
