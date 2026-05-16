# Contract: 复检管理接口

**Base Path**: `/api/v1/reinspections`

---

## POST / — 发起复检

**权限**: `QUALITY_SUPERVISOR` 及以上

**Request** (QcReinspectionAddCmd):
```json
{
  "originalJudgmentId": "...",
  "reinspectionReason": "首次检验取样位置偏差，建议取头尾各一样复检",
  "responsibleNo": "021835"
}
```

**Response**: `ApiResult.success("复检任务已创建", { "reinspectionId": "..." })`

---

## PUT /{id}/complete — 完成复检（关联新检验记录）

复检完成后，需先通过 `POST /api/v1/inspections` 录入复检检验值，
再调用本接口将复检记录与新检验记录关联，系统自动重新触发判定。

**Request**:
```json
{ "newRecordId": "..." }
```

**Response** (ApiResult<ReinspectionCompleteVO>):
```json
{
  "code": 2000,
  "message": "复检完成，已重新触发判定",
  "data": {
    "newJudgmentId": "...",
    "newJudgmentType": "QUALIFIED"
  }
}
```

---

## POST /page — 分页查询复检任务列表

**Request** (ReinspectionPageQuery):
```json
{
  "status": "PENDING",
  "responsibleNo": "",
  "pageNum": 1,
  "pageSize": 20
}
```

**Response** (ApiResult<Page<QcReinspectionVO>>)
