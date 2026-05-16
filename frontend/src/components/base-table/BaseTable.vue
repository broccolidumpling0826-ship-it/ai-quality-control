<template>
  <div class="base-table-wrapper">
    <!-- 表头工具栏（支持插槽扩展） -->
    <div v-if="$slots.toolbar || $slots.search" class="table-toolbar">
      <div class="toolbar-search">
        <slot name="search" />
      </div>
      <div class="toolbar-actions">
        <slot name="toolbar" />
      </div>
    </div>

    <!-- 数据表格 -->
    <el-table
      v-loading="loading"
      :data="data"
      :border="border"
      :stripe="stripe"
      :size="size"
      :height="height"
      :max-height="maxHeight"
      :row-key="rowKey"
      :empty-text="emptyText"
      highlight-current-row
      style="width: 100%"
      v-bind="$attrs"
      @selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
      @row-click="handleRowClick"
    >
      <!-- 多选列 -->
      <el-table-column v-if="selectable" type="selection" width="50" align="center" fixed="left" />

      <!-- 序号列 -->
      <el-table-column v-if="showIndex" type="index" label="序号" width="60" align="center" fixed="left" />

      <!-- 动态列渲染 -->
      <template v-for="col in columns" :key="col.prop">
        <!-- 插槽列：交给父组件自定义渲染 -->
        <el-table-column
          v-if="col.type === 'slot'"
          :prop="col.prop"
          :label="col.label"
          :width="col.width"
          :min-width="col.minWidth"
          :fixed="col.fixed"
          :align="col.align || 'center'"
          :sortable="col.sortable"
          :show-overflow-tooltip="col.showOverflowTooltip !== false"
        >
          <template #default="scope">
            <slot :name="col.prop" :row="scope.row" :$index="scope.$index" />
          </template>
          <template v-if="col.headerSearch" #header>
            <div class="header-search">
              <span>{{ col.label }}</span>
              <el-input
                v-model="headerFilters[col.prop]"
                size="small"
                clearable
                placeholder="搜索..."
                style="margin-top: 4px"
                @input="handleHeaderFilter"
                @click.stop
              />
            </div>
          </template>
        </el-table-column>

        <!-- 字典翻译列（tag 样式）-->
        <el-table-column
          v-else-if="col.type === 'tag'"
          :prop="col.prop"
          :label="col.label"
          :width="col.width"
          :min-width="col.minWidth"
          :fixed="col.fixed"
          :align="col.align || 'center'"
          :sortable="col.sortable"
        >
          <template #default="scope">
            <el-tag
              v-if="getCellValue(scope.row, col.prop) !== null && getCellValue(scope.row, col.prop) !== undefined && getCellValue(scope.row, col.prop) !== ''"
              :type="(dictStore.getColorTag(col.dictCode || '', getCellValue(scope.row, col.prop)) as any)"
              size="small"
              effect="light"
            >
              {{ dictStore.getLabel(col.dictCode || '', getCellValue(scope.row, col.prop)) }}
            </el-tag>
            <span v-else class="cell-empty">-</span>
          </template>
          <template v-if="col.headerSearch" #header>
            <div class="header-search">
              <span>{{ col.label }}</span>
              <el-input
                v-model="headerFilters[col.prop]"
                size="small"
                clearable
                placeholder="搜索..."
                style="margin-top: 4px"
                @input="handleHeaderFilter"
                @click.stop
              />
            </div>
          </template>
        </el-table-column>

        <!-- 字典翻译列（纯文本）-->
        <el-table-column
          v-else-if="col.type === 'dict'"
          :prop="col.prop"
          :label="col.label"
          :width="col.width"
          :min-width="col.minWidth"
          :fixed="col.fixed"
          :align="col.align || 'left'"
          :sortable="col.sortable"
          :show-overflow-tooltip="col.showOverflowTooltip !== false"
        >
          <template #default="scope">
            {{ dictStore.getLabel(col.dictCode || '', getCellValue(scope.row, col.prop)) }}
          </template>
          <template v-if="col.headerSearch" #header>
            <div class="header-search">
              <span>{{ col.label }}</span>
              <el-input
                v-model="headerFilters[col.prop]"
                size="small"
                clearable
                placeholder="搜索..."
                style="margin-top: 4px"
                @input="handleHeaderFilter"
                @click.stop
              />
            </div>
          </template>
        </el-table-column>

        <!-- 日期格式化列 -->
        <el-table-column
          v-else-if="col.type === 'date'"
          :prop="col.prop"
          :label="col.label"
          :width="col.width || 160"
          :min-width="col.minWidth"
          :fixed="col.fixed"
          :align="col.align || 'center'"
          :sortable="col.sortable"
        >
          <template #default="scope">
            {{ formatDate(getCellValue(scope.row, col.prop), col.dateFormat) }}
          </template>
        </el-table-column>

        <!-- 普通文本列 -->
        <el-table-column
          v-else
          :prop="col.prop"
          :label="col.label"
          :width="col.width"
          :min-width="col.minWidth"
          :fixed="col.fixed"
          :align="col.align || 'left'"
          :sortable="col.sortable"
          :show-overflow-tooltip="col.showOverflowTooltip !== false"
        >
          <template #default="scope">
            <span>{{ getCellValue(scope.row, col.prop) ?? '-' }}</span>
          </template>
          <template v-if="col.headerSearch" #header>
            <div class="header-search">
              <span>{{ col.label }}</span>
              <el-input
                v-model="headerFilters[col.prop]"
                size="small"
                clearable
                placeholder="搜索..."
                style="margin-top: 4px"
                @input="handleHeaderFilter"
                @click.stop
              />
            </div>
          </template>
        </el-table-column>
      </template>

      <!-- 操作列插槽 -->
      <el-table-column
        v-if="$slots.action"
        label="操作"
        :width="actionWidth"
        :fixed="actionFixed"
        align="center"
      >
        <template #default="scope">
          <slot name="action" :row="scope.row" :$index="scope.$index" />
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="currentPageModel"
        v-model:page-size="pageSizeModel"
        :total="total"
        :page-sizes="pageSizes"
        :layout="paginationLayout"
        background
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { useDictStore } from '@/store/dict'

// ─── Column 类型定义 ─────────────────────────────────────

export interface TableColumn {
  prop: string
  label: string
  width?: number | string
  minWidth?: number | string
  type?: 'text' | 'dict' | 'tag' | 'date' | 'slot'
  dictCode?: string
  dateFormat?: string
  fixed?: 'left' | 'right' | boolean
  align?: 'left' | 'center' | 'right'
  sortable?: boolean | 'custom'
  showOverflowTooltip?: boolean
  headerSearch?: boolean
}

// ─── Props ────────────────────────────────────────────────

const props = withDefaults(defineProps<{
  columns: TableColumn[]
  data: Record<string, unknown>[]
  total?: number
  pageNum?: number
  pageSize?: number
  loading?: boolean
  border?: boolean
  stripe?: boolean
  size?: 'large' | 'default' | 'small'
  height?: number | string
  maxHeight?: number | string
  rowKey?: string
  selectable?: boolean
  showIndex?: boolean
  emptyText?: string
  actionWidth?: number | string
  actionFixed?: 'left' | 'right'
  pageSizes?: number[]
  paginationLayout?: string
}>(), {
  total: 0,
  pageNum: 1,
  pageSize: 20,
  loading: false,
  border: true,
  stripe: true,
  size: 'default',
  rowKey: 'id',
  selectable: false,
  showIndex: false,
  emptyText: '暂无数据',
  actionWidth: 160,
  actionFixed: 'right',
  pageSizes: () => [10, 20, 50, 100],
  paginationLayout: 'total, sizes, prev, pager, next, jumper'
})

// ─── Emits ────────────────────────────────────────────────

const emit = defineEmits<{
  'update:pageNum': [page: number]
  'update:pageSize': [size: number]
  'page-change': [page: number, size: number]
  'selection-change': [rows: Record<string, unknown>[]]
  'sort-change': [sort: { prop: string; order: string | null }]
  'row-click': [row: Record<string, unknown>]
  'header-filter': [filters: Record<string, string>]
}>()

// ─── 内部状态 ─────────────────────────────────────────────

const dictStore = useDictStore()

const currentPageModel = ref(props.pageNum)
const pageSizeModel = ref(props.pageSize)
const headerFilters = reactive<Record<string, string>>({})

watch(() => props.pageNum, (v) => { currentPageModel.value = v })
watch(() => props.pageSize, (v) => { pageSizeModel.value = v })

// ─── 工具函数 ─────────────────────────────────────────────

function getCellValue(row: Record<string, unknown>, prop: string): unknown {
  // 支持嵌套路径：如 'user.name'
  return prop.split('.').reduce<unknown>((obj, key) => {
    if (obj && typeof obj === 'object') {
      return (obj as Record<string, unknown>)[key]
    }
    return undefined
  }, row)
}

function formatDate(val: unknown, fmt?: string): string {
  if (!val) return '-'
  try {
    const d = new Date(val as string | number)
    if (isNaN(d.getTime())) return String(val)
    const format = fmt || 'YYYY-MM-DD HH:mm:ss'
    const pad = (n: number) => String(n).padStart(2, '0')
    return format
      .replace('YYYY', String(d.getFullYear()))
      .replace('MM', pad(d.getMonth() + 1))
      .replace('DD', pad(d.getDate()))
      .replace('HH', pad(d.getHours()))
      .replace('mm', pad(d.getMinutes()))
      .replace('ss', pad(d.getSeconds()))
  } catch {
    return String(val)
  }
}

// ─── 事件处理 ─────────────────────────────────────────────

function handleSizeChange(size: number) {
  pageSizeModel.value = size
  currentPageModel.value = 1
  emit('update:pageSize', size)
  emit('update:pageNum', 1)
  emit('page-change', 1, size)
}

function handleCurrentChange(page: number) {
  currentPageModel.value = page
  emit('update:pageNum', page)
  emit('page-change', page, pageSizeModel.value)
}

function handleSelectionChange(rows: Record<string, unknown>[]) {
  emit('selection-change', rows)
}

function handleSortChange(sort: { prop: string; order: string | null }) {
  emit('sort-change', sort)
}

function handleRowClick(row: Record<string, unknown>) {
  emit('row-click', row)
}

function handleHeaderFilter() {
  emit('header-filter', { ...headerFilters })
}
</script>

<style scoped>
.base-table-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.table-toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar-search {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  flex: 1;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.header-search {
  display: flex;
  flex-direction: column;
  padding: 2px 0;
}

.cell-empty {
  color: var(--text-muted);
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 8px 0 0;
}

:deep(.el-table th.el-table__cell) {
  background-color: #fafafa;
  color: var(--text-primary);
  font-weight: 600;
}

:deep(.el-table .el-table__row:hover > td) {
  background-color: #f0f7ff !important;
}
</style>
