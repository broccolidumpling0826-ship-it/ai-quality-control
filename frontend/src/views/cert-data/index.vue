<template>
  <div class="cert-data-page">
    <!-- 生成区 -->
    <el-card shadow="never" style="margin-bottom:12px">
      <template #header><span style="font-weight:600">生成质保书数据</span></template>
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
          width="160"
          :filters="getFilters('generateTime')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="operatorName"
          label="操作人"
          width="100"
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
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">查看详情</el-button>
            <el-button link type="success" @click="handleDownload(row)">下载</el-button>
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
          <el-descriptions-item label="卷号">{{ currentDetail.coilNo }}</el-descriptions-item>
          <el-descriptions-item label="批次号">{{ currentDetail.batchNo }}</el-descriptions-item>
          <el-descriptions-item label="炉号">{{ currentDetail.heatNo }}</el-descriptions-item>
          <el-descriptions-item label="品种">{{ currentDetail.productVariety }}</el-descriptions-item>
          <el-descriptions-item label="牌号">{{ currentDetail.productGrade }}</el-descriptions-item>
          <el-descriptions-item label="客户">{{ currentDetail.customer }}</el-descriptions-item>
        </el-descriptions>

        <el-table :data="currentDetail.indicators" border size="small">
          <el-table-column prop="indicatorName" label="指标名称" min-width="130" />
          <el-table-column prop="indicatorCode" label="指标代码" width="120" />
          <el-table-column prop="measuredValue" label="实测值" width="100" align="center" />
          <el-table-column prop="unit" label="单位" width="70" align="center" />
          <el-table-column prop="lowerLimit" label="标准下限" width="90" align="center">
            <template #default="{ row }">{{ row.lowerLimit ?? '-' }}</template>
          </el-table-column>
          <el-table-column prop="upperLimit" label="标准上限" width="90" align="center">
            <template #default="{ row }">{{ row.upperLimit ?? '-' }}</template>
          </el-table-column>
          <el-table-column label="结论" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.result === 'PASS' ? 'success' : 'danger'" size="small">
                {{ row.result === 'PASS' ? '合格' : '不合格' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </template>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button v-if="currentDetail?.fileUrl" type="success" @click="handleDownload(currentDetail)">下载</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useTableFilter } from '@/composables/use-table-filter'
import { generateCertData, pageCertData } from '@/api/cert-data'
import type { PageResult } from '@/types'

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
const tableData = ref<any[]>([])
const { getFilters, filterMethod } = useTableFilter(tableData)

const detailDialogVisible = ref(false)
const currentDetail = ref<any>(null)

async function handleGenerate() {
  if (!generateForm.coilNo && !generateForm.batchNo) {
    ElMessage.warning('请输入卷号或批次号')
    return
  }
  generateLoading.value = true
  try {
    const res = await generateCertData({
      coilNo: generateForm.coilNo || undefined,
      batchNo: generateForm.batchNo || undefined
    }) as any
    ElMessage.success('质保书数据生成成功')
    generateForm.coilNo = ''
    generateForm.batchNo = ''
    loadData()
    // 自动展示详情
    if (res) {
      currentDetail.value = res
      detailDialogVisible.value = true
    }
  } finally {
    generateLoading.value = false
  }
}

async function loadData() {
  loading.value = true
  try {
    const params: any = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      coilNo: searchForm.coilNo || undefined,
      batchNo: searchForm.batchNo || undefined
    }
    if (searchForm.timeRange) {
      params.startTime = searchForm.timeRange[0]
      params.endTime = searchForm.timeRange[1]
    }
    const res = await pageCertData(params) as PageResult<any>
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
  Object.assign(searchForm, { coilNo: '', batchNo: '', timeRange: null })
  pageNum.value = 1
  loadData()
}

function viewDetail(row: any) {
  currentDetail.value = row
  detailDialogVisible.value = true
}

function handleDownload(row: any) {
  if (row.fileUrl) {
    window.open(row.fileUrl, '_blank')
  } else {
    ElMessage.warning('暂无可下载文件')
  }
}

onMounted(loadData)
</script>

<style scoped>
.cert-data-page {
  padding: 16px;
}
.search-card :deep(.el-card__body) {
  padding: 16px 16px 0;
}
</style>
