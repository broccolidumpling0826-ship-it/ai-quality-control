<template>
  <div class="audit-page">
    <!-- 搜索 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.operationType" placeholder="请选择" clearable style="width:150px">
            <el-option
              v-for="item in dictStore.getItems('AUDIT_OPERATION_TYPE')"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人工号">
          <el-input v-model="searchForm.operatorNo" placeholder="输入工号" clearable style="width:150px" />
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="searchForm.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width:320px"
          />
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
        @row-click="handleRowClick"
      >
        <el-table-column
          prop="operationType"
          label="操作类型"
          width="130"
          :filters="getFilters('operationType')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        >
          <template #default="{ row }">
            <el-tag :type="dictStore.getColorTag('AUDIT_OPERATION_TYPE', row.operationType) as any" size="small">
              {{ dictStore.getLabel('AUDIT_OPERATION_TYPE', row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="operationModule"
          label="操作模块"
          width="120"
          :filters="getFilters('operationModule')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="operationObject"
          label="操作对象"
          width="130"
          :filters="getFilters('operationObject')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="operationObjectId"
          label="对象ID"
          width="140"
          show-overflow-tooltip
          :filters="getFilters('operationObjectId')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="operatorName"
          label="操作人"
          width="90"
          :filters="getFilters('operatorName')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="operatorNo"
          label="工号"
          width="90"
          :filters="getFilters('operatorNo')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="operateTime"
          label="操作时间"
          width="170"
          :filters="getFilters('operateTime')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="ip"
          label="IP地址"
          width="140"
          :filters="getFilters('ip')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column label="变更详情" width="90" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.beforeValue || row.afterValue"
              link
              type="primary"
              size="small"
              @click.stop="viewDiff(row)"
            >查看</el-button>
            <span v-else style="color:#c0c4cc">-</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top:16px;justify-content:flex-end"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <!-- 变更详情对话框 -->
    <el-dialog v-model="diffDialogVisible" title="变更详情" width="900px" destroy-on-close>
      <div v-if="diffRow" class="diff-container">
        <div class="diff-panel">
          <div class="diff-panel-title">变更前</div>
          <pre class="json-block">{{ formatJson(diffRow.beforeValue) }}</pre>
        </div>
        <div class="diff-panel">
          <div class="diff-panel-title">变更后</div>
          <pre class="json-block after">{{ formatJson(diffRow.afterValue) }}</pre>
        </div>
      </div>
      <template #footer>
        <el-button @click="diffDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useDictStore } from '@/store/dict'
import { useTableFilter } from '@/composables/use-table-filter'
import { pageAuditLogs } from '@/api/audit'
import type { PageResult } from '@/types'

const dictStore = useDictStore()

const searchForm = reactive({
  operationType: '',
  operatorNo: '',
  timeRange: null as [string, string] | null
})

const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const loading = ref(false)
const tableData = ref<any[]>([])
const { getFilters, filterMethod } = useTableFilter(tableData)

const diffDialogVisible = ref(false)
const diffRow = ref<any>(null)

async function loadData() {
  loading.value = true
  try {
    const params: any = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      operationType: searchForm.operationType || undefined,
      operatorNo: searchForm.operatorNo || undefined
    }
    if (searchForm.timeRange) {
      params.startTime = searchForm.timeRange[0]
      params.endTime = searchForm.timeRange[1]
    }
    const res = await pageAuditLogs(params) as PageResult<any>
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
  Object.assign(searchForm, { operationType: '', operatorNo: '', timeRange: null })
  pageNum.value = 1
  loadData()
}

function handleRowClick(row: any) {
  if (row.beforeValue || row.afterValue) {
    viewDiff(row)
  }
}

function viewDiff(row: any) {
  diffRow.value = row
  diffDialogVisible.value = true
}

function formatJson(jsonStr: string): string {
  if (!jsonStr) return '(空)'
  try {
    const obj = typeof jsonStr === 'string' ? JSON.parse(jsonStr) : jsonStr
    return JSON.stringify(obj, null, 2)
  } catch {
    return jsonStr
  }
}

onMounted(loadData)
</script>

<style scoped>
.audit-page {
  padding: 16px;
}
.search-card :deep(.el-card__body) {
  padding: 16px 16px 0;
}
:deep(.el-table__row) {
  cursor: pointer;
}
.diff-container {
  display: flex;
  gap: 16px;
}
.diff-panel {
  flex: 1;
  min-width: 0;
}
.diff-panel-title {
  font-weight: 600;
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
  padding: 4px 8px;
  background: #f5f7fa;
  border-radius: 4px;
}
.json-block {
  background: #fafafa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
  font-size: 12px;
  font-family: 'Courier New', monospace;
  overflow: auto;
  max-height: 400px;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  color: #333;
}
.json-block.after {
  background: #f0f9eb;
  border-color: #b3e19d;
}
</style>
