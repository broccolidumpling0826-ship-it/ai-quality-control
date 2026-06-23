# 检验记录样例 008 - DC04 / 深冲冷轧板

## 1. 样例定位

| 项目 | 内容 |
| --- | --- |
| 样例编号 | `INSPECT-SAMPLE-008` |
| 来源PDF | `/Users/weixuelei/workspace/aiAgent/ai-project/for-test/std-doc/GB-T-SIM-5213-2026_DC04_deep_drawing_sheet_national.pdf` |
| PDF标准编号 | `GB/T SIM 5213-2026` |
| SQL脚本 | `inspect_sample_008_DC04_deep_drawing.sql` |
| 标准类型 | `NATIONAL` |
| 结构化牌号 | `DC04-QA0623` |
| 记录数量 | `5` |

## 2. 标准录入字段

| 字段 | 值 |
| --- | --- |
| 标准ID | `td_std_08_dc04_deep_drawing` |
| 标准编号 | `GB/T SIM 5213-2026-QA0623` |
| 标准名称 | `DC04深冲冷轧板质量要求（QA0623隔离模拟国标）` |
| 品种 | `深冲冷轧板` |
| 牌号 | `DC04-QA0623` |
| 规格范围 | `厚度0.50mm-2.00mm，宽度800mm-1500mm` |
| 客户 | `空` |
| 生效日期 | `2026-04-01` |
| 失效日期 | `9999-12-31` |

## 3. 结构化指标

| 指标ID | 指标 | 合格下限 | 合格上限 | 让步下限 | 让步上限 | 单位 |
| --- | --- | --- | --- | --- | --- | --- |
| `ind003` | 屈服强度 | 140 | 210 | 130 | 220 | MPa |
| `ind001` | 抗拉强度 | 270 | 350 | 空 | 空 | MPa |
| `ind002` | 延伸率 | 38 | 空 | 36 | 空 | % |
| `ind008` | n值 | 0.18 | 空 | 0.16 | 空 | - |

## 4. 五条检验记录

| 序号 | 场景 | 记录ID | 炉号 | 卷号 | 预期判定 |
| --- | --- | --- | --- | --- | --- |
| 1 | 全部关键指标处于合格范围内 | `td_ins_08_1` | `HT-TD-08-QUAL-001` | `COIL-TD-08-QUAL-001` | `QUALIFIED` |
| 2 | 关键指标取边界值，验证边界包含 | `td_ins_08_2` | `HT-TD-08-EDGE-001` | `COIL-TD-08-EDGE-001` | `QUALIFIED` |
| 3 | 首个关键指标轻微超出合格范围但位于让步范围内 | `td_ins_08_3` | `HT-TD-08-CONC-001` | `COIL-TD-08-CONC-001` | `CAN_CONCESSION` |
| 4 | 第二个关键指标超限且该方向未配置让步范围 | `td_ins_08_4` | `HT-TD-08-REINS-001` | `COIL-TD-08-REINS-001` | `NEED_REINSPECTION` |
| 5 | 首个关键指标超出让步范围 | `td_ins_08_5` | `HT-TD-08-UNQ-001` | `COIL-TD-08-UNQ-001` | `UNQUALIFIED` |

检验值明细：

| 记录ID | 屈服强度 | 抗拉强度 | 延伸率 | n值 |
| --- | --- | --- | --- | --- |
| `td_ins_08_1` | 175 | 310 | 40 | 0.2 |
| `td_ins_08_2` | 140 | 270 | 38 | 0.18 |
| `td_ins_08_3` | 135 | 310 | 40 | 0.2 |
| `td_ins_08_4` | 175 | 265 | 40 | 0.2 |
| `td_ins_08_5` | 119.99 | 310 | 40 | 0.2 |

## 5. 使用说明

1. 直接执行本样例SQL可插入标准、指标、检验记录和检验值。
2. 直接SQL插入不会调用后端 `InspectionService.addRecord()`，因此不会自动生成 `qc_judgment_result`。
3. 如需验证规则引擎自动判定，请先执行本SQL中的标准和字典数据，随后按下方API payload通过页面或接口新增检验记录。
4. 结构化牌号统一使用 `QA0623` 后缀，PDF正文中的原牌号仅代表来源材料。

## 6. API Payloads

### td_ins_08_1

```json
{
  "heatNo": "HT-TD-08-QUAL-001",
  "coilNo": "COIL-TD-08-QUAL-001",
  "customerId": null,
  "productVariety": "深冲冷轧板",
  "productGrade": "DC04-QA0623",
  "productSpec": "厚度0.80mm，宽度1250mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-24 09:24:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 175.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 310.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 40.0
    },
    {
      "indicatorId": "ind008",
      "testValue": 0.2
    }
  ]
}
```

### td_ins_08_2

```json
{
  "heatNo": "HT-TD-08-EDGE-001",
  "coilNo": "COIL-TD-08-EDGE-001",
  "customerId": null,
  "productVariety": "深冲冷轧板",
  "productGrade": "DC04-QA0623",
  "productSpec": "厚度0.80mm，宽度1250mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-24 10:24:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 140.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 270.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 38.0
    },
    {
      "indicatorId": "ind008",
      "testValue": 0.18
    }
  ]
}
```

### td_ins_08_3

```json
{
  "heatNo": "HT-TD-08-CONC-001",
  "coilNo": "COIL-TD-08-CONC-001",
  "customerId": null,
  "productVariety": "深冲冷轧板",
  "productGrade": "DC04-QA0623",
  "productSpec": "厚度0.80mm，宽度1250mm",
  "sampleType": "TAIL",
  "testTime": "2026-06-24 11:24:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 135.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 310.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 40.0
    },
    {
      "indicatorId": "ind008",
      "testValue": 0.2
    }
  ]
}
```

### td_ins_08_4

```json
{
  "heatNo": "HT-TD-08-REINS-001",
  "coilNo": "COIL-TD-08-REINS-001",
  "customerId": null,
  "productVariety": "深冲冷轧板",
  "productGrade": "DC04-QA0623",
  "productSpec": "厚度0.80mm，宽度1250mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-24 12:24:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 175.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 265.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 40.0
    },
    {
      "indicatorId": "ind008",
      "testValue": 0.2
    }
  ]
}
```

### td_ins_08_5

```json
{
  "heatNo": "HT-TD-08-UNQ-001",
  "coilNo": "COIL-TD-08-UNQ-001",
  "customerId": null,
  "productVariety": "深冲冷轧板",
  "productGrade": "DC04-QA0623",
  "productSpec": "厚度0.80mm，宽度1250mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-24 13:24:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 119.99
    },
    {
      "indicatorId": "ind001",
      "testValue": 310.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 40.0
    },
    {
      "indicatorId": "ind008",
      "testValue": 0.2
    }
  ]
}
```
