-- 质保书查询性能优化：生成时间索引（已有库执行一次即可）
USE ai_quality_control;

ALTER TABLE `qc_quality_cert_data`
  ADD INDEX `idx_generate_time` (`generate_time`);
