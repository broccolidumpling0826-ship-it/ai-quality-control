<template>
  <div class="conflicts-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">标准冲突检测</h2>
        <p class="page-desc">多标准同指标限值不一致时的对比与人工裁定</p>
      </div>
    </div>

    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width:140px">
            <el-option label="待裁定" value="PENDING" />
            <el-option label="已裁定-客协优先" value="RESOLVED_CUSTOMER" />
            <el-option label="已裁定-企标优先" value="RESOLVED_ENTERPRISE" />
            <el-option label="已裁定-国标优先" value="RESOLVED_NATIONAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="品种">
          <el-input v-model="searchForm.variety" placeholder="品种" clearable style="width:130px" />
        </el-form-item>
        <el-form-item label="牌号">
          <el-input v-model="searchForm.grade" placeholder="牌号" clearable style="width:130px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20" style="margin-top:12px">
      <el-col :xs="24" :lg="10">
        <el-card shadow="never">
          <el-table
            v-loading="loading"
            :data="tableData"
            border
            stripe
            highlight-current-row
            style="width:100%"
            @row-click="handleRowClick"
          >
            <el-table-column prop="variety" label="品种" width="90" show-overflow-tooltip />
            <el-table-column prop="grade" label="牌号" width="90" show-overflow-tooltip />
            <el-table-column prop="indicatorName" label="指标" min-width="100" show-overflow-tooltip />
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.conflictStatus)" size="small">
                  {{ statusLabel(row.conflictStatus) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, prev, pager, next"
            small
            style="margin-top:12px;justify-content:flex-end"
            @current-change="loadData"
            @size-change="handleSearch"
          />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="14">
        <el-card v-loading="detailLoading" shadow="never" class="detail-card">
          <template v-if="currentDetail">
            <template #header>
              <div class="detail-header">
                <span style="font-weight:600">冲突对比</span>
                <el-tag v-if="currentDetail.demoFlag === 1" type="info" size="small">演示样例</el-tag>
              </div>
            </template>

            <el-descriptions :column="2" border size="small" style="margin-bottom:16px">
              <el-descriptions-item label="品种">{{ currentDetail.variety }}</el-descriptions-item>
              <el-descriptions-item label="牌号">{{ currentDetail.grade }}</el-descriptions-item>
              <el-descriptions-item label="冲突指标" :span="2">
                {{ currentDetail.indicatorName || currentDetail.indicatorId }}
              </el-descriptions-item>
            </el-descriptions>

            <div class="compare-grid">
              <div class="compare-col compare-a">
                <h4>{{ currentDetail.standardNameA || '标准 A' }}</h4>
                <p>上限：<strong>{{ formatLimit(currentDetail.limitAUpper) }}</strong></p>
                <p>下限：<strong>{{ formatLimit(currentDetail.limitALower) }}</strong></p>
              </div>
              <div class="compare-vs">VS</div>
              <div class="compare-col compare-b">
                <h4>{{ currentDetail.standardNameB || '标准 B' }}</h4>
                <p>上限：<strong>{{ formatLimit(currentDetail.limitBUpper) }}</strong></p>
                <p>下限：<strong>{{ formatLimit(currentDetail.limitBLower) }}</strong></p>
              </div>
            </div>

            <div v-if="currentDetail.citations?.length" class="citation-block">
              <p class="block-title">来源引用</p>
              <div
                v-for="(c, i) in currentDetail.citations"
                :key="i"
                class="citation-item"
              >
                <span class="citation-ref">{{ c.standardName }} · {{ c.sectionRef }}</span>
                <p class="citation-text">{{ c.highlightText }}</p>
              </div>
            </div>

            <template v-if="currentDetail.conflictStatus === 'PENDING'">
              <el-divider />
              <el-form ref="resolveFormRef" :model="resolveForm" :rules="resolveRules" label-width="90px">
                <el-form-item label="裁定结论" prop="conflictStatus">
                  <el-radio-group v-model="resolveForm.conflictStatus">
                    <el-radio value="RESOLVED_CUSTOMER">以客协为准</el-radio>
                    <el-radio value="RESOLVED_ENTERPRISE">以企标为准</el-radio>
                    <el-radio value="RESOLVED_NATIONAL">以国标为准</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="裁定说明" prop="resolutionNote">
                  <el-input v-model="resolveForm.resolutionNote" type="textarea" :rows="2" placeholder="请输入裁定说明" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="resolveLoading" @click="submitResolve">提交裁定</el-button>
                </el-form-item>
              </el-form>
            </template>
            <el-alert
              v-else
              type="success"
              :closable="false"
              show-icon
              :title="`已裁定：${statusLabel(currentDetail.conflictStatus)}`"
              :description="currentDetail.resolutionNote || '—'"
            />
          </template>
          <el-empty v-else description="请选择左侧冲突记录查看详情" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import {
  pageStandardConflicts,
  getStandardConflict,
  resolveStandardConflict,
  type StandardConflictDetail,
  type StandardConflictItem
} from '@/api/standard-conflict'

const searchForm = reactive({ status: 'PENDING', variety: '', grade: '' })
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const tableData = ref<StandardConflictItem[]>([])

const currentDetail = ref<StandardConflictDetail | null>(null)
const detailLoading = ref(false)
const resolveLoading = ref(false)
const resolveFormRef = ref<FormInstance>()
const resolveForm = reactive({
  conflictStatus: 'RESOLVED_CUSTOMER',
  resolutionNote: ''
})
const resolveRules = {
  conflictStatus: [{ required: true, message: '请选择裁定结论', trigger: 'change' }],
  resolutionNote: [{ required: true, message: '请输入裁定说明', trigger: 'blur' }]
}

function formatLimit(v?: number | null) {
  return v === null || v === undefined ? '—' : String(v)
}

function statusLabel(status?: string) {
  const map: Record<string, string> = {
    PENDING: '待裁定',
    RESOLVED_CUSTOMER: '客协优先',
    RESOLVED_ENTERPRISE: '企标优先',
    RESOLVED_NATIONAL: '国标优先'
  }
  return map[status || ''] || status || '—'
}

function statusTagType(status?: string) {
  if (status === 'PENDING') return 'warning'
  if (status?.startsWith('RESOLVED')) return 'success'
  return 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await pageStandardConflicts({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: searchForm.status || undefined,
      variety: searchForm.variety || undefined,
      grade: searchForm.grade || undefined
    })
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
  Object.assign(searchForm, { status: 'PENDING', variety: '', grade: '' })
  pageNum.value = 1
  currentDetail.value = null
  loadData()
}

async function handleRowClick(row: StandardConflictItem) {
  detailLoading.value = true
  try {
    currentDetail.value = await getStandardConflict(row.id)
    resolveForm.conflictStatus = 'RESOLVED_CUSTOMER'
    resolveForm.resolutionNote = ''
  } finally {
    detailLoading.value = false
  }
}

async function submitResolve() {
  if (!currentDetail.value) return
  await resolveFormRef.value?.validate()
  resolveLoading.value = true
  try {
    await resolveStandardConflict(currentDetail.value.id, {
      conflictStatus: resolveForm.conflictStatus,
      resolutionNote: resolveForm.resolutionNote
    })
    ElMessage.success('冲突裁定已提交')
    await loadData()
    currentDetail.value = await getStandardConflict(currentDetail.value.id)
  } finally {
    resolveLoading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.conflicts-page {
  padding: 16px;
}

.page-header {
  margin-bottom: 16px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-heading);
  margin-bottom: 4px;
}

.page-desc {
  font-size: 14px;
  color: var(--text-secondary);
}

.search-card :deep(.el-card__body) {
  padding: 16px 16px 0;
}

.detail-card {
  min-height: 480px;
  border-radius: 12px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.compare-grid {
  display: flex;
  align-items: stretch;
  gap: 12px;
  margin-bottom: 16px;
}

.compare-col {
  flex: 1;
  padding: 16px;
  border-radius: 10px;
  border: 1px solid var(--border);
}

.compare-a {
  background: rgba(61, 158, 255, 0.08);
  border-color: rgba(61, 158, 255, 0.3);
}

.compare-b {
  background: rgba(255, 140, 0, 0.08);
  border-color: rgba(255, 140, 0, 0.3);
}

.compare-col h4 {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 10px;
  color: var(--text-heading);
}

.compare-col p {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.compare-vs {
  display: flex;
  align-items: center;
  font-weight: 700;
  color: var(--red);
  font-size: 16px;
}

.citation-block {
  margin-top: 12px;
}

.block-title {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 8px;
  color: var(--text-heading);
}

.citation-item {
  padding: 10px;
  border: 1px solid var(--border);
  border-radius: 8px;
  margin-bottom: 8px;
}

.citation-ref {
  font-size: 12px;
  color: var(--cyan);
}

.citation-text {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 4px;
  line-height: 1.5;
}
</style>
