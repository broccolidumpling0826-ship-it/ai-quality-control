# Contract: 标准库接口

**Base Path**: `/api/v1/standards`
**权限要求**: 标准维护操作（新增/修改/发布）需 `QUALITY_ENGINEER` 及以上角色

---

## POST / — 新增标准

**Request** (QcQualityStandardAddCmd):
```json
{
  "standardType": "CUSTOMER",
  "variety": "冷轧板",
  "grade": "Q235B",
  "specRange": "厚度 0.5-3.0mm，宽度 900-1250mm",
  "versionNo": "协议 C2025-088-v1",
  "effectiveDate": "2025-01-01",
  "expiryDate": "9999-12-31",
  "customerId": "CUST-001",
  "indicators": [
    {
      "indicatorId": "IND-001",
      "upperLimit": 510,
      "lowerLimit": 370,
      "isRequired": true,
      "concessionUpper": 530,
      "concessionLower": 360
    }
  ]
}
```

**Response**: `ApiResult.success("新增成功", { "id": "..." })`

**Errors**:
- 4000: 标准版本时间窗口与已有版本重叠，请调整生效日期
- 4000: 失效日期必须大于生效日期

---

## PUT /{id} — 修改标准（仅草稿状态可修改）

**Request**: 同新增（id 必填）

**Response**: `ApiResult.success("修改成功")`

---

## PUT /{id}/publish — 发布标准

发布时系统检查：同体系下是否有正在有效的旧版本未设置失效日期（发出提示）。

**Response**:
```json
{
  "code": 2000,
  "message": "发布成功",
  "data": {
    "needSetExpiryFor": [
      { "id": "...", "versionNo": "GB/T 700-2006", "currentExpiryDate": "9999-12-31" }
    ]
  }
}
```

---

## POST /page — 分页查询标准列表

**Request** (QcQualityStandardPageQuery):
```json
{
  "standardType": "NATIONAL",
  "variety": "冷轧板",
  "grade": "Q235B",
  "status": "PUBLISHED",
  "pageNum": 1,
  "pageSize": 20
}
```

**Response** (ApiResult<Page<QcQualityStandardVO>>):
```json
{
  "code": 2000,
  "data": {
    "total": 5,
    "records": [
      {
        "id": "...",
        "standardType": "NATIONAL",
        "variety": "冷轧板",
        "grade": "Q235B",
        "versionNo": "GB/T 912-2008",
        "effectiveDate": "2009-06-01",
        "expiryDate": "9999-12-31",
        "status": "PUBLISHED",
        "indicatorCount": 8
      }
    ]
  }
}
```

---

## GET /{id} — 查询标准详情（含指标列表）

**Response** (ApiResult<QcQualityStandardDetailVO>): 返回标准信息 + indicators 数组

---

## GET /indicators — 查询指标项目列表

**Query**: `?category=PERFORMANCE&keyword=抗拉`

**Response** (ApiResult<List<QcIndicatorItemVO>>)
