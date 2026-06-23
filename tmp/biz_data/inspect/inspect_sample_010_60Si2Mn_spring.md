# 检验记录样例 010 - 60Si2Mn / 热轧弹簧扁钢

## 1. 样例定位

| 项目 | 内容 |
| --- | --- |
| 样例编号 | `INSPECT-SAMPLE-010` |
| 来源PDF | `/Users/weixuelei/workspace/aiAgent/ai-project/for-test/std-doc/GB-T-SIM-1222-2026_60Si2Mn_spring_flat_steel_national.pdf` |
| PDF标准编号 | `GB/T SIM 1222-2026` |
| SQL脚本 | `inspect_sample_010_60Si2Mn_spring.sql` |
| 标准类型 | `NATIONAL` |
| 结构化牌号 | `60Si2Mn-QA0623` |
| 记录数量 | `5` |

## 2. 标准录入字段

| 字段 | 值 |
| --- | --- |
| 标准ID | `td_std_10_60si2mn_spring` |
| 标准编号 | `GB/T SIM 1222-2026-QA0623` |
| 标准名称 | `60Si2Mn热轧弹簧扁钢质量要求（QA0623隔离模拟国标）` |
| 品种 | `热轧弹簧扁钢` |
| 牌号 | `60Si2Mn-QA0623` |
| 规格范围 | `厚度5.00mm-30.00mm，宽度40mm-160mm` |
| 客户 | `空` |
| 生效日期 | `2026-04-01` |
| 失效日期 | `9999-12-31` |

## 3. 结构化指标

| 指标ID | 指标 | 合格下限 | 合格上限 | 让步下限 | 让步上限 | 单位 |
| --- | --- | --- | --- | --- | --- | --- |
| `td_ind_c` | 碳 | 0.56 | 0.64 | 0.55 | 0.65 | % |
| `td_ind_si` | 硅 | 1.5 | 2 | 空 | 空 | % |
| `td_ind_mn` | 锰 | 0.6 | 0.9 | 0.58 | 0.92 | % |
| `ind007` | 硬度 | 300 | 380 | 290 | 390 | HV |

## 4. 五条检验记录

| 序号 | 场景 | 记录ID | 炉号 | 卷号 | 预期判定 |
| --- | --- | --- | --- | --- | --- |
| 1 | 全部关键指标处于合格范围内 | `td_ins_10_1` | `HT-TD-10-QUAL-001` | `COIL-TD-10-QUAL-001` | `QUALIFIED` |
| 2 | 关键指标取边界值，验证边界包含 | `td_ins_10_2` | `HT-TD-10-EDGE-001` | `COIL-TD-10-EDGE-001` | `QUALIFIED` |
| 3 | 首个关键指标轻微超出合格范围但位于让步范围内 | `td_ins_10_3` | `HT-TD-10-CONC-001` | `COIL-TD-10-CONC-001` | `CAN_CONCESSION` |
| 4 | 第二个关键指标超限且该方向未配置让步范围 | `td_ins_10_4` | `HT-TD-10-REINS-001` | `COIL-TD-10-REINS-001` | `NEED_REINSPECTION` |
| 5 | 首个关键指标超出让步范围 | `td_ins_10_5` | `HT-TD-10-UNQ-001` | `COIL-TD-10-UNQ-001` | `UNQUALIFIED` |

检验值明细：

| 记录ID | 碳 | 硅 | 锰 | 硬度 |
| --- | --- | --- | --- | --- |
| `td_ins_10_1` | 0.6 | 1.75 | 0.75 | 340 |
| `td_ins_10_2` | 0.56 | 1.5 | 0.6 | 300 |
| `td_ins_10_3` | 0.555 | 1.75 | 0.75 | 340 |
| `td_ins_10_4` | 0.6 | 1 | 0.75 | 340 |
| `td_ins_10_5` | 0.53 | 1.75 | 0.75 | 340 |

## 5. 使用说明

1. 直接执行本样例SQL可插入标准、指标、检验记录和检验值。
2. 直接SQL插入不会调用后端 `InspectionService.addRecord()`，因此不会自动生成 `qc_judgment_result`。
3. 如需验证规则引擎自动判定，请先执行本SQL中的标准和字典数据，随后按下方API payload通过页面或接口新增检验记录。
4. 结构化牌号统一使用 `QA0623` 后缀，PDF正文中的原牌号仅代表来源材料。

## 6. API Payloads

### td_ins_10_1

```json
{
  "heatNo": "HT-TD-10-QUAL-001",
  "coilNo": "COIL-TD-10-QUAL-001",
  "customerId": null,
  "productVariety": "热轧弹簧扁钢",
  "productGrade": "60Si2Mn-QA0623",
  "productSpec": "厚度12.00mm，宽度80mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-25 09:30:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "td_ind_c",
      "testValue": 0.6
    },
    {
      "indicatorId": "td_ind_si",
      "testValue": 1.75
    },
    {
      "indicatorId": "td_ind_mn",
      "testValue": 0.75
    },
    {
      "indicatorId": "ind007",
      "testValue": 340.0
    }
  ]
}
```

### td_ins_10_2

```json
{
  "heatNo": "HT-TD-10-EDGE-001",
  "coilNo": "COIL-TD-10-EDGE-001",
  "customerId": null,
  "productVariety": "热轧弹簧扁钢",
  "productGrade": "60Si2Mn-QA0623",
  "productSpec": "厚度12.00mm，宽度80mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-25 10:30:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "td_ind_c",
      "testValue": 0.56
    },
    {
      "indicatorId": "td_ind_si",
      "testValue": 1.5
    },
    {
      "indicatorId": "td_ind_mn",
      "testValue": 0.6
    },
    {
      "indicatorId": "ind007",
      "testValue": 300.0
    }
  ]
}
```

### td_ins_10_3

```json
{
  "heatNo": "HT-TD-10-CONC-001",
  "coilNo": "COIL-TD-10-CONC-001",
  "customerId": null,
  "productVariety": "热轧弹簧扁钢",
  "productGrade": "60Si2Mn-QA0623",
  "productSpec": "厚度12.00mm，宽度80mm",
  "sampleType": "TAIL",
  "testTime": "2026-06-25 11:30:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "td_ind_c",
      "testValue": 0.555
    },
    {
      "indicatorId": "td_ind_si",
      "testValue": 1.75
    },
    {
      "indicatorId": "td_ind_mn",
      "testValue": 0.75
    },
    {
      "indicatorId": "ind007",
      "testValue": 340.0
    }
  ]
}
```

### td_ins_10_4

```json
{
  "heatNo": "HT-TD-10-REINS-001",
  "coilNo": "COIL-TD-10-REINS-001",
  "customerId": null,
  "productVariety": "热轧弹簧扁钢",
  "productGrade": "60Si2Mn-QA0623",
  "productSpec": "厚度12.00mm，宽度80mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-25 12:30:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "td_ind_c",
      "testValue": 0.6
    },
    {
      "indicatorId": "td_ind_si",
      "testValue": 1.0
    },
    {
      "indicatorId": "td_ind_mn",
      "testValue": 0.75
    },
    {
      "indicatorId": "ind007",
      "testValue": 340.0
    }
  ]
}
```

### td_ins_10_5

```json
{
  "heatNo": "HT-TD-10-UNQ-001",
  "coilNo": "COIL-TD-10-UNQ-001",
  "customerId": null,
  "productVariety": "热轧弹簧扁钢",
  "productGrade": "60Si2Mn-QA0623",
  "productSpec": "厚度12.00mm，宽度80mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-25 13:30:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "td_ind_c",
      "testValue": 0.53
    },
    {
      "indicatorId": "td_ind_si",
      "testValue": 1.75
    },
    {
      "indicatorId": "td_ind_mn",
      "testValue": 0.75
    },
    {
      "indicatorId": "ind007",
      "testValue": 340.0
    }
  ]
}
```
