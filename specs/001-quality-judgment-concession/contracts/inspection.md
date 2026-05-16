# Contract: 检验录入接口

**Base Path**: `/api/v1/inspections`

---

## POST / — 新增检验记录（单条）

**Request** (QcInspectionRecordAddCmd):
```json
{
  "heatNo": "H20250514001",
  "coilNo": "Z123456",
  "batchNo": "B20250514001",
  "sampleType": "HEAD",
  "testTime": "2025-05-14 10:30:00",
  "testerNo": "021835",
  "customerId": "CUST-001",
  "productVariety": "冷轧板",
  "productGrade": "Q235B",
  "productSpec": "厚度1.5mm，宽度1000mm",
  "values": [
    { "indicatorId": "IND-001", "testValue": 420.5 },
    { "indicatorId": "IND-002", "testValue": 28.3 }
  ]
}
```

**Response** (ApiResult<InspectionResultVO>):
```json
{
  "code": 2000,
  "message": "检验录入成功，判定已触发",
  "data": {
    "recordId": "...",
    "judgmentId": "...",
    "judgmentType": "QUALIFIED",
    "judgmentTime": "2025-05-14 10:30:05"
  }
}
```

**Side effect**: 录入成功后自动触发判定引擎，返回判定结论概要

---

## POST /batch — 批量录入检验记录

**Request**: `{ "records": [ /* 同单条 AddCmd 数组 */ ] }`

**Response**: `ApiResult.success("批量录入成功", { "successCount": 5, "failList": [] })`

---

## PUT /{id}/void — 作废检验记录

**权限**: `QUALITY_SUPERVISOR` 或原录入人

**Request**:
```json
{ "voidReason": "抗拉强度录入笔误，正确值应为 450 MPa" }
```

**Response**: `ApiResult.success("作废成功")`

**Side effect**:
1. `status` 置为 `VOID`，记录 `void_by`/`void_time`
2. 关联的 `is_final=1` 的 `JudgmentResult` 标记为失效（`is_final=0`）
3. AOP 写入审计日志

**Errors**:
- 4003: 无权限作废他人记录（非主管身份）
- 4000: 记录已处于作废状态

---

## POST /page — 分页查询检验记录

**Request** (QcInspectionRecordPageQuery):
```json
{
  "coilNo": "Z123456",
  "heatNo": "",
  "status": "NORMAL",
  "testTimeStart": "2025-05-01",
  "testTimeEnd": "2025-05-31",
  "pageNum": 1,
  "pageSize": 20
}
```

**Response** (ApiResult<Page<QcInspectionRecordVO>>)

---

## GET /{id} — 查询检验记录详情（含检验值列表）

**Response** (ApiResult<QcInspectionRecordDetailVO>)
