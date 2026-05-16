<template>
  <div class="concession-page">
    <!-- 搜索 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="客户确认状态">
          <el-select v-model="searchForm.confirmStatus" placeholder="请选择" clearable style="width:140px">
            <el-option
              v-for="item in dictStore.getItems('CONFIRM_STATUS')"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="总状态">
          <el-select v-model="searchForm.concessionStatus" placeholder="请选择" clearable style="width:140px">
            <el-option
              v-for="item in dictStore.getItems('CONCESSION_STATUS')"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="卷号">
          <el-input v-model="searchForm.coilNo" placeholder="输入卷号" clearable style="width:150px" />
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
        :row-class-name="rowClassName"
      >
        <el-table-column label="关联卷号/批次" min-width="180">
          <template #default="{ row }">
            <div>卷号：{{ row.coilNo }}</div>
            <div style="font-size:12px;color:#666">批次：{{ row.batchNo }}</div>
          </template>
        </el-table-column>
        <el-table-column
          prop="concessionScope"
          label="让步范围"
          min-width="180"
          show-overflow-tooltip
          :filters="getFilters('concessionScope')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column label="有效期" width="180">
          <template #default="{ row }">
            <div>{{ row.validFrom }} ~ {{ row.validTo }}</div>
            <div :class="['remaining-days', remainingDaysClass(row.validTo)]">
              剩余 {{ remainingDays(row.validTo) }} 天
            </div>
          </template>
        </el-table-column>
        <el-table-column label="客户确认状态" width="120">
          <template #default="{ row }">
            <el-tag :type="dictStore.getColorTag('CONFIRM_STATUS', row.confirmStatus) as any">
              {{ dictStore.getLabel('CONFIRM_STATUS', row.confirmStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="总状态" width="110">
          <template #default="{ row }">
            <el-tag :type="dictStore.getColorTag('CONCESSION_STATUS', row.concessionStatus) as any">
              {{ dictStore.getLabel('CONCESSION_STATUS', row.concessionStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="applyByName"
          label="申请人"
          width="90"
          :filters="getFilters('applyByName')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="applyTime"
          label="申请时间"
          width="160"
          :filters="getFilters('applyTime')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">查看详情</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useDictStore } from '@/store/dict'
import { useTableFilter } from '@/composables/use-table-filter'
import { pageConcessions } from '@/api/concession'
import type { PageResult } from '@/types'

const router = useRouter()
const dictStore = useDictStore()

const searchForm = reactive({ confirmStatus: '', concessionStatus: '', coilNo: '' })
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const loading = ref(false)
const tableData = ref<any[]>([])
const { getFilters, filterMethod } = useTableFilter(tableData)

function remainingDays(validTo: string): number {
  if (!validTo) return 0
  const diff = new Date(validTo).getTime() - Date.now()
  return Math.max(0, Math.ceil(diff / (24 * 3600 * 1000)))
}

function remainingDaysClass(validTo: string): string {
  const days = remainingDays(validTo)
  if (days <= 7) return 'days-warning'
  return 'days-normal'
}

function rowClassName({ row }: { row: any }): string {
  if (remainingDays(row.validTo) <= 3) return 'expiring-row'
  return ''
}

async function loadData() {
  loading.value = true
  try {
    const res = await pageConcessions({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      confirmStatus: searchForm.confirmStatus || undefined,
      concessionStatus: searchForm.concessionStatus || undefined,
      coilNo: searchForm.coilNo || undefined
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
  Object.assign(searchForm, { confirmStatus: '', concessionStatus: '', coilNo: '' })
  pageNum.value = 1
  loadData()
}

function viewDetail(row: any) {
  router.push(`/concession/detail?id=${row.id}`)
}

onMounted(loadData)
</script>

<style scoped>
.concession-page {
  padding: 16px;
}
.search-card :deep(.el-card__body) {
  padding: 16px 16px 0;
}
.remaining-days {
  font-size: 12px;
  margin-top: 2px;
}
.days-warning {
  color: #e6a23c;
  font-weight: 600;
}
.days-normal {
  color: #909399;
}
:deep(.expiring-row) {
  background-color: #fdf6ec !important;
}
:deep(.expiring-row:hover td) {
  background-color: #faecd8 !important;
}
</style>
