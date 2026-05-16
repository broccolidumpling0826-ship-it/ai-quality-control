# Contract: 让步接收接口

**Base Path**: `/api/v1/concessions`

---

## POST / — 发起让步接收申请

**权限**: `SALES_MANAGER` 或 `QUALITY_SUPERVISOR`

**Request** (QcConcessionAddCmd):
```json
{
  "judgmentId": "...",
  "concessionScope": "抗拉强度允许 360–370 MPa（低于标准下限10 MPa以内）",
  "riskDescription": "该批次仅用于非结构件，承重要求低，经评估影响可控",
  "effectiveDate": "2025-05-15",
  "expiryDate": "2025-06-15"
}
```

**Response**: `ApiResult.success("让步申请已提交，等待内部审批", { "concessionId": "..." })`

**Errors**:
- 4000: 该批次判定结论非"可让步"，无法发起让步接收

---

## PUT /{id}/confirm — 上传客户确认附件

**权限**: `SALES_MANAGER`

**Request** (multipart/form-data):
- `attachmentFile`: 附件文件（必填）
- `confirmNote`: 确认摘要文字（可选，如"客户采购经理张三通过邮件确认"）

**Response**: `ApiResult.success("客户确认附件上传成功")`

**业务规则**:
- `confirm_attachment_url` 写入后不可替换（`ServiceAssert.isTrue(!hasExistingAttachment, "...")`）
- 系统自动记录上传人工号和上传时间
- 上传成功后 `confirm_status` → `CONFIRMED`

**Errors**:
- 4000: 客户确认附件已上传，如需更新请作废本让步申请并重新发起
- 4003: 无权限上传客户确认附件

---

## PUT /{id}/reject — 记录客户拒绝

**权限**: `SALES_MANAGER`

**Request**:
```json
{
  "rejectNote": "客户拒绝让步，要求按原标准重新生产",
  "rejectAttachmentUrl": "/files/reject/xyz789.jpg"
}
```

**Response**: `ApiResult.success("已记录客户拒绝，让步流程终止")`

**Side effect**: `confirm_status → REJECTED`, `approval_status → REJECTED`，通知质量部门

---

## PUT /{id}/approve — 内部审批让步申请

**权限**: `QUALITY_MANAGER` + `SALES_MANAGER`（双签）

**Request**: `{ "action": "APPROVED", "comment": "..." }`

**业务规则**: `confirm_status = CONFIRMED` 才允许审批通过（客户确认是前置条件）

**Response**: `ApiResult.success("让步接收审批成功")`

---

## POST /page — 分页查询让步接收列表

**Request** (ConcessionPageQuery):
```json
{
  "approvalStatus": "PENDING_APPROVAL",
  "confirmStatus": null,
  "expiryDateBefore": "2025-06-30",
  "pageNum": 1,
  "pageSize": 20
}
```

**Response** (ApiResult<Page<QcConcessionVO>>): 含到期剩余天数、超时未确认标记

---

## GET /{id} — 查询让步接收详情

**Response** (ApiResult<QcConcessionDetailVO>): 含附件下载URL、状态变更历史
