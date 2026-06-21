<template>
  <div class="ai-assessments-page">
    <el-card class="filter-card" shadow="never">
      <el-form :model="query" inline>
        <el-form-item label="类型">
          <el-select v-model="query.assessmentType" clearable style="width: 180px">
            <el-option label="判定解释" value="JUDGMENT_EXPLANATION" />
            <el-option label="让步风险" value="CONCESSION_RISK" />
            <el-option label="质保书问答" value="CERT_QA" />
            <el-option label="复检建议" value="REINSPECTION_ADVICE" />
            <el-option label="改判建议" value="REJUDGMENT_ADVICE" />
          </el-select>
        </el-form-item>
        <el-form-item label="采纳状态">
          <el-select v-model="query.adoptionStatus" clearable style="width: 130px">
            <el-option label="待处理" value="PENDING" />
            <el-option label="已采纳" value="ADOPTED" />
            <el-option label="已忽略" value="IGNORED" />
          </el-select>
        </el-form-item>
        <el-form-item label="置信度">
          <el-select v-model="query.confidenceLabel" clearable style="width: 120px">
            <el-option label="高" value="HIGH" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="低" value="LOW" />
          </el-select>
        </el-form-item>
        <el-form-item label="风险">
          <el-select v-model="query.riskLevel" clearable style="width: 140px">
            <el-option label="高风险" value="HIGH" />
            <el-option label="中风险" value="MEDIUM" />
            <el-option label="低风险" value="LOW" />
            <el-option label="阻断" value="BLOCKED" />
            <el-option label="人工复核" value="MANUAL_REVIEW" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务ID">
          <el-input v-model="query.businessId" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
          <el-button :icon="Refresh" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" border stripe>
        <el-table-column prop="assessmentType" label="类型" min-width="160" show-overflow-tooltip />
        <el-table-column prop="businessType" label="业务对象" width="120" />
        <el-table-column prop="businessId" label="业务ID" min-width="180" show-overflow-tooltip />
        <el-table-column prop="riskLevel" label="风险" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.riskLevel" :type="riskTagType(row.riskLevel)" size="small">
              {{ row.riskLevel }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="confidenceLabel" label="置信度" width="110">
          <template #default="{ row }">
            <el-tag :type="confidenceTagType(row.confidenceLabel)" size="small">
              {{ row.confidenceLabel || 'UNKNOWN' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="degradationSource" label="降级来源" width="140" />
        <el-table-column prop="adoptionStatus" label="处理状态" width="120">
          <template #default="{ row }">
            <el-tag :type="adoptionTagType(row.adoptionStatus)" size="small">
              {{ row.adoptionStatus || 'PENDING' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createDateTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="110" fixed="right">
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

    <el-drawer v-model="detailVisible" title="AI评估审计详情" size="720px">
      <div v-loading="detailLoading" class="detail-body" v-if="detail">
        <div class="detail-head">
          <div>
            <div class="mono">{{ detail.id }}</div>
            <div class="detail-sub">{{ detail.assessmentType }} / {{ detail.businessType }}</div>
          </div>
          <div class="detail-tags">
            <el-tag :type="confidenceTagType(detail.confidenceLabel)" size="small">
              {{ detail.confidenceLabel || 'UNKNOWN' }}
            </el-tag>
            <el-tag :type="adoptionTagType(detail.adoptionStatus)" size="small">
              {{ detail.adoptionStatus || 'PENDING' }}
            </el-tag>
            <el-tag v-if="detail.cacheHit === 1" type="warning" size="small">CACHE</el-tag>
          </div>
        </div>

        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="业务ID">{{ detail.businessId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="关联判定">{{ detail.relatedJudgmentId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="风险等级">{{ detail.riskLevel || '-' }}</el-descriptions-item>
          <el-descriptions-item label="置信分">{{ detail.confidenceScore ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="模型">{{ detail.modelProvider || '-' }} / {{ detail.modelName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="提示词版本">{{ detail.promptVersion || '-' }}</el-descriptions-item>
          <el-descriptions-item label="降级来源">{{ detail.degradationSource || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detail.createDateTime || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-tabs class="detail-tabs">
          <el-tab-pane label="结构化输出">
            <pre class="json-box">{{ formatJson(detail.structuredOutput) }}</pre>
          </el-tab-pane>
          <el-tab-pane label="引用来源">
            <pre class="json-box">{{ formatJson(detail.referencesJson) }}</pre>
          </el-tab-pane>
          <el-tab-pane label="输入快照">
            <pre class="json-box">{{ formatJson(detail.inputSnapshot) }}</pre>
          </el-tab-pane>
          <el-tab-pane label="原始输出">
            <pre class="json-box">{{ detail.rawOutput || '-' }}</pre>
          </el-tab-pane>
          <el-tab-pane label="置信因素">
            <pre class="json-box">{{ formatJson(detail.confidenceFactors) }}</pre>
          </el-tab-pane>
        </el-tabs>

        <div class="handle-panel">
          <div class="handle-title">人工处理</div>
          <el-input
            v-model="handleOpinion"
            type="textarea"
            :rows="3"
            maxlength="1000"
            show-word-limit
            placeholder="记录采纳或忽略原因"
          />
          <div class="handle-actions">
            <el-button type="success" :icon="Check" :loading="handling" @click="submitHandle('ADOPTED')">
              采纳
            </el-button>
            <el-button type="danger" :icon="Close" :loading="handling" @click="submitHandle('IGNORED')">
              忽略
            </el-button>
          </div>
          <div v-if="detail.humanOpinion" class="handled-note">
            {{ detail.handledBy || '-' }}：{{ detail.humanOpinion }}
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, Close, Refresh, Search, View } from '@element-plus/icons-vue'
import {
  getAiAssessment,
  handleAiAssessment,
  pageAiAssessments,
  type AiAssessment,
  type AiAssessmentPageQuery
} from '@/api/ai-assessment'

const loading = ref(false)
const detailLoading = ref(false)
const handling = ref(false)
const detailVisible = ref(false)
const rows = ref<AiAssessment[]>([])
const total = ref(0)
const detail = ref<AiAssessment | null>(null)
const handleOpinion = ref('')
const query = reactive<AiAssessmentPageQuery>({ pageNum: 1, pageSize: 10, adoptionStatus: 'PENDING' })

async function loadData() {
  loading.value = true
  try {
    const page = await pageAiAssessments({ ...query })
    rows.value = page.records || []
    total.value = page.total || 0
  } finally {
    loading.value = false
  }
}

function reset() {
  query.assessmentType = ''
  query.businessType = ''
  query.businessId = ''
  query.relatedJudgmentId = ''
  query.riskLevel = ''
  query.confidenceLabel = ''
  query.degradationSource = ''
  query.adoptionStatus = 'PENDING'
  query.pageNum = 1
  loadData()
}

async function openDetail(id: string) {
  detailVisible.value = true
  detailLoading.value = true
  handleOpinion.value = ''
  try {
    detail.value = await getAiAssessment(id)
  } finally {
    detailLoading.value = false
  }
}

async function submitHandle(status: 'ADOPTED' | 'IGNORED') {
  if (!detail.value) return
  handling.value = true
  try {
    detail.value = await handleAiAssessment(detail.value.id, {
      adoptionStatus: status,
      humanOpinion: handleOpinion.value.trim()
    })
    ElMessage.success(status === 'ADOPTED' ? '已采纳AI评估' : '已忽略AI评估')
    loadData()
  } finally {
    handling.value = false
  }
}

function formatJson(value?: string) {
  if (!value) return '-'
  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch {
    return value
  }
}

function confidenceTagType(value?: string): '' | 'success' | 'warning' | 'danger' | 'info' {
  if (value === 'HIGH') return 'success'
  if (value === 'MEDIUM') return 'warning'
  if (value === 'LOW') return 'danger'
  return 'info'
}

function riskTagType(value?: string): '' | 'success' | 'warning' | 'danger' | 'info' {
  if (value === 'HIGH' || value === 'BLOCKED' || value === 'MANUAL_REVIEW') return 'danger'
  if (value === 'MEDIUM') return 'warning'
  if (value === 'LOW') return 'success'
  return 'info'
}

function adoptionTagType(value?: string): '' | 'success' | 'warning' | 'danger' | 'info' {
  if (value === 'ADOPTED') return 'success'
  if (value === 'IGNORED') return 'info'
  return 'warning'
}

onMounted(loadData)
</script>

<style scoped>
.ai-assessments-page {
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

.detail-body {
  color: var(--text-regular);
}

.detail-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.detail-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.detail-sub {
  margin-top: 4px;
  color: var(--text-secondary);
  font-size: 12px;
}

.mono,
.json-box {
  font-family: var(--font-data);
}

.detail-tabs {
  margin-top: 12px;
}

.json-box {
  margin: 0;
  padding: 12px;
  min-height: 120px;
  max-height: 320px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
  color: var(--text-regular);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 4px;
}

.handle-panel {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid var(--border-color);
}

.handle-title {
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-heading);
}

.handle-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 10px;
}

.handled-note {
  margin-top: 10px;
  padding: 8px 10px;
  color: var(--text-secondary);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  line-height: 1.5;
}
</style>
