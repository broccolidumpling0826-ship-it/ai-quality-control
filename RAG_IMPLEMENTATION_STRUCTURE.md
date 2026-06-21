# RAG Implementation Structure

Last updated: 2026-06-21

本文档说明当前项目中标准 RAG 部分的程序结构、接口实现关系，以及文档入库链路和检索问答链路。

## 1. Overall Structure

```text
+-----------------------------+        +-----------------------------+
| Controller                  |        | Service API                 |
|-----------------------------|        |-----------------------------|
| StandardDocumentController  | -----> | StandardDocumentService     |
| StandardRagController       | -----> | StandardRagService          |
|                             |        | AiDegradationService        |
+-----------------------------+        +-----------------------------+
                                                |
                                                v
                                     +-----------------------------+
                                     | Service Impl                |
                                     |-----------------------------|
                                     | StandardDocumentServiceImpl |
                                     | StandardRagServiceImpl      |
                                     +-----------------------------+
                                      |        |        |        |
                                      |        |        |        |
                                      v        v        v        v
+--------------------------------+  +----------------+  +------------------+
| RAG Support                    |  | Gateway        |  | Mapper / DB      |
|--------------------------------|  |----------------|  |------------------|
| StandardDocumentTextExtractor  |  | ModelGateway   |  | QcStandard       |
|   -> SourceDocumentTextExtractor| |   -> OpenAi    |  | DocumentMapper   |
|      -> PdfSource...           |  |      Compatible|  |                  |
|      -> OfficeSource...        |  |      Gateway   |  | QcStandard       |
|      -> PlainTextSource...     |  |                |  | ClauseMapper     |
|                                |  | VectorStore    |  |                  |
| ExtractedStandardDocument      |  | Gateway        |  | MySQL            |
|   -> DocumentTextSegment       |  |   -> Elastic   |  +------------------+
|                                |  |      search    |
| StandardClauseChunker          |  |      Gateway   |  +------------------+
|   -> StandardClauseChunk       |  |                |  | External         |
+--------------------------------+  +----------------+  |------------------|
                                      |        |         | Chat/Embedding   |
                                      v        v         | Model            |
                                Chat/Embedding ES       | Elasticsearch    |
                                                       +------------------+

Key dependency direction:

StandardDocumentController
  -> StandardDocumentService
    -> StandardDocumentServiceImpl
      -> StandardDocumentTextExtractor
        -> SourceDocumentTextExtractor implementations
      -> StandardClauseChunker
      -> ModelGateway
      -> VectorStoreGateway
      -> QcStandardDocumentMapper / QcStandardClauseMapper

StandardRagController
  -> StandardRagService
    -> StandardRagServiceImpl
      -> ModelGateway
      -> VectorStoreGateway
      -> AiDegradationService
      -> StandardDocumentService
```

## 2. Document Ingestion Flow

文档入库链路负责将标准/协议/案例源文件转换为可检索、可引用、可向量召回的条款数据。

```text
Frontend / Debug Client
        |
        | POST /standard-documents/ingest-index
        v
StandardDocumentController
        |
        | ingestAndIndexDocument(cmd)
        v
StandardDocumentServiceImpl
        |
        | 1. Load source document metadata
        v
QcStandardDocumentMapper
        |
        | document source_file_name/source_file_path
        v
StandardDocumentServiceImpl
        |
        | 2. Extract text
        v
StandardDocumentTextExtractor
        |
        | select extractor by file type
        v
SourceDocumentTextExtractor implementation
        |
        | PDF    -> PdfSourceDocumentTextExtractor / PDFBox
        | Office -> OfficeSourceDocumentTextExtractor / POI
        | Text   -> PlainTextSourceDocumentTextExtractor
        v
ExtractedStandardDocument
        |
        | contains List<DocumentTextSegment>
        v
StandardClauseChunker
        |
        | deterministic clause/paragraph chunking
        v
List<StandardClauseChunk>
        |
        | 3. Persist chunks
        v
QcStandardClauseMapper
        |
        | insert qc_standard_clause
        v
StandardDocumentServiceImpl
        |
        | 4. Generate embeddings
        v
ModelGateway
        |
        | embed(chunk texts)
        v
Chat/Embedding Model
        |
        | embedding vectors
        v
StandardDocumentServiceImpl
        |
        | 5. Index text + vectors + metadata
        v
VectorStoreGateway
        |
        | indexClauses(VectorIndexRequest)
        v
Elasticsearch
        |
        | VectorIndexResponse
        v
StandardDocumentServiceImpl
        |
        | 6. Update parse/index status
        v
QcStandardDocumentMapper
        |
        v
StandardDocumentIngestVO
```

### Main Classes

- `StandardDocumentController`: exposes document query, clause query, prepared clause indexing, and document ingest-index APIs.
- `StandardDocumentService`: service API for standard source documents and clauses.
- `StandardDocumentServiceImpl`: orchestrates document extraction, chunking, clause persistence, embedding, ES indexing, and status updates.
- `StandardDocumentTextExtractor`: dispatcher for local source files. It validates the path, detects file type, and delegates to a concrete extractor.
- `SourceDocumentTextExtractor`: document extraction extension interface.
- `PdfSourceDocumentTextExtractor`: extracts PDF text with page anchors through Apache PDFBox.
- `OfficeSourceDocumentTextExtractor`: extracts Office text through Apache POI.
- `PlainTextSourceDocumentTextExtractor`: extracts Markdown, text, and CSV content.
- `StandardClauseChunker`: deterministic clause-aware chunker. It consumes extracted text only and does not depend on the source file type.
- `ModelGateway`: provider-neutral chat and embedding gateway.
- `VectorStoreGateway`: provider-neutral vector indexing/search/delete gateway.

## 3. RAG Query Flow

检索问答链路负责将用户自然语言问题转换为 query embedding，召回 ES 中的条款，再将检索到的来源片段发送给 chat 模型生成带引用的回答。

```text
Standard RAG Page
        |
        | POST /standard-rag/query
        v
StandardRagController
        |
        | query(cmd)
        v
StandardRagServiceImpl
        |
        | 1. Embed user query
        v
ModelGateway
        |
        | embed(query)
        v
Chat/Embedding Model
        |
        | queryVector
        v
StandardRagServiceImpl
        |
        | 2. Search clauses
        v
VectorStoreGateway
        |
        | searchClauses(queryText + queryVector + filters)
        v
Elasticsearch
        |
        | matched clauses
        v
StandardRagServiceImpl
        |
        | 3. Fallback when ES has no usable result
        v
StandardDocumentService.pageClauses(keyword)
        |
        | DB clauses, if needed
        v
StandardRagServiceImpl
        |
        +--> no cited evidence
        |       -> no-evidence refusal
        |
        +--> low relevance
        |       -> raw retrieval / low confidence
        |
        +--> prompt injection suspected
        |       -> raw retrieval, do not call chat
        |
        +--> authoritative evidence exists
                |
                | 4. Build grounded prompt with retrieved chunks only
                v
              ModelGateway.chat(...)
                |
                | generated answer
                v
              AiDegradationService.resolveAiOutput(...)
                |
                | cache / generated / raw retrieval / unavailable
                v
              StandardRagAnswerVO
```

### Main Classes

- `StandardRagController`: exposes natural-language RAG query API.
- `StandardRagService`: service API for standard RAG query.
- `StandardRagServiceImpl`: embeds the query, searches vector store, falls back to DB keyword clauses when needed, builds grounded chat prompts, handles prompt injection protection, and returns cited answers or refusal.
- `AiDegradationService`: provides cache, rule/raw retrieval fallback, and unavailable-state handling.
- `OpenAiCompatibleModelGateway`: current `ModelGateway` implementation for OpenAI-compatible chat and embedding APIs.
- `ElasticsearchVectorStoreGateway`: current `VectorStoreGateway` implementation for Elasticsearch indexing, vector/keyword search, and delete.

## 4. Data Objects

### Ingestion DTO/VO

- `StandardDocumentIngestCmd`
  - `documentId`
  - `reindexExisting`
  - `indexName`
  - `maxChunkChars`

- `StandardDocumentIngestVO`
  - `documentId`
  - `parseStatus`
  - `indexStatus`
  - `extractedPageCount`
  - `chunkCount`
  - `indexedCount`
  - `failedClauseIds`
  - `retrievalMode`
  - `errorCategory`
  - `errorMessage`

### Extraction And Chunking Models

- `ExtractedStandardDocument`
  - source file metadata
  - file type
  - `List<DocumentTextSegment>`

- `DocumentTextSegment`
  - `pageNo`
  - `text`

- `StandardClauseChunk`
  - `clauseNo`
  - `clauseTitle`
  - `pageNo`
  - `paragraphText`
  - `retrievalKeywords`

### Query DTO/VO

- `StandardRagQueryCmd`
  - natural-language query
  - source/customer/product/indicator filters
  - topK

- `StandardRagAnswerVO`
  - answer/refusal fields
  - confidence fields
  - degradation fields
  - embedding/retrieval mode fields
  - source clauses

- `StandardRagSourceVO`
  - clause/document/source metadata
  - source text
  - retrieval score
  - reference-only marker

## 5. Decoupling Rules

- Document extraction and semantic chunking are separated.
- Adding a new source file type must be done by adding a new `SourceDocumentTextExtractor` implementation.
- `StandardClauseChunker` must not depend on PDFBox, POI, OCR, HTML parser, or any source-file-specific library.
- Embedding is performed after deterministic chunking. The embedding model must not decide chunk boundaries.
- RAG source text is retrieval and citation evidence only. Structured standard tables remain the source of truth for deterministic judgment.
- Business services depend on `ModelGateway` and `VectorStoreGateway`, not concrete provider clients.

## 6. Extension Example

To support HTML standards later:

1. Add `HtmlSourceDocumentTextExtractor implements SourceDocumentTextExtractor`.
2. Implement `supports("html")` and `supports("htm")`.
3. Parse HTML into plain text segments and citation anchors.
4. Return `ExtractedStandardDocument`.
5. Do not modify `StandardClauseChunker`.

To support OCR scanned PDFs later:

1. Add an OCR-specific extractor or enhance PDF extraction behind `SourceDocumentTextExtractor`.
2. Return recognized text as `DocumentTextSegment`.
3. Preserve page number when available.
4. Keep chunking unchanged.
