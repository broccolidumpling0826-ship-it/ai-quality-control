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
| `EMBEDDING_ENABLED` | Enable embedding model calls | `false` |
| `EMBEDDING_BASE_URL` | OpenAI-compatible embedding API base URL | `https://api.siliconflow.cn/v1` |
| `EMBEDDING_API_KEY` | Embedding model API key, separate from chat key | empty |
| `EMBEDDING_MODEL` | Embedding model name | `BAAI/bge-m3` |
| `EMBEDDING_PROVIDER` | Embedding provider label | `SILICONFLOW` |
| `EMBEDDING_TIMEOUT_MILLIS` | Embedding model timeout | `15000` |
| `ES_VECTOR_ENABLED` | Enable vector store calls | `false` |
| `ES_HOST` | Elasticsearch endpoint | `http://localhost:9200` |
| `ES_USERNAME` | Elasticsearch username | empty |
| `ES_PASSWORD` | Elasticsearch password | empty |
| `ES_STANDARD_INDEX` | Clause vector index | `quality-standard-clauses` |
| `ES_TIMEOUT_MILLIS` | Vector search timeout | `5000` |

## External Checks Not Completed In This Local Run

The following require live services or credentials and should be verified in the target demo environment:

- MySQL end-to-end demo data load and API workflow.
- Redis dashboard cache read/write behavior.
- Elasticsearch 8.15.0 clause index creation, clause indexing, semantic retrieval, and raw retrieval fallback.
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
