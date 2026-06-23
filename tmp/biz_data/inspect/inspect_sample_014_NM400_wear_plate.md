# 检验记录样例 014 - NM400 / 调质耐磨钢板

## 1. 样例定位

| 项目 | 内容 |
| --- | --- |
| 样例编号 | `INSPECT-SAMPLE-014` |
| 来源PDF | `/Users/weixuelei/workspace/aiAgent/ai-project/for-test/std-doc/Q-JH-STEEL-SIM-040-2026_NM400_wear_plate_enterprise.pdf` |
| PDF标准编号 | `Q/JH STEEL SIM 040-2026` |
| SQL脚本 | `inspect_sample_014_NM400_wear_plate.sql` |
| 标准类型 | `ENTERPRISE` |
| 结构化牌号 | `NM400-QA0623` |
| 记录数量 | `5` |

## 2. 标准录入字段

| 字段 | 值 |
| --- | --- |
| 标准ID | `td_std_14_nm400_wear_plate` |
| 标准编号 | `Q/JH STEEL SIM 040-2026-QA0623` |
| 标准名称 | `NM400调质耐磨钢板企业标准（QA0623隔离模拟企标）` |
| 品种 | `调质耐磨钢板` |
| 牌号 | `NM400-QA0623` |
| 规格范围 | `厚度6.00mm-40.00mm，宽度1500mm-2500mm` |
| 客户 | `空` |
| 生效日期 | `2026-03-01` |
| 失效日期 | `9999-12-31` |

## 3. 结构化指标

| 指标ID | 指标 | 合格下限 | 合格上限 | 让步下限 | 让步上限 | 单位 |
| --- | --- | --- | --- | --- | --- | --- |
| `ind007` | 硬度 | 360 | 440 | 350 | 450 | HV |
| `ind001` | 抗拉强度 | 1200 | 空 | 空 | 空 | MPa |
| `ind002` | 延伸率 | 8 | 空 | 7 | 空 | % |
| `ind004` | 厚度公差 | -0.3 | 0.3 | -0.35 | 0.35 | mm |

## 4. 五条检验记录

| 序号 | 场景 | 记录ID | 炉号 | 卷号 | 预期判定 |
| --- | --- | --- | --- | --- | --- |
| 1 | 全部关键指标处于合格范围内 | `td_ins_14_1` | `HT-TD-14-QUAL-001` | `COIL-TD-14-QUAL-001` | `QUALIFIED` |
| 2 | 关键指标取边界值，验证边界包含 | `td_ins_14_2` | `HT-TD-14-EDGE-001` | `COIL-TD-14-EDGE-001` | `QUALIFIED` |
| 3 | 首个关键指标轻微超出合格范围但位于让步范围内 | `td_ins_14_3` | `HT-TD-14-CONC-001` | `COIL-TD-14-CONC-001` | `CAN_CONCESSION` |
| 4 | 第二个关键指标超限且该方向未配置让步范围 | `td_ins_14_4` | `HT-TD-14-REINS-001` | `COIL-TD-14-REINS-001` | `NEED_REINSPECTION` |
| 5 | 首个关键指标超出让步范围 | `td_ins_14_5` | `HT-TD-14-UNQ-001` | `COIL-TD-14-UNQ-001` | `UNQUALIFIED` |

检验值明细：

| 记录ID | 硬度 | 抗拉强度 | 延伸率 | 厚度公差 |
| --- | --- | --- | --- | --- |
| `td_ins_14_1` | 400 | 1225 | 8.2 | 0 |
| `td_ins_14_2` | 360 | 1200 | 8 | -0.3 |
| `td_ins_14_3` | 355 | 1225 | 8.2 | 0 |
| `td_ins_14_4` | 400 | 1195 | 8.2 | 0 |
| `td_ins_14_5` | 339.99 | 1225 | 8.2 | 0 |

## 5. 使用说明

1. 直接执行本样例SQL可插入标准、指标、检验记录和检验值。
2. 直接SQL插入不会调用后端 `InspectionService.addRecord()`，因此不会自动生成 `qc_judgment_result`。
3. 如需验证规则引擎自动判定，请先执行本SQL中的标准和字典数据，随后按下方API payload通过页面或接口新增检验记录。
4. 结构化牌号统一使用 `QA0623` 后缀，PDF正文中的原牌号仅代表来源材料。

## 6. API Payloads

### td_ins_14_1

```json
{
  "heatNo": "HT-TD-14-QUAL-001",
  "coilNo": "COIL-TD-14-QUAL-001",
  "customerId": null,
  "productVariety": "调质耐磨钢板",
  "productGrade": "NM400-QA0623",
  "productSpec": "厚度20.00mm，宽度2200mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-26 09:42:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind007",
      "testValue": 400.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 1225.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 8.2
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    }
  ]
}
```

### td_ins_14_2

```json
{
  "heatNo": "HT-TD-14-EDGE-001",
  "coilNo": "COIL-TD-14-EDGE-001",
  "customerId": null,
  "productVariety": "调质耐磨钢板",
  "productGrade": "NM400-QA0623",
  "productSpec": "厚度20.00mm，宽度2200mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-26 10:42:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind007",
      "testValue": 360.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 1200.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 8.0
    },
    {
      "indicatorId": "ind004",
      "testValue": -0.3
    }
  ]
}
```

### td_ins_14_3

```json
{
  "heatNo": "HT-TD-14-CONC-001",
  "coilNo": "COIL-TD-14-CONC-001",
  "customerId": null,
  "productVariety": "调质耐磨钢板",
  "productGrade": "NM400-QA0623",
  "productSpec": "厚度20.00mm，宽度2200mm",
  "sampleType": "TAIL",
  "testTime": "2026-06-26 11:42:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind007",
      "testValue": 355.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 1225.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 8.2
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    }
  ]
}
```

### td_ins_14_4

```json
{
  "heatNo": "HT-TD-14-REINS-001",
  "coilNo": "COIL-TD-14-REINS-001",
  "customerId": null,
  "productVariety": "调质耐磨钢板",
  "productGrade": "NM400-QA0623",
  "productSpec": "厚度20.00mm，宽度2200mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-26 12:42:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind007",
      "testValue": 400.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 1195.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 8.2
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    }
  ]
}
```

### td_ins_14_5

```json
{
  "heatNo": "HT-TD-14-UNQ-001",
  "coilNo": "COIL-TD-14-UNQ-001",
  "customerId": null,
  "productVariety": "调质耐磨钢板",
  "productGrade": "NM400-QA0623",
  "productSpec": "厚度20.00mm，宽度2200mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-26 13:42:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind007",
      "testValue": 339.99
    },
    {
      "indicatorId": "ind001",
      "testValue": 1225.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 8.2
    },
    {
      "indicatorId": "ind004",
      "testValue": 0.0
    }
  ]
}
```
