#!/bin/bash
# ============================================================
# Elasticsearch 纯数据灌入（索引须已存在）
# 与 MySQL init-seed-data-only.sql 对应，仅写入 P0 条款文档。
# ============================================================
#
# 用法:
#   ES_HOST=http://localhost:9200 bash backend/scripts/es/init-es-seed-data-only.sh
#
# 环境变量:
#   ES_HOST          默认 http://localhost:9200
#   ES_USERNAME      可选
#   ES_PASSWORD      可选
#   ES_STANDARD_INDEX 默认 quality-standard-clauses
#
# 说明:
#   - 本脚本不含 embedding 向量，支持关键词检索（multi_match）。
#   - 向量检索需配置 Embedding 后执行: node backend/scripts/index-p0-clauses.mjs
#
# ============================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ES_HOST="${ES_HOST:-http://localhost:9200}"
ES_INDEX="${ES_STANDARD_INDEX:-quality-standard-clauses}"
BULK_FILE="${SCRIPT_DIR}/init-es-seed-data-only.bulk.ndjson"

if [ ! -f "$BULK_FILE" ]; then
  echo "缺少 bulk 文件，正在生成..."
  node "${SCRIPT_DIR}/build-es-seed-bulk.mjs"
fi

CURL_AUTH=()
if [ -n "${ES_USERNAME:-}" ] && [ -n "${ES_PASSWORD:-}" ]; then
  CURL_AUTH=(-u "${ES_USERNAME}:${ES_PASSWORD}")
fi

echo "检查索引 ${ES_INDEX} ..."
STATUS=$(curl -s -o /dev/null -w '%{http_code}' "${CURL_AUTH[@]}" -X HEAD "${ES_HOST%/}/${ES_INDEX}")
if [ "$STATUS" = "404" ]; then
  echo "错误: 索引 ${ES_INDEX} 不存在。请先执行 init-es-from-empty.sh 或 init-es-index-mapping.json"
  exit 1
fi

echo "Bulk 写入 P0 条款种子数据..."
RESP=$(curl -s "${CURL_AUTH[@]}" \
  -H 'Content-Type: application/x-ndjson' \
  -X POST "${ES_HOST%/}/_bulk?refresh=wait_for" \
  --data-binary @"${BULK_FILE}")

if echo "$RESP" | grep -q '"errors":true'; then
  echo "Bulk 写入存在错误:"
  echo "$RESP" | head -c 2000
  exit 1
fi

COUNT=$(echo "$RESP" | grep -o '"successful":[0-9]*' | head -1 | cut -d: -f2)
echo "完成。成功写入 ${COUNT:-?} 条文档到 ${ES_INDEX}"
