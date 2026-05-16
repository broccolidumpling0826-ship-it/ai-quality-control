# Contract: 判定结论与判定解释接口

**Base Path**: `/api/v1/judgments`

---

## GET /record/{recordId} — 查询检验记录的当前判定结论

**Response** (ApiResult<QcJudgmentResultVO>):
```json
{
  "code": 2000,
  "data": {
    "judgmentId": "...",
    "recordId": "...",
    "coilNo": "Z123456",
    "judgmentType": "UNQUALIFIED",
    "judgmentTime": "2025-05-14 10:30:05",
    "isFinal": true,
    "matchedStandards": [
      { "standardId": "...", "standardType": "CUSTOMER", "versionNo": "协议C2025-088-v1" }
    ],
    "evidences": [
      {
        "indicatorName": "抗拉强度",
        "indicatorCode": "Rm",
        "unit": "MPa",
        "testValue": 360.0,
        "upperLimit": 510.0,
        "lowerLimit": 370.0,
        "deviation": -10.0,
        "triggerRule": "实测值 360.0 MPa 低于客户协议标准下限 370.0 MPa（客户特殊要求：协议C2025-088-v1）",
        "isPassed": false
      }
    ]
  }
}
```

---

## GET /{id}/explanation — 获取判定解释详情

返回完整的"为什么是这个结论"解释报告，包含：
- 匹配标准的选取过程（三级优先级命中说明）
- 每个指标的通过/不通过详情
- 触发让步/需复检规则说明

**Response** (ApiResult<JudgmentExplanationVO>): 同上，额外包含 `priorityMatchProcess` 字段说明选标逻辑

---

## POST /page — 分页查询判定结论列表（质量工作台用）

**Request** (JudgmentPageQuery):
```json
{
  "judgmentType": "UNQUALIFIED",
  "isFinal": true,
  "timeStart": "2025-05-01",
  "timeEnd": "2025-05-31",
  "pageNum": 1,
  "pageSize": 20
}
```

**Response** (ApiResult<Page<QcJudgmentResultVO>>)

---

## GET /dashboard/summary — 质量工作台统计汇总

**Response** (ApiResult<DashboardSummaryVO>):
```json
{
  "code": 2000,
  "data": {
    "pendingJudgmentCount": 3,
    "unqualifiedCount": 12,
    "pendingReinspectionCount": 2,
    "pendingConcessionApprovalCount": 1,
    "cacheUpdatedAt": "2025-05-14 10:29:00"
  }
}
```

**Cache**: Redis 缓存，TTL 60 秒，相关业务操作发生时主动失效
