-- ============================================================
-- 空库业务数据初始化（编排入口，已合并为单文件）
-- 请直接使用: init-seed-data-only.sql
-- ============================================================
--
-- 本文件保留为兼容入口，内容与 init-seed-data-only.sql 相同。
-- 推荐用法:
--   mysql -u root -p -h 127.0.0.1 -P 3307 ai_quality_control < init-seed-data-only.sql
--
-- 前置条件（须先执行建表 DDL）:
--   1. init-schema.sql
--   2. init-menu-rbac.sql（仅 DDL 部分，或完整执行后 TRUNCATE 再灌数据）
--   3. migrations/20260621_01_ai_quality_p0_schema.sql
--
-- 一键 DDL + 数据:
--   bash backend/scripts/init-db-from-empty.sh

SOURCE init-seed-data-only.sql;
