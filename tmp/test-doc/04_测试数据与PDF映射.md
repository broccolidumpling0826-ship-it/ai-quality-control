# 测试数据与PDF映射

## 1. 标准PDF目录

```text
/Users/weixuelei/workspace/aiAgent/ai-project/for-test/std-doc
```

## 2. 20份PDF覆盖清单

| 类型 | PDF | 建议标准编号 | 品种 | 牌号 |
| --- | --- | --- | --- | --- |
| 国标 | `GB-T-SIM-912-2026_Q235B_cold_rolled_sheet_national.pdf` | `GB/T SIM 912-2026` | 冷轧板 | Q235B |
| 国标 | `GB-T-SIM-1499-2026_HRB400E_rebar_national.pdf` | `GB/T SIM 1499-2026` | 热轧带肋钢筋 | HRB400E |
| 国标 | `GB-T-SIM-3274-2026_Q355B_hot_rolled_plate_national.pdf` | `GB/T SIM 3274-2026` | 热轧板 | Q355B |
| 国标 | `GB-T-SIM-2518-2026_DX51D-Z_galvanized_sheet_national.pdf` | `GB/T SIM 2518-2026` | 镀锌板 | DX51D+Z |
| 国标 | `GB-T-SIM-4237-2026_06Cr19Ni10_stainless_sheet_national.pdf` | `GB/T SIM 4237-2026` | 冷轧不锈钢板 | 06Cr19Ni10 |
| 国标 | `GB-T-SIM-713-2026_Q345R_pressure_vessel_plate_national.pdf` | `GB/T SIM 713-2026` | 压力容器钢板 | Q345R |
| 国标 | `GB-T-SIM-9711-2026_L245M_pipeline_steel_national.pdf` | `GB/T SIM 9711-2026` | 管线钢板卷 | L245M |
| 国标 | `GB-T-SIM-5213-2026_DC04_deep_drawing_sheet_national.pdf` | `GB/T SIM 5213-2026` | 深冲冷轧板 | DC04 |
| 国标 | `GB-T-SIM-2521-2026_50W800_electrical_steel_national.pdf` | `GB/T SIM 2521-2026` | 冷轧无取向电工钢 | 50W800 |
| 国标 | `GB-T-SIM-1222-2026_60Si2Mn_spring_flat_steel_national.pdf` | `GB/T SIM 1222-2026` | 热轧弹簧扁钢 | 60Si2Mn |
| 企标 | `Q-JH-STEEL-SIM-2026-Q345B_hot_rolled_plate_enterprise.pdf` | `Q/JH STEEL SIM 010-2026` | 热轧板 | Q345B |
| 企标 | `Q-JH-STEEL-SIM-020-2026_SPFH590_pickled_strip_enterprise.pdf` | `Q/JH STEEL SIM 020-2026` | 酸洗热轧钢带 | SPFH590 |
| 企标 | `Q-JH-STEEL-SIM-030-2026_HC340LA_cold_rolled_enterprise.pdf` | `Q/JH STEEL SIM 030-2026` | 冷轧低合金高强钢板 | HC340LA |
| 企标 | `Q-JH-STEEL-SIM-040-2026_NM400_wear_plate_enterprise.pdf` | `Q/JH STEEL SIM 040-2026` | 调质耐磨钢板 | NM400 |
| 企标 | `Q-JH-STEEL-SIM-050-2026_S355J2_wind_tower_plate_enterprise.pdf` | `Q/JH STEEL SIM 050-2026` | 风电塔筒用中厚板 | S355J2 |
| 客户协议 | `CUST-XNJC-SIM-2026-Q235B_customer_agreement.pdf` | `AGREEMENT XNJC-SIM-2026-001` | 冷轧板 | Q235B |
| 客户协议 | `CUST-HDQC-SIM-2026-HC340LA_customer_agreement.pdf` | `AGREEMENT HDQC-SIM-2026-002` | 冷轧低合金高强钢板 | HC340LA |
| 客户协议 | `CUST-BYEV-SIM-2026-DX56D-Z_customer_agreement.pdf` | `AGREEMENT BYEV-SIM-2026-003` | 热镀锌深冲板 | DX56D+Z |
| 客户协议 | `CUST-ZYSHIP-SIM-2026-AH36_customer_agreement.pdf` | `AGREEMENT ZYSHIP-SIM-2026-004` | 船体结构钢板 | AH36 |
| 客户协议 | `CUST-HXPIPE-SIM-2026-L360M_customer_agreement.pdf` | `AGREEMENT HXPIPE-SIM-2026-005` | 管线钢热轧卷 | L360M |

## 3. 核心4场景标准

说明：上表“20份PDF覆盖清单”描述PDF文件本身的模拟内容；核心人工测试为了避开系统已有脏数据，结构化标准录入时使用隔离牌号 `Q235B-QA0622`，PDF仍复用Q235B正文作为来源文档。

历史脏数据避让规则：核心4场景不要使用 `STD001`、`STD002`、`STD003`、`协议C2025-088-v2-冲突样例`、`Q/ZX-STEEL-2026-Q345B` 或 `CUST-001`；按下表录入时以 `QA0622` 后缀标准编号、`Q235B-QA0622` 牌号、`CUST-002` 客户为准。

### STD-N-Q235B-001 国标

| 字段 | 值 |
| --- | --- |
| 标准类型 | NATIONAL |
| 标准编号 | `GB/T SIM 912-2026-QA0622` |
| 标准名称 | `碳素结构钢冷轧薄板及钢带质量要求（模拟国标-隔离测试）` |
| 品种 | `冷轧板` |
| 牌号 | `Q235B-QA0622` |
| 规格范围 | `厚度0.50mm-3.00mm，宽度600mm-1500mm` |
| 版本 | `2026版` |
| 生效日期 | `2026-03-01` |
| 失效日期 | `9999-12-31` |
| 客户 | 空 |
| PDF | `GB-T-SIM-912-2026_Q235B_cold_rolled_sheet_national.pdf` |

指标：

| 指标 | 合格下限 | 合格上限 | 让步下限 | 让步上限 |
| --- | --- | --- | --- | --- |
| Rm 抗拉强度 | 370 | 510 | 360 | 520 |
| A 延伸率 | 26 | 空 | 24 | 空 |
| ReL 屈服强度 | 235 | 空 | 空 | 空 |
| Δt 厚度公差 | -0.120 | 0.120 | -0.150 | 0.150 |

### STD-C-XNJC-Q235B-001 客户协议V1

| 字段 | 值 |
| --- | --- |
| 标准类型 | CUSTOMER |
| 标准编号 | `AGREEMENT XNJC-SIM-2026-001-QA0622` |
| 标准名称 | `西南建材集团Q235B冷轧板供货质量协议（模拟客户协议-隔离测试）` |
| 客户 | `CUST-002` |
| 品种 | `冷轧板` |
| 牌号 | `Q235B-QA0622` |
| 规格范围 | `厚度0.80mm-2.50mm，宽度800mm-1250mm` |
| 版本 | `协议V1.0` |
| 生效日期 | `2026-02-01` |
| 失效日期 | `2026-12-31` |
| PDF | `CUST-XNJC-SIM-2026-Q235B_customer_agreement.pdf` |

指标：

| 指标 | 合格下限 | 合格上限 | 让步下限 | 让步上限 |
| --- | --- | --- | --- | --- |
| Rm 抗拉强度 | 375 | 505 | 370 | 510 |
| A 延伸率 | 27 | 空 | 25 | 空 |
| ReL 屈服强度 | 235 | 360 | 空 | 空 |
| Δt 厚度公差 | -0.080 | 0.080 | -0.100 | 0.100 |

### STD-C-XNJC-Q235B-REV-CF 客户协议V2冲突样本

| 字段 | 值 |
| --- | --- |
| 标准类型 | CUSTOMER |
| 标准编号 | `AGREEMENT XNJC-SIM-2026-001-REV-CF-QA0622` |
| 标准名称 | `西南建材集团Q235B冷轧板供货质量协议冲突版（人工测试-隔离测试）` |
| 客户 | `CUST-002` |
| 品种 | `冷轧板` |
| 牌号 | `Q235B-QA0622` |
| 规格范围 | `厚度0.80mm-2.50mm，宽度800mm-1250mm` |
| 版本 | `协议V1.1-CF` |
| 生效日期 | `2026-04-01` |
| 失效日期 | `2026-12-31` |
| PDF | 可复用 `CUST-XNJC-SIM-2026-Q235B_customer_agreement.pdf` |

指标：

| 指标 | 合格下限 | 合格上限 | 让步下限 | 让步上限 |
| --- | --- | --- | --- | --- |
| Rm 抗拉强度 | 390 | 505 | 382 | 510 |
| A 延伸率 | 28 | 空 | 26 | 空 |
| ReL 屈服强度 | 235 | 360 | 空 | 空 |
| Δt 厚度公差 | -0.080 | 0.080 | -0.100 | 0.100 |

## 4. 核心检验数据

| 场景 | 炉号 | 卷号 | 客户 | 品种 | 牌号 | 规格 | Rm | A | ReL | Δt | 预期 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 合格 | `HT-MAN-QUAL-001` | `FT-QUAL-001` | 空 | 冷轧板 | Q235B-QA0622 | `厚度0.50mm-3.00mm，宽度600mm-1500mm` | 430 | 30.5 | 280 | 0.05 | QUALIFIED |
| 不合格 | `HT-MAN-UNQ-001` | `FT-UNQUAL-001` | CUST-002 | 冷轧板 | Q235B-QA0622 | `厚度0.80mm-2.50mm，宽度800mm-1250mm` | 360 | 27 | 260 | 0.03 | UNQUALIFIED |
| 可让步 | `HT-MAN-CONC-001` | `FT-CONC-001` | CUST-002 | 冷轧板 | Q235B-QA0622 | `厚度0.80mm-2.50mm，宽度800mm-1250mm` | 372 | 26 | 250 | 0.09 | CAN_CONCESSION |
| 标准冲突 | `HT-MAN-CF-001` | `FT-CONFLICT-001` | CUST-002 | 冷轧板 | Q235B-QA0622 | `厚度0.80mm-2.50mm，宽度800mm-1250mm` | 382 | 28 | 270 | 0.02 | STANDARD_CONFLICT |

## 5. OpenSpec种子数据对照

如果想先快速验证已有P0种子链路，可直接使用：

| 场景 | 卷号 | 判定ID | 预期 |
| --- | --- | --- | --- |
| 合格 | `Z001001` | `jud001` | QUALIFIED |
| 不合格 | `Z002001` | `jud002` | UNQUALIFIED |
| 可让步 | `Z003001` | `jud003` | CAN_CONCESSION |
| 标准冲突 | `Z004001` | `jud004` | STANDARD_CONFLICT |

这套种子数据引用的是OpenSpec P0准备条款；本测试包核心用例引用的是新生成PDF源文件。
