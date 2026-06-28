## ADDED Requirements

### Requirement: Prompt policy is loaded through a prompt registry abstraction
The system SHALL load model prompt policy (system prompts, user skeletons, citation rule text, and per-scene model parameters) through a prompt registry abstraction rather than hardcoding prompt strings in business services.

#### Scenario: Business service delegates prompt assembly
- **WHEN** a business service needs to call the model gateway for a supported AI scene
- **THEN** it SHALL delegate prompt assembly to a prompt builder that resolves templates from the prompt registry
- **AND** it SHALL NOT embed scene-specific system prompts or user skeleton strings directly in the business service

#### Scenario: Gateway request carries registry prompt version
- **WHEN** a model chat request is built through the prompt registry
- **THEN** the request SHALL include the active template version as `promptVersion`
