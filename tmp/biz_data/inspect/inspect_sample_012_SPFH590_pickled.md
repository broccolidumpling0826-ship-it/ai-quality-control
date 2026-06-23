# 检验记录样例 012 - SPFH590 / 酸洗热轧钢带

## 1. 样例定位

| 项目 | 内容 |
| --- | --- |
| 样例编号 | `INSPECT-SAMPLE-012` |
| 来源PDF | `/Users/weixuelei/workspace/aiAgent/ai-project/for-test/std-doc/Q-JH-STEEL-SIM-020-2026_SPFH590_pickled_strip_enterprise.pdf` |
| PDF标准编号 | `Q/JH STEEL SIM 020-2026` |
| SQL脚本 | `inspect_sample_012_SPFH590_pickled.sql` |
| 标准类型 | `ENTERPRISE` |
| 结构化牌号 | `SPFH590-QA0623` |
| 记录数量 | `5` |

## 2. 标准录入字段

| 字段 | 值 |
| --- | --- |
| 标准ID | `td_std_12_spfh590_pickled` |
| 标准编号 | `Q/JH STEEL SIM 020-2026-QA0623` |
| 标准名称 | `SPFH590酸洗热轧钢带企业标准（QA0623隔离模拟企标）` |
| 品种 | `酸洗热轧钢带` |
| 牌号 | `SPFH590-QA0623` |
| 规格范围 | `厚度1.80mm-6.00mm，宽度800mm-1600mm` |
| 客户 | `空` |
| 生效日期 | `2026-03-01` |
| 失效日期 | `9999-12-31` |

## 3. 结构化指标

| 指标ID | 指标 | 合格下限 | 合格上限 | 让步下限 | 让步上限 | 单位 |
| --- | --- | --- | --- | --- | --- | --- |
| `ind001` | 抗拉强度 | 590 | 760 | 575 | 780 | MPa |
| `ind003` | 屈服强度 | 420 | 空 | 空 | 空 | MPa |
| `ind002` | 延伸率 | 18 | 空 | 17 | 空 | % |
| `ind004` | 厚度公差 | -0.1 | 0.1 | -0.12 | 0.12 | mm |

## 4. 五条检验记录

| 序号 | 场景 | 记录ID | 炉号 | 卷号 | 预期判定 |
| --- | --- | --- | --- | --- | --- |
| 1 | 全部关键指标处于合格范围内 | `td_ins_12_1` | `HT-TD-12-QUAL-001` | `COIL-TD-12-QUAL-001` | `QUALIFIED` |
| 2 | 关键指标取边界值，验证边界包含 | `td_ins_12_2` | `HT-TD-12-EDGE-001` | `COIL-TD-12-EDGE-001` | `QUALIFIED` |
| 3 | 首个关键指标轻微超出合格范围但位于让步范围内 | `td_ins_12_3` | `HT-TD-12-CONC-001` | `COIL-TD-12-CONC-001` | `CAN_CONCESSION` |
| 4 | 第二个关键指标超限且该方向未配置让步范围 | `td_ins_12_4` | `HT-TD-12-REINS-001` | `COIL-TD-12-REINS-001` | `NEED_REINSPECTION` |
| 5 | 首个关键指标超出让步范围 | `td_ins_12_5` | `HT-TD-12-UNQ-001` | `COIL-TD-12-UNQ-001` | `UNQUALIFIED` |

检验值明细：

| 记录ID | 抗拉强度 | 屈服强度 | 延伸率 | 厚度公差 |
| --- | --- | --- | --- | --- |
| `td_ins_12_1` | 675 | 445 | 20 | 0 |
| `td_ins_12_2` | 590 | 420 | 18 | -0.1 |
| `td_ins_12_3` | 582.5 | 445 | 20 | 0 |
| `td_ins_12_4` | 675 | 415 | 20 | 0 |
| `td_ins_12_5` | 559.99 | 445 | 20 | 0 |

## 5. 使用说明

1. 直接执行本样例SQL可插入标准、指标、检验记录和检验值。
2. 直接SQL插入不会调用后端 `InspectionService.addRecord()`，因此不会自动生成 `qc_judgment_result`。
3. 如需验证规则引擎自动判定，请先执行本SQL中的标准和字典数据，随后按下方API payload通过页面或接口新增检验记录。
4. 结构化牌号统一使用 `QA0623` 后缀，PDF正文中的原牌号仅代表来源材料。

## 6. API Payloads

### td_ins_12_1

```json
{
  "heatNo": "HT-TD-12-QUAL-001",
  "coilNo": "COIL-TD-12-QUAL-001",
  "customerId": null,
  "productVariety": "酸洗热轧钢带",
  "productGrade": "SPFH590-QA0623",
  "productSpec": "厚度3.00mm，宽度1250mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-25 09:36:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind001",
      "testValue": 675.0
    },
    {
      "indicatorId": "ind003",
      "testValue": 445.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 20.0
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    }
  ]
}
```

### td_ins_12_2

```json
{
  "heatNo": "HT-TD-12-EDGE-001",
  "coilNo": "COIL-TD-12-EDGE-001",
  "customerId": null,
  "productVariety": "酸洗热轧钢带",
  "productGrade": "SPFH590-QA0623",
  "productSpec": "厚度3.00mm，宽度1250mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-25 10:36:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind001",
      "testValue": 590.0
    },
    {
      "indicatorId": "ind003",
      "testValue": 420.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 18.0
    },
    {
      "indicatorId": "ind004",
      "testValue": -0.1
    }
  ]
}
```

### td_ins_12_3

```json
{
  "heatNo": "HT-TD-12-CONC-001",
  "coilNo": "COIL-TD-12-CONC-001",
  "customerId": null,
  "productVariety": "酸洗热轧钢带",
  "productGrade": "SPFH590-QA0623",
  "productSpec": "厚度3.00mm，宽度1250mm",
  "sampleType": "TAIL",
  "testTime": "2026-06-25 11:36:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind001",
      "testValue": 582.5
    },
    {
      "indicatorId": "ind003",
      "testValue": 445.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 20.0
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    }
  ]
}
```

### td_ins_12_4

```json
{
  "heatNo": "HT-TD-12-REINS-001",
  "coilNo": "COIL-TD-12-REINS-001",
  "customerId": null,
  "productVariety": "酸洗热轧钢带",
  "productGrade": "SPFH590-QA0623",
  "productSpec": "厚度3.00mm，宽度1250mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-25 12:36:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind001",
      "testValue": 675.0
    },
    {
      "indicatorId": "ind003",
      "testValue": 415.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 20.0
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    }
  ]
}
```

### td_ins_12_5

```json
{
  "heatNo": "HT-TD-12-UNQ-001",
  "coilNo": "COIL-TD-12-UNQ-001",
  "customerId": null,
  "productVariety": "酸洗热轧钢带",
  "productGrade": "SPFH590-QA0623",
  "productSpec": "厚度3.00mm，宽度1250mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-25 13:36:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind001",
      "testValue": 559.99
    },
    {
      "indicatorId": "ind003",
      "testValue": 445.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 20.0
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    }
  ]
}
```
