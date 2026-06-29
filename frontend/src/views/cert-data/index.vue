<template>
  <div class="cert-data-page page-list-full">
    <div class="page-toolbar">
      <div>
        <div class="page-toolbar-title">质保书数据</div>
        <div class="page-toolbar-desc">生成正式质保书快照并导出 PDF；出证依据可在质保书问答中查询。</div>
      </div>
      <el-button type="primary" plain :icon="ChatLineSquare" @click="goToCertQa()">质保书问答</el-button>
    </div>

    <!-- 生成区 -->
    <el-card shadow="never" style="margin-bottom:12px">
      <template #header>
        <div class="card-head">
          <span style="font-weight:600">生成质保书数据</span>
          <el-button link type="primary" @click="goToCertQaFromGenerate">生成后去问答页核对 →</el-button>
        </div>
      </template>
      <el-form ref="generateFormRef" :model="generateForm" :rules="generateRules" inline label-width="80px">
        <el-form-item label="卷号" prop="coilNo">
          <el-input
            v-model="generateForm.coilNo"
            placeholder="输入卷号"
            style="width:200px"
            @keyup.enter="handleGenerate"
          />
        </el-form-item>
        <el-form-item label="批次号">
          <el-input
            v-model="generateForm.batchNo"
            placeholder="或输入批次号"
            style="width:200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="generateLoading" @click="handleGenerate">生成</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 搜索 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="卷号">
          <el-input v-model="searchForm.coilNo" placeholder="输入卷号" clearable style="width:150px" />
        </el-form-item>
        <el-form-item label="批次号">
          <el-input v-model="searchForm.batchNo" placeholder="输入批次号" clearable style="width:150px" />
        </el-form-item>
        <el-form-item label="生成时间">
          <el-date-picker
            v-model="searchForm.timeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD"
            style="width:220px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 历史记录 -->
    <el-card shadow="never" style="margin-top:12px">
      <template #header><span style="font-weight:600">历史记录</span></template>
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width:100%"
      >
        <el-table-column
          prop="coilNo"
          label="卷号"
          width="140"
          :filters="getFilters('coilNo')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="batchNo"
          label="批次号"
          width="140"
          :filters="getFilters('batchNo')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="heatNo"
          label="炉号"
          width="120"
          :filters="getFilters('heatNo')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="generateTime"
          label="生成时间"
          min-width="170"
          :filters="getFilters('generateTime')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="operatorName"
          label="操作人"
          min-width="120"
          :filters="getFilters('operatorName')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="status"
          label="状态"
          width="100"
          :filters="getFilters('status')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        >
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
              {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">查看详情</el-button>
            <el-button link type="primary" @click="goToCertQa(row)">问答</el-button>
            <el-button link type="success" @click="handleDownloadPdf(row)">导出PDF</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top:16px;justify-content:flex-end"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="质保书数据详情" width="800px">
      <template v-if="currentDetail">
        <el-descriptions :column="3" border size="small" style="margin-bottom:16px">
          <el-descriptions-item label="卷号">{{ currentDetail.coilNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="批次号">{{ currentDetail.batchNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="炉号">{{ currentDetail.heatNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="品种">{{ currentDetail.productVariety || '-' }}</el-descriptions-item>
          <el-descriptions-item label="牌号">{{ currentDetail.productGrade || '-' }}</el-descriptions-item>
          <el-descriptions-item label="客户">{{ currentDetail.customerLabel || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="currentDetail.finalJudgmentType" label="综合判定" :span="3">
            <el-tag :type="judgmentTagType(currentDetail.finalJudgmentType)" size="small">
              {{ dictStore.getLabel('JUDGMENT_TYPE', currentDetail.finalJudgmentType) || currentDetail.finalJudgmentType }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-table :data="currentDetail.indicators" border size="small">
          <el-table-column prop="indicatorName" label="指标名称" min-width="130" />
          <el-table-column prop="indicatorCode" label="指标代码" width="120" />
          <el-table-column label="实测值" width="100" align="center">
            <template #default="{ row }">{{ formatMeasuredValue(row) }}</template>
          </el-table-column>
          <el-table-column prop="unit" label="单位" width="70" align="center" />
          <el-table-column prop="lowerLimit" label="标准下限" width="90" align="center">
            <template #default="{ row }">{{ row.lowerLimit ?? '-' }}</template>
          </el-table-column>
          <el-table-column prop="upperLimit" label="标准上限" width="90" align="center">
            <template #default="{ row }">{{ row.upperLimit ?? '-' }}</template>
          </el-table-column>
          <el-table-column label="结论" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="indicatorConclusionTag(row).type" size="small">
                {{ indicatorConclusionTag(row).label }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </template>
      <template #footer>
        <el-button v-if="currentDetail?.id" type="success" @click="handleDownloadPdf(currentDetail)">导出PDF</el-button>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatLineSquare } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { useDictStore } from '@/store/dict'
import { useTableFilter } from '@/composables/use-table-filter'
import {
  downloadCertPdf,
  generateCertData,
  getCertDataById,
  mapCertGeneratePayload,
  pageCertData,
  type CertDataDetail,
  type CertDataPageQuery,
  type CertIndicatorSnapshot
} from '@/api/cert-data'
import type { PageResult } from '@/types'

const router = useRouter()
const route = useRoute()
const dictStore = useDictStore()

const DEFAULT_CERT_QA_QUESTION = '这卷当前能否生成正式质保书？'

const generateFormRef = ref<FormInstance>()
const generateLoading = ref(false)
const generateForm = reactive({ coilNo: '', batchNo: '' })
const generateRules = {
  coilNo: [{ required: false }]
}

const searchForm = reactive({
  coilNo: '',
  batchNo: '',
  timeRange: null as [string, string] | null
})

const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const loading = ref(false)

interface CertDisplayRow extends CertDataDetail {
  customerLabel?: string
  operatorName?: string
  indicators: CertIndicatorSnapshot[]
}

const tableData = ref<CertDisplayRow[]>([])
const { getFilters, filterMethod } = useTableFilter(tableData)

const detailDialogVisible = ref(false)
const currentDetail = ref<CertDisplayRow | null>(null)

function formatCustomerLabel(customerId?: string) {
  if (!customerId) return '-'
  return dictStore.getLabel('QC_CUSTOMER', customerId)
}

function indicatorConclusionTag(row: CertIndicatorSnapshot): { label: string; type: string } {
  const result = row.indicatorResult
  if (result === 'CONCESSION') {
    return { label: '可让步', type: 'warning' }
  }
  if (result === 'WARNING') {
    return { label: '-', type: 'info' }
  }
  const overall = currentDetail.value?.finalJudgmentType ?? row.finalJudgmentType
  if (overall === 'QUALIFIED' || result === 'PASS' || row.isPassed === 1 || row.isPassed === true) {
    return { label: '合格', type: 'success' }
  }
  if (overall === 'CAN_CONCESSION' && (row.isPassed === 0 || row.isPassed === false)) {
    return { label: '可让步', type: 'warning' }
  }
  return { label: '不合格', type: 'danger' }
}

function formatMeasuredValue(row: CertIndicatorSnapshot) {
  const val = row.testValue ?? (row as { measuredValue?: unknown }).measuredValue
  if (val === null || val === undefined || val === '') return '-'
  return val
}

function judgmentTagType(type?: string) {
  const map: Record<string, string> = {
    QUALIFIED: 'success',
    UNQUALIFIED: 'danger',
    CONCESSION: 'warning',
    NEED_REINSPECTION: 'info',
    REINSPECTION: 'info',
    CAN_CONCESSION: 'warning',
    STANDARD_CONFLICT: 'danger'
  }
  return (type && map[type]) || 'info'
}

function normalizeCertDetail(raw: CertDataDetail, forList = false): CertDisplayRow {
  const indicators = forList ? [] : (raw.indicators || []).map((ind) => ({
    ...ind,
    indicatorCode: ind.indicatorCode ?? '',
    testValue: ind.testValue
  }))
  return {
    ...raw,
    indicators,
    status: raw.status ?? (indicators.length ? 'SUCCESS' : 'FAILED'),
    operatorName: raw.generatedBy ?? '-',
    customerLabel: formatCustomerLabel(raw.customerId)
  }
}

async function handleGenerate() {
  if (!generateForm.coilNo && !generateForm.batchNo) {
    ElMessage.warning('请输入卷号或批次号')
    return
  }
  generateLoading.value = true
  try {
    const vo = await generateCertData(mapCertGeneratePayload(generateForm))
    ElMessage.success('质保书数据生成成功')
    generateForm.coilNo = ''
    generateForm.batchNo = ''
    await loadData()
    await ensureDetailDicts()
    currentDetail.value = normalizeCertDetail(vo)
    detailDialogVisible.value = true
  } finally {
    generateLoading.value = false
  }
}

async function loadData() {
  loading.value = true
  try {
    const params: Record<string, unknown> = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      coilNo: searchForm.coilNo || undefined,
      batchNo: searchForm.batchNo || undefined
    }
    if (searchForm.timeRange) {
      params.startTime = searchForm.timeRange[0]
      params.endTime = searchForm.timeRange[1]
    }
    const res = await pageCertData(params as unknown as CertDataPageQuery)
    tableData.value = (res.records || []).map((row) => normalizeCertDetail(row, true))
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handleReset() {
  Object.assign(searchForm, { coilNo: '', batchNo: '', timeRange: null })
  pageNum.value = 1
  loadData()
}

async function ensureDetailDicts() {
  await Promise.all([
    dictStore.refreshItems('QC_CUSTOMER').catch(() => {}),
    dictStore.refreshItems('JUDGMENT_TYPE').catch(() => {})
  ])
}

async function viewDetail(row: CertDisplayRow) {
  if (!row.id) {
    await ensureDetailDicts()
    currentDetail.value = normalizeCertDetail(row)
    detailDialogVisible.value = true
    return
  }
  try {
    await ensureDetailDicts()
    const detail = await getCertDataById(row.id)
    currentDetail.value = normalizeCertDetail(detail)
    detailDialogVisible.value = true
  } catch {
    // handled by request interceptor
  }
}

async function handleDownloadPdf(row: CertDisplayRow) {
  if (!row.id) {
    ElMessage.warning('缺少质保书数据ID')
    return
  }
  try {
    const blob = await downloadCertPdf(row.id)
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `质保书-${row.coilNo || row.id}.pdf`
    link.click()
    URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '导出质保书PDF失败')
  }
}

function goToCertQa(source?: Pick<CertDisplayRow, 'coilNo' | 'batchNo'>) {
  const coilNo = source?.coilNo || searchForm.coilNo || generateForm.coilNo || undefined
  const batchNo = source?.batchNo || searchForm.batchNo || generateForm.batchNo || undefined
  router.push({
    path: '/cert-data/qa',
    query: {
      ...(coilNo ? { coilNo } : {}),
      ...(batchNo ? { batchNo } : {}),
      question: DEFAULT_CERT_QA_QUESTION
    }
  })
}

function goToCertQaFromGenerate() {
  if (!generateForm.coilNo && !generateForm.batchNo) {
    ElMessage.warning('请先输入卷号或批次号')
    return
  }
  goToCertQa({ coilNo: generateForm.coilNo, batchNo: generateForm.batchNo })
}

onMounted(() => {
  const coilNo = typeof route.query.coilNo === 'string' ? route.query.coilNo : ''
  const batchNo = typeof route.query.batchNo === 'string' ? route.query.batchNo : ''
  if (coilNo) {
    generateForm.coilNo = coilNo
    searchForm.coilNo = coilNo
  }
  if (batchNo) {
    generateForm.batchNo = batchNo
    searchForm.batchNo = batchNo
  }
  loadData()
})
</script>

<style scoped>
.cert-data-page {
  width: 100%;
  max-width: none;
}

.page-toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.page-toolbar-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.page-toolbar-desc {
  margin-top: 4px;
  font-size: 13px;
  color: var(--text-secondary, #909399);
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.search-card :deep(.el-card__body) {
  padding: 16px 16px 0;
}
</style>
