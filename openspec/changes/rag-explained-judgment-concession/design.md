## Context

The current quality-control system already supports structured standards, inspection records, deterministic judgment, judgment evidence snapshots, reinspection, rejudgment, concession acceptance, quality certificate data, statistics, menu/RBAC, and audit logs. The current judgment engine selects the highest-priority applicable standard using customer agreement > enterprise standard > national standard, then compares structured limits to inspection values.

The competition requirement adds source-grounded AI behavior: standard RAG retrieval, cited judgment explanation, concession risk advice, reinspection/rejudgment suggestions, standard conflict handling, quality certificate PDF output, and repeatable demo/evaluation data. These capabilities affect backend services, frontend pages, database schema, external AI/vector dependencies, permissions, audit, and demo data.

Key constraints:

- Rule-based judgment remains deterministic and authoritative.
- Structured standard rules are the only source of truth for numeric judgment.
- RAG text is used for retrieval, citations, explanation, and review, not for overriding structured limits.
- AI output must never invent standards, clauses, limits, or cases.
- AI may advise or prefill workflows, but humans remain responsible for reinspection, rejudgment, concession approval, and conflict裁决.
- MySQL 5.7 compatibility must be preserved.
- External model and vector services must be replaceable behind local abstractions.

## Goals / Non-Goals

**Goals:**

- Introduce a document-to-clause RAG layer over standard/agreement/case documents using Elasticsearch 8.15.0 behind a vector gateway abstraction.
- Introduce a model gateway abstraction for OpenAI-compatible chat/embedding calls and future provider replacement.
- Add a production-oriented document ingestion path for PDF/Office/Markdown/text files: extract text, chunk by standard clause/paragraph rules, embed chunks, store vectors in ES, then generate answers from retrieved chunks.
- Extend the standard domain with original documents, indexed clauses, structured-rule consistency checks, and source citations.
- Extend the judgment flow with standard conflict detection, a `STANDARD_CONFLICT` result, conflict records, human裁决, and re-judgment after裁决.
- Persist AI assessment outputs and input snapshots for explanation, concession risk, reinspection advice, rejudgment advice, and certificate explanation.
- Add confidence calculation and configurable weights/thresholds with permissions and audit logging.
- Add layered degradation so AI or ES outages do not break core judgment or demos.
- Add repeatable seed data for final competition demonstration and evaluation.

**Non-Goals:**

- Do not use PDF/table extraction as the source of structured judgment rules in the first version.
- Do not let AI decide final judgment, release, concession approval, reinspection, or rejudgment.
- Do not use RAG-extracted numeric values to override structured standard indicators.
- Do not integrate real WMS/MES/APS/ERP/customer complaint systems in this version.
- Do not build production-grade certificate layout features such as electronic seals, QR verification, or formal customer delivery templates.

### Delivery Slices

P0 is the final competition MVP and MUST be implemented first:

- Standard document parsing for PDF/Office/Markdown/text inputs using deterministic parsers and visible parse/index status.
- Clause-aware document chunking based on chapter, clause, paragraph, and natural boundaries.
- Text embedding for every indexed chunk and ES vector storage before retrieval.
- Standard RAG retrieval with citations and no-evidence refusal.
- Inspection entry to deterministic judgment using structured standards.
- AI/rule judgment explanation with citations, conflict warnings, confidence label, and fallback behavior.
- Concession risk assessment with structured risk fields and low-confidence manual-review gate.
- Quality certificate Q&A by coil/batch using certificate snapshot, inspection record, judgment evidence, and standard clauses.
- Standard conflict detection for P0 demo cases, including `STANDARD_CONFLICT` block and human裁决 path.
- Repeatable demo data and fixed scripts for qualified, unqualified, concession, and conflict scenarios.

P1 extends the platform after the MVP is stable:

- AI assessment list/detail pages, confidence configuration page, PDF export, dashboard statistics polish, and broader workflow integration.

P2 is explicitly optional for this change:

- Advanced OCR/layout reconstruction for scanned standards, external inventory/complaint system integration, production-grade certificate layout, and full analytics dashboards.

## Decisions

### 1. Keep Structured Rules As Judgment Truth

Structured standard tables (`qc_quality_standard`, `qc_standard_indicator`) continue to drive automatic judgment. Original documents and RAG clauses provide evidence and explanation only.

Rationale:

- Numeric quality decisions require deterministic comparison and auditability.
- RAG text can contain old versions, OCR errors, examples, notes, or table parsing mistakes.
- This keeps current judgment behavior stable while adding source-grounded explanation.

Alternatives considered:

- Use RAG-extracted limits directly in judgment. Rejected because it creates unacceptable safety and audit risk.
- Keep only structured data and skip RAG. Rejected because competition requires source paragraphs and natural-language standard retrieval.

### 2. Add Gateway Abstractions For AI And Vector Stores

Add `ModelGateway` and `VectorStoreGateway`-style interfaces. Implement first providers using an OpenAI-compatible model platform and Elasticsearch 8.15.0.

Configuration uses environment-backed properties in `application-dev.yml` / `application.yml`, such as:

- `AI_MODEL_API_KEY`
- `AI_MODEL_BASE_URL`
- `AI_MODEL_CHAT_MODEL`
- `EMBEDDING_API_KEY`
- `EMBEDDING_BASE_URL`
- `EMBEDDING_MODEL`
- `ES_HOST`
- `ES_USERNAME`
- `ES_PASSWORD`
- `ES_STANDARD_INDEX`

Rationale:

- The user expects a SiliconFlow/OpenAI-compatible model platform and ES now, but future provider replacement should not affect business services.
- Gateway interfaces isolate timeout, retry, caching, prompt policy, and query details.

Alternatives considered:

- Directly call a concrete model vendor or ES from service implementations. Rejected due to coupling and test difficulty.
- Build a separate AI microservice now. Rejected as too heavy for the current monolith and competition timeline.

### 3. Store Documents And Clauses Separately From Structured Rules

Add standard document and clause persistence:

- `qc_standard_document`: document metadata, standard linkage, source file URL/path, parse/index status.
- `qc_standard_clause`: clause text and metadata, including standard type, code, version, effective dates, clause number, page number, indicator hints, applicability hints, and ES document key.

Elasticsearch stores searchable/vector fields and mirrors stable clause metadata for retrieval.

Rationale:

- The database remains the audit system of record.
- ES remains optimized for semantic and keyword retrieval.
- Clause IDs can be stored in AI outputs and judgment evidence citations.

### 3A. Document Ingestion Uses Parser And Rule-Based Chunking

The RAG ingestion pipeline is:

1. Upload/register source document.
2. Extract text using deterministic parsers:
   - PDF: Apache PDFBox.
   - Word/Excel: Apache POI.
   - Markdown/text: direct text parser.
3. Preserve citation anchors such as document id, standard code, version, page number when available, and clause heading.
4. Chunk extracted text using standard-aware code rules:
   - Prefer chapter/clause headings such as `7.3 力学性能`.
   - Keep a heading and its complete paragraph/table text together where possible.
   - Split oversized chunks on paragraph or sentence boundaries.
   - Optionally use lightweight sentence segmentation such as jieba or spaCy when paragraph boundaries are unclear.
5. Send chunk text plus compact metadata context to the embedding model.
6. Store chunk text, embedding vector, citation metadata, applicability metadata, and parse/index status.

Embedding models are not used for document chunking. They only convert already-chunked text into vectors. A separate semantic completeness model is not required for the default path because steel standards usually have explicit clause and paragraph structure.

Rationale:

- Standard documents already expose strong natural boundaries through clause numbers and paragraphs.
- Rule-based chunking is deterministic, cheap, auditable, and easier to reproduce.
- Lightweight NLP sentence segmentation can improve fallback splitting without introducing a new model dependency.
- RAG chunks remain evidence for retrieval and explanation; structured standard tables remain the judgment truth.

### 3B. Standard Maintenance PDF Upload And Publish-Triggered Ingestion

Standard maintenance integrates structured standards with one linked source document:

1. User saves structured standard metadata and indicators in the standard library page.
2. User uploads one PDF per standard; the file is stored under `backend/resources/standard-documents/{standardId}/`.
3. Backend upserts `qc_standard_document` with `standard_id`, source file metadata, and `parse_status/index_status`.
4. Draft upload/replace stores the file only and resets parse/index status to pending.
5. Publish triggers `ingestAndIndexDocument` for the linked PDF using PDFBox + rule-based chunking + embedding + ES.
6. Published replace purges prior clause rows and ES vectors, stores the new PDF, and re-indexes immediately.
7. Delete standard removes stored PDF, document metadata, clause rows, and ES vectors.

Publish success is not rolled back when indexing fails; the UI exposes index failure and supports manual re-index.

Rationale:

- Structured indicators remain editable before publish.
- Draft PDFs should not pollute RAG retrieval.
- Re-upload must not leave stale vectors in Elasticsearch.

### 4. Implement Consistency Checks Without Blocking All Workflows

When standard structured limits and retrieved/document clauses disagree, runtime behavior must:

- Keep judgment based on structured limits.
- Mark the inconsistency in explanation.
- Create a maintenance warning or conflict/inconsistency record.

For standard publish/update, consistency checks should be available and may block publish when configured.

Rationale:

- Production judgment must stay stable.
- Document quality issues should be visible and remediable.

### 5. Distinguish Priority-Resolvable And Same-Priority Conflicts

Conflict handling has two branches:

- Priority-resolvable conflict: customer agreement is stricter/different than enterprise/national standard. Continue judgment by priority, mark the conflict, and store it for audit.
- Same-priority conflict: two standards at the same priority apply to the same product/indicator/time window and disagree. Return `STANDARD_CONFLICT`, prohibit final certificate generation, and route to human裁决.

Rationale:

- Cross-priority conflicts are common and usually expected.
- Same-priority conflicts are standard governance defects; reinspection will not fix them.

Alternatives considered:

- Block all conflicts. Rejected because normal customer-agreement differences would stop routine production.
- Reuse `NEED_REINSPECTION`. Rejected because it misroutes the problem to lab retesting and corrupts reinspection statistics.

Conflict math:

- A candidate standard is any published standard whose type/customer scope, variety, grade, specification range, and effective date window match the inspection record.
- The selected standard is the highest-priority candidate used for deterministic judgment after conflict resolution.
- A suppressed standard is a lower-priority candidate that also matches but is not selected because a higher-priority candidate exists.
- A conflict standard is a candidate whose rule for the same indicator is incompatible with another applicable candidate under the rules below.
- Numeric limit conflict exists when the same indicator and comparable unit have non-equal lower or upper limits across matching candidates.
- Containment differences across priority levels are priority-resolvable conflicts, not blockers. Example: customer lower limit 420 MPa and national lower limit 400 MPa is marked but customer wins.
- Same-priority numeric differences are blocking conflicts when effective windows and specification ranges overlap.
- Unit mismatch or indicator口径 mismatch is a blocking conflict unless a configured conversion/alias rule makes the two indicators comparable.
- Specification range conflict exists when two same-priority candidates overlap in product spec range and define incompatible limits for the overlapped range.
- Customer agreement wider than enterprise/national standard is a risk conflict: it is priority-resolvable if the agreement is valid, but MUST be surfaced as higher-risk because it relaxes internal or national control.

### 6. Add `STANDARD_CONFLICT` As A Judgment Result

Add `STANDARD_CONFLICT` to judgment enums/dictionaries/front-end mappings/statistics handling.

When human裁决 is completed:

- Store the裁决 result in `standard_conflict`.
- Re-run judgment using the裁决 standard/rule choice.
- Save a new final `qc_judgment_result`.
- Preserve the original conflict judgment for audit.

Rationale:

- The state is semantically distinct and must be queryable/reportable.
- Audit can show why a previous automatic judgment could not be finalized.

### 7. Persist AI Assessment Outputs

Add an AI assessment table for generated explanation/advice outputs. Store:

- assessment type
- business object ID
- input snapshot
- references/citations
- model/provider/prompt version
- raw output
- structured recommendation
- confidence score/label/factors
- degradation/cache source
- adoption status
- human handling opinion
- created by/time

AI raw output is immutable. Users may only append adoption status or human handling opinion.

Rationale:

- Concession and rejudgment advice can be audited later.
- Demo fallback cache can reuse stored/generated content.
- This supports manual review hit-rate analysis.

### 8. Use Layered Degradation

AI-dependent features degrade in this order:

1. Pre-generated cache from database or local JSON seed.
2. Rule-template explanation using structured judgment evidence.
3. Raw ES retrieval summaries without LLM generation.
4. Clear unavailable state while preserving core rule judgment.

Rationale:

- Competition demos must remain presentable during network/API failures.
- Core judgment must never depend on external AI services.

### 9. Confidence Is Configured But Rule-Weighted

Confidence MUST be explainable by a rules table before any weighted score is displayed. The weighted score remains an internal ordering aid, not proof of correctness.

Default component scores:

| Component | High | Medium | Low |
| --- | --- | --- | --- |
| ruleScore | All required structured standards and indicators matched; no unresolved blocking conflict; value not in edge band | Structured rules matched but value is on configured edge band, concession range, or priority-resolvable conflict exists | Missing structured rule, standard gap, same-priority conflict, or invalid input |
| ragScore | Required source clauses found for selected standard and key abnormal indicators with score above high threshold | Some clauses found but not all key indicators or scores are between medium/high thresholds | No usable source clause, mismatched version, or retrieved clause contradicts structured rule |
| llmScore | Output only uses supplied facts/citations and passes contradiction checks | Output is template-like/degraded or minor citation coverage gaps exist | Output contains unsupported claim, missing citation for a claim, or model unavailable without cache/template |

Forced confidence bands override the weighted score:

- Any unresolved `STANDARD_CONFLICT` forces low confidence.
- Any no-standard-covering condition forces low confidence for AI explanation and blocks concession advice.
- Structured rule complete but missing source paragraph caps confidence at medium.
- RAG citation mismatch with structured limit caps confidence at low for generated wording, while judgment still follows structured data.

Default weights remain:

- rule: 0.60
- RAG: 0.30
- LLM: 0.10

Provide a backend configuration page/API to edit weights and thresholds, but only after rule-table banding is applied. Validate:

- weights sum to 1
- highThreshold > mediumThreshold > lowThreshold
- only authorized roles can modify
- all changes are audited

Rationale:

- Industrial users should not rely on LLM self-confidence.
- The business asked for a configurable page in the first version.

### 10. Quality Certificate PDF Has Controlled Scope

Quality certificate Q&A is P0. PDF output is P1 controlled output.

Certificate Q&A answers questions such as:

- “这批卷为什么能出证？”
- “某指标依据是什么？”
- “让步后质保书应该如何说明？”
- “这个卷号当前能否生成正式质保书？”

Answers MUST be grounded in certificate snapshot, inspection record, judgment evidence, concession approval state, conflict state, and cited standard clauses.

PDF output includes key indicators, judgment result, AI explanation, cited sources, generation metadata, and a simple printable layout.

Restrictions:

- `QUALIFIED` can generate formal certificate data and PDF when required key indicators exist.
- `UNQUALIFIED` cannot generate formal certificate; internal data view only.
- `NEED_REINSPECTION` cannot generate formal certificate until reinspection completes and final judgment is no longer pending.
- `CAN_CONCESSION` cannot generate formal certificate until concession approval and customer confirmation rules are satisfied.
- `STANDARD_CONFLICT` cannot generate a formal certificate.
- Standard gap or missing required indicator blocks formal certificate and may produce only an internal incomplete-data view.
- Low-confidence explanation does not block a formal certificate when structured judgment is releasable, but AI wording is omitted or marked as non-authoritative.
- Conflict states may only generate a clearly marked non-final preview.
- Low-confidence AI certificate explanation falls back to the base data template.

Rationale:

- Meets competition Q&A needs first without overbuilding formal document delivery.

### 11. Let Rules Drive Concession Risk Before AI Wording

Concession assessment MUST output structured fields before natural-language text:

- `riskLevel`: LOW, MEDIUM, HIGH, or BLOCKED
- `mustReview`: boolean
- `missingInfo`: list
- `suggestedConditions`: list
- `blockingReasons`: list
- `evidenceRefs`: list of standard/case references

Baseline rules:

- Safety-critical or high-forming usage plus strength/elongation deviation is at least HIGH risk.
- Deviation within configured concession band and no complaint history can be LOW or MEDIUM depending on usage.
- Similar historical complaint above similarity threshold raises risk at least one level.
- Available same-spec qualified replacement stock makes concession less necessary and SHOULD add a condition to use replacement instead of concession.
- Missing customer usage, unresolved conflict, missing concession clause, or no standard coverage sets `mustReview=true`.
- Unresolved conflict or no standard coverage sets `riskLevel=BLOCKED`.

AI is used to organize evidence and wording, not to override blocking rules.

### 12. Protect STANDARD_CONFLICT From Normal Rejudgment

`STANDARD_CONFLICT` is not a normal rejudgment target.

- Users MUST NOT be able to select `STANDARD_CONFLICT` as a manual rejudgment target.
- Users MUST NOT bypass conflict裁决 by rejudging a `STANDARD_CONFLICT` directly to `QUALIFIED` or `CAN_CONCESSION`.
- `STANDARD_CONFLICT` can only be created by the judgment/conflict engine.
- It can only be cleared by conflict裁决 followed by a system-triggered rejudge.

## Risks / Trade-offs

- [Risk] Model API unavailable, slow, or rate-limited during demo → Mitigation: timeout controls, pre-generated cache, rule templates, raw ES retrieval, and clear unavailable UI.
- [Risk] Elasticsearch connection/configuration fails in a system that has not used ES before → Mitigation: health checks, gateway abstraction, startup-safe optional behavior, and fallback to structured/rule display.
- [Risk] Large scope across many modules delays delivery → Mitigation: implement vertical demo slices first, then broaden list/detail/statistics coverage.
- [Risk] Structured rules and source documents diverge → Mitigation: consistency checks, runtime warnings, maintenance records, and structured-rule precedence.
- [Risk] `STANDARD_CONFLICT` affects existing statistics and UI mappings → Mitigation: update dictionaries, tags, filters, dashboard counts, and certificate gates together.
- [Risk] AI suggestions may be mistaken but appear authoritative → Mitigation: confidence labels, human-only workflow actions, immutable AI output snapshots, and adoption logging.
- [Risk] Demo seed data becomes brittle → Mitigation: deterministic IDs, idempotent SQL/JSON seed scripts, and documented scenarios.
- [Risk] PDF generation adds dependency/version friction → Mitigation: keep first layout simple and isolate PDF generation behind a service.

## Migration Plan

1. Add schema migrations for documents, clauses, conflicts, AI assessments/cache, confidence config, alternative stock, and evaluation data.
2. Add dictionary/menu/RBAC seed updates for new judgment result, pages, and permissions.
3. Add backend gateway configuration classes with environment placeholders.
4. Add backend domain services and APIs behind feature-safe fallbacks.
5. Add frontend pages and enhancements using existing menu/dynamic-route patterns.
6. Add deterministic demo/evaluation seed scripts and local JSON cache assets.
7. Validate with backend tests, frontend build, OpenSpec validation, and manual demo scenarios.

Rollback strategy:

- New tables are additive and should not mutate existing rows destructively.
- Existing deterministic judgment remains available if AI/ES features are disabled.
- New menus can be disabled through menu/RBAC seed/status if needed.
- External AI/ES config can be left unset; fallback behavior should avoid startup failure where possible.

## Open Questions

- Which Java PDF library will be used for first-version certificate export?
- Should ES indexing run synchronously on upload for demo simplicity, or via an async job for production-like behavior?
- Should confidence config live in a dedicated table or reuse a generic system config table if one is introduced later?
- How many AI assessment types should be shown in the first frontend list page versus kept backend-only for audit?
