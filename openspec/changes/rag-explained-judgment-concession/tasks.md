## 1. P0 Schema And Seed Foundation

- [x] 1.1 Add additive migrations for standard documents, standard clauses, standard conflicts, AI assessments/cache, confidence config, alternative stock, and evaluation cases
- [x] 1.2 Add `STANDARD_CONFLICT` to backend enum, dictionary seed, frontend label mappings, and status filters
- [x] 1.3 Add P0 menu/RBAC seed entries for standard RAG retrieval, standard conflict detection, certificate Q&A, and hidden conflict裁决/detail routes
- [x] 1.4 Add deterministic P0 demo seed data for one qualified, one unqualified, one concession, and one standard-conflict scenario
- [x] 1.5 Add prepared P0 standard/agreement clause files and matching structured rules for the four required demo scenarios
- [x] 1.6 Add P0 AI cache seed entries for required demo explanation, concession assessment, and certificate Q&A outputs

## 2. P0 AI And Vector Gateways

- [x] 2.1 Add `ModelGateway` interfaces and provider-neutral request/response models
- [x] 2.2 Add DeepSeek-compatible model gateway implementation with environment-backed configuration and timeout handling
- [x] 2.3 Add `VectorStoreGateway` interfaces for clause indexing and search
- [x] 2.4 Add Elasticsearch 8.15.0 vector gateway implementation with environment-backed configuration
- [x] 2.5 Add layered degradation service supporting cache, rule template, raw ES retrieval, and unavailable responses
- [x] 2.6 Add gateway health/failure logging without exposing API keys, passwords, or prompt secrets

## 3. P0 Standard RAG Retrieval

- [x] 3.1 Add standard document and clause entities, mappers, DTOs, VOs, services, and APIs needed for P0 prepared documents
- [x] 3.2 Add prepared clause import/index flow for demo Markdown/text-derived standard and agreement clauses
- [x] 3.3 Add RAG query API returning cited answer, source clauses, retrieval scores, confidence band, and cache/degradation marker
- [x] 3.4 Add no-evidence refusal behavior for missing or low-quality retrieval
- [x] 3.5 Add raw-clause response when ES search succeeds but model generation is unavailable
- [x] 3.6 Add prompt-injection safety checks that keep answers grounded in retrieved clauses
- [x] 3.7 Verify P0 RAG scenarios for normal query, low-score query, no-evidence refusal, and prompt injection

## 4. P0 Deterministic Judgment And Conflict

- [x] 4.1 Add candidate standard query/service that returns candidate, selected, suppressed, and conflict standards instead of only `LIMIT 1`
- [x] 4.2 Define comparable indicator/unit/spec matching utilities for conflict checks
- [x] 4.3 Implement priority-resolvable conflict recording for cross-priority limit differences
- [x] 4.4 Implement same-priority blocking conflict detection for overlapping effective date/spec ranges, incompatible numeric limits, unit mismatch, and口径 mismatch
- [x] 4.5 Return and persist `STANDARD_CONFLICT` for unresolved same-priority conflict cases
- [x] 4.6 Prevent `STANDARD_CONFLICT` from appearing as a manual rejudgment target
- [x] 4.7 Reject direct manual rejudgment that attempts to bypass unresolved `STANDARD_CONFLICT`
- [x] 4.8 Add conflict裁决 API with permission checks,裁决 rationale, audit logging, and rejudge-after裁决
- [ ] 4.9 Verify P0 conflict demo: conflict detection, blocked certificate,裁决, rejudge, final judgment

## 5. P0 Judgment Explanation

- [x] 5.1 Extend judgment explanation response with candidate standards, citations, confidence band/factors, conflict warnings, and degradation source
- [x] 5.2 Implement rule-template explanation from structured judgment evidence
- [x] 5.3 Implement cited AI explanation using selected standard, judgment evidence, conflict data, and retrieved clauses
- [x] 5.4 Mark missing citations explicitly and cap confidence at medium when structured rule exists but source paragraph is missing
- [x] 5.5 Force low confidence for unresolved `STANDARD_CONFLICT`, standard gap, and source/structured contradiction
- [x] 5.6 Persist explanation output, input snapshot, citations, confidence factors, model/cache metadata, and raw output in AI assessment records
- [ ] 5.7 Verify P0 explanation for qualified, unqualified, concession, and conflict records

## 6. P0 Concession Risk Assessment

- [x] 6.1 Add concession risk API linked to `CAN_CONCESSION` judgments
- [x] 6.2 Resolve customer usage from agreement, seeded customer profile/default, or required manual input
- [x] 6.3 Query seeded alternative stock and complaint/case clauses for risk evidence
- [x] 6.4 Implement deterministic risk baseline rules producing `riskLevel`, `mustReview`, `missingInfo`, `suggestedConditions`, `blockingReasons`, and `evidenceRefs`
- [x] 6.5 Use AI only to organize evidence and wording after baseline rules have been applied
- [x] 6.6 Refuse definitive concession advice for low confidence, missing usage, no standard coverage, missing concession clause, or unresolved conflict
- [x] 6.7 Persist concession risk assessment and adoption/ignore status without allowing AI raw output edits
- [ ] 6.8 Verify P0 concession demo with normal risk output, missing-info path, and low-confidence refusal

## 7. P0 Quality Certificate Q&A

- [x] 7.1 Add certificate Q&A API by coil number and batch number
- [x] 7.2 Ground Q&A answers in certificate snapshot, inspection record, final judgment, judgment evidence, concession state, conflict state, and cited standard clauses
- [x] 7.3 Answer “why can this coil be certified” for releasable records with judgment and citation evidence
- [x] 7.4 Answer indicator-basis questions with inspection value, structured limit, deviation, trigger rule, and cited clause
- [x] 7.5 Refuse or mark non-final answers for `STANDARD_CONFLICT`, pending reinspection, pending concession approval/confirmation, missing required indicators, and missing certificate snapshot
- [x] 7.6 Support cache-backed certificate Q&A for P0 demo while exposing cache marker and citations
- [ ] 7.7 Verify P0 certificate Q&A for qualified, concession-pending, conflict, and missing-source cases

## 8. P0 Frontend Demo Slice

- [x] 8.1 Add standard RAG retrieval page with query, answer, citations, score/confidence display, refusal, and raw retrieval fallback
- [x] 8.2 Enhance judgment explanation page with candidate standards, citations, AI/rule explanation, confidence factors, conflict warnings, and degradation labels
- [x] 8.3 Add standard conflict list/detail/裁决 UI for the P0 conflict scenario
- [x] 8.4 Enhance concession page/detail with structured AI risk card, evidence refs, required usage input, missing-info warnings, and adoption actions
- [x] 8.5 Add certificate Q&A UI with coil/batch selector, question input, answer, citations, non-final warnings, and cache marker
- [x] 8.6 Update dashboard/workbench with P0 AI risk warning, low-confidence review, and standard conflict pending裁决 counters
- [x] 8.7 Verify dynamic menu route/component paths for P0 pages

## 9. P0 Demo And Evaluation Acceptance

- [x] 9.1 Write fixed demo script for qualified scenario with input values, matched standard, expected judgment, citations, explanation, certificate Q&A, and degradation output
- [x] 9.2 Write fixed demo script for unqualified scenario with input values, matched standard, expected judgment, citations, explanation, and certificate refusal
- [x] 9.3 Write fixed demo script for concession scenario with input values, matched standard, expected judgment, risk assessment, pending/formal certificate Q&A behavior, and degradation output
- [x] 9.4 Write fixed demo script for standard conflict scenario with conflicting standards, expected `STANDARD_CONFLICT`, blocked certificate,裁决, and rejudge result
- [x] 9.5 Add 30 evaluation cases with expected judgment, expected citation ids or absence, expected refusal behavior, expected confidence band, allowed numeric tolerance, and response time target
- [x] 9.6 Add evaluation runner/report for business rule pass rate, AI conclusion accuracy, citation hit rate, refusal accuracy, confidence band accuracy, average response time, and manual-review hit rate
- [x] 9.7 Verify P0 demo seeds can be rerun safely or document reset procedure

## 10. P1 Audit, Config, And Broader Workflow

- [x] 10.1 Add AI assessment list/detail page for audit review
- [x] 10.2 Add confidence configuration page with weight/threshold validation, role checks, and audit logs
- [x] 10.3 Add full AI reinspection advice API and UI prefill flow without auto-creating reinspection records
- [x] 10.4 Add full AI rejudgment advice API and UI prefill flow without auto-creating rejudgment records
- [x] 10.5 Add certificate data snapshot enhancements and simple PDF export for releasable judgments
- [x] 10.6 Add formal certificate gates for `UNQUALIFIED`, `NEED_REINSPECTION`, `CAN_CONCESSION` pending approval, `STANDARD_CONFLICT`, standard gap, and missing required indicators
- [x] 10.7 Add PDF generation/download audit logging
- [x] 10.8 Broaden dashboard/statistics to include conflict rate, low-confidence count, AI adoption rate, and evaluation summary

## 11. P2 Optional Expansion

- [ ] 11.1 Add advanced PDF/table extraction experiments for standards without using extraction as judgment truth
- [ ] 11.2 Add real customer complaint table integration path while preserving document/RAG simulation
- [ ] 11.3 Add real inventory or MES/WMS adapter behind alternative resource service
- [ ] 11.4 Add production-style certificate layout features such as template selection, signature area, and QR verification if required later
- [x] 11.5 Add broader analytics for AI hit rate, manual review hit rate, and conflict remediation trends

## 12. Verification

- [x] 12.1 Run `openspec validate rag-explained-judgment-concession --strict`
- [x] 12.2 Run backend tests for P0 services and affected judgment/certificate/concession flows
- [x] 12.3 Run frontend build after P0 UI pages are implemented
- [ ] 12.4 Manually verify P0 main chain: inspection entry → standard matching → judgment → cited explanation → concession risk → certificate Q&A
- [ ] 12.5 Manually verify conflict chain: conflict detection → `STANDARD_CONFLICT` → certificate block →裁决 → rejudge
- [ ] 12.6 Manually verify AI degradation: cache, rule template, raw ES retrieval, and unavailable state
- [x] 12.7 Run evaluation report and confirm all expected fields are reported
- [x] 12.8 Document unavailable external-service checks and required environment variables for handoff
