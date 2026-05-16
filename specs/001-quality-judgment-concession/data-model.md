# Data Model: 质量判定解释与让步管理系统

**Feature**: 001-quality-judgment-concession
**Date**: 2026-05-15

> 所有实体均继承 `CoreEntity`，表结构包含以下公共字段（不在各表重复列出）：
> `id VARCHAR(64) PK`, `company_id VARCHAR(64)`,
> `create_user_no VARCHAR(64)`, `update_user_no VARCHAR(64)`,
> `create_date_time VARCHAR(32)`, `update_date_time VARCHAR(32)`.

---

## 1. CoreEntity 基础实体（已由用户提供）

```java
@Setter @Getter
public abstract class CoreEntity implements IEntity {
    @TableId(type = IdType.ASSIGN_ID) @TableField(fill = FieldFill.INSERT)
    protected String id;                  // 雪花ID
    @TableField(fill = FieldFill.INSERT)
    protected String companyId;           // 公司ID
    @TableField(fill = FieldFill.INSERT)
    protected String createUserNo;        // 创建人工号
    @TableField(fill = FieldFill.INSERT_UPDATE)
    protected String updateUserNo;        // 修改人工号
    @TableField(fill = FieldFill.INSERT)
    protected String createDateTime;      // 创建日期
    @TableField(fill = FieldFill.INSERT_UPDATE)
    protected String updateDateTime;      // 修改日期
}
```

---

## 2. 全局枚举

```java
// 标准类型
public enum StandardType { NATIONAL, ENTERPRISE, CUSTOMER }

// 标准状态（仅用于管理，不参与判定逻辑）
public enum StandardStatus { DRAFT, PUBLISHED, DEPRECATED }

// 指标类别
public enum IndicatorCategory { COMPOSITION, PERFORMANCE, DIMENSION, SURFACE, SHAPE }

// 检验记录状态
public enum InspectionStatus { NORMAL, VOID }

// 判定结论类型
public enum JudgmentType { QUALIFIED, UNQUALIFIED, NEED_REINSPECTION, CAN_CONCESSION }

// 复检记录状态
public enum ReinspectionStatus { PENDING, COMPLETED }

// 改判申请审批级别
public enum ApprovalLevel { NORMAL, ENHANCED }

// 改判申请审批状态
public enum ApprovalStatus { PENDING, APPROVED, REJECTED }

// 让步确认状态
public enum ConfirmStatus { PENDING, CONFIRMED, REJECTED }

// 让步总状态（双签审批中间态）
// PENDING_APPROVAL → 发起后等待销售经理第一签
// SALES_APPROVED   → 销售经理已审批，等待质量经理第二签
// APPROVED         → 双签完成，让步正式生效
// INVALID          → 因改判通过或有效期到期自动失效
// REJECTED         → 任一审批人驳回
public enum ConcessionStatus { PENDING_APPROVAL, SALES_APPROVED, APPROVED, INVALID, REJECTED }
```

> **注意（FR-017）**：以上枚举类保留用于后端 Java 类型安全，但其对应的显示值、顺序、
> 可用范围由数据字典（SysDict / SysDictItem）管理，前端下拉框统一从字典接口读取，
> 禁止在前端代码中硬编码枚举显示文本。

---

## 3. 实体详情

### 3.1 QcIndicatorItem — 指标项目

**表名**: `qc_indicator_item`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| indicator_name | VARCHAR(100) | NOT NULL | 指标名称（如"抗拉强度"） |
| indicator_code | VARCHAR(50) | NOT NULL UNIQUE | 指标代码（如"Rm"） |
| indicator_category | VARCHAR(20) | NOT NULL | 指标类别（IndicatorCategory 枚举） |
| unit | VARCHAR(20) | | 单位（如"MPa"、"%"） |
| description | VARCHAR(500) | | 描述 |
| is_deleted | TINYINT(1) | DEFAULT 0 | 软删除标志 |

**索引**: `idx_category(indicator_category)`

---

### 3.2 QcQualityStandard — 质量标准

**表名**: `qc_quality_standard`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| standard_type | VARCHAR(20) | NOT NULL | 标准类型（StandardType 枚举） |
| variety | VARCHAR(100) | NOT NULL | 品种（如"冷轧板"） |
| grade | VARCHAR(100) | NOT NULL | 牌号（如"Q235B"） |
| spec_range | VARCHAR(200) | NOT NULL | 规格范围（如"厚度 0.5-3.0mm"） |
| version_no | VARCHAR(50) | NOT NULL | 版本号（如"GB/T 700-2006"） |
| effective_date | DATE | NOT NULL | 生效日期 |
| expiry_date | DATE | NOT NULL | 失效日期（未失效填 9999-12-31） |
| status | VARCHAR(20) | NOT NULL DEFAULT 'DRAFT' | 状态（StandardStatus 枚举，不参与判定） |
| customer_id | VARCHAR(64) | | 客户ID（仅客户协议标准有值） |
| remark | VARCHAR(500) | | 备注 |

**索引**:
- `uk_standard_window(standard_type, variety, grade, customer_id, effective_date)` — 防版本时间窗口重叠（唯一索引）
- `idx_lookup(standard_type, variety, grade, effective_date, expiry_date)` — 判定引擎查询优化

**业务约束**:
- `expiry_date > effective_date`（应用层校验）
- 同 `(standard_type, variety, grade, customer_id)` 已发布版本的有效时间窗口不可重叠

---

### 3.3 QcStandardIndicator — 标准指标

**表名**: `qc_standard_indicator`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| standard_id | VARCHAR(64) | NOT NULL | 关联 qc_quality_standard.id |
| indicator_id | VARCHAR(64) | NOT NULL | 关联 qc_indicator_item.id |
| upper_limit | DECIMAL(20,6) | | 上限（NULL 表示无上限） |
| lower_limit | DECIMAL(20,6) | | 下限（NULL 表示无下限） |
| is_required | TINYINT(1) | DEFAULT 1 | 是否必检 |
| concession_upper | DECIMAL(20,6) | | 让步上限（NULL 表示不可让步） |
| concession_lower | DECIMAL(20,6) | | 让步下限 |

**索引**: `uk_standard_indicator(standard_id, indicator_id)`，`idx_standard_id(standard_id)`

---

### 3.4 QcInspectionRecord — 检验记录

**表名**: `qc_inspection_record`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| heat_no | VARCHAR(64) | NOT NULL | 炉号 |
| coil_no | VARCHAR(64) | NOT NULL | 卷号 |
| batch_no | VARCHAR(64) | NOT NULL | 批次号（业务聚合键） |
| sample_type | VARCHAR(20) | NOT NULL | 样品类型（如 HEAD/TAIL/MIDDLE） |
| test_time | DATETIME | NOT NULL | 检验时间 |
| tester_no | VARCHAR(64) | NOT NULL | 检验人工号 |
| customer_id | VARCHAR(64) | | 客户ID（用于匹配客户协议标准） |
| product_variety | VARCHAR(100) | NOT NULL | 品种 |
| product_grade | VARCHAR(100) | NOT NULL | 牌号 |
| product_spec | VARCHAR(200) | NOT NULL | 规格 |
| status | VARCHAR(20) | NOT NULL DEFAULT 'NORMAL' | 记录状态（InspectionStatus 枚举） |
| void_reason | VARCHAR(500) | | 作废原因（作废时必填） |
| void_by | VARCHAR(64) | | 作废操作人工号 |
| void_time | DATETIME | | 作废时间 |

**业务约束**:
- 提交后除 `status/void_reason/void_by/void_time` 外，其他字段永不修改（应用层拦截）
- 判定引擎仅取 `status = 'NORMAL'` 的记录

**索引**: `idx_coil_status(coil_no, status)`, `idx_batch(batch_no)`, `idx_heat(heat_no)`

---

### 3.5 QcInspectionValue — 检验值

**表名**: `qc_inspection_value`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| record_id | VARCHAR(64) | NOT NULL | 关联 qc_inspection_record.id |
| indicator_id | VARCHAR(64) | NOT NULL | 关联 qc_indicator_item.id |
| test_value | DECIMAL(20,6) | | 实测值（数值型指标） |
| value_text | VARCHAR(500) | | 文本值（文本型指标） |

**索引**: `idx_record_id(record_id)`, `uk_record_indicator(record_id, indicator_id)`

---

### 3.6 QcJudgmentResult — 判定结论

**表名**: `qc_judgment_result`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| record_id | VARCHAR(64) | NOT NULL | 关联 qc_inspection_record.id |
| judgment_type | VARCHAR(30) | NOT NULL | 判定结论（JudgmentType 枚举） |
| judgment_time | DATETIME | NOT NULL | 判定时间 |
| is_final | TINYINT(1) | DEFAULT 1 | 是否当前最终结论（改判后旧结论置0） |
| matched_standard_ids | TEXT | | 命中标准 ID 列表（JSON 数组） |
| remark | VARCHAR(500) | | 备注 |

**索引**: `idx_record_final(record_id, is_final)`, `idx_judgment_type(judgment_type)`

---

### 3.7 QcJudgmentEvidence — 判定依据

**表名**: `qc_judgment_evidence`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| judgment_id | VARCHAR(64) | NOT NULL | 关联 qc_judgment_result.id |
| standard_id | VARCHAR(64) | NOT NULL | 关联 qc_quality_standard.id |
| indicator_id | VARCHAR(64) | NOT NULL | 关联 qc_indicator_item.id |
| test_value | DECIMAL(20,6) | | 实测值 |
| upper_limit | DECIMAL(20,6) | | 标准上限 |
| lower_limit | DECIMAL(20,6) | | 标准下限 |
| deviation | DECIMAL(20,6) | | 偏差值（实测值 - 超出的限值） |
| trigger_rule | VARCHAR(500) | NOT NULL | 触发规则描述 |
| is_passed | TINYINT(1) | NOT NULL | 该指标是否通过 |

**索引**: `idx_judgment_id(judgment_id)`

---

### 3.8 QcReinspectionRecord — 复检记录

**表名**: `qc_reinspection_record`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| original_judgment_id | VARCHAR(64) | NOT NULL | 原判定结论 ID |
| reinspection_reason | VARCHAR(500) | NOT NULL | 复检原因 |
| new_record_id | VARCHAR(64) | | 复检新检验记录 ID（完成后填写） |
| responsible_no | VARCHAR(64) | NOT NULL | 责任人工号 |
| status | VARCHAR(20) | NOT NULL DEFAULT 'PENDING' | 复检状态（ReinspectionStatus 枚举） |

**索引**: `idx_judgment_id(original_judgment_id)`

---

### 3.9 QcRejudgmentRequest — 改判申请

**表名**: `qc_rejudgment_request`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| original_judgment_id | VARCHAR(64) | NOT NULL | 原判定结论 ID |
| original_judgment_type | VARCHAR(30) | NOT NULL | 改判前结论（JudgmentType 枚举） |
| target_judgment_type | VARCHAR(30) | NOT NULL | 改判后目标结论（JudgmentType 枚举） |
| rejudgment_reason | VARCHAR(1000) | NOT NULL | 改判原因 |
| affect_scope | VARCHAR(1000) | NOT NULL | 影响范围 |
| is_reverse | TINYINT(1) | NOT NULL DEFAULT 0 | 是否逆向改判 |
| new_evidence_source | VARCHAR(500) | | 新证据来源（逆向改判必填） |
| evidence_attachment_url | VARCHAR(500) | | 证据附件URL（逆向改判必填，写入后不可更新） |
| approval_level | VARCHAR(20) | NOT NULL | 审批级别（ApprovalLevel 枚举） |
| approval_status | VARCHAR(20) | NOT NULL DEFAULT 'PENDING' | 审批状态（ApprovalStatus 枚举） |

**业务约束**:
- `is_reverse = 1` 时，`new_evidence_source` 和 `evidence_attachment_url` 均不可为空
- `evidence_attachment_url` 一旦写入不可通过接口替换（应用层 `ServiceAssert` 拦截）
- `approval_level`：`is_reverse = 1` 时自动设为 `ENHANCED`，否则 `NORMAL`

**索引**: `idx_judgment_id(original_judgment_id)`, `idx_approval_status(approval_status)`

---

### 3.10 QcRejudgmentApproval — 改判审批记录

**表名**: `qc_rejudgment_approval`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| request_id | VARCHAR(64) | NOT NULL | 关联 qc_rejudgment_request.id |
| approver_no | VARCHAR(64) | NOT NULL | 审批人工号 |
| approval_action | VARCHAR(20) | NOT NULL | 审批动作（APPROVED/REJECTED） |
| approval_comment | VARCHAR(1000) | | 审批意见 |
| approval_time | DATETIME | NOT NULL | 审批时间 |

**索引**: `idx_request_id(request_id)`

---

### 3.11 QcConcessionAcceptance — 让步接收

**表名**: `qc_concession_acceptance`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| judgment_id | VARCHAR(64) | NOT NULL | 关联 qc_judgment_result.id |
| concession_scope | VARCHAR(1000) | NOT NULL | 让步范围 |
| risk_description | VARCHAR(1000) | NOT NULL | 风险说明 |
| effective_date | DATE | NOT NULL | 让步有效期开始 |
| expiry_date | DATE | NOT NULL | 让步有效期结束 |
| confirm_status | VARCHAR(20) | NOT NULL DEFAULT 'PENDING' | 客户确认状态（ConfirmStatus 枚举） |
| confirm_attachment_url | VARCHAR(500) | | 确认文件附件URL（强制，写入后不可更新） |
| confirm_note | VARCHAR(1000) | | 确认说明文字（可选） |
| confirm_uploader_no | VARCHAR(64) | | 附件上传人工号 |
| confirm_upload_time | DATETIME | | 附件上传时间 |
| approval_status | VARCHAR(20) | NOT NULL DEFAULT 'PENDING_APPROVAL' | 让步总状态（ConcessionStatus 枚举：PENDING_APPROVAL→SALES_APPROVED→APPROVED） |
| last_reminder_time | DATETIME | | 最后一次催确认时间（定时任务维护） |

**业务约束**:
- `confirm_attachment_url` 写入后不可通过接口替换
- 让步最终放行（`approval_status = APPROVED`）MUST 在 `confirm_status = CONFIRMED` 之后
- `expiry_date` 到期后定时任务将 `approval_status` 置为 `INVALID`

**索引**: `idx_judgment_id(judgment_id)`, `idx_expiry(expiry_date, approval_status)`

---

### 3.12 QcQualityCertData — 质保书数据

**表名**: `qc_quality_cert_data`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| coil_no | VARCHAR(64) | | 卷号（与 batch_no 二选一） |
| batch_no | VARCHAR(64) | | 批次号 |
| snapshot_data | LONGTEXT | NOT NULL | 汇总检验指标快照（JSON） |
| generate_time | DATETIME | NOT NULL | 生成时间 |
| generated_by | VARCHAR(64) | NOT NULL | 生成操作人工号 |

**索引**: `idx_coil(coil_no)`, `idx_batch(batch_no)`

---

### 3.13 QcAuditLog — 审计日志

**表名**: `qc_audit_log`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| operation_type | VARCHAR(50) | NOT NULL | 操作类型（如 VOID_INSPECTION、APPROVE_REJUDGMENT） |
| target_entity | VARCHAR(50) | NOT NULL | 操作对象实体名 |
| target_id | VARCHAR(64) | NOT NULL | 操作对象ID |
| before_value | TEXT | | 操作前值（JSON，可为空） |
| after_value | TEXT | | 操作后值（JSON） |
| operator_no | VARCHAR(64) | NOT NULL | 操作人工号 |
| operate_time | DATETIME | NOT NULL | 操作时间 |
| ip_address | VARCHAR(50) | | 操作IP |
| remark | VARCHAR(500) | | 备注 |

**约束**: 该表不提供 UPDATE/DELETE 接口，只有 INSERT（AOP 异步写入）

**索引**: `idx_target(target_entity, target_id)`, `idx_operator(operator_no)`, `idx_operate_time(operate_time)`

---

### 3.14 SysUser — 系统用户

**表名**: `sys_user`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| user_no | VARCHAR(64) | NOT NULL UNIQUE | 工号（登录账号） |
| username | VARCHAR(100) | NOT NULL | 姓名 |
| password | VARCHAR(255) | NOT NULL | BCrypt 加密密码 |
| role | VARCHAR(50) | NOT NULL | 角色（字典 USER_ROLE，ADMIN 可维护） |
| department | VARCHAR(100) | | 部门 |
| status | TINYINT(1) | DEFAULT 1 | 账号状态（1启用/0禁用） |
| last_login_time | DATETIME | | 最后登录时间 |

**索引**: `uk_user_no(user_no)`

---

### 3.15 SysDict — 数据字典分类（FR-017）

**表名**: `sys_dict`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| dict_code | VARCHAR(50) | NOT NULL UNIQUE | 字典分类编码（如 `STANDARD_TYPE`） |
| dict_name | VARCHAR(100) | NOT NULL | 字典分类名称（如"标准类型"） |
| description | VARCHAR(500) | | 描述 |
| is_system | TINYINT(1) | DEFAULT 0 | 是否系统内置（内置字典不可删除分类） |
| sort_no | INT | DEFAULT 0 | 排序号 |
| status | TINYINT(1) | DEFAULT 1 | 启用状态 |

**索引**: `uk_dict_code(dict_code)`

**预置字典分类**（系统初始化时写入）：

| dict_code | dict_name |
|-----------|-----------|
| `STANDARD_TYPE` | 标准类型 |
| `INDICATOR_CATEGORY` | 指标类别 |
| `SAMPLE_TYPE` | 样品类型 |
| `JUDGMENT_TYPE` | 判定结论类型 |
| `STANDARD_STATUS` | 标准状态 |
| `INSPECTION_STATUS` | 检验记录状态 |
| `REINSPECTION_STATUS` | 复检状态 |
| `APPROVAL_LEVEL` | 改判审批级别 |
| `APPROVAL_STATUS` | 改判审批状态 |
| `CONFIRM_STATUS` | 让步客户确认状态 |
| `CONCESSION_STATUS` | 让步总状态 |
| `AUDIT_OPERATION_TYPE` | 审计操作类型 |
| `USER_ROLE` | 用户角色 |
| `NEW_EVIDENCE_SOURCE` | 新证据来源（逆向改判用） |
| `PRODUCT_VARIETY` | 品种 |
| `PRODUCT_GRADE` | 牌号 |

---

### 3.16 SysDictItem — 数据字典项（FR-017）

**表名**: `sys_dict_item`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| dict_code | VARCHAR(50) | NOT NULL | 关联 sys_dict.dict_code |
| item_value | VARCHAR(100) | NOT NULL | 字典项值（枚举值，如 `NATIONAL`） |
| item_label | VARCHAR(200) | NOT NULL | 字典项显示文本（如"国标"） |
| item_label_en | VARCHAR(200) | | 英文标签（可选） |
| color_tag | VARCHAR(20) | | 前端标签颜色（如 `success`/`danger`/`warning`/`info`） |
| sort_no | INT | DEFAULT 0 | 排序号 |
| status | TINYINT(1) | DEFAULT 1 | 启用状态（停用后不再出现在下拉框中） |
| is_system | TINYINT(1) | DEFAULT 0 | 是否系统内置（内置项不可删除） |
| remark | VARCHAR(500) | | 备注 |

**索引**: `uk_dict_item(dict_code, item_value)`, `idx_dict_code(dict_code)`

**业务约束**：
- 前端下拉框 MUST 通过 `/api/v1/dict/items/{dictCode}` 接口获取，不可硬编码。
- `color_tag` 用于 Element Plus `<el-tag type="">` 的 type 属性，统一配置状态色。
- 前端 Pinia `dictStore` 在应用启动时批量加载所有 `status=1` 的字典并缓存，
  减少运行时请求。

---

## 4. 实体关系图（文字描述）

```
SysUser (1) ──────────────── (*) QcInspectionRecord
                                        │ (1)
                                        ▼ (*)
                              QcInspectionValue ──── QcIndicatorItem (*)
                                        │
                         ┌──── (1) ────┘
                         ▼
               QcJudgmentResult (*)
                    │  (1)
                    ├──── (*) QcJudgmentEvidence ──── QcQualityStandard
                    │                                       │ (1)
                    │                                       ▼ (*)
                    │                              QcStandardIndicator ──── QcIndicatorItem
                    │
                    ├──── (*) QcReinspectionRecord
                    ├──── (*) QcRejudgmentRequest ──── (*) QcRejudgmentApproval
                    └──── (*) QcConcessionAcceptance
```

## 5. 状态机汇总

### 检验记录状态
```
NORMAL ──[作废]──▶ VOID
```

### 判定结论流转（改判触发）
```
原结论（任意） ──[改判申请审批通过]──▶ 新结论
（原 is_final = 0，新结论写入新 JudgmentResult）
```

### 改判申请状态
```
PENDING ──[审批通过]──▶ APPROVED
        ──[审批拒绝]──▶ REJECTED
```

### 让步接收状态机
```
confirm_status: PENDING ──[上传附件]──▶ CONFIRMED
                        ──[拒绝]────▶  REJECTED

approval_status: PENDING_APPROVAL ──[内部审批通过]──▶ APPROVED
                 APPROVED ──[到期]──▶ INVALID
                 APPROVED ──[拒绝]──▶ REJECTED（客户拒绝后触发）
```
