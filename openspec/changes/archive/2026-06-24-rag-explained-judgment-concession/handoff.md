# P0 Handoff Notes

Change: `rag-explained-judgment-concession`
Last updated: 2026-06-21

## Environment Variables

Backend runtime:

| Variable | Purpose | Default |
| --- | --- | --- |
| `DB_USER` | MySQL username | `ai_quality_control` in dev |
| `DB_PASSWORD` | MySQL password | dev placeholder in `application-dev.yml` |
| `REDIS_PASSWORD` | Redis password | dev placeholder in `application-dev.yml` |
| `AI_MODEL_PROVIDER` | Chat model provider label | `SILICONFLOW` |
| `AI_MODEL_ENABLED` | Enable chat model gateway calls | `false` |
| `AI_MODEL_BASE_URL` | OpenAI-compatible chat API base URL | `https://api.siliconflow.cn/v1` |
| `AI_MODEL_API_KEY` | Chat model API key | empty |
| `AI_MODEL_CHAT_MODEL` | Chat model name | `deepseek-ai/DeepSeek-V3.2` |
| `AI_MODEL_THINKING_ENABLED` | Enable provider reasoning/thinking mode when supported | `false` |
| `AI_MODEL_REASONING_EFFORT` | Provider reasoning effort when supported | `high` |
| `AI_MODEL_TIMEOUT_MILLIS` | Chat model timeout | `15000` |
| `AI_VISION_ENABLED` | Enable Vision OCR for image source files | `true` in dev |
| `AI_VISION_MODEL` | Vision OCR model name | `deepseek-ai/DeepSeek-OCR` |
| `AI_VISION_OCR_PROMPT` | OCR user prompt sent with image | `<image>\n<|grounding|>Convert the document to markdown.` |
| `AI_VISION_IMAGE_DETAIL` | OpenAI-compatible image detail level | `high` |
| `AI_VISION_TIMEOUT_MILLIS` | Vision OCR timeout | `60000` |
| `EMBEDDING_ENABLED` | Enable embedding model calls | `false` |
| `EMBEDDING_BASE_URL` | OpenAI-compatible embedding API base URL | `https://api.siliconflow.cn/v1` |
| `EMBEDDING_API_KEY` | Embedding model API key, separate from chat key | empty |
| `EMBEDDING_MODEL` | Embedding model name | `BAAI/bge-m3` |
| `EMBEDDING_PROVIDER` | Embedding provider label | `SILICONFLOW` |
| `EMBEDDING_TIMEOUT_MILLIS` | Embedding model timeout | `15000` |
| `ES_VECTOR_ENABLED` | Enable vector store calls | `true` in dev profile |
| `ES_HOST` | Elasticsearch endpoint | `http://localhost:9200` |
| `ES_USERNAME` | Elasticsearch username | empty |
| `ES_PASSWORD` | Elasticsearch password | empty |
| `ES_STANDARD_INDEX` | Clause vector index | `quality-standard-clauses` |
| `ES_TIMEOUT_MILLIS` | Vector search timeout | `5000` |
| `STANDARD_DOC_STORAGE_PATH` | Runtime directory for uploaded standard source files | `resources/standard-documents` (relative to backend working directory) |
| `STANDARD_DOC_MAX_FILES_PER_STANDARD` | Maximum source files per structured standard | `10` |

Supported source file formats: `pdf`, `xlsx`, `xls`, `png`, `jpg`, `jpeg`. Image OCR uses the same SiliconFlow API key as chat (`AI_MODEL_API_KEY`) with model `deepseek-ai/DeepSeek-OCR` by default.

Each structured standard may have **multiple** linked `qc_standard_document` rows (one per uploaded file). Storage path pattern:

```text
backend/resources/standard-documents/{standardId}/{documentId}_{safeFileName}
```

## Standard Source File Storage

Uploaded standard source files from the standard maintenance page are stored under:

```text
backend/resources/standard-documents/{standardId}/
```

This is a runtime directory, not `src/main/resources`. Each structured standard may have one linked `qc_standard_document` row via `standard_id`.

Ingestion behavior:

- `DRAFT` + upload: store file as new linked document row; no ES write
- `PUBLISH`: ingest **each** linked file with an uploaded path (PDFBox / POI Excel rows / Vision OCR for images) → chunk → embed → ES
- `PUBLISHED` + add file: create new document row, ingest new file only
- `PUBLISHED` + delete one file: purge clauses/vectors for that `documentId` only
- `PUBLISHED` + reindex one/all: per-file or all-file vector rebuild
- publish success with partial index failure: keep standard published, expose per-file errors

Multi-file API (planned implementation):

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/v1/standards/{id}/source-files` | List all source files |
| POST | `/api/v1/standards/{id}/source-files` | Add one source file |
| GET | `/api/v1/standards/{id}/source-files/{documentId}` | Download one file |
| DELETE | `/api/v1/standards/{id}/source-files/{documentId}` | Delete one file + vectors |
| POST | `/api/v1/standards/{id}/source-files/{documentId}/reindex` | Reindex one file |
| POST | `/api/v1/standards/{id}/source-files/reindex-all` | Reindex all files |

Legacy singular `/source-file` endpoints remain compatible for one release cycle.

## RAG Ingestion Contract

The current RAG requirement is a full ingestion pipeline, not only a prepared-clause demo:

1. Register or upload a source document from PDF, Excel (`xlsx`/`xls`), image (`png`/`jpg`/`jpeg`), Markdown, or plain text.
2. Extract text deterministically:
   - PDF: Apache PDFBox.
   - Excel: Apache POI row-oriented extractor.
   - Image: ModelGateway Vision OCR (`deepseek-ai/DeepSeek-OCR` on SiliconFlow by default).
   - Markdown/text: direct parser.
3. Preserve citation anchors such as document id, standard code, version, page number when available, clause heading, and applicability metadata.
4. Chunk text with code rules based on chapter, clause, paragraph, and natural boundaries. Optional lightweight NLP sentence segmentation may be used only as a fallback. The embedding model must not decide chunk boundaries.
5. Generate embedding vectors for chunks before ES indexing.
6. Store source text, embedding vector, citation metadata, applicability metadata, and parse/embedding/index status in DB/ES.
7. For RAG query, execute `query text -> query embedding -> vector/keyword retrieval -> source-grounded chat answer`.

After applying P0 clause seed migrations, index prepared clauses into ES:

```bash
cd backend
node scripts/index-p0-clauses.mjs
```

Structured standard tables remain the judgment truth. Extracted PDF/Office/table text is retrieval evidence for citation and review only.

## External Checks Not Completed In This Local Run

The following require live services or credentials and should be verified in the target demo environment:

- MySQL end-to-end demo data load and API workflow.
- Redis dashboard cache read/write behavior.
- PDFBox/POI document extraction, deterministic chunk generation, embedding generation, ES clause vector indexing, semantic retrieval, and raw retrieval fallback.
- Elasticsearch 8.15.0 clause index creation and vector dimension compatibility with the selected embedding model.
- OpenAI-compatible chat model generation and timeout/degradation behavior.
- OpenAI-compatible embedding generation and vector dimension alignment.
- Browser/manual flow from inspection entry to judgment, explanation, concession risk, and certificate Q&A.

## Expected Degradation Behavior

AI-dependent features must degrade in this order:

1. Database pre-generated cache.
2. Rule-template explanation from structured judgment evidence.
3. Raw ES retrieved clauses when model generation is unavailable.
4. Clear unavailable state when neither AI nor retrieval evidence is available.

Core deterministic judgment must continue to use structured standard rules and must not depend on AI or ES.

## Local Verification Completed

- Backend compile: `cd backend && mvn -DskipTests compile`
- Frontend build: `cd frontend && npm run build`
- Evaluation fixture/report validation: `node openspec/changes/rag-explained-judgment-concession/evaluation/run-evaluation.mjs`

## Demo Reset Notes

P0 migrations use `INSERT IGNORE` and scoped updates, so they can be reapplied after the baseline seed scripts.

For the conflict scenario, to replay裁决 from the start:

```sql
UPDATE standard_conflict
SET status = 'PENDING',
    selected_standard_id = NULL,
    decision_standard_id = NULL,
    decision_reason = NULL,
    decision_by = NULL,
    decision_time = NULL,
    rejudge_judgment_id = NULL
WHERE id = 'scf_p0_001';

UPDATE qc_judgment_result
SET is_final = CASE WHEN id = 'jud004' THEN 1 ELSE is_final END
WHERE record_id = 'rec004';
```

If裁决 has already created a new final judgment for `rec004`, manually inspect and clear only the generated demo judgment record after backing up audit data.
