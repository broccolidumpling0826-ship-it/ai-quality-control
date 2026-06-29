# quality-cert-ai-pdf

## Purpose

Quality certificate data snapshots and PDF export with judgment-state gates, controlled AI explanation, and audit logging.

## Requirements

### Requirement: Quality certificate data snapshot includes controlled explanation and citations
The system SHALL generate certificate data snapshots by coil or batch including key indicators, judgment result, controlled AI/rule explanation, and cited sources when available.

#### Scenario: Generate certificate snapshot for qualified record
- **WHEN** a user generates certificate data for a qualified coil or batch
- **THEN** the system SHALL persist key inspection indicators, judgment result, explanation text, citation metadata, generator, and generation time

#### Scenario: Low-confidence explanation unavailable
- **WHEN** AI explanation confidence is low or AI services are unavailable
- **THEN** the certificate snapshot SHALL fall back to base structured data and SHALL record the reason AI explanation was omitted or degraded

### Requirement: Quality certificate PDF can be downloaded
The system SHALL provide PDF export for generated certificate data snapshots using Apache PDFBox and embedded or loadable Chinese fonts.

#### Scenario: Download PDF
- **WHEN** a user downloads a certificate PDF for a releasable snapshot
- **THEN** the PDF SHALL be A4 portrait with Chinese layout including:
  - centered title `质量证明书`
  - basic information table: certificate id, coil no, batch no, heat no, variety, grade, final judgment, customer id (when available), generation time, generator
  - indicator table columns: sequence, indicator name, indicator code, test value, unit, lower limit, upper limit, result
  - footer note stating the document is system-generated for internal traceability

#### Scenario: Chinese labels and encoding
- **WHEN** the PDF is rendered
- **THEN** judgment types and indicator results SHALL be displayed in Chinese (for example `QUALIFIED` → `合格`, `PASS` → `合格`)
- **AND** Chinese characters in variety, grade, and indicator names SHALL render correctly without `?` replacement or mojibake

#### Scenario: Download filename
- **WHEN** the frontend triggers a PDF download
- **THEN** the downloaded filename SHOULD be `质保书-{coilNo}.pdf`, falling back to the certificate data id when coil number is absent

### Requirement: Formal certificate is blocked for unresolved standard conflict
The system SHALL prevent formal quality certificate generation when the final judgment is `STANDARD_CONFLICT`.

#### Scenario: Conflict certificate request
- **WHEN** a user requests a certificate for an unresolved `STANDARD_CONFLICT` judgment
- **THEN** the system SHALL reject formal generation and explain that standard裁决 is required first

#### Scenario: Conflict preview request
- **WHEN** a user requests a preview for an unresolved `STANDARD_CONFLICT` judgment
- **THEN** the system MAY generate preview data only if it is clearly marked non-final and not a formal certificate

### Requirement: Formal certificate generation follows judgment state gates
The system SHALL gate formal certificate generation by final judgment state and approval state.

#### Scenario: Unqualified record certificate request
- **WHEN** the current final judgment is `UNQUALIFIED`
- **THEN** the system SHALL reject formal certificate generation and allow only internal data viewing

#### Scenario: Reinspection pending certificate request
- **WHEN** the current final judgment is `NEED_REINSPECTION`
- **THEN** the system SHALL reject formal certificate generation until reinspection is completed and a releasable final judgment exists

#### Scenario: Concession pending certificate request
- **WHEN** the current final judgment is `CAN_CONCESSION` but concession approval or required customer confirmation is incomplete
- **THEN** the system SHALL reject formal certificate generation and list pending concession requirements

#### Scenario: Standard gap certificate request
- **WHEN** a required indicator has no applicable structured standard or required certificate indicator is missing
- **THEN** the system SHALL reject formal certificate generation and identify the missing standard or data item

#### Scenario: Low-confidence explanation with releasable judgment
- **WHEN** structured judgment is releasable but AI explanation confidence is low
- **THEN** the system SHALL allow formal certificate data generation without authoritative AI wording and SHALL record the omitted explanation reason

#### Scenario: PDF export blocked for incomplete snapshot
- **WHEN** a user requests PDF export for a snapshot with `FAIL` or `WARNING` indicators, empty indicators, or non-`SUCCESS` status
- **THEN** the system SHALL reject PDF export with a clear business error

### Requirement: Certificate generation is auditable
The system SHALL record certificate generation and PDF download operations for audit-sensitive contexts.

#### Scenario: Certificate generated
- **WHEN** a user generates certificate data or downloads a PDF
- **THEN** the system SHALL record operator, target coil/batch, operation type, and timestamp

#### Scenario: PDF download audited
- **WHEN** a user downloads a certificate PDF
- **THEN** the system SHALL write an audit log entry with operation type `EXPORT_CERT_PDF`

### Requirement: PDF generation is implemented in a dedicated builder
The system SHALL centralize PDF layout in `QualityCertPdfBuilder` and MUST NOT use hand-written ASCII-only PDF streams.

#### Scenario: Builder uses PDFBox and Chinese font
- **WHEN** PDF bytes are generated
- **THEN** the implementation SHALL use Apache PDFBox
- **AND** SHALL load Chinese fonts from `backend/src/main/resources/fonts/` or approved system font fallbacks
