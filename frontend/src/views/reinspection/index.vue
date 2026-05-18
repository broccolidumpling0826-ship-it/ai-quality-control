<template>
  <div class="reinspection-page">
    <!-- 搜索 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width:140px">
            <el-option
              v-for="item in dictStore.getItems('REINSPECTION_STATUS')"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="责任人工号">
          <el-input v-model="searchForm.responsibleNo" placeholder="输入工号" clearable style="width:150px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-card shadow="never" style="margin-top:12px">
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width:100%"
      >
        <el-table-column label="原判定记录" min-width="220">
          <template #default="{ row }">
            <div>
              <div>炉号：{{ row.heatNo || '—' }}</div>
              <div>卷号：{{ row.coilNo || '—' }}</div>
              <div class="text-meta-sm">
                判定：
                <el-tag :type="dictStore.getColorTag('JUDGMENT_TYPE', row.originalJudgmentType) as any" size="small">
                  {{ dictStore.getLabel('JUDGMENT_TYPE', row.originalJudgmentType) }}
                </el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="reinspectionReason"
          label="复检原因"
          min-width="180"
          show-overflow-tooltip
          :filters="getFilters('reinspectionReason')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="responsibleNo"
          label="责任人工号"
          width="110"
          :filters="getFilters('responsibleNo')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="createDateTime"
          label="发起时间"
          width="160"
          :filters="getFilters('createDateTime')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="status"
          label="状态"
          width="110"
          :filters="getFilters('status')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        >
          <template #default="{ row }">
            <el-tag :type="dictStore.getColorTag('REINSPECTION_STATUS', row.status) as any">
              {{ dictStore.getLabel('REINSPECTION_STATUS', row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="新检验记录" width="130">
          <template #default="{ row }">
            <span v-if="row.newRecordId">
              <el-button link type="primary" @click="viewNewRecord(row)">{{ row.newRecordId }}</el-button>
            </span>
            <span v-else class="text-muted">未关联</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              link
              type="success"
              @click="handleComplete(row)"
            >完成复检</el-button>
            <el-button link type="primary" @click="viewOriginal(row)">查看原判定</el-button>
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

    <!-- 完成复检弹窗 -->
    <el-dialog v-model="completeDialogVisible" title="完成复检" width="860px" destroy-on-close>
      <el-alert type="info" :closable="false" show-icon class="complete-context-alert">
        <template #title>原检验记录（已自动排除，不可选择）</template>
        <div class="complete-context-grid">
          <span>炉号：{{ completeTarget?.heatNo || '—' }}</span>
          <span>卷号：{{ completeTarget?.coilNo || '—' }}</span>
          <span>批次号：{{ completeTarget?.batchNo || '—' }}</span>
          <span>客户：{{ formatCustomer(completeTarget?.customerId) }}</span>
        </div>
      </el-alert>

      <el-form :model="candidateFilter" inline class="candidate-filter-form" @submit.prevent="searchCandidates">
        <el-form-item label="炉号">
          <el-input v-model="candidateFilter.heatNo" placeholder="炉号" clearable style="width:130px" />
        </el-form-item>
        <el-form-item label="卷号">
          <el-input v-model="candidateFilter.coilNo" placeholder="卷号" clearable style="width:130px" />
        </el-form-item>
        <el-form-item label="批次号">
          <el-input v-model="candidateFilter.batchNo" placeholder="批次号" clearable style="width:130px" />
        </el-form-item>
        <el-form-item label="客户">
          <el-select
            v-model="candidateFilter.customerId"
            placeholder="请选择"
            clearable
            filterable
            style="width:160px"
          >
            <el-option
              v-for="item in dictStore.getItems('QC_CUSTOMER')"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit" :loading="candidateLoading">查询</el-button>
          <el-button @click.prevent="resetCandidateFilter">重置筛选</el-button>
        </el-form-item>
      </el-form>

      <el-form ref="completeFormRef" :model="completeForm" :rules="completeRules">
        <el-form-item prop="newRecordId" label-width="0">
          <el-table
            ref="candidateTableRef"
            v-loading="candidateLoading"
            :data="candidateList"
            border
            stripe
            row-key="id"
            max-height="320"
            style="width:100%"
            @selection-change="onCandidateSelectionChange"
          >
            <el-table-column type="selection" width="48" />
            <el-table-column prop="heatNo" label="炉号" width="120" show-overflow-tooltip />
            <el-table-column prop="coilNo" label="卷号" width="120" show-overflow-tooltip />
            <el-table-column prop="batchNo" label="批次号" width="120" show-overflow-tooltip />
            <el-table-column label="客户" width="120" show-overflow-tooltip>
              <template #default="{ row }">{{ formatCustomer(row.customerId) }}</template>
            </el-table-column>
            <el-table-column label="品种/牌号" min-width="140" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.productVariety || '—' }} / {{ row.productGrade || '—' }}
              </template>
            </el-table-column>
            <el-table-column prop="testTime" label="检验时间" width="160" />
            <el-table-column label="样品类型" width="100">
              <template #default="{ row }">
                {{ dictStore.getLabel('SAMPLE_TYPE', row.sampleType) }}
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="candidatePageNum"
            v-model:page-size="candidatePageSize"
            :total="candidateTotal"
            :page-sizes="[10, 20]"
            layout="total, prev, pager, next"
            small
            style="margin-top:12px;justify-content:flex-end"
            @current-change="loadCandidates"
            @size-change="onCandidatePageSizeChange"
          />
          <p v-if="!candidateLoading && candidateList.length === 0" class="text-hint candidate-empty-hint">
            未找到可关联的检验记录，请调整筛选条件或先完成新检验录入
          </p>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="completeLoading" :disabled="!completeForm.newRecordId" @click="confirmComplete">
          确认完成
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, TableInstance } from 'element-plus'
import { useDictStore } from '@/store/dict'
import { useTableFilter } from '@/composables/use-table-filter'
import {
  pageReinspections,
  completeReinspection,
  listReinspectionCandidateInspections,
  type InspectionRecordCandidate
} from '@/api/reinspection'
import type { PageResult } from '@/types'

const router = useRouter()
const dictStore = useDictStore()

const searchForm = reactive({ status: '', responsibleNo: '' })
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const loading = ref(false)
const tableData = ref<any[]>([])
const { getFilters, filterMethod } = useTableFilter(tableData)

const completeDialogVisible = ref(false)
const completeLoading = ref(false)
const completeTarget = ref<any>(null)
const completeFormRef = ref<FormInstance>()
const completeForm = reactive({ newRecordId: '' })
const completeRules = {
  newRecordId: [{ required: true, message: '请选择新检验记录', trigger: 'change' }]
}

const candidateFilter = reactive({
  heatNo: '',
  coilNo: '',
  batchNo: '',
  customerId: ''
})
const candidateList = ref<InspectionRecordCandidate[]>([])
const candidateLoading = ref(false)
const candidatePageNum = ref(1)
const candidatePageSize = ref(10)
const candidateTotal = ref(0)
const candidateTableRef = ref<TableInstance>()

function formatCustomer(customerId?: string) {
  if (!customerId) return '—'
  return dictStore.getLabel('QC_CUSTOMER', customerId) || customerId
}

async function loadData() {
  loading.value = true
  try {
    const res = await pageReinspections({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: searchForm.status || undefined,
      responsibleNo: searchForm.responsibleNo || undefined
    }) as PageResult<any>
    tableData.value = res.records || []
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
  Object.assign(searchForm, { status: '', responsibleNo: '' })
  pageNum.value = 1
  loadData()
}

function prefillCandidateFilter(row: any) {
  candidateFilter.heatNo = row.heatNo || ''
  candidateFilter.coilNo = row.coilNo || ''
  candidateFilter.batchNo = row.batchNo || ''
  candidateFilter.customerId = row.customerId || ''
}

function resetCandidateFilter() {
  if (completeTarget.value) {
    prefillCandidateFilter(completeTarget.value)
  } else {
    Object.assign(candidateFilter, { heatNo: '', coilNo: '', batchNo: '', customerId: '' })
  }
  candidatePageNum.value = 1
  loadCandidates()
}

function trimFilter(value: string) {
  const v = value?.trim()
  return v || undefined
}

async function loadCandidates() {
  if (!completeTarget.value?.id) return
  candidateLoading.value = true
  try {
    const res = await listReinspectionCandidateInspections(completeTarget.value.id, {
      heatNo: trimFilter(candidateFilter.heatNo),
      coilNo: trimFilter(candidateFilter.coilNo),
      batchNo: trimFilter(candidateFilter.batchNo),
      customerId: trimFilter(candidateFilter.customerId),
      excludeRecordId: completeTarget.value.originalRecordId || undefined,
      pageNum: candidatePageNum.value,
      pageSize: candidatePageSize.value
    })
    candidateList.value = res.records || []
    candidateTotal.value = res.total || 0
    if (completeForm.newRecordId && !candidateList.value.some((r) => r.id === completeForm.newRecordId)) {
      completeForm.newRecordId = ''
    }
    await nextTick()
    syncCandidateTableSelection()
  } finally {
    candidateLoading.value = false
  }
}

function syncCandidateTableSelection() {
  const table = candidateTableRef.value
  if (!table) return
  table.clearSelection()
  if (!completeForm.newRecordId) return
  const row = candidateList.value.find((r) => r.id === completeForm.newRecordId)
  if (row) {
    table.toggleRowSelection(row, true)
  }
}

function onCandidateSelectionChange(rows: InspectionRecordCandidate[]) {
  if (rows.length === 0) {
    completeForm.newRecordId = ''
    return
  }
  const selected = rows[rows.length - 1]
  if (rows.length > 1) {
    candidateTableRef.value?.clearSelection()
    candidateTableRef.value?.toggleRowSelection(selected, true)
  }
  completeForm.newRecordId = selected.id
}

function searchCandidates() {
  candidatePageNum.value = 1
  loadCandidates()
}

function onCandidatePageSizeChange() {
  candidatePageNum.value = 1
  loadCandidates()
}

function handleComplete(row: any) {
  completeTarget.value = row
  completeForm.newRecordId = ''
  prefillCandidateFilter(row)
  candidatePageNum.value = 1
  completeDialogVisible.value = true
  loadCandidates()
}

async function confirmComplete() {
  await completeFormRef.value?.validate()
  completeLoading.value = true
  try {
    await completeReinspection(completeTarget.value.id, completeForm.newRecordId)
    ElMessage.success('复检已完成')
    completeDialogVisible.value = false
    loadData()
  } finally {
    completeLoading.value = false
  }
}

function viewOriginal(row: any) {
  router.push(`/judgment/explanation?id=${row.originalJudgmentId}`)
}

function viewNewRecord(row: any) {
  router.push(`/judgment/explanation?recordId=${row.newRecordId}`)
}

onMounted(async () => {
  await dictStore.refreshItems('QC_CUSTOMER')
  loadData()
})
</script>

<style scoped>
.reinspection-page {
  padding: 16px;
}
.search-card :deep(.el-card__body) {
  padding: 16px 16px 0;
}
.complete-context-alert {
  margin-bottom: 16px;
}
.complete-context-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 4px 16px;
  margin-top: 4px;
  font-size: 13px;
}
.candidate-filter-form {
  margin-bottom: 8px;
}
.candidate-empty-hint {
  margin-top: 8px;
  text-align: center;
}
</style>
