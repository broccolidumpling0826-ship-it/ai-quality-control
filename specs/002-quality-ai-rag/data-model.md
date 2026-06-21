# Data Model: 质量判定解释、让步与标准 RAG 系统（决赛 V2.0）

**Feature**: 002-quality-ai-rag  
**Date**: 2026-06-20  
**Base Tables**: 沿用 001 `data-model.md` 全部 16+ 表，本节仅定义 **002 增量表**

> 所有新实体继承 `CoreEntity`，公共字段同 001 约定。

---

## 1. 新增枚举

```java
/** AI 调用来源 */
public enum AiCallSource {
    RAG_QUERY, JUDGMENT_EXPLAIN, CONCESSION_ASSESS,
    REINSPECTION_ADVICE, REJUDGMENT_ADVICE, CERT_SUMMARY, EVALUATION
}

/** AI 调用错误类型 */
public enum AiErrorType {
    NONE, TIMEOUT, RATE_LIMIT, UNAVAILABLE, INJECTION_BLOCKED, PARSE_ERROR
}

/** 置信度等级 */
public enum ConfidenceLevel { HIGH, MEDIUM, LOW }

/** 标准冲突处理状态 */
public enum ConflictStatus { PENDING, RESOLVED_CUSTOMER, RESOLVED_ENTERPRISE, RESOLVED_MANUAL }

/** 评测样例分类 */
public enum EvaluationCategory {
    NORMAL, BOUNDARY, LOW_CONFIDENCE, INJECTION
}

/** 评测运行状态 */
public enum EvaluationRunStatus { RUNNING, COMPLETED, FAILED }
```

---

## 2. 实体详情

### 2.1 QcStandardDocument — 标准原始文档

**表名**: `qc_standard_document`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| standard_id | VARCHAR(64) | NOT NULL | 关联 qc_quality_standard.id |
| file_name | VARCHAR(255) | NOT NULL | 原始文件名 |
| file_path | VARCHAR(500) | NOT NULL | 存储路径（相对 upload base） |
| file_type | VARCHAR(20) | NOT NULL | PDF / TXT / MD |
| page_count | INT | | 页数 |
| ingest_status | VARCHAR(20) | NOT NULL | PENDING / INDEXED / FAILED |
| ingest_time | VARCHAR(32) | | 入库完成时间 |
| chunk_count | INT | DEFAULT 0 | 段落数 |

**索引**: `idx_standard_id(standard_id)`

---

### 2.2 QcStandardDocumentChunk — RAG 段落索引

**表名**: `qc_standard_document_chunk`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| document_id | VARCHAR(64) | NOT NULL | 关联 qc_standard_document.id |
| standard_id | VARCHAR(64) | NOT NULL | 冗余，便于按标准检索 |
| chunk_index | INT | NOT NULL | 段落序号 |
| section_ref | VARCHAR(100) | | 章节号（如「4.2.1」「表3」） |
| chunk_text | TEXT | NOT NULL | 段落正文 |
| keyword_tags | VARCHAR(500) | | 人工/自动关键词（逗号分隔） |

**索引**:
- `idx_document_chunk(document_id, chunk_index)`
- `FULLTEXT idx_chunk_ft(chunk_text, keyword_tags)` — InnoDB FULLTEXT

---

### 2.3 QcStandardConflict — 标准冲突

**表名**: `qc_standard_conflict`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| variety | VARCHAR(100) | NOT NULL | 品种 |
| grade | VARCHAR(100) | NOT NULL | 牌号 |
| indicator_id | VARCHAR(64) | NOT NULL | 冲突指标 |
| standard_id_a | VARCHAR(64) | NOT NULL | 标准 A |
| standard_id_b | VARCHAR(64) | NOT NULL | 标准 B |
| limit_a_upper | DECIMAL(20,6) | | A 上限 |
| limit_a_lower | DECIMAL(20,6) | | A 下限 |
| limit_b_upper | DECIMAL(20,6) | | B 上限 |
| limit_b_lower | DECIMAL(20,6) | | B 下限 |
| conflict_status | VARCHAR(30) | NOT NULL | ConflictStatus |
| resolution_note | VARCHAR(500) | | 裁定说明 |
| resolved_by | VARCHAR(64) | | 裁定人 |
| resolved_time | VARCHAR(32) | | 裁定时间 |
| demo_flag | TINYINT(1) | DEFAULT 0 | 演示样例标记 |

**索引**: `idx_conflict_lookup(variety, grade, indicator_id, conflict_status)`

---

### 2.4 QcAiPromptVersion — Prompt 版本注册

**表名**: `qc_ai_prompt_version`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| prompt_key | VARCHAR(50) | NOT NULL | 如 rag-qa, judgment-explain |
| version_no | VARCHAR(20) | NOT NULL | 如 v1.0.0 |
| file_path | VARCHAR(200) | NOT NULL | classpath 路径 |
| is_active | TINYINT(1) | DEFAULT 0 | 是否当前激活版本 |
| description | VARCHAR(500) | | 变更说明 |

**唯一索引**: `uk_prompt_version(prompt_key, version_no)`

---

### 2.5 QcAiCallAuditLog — AI 调用审计

**表名**: `qc_ai_call_audit_log`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| call_source | VARCHAR(30) | NOT NULL | AiCallSource |
| prompt_key | VARCHAR(50) | | |
| prompt_version | VARCHAR(20) | | |
| model_name | VARCHAR(50) | | |
| input_summary | VARCHAR(2000) | | 脱敏输入摘要 |
| output_summary | VARCHAR(2000) | | 脱敏输出摘要 |
| citation_ids | VARCHAR(500) | | 引用的 chunk id 列表（JSON） |
| prompt_tokens | INT | DEFAULT 0 | |
| completion_tokens | INT | DEFAULT 0 | |
| total_tokens | INT | DEFAULT 0 | |
| latency_ms | INT | | 响应耗时 |
| success | TINYINT(1) | NOT NULL | |
| degraded | TINYINT(1) | DEFAULT 0 | 是否降级 |
| error_type | VARCHAR(30) | | AiErrorType |
| trace_id | VARCHAR(64) | | MDC traceId |
| biz_ref_id | VARCHAR(64) | | 关联 judgmentId / batchNo 等 |

**索引**: `idx_audit_time(create_date_time)`, `idx_audit_source(call_source)`, `idx_trace(trace_id)`

---

### 2.6 QcAiJudgmentExplanation — AI 判定解释

**表名**: `qc_ai_judgment_explanation`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| judgment_id | VARCHAR(64) | NOT NULL UNIQUE | 关联 qc_judgment_result.id |
| narrative_text | TEXT | | AI 自然语言解释 |
| confidence_level | VARCHAR(10) | NOT NULL | ConfidenceLevel |
| manual_review_required | TINYINT(1) | DEFAULT 0 | |
| degraded | TINYINT(1) | DEFAULT 0 | |
| baseline_json | TEXT | | 规则基线结构化解释（JSON） |
| citation_ids | VARCHAR(500) | | RAG 引用 chunk ids |
| audit_log_id | VARCHAR(64) | | 关联 qc_ai_call_audit_log.id |

---

### 2.7 QcConcessionAssessment — AI 让步评估

**表名**: `qc_concession_assessment`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| judgment_id | VARCHAR(64) | NOT NULL | |
| concession_id | VARCHAR(64) | | 关联让步申请（可选） |
| risk_level | VARCHAR(10) | NOT NULL | LOW / MEDIUM / HIGH |
| customer_impact | TEXT | | |
| suggested_conditions | TEXT | | 建议接收条件 |
| historical_cases | TEXT | | JSON 历史案例引用 |
| confidence_level | VARCHAR(10) | | |
| degraded | TINYINT(1) | DEFAULT 0 | |
| audit_log_id | VARCHAR(64) | | |

**索引**: `idx_assessment_judgment(judgment_id)`

---

### 2.8 QcAiSuggestion — 复检/改判 AI 建议

**表名**: `qc_ai_suggestion`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| suggestion_type | VARCHAR(20) | NOT NULL | REINSPECTION / REJUDGMENT |
| ref_id | VARCHAR(64) | NOT NULL | judgment_id 或 record_id |
| recommended_action | VARCHAR(20) | NOT NULL | YES / NO / CONDITIONAL |
| focus_indicators | VARCHAR(500) | | JSON 关注指标 |
| reason_text | TEXT | | |
| confidence_level | VARCHAR(10) | | |
| degraded | TINYINT(1) | DEFAULT 0 | |
| audit_log_id | VARCHAR(64) | | |

**索引**: `idx_suggestion_ref(suggestion_type, ref_id)`

---

### 2.9 QcEvaluationSample — 评测样例

**表名**: `qc_evaluation_sample`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| sample_code | VARCHAR(50) | NOT NULL UNIQUE | 如 EV-NORMAL-001 |
| category | VARCHAR(30) | NOT NULL | EvaluationCategory |
| input_payload | TEXT | NOT NULL | JSON 输入 |
| expected_outcome | TEXT | NOT NULL | JSON 期望（结论/拒答/引用） |
| weight | DECIMAL(5,2) | DEFAULT 1.0 | 评分权重 |

---

### 2.10 QcEvaluationRun — 评测运行批次

**表名**: `qc_evaluation_run`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| run_no | VARCHAR(50) | NOT NULL UNIQUE | |
| prompt_version | VARCHAR(20) | | 评测时 Prompt 版本 |
| model_name | VARCHAR(50) | | |
| status | VARCHAR(20) | NOT NULL | EvaluationRunStatus |
| total_samples | INT | | |
| passed_samples | INT | | |
| rule_pass_rate | DECIMAL(5,2) | | 业务规则通过率 % |
| ai_accuracy_rate | DECIMAL(5,2) | | |
| citation_hit_rate | DECIMAL(5,2) | | |
| avg_latency_ms | INT | | |
| manual_review_hit_rate | DECIMAL(5,2) | | |
| report_json | TEXT | | 完整报告 |
| finished_time | VARCHAR(32) | | |

---

## 3. 实体关系（002 增量）

```text
QcQualityStandard 1──* QcStandardDocument 1──* QcStandardDocumentChunk
QcQualityStandard *──* QcStandardConflict (via standard_id_a/b)
QcJudgmentResult 1──0..1 QcAiJudgmentExplanation
QcJudgmentResult 1──* QcConcessionAssessment
QcJudgmentResult 1──* QcAiSuggestion
QcAiCallAuditLog 1──0..* (各 AI 实体 via audit_log_id)
QcEvaluationRun 1──* QcEvaluationSample (运行时快照，可选结果明细表扩展)
```

---

## 4. 001 表扩展（非新表）

| 表 | 扩展 | 说明 |
|----|------|------|
| qc_quality_cert_data | + `ai_summary_text` TEXT | 质保书 AI 说明 |
| sys_menu | + 6 菜单 seed | RAG/冲突/AI审计/评测等 |

---

## 5. 状态与约束摘要

- **冲突未裁定（PENDING）**: AI 判定解释 confidence=LOW，manual_review_required=1，叙述中 MUST 列出冲突双方限值。
- **审计日志**: 只增不改；`input_summary`/`output_summary` 经 MaskUtils 脱敏。
- **Prompt 版本**: 每个 `prompt_key` 同时仅一个 `is_active=1`。
- **评测样例**: `expected_outcome` 中 `mustRefuse=true` 时 AI 输出含「未找到」或拒绝执行即 PASS。
