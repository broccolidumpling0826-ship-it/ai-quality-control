# 检验记录样例 002 - Q235B / 冷轧板

## 1. 样例定位

| 项目 | 内容 |
| --- | --- |
| 样例编号 | `INSPECT-SAMPLE-002` |
| 来源PDF | `/Users/weixuelei/workspace/aiAgent/ai-project/for-test/std-doc/GB-T-SIM-912-2026_Q235B_cold_rolled_sheet_national.pdf` |
| PDF标准编号 | `GB/T SIM 912-2026` |
| SQL脚本 | `inspect_sample_002_Q235B_cold_rolled_national.sql` |
| 标准类型 | `NATIONAL` |
| 结构化牌号 | `Q235B-QA0623` |
| 记录数量 | `5` |

## 2. 标准录入字段

| 字段 | 值 |
| --- | --- |
| 标准ID | `td_std_02_q235b_cold_rolled_national` |
| 标准编号 | `GB/T SIM 912-2026-QA0623` |
| 标准名称 | `碳素结构钢冷轧薄板及钢带质量要求（QA0623隔离模拟国标）` |
| 品种 | `冷轧板` |
| 牌号 | `Q235B-QA0623` |
| 规格范围 | `厚度0.50mm-3.00mm，宽度600mm-1500mm` |
| 客户 | `空` |
| 生效日期 | `2026-03-01` |
| 失效日期 | `9999-12-31` |

## 3. 结构化指标

| 指标ID | 指标 | 合格下限 | 合格上限 | 让步下限 | 让步上限 | 单位 |
| --- | --- | --- | --- | --- | --- | --- |
| `ind001` | 抗拉强度 | 370 | 510 | 360 | 520 | MPa |
| `ind002` | 延伸率 | 26 | 空 | 空 | 空 | % |
| `ind003` | 屈服强度 | 235 | 空 | 225 | 空 | MPa |
| `ind004` | 厚度公差 | -0.12 | 0.12 | -0.15 | 0.15 | mm |

## 4. 五条检验记录

| 序号 | 场景 | 记录ID | 炉号 | 卷号 | 预期判定 |
| --- | --- | --- | --- | --- | --- |
| 1 | 全部关键指标处于合格范围内 | `td_ins_02_1` | `HT-TD-02-QUAL-001` | `COIL-TD-02-QUAL-001` | `QUALIFIED` |
| 2 | 关键指标取边界值，验证边界包含 | `td_ins_02_2` | `HT-TD-02-EDGE-001` | `COIL-TD-02-EDGE-001` | `QUALIFIED` |
| 3 | 首个关键指标轻微超出合格范围但位于让步范围内 | `td_ins_02_3` | `HT-TD-02-CONC-001` | `COIL-TD-02-CONC-001` | `CAN_CONCESSION` |
| 4 | 第二个关键指标超限且该方向未配置让步范围 | `td_ins_02_4` | `HT-TD-02-REINS-001` | `COIL-TD-02-REINS-001` | `NEED_REINSPECTION` |
| 5 | 首个关键指标超出让步范围 | `td_ins_02_5` | `HT-TD-02-UNQ-001` | `COIL-TD-02-UNQ-001` | `UNQUALIFIED` |

检验值明细：

| 记录ID | 抗拉强度 | 延伸率 | 屈服强度 | 厚度公差 |
| --- | --- | --- | --- | --- |
| `td_ins_02_1` | 440 | 28 | 260 | 0 |
| `td_ins_02_2` | 370 | 26 | 235 | -0.12 |
| `td_ins_02_3` | 365 | 28 | 260 | 0 |
| `td_ins_02_4` | 440 | 25.5 | 260 | 0 |
| `td_ins_02_5` | 349.99 | 28 | 260 | 0 |

## 5. 使用说明

1. 直接执行本样例SQL可插入标准、指标、检验记录和检验值。
2. 直接SQL插入不会调用后端 `InspectionService.addRecord()`，因此不会自动生成 `qc_judgment_result`。
3. 如需验证规则引擎自动判定，请先执行本SQL中的标准和字典数据，随后按下方API payload通过页面或接口新增检验记录。
4. 结构化牌号统一使用 `QA0623` 后缀，PDF正文中的原牌号仅代表来源材料。

## 6. API Payloads

### td_ins_02_1

```json
{
  "heatNo": "HT-TD-02-QUAL-001",
  "coilNo": "COIL-TD-02-QUAL-001",
  "customerId": null,
  "productVariety": "冷轧板",
  "productGrade": "Q235B-QA0623",
  "productSpec": "厚度1.50mm，宽度1000mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-23 09:06:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind001",
      "testValue": 440.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 28.0
    },
    {
      "indicatorId": "ind003",
      "testValue": 260.0
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    }
  ]
}
```

### td_ins_02_2

```json
{
  "heatNo": "HT-TD-02-EDGE-001",
  "coilNo": "COIL-TD-02-EDGE-001",
  "customerId": null,
  "productVariety": "冷轧板",
  "productGrade": "Q235B-QA0623",
  "productSpec": "厚度1.50mm，宽度1000mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-23 10:06:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind001",
      "testValue": 370.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 26.0
    },
    {
      "indicatorId": "ind003",
      "testValue": 235.0
    },
    {
      "indicatorId": "ind004",
      "testValue": -0.12
    }
  ]
}
```

### td_ins_02_3

```json
{
  "heatNo": "HT-TD-02-CONC-001",
  "coilNo": "COIL-TD-02-CONC-001",
  "customerId": null,
  "productVariety": "冷轧板",
  "productGrade": "Q235B-QA0623",
  "productSpec": "厚度1.50mm，宽度1000mm",
  "sampleType": "TAIL",
  "testTime": "2026-06-23 11:06:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind001",
      "testValue": 365.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 28.0
    },
    {
      "indicatorId": "ind003",
      "testValue": 260.0
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    }
  ]
}
```

### td_ins_02_4

```json
{
  "heatNo": "HT-TD-02-REINS-001",
  "coilNo": "COIL-TD-02-REINS-001",
  "customerId": null,
  "productVariety": "冷轧板",
  "productGrade": "Q235B-QA0623",
  "productSpec": "厚度1.50mm，宽度1000mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-23 12:06:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind001",
      "testValue": 440.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 25.5
    },
    {
      "indicatorId": "ind003",
      "testValue": 260.0
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    }
  ]
}
```

### td_ins_02_5

```json
{
  "heatNo": "HT-TD-02-UNQ-001",
  "coilNo": "COIL-TD-02-UNQ-001",
  "customerId": null,
  "productVariety": "冷轧板",
  "productGrade": "Q235B-QA0623",
  "productSpec": "厚度1.50mm，宽度1000mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-23 13:06:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind001",
      "testValue": 349.99
    },
    {
      "indicatorId": "ind002",
      "testValue": 28.0
    },
    {
      "indicatorId": "ind003",
      "testValue": 260.0
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    }
  ]
}
```
