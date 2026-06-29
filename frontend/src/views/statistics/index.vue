<template>
  <div class="statistics-page">
    <!-- 时间范围选择 -->
    <el-card shadow="never" style="margin-bottom:12px">
      <el-form inline>
        <el-form-item label="统计时间范围">
          <el-date-picker
            v-model="timeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            :shortcuts="dateShortcuts"
            style="width:280px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="loadAll">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="6" v-for="card in summaryCards" :key="card.key">
        <el-card shadow="hover" :class="['stat-card', card.colorClass]">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value">
            <span class="stat-number">{{ overviewData[card.key] ?? '-' }}</span>
            <span v-if="card.unit && !String(overviewData[card.key] ?? '').includes('%')" class="stat-unit">
              {{ card.unit }}
            </span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-alert
      v-if="overviewData.evaluationSummary"
      type="info"
      :closable="false"
      show-icon
      class="stat-summary-alert"
      :title="String(overviewData.evaluationSummary)"
    />

    <el-alert
      v-if="overviewData.analyticsTrendSummary"
      type="success"
      :closable="false"
      show-icon
      class="stat-summary-alert"
      :title="String(overviewData.analyticsTrendSummary)"
    />

    <!-- 图表区域 -->
    <el-row :gutter="16">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header>
            <span style="font-weight:600">指标异常分布</span>
          </template>
          <div ref="barChartRef" style="height:360px" v-loading="chartLoading" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, markRaw } from 'vue'
import { getStatisticsOverview, getIndicatorDistribution } from '@/api/statistics'

const timeRange = ref<[string, string] | null>(null)
const overviewData = ref<Record<string, string | number>>({})
const chartLoading = ref(false)
const loading = ref(false)
const barChartRef = ref<HTMLDivElement>()
let chartInstance: any = null
let echartsLib: any = null

const summaryCards = [
  { key: 'unqualifiedRate', label: '不合格率', unit: '%', colorClass: 'card-danger' },
  { key: 'reinspectionRate', label: '复检率', unit: '%', colorClass: 'card-warning' },
  { key: 'concessionRate', label: '让步率', unit: '%', colorClass: 'card-info' },
  { key: 'standardConflictRate', label: '标准冲突率', unit: '%', colorClass: 'card-danger' },
  { key: 'conflictRemediationRate', label: '冲突闭环率', unit: '%', colorClass: 'card-info' },
  { key: 'lowConfidenceAiCount', label: '低置信AI', unit: '条', colorClass: 'card-warning' },
  { key: 'aiAdoptionRate', label: 'AI采纳率', unit: '%', colorClass: 'card-info' },
  { key: 'aiHitRate', label: 'AI命中率', unit: '%', colorClass: 'card-success' },
  { key: 'manualReviewHandleRate', label: '复核处理率', unit: '%', colorClass: 'card-warning' },
  { key: 'evaluationCaseCount', label: '评测样例', unit: '条', colorClass: 'card-success' },
  { key: 'totalInspection', label: '检验总数', unit: '批', colorClass: 'card-success' }
]

function formatDate(d: Date): string {
  return d.toISOString().slice(0, 10)
}

const dateShortcuts = [
  {
    text: '最近7天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 7)
      return [formatDate(start), formatDate(end)] as [string, string]
    }
  },
  {
    text: '最近30天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 30)
      return [formatDate(start), formatDate(end)] as [string, string]
    }
  },
  {
    text: '最近3个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 3)
      return [formatDate(start), formatDate(end)] as [string, string]
    }
  }
]

function buildParams() {
  const params: Record<string, string> = {}
  if (timeRange.value?.length === 2) {
    params.timeStart = `${timeRange.value[0]} 00:00:00`
    params.timeEnd = `${timeRange.value[1]} 23:59:59`
  }
  return params
}

async function ensureEcharts() {
  if (!echartsLib) {
    const mod = await import('echarts')
    echartsLib = mod
  }
  return echartsLib
}

async function loadOverview() {
  const res = await getStatisticsOverview(buildParams()) as Record<string, string | number>
  overviewData.value = {
    unqualifiedRate: res.unqualifiedRate ?? '-',
    reinspectionRate: res.reinspectionRate ?? '-',
    concessionRate: res.concessionRate ?? '-',
    standardConflictRate: res.standardConflictRate ?? '-',
    conflictRemediationRate: res.conflictRemediationRate ?? '-',
    lowConfidenceAiCount: res.lowConfidenceAiCount ?? 0,
    aiAdoptionRate: res.aiAdoptionRate ?? '-',
    aiHitRate: res.aiHitRate ?? '-',
    manualReviewHandleRate: res.manualReviewHandleRate ?? '-',
    evaluationCaseCount: res.evaluationCaseCount ?? 0,
    evaluationSummary: res.evaluationSummary ?? '',
    analyticsTrendSummary: res.analyticsTrendSummary ?? '',
    totalInspection: res.totalInspection ?? 0
  }
}

async function loadChart() {
  await nextTick()
  if (!barChartRef.value) return

  chartLoading.value = true
  try {
    const echarts = await ensureEcharts()
    const res = await getIndicatorDistribution(buildParams()) as Array<{
      indicatorName: string
      failCount: number
      unqualifiedCount?: number
    }>
    const data = Array.isArray(res) ? res : []

    if (chartInstance) {
      chartInstance.dispose()
      chartInstance = null
    }
    chartInstance = markRaw(echarts.init(barChartRef.value))

    chartInstance.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: 72, right: 24, top: 28, bottom: 64 },
      xAxis: {
        type: 'category',
        data: data.map((d) => d.indicatorName),
        axisLabel: { rotate: 30, interval: 0, color: '#A8C5DC' },
        axisLine: { lineStyle: { color: '#3a4a5c' } }
      },
      yAxis: {
        type: 'value',
        name: '不合格次数',
        nameLocation: 'middle',
        nameRotate: 90,
        nameGap: 48,
        nameTextStyle: { color: '#A8C5DC', fontSize: 12 },
        axisLabel: { color: '#A8C5DC' },
        splitLine: { lineStyle: { color: '#2a3a4c', type: 'dashed' } }
      },
      series: [
        {
          name: '不合格次数',
          type: 'bar',
          barMaxWidth: 48,
          barCategoryGap: '55%',
          data: data.map((d) => d.failCount ?? d.unqualifiedCount ?? 0),
          itemStyle: {
            color: '#f56c6c',
            borderRadius: [6, 6, 0, 0]
          },
          emphasis: { itemStyle: { color: '#e63939' } }
        }
      ]
    })
  } finally {
    chartLoading.value = false
  }
}

function resetFilter() {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 30)
  timeRange.value = [formatDate(start), formatDate(end)]
  loadAll()
}

async function loadAll() {
  loading.value = true
  try {
    await Promise.all([loadOverview(), loadChart()])
  } catch (e) {
    console.error('[Statistics] 加载失败', e)
  } finally {
    loading.value = false
  }
}

function handleResize() {
  chartInstance?.resize()
}

onMounted(() => {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 30)
  timeRange.value = [formatDate(start), formatDate(end)]
  loadAll()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
  chartInstance = null
})
</script>

<style scoped>
.statistics-page {
  padding: 16px;
}
.stat-summary-alert {
  margin-bottom: 16px;
}
.stat-card {
  text-align: center;
  border-top: 4px solid;
}
.card-danger {
  border-top-color: #f56c6c;
}
.card-warning {
  border-top-color: #e6a23c;
}
.card-info {
  border-top-color: #409eff;
}
.card-success {
  border-top-color: #67c23a;
}
.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}
.stat-value {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
}
.stat-number {
  font-size: 36px;
  font-weight: 700;
  font-family: var(--font-data);
  color: var(--text-heading);
}
.stat-unit {
  font-size: 14px;
  color: var(--text-secondary);
}
</style>
