-- 修复 QC_CUSTOMER 字典乱码（须使用 utf8mb4 执行）
-- mysql -h127.0.0.1 -P3307 -uai_quality_control -p --default-character-set=utf8mb4 ai_quality_control < 本文件

USE ai_quality_control;

SET NAMES utf8mb4;

UPDATE sys_dict
SET dict_name = '关联客户',
    update_date_time = NOW()
WHERE dict_code = 'QC_CUSTOMER';

UPDATE sys_dict_item
SET item_label = '华东汽车配件有限公司',
    update_date_time = NOW()
WHERE id = 'di161' AND dict_code = 'QC_CUSTOMER';

UPDATE sys_dict_item
SET item_label = '西南建材集团',
    update_date_time = NOW()
WHERE id = 'di162' AND dict_code = 'QC_CUSTOMER';
