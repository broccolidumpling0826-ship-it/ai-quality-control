-- Prompt 版本 seed（002）
USE ai_quality_control;

INSERT INTO qc_ai_prompt_version (id, prompt_key, version_no, file_path, is_active, description, create_date_time, update_date_time)
VALUES
('prompt-rag-v100', 'rag-qa', 'v1.0.0', 'prompts/v1.0.0/rag-qa.yaml', 1, '标准RAG问答默认版本', '2026-06-20 00:00:00', '2026-06-20 00:00:00'),
('prompt-jdg-v100', 'judgment-explain', 'v1.0.0', 'prompts/v1.0.0/judgment-explain.yaml', 1, 'AI判定解释默认版本', '2026-06-20 00:00:00', '2026-06-20 00:00:00'),
('prompt-con-v100', 'concession-agent', 'v1.0.0', 'prompts/v1.0.0/concession-agent.yaml', 1, '让步评估默认版本', '2026-06-20 00:00:00', '2026-06-20 00:00:00'),
('prompt-cert-v100', 'cert-summary', 'v1.0.0', 'prompts/v1.0.0/cert-summary.yaml', 1, '质保书说明默认版本', '2026-06-20 00:00:00', '2026-06-20 00:00:00')
ON DUPLICATE KEY UPDATE is_active = VALUES(is_active), update_date_time = VALUES(update_date_time);
