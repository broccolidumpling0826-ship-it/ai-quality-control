# 检验记录样例 004 - DX51D+Z / 镀锌板

## 1. 样例定位

| 项目 | 内容 |
| --- | --- |
| 样例编号 | `INSPECT-SAMPLE-004` |
| 来源PDF | `/Users/weixuelei/workspace/aiAgent/ai-project/for-test/std-doc/GB-T-SIM-2518-2026_DX51D-Z_galvanized_sheet_national.pdf` |
| PDF标准编号 | `GB/T SIM 2518-2026` |
| SQL脚本 | `inspect_sample_004_DX51D_Z_galvanized.sql` |
| 标准类型 | `NATIONAL` |
| 结构化牌号 | `DX51D+Z-QA0623` |
| 记录数量 | `5` |

## 2. 标准录入字段

| 字段 | 值 |
| --- | --- |
| 标准ID | `td_std_04_dx51d_z_galvanized` |
| 标准编号 | `GB/T SIM 2518-2026-QA0623` |
| 标准名称 | `DX51D+Z连续热镀锌钢板及钢带质量要求（QA0623隔离模拟国标）` |
| 品种 | `镀锌板` |
| 牌号 | `DX51D+Z-QA0623` |
| 规格范围 | `厚度0.50mm-2.50mm，宽度800mm-1500mm` |
| 客户 | `空` |
| 生效日期 | `2026-04-01` |
| 失效日期 | `9999-12-31` |

## 3. 结构化指标

| 指标ID | 指标 | 合格下限 | 合格上限 | 让步下限 | 让步上限 | 单位 |
| --- | --- | --- | --- | --- | --- | --- |
| `td_ind_coat_mass` | 镀层重量 | 80 | 275 | 70 | 285 | g/m2 |
| `ind001` | 抗拉强度 | 270 | 500 | 空 | 空 | MPa |
| `ind002` | 延伸率 | 22 | 空 | 20 | 空 | % |
| `ind004` | 厚度公差 | -0.08 | 0.08 | -0.1 | 0.1 | mm |

## 4. 五条检验记录

| 序号 | 场景 | 记录ID | 炉号 | 卷号 | 预期判定 |
| --- | --- | --- | --- | --- | --- |
| 1 | 全部关键指标处于合格范围内 | `td_ins_04_1` | `HT-TD-04-QUAL-001` | `COIL-TD-04-QUAL-001` | `QUALIFIED` |
| 2 | 关键指标取边界值，验证边界包含 | `td_ins_04_2` | `HT-TD-04-EDGE-001` | `COIL-TD-04-EDGE-001` | `QUALIFIED` |
| 3 | 首个关键指标轻微超出合格范围但位于让步范围内 | `td_ins_04_3` | `HT-TD-04-CONC-001` | `COIL-TD-04-CONC-001` | `CAN_CONCESSION` |
| 4 | 第二个关键指标超限且该方向未配置让步范围 | `td_ins_04_4` | `HT-TD-04-REINS-001` | `COIL-TD-04-REINS-001` | `NEED_REINSPECTION` |
| 5 | 首个关键指标超出让步范围 | `td_ins_04_5` | `HT-TD-04-UNQ-001` | `COIL-TD-04-UNQ-001` | `UNQUALIFIED` |

检验值明细：

| 记录ID | 镀层重量 | 抗拉强度 | 延伸率 | 厚度公差 |
| --- | --- | --- | --- | --- |
| `td_ins_04_1` | 177.5 | 385 | 24 | 0 |
| `td_ins_04_2` | 80 | 270 | 22 | -0.08 |
| `td_ins_04_3` | 75 | 385 | 24 | 0 |
| `td_ins_04_4` | 177.5 | 265 | 24 | 0 |
| `td_ins_04_5` | 59.99 | 385 | 24 | 0 |

## 5. 使用说明

1. 直接执行本样例SQL可插入标准、指标、检验记录和检验值。
2. 直接SQL插入不会调用后端 `InspectionService.addRecord()`，因此不会自动生成 `qc_judgment_result`。
3. 如需验证规则引擎自动判定，请先执行本SQL中的标准和字典数据，随后按下方API payload通过页面或接口新增检验记录。
4. 结构化牌号统一使用 `QA0623` 后缀，PDF正文中的原牌号仅代表来源材料。

## 6. API Payloads

### td_ins_04_1

```json
{
  "heatNo": "HT-TD-04-QUAL-001",
  "coilNo": "COIL-TD-04-QUAL-001",
  "customerId": null,
  "productVariety": "镀锌板",
  "productGrade": "DX51D+Z-QA0623",
  "productSpec": "厚度1.00mm，宽度1250mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-23 09:12:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "td_ind_coat_mass",
      "testValue": 177.5
    },
    {
      "indicatorId": "ind001",
      "testValue": 385.0
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

### td_ins_04_2

```json
{
  "heatNo": "HT-TD-04-EDGE-001",
  "coilNo": "COIL-TD-04-EDGE-001",
  "customerId": null,
  "productVariety": "镀锌板",
  "productGrade": "DX51D+Z-QA0623",
  "productSpec": "厚度1.00mm，宽度1250mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-23 10:12:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "td_ind_coat_mass",
      "testValue": 80.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 270.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 22.0
    },
    {
      "indicatorId": "ind004",
      "testValue": -0.08
    }
  ]
}
```

### td_ins_04_3

```json
{
  "heatNo": "HT-TD-04-CONC-001",
  "coilNo": "COIL-TD-04-CONC-001",
  "customerId": null,
  "productVariety": "镀锌板",
  "productGrade": "DX51D+Z-QA0623",
  "productSpec": "厚度1.00mm，宽度1250mm",
  "sampleType": "TAIL",
  "testTime": "2026-06-23 11:12:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "td_ind_coat_mass",
      "testValue": 75.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 385.0
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

### td_ins_04_4

```json
{
  "heatNo": "HT-TD-04-REINS-001",
  "coilNo": "COIL-TD-04-REINS-001",
  "customerId": null,
  "productVariety": "镀锌板",
  "productGrade": "DX51D+Z-QA0623",
  "productSpec": "厚度1.00mm，宽度1250mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-23 12:12:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "td_ind_coat_mass",
      "testValue": 177.5
    },
    {
      "indicatorId": "ind001",
      "testValue": 265.0
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

### td_ins_04_5

```json
{
  "heatNo": "HT-TD-04-UNQ-001",
  "coilNo": "COIL-TD-04-UNQ-001",
  "customerId": null,
  "productVariety": "镀锌板",
  "productGrade": "DX51D+Z-QA0623",
  "productSpec": "厚度1.00mm，宽度1250mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-23 13:12:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "td_ind_coat_mass",
      "testValue": 59.99
    },
    {
      "indicatorId": "ind001",
      "testValue": 385.0
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
