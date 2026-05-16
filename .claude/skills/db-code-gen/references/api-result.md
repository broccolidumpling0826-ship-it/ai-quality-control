---
description: ApiResult统一返回对象使用规范
globs: **/*Controller.java
alwaysApply: false
---

# ApiResult 统一返回对象规范

## 状态码定义
```java
public final static int CODE_SUCCESS = 2000;        // 成功
public final static int CODE_BAD_REQUEST = 4000;     // 请求错误
public final static int CODE_UNAUTHORIZED = 4001;    // 未登录
public final static int CODE_FORBIDDEN = 4003;       // 无权限
public final static int CODE_NOT_FOUND = 4004;       // 无效请求
public final static int CODE_SERVER_ERROR = 5000;    // 服务器错误
```

## 使用方式

### 成功返回
```java
// 只返回成功信息，无数据
ApiResult.success();

// 返回成功信息和数据
ApiResult.success("新增成功", data);

// 只返回成功信息
ApiResult.success("操作成功");
```

### 失败返回
```java
// 返回失败信息，无数据
ApiResult.failure("操作失败");

// 返回失败信息和数据
ApiResult.failure("操作失败", errorData);
```

### 自定义返回
```java
ApiResult.normal(code, message, data);
```

## 注意事项
- 所有controller层的返回对象都必须是ApiResult
- 不要返回其他类型或直接返回数据对象
