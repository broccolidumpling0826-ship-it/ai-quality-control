## Why

Steel product quality judgment is currently rule-based and workflow-capable, but it does not provide source-grounded standard retrieval, cited AI explanations, concession risk reasoning, or explicit standard conflict裁决. The final competition scenario requires a traceable loop from inspection entry to standard matching, explanation, concession assessment, conflict handling, and quality certificate output.

This change upgrades the system from “判定结果可见” to “判定依据可追溯、AI 建议可审计、冲突处置可裁决” while preserving deterministic rule judgment as the source of truth.

## What Changes

- Add standard RAG retrieval over national standards, enterprise standards, customer agreements, and complaint/case documents, with source clauses returned for every generated answer.
- Add model and vector-store gateway abstractions so business code is not coupled to DeepSeek or Elasticsearch.
- Store original standard documents and indexed clauses alongside existing structured standard rules.
- Extend judgment explanation with cited clauses, confidence labels, conflict warnings, and AI/rule fallback behavior.
- Add standard conflict detection for cross-priority and same-priority conflicts, including a new `STANDARD_CONFLICT` judgment result and human裁决 flow.
- Add persisted AI assessment records for judgment explanation, concession risk, reinspection advice, rejudgment advice, and certificate explanation outputs.
- Add concession risk assessment using customer usage, deviation, historical complaints/cases, alternative stock, standard clauses, and confidence gating.
- Add AI-assisted reinspection and rejudgment suggestions that can prefill workflows, but never auto-create or auto-approve business records.
- Add quality certificate PDF export with key indicators, judgment result, AI explanation, and cited sources; unresolved `STANDARD_CONFLICT` can only generate a non-final preview.
- Add confidence weight configuration with audit logging and permission controls.
- Add repeatable demo/evaluation seed data for 20 documents, 100 inspection records, 5 conflict samples, and 30 evaluation cases.

## Capabilities

### New Capabilities

- `standard-rag-retrieval`: Standard/agreement/case document ingestion, clause indexing, natural-language retrieval, cited answers, and no-evidence refusal.
- `ai-gateway-degradation`: Model gateway, vector-store gateway, DeepSeek/Elasticsearch configuration, and layered AI fallback behavior.
- `ai-judgment-explanation`: Source-cited judgment explanations with limits, deviations, trigger rules, confidence, and fallback text.
- `standard-conflict-detection`: Multi-standard conflict detection, `STANDARD_CONFLICT`, conflict records, human裁决, and re-judgment after裁决.
- `concession-risk-assessment`: Persisted AI concession risk assessment based on usage, deviation, complaints/cases, alternative stock, and confidence gating.
- `reinspection-rejudgment-advice`: AI suggestions for reinspection and rejudgment that prefill existing workflows while keeping human control.
- `quality-cert-ai-pdf`: Quality certificate data snapshot, AI explanation, cited sources, PDF export, and conflict-state restrictions.
- `ai-confidence-config`: Configurable confidence weights/thresholds with validation, permissions, and audit logging.
- `demo-evaluation-dataset`: Repeatable demo and evaluation data for standards, agreements, inspections, conflicts, AI cache, and adversarial cases.

### Modified Capabilities

- None.

## Impact

- Backend: new services for AI gateway, vector gateway, document indexing, RAG retrieval, AI assessments, standard conflicts, confidence config, and PDF export; updates to judgment, concession, reinspection, rejudgment, certificate, dashboard, audit, and menu/RBAC flows.
- Frontend: new pages for standard RAG retrieval, standard conflict detection, AI assessment records, and confidence configuration; enhancements to judgment explanation, concession, reinspection, rejudgment, certificate, dashboard, and admin pages.
- Database: new tables for standard documents/clauses, standard conflicts, AI assessments/cache, confidence config, alternative stock, and evaluation cases; new migrations and seed scripts.
- Dependencies/systems: DeepSeek-compatible model API via gateway; Elasticsearch 8.15.0 via vector-store gateway; PDF generation support; environment-based configuration for external services.
- Data: repeatable demo/evaluation initialization for competition scenarios and AI fallback cache.
