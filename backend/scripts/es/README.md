# Elasticsearch 初始化脚本

标准 RAG 条款向量索引 `quality-standard-clauses` 的初始化资源，与 MySQL `qc_standard_clause` P0 种子数据对齐。

## 文件说明

| 文件 | 用途 | 类比 MySQL |
|-----|------|-----------|
| `init-es-index-mapping.json` | 索引 settings + mappings | 建表 DDL |
| `p0-clauses-seed.json` | 14 条 P0 条款文档（可读源数据） | 种子 SQL 源 |
| `init-es-seed-data-only.bulk.ndjson` | Bulk 灌数 NDJSON（无 embedding） | `init-seed-data-only.sql` |
| `init-es-seed-data-only.sh` | 仅灌数据（索引须已存在） | 仅执行 INSERT |
| `init-es-from-empty.sh` | 建索引 + 灌数据一键初始化 | DDL + INSERT |
| `build-es-seed-bulk.mjs` | 从 JSON 重新生成 bulk 文件 | — |
| `../init-es-standard-clauses.devtools` | Kibana Dev Tools 版（含验证查询） | — |

## 快速开始

### 1. 一键初始化（推荐）

```bash
bash backend/scripts/es/init-es-from-empty.sh
```

### 2. 仅灌数据（索引已建好）

```bash
bash backend/scripts/es/init-es-seed-data-only.sh
```

### 3. 手动 curl

```bash
# 创建索引
curl -X PUT "http://localhost:9200/quality-standard-clauses" \
  -H 'Content-Type: application/json' \
  -d @backend/scripts/es/init-es-index-mapping.json

# Bulk 写入
curl -X POST "http://localhost:9200/_bulk?refresh=wait_for" \
  -H 'Content-Type: application/x-ndjson' \
  --data-binary @backend/scripts/es/init-es-seed-data-only.bulk.ndjson
```

### 4. Kibana Dev Tools

粘贴执行：`backend/scripts/init-es-standard-clauses.devtools`

## 环境变量

| 变量 | 默认 | 说明 |
|-----|------|------|
| `ES_HOST` | `http://localhost:9200` | ES 地址（SSH 隧道） |
| `ES_USERNAME` | 空 | Basic 认证用户名 |
| `ES_PASSWORD` | 空 | Basic 认证密码 |
| `ES_STANDARD_INDEX` | `quality-standard-clauses` | 索引名，与后端 `ES_STANDARD_INDEX` 一致 |
| `ES_RECREATE` | `0` | 设为 `1` 时删除已有索引后重建 |

## 向量维度

默认 Embedding 模型 `BAAI/bge-m3` 输出 **1024** 维，mapping 中：

```json
"embedding": { "type": "dense_vector", "dims": 1024, "similarity": "cosine" }
```

若曾用 512 维建过索引，需 `ES_RECREATE=1` 重建，或换索引名并更新 `ES_STANDARD_INDEX`。

## 种子数据内容

14 条 P0 条款，`_id` 与 MySQL `qc_standard_clause.id` 一致：

- 国标 GB/T 912-2008 Q235B：4 条
- 客户协议 v1：4 条
- 客户协议 v2（冲突样例）：3 条
- 历史案例：3 条

**不含 `embedding` 字段**，可立即用于关键词检索（`multi_match`）。  
完整向量检索需配置 SiliconFlow Embedding 后执行：

```bash
# 后端已启动且 ES 可用
node backend/scripts/index-p0-clauses.mjs
```

## 验证

```bash
curl -s "http://localhost:9200/quality-standard-clauses/_count?pretty"

curl -s -H 'Content-Type: application/json' \
  -X POST "http://localhost:9200/quality-standard-clauses/_search" \
  -d '{
    "size": 3,
    "query": {
      "multi_match": {
        "query": "Q235B 抗拉强度 让步",
        "fields": ["paragraphText^4", "retrievalKeywords^3", "standardName^2"]
      }
    }
  }'
```

## 与 MySQL 初始化顺序

```bash
# 1. MySQL 表结构 + 数据
bash backend/scripts/init-db-from-empty.sh

# 2. Elasticsearch 索引 + 条款
bash backend/scripts/es/init-es-from-empty.sh

# 3.（可选）生成 embedding 向量
node backend/scripts/index-p0-clauses.mjs
```
