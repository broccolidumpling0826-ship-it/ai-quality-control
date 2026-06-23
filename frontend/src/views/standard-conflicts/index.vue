<template>
  <div class="standard-conflicts-page">

    <div class="search-bar">
      <el-select v-model="query.status" clearable placeholder="状态" class="search-select">
        <el-option label="待裁定" value="PENDING" />
        <el-option label="已裁定" value="RESOLVED" />
      </el-select>
      <el-select v-model="query.conflictLevel" clearable placeholder="级别" class="search-select wide">
        <el-option label="阻断" value="BLOCKING" />
        <el-option label="优先级可解" value="PRIORITY_RESOLVABLE" />
      </el-select>
      <el-select v-model="query.variety" clearable filterable placeholder="品种" class="search-select">
        <el-option
          v-for="item in dictStore.getItems('PRODUCT_VARIETY')"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-select v-model="query.grade" clearable filterable placeholder="牌号" class="search-select wide">
        <el-option
          v-for="item in dictStore.getItems('PRODUCT_GRADE')"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <div class="split-layout">
      <div class="list-panel">
        <el-table
          v-loading="loading"
          :data="rows"
          border
          stripe
          highlight-current-row
          row-key="id"
          :current-row-key="selectedId"
          empty-text="暂无冲突记录"
          @row-click="selectRow"
        >
          <el-table-column prop="variety" label="品种" min-width="96" show-overflow-tooltip />
          <el-table-column prop="grade" label="牌号" width="96">
            <template #default="{ row }">
              <span class="mono">{{ row.grade }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="indicatorName" label="指标" min-width="110" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="96">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 'PENDING' ? 'warning' : 'success'">
                {{ row.status === 'PENDING' ? '待裁定' : '已裁定' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <div class="pager">
          <el-pagination
            v-model:current-page="query.pageNum"
            v-model:page-size="query.pageSize"
            layout="total, prev, pager, next"
            :total="total"
            @current-change="loadData"
            @size-change="loadData"
          />
        </div>
      </div>

      <StandardConflictDetailPanel
        class="detail-panel"
        :detail="selectedDetail"
        :loading="detailLoading"
        @resolved="handleResolved"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { useDictStore } from '@/store/dict'
import StandardConflictDetailPanel from '@/components/standard-conflicts/StandardConflictDetailPanel.vue'
import {
  getStandardConflict,
  pageStandardConflicts,
  type StandardConflict,
  type StandardConflictPageQuery
} from '@/api/standard-conflict'

const route = useRoute()
const dictStore = useDictStore()
const loading = ref(false)
const detailLoading = ref(false)
const rows = ref<StandardConflict[]>([])
const total = ref(0)
const selectedId = ref('')
const selectedDetail = ref<StandardConflict | null>(null)
const query = reactive<StandardConflictPageQuery>({
  pageNum: 1,
  pageSize: 10,
  status: 'PENDING'
})

async function loadData() {
  loading.value = true
  try {
    const page = await pageStandardConflicts({ ...query })
    rows.value = page.records || []
    total.value = page.total || 0
    if (!rows.value.length) {
      selectedId.value = ''
      selectedDetail.value = null
      return
    }
    const routeId = String(route.query.id || '')
    const preferredId = routeId && rows.value.some((row) => row.id === routeId)
      ? routeId
      : selectedId.value && rows.value.some((row) => row.id === selectedId.value)
        ? selectedId.value
        : rows.value[0].id
    await selectById(preferredId)
  } finally {
    loading.value = false
  }
}

async function selectById(id: string) {
  if (!id) return
  selectedId.value = id
  detailLoading.value = true
  try {
    selectedDetail.value = await getStandardConflict(id)
  } finally {
    detailLoading.value = false
  }
}

function selectRow(row: StandardConflict) {
  if (row.id !== selectedId.value) {
    selectById(row.id)
  }
}

function handleResolved(detail: StandardConflict) {
  selectedDetail.value = detail
  loadData()
}

function reset() {
  query.status = 'PENDING'
  query.conflictLevel = ''
  query.customerId = ''
  query.variety = ''
  query.grade = ''
  query.pageNum = 1
  loadData()
}

async function loadFilterOptions() {
  if (!dictStore.loaded) {
    await dictStore.loadAll()
  }
}

onMounted(async () => {
  await loadFilterOptions()
  await loadData()
})
</script>

<style scoped>
.standard-conflicts-page {
  padding: 16px;
}

.page-header {
  margin-bottom: 12px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-mark {
  color: var(--gold, #ffb400);
  font-size: 16px;
}

.title-text {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.page-desc {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-muted);
}

.search-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
  padding: 12px;
  background: var(--bg-panel);
  border: 1px solid var(--border-color);
  border-radius: 4px;
}

.search-input {
  width: 120px;
}

.search-select {
  width: 120px;
}

.search-select.wide {
  width: 150px;
}

.split-layout {
  display: grid;
  grid-template-columns: minmax(360px, 42%) minmax(420px, 1fr);
  gap: 12px;
  align-items: start;
}

.list-panel,
.detail-panel {
  min-width: 0;
}

.list-panel {
  background: var(--bg-panel);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  padding: 8px 8px 0;
}

.mono {
  font-family: var(--font-data);
}

.pager {
  display: flex;
  justify-content: flex-end;
  padding: 8px 4px 10px;
}

@media (max-width: 1100px) {
  .split-layout {
    grid-template-columns: 1fr;
  }
}
</style>
