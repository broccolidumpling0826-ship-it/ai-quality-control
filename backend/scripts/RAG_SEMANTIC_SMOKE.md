# RAG 语义切割与向量入库烟测

本烟测只验证一份 Markdown 文档的最小闭环：

```text
Markdown 文档
  -> Chat 模型语义切割为条款块
  -> Embedding 模型生成向量
  -> 写入 Elasticsearch dense_vector
  -> 后端标准 RAG 用 query embedding 检索
  -> 命中段落进入 Chat 模型生成回答
```

## 1. 初始文档

```text
backend/scripts/demo-documents/semantic-rag-smoke-q235b.md
```

该文档包含 Q235B 冷轧板的适用范围、Rm、A、厚度公差、让步风险、质保书限制等内容。

## 2. 前置条件

先在 ES 控制台执行：

```text
backend/scripts/init-es-standard-clauses.devtools
```

确保 `quality-standard-clauses` 索引已存在，且 `embedding` 字段为 `dense_vector`，维度为 `1024`。

## 3. 执行语义切割、向量化、写 ES

在 `backend` 目录执行：

```bash
node scripts/ingest-semantic-rag-smoke.mjs
```

脚本会读取 `application-dev.yml` 中的本地默认配置；如果你更想显式传入，也可以使用：

```bash
export AI_MODEL_API_KEY="你的chat key"
export EMBEDDING_API_KEY="你的embedding key"
export ES_HOST="http://localhost:9200"
export ES_USERNAME="elastic"
export ES_PASSWORD="你的ES密码"
node scripts/ingest-semantic-rag-smoke.mjs
```

只看切割和 embedding 生成结果、不写 ES：

```bash
node scripts/ingest-semantic-rag-smoke.mjs --dry-run
```

脚本输出文件：

```text
backend/scripts/generated/semantic-rag-smoke-docs.json
```

## 4. 验证 ES 已存入向量

在 ES 控制台执行：

```json
GET /quality-standard-clauses/_search
{
  "size": 10,
  "_source": [
    "clauseId",
    "documentId",
    "standardCode",
    "clauseNo",
    "paragraphText",
    "embedding"
  ],
  "query": {
    "term": {
      "documentId": {
        "value": "smoke_doc_q235b_semantic_001"
      }
    }
  }
}
```

预期：

- 能查到多条 `smoke_clause_...` 文档。
- 每条都有 `paragraphText`。
- 每条都有 `embedding` 数组，长度应为 `1024`。

## 5. 验证后端 RAG 使用向量检索并调用 Chat

启动后端后，调用：

```bash
curl -s http://localhost:8080/api/v1/standard-rag/query \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer 你的登录token' \
  -d '{
    "query": "Q235B冷轧板Rm低于370但不低于360时可以怎么处理？",
    "sourceTypes": ["NATIONAL"],
    "variety": "冷轧板",
    "grade": "Q235B",
    "topK": 5
  }'
```

也可以在前端进入：

```text
标准库 -> 标准RAG检索
```

输入：

```text
Q235B冷轧板Rm低于370但不低于360时可以怎么处理？
```

预期：

- 页面标签显示 `EMBEDDING`。
- 页面标签显示 `ES_VECTOR_SCRIPT_SCORE`。
- 来源段落中出现 `GB/T-SMOKE-912-2026`。
- 回答说明 Rm 在 360 MPa 至 370 MPa 之间可进入让步评审，但不得直接作为正式合格结论。
- 后端日志出现 `标准RAG调用Chat模型，sourceCount=...`，表示检索段落已进入 Chat prompt。

