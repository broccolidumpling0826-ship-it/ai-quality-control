# 100份检验记录样例索引

结构化牌号统一使用 `QA0623` 后缀，PDF正文中的原牌号仅作为来源材料。

| 序号 | PDF | SQL | 品种 | 结构化牌号 | 客户 | 记录数 |
| --- | --- | --- | --- | --- | --- | --- |
| 001 | `GB-T-SIM-1499-2026_HRB400E_rebar_national.pdf` | `inspect_sample_001_HRB400E_rebar.sql` | 热轧带肋钢筋 | `HRB400E-QA0623` | `空` | 5 |
| 002 | `GB-T-SIM-912-2026_Q235B_cold_rolled_sheet_national.pdf` | `inspect_sample_002_Q235B_cold_rolled_national.sql` | 冷轧板 | `Q235B-QA0623` | `空` | 5 |
| 003 | `GB-T-SIM-3274-2026_Q355B_hot_rolled_plate_national.pdf` | `inspect_sample_003_Q355B_hot_rolled_plate.sql` | 热轧板 | `Q355B-QA0623` | `空` | 5 |
| 004 | `GB-T-SIM-2518-2026_DX51D-Z_galvanized_sheet_national.pdf` | `inspect_sample_004_DX51D_Z_galvanized.sql` | 镀锌板 | `DX51D+Z-QA0623` | `空` | 5 |
| 005 | `GB-T-SIM-4237-2026_06Cr19Ni10_stainless_sheet_national.pdf` | `inspect_sample_005_06Cr19Ni10_stainless.sql` | 冷轧不锈钢板 | `06Cr19Ni10-QA0623` | `空` | 5 |
| 006 | `GB-T-SIM-713-2026_Q345R_pressure_vessel_plate_national.pdf` | `inspect_sample_006_Q345R_pressure_vessel.sql` | 压力容器钢板 | `Q345R-QA0623` | `空` | 5 |
| 007 | `GB-T-SIM-9711-2026_L245M_pipeline_steel_national.pdf` | `inspect_sample_007_L245M_pipeline.sql` | 管线钢板卷 | `L245M-QA0623` | `空` | 5 |
| 008 | `GB-T-SIM-5213-2026_DC04_deep_drawing_sheet_national.pdf` | `inspect_sample_008_DC04_deep_drawing.sql` | 深冲冷轧板 | `DC04-QA0623` | `空` | 5 |
| 009 | `GB-T-SIM-2521-2026_50W800_electrical_steel_national.pdf` | `inspect_sample_009_50W800_electrical.sql` | 冷轧无取向电工钢 | `50W800-QA0623` | `空` | 5 |
| 010 | `GB-T-SIM-1222-2026_60Si2Mn_spring_flat_steel_national.pdf` | `inspect_sample_010_60Si2Mn_spring.sql` | 热轧弹簧扁钢 | `60Si2Mn-QA0623` | `空` | 5 |
| 011 | `Q-JH-STEEL-SIM-2026-Q345B_hot_rolled_plate_enterprise.pdf` | `inspect_sample_011_Q345B_enterprise.sql` | 热轧板 | `Q345B-QA0623` | `空` | 5 |
| 012 | `Q-JH-STEEL-SIM-020-2026_SPFH590_pickled_strip_enterprise.pdf` | `inspect_sample_012_SPFH590_pickled.sql` | 酸洗热轧钢带 | `SPFH590-QA0623` | `空` | 5 |
| 013 | `Q-JH-STEEL-SIM-030-2026_HC340LA_cold_rolled_enterprise.pdf` | `inspect_sample_013_HC340LA_enterprise.sql` | 冷轧低合金高强钢板 | `HC340LA-QA0623` | `空` | 5 |
| 014 | `Q-JH-STEEL-SIM-040-2026_NM400_wear_plate_enterprise.pdf` | `inspect_sample_014_NM400_wear_plate.sql` | 调质耐磨钢板 | `NM400-QA0623` | `空` | 5 |
| 015 | `Q-JH-STEEL-SIM-050-2026_S355J2_wind_tower_plate_enterprise.pdf` | `inspect_sample_015_S355J2_wind_tower.sql` | 风电塔筒用中厚板 | `S355J2-QA0623` | `空` | 5 |
| 016 | `CUST-XNJC-SIM-2026-Q235B_customer_agreement.pdf` | `inspect_sample_016_Q235B_customer_XNJC.sql` | 冷轧板 | `Q235B-QA0623` | `TD-CUST-XNJC` | 5 |
| 017 | `CUST-HDQC-SIM-2026-HC340LA_customer_agreement.pdf` | `inspect_sample_017_HC340LA_customer_HDQC.sql` | 冷轧低合金高强钢板 | `HC340LA-QA0623` | `TD-CUST-HDQC` | 5 |
| 018 | `CUST-BYEV-SIM-2026-DX56D-Z_customer_agreement.pdf` | `inspect_sample_018_DX56D_Z_customer_BYEV.sql` | 热镀锌深冲板 | `DX56D+Z-QA0623` | `TD-CUST-BYEV` | 5 |
| 019 | `CUST-ZYSHIP-SIM-2026-AH36_customer_agreement.pdf` | `inspect_sample_019_AH36_customer_ZYSHIP.sql` | 船体结构钢板 | `AH36-QA0623` | `TD-CUST-ZYSHIP` | 5 |
| 020 | `CUST-HXPIPE-SIM-2026-L360M_customer_agreement.pdf` | `inspect_sample_020_L360M_customer_HXPIPE.sql` | 管线钢热轧卷 | `L360M-QA0623` | `TD-CUST-HXPIPE` | 5 |

## 判定分布

每份PDF包含：2条 `QUALIFIED`、1条 `CAN_CONCESSION`、1条 `NEED_REINSPECTION`、1条 `UNQUALIFIED`。
合计100条检验记录。直接执行SQL不会自动生成判定结果；如需验证自动判定，请使用各MD中的API payload录入。
