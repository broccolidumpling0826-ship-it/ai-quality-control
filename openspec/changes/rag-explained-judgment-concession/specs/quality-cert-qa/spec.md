## ADDED Requirements

### Requirement: Quality certificate Q&A answers by coil or batch
The system SHALL support natural-language Q&A for a coil or batch using certificate snapshot data, inspection records, judgment results, judgment evidence, concession status, conflict status, and standard clauses.

#### Scenario: Answer why a coil can be certified
- **WHEN** a user asks why a qualified coil can generate a certificate
- **THEN** the system SHALL answer using the current final judgment, key indicator evidence, certificate snapshot when available, and cited standard clauses

#### Scenario: Answer indicator basis question
- **WHEN** a user asks which basis supports a specific certificate indicator
- **THEN** the system SHALL return the inspection value, structured limit, deviation, trigger rule, and cited standard clause for that indicator

### Requirement: Certificate Q&A refuses unsupported answers
The system SHALL refuse or downgrade certificate Q&A answers when required certificate, judgment, or source evidence is missing.

#### Scenario: Missing certificate snapshot
- **WHEN** no certificate snapshot exists for the requested coil or batch
- **THEN** the system SHALL answer from inspection and judgment data if available and SHALL state that no certificate snapshot has been generated

#### Scenario: No source clause for answer
- **WHEN** a question asks for a standard basis but no source clause is available
- **THEN** the system SHALL show structured judgment evidence and SHALL state that no source paragraph is available rather than inventing one

### Requirement: Certificate Q&A respects non-final states
The system SHALL clearly mark answers for non-releasable states as internal or non-final.

#### Scenario: Standard conflict question
- **WHEN** a coil or batch final judgment is `STANDARD_CONFLICT`
- **THEN** certificate Q&A SHALL state that formal certificate conclusions are unavailable until conflict裁决 completes

#### Scenario: Concession pending question
- **WHEN** a coil or batch is `CAN_CONCESSION` but required concession approval or customer confirmation is not complete
- **THEN** certificate Q&A SHALL state that the certificate cannot be formalized yet and list the pending approvals or confirmations

### Requirement: Certificate Q&A uses AI cache transparently
The system SHALL support cached Q&A answers for demo scenarios while retaining evidence references and cache markers.

#### Scenario: Cached answer used
- **WHEN** the model service is unavailable and a matching certificate Q&A cache entry exists
- **THEN** the system SHALL return the cached answer with citations and mark the response as cache-backed
