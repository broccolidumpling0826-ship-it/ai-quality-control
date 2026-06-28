# standard-source-file-management

## Purpose

Multi-format source file upload, storage, ingestion, and lifecycle management for structured standards without mutating judgment truth.

## Requirements

### Requirement: Standard maintenance supports multiple source files per standard
The system SHALL allow quality engineers to upload and maintain **multiple** source files for one structured quality standard from the standard maintenance page.

Each uploaded file SHALL be stored as its own linked `qc_standard_document` row with independent parse/index status and vector clauses.

Supported formats SHALL be `pdf`, `xlsx`, `xls`, `png`, `jpg`, and `jpeg`.

The system SHALL allow multiple files of the same format for one standard (for example two PDF attachments).

The system SHALL enforce a configurable maximum number of source files per standard (default 10).

#### Scenario: Add a new source file without removing existing files
- **WHEN** a user with `standard:manage` permission uploads a supported source file for a standard that already has other source files
- **THEN** the system SHALL persist the new file under the configured standard-document storage path
- **AND** it SHALL create a new linked `qc_standard_document` record with `standard_id`, source file metadata, and parse/index status
- **AND** it SHALL NOT delete or replace previously uploaded source files

#### Scenario: List all source files for a standard
- **WHEN** a user opens standard maintenance detail for a standard with uploaded source files
- **THEN** the system SHALL return a list of all linked source files with file name, file type, parse status, index status, chunk count, and error messages
- **AND** the UI SHALL surface parse/index error messages when present (for example via tooltip or inline hint)

#### Scenario: Maximum file count is enforced
- **WHEN** a user attempts to upload a source file beyond the configured per-standard limit
- **THEN** the system SHALL reject the upload with a validation error

#### Scenario: Draft standard stores files without vector indexing
- **WHEN** a standard is in `DRAFT` status and source files are uploaded
- **THEN** the system SHALL store the files and metadata only
- **AND** it SHALL NOT write new vectors to Elasticsearch until the standard is published

#### Scenario: Unsupported file type is rejected
- **WHEN** a user uploads an unsupported file type as a standard source
- **THEN** the system SHALL reject the upload with a validation error

### Requirement: Standard source files are stored locally under backend resources
The system SHALL store uploaded standard source files in a dedicated runtime directory under the backend project, not in `src/main/resources`.

Each file SHALL be stored under `backend/resources/standard-documents/{standardId}/` using a path that includes the linked `documentId` so multiple files can coexist.

#### Scenario: File path is persisted for later parsing
- **WHEN** a source file is saved successfully
- **THEN** the system SHALL persist `source_file_name`, `source_file_path`, and `source_file_hash` on the linked standard document record
- **AND** the stored path SHALL resolve to a readable local file for deterministic parsing

### Requirement: Published standards trigger ingestion for all linked source files
The system SHALL parse, chunk, embed, and index **each linked source file that has an uploaded file** when a standard is published.

#### Scenario: Publish ingests all uploaded source files
- **WHEN** a draft standard with multiple uploaded source files is published
- **THEN** the system SHALL ingest each linked file using the appropriate format-specific extractor
- **AND** it SHALL update parse/index status and error messages independently on each document record
- **AND** it SHALL return a per-file ingest summary to the client

#### Scenario: Publish triggers PDF ingestion
- **WHEN** a linked PDF source file is ingested
- **THEN** the system SHALL extract text with Apache PDFBox
- **AND** it SHALL chunk, embed, and index clauses into Elasticsearch

#### Scenario: Publish triggers Excel ingestion
- **WHEN** a linked `xlsx` or `xls` source file is ingested
- **THEN** the system SHALL extract row-oriented text with Apache POI
- **AND** it SHALL chunk, embed, and index the extracted text into Elasticsearch

#### Scenario: Publish triggers image OCR ingestion
- **WHEN** a linked `png`, `jpg`, or `jpeg` source file is ingested
- **THEN** the system SHALL extract text through the configured Vision OCR model gateway (`deepseek-ai/DeepSeek-OCR` on SiliconFlow by default)
- **AND** extracted OCR text SHALL be marked as reference-only evidence

#### Scenario: Publish without source files remains valid
- **WHEN** a draft standard is published without any uploaded source file
- **THEN** the system SHALL publish the structured standard successfully
- **AND** it SHALL expose that no source documents are available for RAG indexing

#### Scenario: Partial index failure does not rollback publish
- **WHEN** publish succeeds but one or more source files fail indexing
- **THEN** the standard SHALL remain `PUBLISHED`
- **AND** each failed document record SHALL show index failure details
- **AND** the user SHALL be able to retry indexing for the failed file without republishing the standard

#### Scenario: Vision unavailable causes visible per-file index failure
- **WHEN** publish succeeds for an image source file but Vision OCR is disabled or unavailable
- **THEN** the standard SHALL remain `PUBLISHED`
- **AND** only the affected image document record SHALL show parse/index failure
- **AND** other successfully indexed source files SHALL remain retrievable

### Requirement: Source files can be downloaded and deleted individually
The system SHALL allow users to download or delete individual source files from the standard maintenance page.

Adding a new source file on a published standard SHALL trigger ingestion for the new file only and SHALL NOT remove vectors from other linked files.

#### Scenario: Download one source file
- **WHEN** a standard has multiple uploaded source files
- **THEN** the user SHALL be able to download any selected file with the correct content type

#### Scenario: Delete one source file on published standard
- **WHEN** a user deletes one source file from a published standard
- **THEN** the system SHALL delete only that stored file
- **AND** it SHALL delete clause records and Elasticsearch vectors linked to that `documentId`
- **AND** it SHALL leave other linked source files and their vectors unchanged

#### Scenario: Delete one source file on draft standard
- **WHEN** a user deletes one source file from a draft standard
- **THEN** the system SHALL delete the stored file and linked document metadata
- **AND** it SHALL NOT affect other linked source files

#### Scenario: Add source file on published standard
- **WHEN** a user uploads a new source file to a published standard
- **THEN** the system SHALL store the new file as a new linked document
- **AND** it SHALL immediately ingest only the new file
- **AND** it SHALL NOT purge vectors from existing linked files

#### Scenario: Reindex one source file
- **WHEN** a user triggers reindex for one linked source file on a published standard
- **THEN** the system SHALL purge and re-ingest vectors only for that `documentId`

#### Scenario: Reindex all source files
- **WHEN** a user triggers reindex-all for a published standard
- **THEN** the system SHALL purge and re-ingest vectors for every linked source file that has an uploaded file

### Requirement: Standard deletion cleans all linked source and vector data
The system SHALL remove all linked source files, document metadata, clause records, and Elasticsearch vectors when a structured standard is deleted.

#### Scenario: Delete standard with multiple uploaded source files
- **WHEN** a standard with multiple linked source documents and indexed clauses is deleted
- **THEN** the system SHALL delete all stored source files when present
- **AND** it SHALL delete all linked clause records and Elasticsearch vectors

### Requirement: Structured rules remain judgment truth
Extracted source text and indexed clauses SHALL be used only for retrieval, citation, and explanation.

#### Scenario: Source upload does not mutate structured indicators
- **WHEN** source files are uploaded, deleted, parsed, or indexed for a standard
- **THEN** the system SHALL NOT modify `qc_standard_indicator` limits based on extracted source text

### Requirement: Legacy single-file API remains temporarily compatible
The system SHALL keep existing singular `/source-file` endpoints for one release cycle with compatibility behavior defined below.

#### Scenario: Legacy upload endpoint adds a file
- **WHEN** a client calls the legacy singular upload endpoint
- **THEN** the system SHALL behave the same as the plural upload endpoint by adding a new source file rather than replacing all existing files

#### Scenario: Legacy download endpoint returns a file when any exist
- **WHEN** a client calls the legacy singular download endpoint and the standard has uploaded source files
- **THEN** the system SHALL download one existing source file (the most recently uploaded file)

### Requirement: Standard maintenance exposes clause split inspection for indexed source files
The system SHALL allow users with `menu:standard` permission to inspect parsed clause splits for a linked source file from the standard maintenance page.

The inspection view SHALL be read-only and SHALL NOT allow editing structured indicators or clause records.

For each source file with at least one stored clause record, the user SHALL be able to open a drawer that shows:

- document-level summary: file name, parse status, index status, chunk count, and parse/index error message when present
- paginated clause list scoped to that `documentId`
- per-clause fields: clause number, page number, paragraph text preview, embedding status
- keyword search across clause number and paragraph text within that document
- clause detail view with full paragraph text and troubleshooting identifiers (`clauseKey`, `esDocumentKey`) when a row is selected

#### Scenario: View splits from standard maintenance after publish
- **WHEN** a user with `menu:standard` permission opens standard maintenance detail for a published standard whose source file has `chunkCount > 0`
- **THEN** the source file row SHALL offer a "view splits" action
- **AND** opening it SHALL show the paginated clause list for that file only

#### Scenario: Search clauses within one source file
- **WHEN** a user enters a keyword in the clause inspection drawer
- **THEN** the system SHALL return matching clauses for that `documentId` only
- **AND** results SHALL be paginated

#### Scenario: View full clause detail
- **WHEN** a user selects a clause row in the inspection drawer
- **THEN** the system SHALL display the full `paragraphText` and embedding status for that clause
- **AND** it SHALL include `clauseKey` and `esDocumentKey` when available

#### Scenario: Scoped API enforces document ownership
- **WHEN** a client requests clause data using `/standards/{standardId}/source-files/{documentId}/clauses/*`
- **THEN** the system SHALL verify that `documentId` belongs to `standardId`
- **AND** it SHALL reject the request if the document is not linked to that standard

#### Scenario: No splits action when no clauses exist
- **WHEN** a source file has `chunkCount = 0` or has not been parsed
- **THEN** the "view splits" action SHALL NOT be shown

#### Scenario: Index failure still allows split inspection when chunks exist
- **WHEN** a source file has `indexStatus = FAILED` but stored clause records exist
- **THEN** the user SHALL still be able to open the inspection drawer
- **AND** failed embedding status SHALL be visible per clause where applicable
