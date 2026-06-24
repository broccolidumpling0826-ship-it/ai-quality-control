# ai-judgment-explanation

## Purpose

Rule-first judgment explanations enriched with source citations, confidence disclosure, and conflict warnings, with persisted AI assessment records.

## Requirements

### Requirement: Judgment explanation includes structured rule details
The system SHALL explain judgment results using matched standards, indicator limits, inspection values, deviations, trigger rules, and final judgment type.

#### Scenario: Explain qualified judgment
- **WHEN** a user opens a qualified judgment detail
- **THEN** the system SHALL show each inspected indicator, test value, lower limit, upper limit, deviation, trigger rule, and pass result

#### Scenario: Explain concession judgment
- **WHEN** a judgment contains an indicator within concession range
- **THEN** the system SHALL show normal limits, concession limits, measured deviation, and the rule that produced `CAN_CONCESSION`

### Requirement: Judgment explanation includes source citations
The system SHALL attach source clauses from standard documents to AI judgment explanations when relevant clauses are available.

#### Scenario: Source clauses found
- **WHEN** RAG retrieval finds clauses for matched judgment indicators
- **THEN** the explanation SHALL include standard name, code, version, clause number, page number, and source paragraph for each cited basis

#### Scenario: Source clauses missing
- **WHEN** no source clause is found for a structured judgment rule
- **THEN** the explanation SHALL still show the structured rule details and SHALL mark the source citation as missing rather than inventing a citation

### Requirement: Judgment explanation displays confidence
The system SHALL calculate and display a high, medium, or low confidence label with contributing factors.

#### Scenario: High confidence explanation
- **WHEN** structured rules are complete, relevant clauses are found, and no unresolved conflict exists
- **THEN** the explanation SHALL display high confidence and state that standards and evidence are sufficient

#### Scenario: Low confidence explanation
- **WHEN** source clauses are missing, structured-document inconsistency exists, or unresolved conflict exists
- **THEN** the explanation SHALL display low confidence and provide an action path for manual review

### Requirement: Judgment explanation marks conflicts
The system SHALL surface standard conflicts and structured-document inconsistencies in the judgment explanation.

#### Scenario: Priority-resolvable conflict
- **WHEN** customer agreement and national standard differ but priority resolves the rule choice
- **THEN** the explanation SHALL mark the conflict, show both sources, and state why the selected standard was used

#### Scenario: Same-priority conflict
- **WHEN** the judgment result is `STANDARD_CONFLICT`
- **THEN** the explanation SHALL show conflict details and SHALL NOT present a final pass/fail release conclusion

### Requirement: AI explanation output is persisted
The system SHALL persist AI judgment explanation output, citations, confidence, model metadata, and input snapshot when generated or served from cache.

#### Scenario: Explanation generated
- **WHEN** an AI judgment explanation is generated for a judgment
- **THEN** the system SHALL store an immutable assessment record linked to the judgment id
