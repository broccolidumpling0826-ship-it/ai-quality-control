<template>
  <div class="gap-page">
    <!-- 页头 -->
    <div class="page-header">
      <div class="page-title">
        <span class="title-mark">⚠</span>
        <span class="title-text">标准覆盖缺口</span>
        <span class="title-sub">StandardGap / FR-015</span>
      </div>
      <div class="page-desc">
        判定引擎发现的无标准覆盖指标记录，需质量工程师补充对应标准限值。
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="query.variety"
        placeholder="品种模糊查询"
        clearable
        class="search-input"
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      />
      <el-input
        v-model="query.grade"
        placeholder="牌号模糊查询"
        clearable
        class="search-input"
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      />
      <el-select
        v-model="query.isResolved"
        placeholder="处理状态"
        clearable
        class="search-select"
        @change="handleSearch"
      >
        <el-option label="未解决" :value="0" />
        <el-option label="已解决" :value="1" />
      </el-select>
      <el-button type="primary" @click="handleSearch" class="search-btn">查询</el-button>
      <el-button @click="handleReset" class="reset-btn">重置</el-button>
    </div>

    <!-- 数据表格 -->
    <div class="table-wrap">
      <el-table
        :data="tableData"
        v-loading="loading"
        row-class-name="gap-row"
        empty-text="暂无缺口记录"
        style="width: 100%"
      >
        <el-table-column
          label="品种"
          prop="variety"
          min-width="100"
          :filters="getFilters('variety')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          label="牌号"
          prop="grade"
          min-width="100"
          :filters="getFilters('grade')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          label="缺口指标"
          prop="indicatorName"
          min-width="130"
          :filters="getFilters('indicatorName')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        >
          <template #default="{ row }">
            <span class="indicator-tag">{{ row.indicatorName }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="首次发现时间"
          prop="firstFoundTime"
          min-width="160"
          :filters="getFilters('firstFoundTime')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        >
          <template #default="{ row }">
            <span class="mono-text">{{ row.firstFoundTime }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="关联检验记录"
          prop="relatedRecordId"
          min-width="150"
          :filters="getFilters('relatedRecordId')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        >
          <template #default="{ row }">
            <span class="mono-text record-id">{{ row.relatedRecordId }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="状态"
          prop="isResolved"
          min-width="100"
          align="center"
          :filters="getFilters('isResolved')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        >
          <template #default="{ row }">
            <el-tag
              :type="row.isResolved === 1 ? 'success' : 'warning'"
              size="small"
              class="status-tag"
            >
              {{ row.isResolved === 1 ? '已解决' : '未解决' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.isResolved === 0"
              size="small"
              class="op-btn op-btn-resolve"
              @click="handleResolve(row)"
            >
              标记已解决
            </el-button>
            <span v-else class="resolved-label">—</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          background
          @current-change="loadData"
          @size-change="handleSearch"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTableFilter } from '@/composables/use-table-filter'
import { standardGapApi, type StandardGapVO } from '@/api/standard-gap'

const loading = ref(false)
const tableData = ref<StandardGapVO[]>([])
const total = ref(0)
const { getFilters, filterMethod } = useTableFilter(tableData)

const query = reactive({
  variety: '',
  grade: '',
  isResolved: null as number | null,
  pageNum: 1,
  pageSize: 20,
})

async function loadData() {
  loading.value = true
  try {
    const res = await standardGapApi.page({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      variety: query.variety || undefined,
      grade: query.grade || undefined,
      isResolved: query.isResolved ?? undefined,
    })
    tableData.value = res?.records ?? []
    total.value = res?.total ?? 0
  } catch {
    // error handled by request interceptor
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNum = 1
  loadData()
}

function handleReset() {
  query.variety = ''
  query.grade = ''
  query.isResolved = null
  handleSearch()
}

async function handleResolve(row: StandardGapVO) {
  await ElMessageBox.confirm(
    `确认将"${row.variety} / ${row.grade} / ${row.indicatorName}"缺口标记为已解决？`,
    '标记已解决',
    { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' }
  )
  await standardGapApi.resolve(row.id)
  ElMessage.success('已标记为解决，请及时在标准库中补充对应指标限值')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.gap-page {
  min-height: 100%;
}

/* ── 页头 ─────────────────────────────────────────── */
.page-header {
  margin-bottom: 20px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.title-mark {
  font-size: 18px;
  color: var(--gold, #FFB400);
}

.title-text {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.title-sub {
  font-family: var(--font-data, monospace);
  font-size: 10px;
  color: var(--text-muted);
  letter-spacing: 0.08em;
  padding: 2px 8px;
  border: 1px solid var(--border, #1e3a5f);
  border-radius: 3px;
}

.page-desc {
  font-size: 12px;
  color: var(--text-secondary);
  padding-left: 28px;
}

/* ── 搜索栏 ───────────────────────────────────────── */
.search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.search-input {
  width: 160px;
}

.search-select {
  width: 130px;
}

.search-btn {
  background: var(--cyan, #00D4FF);
  border-color: var(--cyan, #00D4FF);
  color: #0a1628;
  font-weight: 600;
}

.reset-btn {
  background: transparent;
  border-color: var(--border, #1e3a5f);
  color: var(--text-secondary);
}

/* ── 表格 ─────────────────────────────────────────── */
.table-wrap {
  background: var(--bg-panel, #0b1929);
  border: 1px solid var(--border, #1e3a5f);
  border-radius: 6px;
  overflow: hidden;
  padding-bottom: 8px;
}

.indicator-tag {
  font-family: var(--font-data, monospace);
  font-size: 11px;
  color: var(--gold, #FFB400);
  background: rgba(255, 180, 0, 0.1);
  padding: 2px 8px;
  border-radius: 3px;
  border: 1px solid rgba(255, 180, 0, 0.3);
}

.mono-text {
  font-family: var(--font-data, monospace);
  font-size: 11px;
  color: var(--text-secondary);
}

.record-id {
  color: var(--cyan, #00D4FF);
  opacity: 0.8;
}

.status-tag {
  font-family: var(--font-data, monospace);
  font-size: 10px;
  letter-spacing: 0.05em;
}

/* 操作列：描边按钮，避免实心高亮导致文字看不清 */
.op-btn {
  border-radius: 3px;
  font-size: 12px;
  padding: 4px 10px;
  height: 26px;
  border: 1px solid;
  cursor: pointer;
  transition: all 0.15s;
}

:deep(.op-btn.el-button) {
  font-weight: 500;
}

.op-btn-resolve {
  background: rgba(22, 201, 116, 0.08);
  border-color: rgba(22, 201, 116, 0.35);
  color: #16C974;
}

:deep(.op-btn-resolve.el-button) {
  background: rgba(22, 201, 116, 0.08) !important;
  border-color: rgba(22, 201, 116, 0.35) !important;
  color: #16C974 !important;
}

.op-btn-resolve:hover,
:deep(.op-btn-resolve.el-button:hover) {
  background: rgba(22, 201, 116, 0.15) !important;
  border-color: #16C974 !important;
  color: #16C974 !important;
}

.resolved-label {
  color: var(--text-muted);
  font-size: 12px;
}

/* ── 分页 ─────────────────────────────────────────── */
.pagination-wrap {
  padding: 12px 16px 4px;
  display: flex;
  justify-content: flex-end;
}
</style>
