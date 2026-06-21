<template>
  <div class="standard-conflicts-page">
    <el-card class="filter-card" shadow="never">
      <el-form :model="query" inline>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable style="width: 130px">
            <el-option label="待裁决" value="PENDING" />
            <el-option label="已处理" value="RESOLVED" />
          </el-select>
        </el-form-item>
        <el-form-item label="级别">
          <el-select v-model="query.conflictLevel" clearable style="width: 160px">
            <el-option label="阻断" value="BLOCKING" />
            <el-option label="优先级可解" value="PRIORITY_RESOLVABLE" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户">
          <el-input v-model="query.customerId" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="品种">
          <el-input v-model="query.variety" clearable style="width: 120px" />
        </el-form-item>
        <el-form-item label="牌号">
          <el-input v-model="query.grade" clearable style="width: 120px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" border stripe>
        <el-table-column prop="conflictNo" label="冲突编号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="conflictLevel" label="级别" width="150">
          <template #default="{ row }">
            <el-tag :type="row.conflictLevel === 'BLOCKING' ? 'danger' : 'warning'">
              {{ row.conflictLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PENDING' ? 'danger' : 'success'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="indicatorName" label="指标" min-width="120" />
        <el-table-column prop="customerId" label="客户" width="130" />
        <el-table-column prop="variety" label="品种" width="120" />
        <el-table-column prop="grade" label="牌号" width="110" />
        <el-table-column prop="inspectionDate" label="检验日期" width="120" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" :icon="View" @click="openDetail(row.id)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          layout="total, sizes, prev, pager, next"
          :total="total"
          @current-change="loadData"
          @size-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search, View } from '@element-plus/icons-vue'
import { pageStandardConflicts, type StandardConflict, type StandardConflictPageQuery } from '@/api/standard-conflict'

const router = useRouter()
const loading = ref(false)
const rows = ref<StandardConflict[]>([])
const total = ref(0)
const query = reactive<StandardConflictPageQuery>({ pageNum: 1, pageSize: 10 })

async function loadData() {
  loading.value = true
  try {
    const page = await pageStandardConflicts({ ...query })
    rows.value = page.records || []
    total.value = page.total || 0
  } finally {
    loading.value = false
  }
}

function reset() {
  query.status = ''
  query.conflictLevel = ''
  query.customerId = ''
  query.variety = ''
  query.grade = ''
  query.pageNum = 1
  loadData()
}

function openDetail(id: string) {
  router.push({ path: '/standard-conflicts/detail', query: { id } })
}

onMounted(loadData)
</script>

<style scoped>
.standard-conflicts-page {
  padding: 16px;
}

.filter-card,
.table-card {
  background: var(--bg-panel);
  border-color: var(--border-color);
}

.table-card {
  margin-top: 12px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
