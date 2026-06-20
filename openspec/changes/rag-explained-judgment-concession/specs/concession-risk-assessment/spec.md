## ADDED Requirements

### Requirement: Concession risk assessment uses required business dimensions
The system SHALL assess concession risk using customer usage, deviation degree, standard concession clauses, historical complaints/cases, alternative resources, and current judgment evidence.

#### Scenario: All dimensions available
- **WHEN** a user requests concession risk assessment for a `CAN_CONCESSION` judgment and all dimensions are available
- **THEN** the system SHALL return risk level, reasoning by dimension, cited sources/cases, suggested conditions, and confidence

#### Scenario: Customer usage missing
- **WHEN** customer usage cannot be obtained from agreement or customer profile
- **THEN** the system SHALL require manual usage input before generating a normal concession risk assessment

### Requirement: Historical complaints and cases are retrievable evidence
The system SHALL include simulated complaint/case documents in RAG retrieval for concession risk assessment.

#### Scenario: Similar complaint found
- **WHEN** a concession assessment matches a historical complaint or case
- **THEN** the system SHALL cite the complaint/case id, summary, finding, result, and relevance score in the assessment

### Requirement: Alternative stock affects concession recommendation
The system SHALL query available alternative resources and include them as a factor in concession risk assessment.

#### Scenario: Alternative qualified stock exists
- **WHEN** same or compatible qualified stock is available within the configured time window
- **THEN** the assessment SHALL consider replacement as a lower-risk alternative to concession

#### Scenario: Alternative stock unavailable
- **WHEN** no alternative stock data is available
- **THEN** the assessment SHALL mark that dimension as missing and reduce confidence

### Requirement: Low-confidence concession assessment refuses definitive advice
The system SHALL NOT provide a definitive concession recommendation when confidence is low.

#### Scenario: Low confidence assessment
- **WHEN** unresolved conflict, missing concession clauses, missing usage, or insufficient risk evidence causes low confidence
- **THEN** the system SHALL state that information is insufficient and SHALL require manual quality review

### Requirement: Concession risk assessments are persisted and immutable
The system SHALL persist concession risk assessment inputs, sources, model metadata, output, confidence, and adoption state.

#### Scenario: Assessment generated
- **WHEN** concession risk assessment completes
- **THEN** the system SHALL store an immutable AI assessment record linked to the judgment and concession workflow when available

#### Scenario: User handles assessment
- **WHEN** a user adopts or ignores a concession assessment
- **THEN** the system SHALL store the adoption status and human opinion without modifying the original AI output
