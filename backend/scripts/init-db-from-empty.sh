#!/bin/bash
# ============================================================
# 空库完整初始化：DDL + 种子数据
# 用法: bash backend/scripts/init-db-from-empty.sh
# 环境变量:
#   DB_HOST (默认 127.0.0.1)
#   DB_PORT (默认 3307)
#   DB_USER (默认 root)
#   DB_PASSWORD (必填，或通过交互输入)
# ============================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3307}"
DB_USER="${DB_USER:-root}"

if [ -z "${DB_PASSWORD:-}" ]; then
  echo "请输入 MySQL 密码（或设置 DB_PASSWORD 环境变量）："
  read -rs DB_PASSWORD
  echo
fi

MYSQL=(mysql -u "$DB_USER" -p"$DB_PASSWORD" -h "$DB_HOST" -P "$DB_PORT")

run_sql() {
  local file="$1"
  local label="$2"
  echo "  → $label"
  "${MYSQL[@]}" < "$file"
}

echo "========================================"
echo " 空库完整初始化 (DDL + Seed Data)"
echo " Host: $DB_HOST:$DB_PORT  User: $DB_USER"
echo "========================================"

echo "[1/4] 核心业务表结构..."
run_sql "$SCRIPT_DIR/init-schema.sql" "init-schema.sql"

echo "[2/4] RBAC 表结构与菜单权限种子..."
run_sql "$SCRIPT_DIR/init-menu-rbac.sql" "init-menu-rbac.sql"

echo "[3/4] AI 增强表结构..."
run_sql "$SCRIPT_DIR/migrations/20260621_01_ai_quality_p0_schema.sql" "20260621_01_ai_quality_p0_schema.sql"

echo "[4/4] 业务种子数据..."
run_sql "$SCRIPT_DIR/init-seed-data-only.sql" "init-seed-data-only.sql"

echo ""
echo "========================================"
echo " 初始化完成"
echo " 前端: http://localhost:5173"
echo " API:  http://localhost:8080/doc.html"
echo " 账号: admin / Admin123456"
echo "========================================"
