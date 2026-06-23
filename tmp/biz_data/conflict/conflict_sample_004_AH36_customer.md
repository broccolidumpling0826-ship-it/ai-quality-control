# 冲突标准样例 004 - AH36 / 船体结构钢板

| 项目 | 内容 |
| --- | --- |
| 样例编号 | `CONFLICT-SAMPLE-004` |
| 来源PDF | `/Users/weixuelei/workspace/aiAgent/ai-project/for-test/std-doc/CUST-ZYSHIP-SIM-2026-AH36_customer_agreement.pdf` |
| SQL脚本 | `conflict_sample_004_AH36_customer.sql` |
| 客户 | `TD-CUST-ZYSHIP-CF / 中远船务冲突测试客户` |
| 品种 | `船体结构钢板` |
| 牌号 | `AH36-QA0623` |
| 冲突编号 | `TD-SCF-004` |
| 预期 | `STANDARD_CONFLICT / BLOCKING / PENDING` |

## 1. 冲突标准

| 字段 | V1基准协议 | V2冲突协议 |
| --- | --- | --- |
| 标准ID | `td_conf_04_v1` | `td_conf_04_v2` |
| 标准编号 | `AGREEMENT ZYSHIP-SIM-2026-004-QA0623` | `AGREEMENT ZYSHIP-SIM-2026-004-REV-CF-QA0623` |
| 有效期 | `2026-03-01 至 2026-12-31` | `2026-04-15 至 2026-12-31` |
| 规格范围 | `厚度8.00mm-40.00mm，宽度1500mm-3000mm` | `厚度8.00mm-40.00mm，宽度1500mm-3000mm` |

## 2. 限值差异

| 指标 | V1下限 | V1上限 | V2下限 | V2上限 | 单位 |
| --- | --- | --- | --- | --- | --- |
| 屈服强度 | 355 | 空 | 365 | 空 | MPa |
| 抗拉强度 | 490 | 620 | 500 | 615 | MPa |
| 延伸率 | 21 | 空 | 22 | 空 | % |
| 冲击功KV2 | 34 | 空 | 35 | 空 | J |

## 3. 触发检验记录

| 记录ID | `td_conf_ins_04` |
| --- | --- |
| 炉号 | `HT-TD-CF-04-001` |
| 卷号 | `COIL-TD-CF-04-001` |
| 客户 | `TD-CUST-ZYSHIP-CF` |
| 品种/牌号 | `船体结构钢板 / AH36-QA0623` |
| 规格 | `厚度18.00mm，宽度2200mm` |

| 指标 | 实测值 | 单位 |
| --- | --- | --- |
| 屈服强度 | 360 | MPa |
| 抗拉强度 | 555 | MPa |
| 延伸率 | 23 | % |
| 冲击功KV2 | 36 | J |

## 4. 验证点

1. 标准冲突列表出现对应 `TD-SCF-*` 编号。
2. 冲突等级为 `BLOCKING`，状态为 `PENDING`。
3. 涉及标准包含V1和V2两份客户协议。
4. 正式质保书或最终放行流程应被阻断，直到人工裁决控制标准。
5. 本样例使用 `TD-CUST-*-CF` 客户和 `QA0623` 牌号后缀，避免影响普通100条检验记录。
