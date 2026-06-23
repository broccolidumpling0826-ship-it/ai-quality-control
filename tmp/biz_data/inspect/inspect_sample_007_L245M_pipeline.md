# 检验记录样例 007 - L245M / 管线钢板卷

## 1. 样例定位

| 项目 | 内容 |
| --- | --- |
| 样例编号 | `INSPECT-SAMPLE-007` |
| 来源PDF | `/Users/weixuelei/workspace/aiAgent/ai-project/for-test/std-doc/GB-T-SIM-9711-2026_L245M_pipeline_steel_national.pdf` |
| PDF标准编号 | `GB/T SIM 9711-2026` |
| SQL脚本 | `inspect_sample_007_L245M_pipeline.sql` |
| 标准类型 | `NATIONAL` |
| 结构化牌号 | `L245M-QA0623` |
| 记录数量 | `5` |

## 2. 标准录入字段

| 字段 | 值 |
| --- | --- |
| 标准ID | `td_std_07_l245m_pipeline` |
| 标准编号 | `GB/T SIM 9711-2026-QA0623` |
| 标准名称 | `L245M管线钢板卷质量要求（QA0623隔离模拟国标）` |
| 品种 | `管线钢板卷` |
| 牌号 | `L245M-QA0623` |
| 规格范围 | `厚度4.00mm-16.00mm，宽度1000mm-1800mm` |
| 客户 | `空` |
| 生效日期 | `2026-04-01` |
| 失效日期 | `9999-12-31` |

## 3. 结构化指标

| 指标ID | 指标 | 合格下限 | 合格上限 | 让步下限 | 让步上限 | 单位 |
| --- | --- | --- | --- | --- | --- | --- |
| `ind003` | 屈服强度 | 245 | 450 | 235 | 460 | MPa |
| `ind001` | 抗拉强度 | 415 | 760 | 空 | 空 | MPa |
| `ind002` | 延伸率 | 22 | 空 | 20 | 空 | % |
| `td_ind_ceq` | 碳当量 | 空 | 0.43 | 空 | 0.45 | % |

## 4. 五条检验记录

| 序号 | 场景 | 记录ID | 炉号 | 卷号 | 预期判定 |
| --- | --- | --- | --- | --- | --- |
| 1 | 全部关键指标处于合格范围内 | `td_ins_07_1` | `HT-TD-07-QUAL-001` | `COIL-TD-07-QUAL-001` | `QUALIFIED` |
| 2 | 关键指标取边界值，验证边界包含 | `td_ins_07_2` | `HT-TD-07-EDGE-001` | `COIL-TD-07-EDGE-001` | `QUALIFIED` |
| 3 | 首个关键指标轻微超出合格范围但位于让步范围内 | `td_ins_07_3` | `HT-TD-07-CONC-001` | `COIL-TD-07-CONC-001` | `CAN_CONCESSION` |
| 4 | 第二个关键指标超限且该方向未配置让步范围 | `td_ins_07_4` | `HT-TD-07-REINS-001` | `COIL-TD-07-REINS-001` | `NEED_REINSPECTION` |
| 5 | 首个关键指标超出让步范围 | `td_ins_07_5` | `HT-TD-07-UNQ-001` | `COIL-TD-07-UNQ-001` | `UNQUALIFIED` |

检验值明细：

| 记录ID | 屈服强度 | 抗拉强度 | 延伸率 | 碳当量 |
| --- | --- | --- | --- | --- |
| `td_ins_07_1` | 347.5 | 587.5 | 24 | 0.41 |
| `td_ins_07_2` | 245 | 415 | 22 | 0.43 |
| `td_ins_07_3` | 240 | 587.5 | 24 | 0.41 |
| `td_ins_07_4` | 347.5 | 410 | 24 | 0.41 |
| `td_ins_07_5` | 224.99 | 587.5 | 24 | 0.41 |

## 5. 使用说明

1. 直接执行本样例SQL可插入标准、指标、检验记录和检验值。
2. 直接SQL插入不会调用后端 `InspectionService.addRecord()`，因此不会自动生成 `qc_judgment_result`。
3. 如需验证规则引擎自动判定，请先执行本SQL中的标准和字典数据，随后按下方API payload通过页面或接口新增检验记录。
4. 结构化牌号统一使用 `QA0623` 后缀，PDF正文中的原牌号仅代表来源材料。

## 6. API Payloads

### td_ins_07_1

```json
{
  "heatNo": "HT-TD-07-QUAL-001",
  "coilNo": "COIL-TD-07-QUAL-001",
  "customerId": null,
  "productVariety": "管线钢板卷",
  "productGrade": "L245M-QA0623",
  "productSpec": "厚度8.00mm，宽度1550mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-24 09:21:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 347.5
    },
    {
      "indicatorId": "ind001",
      "testValue": 587.5
    },
    {
      "indicatorId": "ind002",
      "testValue": 24.0
    },
    {
      "indicatorId": "td_ind_ceq",
      "testValue": 0.41
    }
  ]
}
```

### td_ins_07_2

```json
{
  "heatNo": "HT-TD-07-EDGE-001",
  "coilNo": "COIL-TD-07-EDGE-001",
  "customerId": null,
  "productVariety": "管线钢板卷",
  "productGrade": "L245M-QA0623",
  "productSpec": "厚度8.00mm，宽度1550mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-24 10:21:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 245.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 415.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 22.0
    },
    {
      "indicatorId": "td_ind_ceq",
      "testValue": 0.43
    }
  ]
}
```

### td_ins_07_3

```json
{
  "heatNo": "HT-TD-07-CONC-001",
  "coilNo": "COIL-TD-07-CONC-001",
  "customerId": null,
  "productVariety": "管线钢板卷",
  "productGrade": "L245M-QA0623",
  "productSpec": "厚度8.00mm，宽度1550mm",
  "sampleType": "TAIL",
  "testTime": "2026-06-24 11:21:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 240.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 587.5
    },
    {
      "indicatorId": "ind002",
      "testValue": 24.0
    },
    {
      "indicatorId": "td_ind_ceq",
      "testValue": 0.41
    }
  ]
}
```

### td_ins_07_4

```json
{
  "heatNo": "HT-TD-07-REINS-001",
  "coilNo": "COIL-TD-07-REINS-001",
  "customerId": null,
  "productVariety": "管线钢板卷",
  "productGrade": "L245M-QA0623",
  "productSpec": "厚度8.00mm，宽度1550mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-24 12:21:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 347.5
    },
    {
      "indicatorId": "ind001",
      "testValue": 410.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 24.0
    },
    {
      "indicatorId": "td_ind_ceq",
      "testValue": 0.41
    }
  ]
}
```

### td_ins_07_5

```json
{
  "heatNo": "HT-TD-07-UNQ-001",
  "coilNo": "COIL-TD-07-UNQ-001",
  "customerId": null,
  "productVariety": "管线钢板卷",
  "productGrade": "L245M-QA0623",
  "productSpec": "厚度8.00mm，宽度1550mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-24 13:21:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 224.99
    },
    {
      "indicatorId": "ind001",
      "testValue": 587.5
    },
    {
      "indicatorId": "ind002",
      "testValue": 24.0
    },
    {
      "indicatorId": "td_ind_ceq",
      "testValue": 0.41
    }
  ]
}
```
