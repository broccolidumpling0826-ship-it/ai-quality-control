## Why

Steel product quality judgment is currently rule-based and workflow-capable, but it does not provide source-grounded standard retrieval, cited AI explanations, concession risk reasoning, or explicit standard conflict裁决. The final competition scenario requires a traceable loop from inspection entry to standard matching, explanation, concession assessment, conflict handling, and quality certificate output.

This change upgrades the system from “判定结果可见” to “判定依据可追溯、AI 建议可审计、冲突处置可裁决” while preserving deterministic rule judgment as the source of truth.

## What Changes

- Add standard RAG retrieval over national standards, enterprise standards, customer agreements, and complaint/case documents, with source clauses returned for every generated answer.
- Add a real document ingestion path for PDF, Office, Markdown, and text sources: extract text, chunk by clause/paragraph rules, generate embeddings, and store vectors in Elasticsearch before retrieval.
- Add model and vector-store gateway abstractions so business code is not coupled to a concrete model vendor or Elasticsearch implementation details.
- Store original standard documents, parser status, indexed chunks, embedding/index status, and citation metadata alongside existing structured standard rules.
- Extend judgment explanation with cited clauses, confidence labels, conflict warnings, and AI/rule fallback behavior.
- Add standard conflict detection for cross-priority and same-priority conflicts, including a new `STANDARD_CONFLICT` judgment result and human裁决 flow.
- Add persisted AI assessment records for judgment explanation, concession risk, reinspection advice, rejudgment advice, and certificate explanation outputs.
- Add concession risk assessment using customer usage, deviation, historical complaints/cases, alternative stock, standard clauses, and confidence gating.
- Add AI-assisted reinspection and rejudgment suggestions that can prefill workflows, but never auto-create or auto-approve business records.
- Add quality certificate Q&A by coil/batch, grounded in certificate snapshots, inspection records, judgment evidence, and standard clauses.
- Add quality certificate output controls with data snapshot and optional PDF export; unresolved or non-release states can only generate internal preview where allowed.
- Add confidence weight configuration with audit logging and permission controls.
- Add repeatable demo/evaluation seed data for 20 documents, 100 inspection records, 5 conflict samples, and 30 evaluation cases.
- Split delivery into P0/P1/P2 so the final competition MVP is stable before broad platform expansion.

## Capabilities

### New Capabilities

- `standard-rag-retrieval`: Standard/agreement/case document ingestion from PDF/Office/Markdown/text files, deterministic clause-aware chunking, embedding-based vector indexing, natural-language retrieval, cited answers, and no-evidence refusal.
- `ai-gateway-degradation`: Model gateway, vector-store gateway, OpenAI-compatible model configuration, Elasticsearch configuration, and layered AI fallback behavior.
- `ai-judgment-explanation`: Source-cited judgment explanations with limits, deviations, trigger rules, confidence, and fallback text.
- `standard-conflict-detection`: Multi-standard conflict detection, `STANDARD_CONFLICT`, conflict records, human裁决, and re-judgment after裁决.
- `concession-risk-assessment`: Persisted AI concession risk assessment based on usage, deviation, complaints/cases, alternative stock, and confidence gating.
- `reinspection-rejudgment-advice`: AI suggestions for reinspection and rejudgment that prefill existing workflows while keeping human control.
- `quality-cert-qa`: Coil/batch certificate Q&A grounded in certificate snapshots, inspection records, judgment evidence, and standard clauses.
- `quality-cert-ai-pdf`: Quality certificate data snapshot, AI explanation, cited sources, PDF export, and conflict-state restrictions.
- `ai-confidence-config`: Configurable confidence weights/thresholds with validation, permissions, and audit logging.
- `demo-evaluation-dataset`: Repeatable demo and evaluation data for standards, agreements, inspections, conflicts, AI cache, and adversarial cases.

### Modified Capabilities

- `dynamic-menu`: Menu/RBAC seed and dynamic route coverage must include the new P0/P1 AI quality pages and permissions.

## Impact

- Backend: new services for AI gateway, vector gateway, document parsing, deterministic chunking, embedding/indexing, RAG retrieval, certificate Q&A, AI assessments, standard conflicts, confidence config, and controlled certificate output; updates to judgment, concession, reinspection, rejudgment, certificate, dashboard, audit, and menu/RBAC flows.
- Frontend: P0 pages/enhancements for standard RAG retrieval, judgment explanation, concession risk, certificate Q&A, conflict handling, and demo scripts; P1 pages for AI assessment records, confidence configuration, PDF output, and broader workflow polish.
- Database: new tables for standard documents/clauses, standard conflicts, AI assessments/cache, confidence config, alternative stock, and evaluation cases; new migrations and seed scripts.
- Dependencies/systems: OpenAI-compatible chat and embedding APIs via gateway; Elasticsearch 8.15.0 via vector-store gateway; Apache PDFBox/Apache POI for document text extraction; PDF generation support; environment-based configuration for external services.
- Data: repeatable demo/evaluation initialization for competition scenarios and AI fallback cache.
