---
description: 控制层开发规范
globs: **/*Controller.java
alwaysApply: false
---

# 控制层开发规范

## 代码量原则
控制层的代码量应该尽可能少，因为不可复用，只做简单的参数处理或数据聚合，业务逻辑在service层做。

## 返回对象
所有controller层的返回对象都是ApiResult，不要返回其他类型。

## 异常处理
控制层不需要做异常处理，在网关层和feign中间会自动将异常转为前端可读message。
- 平台封装了ServiceException，抛出的异常如果是ServiceException，会自动转换异常里的消息体给前端作为提示信息
- 如果是空指针、sql等用户不可读异常，会提示系统错误并打印异常堆栈到控制台，防止后台信息泄露到前端
- 一般情况下用断言ServiceAssert可以满足需要，特殊情况下用throw new ServiceException

## 接口路径规范
- 不要使用`/get/{id}`格式路径参数
- 路径参数不要超过三层（服务路由/controller上的标识/接口上的标识）
- 所有查询多条的用`list`开头做接口
- 更新用`update`开头
- 删除用`remove`开头
- 查询单个用`get`开头
- 保存用`save`开头

示例：
```java
@PostMapping("startFlow")
public ApiResult startFlow(@RequestBody FlowStartQuery flowStartQuery) {
    return ApiResult.success("发起成功", templateService.start(flowStartQuery));
}
```

## 请求方式
- 查询类型的请求方式用GET
- 新增用POST
- 更新用PUT
- 删除用DELETE

## 调用规范
controller里只能调用service，不能调用其他的controller接口或者mapper接口。
