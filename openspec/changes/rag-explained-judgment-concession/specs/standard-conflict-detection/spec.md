## ADDED Requirements

### Requirement: System detects priority-resolvable standard conflicts
The system SHALL detect differences between applicable customer, enterprise, and national standards for the same indicator and SHALL record them when priority can resolve the choice.

#### Scenario: Customer agreement stricter than national standard
- **WHEN** a customer agreement and national standard both apply to an indicator with different limits
- **THEN** the system SHALL apply the customer agreement, record the conflict as priority-resolvable, and mark the judgment explanation with both sources

### Requirement: System detects same-priority standard conflicts
The system SHALL detect same-priority conflicts when more than one standard of the same priority applies to the same product, indicator, and inspection date with incompatible rules.

#### Scenario: Overlapping customer agreements conflict
- **WHEN** two customer agreements for the same customer, product, indicator, and inspection date have different limits
- **THEN** the system SHALL create a conflict record and return `STANDARD_CONFLICT` instead of a release judgment

#### Scenario: Overlapping enterprise standards conflict
- **WHEN** two enterprise standards apply to the same product, indicator, and inspection date with incompatible limits
- **THEN** the system SHALL create a conflict record and return `STANDARD_CONFLICT`

### Requirement: STANDARD_CONFLICT is a distinct judgment result
The system SHALL support `STANDARD_CONFLICT` as a first-class judgment result distinct from `NEED_REINSPECTION`.

#### Scenario: Conflict judgment appears in list
- **WHEN** a judgment result is `STANDARD_CONFLICT`
- **THEN** list pages, detail pages, filters, dictionary labels, dashboard warnings, and statistics SHALL identify it as a standard conflict rather than a reinspection task

### Requirement: Standard conflict裁决 is human-controlled
The system SHALL require an authorized user to裁决 same-priority standard conflicts before final certificate generation or release judgment.

#### Scenario: Authorized裁决
- **WHEN** an authorized quality manager selects the controlling standard and enters裁决 rationale
- **THEN** the system SHALL store裁决 standard, rationale, operator, timestamp, and status on the conflict record

#### Scenario: Unauthorized裁决 attempt
- **WHEN** a user without conflict裁决 permission attempts to裁决 a conflict
- **THEN** the system SHALL reject the operation and write no裁决 result

### Requirement: Judgment is rerun after conflict裁决
The system SHALL rerun judgment after a same-priority conflict is裁决 and SHALL preserve the original conflict judgment for audit.

#### Scenario: Rejudge after裁决
- **WHEN** a conflict is裁决
- **THEN** the system SHALL mark the previous `STANDARD_CONFLICT` judgment as historical/non-final and create a new final judgment using the裁决 result

### Requirement: Formal certificate generation is blocked for unresolved conflict
The system SHALL block formal quality certificate generation when the current final judgment is `STANDARD_CONFLICT`.

#### Scenario: Certificate requested before裁决
- **WHEN** a user requests a formal certificate for a record whose final judgment is `STANDARD_CONFLICT`
- **THEN** the system SHALL reject formal generation and MAY provide only a non-final preview clearly marked as pending裁决
