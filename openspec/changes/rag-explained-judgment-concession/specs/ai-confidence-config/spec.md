## ADDED Requirements

### Requirement: Confidence calculation uses configurable weights
The system SHALL calculate AI confidence using rule score, RAG score, and LLM score with configurable weights.

#### Scenario: Default confidence config exists
- **WHEN** the system is initialized
- **THEN** it SHALL provide default weights of rule 0.60, RAG 0.30, and LLM 0.10 with default high/medium/low thresholds

#### Scenario: Confidence score calculated
- **WHEN** an AI assessment is generated
- **THEN** the system SHALL calculate confidence using the active weights and store score, label, and factor details

### Requirement: Confidence config can be managed by authorized users
The system SHALL provide a backend management page/API for authorized users to edit confidence weights and thresholds.

#### Scenario: Authorized update
- **WHEN** an authorized admin or quality manager submits valid confidence configuration
- **THEN** the system SHALL save the new active configuration and record an audit log

#### Scenario: Unauthorized update
- **WHEN** an unauthorized user attempts to update confidence configuration
- **THEN** the system SHALL reject the request

### Requirement: Confidence config validates weights and thresholds
The system SHALL reject invalid confidence configuration.

#### Scenario: Weights do not sum to one
- **WHEN** submitted rule, RAG, and LLM weights do not sum to 1
- **THEN** the system SHALL reject the configuration with a validation error

#### Scenario: Threshold order invalid
- **WHEN** submitted thresholds do not satisfy highThreshold > mediumThreshold > lowThreshold
- **THEN** the system SHALL reject the configuration with a validation error

### Requirement: Confidence labels drive product behavior
The system SHALL use confidence labels to control warnings, fallback, and manual-review requirements.

#### Scenario: Low confidence in high-risk feature
- **WHEN** concession, rejudgment, or certificate explanation confidence is low
- **THEN** the system SHALL apply the feature-specific low-confidence restrictions defined by that capability
