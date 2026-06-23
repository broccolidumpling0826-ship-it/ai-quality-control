# 检验记录样例 003 - Q355B / 热轧板

## 1. 样例定位

| 项目 | 内容 |
| --- | --- |
| 样例编号 | `INSPECT-SAMPLE-003` |
| 来源PDF | `/Users/weixuelei/workspace/aiAgent/ai-project/for-test/std-doc/GB-T-SIM-3274-2026_Q355B_hot_rolled_plate_national.pdf` |
| PDF标准编号 | `GB/T SIM 3274-2026` |
| SQL脚本 | `inspect_sample_003_Q355B_hot_rolled_plate.sql` |
| 标准类型 | `NATIONAL` |
| 结构化牌号 | `Q355B-QA0623` |
| 记录数量 | `5` |

## 2. 标准录入字段

| 字段 | 值 |
| --- | --- |
| 标准ID | `td_std_03_q355b_hot_rolled_plate` |
| 标准编号 | `GB/T SIM 3274-2026-QA0623` |
| 标准名称 | `Q355B热轧钢板和钢带质量要求（QA0623隔离模拟国标）` |
| 品种 | `热轧板` |
| 牌号 | `Q355B-QA0623` |
| 规格范围 | `厚度3.00mm-25.00mm，宽度1000mm-2200mm` |
| 客户 | `空` |
| 生效日期 | `2026-04-01` |
| 失效日期 | `9999-12-31` |

## 3. 结构化指标

| 指标ID | 指标 | 合格下限 | 合格上限 | 让步下限 | 让步上限 | 单位 |
| --- | --- | --- | --- | --- | --- | --- |
| `ind003` | 屈服强度 | 355 | 空 | 345 | 空 | MPa |
| `ind001` | 抗拉强度 | 470 | 630 | 空 | 空 | MPa |
| `ind002` | 延伸率 | 21 | 空 | 20 | 空 | % |
| `ind004` | 厚度公差 | -0.25 | 0.25 | -0.3 | 0.3 | mm |

## 4. 五条检验记录

| 序号 | 场景 | 记录ID | 炉号 | 卷号 | 预期判定 |
| --- | --- | --- | --- | --- | --- |
| 1 | 全部关键指标处于合格范围内 | `td_ins_03_1` | `HT-TD-03-QUAL-001` | `COIL-TD-03-QUAL-001` | `QUALIFIED` |
| 2 | 关键指标取边界值，验证边界包含 | `td_ins_03_2` | `HT-TD-03-EDGE-001` | `COIL-TD-03-EDGE-001` | `QUALIFIED` |
| 3 | 首个关键指标轻微超出合格范围但位于让步范围内 | `td_ins_03_3` | `HT-TD-03-CONC-001` | `COIL-TD-03-CONC-001` | `CAN_CONCESSION` |
| 4 | 第二个关键指标超限且该方向未配置让步范围 | `td_ins_03_4` | `HT-TD-03-REINS-001` | `COIL-TD-03-REINS-001` | `NEED_REINSPECTION` |
| 5 | 首个关键指标超出让步范围 | `td_ins_03_5` | `HT-TD-03-UNQ-001` | `COIL-TD-03-UNQ-001` | `UNQUALIFIED` |

检验值明细：

| 记录ID | 屈服强度 | 抗拉强度 | 延伸率 | 厚度公差 |
| --- | --- | --- | --- | --- |
| `td_ins_03_1` | 380 | 550 | 23 | 0 |
| `td_ins_03_2` | 355 | 470 | 21 | -0.25 |
| `td_ins_03_3` | 350 | 550 | 23 | 0 |
| `td_ins_03_4` | 380 | 465 | 23 | 0 |
| `td_ins_03_5` | 334.99 | 550 | 23 | 0 |

## 5. 使用说明

1. 直接执行本样例SQL可插入标准、指标、检验记录和检验值。
2. 直接SQL插入不会调用后端 `InspectionService.addRecord()`，因此不会自动生成 `qc_judgment_result`。
3. 如需验证规则引擎自动判定，请先执行本SQL中的标准和字典数据，随后按下方API payload通过页面或接口新增检验记录。
4. 结构化牌号统一使用 `QA0623` 后缀，PDF正文中的原牌号仅代表来源材料。

## 6. API Payloads

### td_ins_03_1

```json
{
  "heatNo": "HT-TD-03-QUAL-001",
  "coilNo": "COIL-TD-03-QUAL-001",
  "customerId": null,
  "productVariety": "热轧板",
  "productGrade": "Q355B-QA0623",
  "productSpec": "厚度8.00mm，宽度1500mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-23 09:09:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 380.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 550.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 23.0
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    }
  ]
}
```

### td_ins_03_2

```json
{
  "heatNo": "HT-TD-03-EDGE-001",
  "coilNo": "COIL-TD-03-EDGE-001",
  "customerId": null,
  "productVariety": "热轧板",
  "productGrade": "Q355B-QA0623",
  "productSpec": "厚度8.00mm，宽度1500mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-23 10:09:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 355.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 470.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 21.0
    },
    {
      "indicatorId": "ind004",
      "testValue": -0.25
    }
  ]
}
```

### td_ins_03_3

```json
{
  "heatNo": "HT-TD-03-CONC-001",
  "coilNo": "COIL-TD-03-CONC-001",
  "customerId": null,
  "productVariety": "热轧板",
  "productGrade": "Q355B-QA0623",
  "productSpec": "厚度8.00mm，宽度1500mm",
  "sampleType": "TAIL",
  "testTime": "2026-06-23 11:09:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 350.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 550.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 23.0
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    }
  ]
}
```

### td_ins_03_4

```json
{
  "heatNo": "HT-TD-03-REINS-001",
  "coilNo": "COIL-TD-03-REINS-001",
  "customerId": null,
  "productVariety": "热轧板",
  "productGrade": "Q355B-QA0623",
  "productSpec": "厚度8.00mm，宽度1500mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-23 12:09:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 380.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 465.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 23.0
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    }
  ]
}
```

### td_ins_03_5

```json
{
  "heatNo": "HT-TD-03-UNQ-001",
  "coilNo": "COIL-TD-03-UNQ-001",
  "customerId": null,
  "productVariety": "热轧板",
  "productGrade": "Q355B-QA0623",
  "productSpec": "厚度8.00mm，宽度1500mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-23 13:09:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 334.99
    },
    {
      "indicatorId": "ind001",
      "testValue": 550.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 23.0
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    }
  ]
}
```
