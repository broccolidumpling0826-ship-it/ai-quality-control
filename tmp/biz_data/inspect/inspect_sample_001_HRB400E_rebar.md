# 检验记录样例 001 - HRB400E / 热轧带肋钢筋

## 1. 样例定位

| 项目 | 内容 |
| --- | --- |
| 样例编号 | `INSPECT-SAMPLE-001` |
| 来源PDF | `/Users/weixuelei/workspace/aiAgent/ai-project/for-test/std-doc/GB-T-SIM-1499-2026_HRB400E_rebar_national.pdf` |
| PDF标准编号 | `GB/T SIM 1499-2026` |
| SQL脚本 | `inspect_sample_001_HRB400E_rebar.sql` |
| 标准类型 | `NATIONAL` |
| 结构化牌号 | `HRB400E-QA0623` |
| 记录数量 | `5` |

## 2. 标准录入字段

| 字段 | 值 |
| --- | --- |
| 标准ID | `td_std_01_hrb400e_rebar` |
| 标准编号 | `GB/T SIM 1499-2026-QA0623` |
| 标准名称 | `HRB400E热轧带肋钢筋质量要求（QA0623隔离模拟国标）` |
| 品种 | `热轧带肋钢筋` |
| 牌号 | `HRB400E-QA0623` |
| 规格范围 | `公称直径12mm-32mm` |
| 客户 | `空` |
| 生效日期 | `2026-04-01` |
| 失效日期 | `9999-12-31` |

## 3. 结构化指标

| 指标ID | 指标 | 合格下限 | 合格上限 | 让步下限 | 让步上限 | 单位 |
| --- | --- | --- | --- | --- | --- | --- |
| `ind003` | 屈服强度 | 400 | 空 | 392 | 空 | MPa |
| `ind001` | 抗拉强度 | 540 | 空 | 空 | 空 | MPa |
| `ind002` | 延伸率 | 16 | 空 | 15 | 空 | % |
| `td_ind_wdev` | 重量偏差 | -6 | 6 | -6.5 | 6.5 | % |

## 4. 五条检验记录

| 序号 | 场景 | 记录ID | 炉号 | 卷号 | 预期判定 |
| --- | --- | --- | --- | --- | --- |
| 1 | 全部关键指标处于合格范围内 | `td_ins_01_1` | `HT-TD-01-QUAL-001` | `COIL-TD-01-QUAL-001` | `QUALIFIED` |
| 2 | 关键指标取边界值，验证边界包含 | `td_ins_01_2` | `HT-TD-01-EDGE-001` | `COIL-TD-01-EDGE-001` | `QUALIFIED` |
| 3 | 首个关键指标轻微超出合格范围但位于让步范围内 | `td_ins_01_3` | `HT-TD-01-CONC-001` | `COIL-TD-01-CONC-001` | `CAN_CONCESSION` |
| 4 | 第二个关键指标超限且该方向未配置让步范围 | `td_ins_01_4` | `HT-TD-01-REINS-001` | `COIL-TD-01-REINS-001` | `NEED_REINSPECTION` |
| 5 | 首个关键指标超出让步范围 | `td_ins_01_5` | `HT-TD-01-UNQ-001` | `COIL-TD-01-UNQ-001` | `UNQUALIFIED` |

检验值明细：

| 记录ID | 屈服强度 | 抗拉强度 | 延伸率 | 重量偏差 |
| --- | --- | --- | --- | --- |
| `td_ins_01_1` | 425 | 565 | 18 | 0 |
| `td_ins_01_2` | 400 | 540 | 16 | -6 |
| `td_ins_01_3` | 396 | 565 | 18 | 0 |
| `td_ins_01_4` | 425 | 535 | 18 | 0 |
| `td_ins_01_5` | 383.99 | 565 | 18 | 0 |

## 5. 使用说明

1. 直接执行本样例SQL可插入标准、指标、检验记录和检验值。
2. 直接SQL插入不会调用后端 `InspectionService.addRecord()`，因此不会自动生成 `qc_judgment_result`。
3. 如需验证规则引擎自动判定，请先执行本SQL中的标准和字典数据，随后按下方API payload通过页面或接口新增检验记录。
4. 结构化牌号统一使用 `QA0623` 后缀，PDF正文中的原牌号仅代表来源材料。

## 6. API Payloads

### td_ins_01_1

```json
{
  "heatNo": "HT-TD-01-QUAL-001",
  "coilNo": "COIL-TD-01-QUAL-001",
  "customerId": null,
  "productVariety": "热轧带肋钢筋",
  "productGrade": "HRB400E-QA0623",
  "productSpec": "公称直径16mm，定尺12m",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-23 09:03:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 425.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 565.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 18.0
    },
    {
      "indicatorId": "td_ind_wdev",
      "testValue": 0.0
    }
  ]
}
```

### td_ins_01_2

```json
{
  "heatNo": "HT-TD-01-EDGE-001",
  "coilNo": "COIL-TD-01-EDGE-001",
  "customerId": null,
  "productVariety": "热轧带肋钢筋",
  "productGrade": "HRB400E-QA0623",
  "productSpec": "公称直径16mm，定尺12m",
  "sampleType": "HEAD",
  "testTime": "2026-06-23 10:03:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 400.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 540.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 16.0
    },
    {
      "indicatorId": "td_ind_wdev",
      "testValue": -6.0
    }
  ]
}
```

### td_ins_01_3

```json
{
  "heatNo": "HT-TD-01-CONC-001",
  "coilNo": "COIL-TD-01-CONC-001",
  "customerId": null,
  "productVariety": "热轧带肋钢筋",
  "productGrade": "HRB400E-QA0623",
  "productSpec": "公称直径16mm，定尺12m",
  "sampleType": "TAIL",
  "testTime": "2026-06-23 11:03:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 396.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 565.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 18.0
    },
    {
      "indicatorId": "td_ind_wdev",
      "testValue": 0.0
    }
  ]
}
```

### td_ins_01_4

```json
{
  "heatNo": "HT-TD-01-REINS-001",
  "coilNo": "COIL-TD-01-REINS-001",
  "customerId": null,
  "productVariety": "热轧带肋钢筋",
  "productGrade": "HRB400E-QA0623",
  "productSpec": "公称直径16mm，定尺12m",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-23 12:03:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 425.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 535.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 18.0
    },
    {
      "indicatorId": "td_ind_wdev",
      "testValue": 0.0
    }
  ]
}
```

### td_ins_01_5

```json
{
  "heatNo": "HT-TD-01-UNQ-001",
  "coilNo": "COIL-TD-01-UNQ-001",
  "customerId": null,
  "productVariety": "热轧带肋钢筋",
  "productGrade": "HRB400E-QA0623",
  "productSpec": "公称直径16mm，定尺12m",
  "sampleType": "HEAD",
  "testTime": "2026-06-23 13:03:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 383.99
    },
    {
      "indicatorId": "ind001",
      "testValue": 565.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 18.0
    },
    {
      "indicatorId": "td_ind_wdev",
      "testValue": 0.0
    }
  ]
}
```
