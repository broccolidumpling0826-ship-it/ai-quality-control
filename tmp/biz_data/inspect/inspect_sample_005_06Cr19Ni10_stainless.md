# 检验记录样例 005 - 06Cr19Ni10 / 冷轧不锈钢板

## 1. 样例定位

| 项目 | 内容 |
| --- | --- |
| 样例编号 | `INSPECT-SAMPLE-005` |
| 来源PDF | `/Users/weixuelei/workspace/aiAgent/ai-project/for-test/std-doc/GB-T-SIM-4237-2026_06Cr19Ni10_stainless_sheet_national.pdf` |
| PDF标准编号 | `GB/T SIM 4237-2026` |
| SQL脚本 | `inspect_sample_005_06Cr19Ni10_stainless.sql` |
| 标准类型 | `NATIONAL` |
| 结构化牌号 | `06Cr19Ni10-QA0623` |
| 记录数量 | `5` |

## 2. 标准录入字段

| 字段 | 值 |
| --- | --- |
| 标准ID | `td_std_05_06cr19ni10_stainless` |
| 标准编号 | `GB/T SIM 4237-2026-QA0623` |
| 标准名称 | `06Cr19Ni10不锈钢冷轧钢板质量要求（QA0623隔离模拟国标）` |
| 品种 | `冷轧不锈钢板` |
| 牌号 | `06Cr19Ni10-QA0623` |
| 规格范围 | `厚度0.80mm-6.00mm，宽度800mm-1600mm` |
| 客户 | `空` |
| 生效日期 | `2026-04-01` |
| 失效日期 | `9999-12-31` |

## 3. 结构化指标

| 指标ID | 指标 | 合格下限 | 合格上限 | 让步下限 | 让步上限 | 单位 |
| --- | --- | --- | --- | --- | --- | --- |
| `td_ind_cr` | 铬 | 18 | 20 | 17.8 | 20.2 | % |
| `td_ind_ni` | 镍 | 8 | 11 | 空 | 空 | % |
| `ind001` | 抗拉强度 | 520 | 空 | 510 | 空 | MPa |
| `ind002` | 延伸率 | 40 | 空 | 38 | 空 | % |

## 4. 五条检验记录

| 序号 | 场景 | 记录ID | 炉号 | 卷号 | 预期判定 |
| --- | --- | --- | --- | --- | --- |
| 1 | 全部关键指标处于合格范围内 | `td_ins_05_1` | `HT-TD-05-QUAL-001` | `COIL-TD-05-QUAL-001` | `QUALIFIED` |
| 2 | 关键指标取边界值，验证边界包含 | `td_ins_05_2` | `HT-TD-05-EDGE-001` | `COIL-TD-05-EDGE-001` | `QUALIFIED` |
| 3 | 首个关键指标轻微超出合格范围但位于让步范围内 | `td_ins_05_3` | `HT-TD-05-CONC-001` | `COIL-TD-05-CONC-001` | `CAN_CONCESSION` |
| 4 | 第二个关键指标超限且该方向未配置让步范围 | `td_ins_05_4` | `HT-TD-05-REINS-001` | `COIL-TD-05-REINS-001` | `NEED_REINSPECTION` |
| 5 | 首个关键指标超出让步范围 | `td_ins_05_5` | `HT-TD-05-UNQ-001` | `COIL-TD-05-UNQ-001` | `UNQUALIFIED` |

检验值明细：

| 记录ID | 铬 | 镍 | 抗拉强度 | 延伸率 |
| --- | --- | --- | --- | --- |
| `td_ins_05_1` | 19 | 9.5 | 545 | 42 |
| `td_ins_05_2` | 18 | 8 | 520 | 40 |
| `td_ins_05_3` | 17.9 | 9.5 | 545 | 42 |
| `td_ins_05_4` | 19 | 7.5 | 545 | 42 |
| `td_ins_05_5` | 17.59 | 9.5 | 545 | 42 |

## 5. 使用说明

1. 直接执行本样例SQL可插入标准、指标、检验记录和检验值。
2. 直接SQL插入不会调用后端 `InspectionService.addRecord()`，因此不会自动生成 `qc_judgment_result`。
3. 如需验证规则引擎自动判定，请先执行本SQL中的标准和字典数据，随后按下方API payload通过页面或接口新增检验记录。
4. 结构化牌号统一使用 `QA0623` 后缀，PDF正文中的原牌号仅代表来源材料。

## 6. API Payloads

### td_ins_05_1

```json
{
  "heatNo": "HT-TD-05-QUAL-001",
  "coilNo": "COIL-TD-05-QUAL-001",
  "customerId": null,
  "productVariety": "冷轧不锈钢板",
  "productGrade": "06Cr19Ni10-QA0623",
  "productSpec": "厚度2.00mm，宽度1219mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-24 09:15:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "td_ind_cr",
      "testValue": 19.0
    },
    {
      "indicatorId": "td_ind_ni",
      "testValue": 9.5
    },
    {
      "indicatorId": "ind001",
      "testValue": 545.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 42.0
    }
  ]
}
```

### td_ins_05_2

```json
{
  "heatNo": "HT-TD-05-EDGE-001",
  "coilNo": "COIL-TD-05-EDGE-001",
  "customerId": null,
  "productVariety": "冷轧不锈钢板",
  "productGrade": "06Cr19Ni10-QA0623",
  "productSpec": "厚度2.00mm，宽度1219mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-24 10:15:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "td_ind_cr",
      "testValue": 18.0
    },
    {
      "indicatorId": "td_ind_ni",
      "testValue": 8.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 520.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 40.0
    }
  ]
}
```

### td_ins_05_3

```json
{
  "heatNo": "HT-TD-05-CONC-001",
  "coilNo": "COIL-TD-05-CONC-001",
  "customerId": null,
  "productVariety": "冷轧不锈钢板",
  "productGrade": "06Cr19Ni10-QA0623",
  "productSpec": "厚度2.00mm，宽度1219mm",
  "sampleType": "TAIL",
  "testTime": "2026-06-24 11:15:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "td_ind_cr",
      "testValue": 17.9
    },
    {
      "indicatorId": "td_ind_ni",
      "testValue": 9.5
    },
    {
      "indicatorId": "ind001",
      "testValue": 545.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 42.0
    }
  ]
}
```

### td_ins_05_4

```json
{
  "heatNo": "HT-TD-05-REINS-001",
  "coilNo": "COIL-TD-05-REINS-001",
  "customerId": null,
  "productVariety": "冷轧不锈钢板",
  "productGrade": "06Cr19Ni10-QA0623",
  "productSpec": "厚度2.00mm，宽度1219mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-24 12:15:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "td_ind_cr",
      "testValue": 19.0
    },
    {
      "indicatorId": "td_ind_ni",
      "testValue": 7.5
    },
    {
      "indicatorId": "ind001",
      "testValue": 545.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 42.0
    }
  ]
}
```

### td_ins_05_5

```json
{
  "heatNo": "HT-TD-05-UNQ-001",
  "coilNo": "COIL-TD-05-UNQ-001",
  "customerId": null,
  "productVariety": "冷轧不锈钢板",
  "productGrade": "06Cr19Ni10-QA0623",
  "productSpec": "厚度2.00mm，宽度1219mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-24 13:15:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "td_ind_cr",
      "testValue": 17.59
    },
    {
      "indicatorId": "td_ind_ni",
      "testValue": 9.5
    },
    {
      "indicatorId": "ind001",
      "testValue": 545.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 42.0
    }
  ]
}
```
