# AQC Frontend Design

Status: Draft
Last updated: 2026-06-20
Owner: Frontend implementation team

## 1. Summary

This document defines the frontend design for the AI quality-control enhancement in the existing steel quality management system. It follows a Google-style design document structure: context, goals, non-goals, detailed design, alternatives, data contracts, rollout, testing, risks, and open questions.

The frontend must preserve the current AQC visual language: dark industrial control-room UI, dense operational screens, Element Plus components, dynamic menu routing, dictionary-driven labels, and audit-friendly workflows. AI features must feel like quality-control tools, not a marketing experience or chatbot demo.

## 2. Context

The current frontend stack is:

- Vue 3 Composition API
- TypeScript
- Vite
- Vue Router 4
- Pinia
- Axios wrapper in `src/utils/request.ts`
- Element Plus and `@element-plus/icons-vue`
- ECharts for statistics

The current frontend style is defined mainly by `src/styles/global.css`:

- Dark base: `--bg-base: #060E1C`
- Panel/card background: `--bg-panel`, `--bg-card`
- Industrial cyan primary: `--cyan: #00D4FF`
- Semantic states: green, red, orange, blue, gold
- Compact component sizing, 2px to 4px radius, dense tables and forms
- Data-oriented font usage through `--font-data`

Routes are dynamically generated from backend menu data. New page components must match `frontend/src/views/<component>.vue` paths referenced by menu seed data.

## 3. Goals

- Provide consistent frontend interaction patterns for standard RAG retrieval, AI judgment explanation, concession risk assessment, reinspection/rejudgment advice, standard conflict handling, quality certificate Q&A, AI assessment records, and confidence configuration.
- Preserve existing deterministic business workflow boundaries: AI can explain, suggest, prefill, and warn, but must not auto-submit reinspection, rejudgment, concession approval, or conflict resolution.
- Make every AI answer traceable through source clauses, input snapshots, confidence factors, and degradation labels.
- Keep pages dense, scan-friendly, table-first, and suitable for repeated quality operations.
- Define route, page, component, state, permission, and data-contract conventions before implementation.

## 4. Non-Goals

- Do not introduce a landing page, marketing hero, decorative AI dashboard, or chat-only product surface.
- Do not replace deterministic judgment UI with generated prose.
- Do not let frontend hide missing sources, low confidence, unresolved conflicts, or degraded AI outputs.
- Do not build a separate design system or add a new UI library.
- Do not implement document parsing UX for fully automatic PDF/table extraction in the first version.
- Do not add customer-facing formal certificate signing, QR verification, or electronic seal UI in this phase.

## 5. Users And Primary Workflows

### 5.1 Quality Engineer

Tasks:

- Search standards and customer agreements.
- Inspect cited source clauses.
- Review judgment explanation and conflicts.
- Maintain standard documents and clause indexing status.
- Trigger reinspection or rejudgment manually when needed.

### 5.2 Quality Manager

Tasks:

- Resolve same-priority standard conflicts.
- Review low-confidence AI output.
- Approve high-risk concession decisions.
- Configure AI confidence weights and thresholds.

### 5.3 Sales Or Customer-Facing User

Tasks:

- Review concession risk advice.
- Check customer impact and historical complaints.
- View quality certificate data and ask certificate-related questions.

### 5.4 System Auditor

Tasks:

- Inspect AI assessment records.
- Verify adoption/ignore actions.
- Review permission-sensitive operations and generated certificate history.

## 6. Existing Frontend Design Constraints

### 6.1 Layout

Use the existing `MainLayout.vue` shell:

- Left sidebar with dynamic menus.
- Top bar with breadcrumb-like current page title.
- Main content inside `.content-area`.
- Page root class named after the feature, for example `.standard-rag-page`.
- Default page padding: `16px`.
- Operational content should generally fit within `1200px` to `1440px`, unless the page is a table-heavy management screen.

### 6.2 Visual Style

Use the existing industrial control-room language:

- Use dark panel/card backgrounds from CSS variables.
- Use cyan only for primary actions and active/focus states.
- Use green for qualified/success, red for unqualified/blocking, orange for warning/concession/medium risk, info/blue for neutral processing.
- Avoid oversized hero sections, decorative gradients, illustrations, floating nested cards, and low-density marketing composition.
- Cards are allowed for repeated items, major panels, detail sections, and dialogs. Do not place cards inside cards unless the inner item is a repeated data artifact.
- Border radius should stay at 2px to 4px unless Element Plus default requires otherwise.

### 6.3 Typography

- Use existing global fonts.
- Use normal UI text for labels and explanations.
- Use `var(--font-data)` for IDs, codes, scores, dates, limits, measured values, model names, and trace IDs.
- Keep headings compact. Page titles should not use hero-scale typography.
- Do not use negative letter spacing.

### 6.4 Component Use

Prefer Element Plus components:

- `el-table` for records, citations, indicators, conflicts, assessments.
- `el-card` for page panels.
- `el-form`, `el-input`, `el-select`, `el-date-picker` for filters and forms.
- `el-drawer` for side detail or edit workflows.
- `el-dialog` for short confirmations and quick actions.
- `el-tag` for status and confidence labels.
- `el-alert` for low confidence, missing evidence, conflict, and degradation warnings.
- `el-tabs` for detail pages with multiple evidence groups.
- `el-collapse` only for long cited source sections.
- `el-tooltip` for icon-only controls.

Use icons from `@element-plus/icons-vue` for buttons when available. Avoid manual SVG icons.

## 7. Information Architecture

### 7.1 New Or Enhanced Pages

| Feature | Route Path | Component | Menu Visibility | Permission |
| --- | --- | --- | --- | --- |
| Standard RAG Retrieval | `standard-rag` | `standard-rag/index` | visible | `menu:standard-rag` |
| Standard Document Status | `standard-lib/documents` | `standard-lib/documents` | visible or nested | `menu:standard` |
| Judgment Explanation Enhancement | `judgment/explanation` | `judgment/explanation` | hidden existing | `menu:judgment` |
| Standard Conflict List | `standard-conflicts` | `standard-conflicts/index` | visible | `menu:standard-conflict` |
| Standard Conflict Detail | `standard-conflicts/detail` | `standard-conflicts/detail` | hidden | `menu:standard-conflict` |
| Concession AI Risk | existing concession detail/form | existing enhanced pages | existing | `menu:concession` |
| Reinspection Advice | existing judgment/reinspection pages | existing enhanced pages | existing | `menu:reinspection` |
| Rejudgment Advice | existing judgment/re-judgment pages | existing enhanced pages | existing | `menu:rejudgment` |
| Quality Certificate Data | existing `cert-data` | existing enhanced page | existing | `menu:cert-data` |
| Quality Certificate Q&A | `cert-data/qa` or tab inside `cert-data` | `cert-data/qa` or embedded tab | visible or nested | `menu:cert-data` |
| AI Assessment Records | `ai-assessments` | `ai-assessments/index` | visible for audit/admin | `menu:ai-assessment` |
| Confidence Configuration | `admin/ai-confidence` | `admin/ai-confidence/index` | visible under admin | `menu:ai-confidence` |

### 7.2 Menu Grouping

Recommended grouping:

- `标准库`: Standard maintenance, indicator items, coverage gaps, standard documents, standard RAG retrieval.
- `检验与判定`: Inspection entry, judgment explanation.
- `质量流程`: Reinspection, rejudgment, concession, standard conflicts.
- `数据汇总`: Certificate data, certificate Q&A, quality statistics.
- `管理员`: AI assessment records if admin-only, confidence configuration, users, roles, dictionaries, menus.

## 8. Detailed Page Design

### 8.1 Standard RAG Retrieval

Purpose: natural-language standard search with source-grounded answers.

Layout:

- Top search panel with query input, source type filter, product variety, grade, customer, effective date, and search button.
- Main area split into two columns:
  - Left: generated answer or refusal/degraded state.
  - Right: cited source clauses list.
- Lower table: raw retrieval results with score, standard code, version, clause number, page number, source type, applicability.

Primary controls:

- `Search` button with search icon.
- `Clear` button.
- `Copy citation` action for each source.
- `Open document` or `View clause` action when backend supports it.

States:

- Loading: `v-loading` on answer and source panels.
- No evidence: show `el-alert` with text equivalent to "在已上传的标准/协议中未找到相关依据".
- Low relevance: show warning alert and label clauses as reference-only.
- Model unavailable but retrieval works: show raw clauses and `AI_UNAVAILABLE` degradation tag.
- Model and vector unavailable: show unavailable state, no fabricated answer.
- Prompt injection detected or unsupported instruction: show refusal and cite no sources unless normal clauses match.

Do:

- Highlight matched text with a controlled frontend highlighter that escapes HTML.
- Render source paragraph as plain text.

Do not:

- Render model output as raw HTML.
- Show an answer without source clauses unless it is an explicit no-evidence refusal or rule-template fallback.

### 8.2 Judgment Explanation Enhancement

Purpose: explain final judgment with structured rule details, source citations, conflicts, confidence, and AI/rule fallback.

Existing page `judgment/explanation.vue` remains the anchor.

Recommended sections:

1. Judgment header:
   - Coil, batch, heat, customer, variety, grade, specification.
   - Judgment tag.
   - Confidence tag.
   - Degradation tag when output is cache/template/raw retrieval.

2. Standard match and conflict strip:
   - Customer agreement, enterprise standard, national standard.
   - Hit/skipped/conflict state.
   - Same-priority conflict must use blocking red state.

3. Indicator details:
   - Existing table retained.
   - Add source citation count column.
   - Add conflict/inconsistency column when relevant.
   - Numeric values use `font-data`.

4. AI explanation panel:
   - Structured explanation first.
   - AI-generated explanation second.
   - Missing citation and low-confidence alerts above prose.

5. Source citations:
   - Table or collapsible list.
   - Fields: source type, standard code, standard name, version, clause number, page, relevance score, paragraph.

6. Actions:
   - Reinspection: only when judgment allows.
   - Rejudgment: allowed but must remain manual submission.
   - Concession: only `CAN_CONCESSION`.
   - Conflict resolution: only visible for `STANDARD_CONFLICT` and authorized users.

`STANDARD_CONFLICT` behavior:

- Do not show a final pass/fail release conclusion.
- Disable concession and certificate formal generation actions.
- Provide route to conflict detail/c裁决 page.

### 8.3 Concession AI Risk Assessment

Purpose: assist concession review without auto-approving or auto-submitting workflow records.

Placement:

- Add an AI risk panel to concession form/detail pages.
- Also provide entry from judgment explanation when judgment is `CAN_CONCESSION`.

Panel layout:

- Risk summary row:
  - Risk level tag: `LOW`, `MEDIUM`, `HIGH`, `BLOCKED`.
  - Confidence tag.
  - Degradation tag.
  - Assessment time.

- Dimension grid:
  - Customer usage.
  - Deviation degree.
  - Historical complaints/cases.
  - Alternative stock.
  - Standard concession clause.
  - Current judgment evidence.

- Suggested conditions:
  - Shipment restrictions.
  - Customer confirmation.
  - Additional inspection.
  - Traceability note.

- Source citations:
  - Standard clauses and complaint/case records.

Required user behavior:

- If customer usage is missing, show required manual input before normal assessment.
- If low confidence, do not show a definitive "recommend concession" result.
- Adoption/ignore buttons update AI assessment state only. They must not approve concession.

### 8.4 Reinspection And Rejudgment Advice

Purpose: provide suggestions and prefill workflow forms while keeping human control.

Placement:

- Judgment explanation page.
- Reinspection and rejudgment form pages as optional "AI suggestion" side panel or top alert.

Behavior:

- `Accept suggestion` populates form fields only.
- `Ignore suggestion` records the action and hides or collapses the panel.
- User must still review and submit the form.
- Low-confidence advice shows an unavailable/insufficient evidence message and no definitive recommendation.

Rejudgment guard:

- `STANDARD_CONFLICT` must not be a normal target judgment option.
- Conflict resolution must go through the standard conflict裁决 flow.

### 8.5 Standard Conflict Detection

Purpose: list, inspect, and resolve standard conflicts.

List page:

- Search filters: conflict status, conflict type, standard type, product variety, grade, customer, indicator, date range.
- Table columns:
  - Conflict ID.
  - Status.
  - Conflict type.
  - Product scope.
  - Customer.
  - Indicator.
  - Involved standards.
  - Created time.
  - Related judgment.
  - Actions.

Detail page:

- Header with blocking status and related inspection/judgment.
- Comparison table:
  - Standard code/name/version/type.
  - Effective window.
  - Indicator.
  - Lower/upper/concession limits.
  - Unit.
  - Clause citation.
-裁决 panel:
  - Controlling standard selector.
  - Required rationale.
  - Confirmation checkbox for audit-sensitive action.
  - Submit button visible only to authorized roles.

After裁决:

- Show new final judgment link.
- Preserve original conflict judgment in history.
- Make the page read-only except audit notes if resolved.

### 8.6 Quality Certificate Data And Q&A

Purpose: generate certificate snapshots, export PDF, and answer questions using certificate snapshot plus judgment evidence and cited standards.

Certificate data page enhancements:

- Add status markers:
  - `FINAL`
  - `PREVIEW`
  - `BLOCKED`
  - `DEGRADED`
- Show final judgment and confidence next to snapshot metadata.
- Disable formal generation for unresolved `STANDARD_CONFLICT`.
- For low-confidence AI explanation, fall back to base structured data and show reason.

Certificate Q&A design:

- Can be a tab inside certificate detail or a nested route `cert-data/qa`.
- Query context selector:
  - Coil number.
  - Batch number.
  - Existing certificate snapshot.
- Question input:
  - Single-line input for short questions.
  - Optional multiline advanced input only if needed.
- Answer panel:
  - Direct answer.
  - Referenced indicators.
  - Referenced judgment evidence.
  - Referenced standard clauses.
  - Degradation/confidence labels.

Required refusal examples:

- No certificate snapshot found.
- Unresolved conflict exists.
- Asked to invent unavailable clause or limit.
- Asked beyond uploaded standards or current inspection records.

### 8.7 AI Assessment Records

Purpose: audit AI outputs and user handling decisions.

List page:

- Filters: assessment type, business object ID, confidence label, degradation source, adoption status, created by, date range.
- Table columns:
  - Assessment type.
  - Business object.
  - Confidence.
  - Degradation source.
  - Model/provider.
  - Prompt version.
  - Adoption status.
  - Created time.

Detail page or drawer:

- Input snapshot.
- Raw output, read-only.
- Structured recommendation.
- Citations.
- Confidence factors.
- Adoption/ignore history.

Raw model output must be immutable in the UI.

### 8.8 Confidence Configuration

Purpose: controlled configuration for confidence weights and thresholds.

Layout:

- Current active config summary.
- Edit form with:
  - Rule weight.
  - RAG weight.
  - LLM weight.
  - High threshold.
  - Medium threshold.
  - Low threshold.
  - Enabled flag.
- Validation preview:
  - Weight sum.
  - Threshold order.
  - Resulting confidence bands.
- Audit warning alert before save.

Behavior:

- Unauthorized users see read-only state or no route access.
- Invalid config blocks submit.
- Save action must confirm because it affects high-risk recommendations.

## 9. Common Components And Patterns

### 9.1 AI Status Tags

Use a shared mapping for AI status labels.

| Concept | Value | Tag Type |
| --- | --- | --- |
| High confidence | `HIGH` | success |
| Medium confidence | `MEDIUM` | warning |
| Low confidence | `LOW` | danger |
| Generated | `GENERATED` | primary |
| Cache-backed | `CACHE` | info |
| Rule template | `RULE_TEMPLATE` | warning |
| Raw retrieval | `RAW_RETRIEVAL` | info |
| Unavailable | `UNAVAILABLE` | danger |

### 9.2 Judgment Tags

Use dictionary data when available. Fallback mapping:

| Judgment | Tag Type |
| --- | --- |
| `QUALIFIED` | success |
| `UNQUALIFIED` | danger |
| `NEED_REINSPECTION` | info |
| `CAN_CONCESSION` | warning |
| `STANDARD_CONFLICT` | danger |

Do not use legacy aliases such as `CONCESSION` or `REINSPECTION` in new code. Existing compatibility checks may remain only where needed for old data.

### 9.3 Search And Table Pattern

For management pages:

- Top `el-card.search-card` with inline `el-form`.
- Main `el-card` containing `el-table`.
- Pagination aligned right.
- Row actions fixed right.
- Use `show-overflow-tooltip` for long IDs, names, clauses, and reasons.
- Use `useTableFilter` for local column filters where current pages already use it.

### 9.4 Detail Page Pattern

For detail pages:

- Top summary panel.
- One or more dense sections.
- Source/citation tables near the generated explanation.
- Bottom action bar aligned right.
- Destructive or audit-sensitive actions require `ElMessageBox.confirm`.

### 9.5 Empty, Error, And Loading States

Every AI page must handle:

- Normal loading.
- Empty data.
- No source evidence.
- Low-confidence result.
- AI model unavailable.
- Vector retrieval unavailable.
- Permission denied.
- Business blocked by conflict.

Do not leave blank panels without `el-empty` or `el-alert`.

## 10. Data And API Contract Guidelines

### 10.1 API Wrapper Rules

- Keep API calls in `frontend/src/api/`.
- Use `get`, `post`, `put`, `del` from `src/utils/request.ts`.
- Do not call `axios` directly in views.
- Keep reusable response types in `frontend/src/types/` when used by more than one page.
- Use backend dictionary codes for labels and colors where available.

### 10.2 Recommended Frontend Types

```ts
export interface SourceCitation {
  clauseId: string
  documentId: string
  sourceType: 'NATIONAL' | 'ENTERPRISE' | 'CUSTOMER' | 'CASE' | 'COMPLAINT'
  standardCode?: string
  standardName?: string
  version?: string
  clauseNo?: string
  pageNo?: number
  paragraph: string
  score?: number
}

export interface AiConfidence {
  score: number
  label: 'HIGH' | 'MEDIUM' | 'LOW'
  factors: Array<{
    name: string
    score: number
    reason: string
  }>
}

export interface AiDegradation {
  source: 'GENERATED' | 'CACHE' | 'RULE_TEMPLATE' | 'RAW_RETRIEVAL' | 'UNAVAILABLE'
  reason?: string
}

export interface AiAssessmentSummary {
  id: string
  assessmentType: string
  businessType: string
  businessId: string
  confidence: AiConfidence
  degradation: AiDegradation
  adoptionStatus?: 'PENDING' | 'ADOPTED' | 'IGNORED'
  createdBy: string
  createdAt: string
}
```

### 10.3 Suggested API Modules

Create focused modules:

- `src/api/standard-rag.ts`
- `src/api/standard-document.ts`
- `src/api/standard-conflict.ts`
- `src/api/ai-assessment.ts`
- `src/api/ai-confidence.ts`
- Extend existing `judgment.ts`, `concession.ts`, `reinspection.ts`, `rejudgment.ts`, and `cert-data.ts`.

View components should not know provider details such as DeepSeek or Elasticsearch.

## 11. Security And Safety

- Treat model output, retrieved clauses, uploaded document text, and user prompts as untrusted text.
- Render AI and citation content as escaped text, not raw HTML.
- Do not expose API keys, ES credentials, prompts containing secrets, or backend stack traces.
- Display prompt-injection attempts as normal user input or refusal reasons, never as executable instructions.
- AI adoption/ignore actions must use authenticated API calls and audit logs.
- High-risk operations need explicit confirmation:
  - Conflict裁决.
  - Confidence config save.
  - Formal certificate generation after previous conflict resolution.
  - Concession assessment adoption.

## 12. Accessibility And Responsiveness

- Minimum supported operational viewport: 1366 x 768.
- Tables may scroll horizontally on small screens; do not compress critical status columns into unreadable text.
- Icon-only buttons require `title` or `el-tooltip`.
- Color must not be the only signal. Pair tags with text labels.
- Form validation must show text messages.
- Keyboard focus should remain visible through Element Plus focus states.
- Dialogs and drawers must have clear cancel/confirm actions.

## 13. Performance

- Use pagination for management lists.
- Debounce RAG and certificate Q&A submit only if implementing live suggestions. Normal submit buttons do not need debounce.
- Do not fetch AI assessment details for every list row. Load detail on demand.
- Do not render long source paragraphs in every table row by default. Use tooltip, drawer, or expandable row.
- Use backend filtering for large lists. Use local `useTableFilter` only for already-loaded page data.

## 14. Testing And Verification

Minimum frontend checks:

- `npm run build`
- Manual route verification for all new dynamic menu component paths.
- Manual verification of required demo chain:
  - Inspection entry to standard match.
  - AI judgment explanation with citations.
  - Concession risk assessment.
  - Certificate data generation.
  - Certificate Q&A with cited answer.
  - Standard conflict block and裁决 path.

State coverage:

- Generated AI answer.
- Cache-backed answer.
- Rule-template explanation.
- Raw retrieval fallback.
- Model/vector unavailable.
- No evidence refusal.
- Low-confidence refusal.
- Prompt-injection refusal.
- Unauthorized action.

Visual verification:

- No text overlap at 1366 x 768.
- Tables retain action column usability.
- Long standard names, clauses, and batch IDs use overflow behavior.
- Tags remain readable against dark background.

## 15. Rollout Plan

Recommended implementation order:

1. Add shared AI display types, tag helpers, and API modules.
2. Add menu/RBAC seed entries and route components with placeholder-safe pages.
3. Implement Standard RAG Retrieval page.
4. Enhance Judgment Explanation page with citations, confidence, degradation, and conflict state.
5. Implement Standard Conflict list/detail/c裁决 pages.
6. Enhance Concession pages with AI risk assessment.
7. Add reinspection/rejudgment suggestion prefill.
8. Enhance Certificate data page and add Certificate Q&A.
9. Add AI Assessment Records page.
10. Add Confidence Configuration page.
11. Run frontend build and manual demo verification.

## 16. Alternatives Considered

### 16.1 Chatbot-First UI

Rejected. It would hide deterministic judgment facts behind conversation and weaken auditability. This system needs evidence-first operational pages with optional question answering.

### 16.2 Separate AI Portal

Rejected for the first version. AI outputs must live next to the business object they explain: judgment, concession, conflict, or certificate.

### 16.3 New UI Library

Rejected. Element Plus is already integrated, themed, and used across pages. A new library would increase inconsistency and implementation risk.

### 16.4 Light Theme For AI Pages

Rejected. It would break the current AQC industrial control-room language and make AI features feel detached from the quality workflow.

## 17. Risks And Mitigations

| Risk | Impact | Mitigation |
| --- | --- | --- |
| AI output appears authoritative without evidence | Users over-trust suggestions | Always show citations, confidence, and degradation source |
| `STANDARD_CONFLICT` leaks into normal rejudgment options | Users bypass conflict裁决 | Filter conflict value from normal target-judgment select |
| Long source paragraphs break table layout | Poor usability | Use overflow tooltips, drawers, expandable rows |
| Missing backend services block UI development | Frontend stalls | Build with typed API wrappers and explicit empty/degraded states |
| Too many new menus reduce demo focus | Judges miss main story | Keep dashboard and demo chain prominent |
| Low-confidence behavior inconsistent across pages | Business risk | Use shared tag and restriction rules |

## 18. Open Questions

- Should certificate Q&A be a standalone menu item or a tab inside certificate detail?
- Should AI assessment records be visible to quality managers or admin/auditor roles only?
- Should confidence configuration be editable by `ADMIN` only, or by both `ADMIN` and quality manager roles?
- Should source paragraph highlighting be done by backend offsets or frontend keyword matching?
- Should Standard RAG Retrieval include complaint/case documents by default, or require explicit source-type selection?

## 19. Implementation Checklist

- New pages follow existing layout, dark theme, Element Plus components, and dynamic route conventions.
- All source and AI text is escaped.
- No generated answer appears without citations or explicit degraded/refusal state.
- AI suggestions never submit business workflows automatically.
- `STANDARD_CONFLICT` blocks formal certificate generation and normal release conclusion display.
- Low-confidence concession/rejudgment/certificate explanations follow feature-specific restrictions.
- Shared types and API wrappers are used instead of view-local ad hoc shapes when reused.
- Frontend build passes before handoff.
