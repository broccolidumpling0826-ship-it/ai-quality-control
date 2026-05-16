# Contract: 数据字典接口

**Base Path**: `/api/v1/dict`
**说明**: FR-017 — 所有前端下拉框数据必须从此接口获取，禁止前端硬编码枚举显示文本。

---

## GET /all — 批量加载全部字典（前端启动时调用一次）

**说明**: 前端应用初始化时调用，将结果写入 Pinia `dictStore` 并缓存至 sessionStorage。

**Response** (ApiResult<Map<String, List<DictItemVO>>>):
```json
{
  "code": 2000,
  "data": {
    "STANDARD_TYPE": [
      { "value": "NATIONAL",    "label": "国标",     "colorTag": "info"    },
      { "value": "ENTERPRISE",  "label": "企标",     "colorTag": "primary" },
      { "value": "CUSTOMER",    "label": "客户协议", "colorTag": "success" }
    ],
    "JUDGMENT_TYPE": [
      { "value": "QUALIFIED",         "label": "合格",   "colorTag": "success" },
      { "value": "UNQUALIFIED",       "label": "不合格", "colorTag": "danger"  },
      { "value": "NEED_REINSPECTION", "label": "需复检", "colorTag": "warning" },
      { "value": "CAN_CONCESSION",    "label": "可让步", "colorTag": "primary" }
    ],
    "INDICATOR_CATEGORY": [
      { "value": "COMPOSITION", "label": "成分", "colorTag": "" },
      { "value": "PERFORMANCE", "label": "性能", "colorTag": "" },
      { "value": "DIMENSION",   "label": "尺寸", "colorTag": "" },
      { "value": "SURFACE",     "label": "表面", "colorTag": "" },
      { "value": "SHAPE",       "label": "外形", "colorTag": "" }
    ],
    "SAMPLE_TYPE": [
      { "value": "HEAD",   "label": "头部", "colorTag": "" },
      { "value": "TAIL",   "label": "尾部", "colorTag": "" },
      { "value": "MIDDLE", "label": "中部", "colorTag": "" }
    ],
    "NEW_EVIDENCE_SOURCE": [
      { "value": "SUBSEQUENT_PROCESS", "label": "后续工序缺陷发现",   "colorTag": "danger"  },
      { "value": "CUSTOMER_COMPLAINT", "label": "客户质量异议",       "colorTag": "danger"  },
      { "value": "BATCH_TRACING",      "label": "同炉批次追溯",       "colorTag": "warning" },
      { "value": "THIRD_PARTY",        "label": "第三方检测机构复验", "colorTag": "warning" },
      { "value": "OTHER",              "label": "其他",               "colorTag": ""        }
    ]
  }
}
```

**Cache**: 后端 Redis TTL 10 分钟；前端 Pinia + sessionStorage 缓存至会话结束。

---

## GET /items/{dictCode} — 获取单个字典分类下的字典项

**Path**: `dictCode` — 字典分类编码（如 `STANDARD_TYPE`）

**Query**: `?status=1`（默认只返回启用状态的字典项）

**Response** (ApiResult<List<DictItemVO>>):
```json
{
  "code": 2000,
  "data": [
    { "value": "NATIONAL",   "label": "国标",     "colorTag": "info",    "sortNo": 1 },
    { "value": "ENTERPRISE", "label": "企标",     "colorTag": "primary", "sortNo": 2 },
    { "value": "CUSTOMER",   "label": "客户协议", "colorTag": "success", "sortNo": 3 }
  ]
}
```

---

## POST /page — 分页查询字典分类列表（ADMIN 管理界面）

**权限**: `ADMIN`

**Request**:
```json
{
  "dictCode": "",
  "dictName": "",
  "status": null,
  "pageNum": 1,
  "pageSize": 20
}
```

**Response** (ApiResult<Page<SysDictVO>>)

---

## POST / — 新增字典分类

**权限**: `ADMIN`

**Request**:
```json
{
  "dictCode": "PRODUCT_GRADE_DC",
  "dictName": "DC系列牌号",
  "description": "冷轧 DC 系列牌号",
  "sortNo": 10
}
```

**Errors**:
- 4000: 字典编码已存在
- 4003: 无权限（非 ADMIN）

---

## POST /items — 新增字典项

**权限**: `ADMIN`

**Request**:
```json
{
  "dictCode": "PRODUCT_GRADE_DC",
  "itemValue": "DC01",
  "itemLabel": "DC01",
  "colorTag": "info",
  "sortNo": 1
}
```

**Errors**:
- 4000: 同一字典下该 itemValue 已存在
- 4000: 不可修改系统内置字典项的 itemValue

---

## PUT /items/{id} — 修改字典项（仅 label、colorTag、sortNo 可改）

**权限**: `ADMIN`

**Errors**:
- 4000: 系统内置字典项的 itemValue 不可修改
- 4000: 系统内置字典分类不可删除

---

## DELETE /items/{id} — 删除字典项

**权限**: `ADMIN`

**Errors**:
- 4000: 系统内置字典项不可删除（is_system=1）

---

## 前端使用规范

```typescript
// 在 store/dict.ts (Pinia)
import { defineStore } from 'pinia'
import { getAllDict } from '@/api/dict'

interface DictItem {
  value: string
  label: string
  colorTag: string
}

export const useDictStore = defineStore('dict', {
  state: () => ({
    dictMap: {} as Record<string, DictItem[]>
  }),
  actions: {
    async loadAll() {
      const res = await getAllDict()
      this.dictMap = res.data
    },
    getItems(dictCode: string): DictItem[] {
      return this.dictMap[dictCode] ?? []
    },
    getLabel(dictCode: string, value: string): string {
      return this.getItems(dictCode).find(i => i.value === value)?.label ?? value
    }
  }
})

// 在组件中使用（下拉框示例）：
// <el-select v-model="form.standardType">
//   <el-option
//     v-for="item in dictStore.getItems('STANDARD_TYPE')"
//     :key="item.value"
//     :value="item.value"
//     :label="item.label"
//   />
// </el-select>

// 在表格中显示标签：
// <el-tag :type="dictStore.getItems('JUDGMENT_TYPE').find(i=>i.value===row.judgmentType)?.colorTag">
//   {{ dictStore.getLabel('JUDGMENT_TYPE', row.judgmentType) }}
// </el-tag>
```

---

## 表头筛选规范（FR-018）

所有分页查询接口的 Request DTO MUST 包含可筛选字段，对应前端表头筛选项：

```typescript
// 通用分页查询 DTO 模式（以检验记录为例）
interface InspectionRecordPageQuery {
  // 文本筛选（模糊）
  coilNo?: string
  heatNo?: string
  // 枚举筛选（多选，从字典获取选项）
  status?: string[]          // INSPECTION_STATUS 字典
  sampleType?: string[]      // SAMPLE_TYPE 字典
  // 日期范围
  testTimeStart?: string
  testTimeEnd?: string
  // 分页
  pageNum: number
  pageSize: number
}
```

筛选 URL 持久化示例：
```
/inspection?coilNo=Z123&status=NORMAL&testTimeStart=2025-05-01&pageNum=1
```
