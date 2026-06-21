<template>
  <div class="dashboard">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">质量工作台</h2>
        <p class="page-desc">实时监控质量关键指标，掌握待处理任务动态</p>
      </div>
      <el-button type="primary" plain :icon="Refresh" @click="loadData" :loading="summaryLoading">
        刷新数据
      </el-button>
    </div>

    <!-- AI 风险预警 -->
    <el-card
      v-if="aiRiskAlertCount > 0"
      shadow="never"
      class="ai-risk-card"
    >
      <div class="ai-risk-content">
        <el-icon class="ai-risk-icon"><WarningFilled /></el-icon>
        <div class="ai-risk-body">
          <p class="ai-risk-title">AI 风险预警</p>
          <p class="ai-risk-desc">
            检测到 {{ aiRiskAlertCount }} 项需关注风险（高险让步 / 未裁定冲突 / 低置信度判定）
          </p>
        </div>
        <div class="ai-risk-actions">
          <el-button type="warning" plain size="small" @click="router.push('/standard-lib/conflicts')">
            查看冲突
          </el-button>
          <el-button type="danger" plain size="small" @click="router.push('/ai-audit')">
            AI 审计
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 决赛演示快捷入口 -->
    <el-card shadow="never" class="demo-card" v-loading="demoLoading">
      <template #header>
        <div class="card-title">
          <el-icon class="title-icon"><Promotion /></el-icon>
          <span>决赛演示快捷入口</span>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col
          v-for="scenario in demoScenarios"
          :key="scenario.demoCode"
          :xs="24"
          :sm="12"
          :lg="6"
        >
          <div
            class="demo-scenario-card"
            :class="`demo-scenario-card--${scenarioTypeClass(scenario.judgmentType)}`"
            @click="goDemoScenario(scenario)"
          >
            <div class="demo-scenario-badge">
              <el-tag :type="judgmentTagType(scenario.judgmentType) as any" size="small" effect="dark">
                {{ judgmentTypeLabel(scenario.judgmentType) }}
              </el-tag>
            </div>
            <p class="demo-scenario-title">{{ scenario.title }}</p>
            <p class="demo-scenario-code">{{ scenario.demoCode }}</p>
            <div class="demo-scenario-footer">
              <span>查看判定解释</span>
              <el-icon><ArrowRight /></el-icon>
            </div>
          </div>
        </el-col>
      </el-row>
      <el-empty v-if="!demoLoading && demoScenarios.length === 0" description="暂无演示数据" :image-size="60" />
    </el-card>

    <!-- 指标卡片区 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card stat-card--warning" shadow="hover">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-label">待判样品</p>
              <p class="stat-value">{{ summary.pendingJudgment }}</p>
              <p class="stat-hint">待质量判定处理</p>
            </div>
            <div class="stat-icon stat-icon--warning">
              <el-icon><Stamp /></el-icon>
            </div>
          </div>
          <div class="stat-footer">
            <router-link to="/judgment" class="stat-link">
              去处理 <el-icon><ArrowRight /></el-icon>
            </router-link>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card stat-card--danger" shadow="hover">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-label">不合格批次</p>
              <p class="stat-value">{{ summary.unqualifiedBatch }}</p>
              <p class="stat-hint">本月不合格批次数</p>
            </div>
            <div class="stat-icon stat-icon--danger">
              <el-icon><CircleCloseFilled /></el-icon>
            </div>
          </div>
          <div class="stat-footer">
            <router-link to="/judgment" class="stat-link">
              查看详情 <el-icon><ArrowRight /></el-icon>
            </router-link>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card stat-card--primary" shadow="hover">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-label">复检任务</p>
              <p class="stat-value">{{ summary.reinspectionTask }}</p>
              <p class="stat-hint">待复检样品任务</p>
            </div>
            <div class="stat-icon stat-icon--primary">
              <el-icon><RefreshRight /></el-icon>
            </div>
          </div>
          <div class="stat-footer">
            <router-link to="/reinspection" class="stat-link">
              去处理 <el-icon><ArrowRight /></el-icon>
            </router-link>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card stat-card--success" shadow="hover">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-label">让步审批</p>
              <p class="stat-value">{{ summary.concessionApproval }}</p>
              <p class="stat-hint">待审批让步申请</p>
            </div>
            <div class="stat-icon stat-icon--success">
              <el-icon><Check /></el-icon>
            </div>
          </div>
          <div class="stat-footer">
            <router-link to="/concession" class="stat-link">
              去审批 <el-icon><ArrowRight /></el-icon>
            </router-link>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 下方主内容区 -->
    <el-row :gutter="20" class="main-row">
      <!-- 待处理事项列表 -->
      <el-col :xs="24" :lg="16">
        <el-card shadow="never" class="content-card">
          <template #header>
            <div class="card-header">
              <div class="card-title">
                <el-icon class="title-icon"><List /></el-icon>
                <span>待处理事项</span>
                <el-badge :value="pendingItems.length" :hidden="pendingItems.length === 0" class="badge" />
              </div>
              <div class="card-actions">
                <el-radio-group v-model="pendingFilter" size="small" @change="filterPendingItems">
                  <el-radio-button value="">全部</el-radio-button>
                  <el-radio-button value="judgment">判定</el-radio-button>
                  <el-radio-button value="reinspection">复检</el-radio-button>
                  <el-radio-button value="concession">让步</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>

          <el-table
            v-loading="pendingLoading"
            :data="filteredPendingItems"
            :border="false"
            stripe
            style="width: 100%"
          >
            <el-table-column label="类型" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="getTypeTagType(row.type)" size="small" effect="light">
                  {{ getTypeLabel(row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="batchNo" label="批次号" width="140" show-overflow-tooltip />
            <el-table-column prop="description" label="事项描述" show-overflow-tooltip />
            <el-table-column label="优先级" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="getPriorityTagType(row.priority)" size="small">
                  {{ getPriorityLabel(row.priority) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="150" align="center" />
            <el-table-column label="操作" width="80" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handlePendingItem(row)">
                  处理
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div v-if="filteredPendingItems.length === 0 && !pendingLoading" class="empty-hint">
            <el-empty description="暂无待处理事项" :image-size="80" />
          </div>
        </el-card>
      </el-col>

      <!-- 系统消息区 -->
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="content-card">
          <template #header>
            <div class="card-header">
              <div class="card-title">
                <el-icon class="title-icon"><Bell /></el-icon>
                <span>系统消息</span>
              </div>
              <el-button type="primary" link size="small" @click="markAllRead">全部已读</el-button>
            </div>
          </template>

          <div v-loading="msgLoading" class="msg-list">
            <div
              v-for="msg in systemMessages"
              :key="msg.id"
              class="msg-item"
              :class="{ unread: !msg.isRead }"
              @click="handleMsgClick(msg)"
            >
              <el-icon class="msg-dot" :class="`msg-dot--${msg.level}`">
                <component :is="getMsgIcon(msg.level)" />
              </el-icon>
              <div class="msg-body">
                <p class="msg-title">{{ msg.title }}</p>
                <p class="msg-content">{{ msg.content }}</p>
                <span class="msg-time">{{ msg.createTime }}</span>
              </div>
              <div v-if="!msg.isRead" class="unread-dot"></div>
            </div>

            <div v-if="systemMessages.length === 0 && !msgLoading" class="empty-hint">
              <el-empty description="暂无系统消息" :image-size="60" />
            </div>
          </div>
        </el-card>

        <!-- 快捷操作卡片 -->
        <el-card shadow="never" class="content-card quick-actions" style="margin-top: 20px;">
          <template #header>
            <div class="card-title">
              <el-icon class="title-icon"><Grid /></el-icon>
              <span>快捷操作</span>
            </div>
          </template>
          <div class="quick-grid">
            <div
              v-for="action in quickActions"
              :key="action.path"
              class="quick-item"
              @click="router.push(action.path)"
            >
              <el-icon class="quick-icon" :class="`quick-icon--${action.color}`">
                <component :is="action.icon" />
              </el-icon>
              <span>{{ action.label }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Refresh, Stamp, RefreshRight, Check, ArrowRight,
  List, Bell, Grid, CircleCloseFilled,
  InfoFilled, WarningFilled, CircleCheckFilled, Promotion
} from '@element-plus/icons-vue'
import { get } from '@/utils/request'
import { getDashboardOverview, getDemoScenarios, type DemoScenario } from '@/api/dashboard'
import type { DashboardSummary, PendingItem, SystemMessage } from '@/types'

const router = useRouter()

// ─── 摘要数据 ─────────────────────────────────────────────
const summaryLoading = ref(false)
const summary = ref<DashboardSummary>({
  pendingJudgment: 0,
  unqualifiedBatch: 0,
  reinspectionTask: 0,
  concessionApproval: 0
})
const aiRiskAlertCount = ref(0)
const demoLoading = ref(false)
const demoScenarios = ref<DemoScenario[]>([])

async function loadSummary() {
  summaryLoading.value = true
  try {
    const data = await getDashboardOverview()
    summary.value = {
      pendingJudgment: Number(data.pendingJudgmentCount ?? 0),
      unqualifiedBatch: Number(data.unqualifiedCount ?? 0),
      reinspectionTask: Number(data.pendingReinspectionCount ?? 0),
      concessionApproval: Number(data.pendingConcessionApprovalCount ?? 0)
    }
    aiRiskAlertCount.value = Number(data.aiRiskAlertCount ?? 0)
    if (data.demoLinks?.length) {
      demoScenarios.value = data.demoLinks
    }
  } catch {
    summary.value = {
      pendingJudgment: 0,
      unqualifiedBatch: 0,
      reinspectionTask: 0,
      concessionApproval: 0
    }
    aiRiskAlertCount.value = 0
  } finally {
    summaryLoading.value = false
  }
}

async function loadDemoScenarios() {
  demoLoading.value = true
  try {
    if (!demoScenarios.value.length) {
      demoScenarios.value = await getDemoScenarios()
    }
  } finally {
    demoLoading.value = false
  }
}

function judgmentTypeLabel(type?: string) {
  const map: Record<string, string> = {
    QUALIFIED: '合格',
    UNQUALIFIED: '不合格',
    CAN_CONCESSION: '可让步',
    CONCESSION: '让步',
    NEED_REINSPECTION: '需复检'
  }
  return map[type || ''] || type || '演示'
}

function judgmentTagType(type?: string) {
  const map: Record<string, string> = {
    QUALIFIED: 'success',
    UNQUALIFIED: 'danger',
    CAN_CONCESSION: 'warning',
    CONCESSION: 'warning',
    NEED_REINSPECTION: 'info'
  }
  return map[type || ''] || 'info'
}

function scenarioTypeClass(type?: string) {
  const map: Record<string, string> = {
    QUALIFIED: 'success',
    UNQUALIFIED: 'danger',
    CAN_CONCESSION: 'warning',
    CONCESSION: 'warning'
  }
  return map[type || ''] || 'primary'
}

function goDemoScenario(scenario: DemoScenario) {
  if (scenario.routePath) {
    router.push(scenario.routePath)
    return
  }
  if (scenario.judgmentId) {
    router.push(`/judgment/explanation?id=${scenario.judgmentId}`)
  }
}

// ─── 待处理事项 ───────────────────────────────────────────
const pendingLoading = ref(false)
const pendingItems = ref<PendingItem[]>([])
const pendingFilter = ref('')

const filteredPendingItems = computed(() => {
  if (!pendingFilter.value) return pendingItems.value
  return pendingItems.value.filter((item) => item.type === pendingFilter.value)
})

function filterPendingItems() {
  // 过滤由 computed 自动处理
}

async function loadPendingItems() {
  pendingLoading.value = true
  try {
    const data = await get<PendingItem[]>('/dashboard/pending-items')
    pendingItems.value = data
  } catch {
    pendingItems.value = []
  } finally {
    pendingLoading.value = false
  }
}

function getTypeLabel(type: string): string {
  const map: Record<string, string> = {
    judgment: '判定',
    reinspection: '复检',
    concession: '让步',
    audit: '审计'
  }
  return map[type] ?? type
}

function getTypeTagType(type: string): '' | 'success' | 'warning' | 'danger' | 'info' {
  const map: Record<string, '' | 'success' | 'warning' | 'danger' | 'info'> = {
    judgment: 'warning',
    reinspection: '',
    concession: 'success',
    audit: 'info'
  }
  return map[type] ?? 'info'
}

function getPriorityLabel(priority: string): string {
  const map: Record<string, string> = { high: '高', medium: '中', low: '低' }
  return map[priority] ?? priority
}

function getPriorityTagType(priority: string): '' | 'success' | 'warning' | 'danger' | 'info' {
  const map: Record<string, '' | 'success' | 'warning' | 'danger' | 'info'> = {
    high: 'danger',
    medium: 'warning',
    low: 'success'
  }
  return map[priority] ?? 'info'
}

function handlePendingItem(row: PendingItem) {
  const routeMap: Record<string, string> = {
    judgment: '/judgment',
    reinspection: '/reinspection',
    concession: '/concession'
  }
  const path = routeMap[row.type] || '/dashboard'
  router.push(path)
}

// ─── 系统消息 ─────────────────────────────────────────────
const msgLoading = ref(false)
const systemMessages = ref<SystemMessage[]>([])

async function loadSystemMessages() {
  msgLoading.value = true
  try {
    const data = await get<SystemMessage[]>('/dashboard/messages')
    systemMessages.value = data
  } catch {
    systemMessages.value = []
  } finally {
    msgLoading.value = false
  }
}

function getMsgIcon(level: string) {
  const map: Record<string, typeof InfoFilled> = {
    info: InfoFilled,
    success: CircleCheckFilled,
    warning: WarningFilled,
    error: CircleCloseFilled
  }
  return map[level] ?? InfoFilled
}

function handleMsgClick(msg: SystemMessage) {
  msg.isRead = true
}

function markAllRead() {
  systemMessages.value.forEach((msg) => (msg.isRead = true))
  ElMessage.success('已全部标记为已读')
}

// ─── 快捷操作 ─────────────────────────────────────────────
const quickActions = [
  { label: '检验录入', path: '/inspection', icon: 'EditPen', color: 'primary' },
  { label: '判定解释', path: '/judgment', icon: 'Stamp', color: 'warning' },
  { label: '复检管理', path: '/reinspection', icon: 'RefreshRight', color: 'info' },
  { label: '质量统计', path: '/statistics', icon: 'PieChart', color: 'success' },
  { label: '标准库', path: '/standard-lib', icon: 'Document', color: 'primary' },
  { label: '权限审计', path: '/audit', icon: 'Lock', color: 'danger' }
]

// ─── 加载数据 ─────────────────────────────────────────────
async function loadData() {
  await Promise.all([
    loadSummary(),
    loadDemoScenarios(),
    loadPendingItems(),
    loadSystemMessages()
  ])
}

onMounted(loadData)
</script>

<style scoped>
.dashboard {
  min-height: calc(100vh - 100px);
}

/* ── 页面标题 ─────────────────────────────────────────────── */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
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

/* ── AI 风险预警 ─────────────────────────────────────────── */
.ai-risk-card {
  border-radius: 12px;
  margin-bottom: 16px;
  border: 1px solid rgba(255, 140, 0, 0.35);
  background: rgba(255, 140, 0, 0.08);
}

.ai-risk-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.ai-risk-icon {
  font-size: 32px;
  color: var(--orange);
  flex-shrink: 0;
}

.ai-risk-body {
  flex: 1;
}

.ai-risk-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-heading);
  margin-bottom: 4px;
}

.ai-risk-desc {
  font-size: 13px;
  color: var(--text-secondary);
}

.ai-risk-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

/* ── 演示快捷入口 ─────────────────────────────────────────── */
.demo-card {
  border-radius: 12px;
  margin-bottom: 20px;
}

.demo-card :deep(.el-card__header) {
  padding: 14px 20px;
  border-bottom: 1px solid var(--border);
}

.demo-scenario-card {
  padding: 16px;
  border-radius: 12px;
  border: 1px solid var(--border);
  background: var(--bg-card);
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 12px;
  min-height: 130px;
  display: flex;
  flex-direction: column;
}

.demo-scenario-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.demo-scenario-card--success:hover { border-color: var(--green); }
.demo-scenario-card--danger:hover { border-color: var(--red); }
.demo-scenario-card--warning:hover { border-color: var(--orange); }
.demo-scenario-card--primary:hover { border-color: var(--cyan); }

.demo-scenario-badge {
  margin-bottom: 10px;
}

.demo-scenario-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-heading);
  margin-bottom: 4px;
  flex: 1;
}

.demo-scenario-code {
  font-size: 12px;
  color: var(--text-muted);
  font-family: var(--font-data);
  margin-bottom: 10px;
}

.demo-scenario-footer {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-secondary);
}

.demo-scenario-card:hover .demo-scenario-footer {
  color: var(--cyan);
}

/* ── 指标卡片 ─────────────────────────────────────────────── */
.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
  margin-bottom: 0;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12) !important;
}

.stat-card :deep(.el-card__body) {
  padding: 20px;
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.stat-value {
  font-size: 36px;
  font-weight: 800;
  line-height: 1;
  margin-bottom: 6px;
  font-family: var(--font-data);
  color: var(--text-heading);
}

.stat-card--warning .stat-value { color: var(--orange); }
.stat-card--danger .stat-value  { color: var(--red); }
.stat-card--primary .stat-value { color: var(--blue); }
.stat-card--success .stat-value { color: var(--green); }

.stat-hint {
  font-size: 12px;
  color: var(--text-muted);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  flex-shrink: 0;
}

.stat-icon--warning { background: rgba(255, 140, 0, 0.15); color: var(--orange); }
.stat-icon--danger  { background: rgba(255, 59, 92, 0.15); color: var(--red); }
.stat-icon--primary { background: rgba(61, 158, 255, 0.15); color: var(--blue); }
.stat-icon--success { background: rgba(22, 201, 116, 0.15); color: var(--green); }

.stat-footer {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--border);
}

.stat-link {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-secondary);
  text-decoration: none;
  transition: color 0.2s;
}

.stat-link:hover {
  color: var(--cyan);
}

/* ── 主内容区 ─────────────────────────────────────────────── */
.main-row {
  margin-top: 0;
}

.content-card {
  border-radius: 12px;
  margin-bottom: 0;
}

.content-card :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-heading);
}

.title-icon {
  color: var(--cyan);
}

.badge {
  margin-left: 4px;
}

.empty-hint {
  padding: 24px 0;
  display: flex;
  justify-content: center;
}

/* ── 消息列表 ─────────────────────────────────────────────── */
.msg-list {
  max-height: 360px;
  overflow-y: auto;
}

.msg-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 8px;
  border-bottom: 1px solid var(--border);
  cursor: pointer;
  position: relative;
  transition: background-color 0.2s;
  border-radius: 6px;
}

.msg-item:last-child {
  border-bottom: none;
}

.msg-item:hover {
  background-color: var(--bg-hover);
}

.msg-item.unread {
  background-color: rgba(0, 212, 255, 0.08);
}

.msg-dot {
  font-size: 18px;
  flex-shrink: 0;
  margin-top: 2px;
}

.msg-dot--info    { color: #409eff; }
.msg-dot--success { color: #67c23a; }
.msg-dot--warning { color: #e6a23c; }
.msg-dot--error   { color: #f56c6c; }

.msg-body {
  flex: 1;
  min-width: 0;
}

.msg-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-heading);
  margin-bottom: 4px;
}

.msg-content {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 4px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.msg-time {
  font-size: 11px;
  color: var(--text-muted);
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: var(--cyan);
  flex-shrink: 0;
  margin-top: 6px;
}

/* ── 快捷操作 ─────────────────────────────────────────────── */
.quick-actions :deep(.el-card__body) {
  padding: 16px;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 14px 8px;
  border-radius: 10px;
  cursor: pointer;
  border: 1px solid var(--border);
  background: var(--bg-card);
  transition: all 0.2s;
  font-size: 12px;
  color: var(--text-secondary);
}

.quick-item:hover {
  border-color: var(--cyan);
  background-color: rgba(0, 212, 255, 0.08);
  color: var(--text-heading);
  transform: translateY(-2px);
}

.quick-icon {
  font-size: 22px;
}

.quick-icon--primary { color: var(--blue); }
.quick-icon--warning { color: var(--orange); }
.quick-icon--info    { color: var(--text-secondary); }
.quick-icon--success { color: var(--green); }
.quick-icon--danger  { color: var(--red); }
</style>
