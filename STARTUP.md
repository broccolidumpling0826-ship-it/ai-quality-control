# 前后端启动步骤

本文档记录当前项目在本地开发环境启动前端、后端、云端 MySQL/Redis/Elasticsearch、SiliconFlow Chat/Embedding 的完整步骤。

## 1. 前置条件

- JDK 8
- Maven
- Node.js / npm
- 可访问云服务器的 SSH 账号
- 云服务器上已运行 MySQL、Redis、Elasticsearch 8.x
- SiliconFlow Chat API Key
- SiliconFlow Embedding API Key

默认本地端口：

| 服务 | 本地端口 | 说明 |
| --- | --- | --- |
| MySQL | `3307` | SSH 隧道转发到云端 `3306` |
| Redis | `6380` | SSH 隧道转发到云端 `6379` |
| Elasticsearch | `9200` | SSH 隧道转发到云端 `9200` |
| Backend | `8080` | Spring Boot |
| Frontend | `5173` | Vite dev server |

## 2. 建立 SSH 隧道

如果云端 MySQL、Redis、ES 没有直接暴露公网端口，本地启动前先建立 SSH 隧道：

```bash
ssh -L 3307:127.0.0.1:3306 \
    -L 6380:127.0.0.1:6379 \
    -L 9200:127.0.0.1:9200 \
    -N ubuntu@你的云服务器IP
```

如果希望隧道在后台运行，可以加 `-f`：

```bash
ssh -L 3307:127.0.0.1:3306 \
    -L 6380:127.0.0.1:6379 \
    -L 9200:127.0.0.1:9200 \
    -N -f ubuntu@你的云服务器IP
```

检查端口：

```bash
nc -z 127.0.0.1 3307
nc -z 127.0.0.1 6380
nc -z 127.0.0.1 9200
```

## 3. 初始化 MySQL

首次启动或重建数据库时执行：

```bash
bash scripts/setup-dev.sh
```

该脚本会创建本地上传目录、初始化基础表结构/字典/测试数据，并安装前端依赖。

AI 增量能力还需要执行 `backend/scripts/migrations/` 下的迁移脚本。建议按文件名顺序执行：

```bash
mysql -u "$DB_USER" -p"$DB_PASSWORD" -h 127.0.0.1 -P 3307 ai_quality_control < backend/scripts/migrations/20260621_01_ai_quality_p0_schema.sql
mysql -u "$DB_USER" -p"$DB_PASSWORD" -h 127.0.0.1 -P 3307 ai_quality_control < backend/scripts/migrations/20260621_02_standard_conflict_dict.sql
mysql -u "$DB_USER" -p"$DB_PASSWORD" -h 127.0.0.1 -P 3307 ai_quality_control < backend/scripts/migrations/20260621_03_ai_quality_p0_menu_rbac.sql
mysql -u "$DB_USER" -p"$DB_PASSWORD" -h 127.0.0.1 -P 3307 ai_quality_control < backend/scripts/migrations/20260621_04_ai_quality_p0_demo_seed.sql
mysql -u "$DB_USER" -p"$DB_PASSWORD" -h 127.0.0.1 -P 3307 ai_quality_control < backend/scripts/migrations/20260621_05_ai_quality_p0_clause_seed.sql
mysql -u "$DB_USER" -p"$DB_PASSWORD" -h 127.0.0.1 -P 3307 ai_quality_control < backend/scripts/migrations/20260621_06_ai_quality_p0_ai_cache_seed.sql
```

## 4. 初始化 Elasticsearch

推荐一键脚本（建索引 + P0 条款种子数据）：

```bash
bash backend/scripts/es/init-es-from-empty.sh
```

仅灌数据（索引已存在）：

```bash
bash backend/scripts/es/init-es-seed-data-only.sh
```

详细说明见 [backend/scripts/es/README.md](backend/scripts/es/README.md)。

也可在 Kibana Dev Tools 中执行（仅建索引 + 验证查询）：

[backend/scripts/init-es-standard-clauses.devtools](backend/scripts/init-es-standard-clauses.devtools)

该脚本会创建默认索引：

```text
quality-standard-clauses
```

当前默认 Embedding 模型是 SiliconFlow `BAAI/bge-m3`，ES 向量维度应为：

```json
"embedding": {
  "type": "dense_vector",
  "dims": 1024,
  "index": true,
  "similarity": "cosine"
}
```

如果之前用 `512` 维创建过索引，需要先删除旧索引再重建，或者换一个新索引名并设置 `ES_STANDARD_INDEX`：

```http
DELETE /quality-standard-clauses
```

验证索引：

```http
GET /quality-standard-clauses/_mapping
GET /quality-standard-clauses/_search
{
  "size": 1,
  "query": {
    "match_all": {}
  }
}
```

## 5. 配置 SiliconFlow Chat 和 Embedding

项目使用两套独立 key：

- `AI_MODEL_API_KEY`：Chat key
- `EMBEDDING_API_KEY`：Embedding key

可以复制模板：

```bash
source backend/scripts/siliconflow-env.example.sh
```

然后替换真实 key：

```bash
export AI_MODEL_API_KEY="你的 SiliconFlow Chat API Key"
export EMBEDDING_API_KEY="你的 SiliconFlow Embedding API Key"
```

当前默认配置：

```bash
export AI_MODEL_PROVIDER="SILICONFLOW"
export AI_MODEL_ENABLED=true
export AI_MODEL_BASE_URL="https://api.siliconflow.cn/v1"
export AI_MODEL_CHAT_MODEL="deepseek-ai/DeepSeek-V3.2"

export EMBEDDING_PROVIDER="SILICONFLOW"
export EMBEDDING_ENABLED=true
export EMBEDDING_BASE_URL="https://api.siliconflow.cn/v1"
export EMBEDDING_MODEL="BAAI/bge-m3"
```

## 6. 配置 ES 和数据库环境变量

按实际云端账号设置：

```bash
export DB_USER="ai_quality_control"
export DB_PASSWORD="你的MySQL密码"

export REDIS_PASSWORD="你的Redis密码"

export ES_VECTOR_ENABLED=true
export ES_HOST="http://127.0.0.1:9200"
export ES_USERNAME="elastic"
export ES_PASSWORD="你的ES密码"
export ES_STANDARD_INDEX="quality-standard-clauses"
```

## 7. 启动后端

```bash
cd backend
mvn spring-boot:run -Dspring.profiles.active=dev
```

后端接口文档：

```text
http://localhost:8080/doc.html
```

## 8. 启动前端

首次启动：

```bash
cd frontend
npm install
npm run dev
```

日常启动：

```bash
cd frontend
npm run dev
```

前端地址：

```text
http://localhost:5173
```

默认账号：

```text
admin / Admin123456
```

## 9. 启动后验证

### 9.1 验证 ES 连接

```bash
curl -u "$ES_USERNAME:$ES_PASSWORD" "$ES_HOST/_cluster/health"
```

### 9.2 验证 Chat 配置

```bash
curl "$AI_MODEL_BASE_URL/chat/completions" \
  -H "Authorization: Bearer $AI_MODEL_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "'"$AI_MODEL_CHAT_MODEL"'",
    "messages": [
      { "role": "user", "content": "请用一句话说明你可以正常响应。" }
    ],
    "temperature": 0.1,
    "max_tokens": 80,
    "enable_thinking": false
  }'
```

### 9.3 验证 Embedding 配置

```bash
curl "$EMBEDDING_BASE_URL/embeddings" \
  -H "Authorization: Bearer $EMBEDDING_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "'"$EMBEDDING_MODEL"'",
    "input": ["Q235B 屈服强度标准条款"]
  }'
```

检查向量维度：

```bash
curl "$EMBEDDING_BASE_URL/embeddings" \
  -H "Authorization: Bearer $EMBEDDING_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "'"$EMBEDDING_MODEL"'",
    "input": ["Q235B 屈服强度标准条款"]
  }' | jq '.data[0].embedding | length'
```

`BAAI/bge-m3` 应与 ES mapping 的 `dims=1024` 对齐。

### 9.4 验证后端构建

```bash
cd backend
mvn test
```

### 9.5 验证前端构建

```bash
cd frontend
npm run build
```

## 10. 常见问题

### ES 索引已存在

如果执行 `PUT /quality-standard-clauses` 返回 `resource_already_exists_exception`，说明索引已经存在。先检查 mapping 是否符合当前模型维度：

```http
GET /quality-standard-clauses/_mapping
```

如果 `embedding.dims` 不是 `1024`，需要删除重建或换新索引名。

### 后端能启动但 RAG 没有向量效果

ES mapping 只是准备索引结构。真正向量化需要写入文档时包含 `embedding` 字段，且查询时传入 `queryVector`。如果没有向量，系统会优先走 ES 文本检索和本地条款降级检索。

### 不想后台运行 SSH 隧道

去掉 `-f`，让隧道占用当前终端即可。按 `Ctrl+C` 会关闭隧道。

### 关闭后台 SSH 隧道

```bash
ps aux | grep "ssh -L"
pkill -f "ssh -L 3307:127.0.0.1:3306"
```
