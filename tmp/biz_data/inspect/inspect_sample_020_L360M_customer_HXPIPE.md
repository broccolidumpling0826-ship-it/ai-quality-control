# 检验记录样例 020 - L360M / 管线钢热轧卷

## 1. 样例定位

| 项目 | 内容 |
| --- | --- |
| 样例编号 | `INSPECT-SAMPLE-020` |
| 来源PDF | `/Users/weixuelei/workspace/aiAgent/ai-project/for-test/std-doc/CUST-HXPIPE-SIM-2026-L360M_customer_agreement.pdf` |
| PDF标准编号 | `AGREEMENT HXPIPE-SIM-2026-005` |
| SQL脚本 | `inspect_sample_020_L360M_customer_HXPIPE.sql` |
| 标准类型 | `CUSTOMER` |
| 结构化牌号 | `L360M-QA0623` |
| 记录数量 | `5` |

## 2. 标准录入字段

| 字段 | 值 |
| --- | --- |
| 标准ID | `td_std_20_l360m_customer_hxpipe` |
| 标准编号 | `AGREEMENT HXPIPE-SIM-2026-005-QA0623` |
| 标准名称 | `华信管业L360M管线钢热轧卷供货质量协议（QA0623隔离模拟客户协议）` |
| 品种 | `管线钢热轧卷` |
| 牌号 | `L360M-QA0623` |
| 规格范围 | `厚度5.00mm-18.00mm，宽度1000mm-1800mm` |
| 客户 | `TD-CUST-HXPIPE` |
| 生效日期 | `2026-03-01` |
| 失效日期 | `2026-12-31` |

## 3. 结构化指标

| 指标ID | 指标 | 合格下限 | 合格上限 | 让步下限 | 让步上限 | 单位 |
| --- | --- | --- | --- | --- | --- | --- |
| `ind003` | 屈服强度 | 360 | 510 | 350 | 520 | MPa |
| `ind001` | 抗拉强度 | 460 | 650 | 空 | 空 | MPa |
| `ind002` | 延伸率 | 20 | 空 | 19 | 空 | % |
| `td_ind_ceq` | 碳当量 | 空 | 0.42 | 空 | 0.44 | % |

## 4. 五条检验记录

| 序号 | 场景 | 记录ID | 炉号 | 卷号 | 预期判定 |
| --- | --- | --- | --- | --- | --- |
| 1 | 全部关键指标处于合格范围内 | `td_ins_20_1` | `HT-TD-20-QUAL-001` | `COIL-TD-20-QUAL-001` | `QUALIFIED` |
| 2 | 关键指标取边界值，验证边界包含 | `td_ins_20_2` | `HT-TD-20-EDGE-001` | `COIL-TD-20-EDGE-001` | `QUALIFIED` |
| 3 | 首个关键指标轻微超出合格范围但位于让步范围内 | `td_ins_20_3` | `HT-TD-20-CONC-001` | `COIL-TD-20-CONC-001` | `CAN_CONCESSION` |
| 4 | 第二个关键指标超限且该方向未配置让步范围 | `td_ins_20_4` | `HT-TD-20-REINS-001` | `COIL-TD-20-REINS-001` | `NEED_REINSPECTION` |
| 5 | 首个关键指标超出让步范围 | `td_ins_20_5` | `HT-TD-20-UNQ-001` | `COIL-TD-20-UNQ-001` | `UNQUALIFIED` |

检验值明细：

| 记录ID | 屈服强度 | 抗拉强度 | 延伸率 | 碳当量 |
| --- | --- | --- | --- | --- |
| `td_ins_20_1` | 435 | 555 | 22 | 0.4 |
| `td_ins_20_2` | 360 | 460 | 20 | 0.42 |
| `td_ins_20_3` | 355 | 555 | 22 | 0.4 |
| `td_ins_20_4` | 435 | 455 | 22 | 0.4 |
| `td_ins_20_5` | 339.99 | 555 | 22 | 0.4 |

## 5. 使用说明

1. 直接执行本样例SQL可插入标准、指标、检验记录和检验值。
2. 直接SQL插入不会调用后端 `InspectionService.addRecord()`，因此不会自动生成 `qc_judgment_result`。
3. 如需验证规则引擎自动判定，请先执行本SQL中的标准和字典数据，随后按下方API payload通过页面或接口新增检验记录。
4. 结构化牌号统一使用 `QA0623` 后缀，PDF正文中的原牌号仅代表来源材料。

## 6. API Payloads

### td_ins_20_1

```json
{
  "heatNo": "HT-TD-20-QUAL-001",
  "coilNo": "COIL-TD-20-QUAL-001",
  "customerId": "TD-CUST-HXPIPE",
  "productVariety": "管线钢热轧卷",
  "productGrade": "L360M-QA0623",
  "productSpec": "厚度10.00mm，宽度1500mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-27 09:00:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 435.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 555.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 22.0
    },
    {
      "indicatorId": "td_ind_ceq",
      "testValue": 0.4
    }
  ]
}
```

### td_ins_20_2

```json
{
  "heatNo": "HT-TD-20-EDGE-001",
  "coilNo": "COIL-TD-20-EDGE-001",
  "customerId": "TD-CUST-HXPIPE",
  "productVariety": "管线钢热轧卷",
  "productGrade": "L360M-QA0623",
  "productSpec": "厚度10.00mm，宽度1500mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-27 10:00:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 360.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 460.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 20.0
    },
    {
      "indicatorId": "td_ind_ceq",
      "testValue": 0.42
    }
  ]
}
```

### td_ins_20_3

```json
{
  "heatNo": "HT-TD-20-CONC-001",
  "coilNo": "COIL-TD-20-CONC-001",
  "customerId": "TD-CUST-HXPIPE",
  "productVariety": "管线钢热轧卷",
  "productGrade": "L360M-QA0623",
  "productSpec": "厚度10.00mm，宽度1500mm",
  "sampleType": "TAIL",
  "testTime": "2026-06-27 11:00:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 355.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 555.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 22.0
    },
    {
      "indicatorId": "td_ind_ceq",
      "testValue": 0.4
    }
  ]
}
```

### td_ins_20_4

```json
{
  "heatNo": "HT-TD-20-REINS-001",
  "coilNo": "COIL-TD-20-REINS-001",
  "customerId": "TD-CUST-HXPIPE",
  "productVariety": "管线钢热轧卷",
  "productGrade": "L360M-QA0623",
  "productSpec": "厚度10.00mm，宽度1500mm",
  "sampleType": "MIDDLE",
  "testTime": "2026-06-27 12:00:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 435.0
    },
    {
      "indicatorId": "ind001",
      "testValue": 455.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 22.0
    },
    {
      "indicatorId": "td_ind_ceq",
      "testValue": 0.4
    }
  ]
}
```

### td_ins_20_5

```json
{
  "heatNo": "HT-TD-20-UNQ-001",
  "coilNo": "COIL-TD-20-UNQ-001",
  "customerId": "TD-CUST-HXPIPE",
  "productVariety": "管线钢热轧卷",
  "productGrade": "L360M-QA0623",
  "productSpec": "厚度10.00mm，宽度1500mm",
  "sampleType": "HEAD",
  "testTime": "2026-06-27 13:00:00",
  "testerNo": "021001",
  "values": [
    {
      "indicatorId": "ind003",
      "testValue": 339.99
    },
    {
      "indicatorId": "ind001",
      "testValue": 555.0
    },
    {
      "indicatorId": "ind002",
      "testValue": 22.0
    },
    {
      "indicatorId": "td_ind_ceq",
      "testValue": 0.4
    }
  ]
}
```
