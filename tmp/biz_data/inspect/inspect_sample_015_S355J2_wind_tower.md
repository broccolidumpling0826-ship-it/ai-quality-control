# 检验记录样例 015 - S355J2 / 风电塔筒用中厚板

## 1. 样例定位

| 项目 | 内容 |
| --- | --- |
| 样例编号 | `INSPECT-SAMPLE-015` |
| 来源PDF | `/Users/weixuelei/workspace/aiAgent/ai-project/for-test/std-doc/Q-JH-STEEL-SIM-050-2026_S355J2_wind_tower_plate_enterprise.pdf` |
| PDF标准编号 | `Q/JH STEEL SIM 050-2026` |
| SQL脚本 | `inspect_sample_015_S355J2_wind_tower.sql` |
| 标准类型 | `ENTERPRISE` |
| 结构化牌号 | `S355J2-QA0623` |
| 记录数量 | `5` |

## 2. 标准录入字段

| 字段 | 值 |
| --- | --- |
| 标准ID | `td_std_15_s355j2_wind_tower` |
| 标准编号 | `Q/JH STEEL SIM 050-2026-QA0623` |
| 标准名称 | `S355J2风电塔筒用中厚板企业标准（QA0623隔离模拟企标）` |
| 品种 | `风电塔筒用中厚板` |
| 牌号 | `S355J2-QA0623` |
| 规格范围 | `厚度8.00mm-50.00mm，宽度1800mm-3200mm` |
| 客户 | `空` |
| 生效日期 | `2026-03-01` |
| 失效日期 | `9999-12-31` |

## 3. 结构化指标

| 指标ID | 指标 | 合格下限 | 合格上限 | 让步下限 | 让步上限 | 单位 |
| --- | --- | --- | --- | --- | --- | --- |
| `ind003` | 屈服强度 | 355 | 空 | 345 | 空 | MPa |
| `ind001` | 抗拉强度 | 470 | 630 | 空 | 空 | MPa |
| `ind002` | 延伸率 | 22 | 空 | 21 | 空 | % |
| `td_ind_kv2` | 冲击功KV2 | 27 | 空 | 24 | 空 | J |

## 4. 五条检验记录

| 序号 | 场景 | 记录ID | 炉号 | 卷号 | 预期判定 |
| --- | --- | --- | --- | --- | --- |
| 1 | 全部关键指标处于合格范围内 | `td_ins_15_1` | `HT-TD-15-QUAL-001` | `COIL-TD-15-QUAL-001` | `QUALIFIED` |
| 2 | 关键指标取边界值，验证边界包含 | `td_ins_15_2` | `HT-TD-15-EDGE-001` | `COIL-TD-15-EDGE-001` | `QUALIFIED` |
| 3 | 首个关键指标轻微超出合格范围但位于让步范围内 | `td_ins_15_3` | `HT-TD-15-CONC-001` | `COIL-TD-15-CONC-001` | `CAN_CONCESSION` |
| 4 | 第二个关键指标超限且该方向未配置让步范围 | `td_ins_15_4` | `HT-TD-15-REINS-001` | `COIL-TD-15-REINS-001` | `NEED_REINSPECTION` |
| 5 | 首个关键指标超出让步范围 | `td_ins_15_5` | `HT-TD-15-UNQ-001` | `COIL-TD-15-UNQ-001` | `UNQUALIFIED` |

检验值明细：

| 记录ID | 屈服强度 | 抗拉强度 | 延伸率 | 冲击功KV2 |
| --- | --- | --- | --- | --- |
| `td_ins_15_1` | 380 | 550 | 24 | 29 |
| `td_ins_15_2` | 355 | 470 | 22 | 27 |
| `td_ins_15_3` | 350 | 550 | 24 | 29 |
| `td_ins_15_4` | 380 | 465 | 24 | 29 |
| `td_ins_15_5` | 334.99 | 550 | 24 | 29 |

## 5. 使用说明

1. 直接执行本样例SQL可插入标准、指标、检验记录和检验值。
2. 直接SQL插入不会调用后端 `InspectionService.addRecord()`，因此不会自动生成 `qc_judgment_result`。
3. 如需验证规则引擎自动判定，请先执行本SQL中的标准和字典数据，随后按下方API payload通过页面或接口新增检验记录。
4. 结构化牌号统一使用 `QA0623` 后缀，PDF正文中的原牌号仅代表来源材料。

## 6. API Payloads

### td_ins_15_1

```json
{
  "heatNo": "HT-TD-15-QUAL-001",
  "coilNo": "COIL-TD-15-QUAL-001",
  "customerId": null,
  "productVariety": "风电塔筒用中厚板",
  "productGrade": "S355J2-QA0623",
  "productSpec": "厚度24.00mm，宽度2500mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-26 09:45:00",
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
      "testValue": 24.0
    },
    {
      "indicatorId": "td_ind_kv2",
      "testValue": 29.0
    }
  ]
}
```

### td_ins_15_2

```json
{
  "heatNo": "HT-TD-15-EDGE-001",
  "coilNo": "COIL-TD-15-EDGE-001",
  "customerId": null,
  "productVariety": "风电塔筒用中厚板",
  "productGrade": "S355J2-QA0623",
  "productSpec": "厚度24.00mm，宽度2500mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-26 10:45:00",
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
      "testValue": 22.0
    },
    {
      "indicatorId": "td_ind_kv2",
      "testValue": 27.0
    }
  ]
}
```

### td_ins_15_3

```json
{
  "heatNo": "HT-TD-15-CONC-001",
  "coilNo": "COIL-TD-15-CONC-001",
  "customerId": null,
  "productVariety": "风电塔筒用中厚板",
  "productGrade": "S355J2-QA0623",
  "productSpec": "厚度24.00mm，宽度2500mm",
  "sampleType": "TAIL",
  "testTime": "2026-06-26 11:45:00",
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
      "testValue": 24.0
    },
    {
      "indicatorId": "td_ind_kv2",
      "testValue": 29.0
    }
  ]
}
```

### td_ins_15_4

```json
{
  "heatNo": "HT-TD-15-REINS-001",
  "coilNo": "COIL-TD-15-REINS-001",
  "customerId": null,
  "productVariety": "风电塔筒用中厚板",
  "productGrade": "S355J2-QA0623",
  "productSpec": "厚度24.00mm，宽度2500mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-26 12:45:00",
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
      "testValue": 24.0
    },
    {
      "indicatorId": "td_ind_kv2",
      "testValue": 29.0
    }
  ]
}
```

### td_ins_15_5

```json
{
  "heatNo": "HT-TD-15-UNQ-001",
  "coilNo": "COIL-TD-15-UNQ-001",
  "customerId": null,
  "productVariety": "风电塔筒用中厚板",
  "productGrade": "S355J2-QA0623",
  "productSpec": "厚度24.00mm，宽度2500mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-26 13:45:00",
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
      "testValue": 24.0
    },
    {
      "indicatorId": "td_ind_kv2",
      "testValue": 29.0
    }
  ]
}
```
