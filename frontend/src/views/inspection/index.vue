<template>
  <div class="inspection-page">
    <!-- 搜索 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="卷号">
          <el-input v-model="searchForm.coilNo" placeholder="输入卷号" clearable style="width:150px" />
        </el-form-item>
        <el-form-item label="炉号">
          <el-input v-model="searchForm.heatNo" placeholder="输入炉号" clearable style="width:150px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width:130px">
            <el-option
              v-for="item in dictStore.getItems('INSPECTION_STATUS')"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="样品类型">
          <el-select v-model="searchForm.sampleType" placeholder="请选择" clearable style="width:130px">
            <el-option
              v-for="item in dictStore.getItems('SAMPLE_TYPE')"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="检验时间">
          <el-date-picker
            v-model="searchForm.timeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width:240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">新建检验</el-button>
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
        <el-table-column
          prop="heatNo"
          label="炉号"
          width="130"
          :filters="getFilters('heatNo')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="coilNo"
          label="卷号"
          width="130"
          :filters="getFilters('coilNo')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="batchNo"
          label="批次号"
          width="130"
          :filters="getFilters('batchNo')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="customerId"
          label="客户"
          min-width="160"
          show-overflow-tooltip
          :filters="getFilters('customerLabel')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        >
          <template #default="{ row }">
            {{ formatCustomerLabel(row.customerId) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="sampleType"
          label="样品类型"
          width="110"
          :filters="getFilters('sampleType')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        >
          <template #default="{ row }">
            {{ dictStore.getLabel('SAMPLE_TYPE', row.sampleType) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="testTime"
          label="检验时间"
          width="160"
          :filters="getFilters('testTime')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="testerNo"
          label="检验人"
          width="100"
          :filters="getFilters('testerNo')"
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
            <el-tag :type="dictStore.getColorTag('INSPECTION_STATUS', row.status) as any">
              {{ dictStore.getLabel('INSPECTION_STATUS', row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">查看详情</el-button>
            <el-button
              v-if="row.status !== 'VOID'"
              link
              type="danger"
              @click="handleVoid(row)"
            >作废</el-button>
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

    <!-- 作废对话框 -->
    <el-dialog v-model="voidDialogVisible" title="作废检验记录" width="440px">
      <el-alert type="warning" :closable="false" style="margin-bottom:12px">
        作废后该检验记录将无法恢复，请谨慎操作。
      </el-alert>
      <el-form ref="voidFormRef" :model="voidForm" :rules="voidRules" label-width="80px">
        <el-form-item label="作废原因" prop="reason">
          <el-input
            v-model="voidForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入作废原因（必填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="voidDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="voidLoading" @click="confirmVoid">确认作废</el-button>
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
import { useTableFilter } from '@/composables/use-table-filter'
import { pageInspections, voidInspection } from '@/api/inspection'
import type { PageResult } from '@/types'

const router = useRouter()
const dictStore = useDictStore()

const searchForm = reactive({
  coilNo: '',
  heatNo: '',
  status: '',
  sampleType: '',
  timeRange: null as [string, string] | null
})

const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const loading = ref(false)
const tableData = ref<any[]>([])
const { getFilters, filterMethod } = useTableFilter(tableData)

const voidDialogVisible = ref(false)
const voidLoading = ref(false)
const voidTarget = ref<any>(null)
const voidFormRef = ref<FormInstance>()
const voidForm = reactive({ reason: '' })
const voidRules = {
  reason: [{ required: true, message: '请输入作废原因', trigger: 'blur' }]
}

function formatCustomerLabel(customerId?: string) {
  if (!customerId) return '-'
  return dictStore.getLabel('QC_CUSTOMER', customerId)
}

async function loadData() {
  loading.value = true
  try {
    const params: any = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      coilNo: searchForm.coilNo || undefined,
      heatNo: searchForm.heatNo || undefined,
      status: searchForm.status || undefined,
      sampleType: searchForm.sampleType || undefined
    }
    if (searchForm.timeRange) {
      params.startTime = searchForm.timeRange[0]
      params.endTime = searchForm.timeRange[1]
    }
    const res = await pageInspections(params) as PageResult<any>
    tableData.value = (res.records || []).map((row) => ({
      ...row,
      testTime: row.testTime ?? row.inspectionTime,
      testerNo: row.testerNo ?? row.inspector,
      customerLabel: formatCustomerLabel(row.customerId)
    }))
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
  Object.assign(searchForm, { coilNo: '', heatNo: '', status: '', sampleType: '', timeRange: null })
  pageNum.value = 1
  loadData()
}

function handleAdd() {
  router.push('/inspection/form')
}

function handleViewDetail(row: any) {
  router.push(`/judgment/explanation?recordId=${row.id}`)
}

function handleVoid(row: any) {
  voidTarget.value = row
  voidForm.reason = ''
  voidDialogVisible.value = true
}

async function confirmVoid() {
  await voidFormRef.value?.validate()
  voidLoading.value = true
  try {
    await voidInspection(voidTarget.value.id, voidForm.reason)
    ElMessage.success('作废成功')
    voidDialogVisible.value = false
    loadData()
  } finally {
    voidLoading.value = false
  }
}

onMounted(async () => {
  await dictStore.loadAll()
  try {
    await dictStore.refreshItems('QC_CUSTOMER')
  } catch {
    // 字典加载失败时仍用 getLabel 兜底显示字典值
  }
  loadData()
})
</script>

<style scoped>
.inspection-page {
  padding: 16px;
}
.search-card :deep(.el-card__body) {
  padding: 16px 16px 0;
}
</style>
