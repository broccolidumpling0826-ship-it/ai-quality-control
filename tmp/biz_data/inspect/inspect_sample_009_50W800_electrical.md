# 检验记录样例 009 - 50W800 / 冷轧无取向电工钢

## 1. 样例定位

| 项目 | 内容 |
| --- | --- |
| 样例编号 | `INSPECT-SAMPLE-009` |
| 来源PDF | `/Users/weixuelei/workspace/aiAgent/ai-project/for-test/std-doc/GB-T-SIM-2521-2026_50W800_electrical_steel_national.pdf` |
| PDF标准编号 | `GB/T SIM 2521-2026` |
| SQL脚本 | `inspect_sample_009_50W800_electrical.sql` |
| 标准类型 | `NATIONAL` |
| 结构化牌号 | `50W800-QA0623` |
| 记录数量 | `5` |

## 2. 标准录入字段

| 字段 | 值 |
| --- | --- |
| 标准ID | `td_std_09_50w800_electrical` |
| 标准编号 | `GB/T SIM 2521-2026-QA0623` |
| 标准名称 | `50W800冷轧无取向电工钢质量要求（QA0623隔离模拟国标）` |
| 品种 | `冷轧无取向电工钢` |
| 牌号 | `50W800-QA0623` |
| 规格范围 | `厚度0.47mm-0.53mm，宽度900mm-1250mm` |
| 客户 | `空` |
| 生效日期 | `2026-04-01` |
| 失效日期 | `9999-12-31` |

## 3. 结构化指标

| 指标ID | 指标 | 合格下限 | 合格上限 | 让步下限 | 让步上限 | 单位 |
| --- | --- | --- | --- | --- | --- | --- |
| `td_ind_p15_50` | 铁损P15/50 | 空 | 8 | 空 | 8.3 | W/kg |
| `td_ind_b50` | 磁感B50 | 1.67 | 空 | 空 | 空 | T |
| `ind004` | 厚度公差 | -0.03 | 0.03 | -0.04 | 0.04 | mm |
| `ind007` | 硬度 | 130 | 210 | 120 | 220 | HV |

## 4. 五条检验记录

| 序号 | 场景 | 记录ID | 炉号 | 卷号 | 预期判定 |
| --- | --- | --- | --- | --- | --- |
| 1 | 全部关键指标处于合格范围内 | `td_ins_09_1` | `HT-TD-09-QUAL-001` | `COIL-TD-09-QUAL-001` | `QUALIFIED` |
| 2 | 关键指标取边界值，验证边界包含 | `td_ins_09_2` | `HT-TD-09-EDGE-001` | `COIL-TD-09-EDGE-001` | `QUALIFIED` |
| 3 | 首个关键指标轻微超出合格范围但位于让步范围内 | `td_ins_09_3` | `HT-TD-09-CONC-001` | `COIL-TD-09-CONC-001` | `CAN_CONCESSION` |
| 4 | 第二个关键指标超限且该方向未配置让步范围 | `td_ins_09_4` | `HT-TD-09-REINS-001` | `COIL-TD-09-REINS-001` | `NEED_REINSPECTION` |
| 5 | 首个关键指标超出让步范围 | `td_ins_09_5` | `HT-TD-09-UNQ-001` | `COIL-TD-09-UNQ-001` | `UNQUALIFIED` |

检验值明细：

| 记录ID | 铁损P15/50 | 磁感B50 | 厚度公差 | 硬度 |
| --- | --- | --- | --- | --- |
| `td_ins_09_1` | 7.8 | 1.87 | 0 | 170 |
| `td_ins_09_2` | 8 | 1.67 | -0.03 | 130 |
| `td_ins_09_3` | 8.15 | 1.87 | 0 | 170 |
| `td_ins_09_4` | 7.8 | 1.17 | 0 | 170 |
| `td_ins_09_5` | 8.61 | 1.87 | 0 | 170 |

## 5. 使用说明

1. 直接执行本样例SQL可插入标准、指标、检验记录和检验值。
2. 直接SQL插入不会调用后端 `InspectionService.addRecord()`，因此不会自动生成 `qc_judgment_result`。
3. 如需验证规则引擎自动判定，请先执行本SQL中的标准和字典数据，随后按下方API payload通过页面或接口新增检验记录。
4. 结构化牌号统一使用 `QA0623` 后缀，PDF正文中的原牌号仅代表来源材料。

## 6. API Payloads

### td_ins_09_1

```json
{
  "heatNo": "HT-TD-09-QUAL-001",
  "coilNo": "COIL-TD-09-QUAL-001",
  "customerId": null,
  "productVariety": "冷轧无取向电工钢",
  "productGrade": "50W800-QA0623",
  "productSpec": "厚度0.50mm，宽度1200mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-25 09:27:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "td_ind_p15_50",
      "testValue": 7.8
    },
    {
      "indicatorId": "td_ind_b50",
      "testValue": 1.87
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    },
    {
      "indicatorId": "ind007",
      "testValue": 170.0
    }
  ]
}
```

### td_ins_09_2

```json
{
  "heatNo": "HT-TD-09-EDGE-001",
  "coilNo": "COIL-TD-09-EDGE-001",
  "customerId": null,
  "productVariety": "冷轧无取向电工钢",
  "productGrade": "50W800-QA0623",
  "productSpec": "厚度0.50mm，宽度1200mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-25 10:27:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "td_ind_p15_50",
      "testValue": 8.0
    },
    {
      "indicatorId": "td_ind_b50",
      "testValue": 1.67
    },
    {
      "indicatorId": "ind004",
      "testValue": -0.03
    },
    {
      "indicatorId": "ind007",
      "testValue": 130.0
    }
  ]
}
```

### td_ins_09_3

```json
{
  "heatNo": "HT-TD-09-CONC-001",
  "coilNo": "COIL-TD-09-CONC-001",
  "customerId": null,
  "productVariety": "冷轧无取向电工钢",
  "productGrade": "50W800-QA0623",
  "productSpec": "厚度0.50mm，宽度1200mm",
  "sampleType": "TAIL",
  "testTime": "2026-06-25 11:27:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "td_ind_p15_50",
      "testValue": 8.15
    },
    {
      "indicatorId": "td_ind_b50",
      "testValue": 1.87
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    },
    {
      "indicatorId": "ind007",
      "testValue": 170.0
    }
  ]
}
```

### td_ins_09_4

```json
{
  "heatNo": "HT-TD-09-REINS-001",
  "coilNo": "COIL-TD-09-REINS-001",
  "customerId": null,
  "productVariety": "冷轧无取向电工钢",
  "productGrade": "50W800-QA0623",
  "productSpec": "厚度0.50mm，宽度1200mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-25 12:27:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "td_ind_p15_50",
      "testValue": 7.8
    },
    {
      "indicatorId": "td_ind_b50",
      "testValue": 1.17
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    },
    {
      "indicatorId": "ind007",
      "testValue": 170.0
    }
  ]
}
```

### td_ins_09_5

```json
{
  "heatNo": "HT-TD-09-UNQ-001",
  "coilNo": "COIL-TD-09-UNQ-001",
  "customerId": null,
  "productVariety": "冷轧无取向电工钢",
  "productGrade": "50W800-QA0623",
  "productSpec": "厚度0.50mm，宽度1200mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-25 13:27:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "td_ind_p15_50",
      "testValue": 8.61
    },
    {
      "indicatorId": "td_ind_b50",
      "testValue": 1.87
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    },
    {
      "indicatorId": "ind007",
      "testValue": 170.0
    }
  ]
}
```
