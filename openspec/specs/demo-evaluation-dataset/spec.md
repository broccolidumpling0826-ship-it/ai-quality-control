# demo-evaluation-dataset

## Purpose

Repeatable demo seed data and evaluation cases for AI quality features, with documented scripts and machine-checkable expected outcomes.

## Requirements

### Requirement: Demo dataset is repeatably initialized
The system SHALL provide deterministic seed scripts or seed files for competition demo data.

#### Scenario: Fresh database demo seed
- **WHEN** the demo seed is executed on a fresh database
- **THEN** the system SHALL create required standards, agreements, documents, clauses, inspections, judgments, conflicts, complaints/cases, alternative stock, AI cache, and evaluation cases with deterministic identifiers

#### Scenario: Demo seed is rerun
- **WHEN** the demo seed is executed multiple times
- **THEN** it SHALL be idempotent or documented as reset-safe and SHALL NOT create duplicate business records

### Requirement: Dataset includes required standard and inspection volume
The dataset SHALL include at least 20 standard/agreement documents, 100 inspection records, and 5 conflict samples.

#### Scenario: Dataset volume check
- **WHEN** the initialized demo dataset is inspected
- **THEN** it SHALL contain at least 20 documents, 100 inspection records, and 5 conflict standard samples

### Requirement: Dataset covers required demonstration scenarios
The dataset SHALL include scenarios for qualified, unqualified, concession-eligible, and standard-conflict demonstrations.

#### Scenario: Main demo path available
- **WHEN** the demo scenario list is loaded
- **THEN** it SHALL identify records for inspection entry, standard matching, AI explanation, concession assessment, certificate generation, and separate standard conflict demonstration

### Requirement: Evaluation set covers normal, boundary, low-confidence, and safety cases
The dataset SHALL include at least 30 evaluation cases distributed across normal, boundary/abnormal, low-confidence/refusal, and prompt-injection/safety scenarios.

#### Scenario: Evaluation distribution
- **WHEN** evaluation cases are queried
- **THEN** the system SHALL provide at least 10 normal cases, 10 boundary or abnormal cases, 5 low-confidence/refusal cases, and 5 prompt-injection or safety cases

### Requirement: Evaluation cases include expected outcomes
Each evaluation case SHALL include machine-checkable expected outputs.

#### Scenario: Evaluation case inspected
- **WHEN** an evaluation case is loaded
- **THEN** it SHALL include expected judgment, expected citation identifiers or citation absence, expected refusal behavior, expected confidence band, allowed numeric tolerance, and response time target

### Requirement: Evaluation report is generated
The system SHALL produce an evaluation report from the evaluation set.

#### Scenario: Evaluation report run
- **WHEN** the evaluation runner is executed
- **THEN** it SHALL report business rule pass rate, AI conclusion accuracy, citation hit rate, refusal accuracy, confidence band accuracy, average response time, and manual-review hit rate

### Requirement: Fixed demo scripts are documented
The dataset SHALL include fixed scripts for required live demo scenarios.

#### Scenario: Demo script inspected
- **WHEN** a required demo script is opened
- **THEN** it SHALL include input values, matched standards, expected judgment, cited clauses, AI output summary, degradation output, certificate or Q&A result, and operator steps

### Requirement: Demo AI cache supports offline presentation
The dataset SHALL include pre-generated AI cache entries for key demonstration records.

#### Scenario: AI service unavailable during demo
- **WHEN** the model service is unavailable for a seeded demo scenario
- **THEN** the system SHALL be able to return the pre-generated cache entry for that scenario
