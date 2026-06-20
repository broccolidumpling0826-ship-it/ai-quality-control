## 1. Schema, Dictionaries, And Seeds

- [ ] 1.1 Add migration for `qc_standard_document` with document metadata, source file path, parse/index status, and applicability fields
- [ ] 1.2 Add migration for `qc_standard_clause` with clause text, citation metadata, standard linkage, applicability hints, and ES document key
- [ ] 1.3 Add migration for `standard_conflict` with conflict type, involved standards, indicator, limits, status,裁决 fields, and judgment linkage
- [ ] 1.4 Add migration for `qc_ai_assessment` with assessment type, business object, input snapshot, references, model metadata, raw output, confidence, cache flag, adoption status, and human opinion
- [ ] 1.5 Add migration for confidence configuration with rule/RAG/LLM weights, thresholds, enabled flag, operator, and update time
- [ ] 1.6 Add migration for `alternative_stock` demo-compatible replacement resource data
- [ ] 1.7 Add migration or seed structure for evaluation cases and AI cache entries
- [ ] 1.8 Add `STANDARD_CONFLICT` to judgment dictionaries and backend enum mappings
- [ ] 1.9 Update menu/RBAC seed data for standard RAG retrieval, standard conflict detection, AI assessment records, confidence configuration, and hidden detail routes
- [ ] 1.10 Add idempotent demo seed scripts for 20 documents, 100 inspection records, 5 conflict samples, complaint/case documents, alternative stock, and 30 evaluation cases

## 2. Backend Gateway Infrastructure

- [ ] 2.1 Add model gateway interfaces for chat generation, embeddings, timeout handling, and provider-neutral request/response models
- [ ] 2.2 Add DeepSeek-compatible model gateway implementation with environment-backed configuration
- [ ] 2.3 Add vector-store gateway interfaces for clause indexing, search, delete/reindex, and provider-neutral retrieval results
- [ ] 2.4 Add Elasticsearch 8.15.0 vector-store gateway implementation with environment-backed configuration
- [ ] 2.5 Add gateway health checks that do not expose secrets
- [ ] 2.6 Add gateway failure logging with trace id, operation, provider, business id, and safe error category
- [ ] 2.7 Add AI degradation service that selects cache, rule template, raw ES retrieval, or unavailable response
- [ ] 2.8 Add unit tests for gateway fallback selection and secret-safe logging behavior

## 3. Standard Document And RAG Services

- [ ] 3.1 Add entities, mappers, DTOs, VOs, and service interfaces for standard documents
- [ ] 3.2 Add entities, mappers, DTOs, VOs, and service interfaces for standard clauses
- [ ] 3.3 Add API endpoints to register/list/detail/delete standard documents and view parse/index status
- [ ] 3.4 Add clause splitting/import flow for prepared Markdown/text/PDF-derived content used by the demo
- [ ] 3.5 Add indexing service that writes clause metadata to MySQL and searchable/vector payloads to the vector gateway
- [ ] 3.6 Add reindex and failed-index retry operations for standard documents
- [ ] 3.7 Add structured-rule versus clause consistency check service and warning output
- [ ] 3.8 Add RAG query API returning generated answer, cited clauses, scores, confidence status, and no-evidence refusal
- [ ] 3.9 Add raw-clause retrieval response path when model generation is unavailable but ES search succeeds
- [ ] 3.10 Add prompt-injection guardrails to RAG prompt construction and refusal behavior
- [ ] 3.11 Add tests for no-evidence refusal, low-score warning, citation metadata, and raw retrieval fallback

## 4. Standard Conflict And Judgment Engine

- [ ] 4.1 Extend judgment enum, dictionary conversion, frontend labels, and API VOs for `STANDARD_CONFLICT`
- [ ] 4.2 Add mapper queries to retrieve all applicable standards per priority for a product, customer, indicator, and inspection date
- [ ] 4.3 Implement priority-resolvable conflict detection and conflict record creation without blocking normal judgment
- [ ] 4.4 Implement same-priority conflict detection that returns `STANDARD_CONFLICT`
- [ ] 4.5 Persist conflict details with involved standards, compared limits, conflict level, selected priority when resolvable, and related judgment id
- [ ] 4.6 Update judgment save flow to snapshot conflict judgment and keep existing deterministic evidence behavior
- [ ] 4.7 Add conflict裁决 service with permission checks,裁决 standard, rationale, operator, and audit logging
- [ ] 4.8 Add rejudge-after裁决 flow that marks the conflict judgment historical/non-final and creates a new final judgment
- [ ] 4.9 Update dashboard/statistics/list filters to count and display standard conflicts separately from reinspection
- [ ] 4.10 Add backend tests for priority-resolvable conflict, same-priority conflict, unauthorized裁决, and rejudge-after裁决

## 5. AI Assessment And Confidence

- [ ] 5.1 Add AI assessment entity, mapper, DTOs, VOs, service, and list/detail APIs
- [ ] 5.2 Enforce immutability of AI raw output while allowing adoption status and human opinion updates
- [ ] 5.3 Add confidence config entity, mapper, service, and API with active/default config retrieval
- [ ] 5.4 Validate confidence weights sum to 1 and thresholds satisfy high > medium > low
- [ ] 5.5 Restrict confidence config updates to authorized roles and write audit logs
- [ ] 5.6 Implement confidence calculation using rule score, RAG score, LLM score, active weights, and factor details
- [ ] 5.7 Apply confidence labels consistently across explanation, concession, advice, and certificate features
- [ ] 5.8 Add backend tests for confidence validation, calculation, permissions, and audit logging

## 6. AI Judgment Explanation

- [ ] 6.1 Extend judgment explanation VO/API to include source citations, confidence label/factors, AI text, degradation source, and conflict warnings
- [ ] 6.2 Implement structured rule explanation template from existing judgment evidence
- [ ] 6.3 Implement cited AI explanation generation using judgment evidence, matched standards, retrieved clauses, conflicts, and confidence context
- [ ] 6.4 Persist generated or cached judgment explanations as AI assessment records
- [ ] 6.5 Mark missing citations explicitly without inventing clauses
- [ ] 6.6 Display structured-document inconsistency warnings in explanation output
- [ ] 6.7 Add low-confidence manual review guidance to explanation output
- [ ] 6.8 Add tests for qualified, concession, missing-citation, low-confidence, and conflict explanation cases

## 7. Concession Risk Assessment

- [ ] 7.1 Add concession risk request/response DTOs and APIs linked to `CAN_CONCESSION` judgments
- [ ] 7.2 Implement customer usage resolution from customer agreement, customer profile/default seed data, or manual input
- [ ] 7.3 Add alternative stock query service using the seeded `alternative_stock` data
- [ ] 7.4 Retrieve historical complaint/case clauses for concession context through RAG
- [ ] 7.5 Generate concession risk assessment with risk level, dimension reasoning, cited sources, suggested conditions, and confidence
- [ ] 7.6 Enforce low-confidence refusal for concession recommendations and require manual quality review
- [ ] 7.7 Persist concession risk outputs as immutable AI assessment records
- [ ] 7.8 Add adoption/ignore handling for concession assessment with audit trail
- [ ] 7.9 Add backend tests for complete assessment, missing usage, missing alternative stock, complaint citation, and low-confidence refusal

## 8. Reinspection And Rejudgment Advice

- [ ] 8.1 Add AI reinspection advice API linked to judgment details
- [ ] 8.2 Generate reinspection advice from abnormal indicators, sample type, deviation, history, and confidence
- [ ] 8.3 Add accept/ignore advice actions without auto-creating reinspection records
- [ ] 8.4 Prefill existing reinspection application with accepted suggestion reason and responsible context
- [ ] 8.5 Add AI rejudgment advice API linked to judgment details or new evidence context
- [ ] 8.6 Generate rejudgment advice with target judgment, reason, evidence summary, affected scope, citations, and confidence
- [ ] 8.7 Prefill existing rejudgment application with accepted suggestion data
- [ ] 8.8 Withhold definitive advice when confidence is low
- [ ] 8.9 Persist advice outputs and adoption/ignore status as AI assessment records
- [ ] 8.10 Add tests for advice generation, low-confidence withholding, accept prefill, and ignore audit

## 9. Quality Certificate AI And PDF

- [ ] 9.1 Extend certificate generation DTO/VO to include explanation text, citation summary, confidence, and preview/final marker
- [ ] 9.2 Block formal certificate generation for final `STANDARD_CONFLICT` judgments
- [ ] 9.3 Add non-final preview generation path for unresolved conflict records with clear warning text
- [ ] 9.4 Generate certificate explanation from AI assessment or fallback rule template
- [ ] 9.5 Persist certificate AI explanation and citations in certificate snapshot data
- [ ] 9.6 Add PDF generation service with key indicators, product metadata, judgment result, explanation, citations, generation time, and operator
- [ ] 9.7 Add PDF download API and audit logging
- [ ] 9.8 Add tests for qualified PDF, low-confidence fallback, conflict block, conflict preview, and audit logging

## 10. Frontend Pages And UX

- [ ] 10.1 Add standard RAG retrieval page with query input, answer area, source clause list, scores, low-confidence warning, and raw retrieval fallback display
- [ ] 10.2 Add standard document/clauses status UI entry points under standard library where needed for demo operation
- [ ] 10.3 Enhance judgment explanation page with AI explanation, citations, confidence factors, conflict warnings, and degradation labels
- [ ] 10.4 Add standard conflict list/detail/裁决 pages with involved standards, indicator comparisons, status,裁决 form, and rejudge result link
- [ ] 10.5 Enhance concession pages with AI risk assessment card, required usage input, citations, confidence, and adoption actions
- [ ] 10.6 Enhance reinspection flow to accept AI suggestion prefill while requiring user submit
- [ ] 10.7 Enhance rejudgment flow to accept AI suggestion prefill while requiring user submit
- [ ] 10.8 Enhance certificate page with AI explanation, citation summary, PDF download, conflict block, and non-final preview marker
- [ ] 10.9 Add AI assessment record list/detail page for audit-oriented review
- [ ] 10.10 Add confidence configuration page with validation feedback and permission-aware controls
- [ ] 10.11 Update dashboard to show AI risk warnings, low-confidence pending review, and standard conflict pending裁决
- [ ] 10.12 Verify dynamic menu component paths, route names, icons, and permissions for all new pages

## 11. Demo And Evaluation Data

- [ ] 11.1 Create 20 prepared standard/agreement documents and clause files covering national, enterprise, and customer agreement sources
- [ ] 11.2 Create structured standard rules matching the prepared documents and preserving structured data as truth
- [ ] 11.3 Create 100 inspection records covering qualified, unqualified, need reinspection, concession, no-standard, low-confidence, and conflict cases
- [ ] 11.4 Create 5 conflict standard samples covering cross-priority and same-priority conflicts
- [ ] 11.5 Create simulated complaint/case JSON or text documents and indexable clauses
- [ ] 11.6 Create alternative stock seed data for concession assessment scenarios
- [ ] 11.7 Create at least 30 evaluation cases with 10 normal, 10 boundary/abnormal, 5 low-confidence/refusal, and 5 prompt-injection/safety cases
- [ ] 11.8 Create pre-generated AI cache entries for the required final demo scenarios
- [ ] 11.9 Document the main demo chain and separate standard conflict demo scenario
- [ ] 11.10 Verify demo seeds can be rerun without duplicate records or document the reset procedure

## 12. Verification

- [ ] 12.1 Run OpenSpec validation for the change
- [ ] 12.2 Run backend tests for new and affected services
- [ ] 12.3 Run frontend build
- [ ] 12.4 Manually verify standard RAG query with cited answer and no-evidence refusal
- [ ] 12.5 Manually verify inspection entry to rule judgment to cited AI explanation
- [ ] 12.6 Manually verify concession risk assessment with low-confidence refusal path
- [ ] 12.7 Manually verify standard conflict detection,裁决, rejudge, and certificate block
- [ ] 12.8 Manually verify certificate snapshot and PDF download
- [ ] 12.9 Manually verify AI gateway degradation using cache, rule template, raw retrieval, and unavailable states
- [ ] 12.10 Record any unavailable external-service checks and required environment variables for implementation handoff
