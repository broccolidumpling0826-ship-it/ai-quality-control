## ADDED Requirements

### Requirement: Standard maintenance supports PDF source file upload
The system SHALL allow quality engineers to upload one PDF source file per structured quality standard from the standard maintenance page.

#### Scenario: Upload PDF while editing a standard
- **WHEN** a user with `standard:manage` permission uploads a `.pdf` file for an existing standard record
- **THEN** the system SHALL persist the file under the configured standard-document storage path
- **AND** it SHALL create or update the linked `qc_standard_document` record with `standard_id`, source file metadata, and parse/index status

#### Scenario: Draft standard stores file without vector indexing
- **WHEN** a standard is in `DRAFT` status and a PDF is uploaded or replaced
- **THEN** the system SHALL store the file and metadata only
- **AND** it SHALL NOT write new vectors to Elasticsearch until the standard is published

#### Scenario: Unsupported file type is rejected
- **WHEN** a user uploads a non-PDF file as the standard source
- **THEN** the system SHALL reject the upload with a validation error

### Requirement: Standard source files are stored locally under backend resources
The system SHALL store uploaded standard PDF files in a dedicated runtime directory under the backend project, not in `src/main/resources`.

#### Scenario: File path is persisted for later parsing
- **WHEN** a PDF is saved successfully
- **THEN** the system SHALL persist `source_file_name`, `source_file_path`, and `source_file_hash` on the linked standard document record
- **AND** the stored path SHALL resolve to a readable local file for deterministic parsing

### Requirement: Published standards trigger PDF ingestion into RAG
The system SHALL parse, chunk, embed, and index the linked PDF when a standard is published.

#### Scenario: Publish triggers ingestion
- **WHEN** a draft standard with an uploaded PDF is published
- **THEN** the system SHALL extract text with Apache PDFBox
- **AND** it SHALL chunk text with deterministic clause-aware rules
- **AND** it SHALL generate embeddings and index clauses into Elasticsearch
- **AND** it SHALL update parse/index status and error messages on the document record

#### Scenario: Publish without source file remains valid
- **WHEN** a draft standard is published without an uploaded PDF
- **THEN** the system SHALL publish the structured standard successfully
- **AND** it SHALL expose that no source document is available for RAG indexing

#### Scenario: Index failure does not rollback publish
- **WHEN** publish succeeds but vector indexing fails
- **THEN** the standard SHALL remain `PUBLISHED`
- **AND** the document record SHALL show index failure details
- **AND** the user SHALL be able to retry indexing without republishing the standard

### Requirement: Source files can be downloaded and replaced
The system SHALL allow users to download the current PDF and replace it from the standard maintenance page.

#### Scenario: Download current PDF
- **WHEN** a standard has an uploaded source file
- **THEN** the user SHALL be able to download the current PDF from the standard detail/maintenance UI

#### Scenario: Replace PDF on published standard
- **WHEN** a user replaces the PDF of a published standard
- **THEN** the system SHALL delete prior clause records and Elasticsearch vectors for the linked document
- **AND** it SHALL store the new PDF
- **AND** it SHALL immediately re-run parse/chunk/embed/index for the new file

#### Scenario: Replace PDF on draft standard
- **WHEN** a user replaces the PDF of a draft standard
- **THEN** the system SHALL replace the stored file and reset parse/index status to pending
- **AND** it SHALL NOT write vectors until publish

### Requirement: Standard deletion cleans linked source and vector data
The system SHALL remove linked source files, document metadata, clause records, and Elasticsearch vectors when a structured standard is deleted.

#### Scenario: Delete standard with uploaded PDF
- **WHEN** a standard with linked source document and indexed clauses is deleted
- **THEN** the system SHALL delete the stored PDF file when present
- **AND** it SHALL delete linked clause records and Elasticsearch vectors

### Requirement: Structured rules remain judgment truth
Extracted PDF text and indexed clauses SHALL be used only for retrieval, citation, and explanation.

#### Scenario: PDF upload does not mutate structured indicators
- **WHEN** a PDF is uploaded, replaced, parsed, or indexed for a standard
- **THEN** the system SHALL NOT modify `qc_standard_indicator` limits based on extracted PDF text
