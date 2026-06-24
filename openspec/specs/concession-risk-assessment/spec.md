# concession-risk-assessment

## Purpose

Deterministic concession risk assessment augmented by cited evidence and AI wording, with low-confidence refusal and immutable persisted assessments.

## Requirements

### Requirement: Concession risk assessment uses required business dimensions
The system SHALL assess concession risk using customer usage, deviation degree, standard concession clauses, historical complaints/cases, alternative resources, and current judgment evidence.

#### Scenario: All dimensions available
- **WHEN** a user requests concession risk assessment for a `CAN_CONCESSION` judgment and all dimensions are available
- **THEN** the system SHALL return risk level, reasoning by dimension, cited sources/cases, suggested conditions, and confidence

#### Scenario: Customer usage missing
- **WHEN** customer usage cannot be obtained from agreement or customer profile
- **THEN** the system SHALL require manual usage input before generating a normal concession risk assessment

### Requirement: Concession risk output is structured
The system SHALL output structured risk fields before natural-language explanation.

#### Scenario: Risk assessment completed
- **WHEN** concession risk assessment completes
- **THEN** the response SHALL include `riskLevel`, `mustReview`, `missingInfo`, `suggestedConditions`, `blockingReasons`, `evidenceRefs`, confidence label, and narrative explanation

### Requirement: Concession risk baseline rules are deterministic
The system SHALL apply deterministic baseline rules before AI wording is generated.

#### Scenario: Blocking conflict
- **WHEN** the judgment or matched standard basis has unresolved `STANDARD_CONFLICT`
- **THEN** the concession assessment SHALL return `riskLevel=BLOCKED`, `mustReview=true`, and include a blocking reason

#### Scenario: High-risk usage with mechanical deviation
- **WHEN** customer usage is safety-critical or high-forming and the abnormal indicator is strength or elongation
- **THEN** the concession assessment SHALL classify risk as at least HIGH

#### Scenario: Similar complaint found
- **WHEN** a historical complaint/case above the similarity threshold is found for the same customer, usage, indicator, or defect mode
- **THEN** the concession assessment SHALL raise risk by at least one level and cite the complaint/case

#### Scenario: Replacement stock available
- **WHEN** compatible qualified alternative stock is available within the configured delivery window
- **THEN** the concession assessment SHALL include replacement as a suggested condition or alternative and SHALL NOT rely only on concession approval

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

### Requirement: Concession risk assessment shows centered loading feedback
The frontend SHALL show centered, scenario-specific loading feedback while concession risk assessment API calls are pending, as defined by `ai-interaction-ux`.

#### Scenario: Risk assessment loading
- **WHEN** a user clicks assess on the concession detail page
- **THEN** the UI SHALL show a centered loading overlay with concession-risk loading copy until the assessment response returns or fails
