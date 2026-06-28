#!/bin/bash
# ============================================================
# Elasticsearch 空索引完整初始化：创建索引 + P0 种子数据
# ============================================================
#
# 用法:
#   bash backend/scripts/es/init-es-from-empty.sh
#
# 环境变量:
#   ES_HOST           默认 http://localhost:9200
#   ES_USERNAME       可选
#   ES_PASSWORD       可选
#   ES_STANDARD_INDEX 默认 quality-standard-clauses
#   ES_RECREATE=1     若索引已存在则先删除再重建（慎用）
#
# 前置:
#   - Elasticsearch 8.x 已启动（默认经 SSH 隧道 http://localhost:9200）
#   - 建议 MySQL 已执行 init-seed-data-only.sql（条款 ID 与 MySQL qc_standard_clause 对齐）
#
# ============================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ES_HOST="${ES_HOST:-http://localhost:9200}"
ES_INDEX="${ES_STANDARD_INDEX:-quality-standard-clauses}"
MAPPING_FILE="${SCRIPT_DIR}/init-es-index-mapping.json"

CURL_AUTH=()
if [ -n "${ES_USERNAME:-}" ] && [ -n "${ES_PASSWORD:-}" ]; then
  CURL_AUTH=(-u "${ES_USERNAME}:${ES_PASSWORD}")
fi

echo "========================================"
echo " Elasticsearch 初始化"
echo " Host:  ${ES_HOST}"
echo " Index: ${ES_INDEX}"
echo "========================================"

echo "[1/3] 检查集群健康..."
curl -s "${CURL_AUTH[@]}" "${ES_HOST%/}/_cluster/health?pretty" | head -5

echo "[2/3] 创建索引 ${ES_INDEX} ..."
STATUS=$(curl -s -o /dev/null -w '%{http_code}' "${CURL_AUTH[@]}" -X HEAD "${ES_HOST%/}/${ES_INDEX}")
if [ "$STATUS" = "200" ]; then
  if [ "${ES_RECREATE:-0}" = "1" ]; then
    echo "  索引已存在，ES_RECREATE=1，删除旧索引..."
    curl -s "${CURL_AUTH[@]}" -X DELETE "${ES_HOST%/}/${ES_INDEX}" | head -c 200
    echo
  else
    echo "  索引已存在，跳过创建（如需重建: ES_RECREATE=1）"
  fi
fi

if [ "$STATUS" != "200" ] || [ "${ES_RECREATE:-0}" = "1" ]; then
  CREATE_RESP=$(curl -s -w '\nHTTP:%{http_code}' "${CURL_AUTH[@]}" \
    -H 'Content-Type: application/json' \
    -X PUT "${ES_HOST%/}/${ES_INDEX}" \
    -d @"${MAPPING_FILE}")
  echo "$CREATE_RESP" | tail -3
fi

echo "[3/3] 灌入 P0 条款种子数据..."
bash "${SCRIPT_DIR}/init-es-seed-data-only.sh"

echo ""
echo "验证:"
curl -s "${CURL_AUTH[@]}" \
  -H 'Content-Type: application/json' \
  -X GET "${ES_HOST%/}/${ES_INDEX}/_count" | head -c 200
echo
echo "========================================"
echo " 初始化完成"
echo " 可选: 配置 Embedding 后执行"
echo "   node backend/scripts/index-p0-clauses.mjs"
echo "========================================"
