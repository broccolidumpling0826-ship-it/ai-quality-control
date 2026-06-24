# ai-gateway-degradation

## Purpose

Abstract model and vector store access with layered degradation so AI features enhance quality workflows without breaking core rule-based judgment.

## Requirements

### Requirement: Model access is abstracted behind a gateway
The system SHALL route external model calls through a model gateway abstraction rather than calling a specific provider directly from business services.

#### Scenario: Business service calls model gateway
- **WHEN** a business service needs a generated explanation or recommendation
- **THEN** it SHALL call the model gateway interface and SHALL NOT depend on provider-specific API details

#### Scenario: Model provider configuration is environment-backed
- **WHEN** the application starts in dev profile
- **THEN** model endpoint, model name, API key, timeout, and enablement settings SHALL be loaded from configuration with environment variable placeholders

### Requirement: Vector access is abstracted behind a gateway
The system SHALL route vector search and indexing through a vector-store gateway abstraction rather than coupling business services to Elasticsearch APIs.

#### Scenario: RAG service searches clauses
- **WHEN** the RAG service searches for standard clauses
- **THEN** it SHALL call the vector-store gateway and receive provider-neutral retrieval results

#### Scenario: Elasticsearch configuration is environment-backed
- **WHEN** the application starts with vector retrieval enabled
- **THEN** Elasticsearch host, credentials, index names, and timeout settings SHALL be loaded from configuration with environment variable placeholders

### Requirement: AI features degrade without breaking core judgment
The system SHALL preserve automatic rule judgment and structured data views when model or vector services are unavailable.

#### Scenario: Model service timeout with cache hit
- **WHEN** the model gateway times out and a pre-generated cached output exists for the business scenario
- **THEN** the system SHALL return the cached output and mark it as cache-backed

#### Scenario: Model service unavailable without cache
- **WHEN** the model gateway is unavailable and no cached output exists
- **THEN** the system SHALL generate a rule-template explanation when structured judgment evidence is available

#### Scenario: Model unavailable but vector search works
- **WHEN** a RAG query cannot call the model but vector search succeeds
- **THEN** the system SHALL return raw matched clauses with citation metadata and an AI-unavailable notice

#### Scenario: Model and vector services unavailable
- **WHEN** both model and vector services are unavailable
- **THEN** the system SHALL show a clear AI knowledge service unavailable state while keeping rule judgment results accessible

### Requirement: Gateway failures are observable
The system SHALL log AI and vector gateway failures with enough context for diagnosis without leaking secrets.

#### Scenario: External API error occurs
- **WHEN** a model or vector provider returns an error
- **THEN** the system SHALL log provider type, operation, trace id, timeout/error category, and business object id when available, without logging API keys or passwords
