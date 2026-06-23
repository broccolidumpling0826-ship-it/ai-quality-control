# 检验记录样例 019 - AH36 / 船体结构钢板

## 1. 样例定位

| 项目 | 内容 |
| --- | --- |
| 样例编号 | `INSPECT-SAMPLE-019` |
| 来源PDF | `/Users/weixuelei/workspace/aiAgent/ai-project/for-test/std-doc/CUST-ZYSHIP-SIM-2026-AH36_customer_agreement.pdf` |
| PDF标准编号 | `AGREEMENT ZYSHIP-SIM-2026-004` |
| SQL脚本 | `inspect_sample_019_AH36_customer_ZYSHIP.sql` |
| 标准类型 | `CUSTOMER` |
| 结构化牌号 | `AH36-QA0623` |
| 记录数量 | `5` |

## 2. 标准录入字段

| 字段 | 值 |
| --- | --- |
| 标准ID | `td_std_19_ah36_customer_zyship` |
| 标准编号 | `AGREEMENT ZYSHIP-SIM-2026-004-QA0623` |
| 标准名称 | `中远船务AH36船体结构钢板供货质量协议（QA0623隔离模拟客户协议）` |
| 品种 | `船体结构钢板` |
| 牌号 | `AH36-QA0623` |
| 规格范围 | `厚度8.00mm-40.00mm，宽度1500mm-3000mm` |
| 客户 | `TD-CUST-ZYSHIP` |
| 生效日期 | `2026-03-01` |
| 失效日期 | `2026-12-31` |

## 3. 结构化指标

| 指标ID | 指标 | 合格下限 | 合格上限 | 让步下限 | 让步上限 | 单位 |
| --- | --- | --- | --- | --- | --- | --- |
| `ind003` | 屈服强度 | 355 | 空 | 345 | 空 | MPa |
| `ind001` | 抗拉强度 | 490 | 620 | 空 | 空 | MPa |
| `ind002` | 延伸率 | 21 | 空 | 20 | 空 | % |
| `td_ind_kv2` | 冲击功KV2 | 34 | 空 | 30 | 空 | J |

## 4. 五条检验记录

| 序号 | 场景 | 记录ID | 炉号 | 卷号 | 预期判定 |
| --- | --- | --- | --- | --- | --- |
| 1 | 全部关键指标处于合格范围内 | `td_ins_19_1` | `HT-TD-19-QUAL-001` | `COIL-TD-19-QUAL-001` | `QUALIFIED` |
| 2 | 关键指标取边界值，验证边界包含 | `td_ins_19_2` | `HT-TD-19-EDGE-001` | `COIL-TD-19-EDGE-001` | `QUALIFIED` |
| 3 | 首个关键指标轻微超出合格范围但位于让步范围内 | `td_ins_19_3` | `HT-TD-19-CONC-001` | `COIL-TD-19-CONC-001` | `CAN_CONCESSION` |
| 4 | 第二个关键指标超限且该方向未配置让步范围 | `td_ins_19_4` | `HT-TD-19-REINS-001` | `COIL-TD-19-REINS-001` | `NEED_REINSPECTION` |
| 5 | 首个关键指标超出让步范围 | `td_ins_19_5` | `HT-TD-19-UNQ-001` | `COIL-TD-19-UNQ-001` | `UNQUALIFIED` |

检验值明细：

| 记录ID | 屈服强度 | 抗拉强度 | 延伸率 | 冲击功KV2 |
| --- | --- | --- | --- | --- |
| `td_ins_19_1` | 380 | 555 | 23 | 36 |
| `td_ins_19_2` | 355 | 490 | 21 | 34 |
| `td_ins_19_3` | 350 | 555 | 23 | 36 |
| `td_ins_19_4` | 380 | 485 | 23 | 36 |
| `td_ins_19_5` | 334.99 | 555 | 23 | 36 |

## 5. 使用说明

1. 直接执行本样例SQL可插入标准、指标、检验记录和检验值。
2. 直接SQL插入不会调用后端 `InspectionService.addRecord()`，因此不会自动生成 `qc_judgment_result`。
3. 如需验证规则引擎自动判定，请先执行本SQL中的标准和字典数据，随后按下方API payload通过页面或接口新增检验记录。
4. 结构化牌号统一使用 `QA0623` 后缀，PDF正文中的原牌号仅代表来源材料。

## 6. API Payloads

### td_ins_19_1

```json
{
  "heatNo": "HT-TD-19-QUAL-001",
  "coilNo": "COIL-TD-19-QUAL-001",
  "customerId": "TD-CUST-ZYSHIP",
  "productVariety": "船体结构钢板",
  "productGrade": "AH36-QA0623",
  "productSpec": "厚度18.00mm，宽度2200mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-27 09:57:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 380.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 555.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 23.0
    },
    {
      "indicatorId": "td_ind_kv2",
      "testValue": 36.0
    }
  ]
}
```

### td_ins_19_2

```json
{
  "heatNo": "HT-TD-19-EDGE-001",
  "coilNo": "COIL-TD-19-EDGE-001",
  "customerId": "TD-CUST-ZYSHIP",
  "productVariety": "船体结构钢板",
  "productGrade": "AH36-QA0623",
  "productSpec": "厚度18.00mm，宽度2200mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-27 10:57:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 355.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 490.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 21.0
    },
    {
      "indicatorId": "td_ind_kv2",
      "testValue": 34.0
    }
  ]
}
```

### td_ins_19_3

```json
{
  "heatNo": "HT-TD-19-CONC-001",
  "coilNo": "COIL-TD-19-CONC-001",
  "customerId": "TD-CUST-ZYSHIP",
  "productVariety": "船体结构钢板",
  "productGrade": "AH36-QA0623",
  "productSpec": "厚度18.00mm，宽度2200mm",
  "sampleType": "TAIL",
  "testTime": "2026-06-27 11:57:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 350.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 555.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 23.0
    },
    {
      "indicatorId": "td_ind_kv2",
      "testValue": 36.0
    }
  ]
}
```

### td_ins_19_4

```json
{
  "heatNo": "HT-TD-19-REINS-001",
  "coilNo": "COIL-TD-19-REINS-001",
  "customerId": "TD-CUST-ZYSHIP",
  "productVariety": "船体结构钢板",
  "productGrade": "AH36-QA0623",
  "productSpec": "厚度18.00mm，宽度2200mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-27 12:57:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 380.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 485.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 23.0
    },
    {
      "indicatorId": "td_ind_kv2",
      "testValue": 36.0
    }
  ]
}
```

### td_ins_19_5

```json
{
  "heatNo": "HT-TD-19-UNQ-001",
  "coilNo": "COIL-TD-19-UNQ-001",
  "customerId": "TD-CUST-ZYSHIP",
  "productVariety": "船体结构钢板",
  "productGrade": "AH36-QA0623",
  "productSpec": "厚度18.00mm，宽度2200mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-27 13:57:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 334.99
    },
    {
      "indicatorId": "ind001",
      "testValue": 555.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 23.0
    },
    {
      "indicatorId": "td_ind_kv2",
      "testValue": 36.0
    }
  ]
}
```
