# Contract: 质保书数据 & 质量统计接口

**Base Path**:
- 质保书数据: `/api/v1/cert-data`
- 质量统计: `/api/v1/statistics`

---

## POST /cert-data/generate — 生成质保书数据快照

**权限**: `QUALITY_MANAGER` 及以上

**Request**:
```json
{
  "queryType": "COIL",
  "coilNo": "Z123456",
  "batchNo": null
}
```

**Response** (ApiResult<QcQualityCertDataVO>):
```json
{
  "code": 2000,
  "data": {
    "id": "...",
    "coilNo": "Z123456",
    "generateTime": "2025-05-14 11:00:00",
    "finalJudgmentType": "QUALIFIED",
    "indicators": [
      {
        "indicatorName": "抗拉强度", "unit": "MPa",
        "testValue": 425.0, "upperLimit": 510.0, "lowerLimit": 370.0, "isPassed": true
      }
    ]
  }
}
```

---

## POST /cert-data/page — 分页查询质保书数据列表

**Request**: `{ "coilNo": "", "batchNo": "", "pageNum": 1, "pageSize": 20 }`

**Response** (ApiResult<Page<QcQualityCertDataVO>>)

---

## GET /statistics/overview — 质量统计总览

**Query**: `?timeStart=2025-01-01&timeEnd=2025-05-31`

**Response** (ApiResult<StatisticsOverviewVO>):
```json
{
  "code": 2000,
  "data": {
    "totalInspection": 1200,
    "qualifiedCount": 1100,
    "unqualifiedCount": 60,
    "needReinspectionCount": 25,
    "canConcessionCount": 15,
    "unqualifiedRate": "5.0%",
    "reinspectionRate": "2.08%",
    "concessionRate": "1.25%"
  }
}
```

---

## GET /statistics/indicator-distribution — 指标异常分布

**Query**: `?timeStart=2025-01-01&timeEnd=2025-05-31&judgmentType=UNQUALIFIED`

**Response** (ApiResult<List<IndicatorDistributionVO>>):
```json
{
  "code": 2000,
  "data": [
    { "indicatorName": "抗拉强度", "failCount": 32, "failRate": "53.3%" },
    { "indicatorName": "延伸率", "failCount": 18, "failRate": "30.0%" }
  ]
}
```
