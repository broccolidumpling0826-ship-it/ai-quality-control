-- 修复判定解释页品种、规格、触发规则乱码（须使用 utf8mb4 执行）
-- mysql -h127.0.0.1 -P3307 -uai_quality_control -p --default-character-set=utf8mb4 ai_quality_control < 本文件

USE ai_quality_control;

SET NAMES utf8mb4;

-- 1. 品种字典
UPDATE sys_dict_item SET item_label = '冷轧板', item_value = '冷轧板', update_date_time = NOW()
WHERE dict_code = 'PRODUCT_VARIETY' AND id = 'di131';

UPDATE sys_dict_item SET item_label = '热轧板', item_value = '热轧板', update_date_time = NOW()
WHERE dict_code = 'PRODUCT_VARIETY' AND id = 'di132';

UPDATE sys_dict_item SET item_label = '镀锌板', item_value = '镀锌板', update_date_time = NOW()
WHERE dict_code = 'PRODUCT_VARIETY' AND id = 'di133';

-- 2. 检验记录：品种与规格
UPDATE qc_inspection_record
SET product_variety = '冷轧板',
    product_spec = '厚度1.5mm×宽度1000mm',
    update_date_time = NOW()
WHERE product_variety LIKE '%?%'
   OR product_spec LIKE '%?%';

-- 兜底：演示/测试批次统一规格文案
UPDATE qc_inspection_record
SET product_variety = '冷轧板',
    product_spec = '厚度1.5mm×宽度1000mm',
    update_date_time = NOW()
WHERE coil_no LIKE 'Z-DEMO-%'
   OR coil_no LIKE 'Z-BULK-%'
   OR coil_no IN ('Z001001', 'Z002001', 'Z003001');

-- 3. 判定依据：演示场景触发规则
UPDATE qc_judgment_evidence SET trigger_rule = '实测值 430.0 MPa 在国标范围 [370.0, 510.0] 内', update_date_time = NOW() WHERE id = 'demo-je-q1';
UPDATE qc_judgment_evidence SET trigger_rule = '实测值 30.5% ≥ 国标下限 26.0%', update_date_time = NOW() WHERE id = 'demo-je-q2';
UPDATE qc_judgment_evidence SET trigger_rule = '实测值 360.0 MPa 低于客户协议下限 380.0 MPa', update_date_time = NOW() WHERE id = 'demo-je-u1';
UPDATE qc_judgment_evidence SET trigger_rule = '实测值 27.0% 低于客户协议下限 28.0%', update_date_time = NOW() WHERE id = 'demo-je-u2';
UPDATE qc_judgment_evidence SET trigger_rule = '实测值 365.0 MPa 略低于国标下限 370.0 MPa，在让步范围内', update_date_time = NOW() WHERE id = 'demo-je-c1';
UPDATE qc_judgment_evidence SET trigger_rule = '国标判定：375.0 MPa 在 [370.0, 510.0] 内', update_date_time = NOW() WHERE id = 'demo-je-f1';
UPDATE qc_judgment_evidence SET trigger_rule = '客协判定：375.0 MPa 低于下限 380.0 MPa', update_date_time = NOW() WHERE id = 'demo-je-f2';

UPDATE qc_judgment_evidence SET trigger_rule = '实测值 430.0 MPa 在国标范围 [370.0, 510.0] 内', update_date_time = NOW() WHERE id = 'je001';
UPDATE qc_judgment_evidence SET trigger_rule = '实测值 30.5% ≥ 国标下限 26.0%', update_date_time = NOW() WHERE id = 'je002';
UPDATE qc_judgment_evidence SET trigger_rule = '实测值 360.0 MPa 低于客户协议下限 380.0 MPa（协议C2025-088-v1）', update_date_time = NOW() WHERE id = 'je011';
UPDATE qc_judgment_evidence SET trigger_rule = '实测值 27.0% 低于客户协议下限 28.0%（协议C2025-088-v1）', update_date_time = NOW() WHERE id = 'je012';

-- 4. 引擎实时判定产生的触发规则（含 ? 的通用修复）
UPDATE qc_judgment_evidence
SET trigger_rule = CONCAT(
  '实测值 ',
  TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(trigger_rule, 'MPa', 1), ' ', -1)),
  ' MPa 在国标范围 [',
  CAST(lower_limit AS CHAR),
  ', ',
  CAST(upper_limit AS CHAR),
  '] 内'
),
update_date_time = NOW()
WHERE trigger_rule LIKE '%?%'
  AND trigger_rule LIKE '%[%]%'
  AND is_passed = 1
  AND lower_limit IS NOT NULL
  AND upper_limit IS NOT NULL;

UPDATE qc_judgment_evidence
SET trigger_rule = CONCAT(
  '实测值 ',
  TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(trigger_rule, 'MPa', 1), ' ', -1)),
  ' MPa 低于下限 ',
  CAST(lower_limit AS CHAR),
  ' MPa'
),
update_date_time = NOW()
WHERE trigger_rule LIKE '%?%'
  AND trigger_rule LIKE '%MPa%'
  AND is_passed = 0
  AND lower_limit IS NOT NULL
  AND (upper_limit IS NULL OR test_value < lower_limit);
