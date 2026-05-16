# Contract: 站内消息接口（FR-016）

**Base Path**: `/api/v1/messages`
**权限要求**: 所有已登录用户；每个用户只能读取/操作自己的消息

---

## GET /unread-count — 查询未读消息数（工作台角标）

**Response** (`ApiResult<Integer>`):
```json
{ "code": 2000, "data": 3 }
```

**说明**: 前端工作台 `main-layout.vue` 在挂载时调用一次，并每 60 秒轮询一次（与 Dashboard 缓存 TTL 对齐）。

---

## POST /page — 分页查询消息列表

**Request** (`SysMessagePageQuery`):
```json
{
  "isRead": null,
  "messageType": null,
  "pageNum": 1,
  "pageSize": 20
}
```

**Response** (`ApiResult<Page<SysMessageVO>>`):
```json
{
  "code": 2000,
  "data": {
    "total": 5,
    "records": [
      {
        "id": "MSG-001",
        "messageType": "CONCESSION_OVERDUE",
        "title": "让步接收超时提醒",
        "content": "批次 B2025-001 的让步申请已超过 7 天未获客户确认，请及时跟进。",
        "relatedEntityType": "ConcessionAcceptance",
        "relatedEntityId": "CA-001",
        "isRead": false,
        "createDateTime": "2026-05-16 09:00:00"
      }
    ]
  }
}
```

**messageType 枚举值**（对应 FR-016 四种通知场景）：

| messageType | 触发场景 | 接收人 |
|-------------|---------|--------|
| `CONCESSION_REMIND` | 让步超时未确认（超阈值天数） | 销售人员 |
| `CONCESSION_INVALIDATED` | 让步因改判审批通过自动失效 | 销售人员 |
| `STANDARD_GAP` | StandardGap 覆盖缺口发现 | 质量工程师 |
| `CONCESSION_EXPIRED` | 让步有效期到期自动失效 | 让步申请责任人 |

---

## PUT /{id}/read — 标记单条消息已读

**Response**: `ApiResult.success("标记已读成功")`

**Errors**:
- 4003: 无权限（只能操作自己的消息）
- 4000: 消息不存在

---

## PUT /read-all — 标记当前用户全部消息已读

**Response**: `ApiResult.success("全部已读")`

---

## 数据模型（SysMessage）

**表名**: `sys_message`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| receiver_no | VARCHAR(64) | NOT NULL | 接收人工号 |
| message_type | VARCHAR(50) | NOT NULL | 消息类型（枚举见上表） |
| title | VARCHAR(200) | NOT NULL | 消息标题 |
| content | VARCHAR(1000) | NOT NULL | 消息内容 |
| related_entity_type | VARCHAR(50) | | 关联实体类型（供前端跳转） |
| related_entity_id | VARCHAR(64) | | 关联实体 ID |
| is_read | TINYINT(1) | DEFAULT 0 | 是否已读 |

**索引**: `idx_receiver_read(receiver_no, is_read)`, `idx_receiver_type(receiver_no, message_type)`

**写入来源**:
- `StandardGapService`（FR-015）：写入 STANDARD_GAP 类型消息
- `ConcessionScheduler`（定时任务）：写入 CONCESSION_REMIND / CONCESSION_EXPIRED 类型消息
- `RejudgmentServiceImpl.notifySalesIfConcessionConfirmed()`：写入 CONCESSION_INVALIDATED 类型消息
