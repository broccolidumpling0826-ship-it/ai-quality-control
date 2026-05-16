# Contract: 认证接口

**Base Path**: `/api/v1/auth`
**Return format**: `ApiResult<T>` — code 2000=成功, 4000=请求错误, 4001=未登录, 5000=服务器错误

---

## POST /login — 用户登录

**Request**:
```json
{ "userNo": "021835", "password": "******" }
```

**Response** (ApiResult<LoginVO>):
```json
{
  "code": 2000,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGci...",
    "userNo": "021835",
    "username": "张三",
    "role": "QUALITY_SUPERVISOR",
    "expiresIn": 86400
  }
}
```

**Errors**:
- 4000: 用户名或密码错误
- 4000: 账号已被禁用

---

## POST /logout — 退出登录

**Header**: `Authorization: Bearer {token}`

**Response**: `ApiResult.success("退出成功")`

**Side effect**: Token 加入 Redis 黑名单（TTL = token 剩余有效期）

---

## GET /user/info — 获取当前用户信息

**Header**: `Authorization: Bearer {token}`

**Response** (ApiResult<UserInfoVO>):
```json
{
  "code": 2000,
  "data": {
    "userNo": "021835",
    "username": "张三",
    "role": "QUALITY_SUPERVISOR",
    "permissions": ["VOID_INSPECTION", "INITIATE_REINSPECTION", "APPROVE_REJUDGMENT_NORMAL"]
  }
}
```
