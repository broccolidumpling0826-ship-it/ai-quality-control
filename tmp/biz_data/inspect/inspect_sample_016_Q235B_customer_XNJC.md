# 检验记录样例 016 - Q235B / 冷轧板

## 1. 样例定位

| 项目 | 内容 |
| --- | --- |
| 样例编号 | `INSPECT-SAMPLE-016` |
| 来源PDF | `/Users/weixuelei/workspace/aiAgent/ai-project/for-test/std-doc/CUST-XNJC-SIM-2026-Q235B_customer_agreement.pdf` |
| PDF标准编号 | `AGREEMENT XNJC-SIM-2026-001` |
| SQL脚本 | `inspect_sample_016_Q235B_customer_XNJC.sql` |
| 标准类型 | `CUSTOMER` |
| 结构化牌号 | `Q235B-QA0623` |
| 记录数量 | `5` |

## 2. 标准录入字段

| 字段 | 值 |
| --- | --- |
| 标准ID | `td_std_16_q235b_customer_xnjc` |
| 标准编号 | `AGREEMENT XNJC-SIM-2026-001-QA0623` |
| 标准名称 | `西南建材集团Q235B冷轧板供货质量协议（QA0623隔离模拟客户协议）` |
| 品种 | `冷轧板` |
| 牌号 | `Q235B-QA0623` |
| 规格范围 | `厚度0.80mm-2.50mm，宽度800mm-1250mm` |
| 客户 | `TD-CUST-XNJC` |
| 生效日期 | `2026-02-01` |
| 失效日期 | `2026-12-31` |

## 3. 结构化指标

| 指标ID | 指标 | 合格下限 | 合格上限 | 让步下限 | 让步上限 | 单位 |
| --- | --- | --- | --- | --- | --- | --- |
| `ind001` | 抗拉强度 | 375 | 505 | 370 | 510 | MPa |
| `ind002` | 延伸率 | 27 | 空 | 空 | 空 | % |
| `ind003` | 屈服强度 | 235 | 360 | 225 | 370 | MPa |
| `ind004` | 厚度公差 | -0.08 | 0.08 | -0.1 | 0.1 | mm |

## 4. 五条检验记录

| 序号 | 场景 | 记录ID | 炉号 | 卷号 | 预期判定 |
| --- | --- | --- | --- | --- | --- |
| 1 | 全部关键指标处于合格范围内 | `td_ins_16_1` | `HT-TD-16-QUAL-001` | `COIL-TD-16-QUAL-001` | `QUALIFIED` |
| 2 | 关键指标取边界值，验证边界包含 | `td_ins_16_2` | `HT-TD-16-EDGE-001` | `COIL-TD-16-EDGE-001` | `QUALIFIED` |
| 3 | 首个关键指标轻微超出合格范围但位于让步范围内 | `td_ins_16_3` | `HT-TD-16-CONC-001` | `COIL-TD-16-CONC-001` | `CAN_CONCESSION` |
| 4 | 第二个关键指标超限且该方向未配置让步范围 | `td_ins_16_4` | `HT-TD-16-REINS-001` | `COIL-TD-16-REINS-001` | `NEED_REINSPECTION` |
| 5 | 首个关键指标超出让步范围 | `td_ins_16_5` | `HT-TD-16-UNQ-001` | `COIL-TD-16-UNQ-001` | `UNQUALIFIED` |

检验值明细：

| 记录ID | 抗拉强度 | 延伸率 | 屈服强度 | 厚度公差 |
| --- | --- | --- | --- | --- |
| `td_ins_16_1` | 440 | 29 | 297.5 | 0 |
| `td_ins_16_2` | 375 | 27 | 235 | -0.08 |
| `td_ins_16_3` | 372.5 | 29 | 297.5 | 0 |
| `td_ins_16_4` | 440 | 26.5 | 297.5 | 0 |
| `td_ins_16_5` | 364.99 | 29 | 297.5 | 0 |

## 5. 使用说明

1. 直接执行本样例SQL可插入标准、指标、检验记录和检验值。
2. 直接SQL插入不会调用后端 `InspectionService.addRecord()`，因此不会自动生成 `qc_judgment_result`。
3. 如需验证规则引擎自动判定，请先执行本SQL中的标准和字典数据，随后按下方API payload通过页面或接口新增检验记录。
4. 结构化牌号统一使用 `QA0623` 后缀，PDF正文中的原牌号仅代表来源材料。

## 6. API Payloads

### td_ins_16_1

```json
{
  "heatNo": "HT-TD-16-QUAL-001",
  "coilNo": "COIL-TD-16-QUAL-001",
  "customerId": "TD-CUST-XNJC",
  "productVariety": "冷轧板",
  "productGrade": "Q235B-QA0623",
  "productSpec": "厚度1.20mm，宽度1000mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-26 09:48:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind001",
      "testValue": 440.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 29.0
    },
    {
      "indicatorId": "ind003",
      "testValue": 297.5
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    }
  ]
}
```

### td_ins_16_2

```json
{
  "heatNo": "HT-TD-16-EDGE-001",
  "coilNo": "COIL-TD-16-EDGE-001",
  "customerId": "TD-CUST-XNJC",
  "productVariety": "冷轧板",
  "productGrade": "Q235B-QA0623",
  "productSpec": "厚度1.20mm，宽度1000mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-26 10:48:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind001",
      "testValue": 375.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 27.0
    },
    {
      "indicatorId": "ind003",
      "testValue": 235.0
    },
    {
      "indicatorId": "ind004",
      "testValue": -0.08
    }
  ]
}
```

### td_ins_16_3

```json
{
  "heatNo": "HT-TD-16-CONC-001",
  "coilNo": "COIL-TD-16-CONC-001",
  "customerId": "TD-CUST-XNJC",
  "productVariety": "冷轧板",
  "productGrade": "Q235B-QA0623",
  "productSpec": "厚度1.20mm，宽度1000mm",
  "sampleType": "TAIL",
  "testTime": "2026-06-26 11:48:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind001",
      "testValue": 372.5
    },
    {
      "indicatorId": "ind002",
      "testValue": 29.0
    },
    {
      "indicatorId": "ind003",
      "testValue": 297.5
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    }
  ]
}
```

### td_ins_16_4

```json
{
  "heatNo": "HT-TD-16-REINS-001",
  "coilNo": "COIL-TD-16-REINS-001",
  "customerId": "TD-CUST-XNJC",
  "productVariety": "冷轧板",
  "productGrade": "Q235B-QA0623",
  "productSpec": "厚度1.20mm，宽度1000mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-26 12:48:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind001",
      "testValue": 440.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 26.5
    },
    {
      "indicatorId": "ind003",
      "testValue": 297.5
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    }
  ]
}
```

### td_ins_16_5

```json
{
  "heatNo": "HT-TD-16-UNQ-001",
  "coilNo": "COIL-TD-16-UNQ-001",
  "customerId": "TD-CUST-XNJC",
  "productVariety": "冷轧板",
  "productGrade": "Q235B-QA0623",
  "productSpec": "厚度1.20mm，宽度1000mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-26 13:48:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind001",
      "testValue": 364.99
    },
    {
      "indicatorId": "ind002",
      "testValue": 29.0
    },
    {
      "indicatorId": "ind003",
      "testValue": 297.5
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    }
  ]
}
```
