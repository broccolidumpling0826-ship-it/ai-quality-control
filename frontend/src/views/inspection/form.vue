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
            <el-form-item label="客户" prop="customer">
              <el-input v-model="baseForm.customer" placeholder="输入客户名称" />
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
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
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

    <!-- 判定结论对话框 -->
    <el-dialog v-model="resultDialogVisible" title="检验提交完成" width="480px" :close-on-click-modal="false">
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
        <el-descriptions :column="2" border>
          <el-descriptions-item label="炉号">{{ judgmentResult?.heatNo }}</el-descriptions-item>
          <el-descriptions-item label="卷号">{{ judgmentResult?.coilNo }}</el-descriptions-item>
          <el-descriptions-item label="判定时间">{{ judgmentResult?.judgeTime }}</el-descriptions-item>
          <el-descriptions-item label="检验人">{{ judgmentResult?.inspector }}</el-descriptions-item>
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useDictStore } from '@/store/dict'
import { useAuthStore } from '@/store/auth'
import { addInspection, mapInspectionAddPayload } from '@/api/inspection'
import { listIndicators, getSpecRanges, type SpecRangeOption } from '@/api/standard'

const router = useRouter()
const dictStore = useDictStore()
const authStore = useAuthStore()

const baseFormRef = ref<FormInstance>()
const submitLoading = ref(false)
const loadingIndicators = ref(false)

const baseForm = reactive({
  heatNo: '',
  coilNo: '',
  batchNo: '',
  customer: '',
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
  measuredValue: string
  unit: string
  noStandard: boolean
}

const indicatorRows = ref<IndicatorRow[]>([])

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
      customerId: baseForm.customer || undefined,
    })
    specRangeOptions.value = opts ?? []
    // 清空已选规格（品种或牌号已变更）
    if (!specRangeOptions.value.some(o => o.value === baseForm.specification)) {
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
  loadSpecRanges()
}

async function loadStandardIndicators() {
  loadingIndicators.value = true
  try {
    const list = await listIndicators({
      productVariety: baseForm.productVariety,
      productGrade: baseForm.productGrade
    }) as any[]
    if (list && list.length > 0) {
      indicatorRows.value = list.map((ind: any) => ({
        indicatorId: ind.id,
        indicatorName: ind.indicatorName,
        indicatorCode: ind.indicatorCode,
        category: ind.category,
        lowerLimit: ind.lowerLimit ?? null,
        upperLimit: ind.upperLimit ?? null,
        measuredValue: '',
        unit: ind.unit || '',
        noStandard: false
      }))
      ElMessage.success(`已加载 ${list.length} 个指标`)
    } else {
      ElMessage.warning('未找到匹配的标准指标，请手动添加')
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
    CONCESSION: 'warning',
    REINSPECTION: 'info'
  }
  return map[type] || 'info'
}

function goToExplanation() {
  resultDialogVisible.value = false
  if (judgmentResult.value?.judgmentId) {
    router.push(`/judgment/explanation?id=${judgmentResult.value.judgmentId}`)
  }
}

async function handleSubmit() {
  await baseFormRef.value?.validate()
  if (indicatorRows.value.length === 0) {
    ElMessage.warning('请至少添加一条指标记录')
    return
  }
  submitLoading.value = true
  try {
    const payload = mapInspectionAddPayload({
      ...baseForm,
      productSpec: baseForm.specification,
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
      judgmentId: res?.judgmentId
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
</style>
