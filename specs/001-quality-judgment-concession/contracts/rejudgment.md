# Contract: 改判管理接口

**Base Path**: `/api/v1/rejudgments`

---

## POST / — 发起改判申请

**权限**: 有"发起改判"权限的角色（`QUALITY_SUPERVISOR` 及以上）

**Request** (QcRejudgmentRequestAddCmd):
```json
{
  "originalJudgmentId": "...",
  "targetJudgmentType": "UNQUALIFIED",
  "rejudgmentReason": "后续冷轧工序发现皮下气泡，原合格判定需更新",
  "affectScope": "同炉号 H20250514001 所有卷",
  "newEvidenceSource": "后续工序缺陷",
  "evidenceAttachmentUrl": "/files/evidence/abc123.jpg"
}
```

**Response**:
```json
{
  "code": 2000,
  "message": "改判申请已提交",
  "data": {
    "requestId": "...",
    "isReverse": true,
    "approvalLevel": "ENHANCED",
    "approver": "质量部长（系统自动指派）"
  }
}
```

**业务规则**:
- 系统自动判断 `isReverse`：若 `originalJudgmentType` 为 `QUALIFIED` 或 `CAN_CONCESSION` 且
  `targetJudgmentType` 为更差结论，则 `isReverse = true`
- `isReverse = true` 时，`newEvidenceSource` 和 `evidenceAttachmentUrl` 为必填（`ServiceAssert` 校验）
- `approvalLevel` 自动设置：`isReverse = ENHANCED`，否则 `NORMAL`

**Errors**:
- 4000: 逆向改判必须提供新证据来源
- 4000: 逆向改判必须上传证据附件

---

## PUT /{id}/approve — 审批改判申请

**权限**:
- 常规改判：`QUALITY_SUPERVISOR`
- 逆向改判：`QUALITY_MANAGER` 或 `ADMIN`

**Request**:
```json
{
  "action": "APPROVED",
  "comment": "已确认后续工序缺陷证据，同意改判"
}
```

**Response**: `ApiResult.success("审批成功")`

**Side effect**（审批通过时）:
1. 原 `JudgmentResult.isFinal` 置为 0
2. 新建 `JudgmentResult`（`judgmentType = targetJudgmentType`，`isFinal = 1`）
3. `QcRejudgmentRequest.approvalStatus` 置为 `APPROVED`
4. AOP 写入审计日志（带"逆向改判"标记）
5. 失效 Dashboard Redis 缓存

---

## POST /page — 分页查询改判申请列表

**Request** (RejudgmentPageQuery):
```json
{
  "approvalStatus": "PENDING",
  "isReverse": null,
  "approvalLevel": "ENHANCED",
  "pageNum": 1,
  "pageSize": 20
}
```

**Response** (ApiResult<Page<QcRejudgmentRequestVO>>): 含 `isReverseLabel`（"逆向改判"红色标记）

---

## GET /{id} — 查询改判申请详情（含审批历史）

**Response** (ApiResult<QcRejudgmentRequestDetailVO>): 含 `approvalRecords` 列表
