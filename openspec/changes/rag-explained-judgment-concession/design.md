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
- Introduce a model gateway abstraction for DeepSeek-compatible chat/embedding calls and future provider replacement.
- Extend the standard domain with original documents, indexed clauses, structured-rule consistency checks, and source citations.
- Extend the judgment flow with standard conflict detection, a `STANDARD_CONFLICT` result, conflict records, human裁决, and re-judgment after裁决.
- Persist AI assessment outputs and input snapshots for explanation, concession risk, reinspection advice, rejudgment advice, and certificate explanation.
- Add confidence calculation and configurable weights/thresholds with permissions and audit logging.
- Add layered degradation so AI or ES outages do not break core judgment or demos.
- Add repeatable seed data for final competition demonstration and evaluation.

**Non-Goals:**

- Do not implement full automatic PDF/table extraction as the source of structured judgment rules in the first version.
- Do not let AI decide final judgment, release, concession approval, reinspection, or rejudgment.
- Do not use RAG-extracted numeric values to override structured standard indicators.
- Do not integrate real WMS/MES/APS/ERP/customer complaint systems in this version.
- Do not build production-grade certificate layout features such as electronic seals, QR verification, or formal customer delivery templates.

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

Add `ModelGateway` and `VectorStoreGateway`-style interfaces. Implement first providers using DeepSeek-compatible APIs and Elasticsearch 8.15.0.

Configuration uses environment-backed properties in `application-dev.yml` / `application.yml`, such as:

- `DEEPSEEK_API_KEY`
- `DEEPSEEK_BASE_URL`
- `DEEPSEEK_MODEL`
- `ES_HOST`
- `ES_USERNAME`
- `ES_PASSWORD`
- `ES_STANDARD_INDEX`

Rationale:

- The user expects DeepSeek and ES now, but future provider replacement should not affect business services.
- Gateway interfaces isolate timeout, retry, caching, prompt policy, and query details.

Alternatives considered:

- Directly call DeepSeek/ES from service implementations. Rejected due to coupling and test difficulty.
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

Confidence score is calculated as:

```text
ruleScore * ruleWeight + ragScore * ragWeight + llmScore * llmWeight
```

Default weights:

- rule: 0.60
- RAG: 0.30
- LLM: 0.10

Provide a backend configuration page/API to edit weights and thresholds. Validate:

- weights sum to 1
- highThreshold > mediumThreshold > lowThreshold
- only authorized roles can modify
- all changes are audited

Rationale:

- Industrial users should not rely on LLM self-confidence.
- The business asked for a configurable page in the first version.

### 10. Quality Certificate PDF Has Controlled Scope

PDF output includes key indicators, judgment result, AI explanation, cited sources, generation metadata, and a simple printable layout.

Restrictions:

- `STANDARD_CONFLICT` cannot generate a formal certificate.
- Conflict states may only generate a clearly marked non-final preview.
- Low-confidence AI certificate explanation falls back to the base data template.

Rationale:

- Meets competition needs without overbuilding formal document delivery.

## Risks / Trade-offs

- [Risk] DeepSeek API unavailable, slow, or rate-limited during demo → Mitigation: timeout controls, pre-generated cache, rule templates, raw ES retrieval, and clear unavailable UI.
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
