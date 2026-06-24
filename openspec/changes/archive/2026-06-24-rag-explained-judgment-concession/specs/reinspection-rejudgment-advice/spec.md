## ADDED Requirements

### Requirement: AI suggests reinspection without auto-creating workflow records
The system SHALL provide reinspection suggestions based on abnormal indicators, sample type, deviation, history, and confidence, but SHALL NOT automatically create reinspection records.

#### Scenario: Reinspection suggestion accepted
- **WHEN** a user accepts a reinspection suggestion
- **THEN** the system SHALL navigate to the reinspection application flow and prefill the suggested reason while requiring user submission

#### Scenario: Reinspection suggestion ignored
- **WHEN** a user ignores a reinspection suggestion
- **THEN** the system SHALL record the ignore action for audit or assessment tracking

### Requirement: AI suggests rejudgment without auto-creating workflow records
The system SHALL provide rejudgment suggestions when new evidence, complaint/case context, or post-process defect information indicates possible judgment change, but SHALL NOT automatically create rejudgment requests.

#### Scenario: Rejudgment suggestion accepted
- **WHEN** a user accepts a rejudgment suggestion
- **THEN** the system SHALL navigate to the rejudgment application flow and prefill suggested target judgment, reason, evidence summary, and affected scope where available

### Requirement: Low-confidence advice is withheld
The system SHALL withhold definitive reinspection or rejudgment advice when confidence is low.

#### Scenario: Insufficient advice evidence
- **WHEN** historical evidence, source clauses, or triggering facts are insufficient for a reliable suggestion
- **THEN** the system SHALL display that AI cannot provide reliable advice and SHALL leave workflow initiation to manual decision

### Requirement: Advice assessments are persisted
The system SHALL persist reinspection and rejudgment AI advice outputs, references, confidence, input snapshots, and user adoption status.

#### Scenario: Advice generated
- **WHEN** AI advice is generated for a judgment or record
- **THEN** the system SHALL store an AI assessment record linked to the source judgment or inspection record
