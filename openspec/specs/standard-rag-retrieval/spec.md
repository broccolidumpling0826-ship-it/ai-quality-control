# standard-rag-retrieval

## Purpose

Standard document ingestion, clause-aware chunking, vector indexing, and source-grounded RAG retrieval with citation and safety controls.

## Requirements

### Requirement: Standard documents are managed as retrievable sources
The system SHALL store original national standard, enterprise standard, customer agreement, and case/complaint documents with metadata needed for retrieval and citation.

#### Scenario: Upload standard document metadata
- **WHEN** a quality engineer registers a standard document with standard code, name, type, version, effective date, expiry date, and source file
- **THEN** the system SHALL persist the document metadata and make it available for indexing

#### Scenario: Document metadata includes applicability
- **WHEN** a document is registered for a specific variety, grade, customer, or usage scope
- **THEN** the system SHALL persist the applicability metadata for later retrieval filtering

### Requirement: Uploaded PDF and Office documents are parsed into text
The system SHALL ingest source files such as PDF, Word, Excel, and Markdown/text documents by extracting text before indexing, while preserving source metadata for citation.

#### Scenario: Upload PDF standard document
- **WHEN** a quality engineer uploads a PDF standard document such as a national standard or customer agreement
- **THEN** the system SHALL extract text using a deterministic document parser such as Apache PDFBox for PDF or Apache POI for Office files
- **AND** the system SHALL persist parse status, source file metadata, and any parse error without deleting the original document record

#### Scenario: Extracted text preserves citation anchors
- **WHEN** text is extracted from a source document
- **THEN** the system SHALL preserve page number when available, document id, standard code, version, effective dates, customer/applicability metadata, and recognizable clause headings for later citation

#### Scenario: Tables are extracted conservatively
- **WHEN** a PDF or Office document contains tables
- **THEN** the system MAY extract table text into row-oriented text blocks
- **AND** it SHALL mark table-derived text as extracted evidence rather than structured judgment truth

### Requirement: Document chunking is deterministic and clause-aware
The system SHALL chunk extracted standard text into retrieval clauses by code rules using chapter, clause, paragraph, and natural boundary structure, not by asking the embedding model to decide chunks.

#### Scenario: Standard has clear clause numbers
- **WHEN** extracted text contains headings such as `7.3 力学性能` or `3.1 Customer Mechanical Requirement`
- **THEN** the system SHALL create chunks that keep the heading and its complete paragraph together
- **AND** each chunk SHALL include clause number, clause title when available, page number when available, and original paragraph text

#### Scenario: Paragraph is too long
- **WHEN** a clause paragraph exceeds the configured chunk size
- **THEN** the system SHALL split on natural paragraph or sentence boundaries and keep overlap only when needed to preserve context

#### Scenario: Optional lightweight NLP boundary detection
- **WHEN** paragraph boundaries are unclear
- **THEN** the system MAY use lightweight NLP tools such as jieba or spaCy for sentence segmentation before merging adjacent sentences into coherent chunks
- **AND** the system SHALL NOT require an additional semantic model for chunking in the default path

### Requirement: Standard clauses are indexed for RAG
The system SHALL split registered documents into clauses, generate embeddings for clause text, and index clause text, vector, and citation metadata through the vector-store gateway.

#### Scenario: Clause index contains citation fields
- **WHEN** a document is indexed
- **THEN** each indexed clause SHALL include document id, standard code, standard name, version, standard type, clause number, page number, original text, applicability metadata, and embedding vector

#### Scenario: Text is vectorized before storage
- **WHEN** a chunk is ready for indexing
- **THEN** the system SHALL call the configured embedding model with the chunk text and relevant metadata context
- **AND** it SHALL store the returned vector in the vector index with the chunk source fields

#### Scenario: Embedding failure is visible
- **WHEN** vector generation fails for one or more chunks
- **THEN** the system SHALL mark those chunks as embedding/index failed and SHALL NOT silently index them as successful vector chunks

#### Scenario: Indexing failure is visible
- **WHEN** indexing a document fails
- **THEN** the system SHALL persist an indexing failure status and error message without deleting the source document metadata

### Requirement: Natural language standard retrieval returns cited answers
The system SHALL answer natural-language standard queries using the pipeline `query text -> query embedding -> vector/keyword retrieval -> source-grounded answer generation` and SHALL include source clauses in every generated answer.

#### Scenario: Query with matching clauses
- **WHEN** a user asks a question that matches uploaded standard clauses
- **THEN** the system SHALL embed the query text, retrieve matching clauses from the vector index with applicable filters, and pass only the retrieved clauses plus system rules to the chat model
- **AND** it SHALL return an answer, matched clauses, standard names, versions, clause numbers, page numbers, retrieval scores, and retrieval mode

#### Scenario: Answer generation uses retrieved context
- **WHEN** retrieved clauses meet the answer threshold
- **THEN** the chat model prompt SHALL include the user question, retrieved source chunks, and grounding instructions
- **AND** the answer SHALL cite source clauses or be degraded/refused when citations cannot be verified

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

### Requirement: RAG retrieval excludes draft-only source documents
The system SHALL retrieve clauses only from active source documents linked to published structured standards unless an explicit administrative override is configured.

#### Scenario: Published standard maintenance upload becomes retrievable
- **WHEN** a standard is published and its linked PDF has been successfully indexed
- **THEN** RAG queries SHALL be able to retrieve clauses from that standard's indexed chunks with citation metadata

#### Scenario: Draft standard upload is not retrievable
- **WHEN** a standard remains in `DRAFT` status with an uploaded source file
- **THEN** RAG queries SHALL NOT return clauses from that draft-only source document

### Requirement: Excel table sources are extracted as row-oriented evidence
The system SHALL extract `xlsx` and `xls` standard source files into row-oriented text blocks suitable for clause-aware chunking.

#### Scenario: Excel upload produces row-oriented segments
- **WHEN** a published standard has an uploaded Excel source file
- **THEN** the system SHALL extract each sheet row as a text segment with sheet and row context
- **AND** extracted table text SHALL be marked as extracted evidence rather than structured judgment truth

### Requirement: Image sources are extracted through Vision OCR
The system SHALL extract text from scanned `png`, `jpg`, and `jpeg` standard source files through the configured Vision OCR model gateway.

#### Scenario: Image upload produces OCR text for indexing
- **WHEN** a published standard has an uploaded image source file and Vision OCR is enabled
- **THEN** the system SHALL call the configured Vision model (default `deepseek-ai/DeepSeek-OCR` on SiliconFlow) with the image content
- **AND** it SHALL index the returned text as reference-only extracted evidence with source metadata

#### Scenario: Vision OCR output is non-deterministic and reference-only
- **WHEN** text is extracted from an image source through Vision OCR
- **THEN** the system SHALL treat the result as reference-only citation evidence
- **AND** it SHALL NOT use OCR output to override structured indicator limits in `qc_standard_indicator`

### Requirement: Multiple source files for one standard are retrievable together
The system SHALL retrieve and cite clauses from all successfully indexed source files linked to a published structured standard.

#### Scenario: RAG retrieves clauses from PDF and Excel together
- **WHEN** a published standard has both an indexed PDF and an indexed Excel source file
- **AND** a user asks a question that matches content from either file
- **THEN** the system SHALL be able to retrieve clauses from both `documentId` values
- **AND** the answer or citation list SHALL identify the source file name where available

#### Scenario: Partially indexed standard still retrieves successful files
- **WHEN** a published standard has multiple source files and one file failed indexing while others succeeded
- **THEN** RAG queries SHALL retrieve clauses only from successfully indexed source files
- **AND** the system SHALL NOT invent content from the failed source file
