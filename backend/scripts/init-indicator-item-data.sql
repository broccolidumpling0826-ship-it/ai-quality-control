-- ============================================================
-- 指标项目初始化（仅 qc_indicator_item INSERT）
-- 用法: mysql -u root -p ai_quality_control < init-indicator-item-data.sql
-- ============================================================

USE ai_quality_control;

INSERT IGNORE INTO qc_indicator_item (
  id, indicator_name, indicator_code, indicator_category, unit,
  test_method, description, status, is_deleted,
  create_date_time, update_date_time
) VALUES
('ind001', '抗拉强度', 'Rm',  'PERFORMANCE', 'MPa', 'GB/T 228.1', '最大拉伸力/原始横截面积', 'ACTIVE', 0, NOW(), NOW()),
('ind002', '延伸率',   'A',   'PERFORMANCE', '%',   'GB/T 228.1', '断后伸长量/原始标距长度',  'ACTIVE', 0, NOW(), NOW()),
('ind003', '屈服强度', 'ReL', 'PERFORMANCE', 'MPa', 'GB/T 228.1', '屈服点的应力值',          'ACTIVE', 0, NOW(), NOW()),
('ind004', '厚度公差', 'Δt',  'DIMENSION',   'mm',  NULL,         '实测厚度与名义厚度之差',  'ACTIVE', 0, NOW(), NOW()),
('ind005', '宽度公差', 'Δw',  'DIMENSION',   'mm',  NULL,         '实测宽度与名义宽度之差',  'ACTIVE', 0, NOW(), NOW()),
('ind006', '表面等级', 'FS',  'SURFACE',     '-',   NULL,         'FB/FC/FD 表面质量等级',   'ACTIVE', 0, NOW(), NOW()),
('ind007', '硬度',     'HV',  'PERFORMANCE', 'HV',  'GB/T 4340.1','Vickers 硬度',            'ACTIVE', 0, NOW(), NOW()),
('ind008', 'n值',      'n',   'PERFORMANCE', '-',   'GB/T 5027',  '应变硬化指数',            'ACTIVE', 0, NOW(), NOW());
