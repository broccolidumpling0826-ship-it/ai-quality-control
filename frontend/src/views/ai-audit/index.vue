<template>
  <div class="ai-audit-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">AI 调用审计</h2>
        <p class="page-desc">汇总 AI 调用统计、明细日志与调用链回放</p>
      </div>
      <el-button type="primary" plain :icon="Refresh" :loading="dashboardLoading" @click="loadDashboard">
        刷新
      </el-button>
    </div>

    <!-- 汇总卡片 -->
    <el-row :gutter="16" class="stat-row" v-loading="dashboardLoading">
      <el-col :xs="12" :sm="8" :lg="4">
        <el-card shadow="hover" class="stat-card">
          <p class="stat-label">调用次数</p>
          <p class="stat-value">{{ dashboard.callCount ?? 0 }}</p>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :lg="4">
        <el-card shadow="hover" class="stat-card">
          <p class="stat-label">总 Token</p>
          <p class="stat-value">{{ dashboard.totalTokens ?? 0 }}</p>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :lg="4">
        <el-card shadow="hover" class="stat-card">
          <p class="stat-label">平均延迟</p>
          <p class="stat-value">{{ dashboard.avgLatencyMs ?? 0 }}<span class="stat-unit">ms</span></p>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :lg="4">
        <el-card shadow="hover" class="stat-card stat-card--danger">
          <p class="stat-label">错误率</p>
          <p class="stat-value">{{ formatRate(dashboard.errorRate) }}</p>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :lg="4">
        <el-card shadow="hover" class="stat-card stat-card--warning">
          <p class="stat-label">降级次数</p>
          <p class="stat-value">{{ dashboard.degradedCount ?? 0 }}</p>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="16" :lg="4">
        <el-card shadow="hover" class="stat-card">
          <p class="stat-label">缓存更新</p>
          <p class="stat-meta">{{ dashboard.cacheUpdatedAt || '—' }}</p>
        </el-card>
      </el-col>
    </el-row>

    <!-- 来源分布 -->
    <el-card v-if="sourceEntries.length" shadow="never" class="source-card">
      <template #header><span style="font-weight:600">按调用来源分布</span></template>
      <div class="source-grid">
        <div v-for="[source, count] in sourceEntries" :key="source" class="source-item">
          <span class="source-name">{{ sourceLabel(source) }}</span>
          <el-progress :percentage="sourcePercent(count)" :stroke-width="10" />
          <span class="source-count">{{ count }}</span>
        </div>
      </div>
    </el-card>

    <!-- 日志列表 -->
    <el-card shadow="never" style="margin-top:16px">
      <template #header>
        <div class="card-header">
          <span style="font-weight:600">调用明细</span>
          <div class="header-actions">
            <el-select v-model="logFilter.callSource" placeholder="来源" clearable size="small" style="width:130px" @change="handleLogSearch">
              <el-option label="RAG 问答" value="RAG_QUERY" />
              <el-option label="判定解释" value="JUDGMENT_EXPLAIN" />
              <el-option label="让步评估" value="CONCESSION_ASSESS" />
              <el-option label="质保书摘要" value="CERT_SUMMARY" />
              <el-option label="复检建议" value="REINSPECTION_ADVICE" />
            </el-select>
            <el-checkbox v-model="logFilter.degradedOnly" @change="handleLogSearch">仅降级</el-checkbox>
          </div>
        </div>
      </template>

      <el-table
        v-loading="logsLoading"
        :data="logList"
        border
        stripe
        style="width:100%"
        @row-click="openReplay"
      >
        <el-table-column prop="callSource" label="来源" width="120">
          <template #default="{ row }">{{ sourceLabel(row.callSource) }}</template>
        </el-table-column>
        <el-table-column prop="promptVersion" label="Prompt" width="90" />
        <el-table-column prop="modelName" label="模型" width="110" show-overflow-tooltip />
        <el-table-column prop="latencyMs" label="延迟(ms)" width="90" align="center" />
        <el-table-column prop="totalTokens" label="Token" width="80" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.degraded" type="warning" size="small">降级</el-tag>
            <el-tag v-else-if="row.success" type="success" size="small">成功</el-tag>
            <el-tag v-else type="danger" size="small">{{ row.errorType || '失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createDateTime" label="时间" width="160" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click.stop="openReplay(row)">回放</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="logPageNum"
        v-model:page-size="logPageSize"
        :total="logTotal"
        :page-sizes="[10, 20, 50]"
        layout="total, prev, pager, next"
        small
        style="margin-top:12px;justify-content:flex-end"
        @current-change="loadLogs"
        @size-change="handleLogSearch"
      />
    </el-card>

    <!-- 回放抽屉 -->
    <el-drawer v-model="replayVisible" title="调用链回放" size="520px" destroy-on-close>
      <div v-loading="replayLoading">
        <template v-if="replay">
          <el-descriptions :column="1" border size="small" style="margin-bottom:16px">
            <el-descriptions-item label="Prompt">{{ replay.promptKey }} @ {{ replay.promptVersion }}</el-descriptions-item>
            <el-descriptions-item label="模型">{{ replay.modelName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="延迟">{{ replay.latencyMs ?? '—' }} ms</el-descriptions-item>
            <el-descriptions-item label="Token">{{ replay.totalTokens ?? '—' }}</el-descriptions-item>
            <el-descriptions-item label="降级">
              <el-tag :type="replay.degraded ? 'warning' : 'success'" size="small">
                {{ replay.degraded ? '是' : '否' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item v-if="replay.errorType" label="错误类型">{{ replay.errorType }}</el-descriptions-item>
          </el-descriptions>

          <div class="replay-block">
            <p class="replay-label">输入摘要（已脱敏）</p>
            <pre class="replay-content">{{ replay.inputSummary || '—' }}</pre>
          </div>
          <div class="replay-block">
            <p class="replay-label">输出摘要（已脱敏）</p>
            <pre class="replay-content">{{ replay.outputSummary || '—' }}</pre>
          </div>
          <div v-if="replay.citations?.length" class="replay-block">
            <p class="replay-label">引用</p>
            <div v-for="(c, i) in replay.citations" :key="i" class="citation-item">
              <span class="citation-ref">{{ c.standardName }} · {{ c.sectionRef }}</span>
              <p>{{ c.highlightText }}</p>
            </div>
          </div>
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Refresh } from '@element-plus/icons-vue'
import {
  getAiAuditDashboard,
  pageAiAuditLogs,
  replayAiAuditLog,
  type AiAuditDashboard,
  type AiAuditLogItem,
  type AiAuditReplay
} from '@/api/ai-audit'

const route = useRoute()

const dashboardLoading = ref(false)
const dashboard = ref<AiAuditDashboard>({})

const logsLoading = ref(false)
const logList = ref<AiAuditLogItem[]>([])
const logPageNum = ref(1)
const logPageSize = ref(20)
const logTotal = ref(0)
const logFilter = reactive({ callSource: '', degradedOnly: false })

const replayVisible = ref(false)
const replayLoading = ref(false)
const replay = ref<AiAuditReplay | null>(null)

const sourceEntries = computed(() => {
  const bySource = dashboard.value.bySource || {}
  return Object.entries(bySource).sort((a, b) => b[1] - a[1])
})

const totalSourceCount = computed(() =>
  sourceEntries.value.reduce((sum, [, c]) => sum + c, 0) || 1
)

function formatRate(rate?: number) {
  if (rate === null || rate === undefined) return '0%'
  return `${(rate * 100).toFixed(1)}%`
}

function sourcePercent(count: number) {
  return Math.round((count / totalSourceCount.value) * 100)
}

function sourceLabel(source?: string) {
  const map: Record<string, string> = {
    RAG_QUERY: 'RAG 问答',
    JUDGMENT_EXPLAIN: '判定解释',
    CONCESSION_ASSESS: '让步评估',
    CERT_SUMMARY: '质保书摘要',
    REINSPECTION_ADVICE: '复检建议',
    REJUDGMENT_ADVICE: '改判建议'
  }
  return map[source || ''] || source || '—'
}

async function loadDashboard() {
  dashboardLoading.value = true
  try {
    dashboard.value = await getAiAuditDashboard()
  } finally {
    dashboardLoading.value = false
  }
}

async function loadLogs() {
  logsLoading.value = true
  try {
    const res = await pageAiAuditLogs({
      pageNum: logPageNum.value,
      pageSize: logPageSize.value,
      callSource: logFilter.callSource || undefined,
      degraded: logFilter.degradedOnly ? true : undefined
    })
    logList.value = res.records || []
    logTotal.value = res.total || 0
  } finally {
    logsLoading.value = false
  }
}

function handleLogSearch() {
  logPageNum.value = 1
  loadLogs()
}

async function openReplay(row: AiAuditLogItem) {
  replayVisible.value = true
  replayLoading.value = true
  replay.value = null
  try {
    replay.value = await replayAiAuditLog(row.id)
  } finally {
    replayLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadDashboard(), loadLogs()])
  const auditLogId = route.query.auditLogId as string
  if (auditLogId) {
    replayVisible.value = true
    replayLoading.value = true
    try {
      replay.value = await replayAiAuditLog(auditLogId)
    } finally {
      replayLoading.value = false
    }
  }
})
</script>

<style scoped>
.ai-audit-page {
  padding: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 20px;
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

.stat-row {
  margin-bottom: 16px;
}

.stat-card {
  border-radius: 10px;
  margin-bottom: 12px;
  text-align: center;
}

.stat-card :deep(.el-card__body) {
  padding: 16px 12px;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.stat-value {
  font-size: 28px;
  font-weight: 800;
  color: var(--text-heading);
  font-family: var(--font-data);
}

.stat-card--danger .stat-value { color: var(--red); }
.stat-card--warning .stat-value { color: var(--orange); }

.stat-unit {
  font-size: 14px;
  font-weight: 400;
  margin-left: 2px;
}

.stat-meta {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 8px;
}

.source-card {
  border-radius: 12px;
}

.source-grid {
  display: grid;
  gap: 12px;
}

.source-item {
  display: grid;
  grid-template-columns: 120px 1fr 40px;
  align-items: center;
  gap: 12px;
}

.source-name {
  font-size: 13px;
  color: var(--text-secondary);
}

.source-count {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-heading);
  text-align: right;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.replay-block {
  margin-bottom: 16px;
}

.replay-label {
  font-weight: 600;
  font-size: 13px;
  color: var(--text-heading);
  margin-bottom: 6px;
}

.replay-content {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 10px;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  color: var(--text-secondary);
  max-height: 200px;
  overflow-y: auto;
}

.citation-item {
  padding: 8px;
  border: 1px solid var(--border);
  border-radius: 6px;
  margin-bottom: 6px;
  font-size: 13px;
}

.citation-ref {
  color: var(--cyan);
  font-size: 12px;
}
</style>
