<template>
  <div class="judgment-page">
    <!-- 搜索 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="卷号">
          <el-input v-model="searchForm.coilNo" placeholder="输入卷号" clearable style="width:150px" />
        </el-form-item>
        <el-form-item label="批次号">
          <el-input v-model="searchForm.batchNo" placeholder="输入批次号" clearable style="width:150px" />
        </el-form-item>
        <el-form-item label="判定结论">
          <el-select v-model="searchForm.judgmentType" placeholder="请选择" clearable style="width:140px">
            <el-option
              v-for="item in dictStore.getItems('JUDGMENT_TYPE')"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="判定时间">
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
          prop="heatNo"
          label="炉号"
          width="120"
          :filters="getFilters('heatNo')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="productVariety"
          label="品种"
          width="100"
          :filters="getFilters('productVariety')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        >
          <template #default="{ row }">
            {{ dictStore.getLabel('PRODUCT_VARIETY', row.productVariety) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="productGrade"
          label="牌号"
          width="100"
          :filters="getFilters('productGrade')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="judgmentType"
          label="判定结论"
          width="120"
          :filters="getFilters('judgmentType')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        >
          <template #default="{ row }">
            <el-tag :type="dictStore.getColorTag('JUDGMENT_TYPE', row.judgmentType) as any">
              {{ dictStore.getLabel('JUDGMENT_TYPE', row.judgmentType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="judgmentTime"
          label="判定时间"
          width="160"
          :filters="getFilters('judgmentTime')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="inspector"
          label="检验人"
          width="100"
          :filters="getFilters('inspector')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click.stop="viewExplanation(row)">查看解释</el-button>
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
import { pageJudgments } from '@/api/judgment'
import type { PageResult } from '@/types'

const router = useRouter()
const dictStore = useDictStore()

const searchForm = reactive({
  coilNo: '',
  batchNo: '',
  judgmentType: '',
  timeRange: null as [string, string] | null
})

const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const loading = ref(false)
const tableData = ref<any[]>([])
const { getFilters, filterMethod } = useTableFilter(tableData)

async function loadData() {
  loading.value = true
  try {
    const params: any = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      coilNo: searchForm.coilNo || undefined,
      batchNo: searchForm.batchNo || undefined,
      judgmentType: searchForm.judgmentType || undefined
    }
    if (searchForm.timeRange) {
      params.timeStart = `${searchForm.timeRange[0]} 00:00:00`
      params.timeEnd = `${searchForm.timeRange[1]} 23:59:59`
    }
    const res = await pageJudgments(params) as PageResult<any>
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
  Object.assign(searchForm, { coilNo: '', batchNo: '', judgmentType: '', timeRange: null })
  pageNum.value = 1
  loadData()
}

function handleRowClick(row: any) {
  viewExplanation(row)
}

function viewExplanation(row: any) {
  router.push(`/judgment/explanation?id=${row.id ?? row.judgmentId}`)
}

onMounted(loadData)
</script>

<style scoped>
.judgment-page {
  padding: 16px;
}
.search-card :deep(.el-card__body) {
  padding: 16px 16px 0;
}
:deep(.el-table__row) {
  cursor: pointer;
}
</style>
