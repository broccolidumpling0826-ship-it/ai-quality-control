## MODIFIED Requirements

### Requirement: Initial menu seed from existing routes
The system SHALL provide a seed script that imports all existing routes from the current router configuration as initial sys_menu records with appropriate role assignments matching current access patterns, and it SHALL include new AI quality routes introduced by this change.

#### Scenario: Seed script populates all business pages
- **WHEN** the seed script is executed on a fresh database
- **THEN** all 19+ existing business routes SHALL be present in sys_menu including hidden form/detail pages as HIDDEN type

#### Scenario: Seed script populates AI quality pages
- **WHEN** the seed script is executed on a fresh database after this change
- **THEN** P0 AI quality pages SHALL be present in sys_menu, including standard RAG retrieval, standard conflict detection, quality certificate Q&A, and hidden conflict裁决/detail routes

#### Scenario: Seed script assigns AI permissions
- **WHEN** the seed script populates AI quality pages
- **THEN** it SHALL assign menu and action permissions for AI retrieval, conflict裁决, certificate Q&A, confidence configuration, and AI assessment review according to the target roles
