## ADDED Requirements

### Requirement: Prompt templates are stored in versioned classpath resources
The system SHALL store AI prompt templates for supported scenes in classpath resources under a dedicated prompts directory, with a manifest file mapping each prompt scene to an active template version.

#### Scenario: Application loads prompt manifest at startup
- **WHEN** the application starts with prompt registry enabled
- **THEN** it SHALL load `manifest.yaml` and all referenced template files for active versions
- **AND** it SHALL fail startup if a referenced template file is missing

#### Scenario: Multiple template versions coexist
- **WHEN** a prompt scene has more than one version file in the prompts directory
- **THEN** the manifest SHALL designate exactly one active version per scene for runtime resolution

### Requirement: Supported prompt scenes are centrally registered
The system SHALL register prompt templates for at least the following scenes: judgment explanation, standard RAG, certificate QA, concession risk wording, and vision OCR.

#### Scenario: Judgment explanation resolves a registered template
- **WHEN** the judgment explanation flow builds a model chat request
- **THEN** it SHALL resolve the active template for the judgment explanation scene from the prompt registry

#### Scenario: Vision OCR resolves a registered template
- **WHEN** the vision OCR extractor builds a model vision request
- **THEN** it SHALL resolve the active system prompt template for the vision OCR scene from the prompt registry

### Requirement: Prompt assembly separates static templates from runtime facts
The system SHALL assemble final model prompts by combining static template content with runtime business facts supplied by the calling service.

#### Scenario: Static system prompt comes from template
- **WHEN** a prompt builder assembles a chat request
- **THEN** the system prompt SHALL come from the resolved template and SHALL NOT be hardcoded in the business service

#### Scenario: Dynamic evidence is appended at runtime
- **WHEN** a prompt builder assembles a judgment explanation user message
- **THEN** it SHALL inject runtime judgment type, rule explanation, indicator evidences, and numbered citation blocks into the user message

#### Scenario: Conditional prompt snippets are selected in code
- **WHEN** certificate QA requires a non-final or concession-approved append message
- **THEN** the system SHALL evaluate business conditions in code and SHALL append the corresponding snippet text loaded from the template resources

### Requirement: Citation answer rule text is loaded from shared prompt resources
The system SHALL load citation answer rule text for judgment explanation, certificate QA, and standard RAG from shared prompt resource files rather than hardcoded switch cases.

#### Scenario: Citation rules are appended from registry
- **WHEN** a cited chat prompt is assembled for standard RAG
- **THEN** the system SHALL append the standard RAG citation rule text from the prompt registry shared resources

### Requirement: Grounded citation validation remains code-enforced
The system SHALL keep grounded citation validation logic in code and SHALL NOT move acceptance rules into editable prompt configuration.

#### Scenario: Prompt text change does not bypass grounded validation
- **WHEN** citation answer rule text is updated in prompt resources
- **THEN** the system SHALL still apply the existing grounded citation validation before accepting generated output as trusted

### Requirement: Prompt version audit remains consistent
The system SHALL continue recording `prompt_version` on AI assessments and model chat requests using the active template version resolved from the prompt registry.

#### Scenario: Assessment stores registry version
- **WHEN** a judgment explanation assessment is persisted
- **THEN** the stored `prompt_version` SHALL match the active template version resolved for the judgment explanation scene

### Requirement: Prompt registry migration preserves runtime behavior
The system SHALL preserve existing AI prompt behavior when migrating from hardcoded service prompts to registry-based assembly.

#### Scenario: Migrated prompt matches legacy output
- **WHEN** a prompt builder assembles a request using fixture business data equivalent to the pre-migration implementation
- **THEN** the rendered system prompt and user message SHALL match the legacy hardcoded output byte-for-byte

### Requirement: Prompt resource changes require developer review workflow
The system SHALL document how to add, version, and activate prompt templates through source-controlled resources and application release.

#### Scenario: Prompt README documents version workflow
- **WHEN** a developer needs to introduce a new prompt version
- **THEN** repository documentation under the prompts directory SHALL describe copying a version folder, updating the manifest active version, and running equivalence tests before release
