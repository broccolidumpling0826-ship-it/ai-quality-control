# 检验记录样例 013 - HC340LA / 冷轧低合金高强钢板

## 1. 样例定位

| 项目 | 内容 |
| --- | --- |
| 样例编号 | `INSPECT-SAMPLE-013` |
| 来源PDF | `/Users/weixuelei/workspace/aiAgent/ai-project/for-test/std-doc/Q-JH-STEEL-SIM-030-2026_HC340LA_cold_rolled_enterprise.pdf` |
| PDF标准编号 | `Q/JH STEEL SIM 030-2026` |
| SQL脚本 | `inspect_sample_013_HC340LA_enterprise.sql` |
| 标准类型 | `ENTERPRISE` |
| 结构化牌号 | `HC340LA-QA0623` |
| 记录数量 | `5` |

## 2. 标准录入字段

| 字段 | 值 |
| --- | --- |
| 标准ID | `td_std_13_hc340la_enterprise` |
| 标准编号 | `Q/JH STEEL SIM 030-2026-QA0623` |
| 标准名称 | `HC340LA冷轧低合金高强钢板企业标准（QA0623隔离模拟企标）` |
| 品种 | `冷轧低合金高强钢板` |
| 牌号 | `HC340LA-QA0623` |
| 规格范围 | `厚度0.80mm-2.50mm，宽度900mm-1500mm` |
| 客户 | `空` |
| 生效日期 | `2026-03-01` |
| 失效日期 | `9999-12-31` |

## 3. 结构化指标

| 指标ID | 指标 | 合格下限 | 合格上限 | 让步下限 | 让步上限 | 单位 |
| --- | --- | --- | --- | --- | --- | --- |
| `ind003` | 屈服强度 | 340 | 430 | 330 | 440 | MPa |
| `ind001` | 抗拉强度 | 410 | 520 | 空 | 空 | MPa |
| `ind002` | 延伸率 | 22 | 空 | 20 | 空 | % |
| `ind004` | 厚度公差 | -0.06 | 0.06 | -0.08 | 0.08 | mm |

## 4. 五条检验记录

| 序号 | 场景 | 记录ID | 炉号 | 卷号 | 预期判定 |
| --- | --- | --- | --- | --- | --- |
| 1 | 全部关键指标处于合格范围内 | `td_ins_13_1` | `HT-TD-13-QUAL-001` | `COIL-TD-13-QUAL-001` | `QUALIFIED` |
| 2 | 关键指标取边界值，验证边界包含 | `td_ins_13_2` | `HT-TD-13-EDGE-001` | `COIL-TD-13-EDGE-001` | `QUALIFIED` |
| 3 | 首个关键指标轻微超出合格范围但位于让步范围内 | `td_ins_13_3` | `HT-TD-13-CONC-001` | `COIL-TD-13-CONC-001` | `CAN_CONCESSION` |
| 4 | 第二个关键指标超限且该方向未配置让步范围 | `td_ins_13_4` | `HT-TD-13-REINS-001` | `COIL-TD-13-REINS-001` | `NEED_REINSPECTION` |
| 5 | 首个关键指标超出让步范围 | `td_ins_13_5` | `HT-TD-13-UNQ-001` | `COIL-TD-13-UNQ-001` | `UNQUALIFIED` |

检验值明细：

| 记录ID | 屈服强度 | 抗拉强度 | 延伸率 | 厚度公差 |
| --- | --- | --- | --- | --- |
| `td_ins_13_1` | 385 | 465 | 24 | 0 |
| `td_ins_13_2` | 340 | 410 | 22 | -0.06 |
| `td_ins_13_3` | 335 | 465 | 24 | 0 |
| `td_ins_13_4` | 385 | 405 | 24 | 0 |
| `td_ins_13_5` | 319.99 | 465 | 24 | 0 |

## 5. 使用说明

1. 直接执行本样例SQL可插入标准、指标、检验记录和检验值。
2. 直接SQL插入不会调用后端 `InspectionService.addRecord()`，因此不会自动生成 `qc_judgment_result`。
3. 如需验证规则引擎自动判定，请先执行本SQL中的标准和字典数据，随后按下方API payload通过页面或接口新增检验记录。
4. 结构化牌号统一使用 `QA0623` 后缀，PDF正文中的原牌号仅代表来源材料。

## 6. API Payloads

### td_ins_13_1

```json
{
  "heatNo": "HT-TD-13-QUAL-001",
  "coilNo": "COIL-TD-13-QUAL-001",
  "customerId": null,
  "productVariety": "冷轧低合金高强钢板",
  "productGrade": "HC340LA-QA0623",
  "productSpec": "厚度1.20mm，宽度1200mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-26 09:39:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 385.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 465.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 24.0
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    }
  ]
}
```

### td_ins_13_2

```json
{
  "heatNo": "HT-TD-13-EDGE-001",
  "coilNo": "COIL-TD-13-EDGE-001",
  "customerId": null,
  "productVariety": "冷轧低合金高强钢板",
  "productGrade": "HC340LA-QA0623",
  "productSpec": "厚度1.20mm，宽度1200mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-26 10:39:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 340.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 410.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 22.0
    },
    {
      "indicatorId": "ind004",
      "testValue": -0.06
    }
  ]
}
```

### td_ins_13_3

```json
{
  "heatNo": "HT-TD-13-CONC-001",
  "coilNo": "COIL-TD-13-CONC-001",
  "customerId": null,
  "productVariety": "冷轧低合金高强钢板",
  "productGrade": "HC340LA-QA0623",
  "productSpec": "厚度1.20mm，宽度1200mm",
  "sampleType": "TAIL",
  "testTime": "2026-06-26 11:39:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 335.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 465.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 24.0
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    }
  ]
}
```

### td_ins_13_4

```json
{
  "heatNo": "HT-TD-13-REINS-001",
  "coilNo": "COIL-TD-13-REINS-001",
  "customerId": null,
  "productVariety": "冷轧低合金高强钢板",
  "productGrade": "HC340LA-QA0623",
  "productSpec": "厚度1.20mm，宽度1200mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-26 12:39:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 385.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 405.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 24.0
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    }
  ]
}
```

### td_ins_13_5

```json
{
  "heatNo": "HT-TD-13-UNQ-001",
  "coilNo": "COIL-TD-13-UNQ-001",
  "customerId": null,
  "productVariety": "冷轧低合金高强钢板",
  "productGrade": "HC340LA-QA0623",
  "productSpec": "厚度1.20mm，宽度1200mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-26 13:39:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 319.99
    },
    {
      "indicatorId": "ind001",
      "testValue": 465.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 24.0
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    }
  ]
}
```
