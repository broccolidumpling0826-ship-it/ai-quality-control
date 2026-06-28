## ADDED Requirements

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

## MODIFIED Requirements

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
