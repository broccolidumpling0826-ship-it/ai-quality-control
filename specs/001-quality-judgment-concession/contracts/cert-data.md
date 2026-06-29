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

**业务门禁**（与正式 PDF 导出一致）:
- 最终判定 MUST 为可放行状态（`QUALIFIED`，或已完成审批的 `CAN_CONCESSION`）
- `STANDARD_CONFLICT`、`UNQUALIFIED`、`NEED_REINSPECTION`、未完成让步审批 MUST 拒绝
- 仅汇总 `status = NORMAL` 的检验记录

**Response** (ApiResult<QcQualityCertDataVO>):
```json
{
  "code": 2000,
  "data": {
    "id": "...",
    "coilNo": "Z123456",
    "batchNo": "HT-MAN-QUAL-001",
    "heatNo": "HT-MAN-QUAL-001",
    "productVariety": "热轧板",
    "productGrade": "Q235B",
    "finalJudgmentType": "QUALIFIED",
    "status": "SUCCESS",
    "generateTime": "2026-06-29T14:19:34",
    "generatedBy": "admin",
    "indicators": [
      {
        "indicatorName": "抗拉强度",
        "indicatorCode": "Rm",
        "unit": "MPa",
        "testValue": 430.0,
        "upperLimit": 505.0,
        "lowerLimit": 375.0,
        "isPassed": 1,
        "indicatorResult": "PASS"
      }
    ]
  }
}
```

---

## GET /cert-data/{id} — 查询质保书数据详情

**权限**: 已登录用户

**Response** (ApiResult<QcQualityCertDataVO>): 字段同生成接口；`indicators` 为快照指标列表。

---

## GET /cert-data/{id}/pdf — 导出正式质保书 PDF

**权限**: 已登录用户（建议 `QUALITY_MANAGER` 及以上）

**Response**: `application/pdf` 二进制流

**Content-Disposition**: `attachment; filename="quality-cert-{id}.pdf"`（前端覆盖为 `质保书-{卷号}.pdf`）

**PDF 内容规范**（FR-010a）:

| 区块 | 内容 |
|------|------|
| 标题 | 质量证明书（居中） |
| 基本信息表 | 证书编号、卷号、批次号、炉号、品种、牌号、最终结论、客户编号、生成时间、生成人 |
| 检测指标表 | 序号、指标名称、指标代码、实测值、单位、标准下限、标准上限、结论 |
| 页脚 | 系统自动生成说明；右下角重复最终结论 |

**中文展示要求**:
- 最终结论：`QUALIFIED`→合格，`UNQUALIFIED`→不合格，`NEED_REINSPECTION`→需复检，`CAN_CONCESSION`→可让步，`STANDARD_CONFLICT`→标准冲突
- 指标结论：`PASS`→合格，`FAIL`→不合格，`CONCESSION`→让步，`WARNING`→缺失
- 生成时间格式：`yyyy年MM月dd日 HH:mm`

**导出门禁**:
- 快照 `status` MUST 为 `SUCCESS` 且 `indicators` 非空
- 最终判定 MUST 可放行
- 指标结论 MUST NOT 含 `FAIL` 或 `WARNING`

**审计**: 记录 `EXPORT_CERT_PDF`（操作人、目标实体 `QcQualityCertData`、目标 ID、时间戳）

**错误示例**:
```json
{
  "code": 5000,
  "message": "存在未解决标准冲突，禁止生成正式质保书"
}
```

---

## POST /cert-data/page — 分页查询质保书数据列表

**Request**: Query Params — `pageNum`, `pageSize`, `coilNo`, `batchNo`, `startTime`, `endTime`

**Response** (ApiResult<Page<QcQualityCertDataVO>>): 列表项含 `id`、`coilNo`、`batchNo`、`heatNo`、`generateTime`、`generatedBy`、`status`

---

## POST /cert-data/qa — 质保书问答

见 OpenSpec `quality-cert-qa`；基于快照与判定依据回答出证相关问题。

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
