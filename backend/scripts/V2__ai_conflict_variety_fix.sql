-- 修复 qc_standard_conflict.variety 因导入时字符集错误导致的乱码
-- 执行：mysql -h 127.0.0.1 -P 3307 -u user -p --default-character-set=utf8mb4 ai_quality_control < backend/scripts/V2__ai_conflict_variety_fix.sql
SET NAMES utf8mb4;
USE ai_quality_control;

UPDATE qc_standard_conflict SET variety = '冷轧板' WHERE grade = 'Q235B';
UPDATE qc_standard_conflict SET variety = '热轧板' WHERE grade = 'SPHC';

SELECT id, variety, grade FROM qc_standard_conflict;
