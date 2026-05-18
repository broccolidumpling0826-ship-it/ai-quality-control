-- 为已审批通过、但新判定结论尚未复制依据的改判记录补全 qc_judgment_evidence
-- 执行前请备份；改判目标为 QUALIFIED 时指标 is_passed 置为 1

INSERT INTO qc_judgment_evidence (
    id, judgment_id, standard_id, indicator_id, test_value,
    upper_limit, lower_limit, deviation, trigger_rule, is_passed,
    company_id, create_user_no, update_user_no, create_date_time, update_date_time
)
SELECT
    REPLACE(UUID(), '-', ''),
    new_j.id,
    old_e.standard_id,
    old_e.indicator_id,
    old_e.test_value,
    old_e.upper_limit,
    old_e.lower_limit,
    old_e.deviation,
    old_e.trigger_rule,
    CASE WHEN new_j.judgment_type = 'QUALIFIED' THEN 1 ELSE old_e.is_passed END,
    old_e.company_id,
    old_e.create_user_no,
    old_e.update_user_no,
    NOW(),
    NOW()
FROM qc_rejudgment_request req
INNER JOIN qc_judgment_result old_j ON old_j.id = req.original_judgment_id
INNER JOIN qc_judgment_result new_j ON new_j.record_id = old_j.record_id
    AND new_j.is_final = 1
    AND new_j.id <> old_j.id
    AND new_j.remark LIKE CONCAT('改判申请[', req.id, ']%')
INNER JOIN qc_judgment_evidence old_e ON old_e.judgment_id = old_j.id
WHERE req.approval_status = 'APPROVED'
  AND NOT EXISTS (
      SELECT 1 FROM qc_judgment_evidence ne WHERE ne.judgment_id = new_j.id
  );
