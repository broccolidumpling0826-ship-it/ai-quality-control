# 检验记录样例 018 - DX56D+Z / 热镀锌深冲板

## 1. 样例定位

| 项目 | 内容 |
| --- | --- |
| 样例编号 | `INSPECT-SAMPLE-018` |
| 来源PDF | `/Users/weixuelei/workspace/aiAgent/ai-project/for-test/std-doc/CUST-BYEV-SIM-2026-DX56D-Z_customer_agreement.pdf` |
| PDF标准编号 | `AGREEMENT BYEV-SIM-2026-003` |
| SQL脚本 | `inspect_sample_018_DX56D_Z_customer_BYEV.sql` |
| 标准类型 | `CUSTOMER` |
| 结构化牌号 | `DX56D+Z-QA0623` |
| 记录数量 | `5` |

## 2. 标准录入字段

| 字段 | 值 |
| --- | --- |
| 标准ID | `td_std_18_dx56d_z_customer_byev` |
| 标准编号 | `AGREEMENT BYEV-SIM-2026-003-QA0623` |
| 标准名称 | `北源新能源DX56D+Z热镀锌深冲板供货质量协议（QA0623隔离模拟客户协议）` |
| 品种 | `热镀锌深冲板` |
| 牌号 | `DX56D+Z-QA0623` |
| 规格范围 | `厚度0.60mm-1.60mm，宽度900mm-1450mm` |
| 客户 | `TD-CUST-BYEV` |
| 生效日期 | `2026-03-01` |
| 失效日期 | `2026-12-31` |

## 3. 结构化指标

| 指标ID | 指标 | 合格下限 | 合格上限 | 让步下限 | 让步上限 | 单位 |
| --- | --- | --- | --- | --- | --- | --- |
| `ind002` | 延伸率 | 39 | 空 | 37 | 空 | % |
| `td_ind_coat_mass` | 镀层重量 | 70 | 140 | 空 | 空 | g/m2 |
| `ind003` | 屈服强度 | 120 | 180 | 110 | 190 | MPa |
| `ind004` | 厚度公差 | -0.05 | 0.05 | -0.06 | 0.06 | mm |

## 4. 五条检验记录

| 序号 | 场景 | 记录ID | 炉号 | 卷号 | 预期判定 |
| --- | --- | --- | --- | --- | --- |
| 1 | 全部关键指标处于合格范围内 | `td_ins_18_1` | `HT-TD-18-QUAL-001` | `COIL-TD-18-QUAL-001` | `QUALIFIED` |
| 2 | 关键指标取边界值，验证边界包含 | `td_ins_18_2` | `HT-TD-18-EDGE-001` | `COIL-TD-18-EDGE-001` | `QUALIFIED` |
| 3 | 首个关键指标轻微超出合格范围但位于让步范围内 | `td_ins_18_3` | `HT-TD-18-CONC-001` | `COIL-TD-18-CONC-001` | `CAN_CONCESSION` |
| 4 | 第二个关键指标超限且该方向未配置让步范围 | `td_ins_18_4` | `HT-TD-18-REINS-001` | `COIL-TD-18-REINS-001` | `NEED_REINSPECTION` |
| 5 | 首个关键指标超出让步范围 | `td_ins_18_5` | `HT-TD-18-UNQ-001` | `COIL-TD-18-UNQ-001` | `UNQUALIFIED` |

检验值明细：

| 记录ID | 延伸率 | 镀层重量 | 屈服强度 | 厚度公差 |
| --- | --- | --- | --- | --- |
| `td_ins_18_1` | 41 | 105 | 150 | 0 |
| `td_ins_18_2` | 39 | 70 | 120 | -0.05 |
| `td_ins_18_3` | 38 | 105 | 150 | 0 |
| `td_ins_18_4` | 41 | 69.5 | 150 | 0 |
| `td_ins_18_5` | 34.99 | 105 | 150 | 0 |

## 5. 使用说明

1. 直接执行本样例SQL可插入标准、指标、检验记录和检验值。
2. 直接SQL插入不会调用后端 `InspectionService.addRecord()`，因此不会自动生成 `qc_judgment_result`。
3. 如需验证规则引擎自动判定，请先执行本SQL中的标准和字典数据，随后按下方API payload通过页面或接口新增检验记录。
4. 结构化牌号统一使用 `QA0623` 后缀，PDF正文中的原牌号仅代表来源材料。

## 6. API Payloads

### td_ins_18_1

```json
{
  "heatNo": "HT-TD-18-QUAL-001",
  "coilNo": "COIL-TD-18-QUAL-001",
  "customerId": "TD-CUST-BYEV",
  "productVariety": "热镀锌深冲板",
  "productGrade": "DX56D+Z-QA0623",
  "productSpec": "厚度0.80mm，宽度1250mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-27 09:54:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind002",
      "testValue": 41.0
    },
    {
      "indicatorId": "td_ind_coat_mass",
      "testValue": 105.0
    },
    {
      "indicatorId": "ind003",
      "testValue": 150.0
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    }
  ]
}
```

### td_ins_18_2

```json
{
  "heatNo": "HT-TD-18-EDGE-001",
  "coilNo": "COIL-TD-18-EDGE-001",
  "customerId": "TD-CUST-BYEV",
  "productVariety": "热镀锌深冲板",
  "productGrade": "DX56D+Z-QA0623",
  "productSpec": "厚度0.80mm，宽度1250mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-27 10:54:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind002",
      "testValue": 39.0
    },
    {
      "indicatorId": "td_ind_coat_mass",
      "testValue": 70.0
    },
    {
      "indicatorId": "ind003",
      "testValue": 120.0
    },
    {
      "indicatorId": "ind004",
      "testValue": -0.05
    }
  ]
}
```

### td_ins_18_3

```json
{
  "heatNo": "HT-TD-18-CONC-001",
  "coilNo": "COIL-TD-18-CONC-001",
  "customerId": "TD-CUST-BYEV",
  "productVariety": "热镀锌深冲板",
  "productGrade": "DX56D+Z-QA0623",
  "productSpec": "厚度0.80mm，宽度1250mm",
  "sampleType": "TAIL",
  "testTime": "2026-06-27 11:54:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind002",
      "testValue": 38.0
    },
    {
      "indicatorId": "td_ind_coat_mass",
      "testValue": 105.0
    },
    {
      "indicatorId": "ind003",
      "testValue": 150.0
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    }
  ]
}
```

### td_ins_18_4

```json
{
  "heatNo": "HT-TD-18-REINS-001",
  "coilNo": "COIL-TD-18-REINS-001",
  "customerId": "TD-CUST-BYEV",
  "productVariety": "热镀锌深冲板",
  "productGrade": "DX56D+Z-QA0623",
  "productSpec": "厚度0.80mm，宽度1250mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-27 12:54:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind002",
      "testValue": 41.0
    },
    {
      "indicatorId": "td_ind_coat_mass",
      "testValue": 69.5
    },
    {
      "indicatorId": "ind003",
      "testValue": 150.0
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    }
  ]
}
```

### td_ins_18_5

```json
{
  "heatNo": "HT-TD-18-UNQ-001",
  "coilNo": "COIL-TD-18-UNQ-001",
  "customerId": "TD-CUST-BYEV",
  "productVariety": "热镀锌深冲板",
  "productGrade": "DX56D+Z-QA0623",
  "productSpec": "厚度0.80mm，宽度1250mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-27 13:54:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind002",
      "testValue": 34.99
    },
    {
      "indicatorId": "td_ind_coat_mass",
      "testValue": 105.0
    },
    {
      "indicatorId": "ind003",
      "testValue": 150.0
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    }
  ]
}
```
