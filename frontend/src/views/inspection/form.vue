<template>
  <div class="inspection-form-page">
    <el-page-header @back="router.back()" content="新建检验记录" style="margin-bottom:16px" />

    <!-- 基本信息 -->
    <el-card title="基本信息" shadow="never">
      <template #header>
        <span style="font-weight:600">基本信息</span>
      </template>
      <el-form ref="baseFormRef" :model="baseForm" :rules="baseRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="炉号" prop="heatNo">
              <el-input v-model="baseForm.heatNo" placeholder="输入炉号" @blur="syncBatchNo" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="卷号" prop="coilNo">
              <el-input v-model="baseForm.coilNo" placeholder="输入卷号" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="批次号" prop="batchNo">
              <el-input v-model="baseForm.batchNo" placeholder="自动同步炉号" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="客户" prop="customerId">
              <el-select
                v-model="baseForm.customerId"
                placeholder="请选择客户"
                filterable
                clearable
                style="width:100%"
              >
                <el-option
                  v-for="item in customerOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="品种" prop="productVariety">
              <el-select v-model="baseForm.productVariety" placeholder="请选择" style="width:100%" @change="onProductChange">
                <el-option
                  v-for="item in dictStore.getItems('PRODUCT_VARIETY')"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="牌号" prop="productGrade">
              <el-select v-model="baseForm.productGrade" placeholder="请选择" style="width:100%">
                <el-option
                  v-for="item in dictStore.getItems('PRODUCT_GRADE')"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="规格" prop="specification">
              <!-- D-016：product_spec 强制下拉，禁止自由文本输入；随品种/牌号/客户变化动态加载 -->
              <el-select
                v-model="baseForm.specification"
                placeholder="请先选择品种和牌号"
                style="width:100%"
                :disabled="!baseForm.productVariety || !baseForm.productGrade"
                :loading="specRangeLoading"
                clearable
              >
                <el-option
                  v-for="opt in specRangeOptions"
                  :key="opt.standardId"
                  :label="opt.label"
                  :value="opt.standardId"
                />
                <template v-if="specRangeOptions.length === 0 && !specRangeLoading" #empty>
                  <div class="text-muted" style="padding:8px 16px;font-size:12px;">
                    暂无有效规格，请先在标准库维护对应标准
                  </div>
                </template>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="样品类型" prop="sampleType">
              <el-select v-model="baseForm.sampleType" placeholder="请选择" style="width:100%">
                <el-option
                  v-for="item in dictStore.getItems('SAMPLE_TYPE')"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="检验时间" prop="inspectionTime">
              <el-date-picker
                v-model="baseForm.inspectionTime"
                type="datetime"
                placeholder="选择检验时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width:100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="检验人">
              <el-input :value="authStore.userInfo?.username" disabled />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 指标录入 -->
    <el-card shadow="never" style="margin-top:12px">
      <template #header>
        <div style="display:flex;align-items:center;justify-content:space-between">
          <span style="font-weight:600">指标录入</span>
          <div>
            <el-button size="small" @click="addManualRow">手动添加指标</el-button>
            <el-button size="small" type="primary" @click="loadStandardIndicators" :loading="loadingIndicators">
              从标准加载
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="indicatorRows" border>
        <el-table-column prop="indicatorName" label="指标名称" min-width="130">
          <template #default="{ row }">
            <span v-if="row.noStandard" style="color:#e6a23c;font-weight:500">{{ row.indicatorName }}</span>
            <span v-else>{{ row.indicatorName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类别" width="100">
          <template #default="{ row }">
            <el-tag :type="dictStore.getColorTag('INDICATOR_CATEGORY', row.category) as any" size="small">
              {{ dictStore.getLabel('INDICATOR_CATEGORY', row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lowerLimit" label="标准下限" width="100" align="center">
          <template #default="{ row }">
            {{ row.lowerLimit ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="upperLimit" label="标准上限" width="100" align="center">
          <template #default="{ row }">
            {{ row.upperLimit ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="让步下限" width="90" align="center">
          <template #default="{ row }">
            {{ row.concessionLower ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="让步上限" width="90" align="center">
          <template #default="{ row }">
            {{ row.concessionUpper ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="实测值" width="130">
          <template #default="{ row }">
            <el-input
              v-model="row.measuredValue"
              size="small"
              placeholder="输入实测值"
              :class="{ 'input-warning': row.noStandard }"
            />
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="70" align="center" />
        <el-table-column label="" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.noStandard" type="warning" size="small">无标准覆盖</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="70" align="center">
          <template #default="{ $index }">
            <el-button link type="danger" size="small" @click="removeRow($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 底部操作 -->
    <div class="bottom-bar">
      <el-button @click="router.back()">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交检验</el-button>
    </div>

    <!-- 从标准选择指标 -->
    <el-dialog
      v-model="indicatorPickDialogVisible"
      title="从标准选择指标"
      width="760px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <p v-if="pickStandardLabel" style="margin:0 0 12px;font-size:13px;color:var(--el-text-color-secondary)">
        匹配标准：{{ pickStandardLabel }}
      </p>
      <el-table
        ref="indicatorPickTableRef"
        v-loading="loadingIndicators"
        :data="standardIndicatorCandidates"
        border
        max-height="400"
        row-key="indicatorId"
      >
        <el-table-column type="selection" width="48" />
        <el-table-column prop="indicatorName" label="指标名称" min-width="120" />
        <el-table-column prop="indicatorCode" label="代码" width="80" />
        <el-table-column label="类别" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.category" :type="dictStore.getColorTag('INDICATOR_CATEGORY', row.category) as any" size="small">
              {{ dictStore.getLabel('INDICATOR_CATEGORY', row.category) }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="标准下限" width="90" align="center">
          <template #default="{ row }">{{ row.lowerLimit ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="标准上限" width="90" align="center">
          <template #default="{ row }">{{ row.upperLimit ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="让步下限" width="88" align="center">
          <template #default="{ row }">{{ row.concessionLower ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="让步上限" width="88" align="center">
          <template #default="{ row }">{{ row.concessionUpper ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="60" align="center" />
        <el-table-column label="必检" width="60" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isRequired === 1" type="danger" size="small">必检</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="indicatorPickDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPickIndicators">添加所选</el-button>
      </template>
    </el-dialog>

    <!-- 判定结论对话框 -->
    <el-dialog v-model="resultDialogVisible" title="检验提交完成" width="520px" :close-on-click-modal="false">
      <div class="result-area">
        <div style="text-align:center;margin-bottom:16px">
          <el-tag
            :type="judgmentTypeColor(judgmentResult?.judgmentType)"
            size="large"
            style="font-size:20px;padding:12px 32px;height:auto"
          >
            {{ dictStore.getLabel('JUDGMENT_TYPE', judgmentResult?.judgmentType) || judgmentResult?.judgmentType }}
          </el-tag>
        </div>
        <el-descriptions :column="2" border label-width="96px" class="result-descriptions">
          <el-descriptions-item label="炉号">{{ judgmentResult?.heatNo }}</el-descriptions-item>
          <el-descriptions-item label="卷号">{{ judgmentResult?.coilNo }}</el-descriptions-item>
          <el-descriptions-item label="判定时间">{{ judgmentResult?.judgeTime }}</el-descriptions-item>
          <el-descriptions-item label="检验人">{{ judgmentResult?.inspector || '-' }}</el-descriptions-item>
        </el-descriptions>
        <p v-if="judgmentResult?.remark" class="text-secondary" style="margin-top:12px">
          {{ judgmentResult?.remark }}
        </p>
      </div>
      <template #footer>
        <el-button @click="resultDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="goToExplanation">查看判定详情</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import type { DictItem } from '@/types'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, TableInstance } from 'element-plus'
import { useDictStore } from '@/store/dict'
import { useAuthStore } from '@/store/auth'
import { addInspection, mapInspectionAddPayload } from '@/api/inspection'
import { getStandardById, getSpecRanges, type SpecRangeOption } from '@/api/standard'

const router = useRouter()
const dictStore = useDictStore()
const authStore = useAuthStore()

const baseFormRef = ref<FormInstance>()
const submitLoading = ref(false)
const loadingIndicators = ref(false)

/** 字典未同步时的演示客户兜底（与 init-dict-data.sql 一致） */
const FALLBACK_CUSTOMERS: DictItem[] = [
  { value: 'CUST-001', label: '华东汽车配件有限公司', colorTag: '', sortNo: 1 },
  { value: 'CUST-002', label: '西南建材集团', colorTag: '', sortNo: 2 }
]

const customerOptions = ref<DictItem[]>([...FALLBACK_CUSTOMERS])

const baseForm = reactive({
  heatNo: '',
  coilNo: '',
  batchNo: '',
  customerId: '',
  productVariety: '',
  productGrade: '',
  specification: '',
  sampleType: '',
  inspectionTime: ''
})

const baseRules = {
  heatNo: [{ required: true, message: '请输入炉号', trigger: 'blur' }],
  coilNo: [{ required: true, message: '请输入卷号', trigger: 'blur' }],
  productVariety: [{ required: true, message: '请选择品种', trigger: 'change' }],
  sampleType: [{ required: true, message: '请选择样品类型', trigger: 'change' }],
  inspectionTime: [{ required: true, message: '请选择检验时间', trigger: 'change' }]
}

interface IndicatorRow {
  indicatorId: string
  indicatorName: string
  indicatorCode: string
  category: string
  lowerLimit: number | null
  upperLimit: number | null
  concessionLower: number | null
  concessionUpper: number | null
  measuredValue: string
  unit: string
  noStandard: boolean
}

const indicatorRows = ref<IndicatorRow[]>([])

interface StandardIndicatorCandidate {
  indicatorId: string
  indicatorName: string
  indicatorCode: string
  category: string
  lowerLimit: number | null
  upperLimit: number | null
  concessionLower: number | null
  concessionUpper: number | null
  unit: string
  isRequired: number
}

function parseLimitVal(val: unknown): number | null {
  if (val === '' || val === null || val === undefined) return null
  const n = Number(val)
  return Number.isFinite(n) ? n : null
}

const indicatorPickDialogVisible = ref(false)
const standardIndicatorCandidates = ref<StandardIndicatorCandidate[]>([])
const pickStandardLabel = ref('')
const indicatorPickTableRef = ref<TableInstance>()

const resultDialogVisible = ref(false)
const judgmentResult = ref<any>(null)
const createdRecordId = ref('')

function syncBatchNo() {
  if (!baseForm.batchNo) {
    baseForm.batchNo = baseForm.heatNo
  }
}

// D-016: spec-range dropdown state
const specRangeOptions = ref<SpecRangeOption[]>([])
const specRangeLoading = ref(false)

async function loadSpecRanges() {
  if (!baseForm.productVariety || !baseForm.productGrade) {
    specRangeOptions.value = []
    baseForm.specification = ''
    return
  }
  specRangeLoading.value = true
  try {
    const opts = await getSpecRanges({
      variety: baseForm.productVariety,
      grade: baseForm.productGrade,
      customerId: baseForm.customerId || undefined,
    })
    specRangeOptions.value = opts ?? []
    // 清空已选规格（品种或牌号已变更）
    if (!specRangeOptions.value.some((o) => o.standardId === baseForm.specification)) {
      baseForm.specification = ''
    }
  } catch {
    specRangeOptions.value = []
  } finally {
    specRangeLoading.value = false
  }
}

function onProductChange() {
  indicatorRows.value = []
}

/** 品种/牌号/客户任一变化时自动刷新规格下拉 */
watch(
  () => [baseForm.productVariety, baseForm.productGrade, baseForm.customerId] as const,
  () => {
    loadSpecRanges()
  }
)

function mapStandardIndicatorToRow(ind: StandardIndicatorCandidate): IndicatorRow {
  return {
    indicatorId: ind.indicatorId,
    indicatorName: ind.indicatorName,
    indicatorCode: ind.indicatorCode,
    category: ind.category,
    lowerLimit: ind.lowerLimit,
    upperLimit: ind.upperLimit,
    concessionLower: ind.concessionLower,
    concessionUpper: ind.concessionUpper,
    measuredValue: '',
    unit: ind.unit,
    noStandard: false
  }
}

function confirmPickIndicators() {
  const selected = indicatorPickTableRef.value?.getSelectionRows() as StandardIndicatorCandidate[] | undefined
  if (!selected?.length) {
    ElMessage.warning('请至少选择一个指标')
    return
  }
  const existingIds = new Set(indicatorRows.value.map((r) => r.indicatorId).filter(Boolean))
  const toAdd = selected.filter((ind) => !existingIds.has(ind.indicatorId))
  if (!toAdd.length) {
    ElMessage.warning('所选指标均已存在，请勿重复添加')
    return
  }
  indicatorRows.value.push(...toAdd.map(mapStandardIndicatorToRow))
  indicatorPickDialogVisible.value = false
  ElMessage.success(`已添加 ${toAdd.length} 个指标`)
}

async function loadStandardIndicators() {
  if (!baseForm.productVariety || !baseForm.productGrade) {
    ElMessage.warning('请先选择品种和牌号')
    return
  }
  if (!baseForm.specification) {
    ElMessage.warning('请先选择规格')
    return
  }

  const specOpt = specRangeOptions.value.find((o) => o.standardId === baseForm.specification)
  if (!specOpt?.standardId) {
    ElMessage.warning('未找到匹配的质量标准，请确认规格选择或先在标准库维护')
    return
  }

  loadingIndicators.value = true
  try {
    const detail = await getStandardById(specOpt.standardId) as {
      standardName?: string
      standardCode?: string
      indicators?: Array<Record<string, unknown>>
    }
    pickStandardLabel.value = [detail.standardName, detail.standardCode].filter(Boolean).join(' / ')

    const existingIds = new Set(indicatorRows.value.map((r) => r.indicatorId).filter(Boolean))
    const candidates: StandardIndicatorCandidate[] = (detail.indicators || [])
      .filter((ind) => ind.indicatorId && !existingIds.has(String(ind.indicatorId)))
      .map((ind) => ({
        indicatorId: String(ind.indicatorId),
        indicatorName: String(ind.indicatorName ?? ''),
        indicatorCode: String(ind.indicatorCode ?? ''),
        category: String(ind.category ?? ind.indicatorCategory ?? ''),
        lowerLimit: parseLimitVal(ind.lowerLimit),
        upperLimit: parseLimitVal(ind.upperLimit),
        concessionLower: parseLimitVal(ind.concessionLower),
        concessionUpper: parseLimitVal(ind.concessionUpper),
        unit: String(ind.unit ?? ''),
        isRequired: ind.isRequired != null ? Number(ind.isRequired) : 0
      }))

    if (!candidates.length) {
      ElMessage.warning(
        detail.indicators?.length
          ? '该标准下的指标已全部添加'
          : '该标准未配置指标，请先在标准库维护'
      )
      return
    }

    standardIndicatorCandidates.value = candidates
    indicatorPickDialogVisible.value = true
    await nextTick()
    indicatorPickTableRef.value?.clearSelection()
    for (const row of candidates) {
      if (row.isRequired === 1) {
        indicatorPickTableRef.value?.toggleRowSelection(row, true)
      }
    }
  } finally {
    loadingIndicators.value = false
  }
}

function addManualRow() {
  indicatorRows.value.push({
    indicatorId: '',
    indicatorName: '自定义指标',
    indicatorCode: '',
    category: '',
    lowerLimit: null,
    upperLimit: null,
    concessionLower: null,
    concessionUpper: null,
    measuredValue: '',
    unit: '',
    noStandard: true
  })
}

function removeRow(index: number) {
  indicatorRows.value.splice(index, 1)
}

function judgmentTypeColor(type: string): any {
  const map: Record<string, string> = {
    QUALIFIED: 'success',
    UNQUALIFIED: 'danger',
    CAN_CONCESSION: 'warning',
    CONCESSION: 'warning',
    NEED_REINSPECTION: 'info',
    REINSPECTION: 'info',
    STANDARD_CONFLICT: 'danger'
  }
  return map[type] || 'info'
}

function goToExplanation() {
  resultDialogVisible.value = false
  if (judgmentResult.value?.judgmentId) {
    router.push(`/judgment/explanation?id=${judgmentResult.value.judgmentId}`)
  }
}

/** 关联客户下拉：强制从 DB 刷新，避免 Redis 缓存旧数据 */
async function loadCustomerOptions() {
  try {
    const items = await dictStore.refreshItems('QC_CUSTOMER')
    customerOptions.value = items.length ? items : [...FALLBACK_CUSTOMERS]
  } catch {
    customerOptions.value = [...FALLBACK_CUSTOMERS]
  }
}

onMounted(() => {
  loadCustomerOptions()
})

async function handleSubmit() {
  await baseFormRef.value?.validate()
  if (indicatorRows.value.length === 0) {
    ElMessage.warning('请至少添加一条指标记录')
    return
  }
  submitLoading.value = true
  try {
    const selectedSpec = specRangeOptions.value.find((o) => o.standardId === baseForm.specification)
    const payload = mapInspectionAddPayload({
      ...baseForm,
      productSpec: selectedSpec?.specRange || selectedSpec?.label || baseForm.specification,
      testTime: baseForm.inspectionTime,
      testerNo: authStore.userInfo?.userNo,
      indicators: indicatorRows.value
    })
    const res = await addInspection(payload) as any
    createdRecordId.value = res?.recordId || res?.id || ''
    judgmentResult.value = {
      judgmentType: res?.judgmentType,
      heatNo: baseForm.heatNo,
      coilNo: baseForm.coilNo,
      judgeTime: res?.judgmentTime,
      judgmentId: res?.judgmentId,
      inspector: res?.inspector ?? authStore.userInfo?.username ?? authStore.userInfo?.userNo ?? '-'
    }
    resultDialogVisible.value = true
  } finally {
    submitLoading.value = false
  }
}
</script>

<style scoped>
.inspection-form-page {
  padding: 16px;
  max-width: 1200px;
}
.bottom-bar {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 12px 0;
}
:deep(.input-warning .el-input__inner) {
  background-color: #fdf6ec;
  border-color: #e6a23c;
}
:deep(.el-table tr.no-standard-row) {
  background-color: #fdf6ec !important;
}
.result-area :deep(.result-descriptions .el-descriptions__label) {
  width: 96px;
  min-width: 96px;
}
.result-area :deep(.result-descriptions .el-descriptions__content) {
  min-width: 140px;
}
</style>
