## ADDED Requirements

### Requirement: Standard documents are managed as retrievable sources
The system SHALL store original national standard, enterprise standard, customer agreement, and case/complaint documents with metadata needed for retrieval and citation.

#### Scenario: Upload standard document metadata
- **WHEN** a quality engineer registers a standard document with standard code, name, type, version, effective date, expiry date, and source file
- **THEN** the system SHALL persist the document metadata and make it available for indexing

#### Scenario: Document metadata includes applicability
- **WHEN** a document is registered for a specific variety, grade, customer, or usage scope
- **THEN** the system SHALL persist the applicability metadata for later retrieval filtering

### Requirement: Standard clauses are indexed for RAG
The system SHALL split registered documents into clauses and index clause text plus citation metadata through the vector-store gateway.

#### Scenario: Clause index contains citation fields
- **WHEN** a document is indexed
- **THEN** each indexed clause SHALL include document id, standard code, standard name, version, standard type, clause number, page number, original text, and applicability metadata

#### Scenario: Indexing failure is visible
- **WHEN** indexing a document fails
- **THEN** the system SHALL persist an indexing failure status and error message without deleting the source document metadata

### Requirement: Natural language standard retrieval returns cited answers
The system SHALL answer natural-language standard queries using retrieved standard/agreement/case clauses and SHALL include source clauses in every generated answer.

#### Scenario: Query with matching clauses
- **WHEN** a user asks a question that matches uploaded standard clauses
- **THEN** the system SHALL return an answer, matched clauses, standard names, versions, clause numbers, page numbers, and retrieval scores

#### Scenario: Query without matching clauses
- **WHEN** no retrieved clause meets the configured relevance threshold
- **THEN** the system SHALL refuse to answer from model memory and state that no related standard basis was found

### Requirement: RAG retrieval supports low-confidence disclosure
The system SHALL identify low-relevance retrieval results and mark them as reference-only rather than authoritative.

#### Scenario: Low similarity results
- **WHEN** retrieved clauses are below the normal answer threshold but above the display threshold
- **THEN** the system SHALL return the clauses with a low-confidence warning and SHALL NOT present them as a definitive answer

### Requirement: RAG answers are protected against prompt injection
The system SHALL treat user questions and retrieved documents as untrusted input and SHALL enforce the rule that answers must be grounded in retrieved clauses.

#### Scenario: User asks to ignore standards
- **WHEN** a user query instructs the model to ignore uploaded standards or invent a limit
- **THEN** the system SHALL ignore the injection instruction and answer only from retrieved source clauses or refuse when no basis exists
