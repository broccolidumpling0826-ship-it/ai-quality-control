<template>
  <div class="ai-evaluation-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">AI 评测中心</h2>
        <p class="page-desc">一键运行 30+ 评测样例，查看通过率与引用命中率</p>
      </div>
    </div>

    <el-card shadow="never" class="run-card">
      <template #header><span style="font-weight:600">运行评测</span></template>
      <el-form :model="runForm" inline>
        <el-form-item label="Prompt 版本">
          <el-input v-model="runForm.promptVersion" placeholder="v1.0.0" style="width:120px" />
        </el-form-item>
        <el-form-item label="评测类别">
          <el-checkbox-group v-model="runForm.categories">
            <el-checkbox value="NORMAL">正常</el-checkbox>
            <el-checkbox value="BOUNDARY">边界</el-checkbox>
            <el-checkbox value="LOW_CONFIDENCE">拒答</el-checkbox>
            <el-checkbox value="INJECTION">注入</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="running" @click="handleRun">一键运行</el-button>
          <el-button v-if="currentRun?.runNo" :loading="polling" @click="refreshRun">刷新报告</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <template v-if="currentRun">
      <el-row :gutter="16" class="metric-row" v-loading="polling">
        <el-col :xs="12" :sm="8" :lg="4">
          <el-card shadow="hover" class="metric-card">
            <p class="metric-label">运行状态</p>
            <el-tag :type="statusTagType(currentRun.status)" size="large">
              {{ statusLabel(currentRun.status) }}
            </el-tag>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="8" :lg="4">
          <el-card shadow="hover" class="metric-card">
            <p class="metric-label">规则通过率</p>
            <p class="metric-value">{{ formatPercent(currentRun.rulePassRate) }}</p>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="8" :lg="4">
          <el-card shadow="hover" class="metric-card">
            <p class="metric-label">AI 准确率</p>
            <p class="metric-value">{{ formatPercent(currentRun.aiAccuracyRate) }}</p>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="8" :lg="4">
          <el-card shadow="hover" class="metric-card">
            <p class="metric-label">引用命中率</p>
            <p class="metric-value">{{ formatPercent(currentRun.citationHitRate) }}</p>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="8" :lg="4">
          <el-card shadow="hover" class="metric-card">
            <p class="metric-label">平均延迟</p>
            <p class="metric-value">{{ currentRun.avgLatencyMs ?? '—' }}<span class="metric-unit">ms</span></p>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="8" :lg="4">
          <el-card shadow="hover" class="metric-card">
            <p class="metric-label">样例进度</p>
            <p class="metric-value">{{ currentRun.passedSamples ?? 0 }}/{{ currentRun.totalSamples ?? 0 }}</p>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区 -->
      <el-row :gutter="16">
        <el-col :xs="24" :lg="12">
          <el-card shadow="never" class="chart-card">
            <template #header><span style="font-weight:600">通过率对比</span></template>
            <div class="bar-chart">
              <div v-for="item in chartMetrics" :key="item.label" class="bar-item">
                <span class="bar-label">{{ item.label }}</span>
                <div class="bar-track">
                  <div class="bar-fill" :style="{ width: item.value + '%', background: item.color }" />
                </div>
                <span class="bar-value">{{ item.value.toFixed(1) }}%</span>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="12">
          <el-card shadow="never" class="chart-card">
            <template #header><span style="font-weight:600">运行信息</span></template>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="运行编号">{{ currentRun.runNo }}</el-descriptions-item>
              <el-descriptions-item label="Prompt 版本">{{ currentRun.promptVersion || '—' }}</el-descriptions-item>
              <el-descriptions-item label="模型">{{ currentRun.modelName || '—' }}</el-descriptions-item>
              <el-descriptions-item label="完成时间">{{ currentRun.finishedTime || '—' }}</el-descriptions-item>
              <el-descriptions-item label="复核命中率">
                {{ formatPercent(currentRun.manualReviewHitRate) }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
      </el-row>

      <!-- 失败样例 -->
      <el-card v-if="failureSamples.length" shadow="never" style="margin-top:16px">
        <template #header><span style="font-weight:600">失败样例明细</span></template>
        <el-table :data="failureSamples" border size="small">
          <el-table-column prop="sampleCode" label="样例编号" width="140" />
          <el-table-column prop="category" label="类别" width="120" />
          <el-table-column prop="reason" label="失败原因" show-overflow-tooltip />
          <el-table-column prop="expected" label="期望" width="160" show-overflow-tooltip />
          <el-table-column prop="actual" label="实际" width="160" show-overflow-tooltip />
        </el-table>
      </el-card>
    </template>

    <el-empty v-else description="点击「一键运行」开始评测" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  runEvaluation,
  getEvaluationRun,
  type EvaluationRunResult,
  type EvaluationCategory
} from '@/api/evaluation'

const running = ref(false)
const polling = ref(false)
const currentRun = ref<EvaluationRunResult | null>(null)
const failureSamples = ref<Array<{ sampleCode?: string; category?: string; reason?: string; expected?: string; actual?: string }>>([])

const runForm = reactive({
  promptVersion: 'v1.0.0',
  categories: ['NORMAL', 'BOUNDARY', 'LOW_CONFIDENCE', 'INJECTION'] as EvaluationCategory[]
})

const chartMetrics = computed(() => {
  if (!currentRun.value) return []
  return [
    { label: '规则通过率', value: toPercentNum(currentRun.value.rulePassRate), color: 'var(--green)' },
    { label: 'AI 准确率', value: toPercentNum(currentRun.value.aiAccuracyRate), color: 'var(--blue)' },
    { label: '引用命中率', value: toPercentNum(currentRun.value.citationHitRate), color: 'var(--cyan)' },
    { label: '复核命中率', value: toPercentNum(currentRun.value.manualReviewHitRate), color: 'var(--orange)' }
  ]
})

function toPercentNum(v?: number) {
  if (v === null || v === undefined) return 0
  return v <= 1 ? v * 100 : v
}

function formatPercent(v?: number) {
  if (v === null || v === undefined) return '—'
  const n = v <= 1 ? v * 100 : v
  return `${n.toFixed(1)}%`
}

function statusLabel(status?: string) {
  const map: Record<string, string> = {
    RUNNING: '运行中',
    COMPLETED: '已完成',
    FAILED: '失败'
  }
  return map[status || ''] || status || '—'
}

function statusTagType(status?: string) {
  const map: Record<string, string> = {
    RUNNING: 'warning',
    COMPLETED: 'success',
    FAILED: 'danger'
  }
  return map[status || ''] || 'info'
}

function parseReportJson(run: EvaluationRunResult) {
  if (!run.reportJson) {
    failureSamples.value = []
    return
  }
  try {
    const report = JSON.parse(run.reportJson)
    failureSamples.value = report.failures || report.failureSamples || []
  } catch {
    failureSamples.value = []
  }
}

async function refreshRun() {
  if (!currentRun.value?.runNo) return
  polling.value = true
  try {
    currentRun.value = await getEvaluationRun(currentRun.value.runNo)
    parseReportJson(currentRun.value)
  } finally {
    polling.value = false
  }
}

async function pollUntilDone(runNo: string) {
  const maxAttempts = 60
  for (let i = 0; i < maxAttempts; i++) {
    await new Promise((r) => setTimeout(r, 3000))
    const run = await getEvaluationRun(runNo)
    currentRun.value = run
    if (run.status === 'COMPLETED' || run.status === 'FAILED') {
      parseReportJson(run)
      return
    }
  }
}

async function handleRun() {
  running.value = true
  try {
    const run = await runEvaluation({
      promptVersion: runForm.promptVersion || undefined,
      categories: runForm.categories
    })
    currentRun.value = run
    ElMessage.success(`评测已启动：${run.runNo}`)
    if (run.status === 'RUNNING') {
      await pollUntilDone(run.runNo)
    } else {
      parseReportJson(run)
    }
  } finally {
    running.value = false
  }
}
</script>

<style scoped>
.ai-evaluation-page {
  padding: 16px;
}

.page-header {
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

.run-card {
  border-radius: 12px;
  margin-bottom: 16px;
}

.metric-row {
  margin-bottom: 16px;
}

.metric-card {
  border-radius: 10px;
  margin-bottom: 12px;
  text-align: center;
}

.metric-card :deep(.el-card__body) {
  padding: 16px 12px;
}

.metric-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.metric-value {
  font-size: 26px;
  font-weight: 800;
  color: var(--text-heading);
  font-family: var(--font-data);
}

.metric-unit {
  font-size: 14px;
  font-weight: 400;
}

.chart-card {
  border-radius: 12px;
  margin-bottom: 16px;
}

.bar-chart {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 8px 0;
}

.bar-item {
  display: grid;
  grid-template-columns: 100px 1fr 50px;
  align-items: center;
  gap: 12px;
}

.bar-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.bar-track {
  height: 12px;
  background: var(--border);
  border-radius: 6px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 6px;
  transition: width 0.5s ease;
}

.bar-value {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-heading);
  text-align: right;
}
</style>
