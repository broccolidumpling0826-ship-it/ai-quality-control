<template>
  <div class="ai-explanation-page" v-loading="loading">
    <el-page-header @back="router.back()" content="AI 判定解释" style="margin-bottom:16px" />

    <template v-if="explanation">
      <!-- 状态栏 -->
      <div class="status-bar">
        <el-tag
          :type="confidenceTagType(explanation.confidenceLevel)"
          size="large"
          effect="dark"
          class="confidence-badge"
        >
          置信度：{{ confidenceLabel(explanation.confidenceLevel) }}
        </el-tag>
        <el-tag v-if="explanation.degraded" type="warning" size="large" effect="plain">
          降级模式（规则基线）
        </el-tag>
        <el-tag v-if="explanation.manualReviewRequired" type="danger" size="large" effect="plain">
          需人工复核
        </el-tag>
      </div>

      <!-- 自然语言解释 -->
      <el-card shadow="never" class="content-card">
        <template #header><span style="font-weight:600">AI 自然语言解释</span></template>
        <div class="narrative-text">{{ explanation.narrativeText || '暂无解释内容' }}</div>
      </el-card>

      <!-- 引用列表 -->
      <el-card v-if="explanation.citations?.length" shadow="never" class="content-card">
        <template #header><span style="font-weight:600">标准引用</span></template>
        <div
          v-for="(c, i) in explanation.citations"
          :key="i"
          class="citation-card"
        >
          <div class="citation-header">
            <span class="citation-index">[{{ i + 1 }}]</span>
            <span class="citation-name">{{ c.standardName || '标准文档' }}</span>
            <span v-if="c.sectionRef" class="citation-section">{{ c.sectionRef }}</span>
            <el-button
              link
              type="primary"
              size="small"
              @click="goToRag(c)"
            >在 RAG 中查看</el-button>
          </div>
          <p class="citation-text">{{ c.highlightText }}</p>
        </div>
      </el-card>

      <!-- 规则基线 -->
      <el-card v-if="explanation.baselineJson" shadow="never" class="content-card">
        <template #header>
          <div class="baseline-header">
            <span style="font-weight:600">规则基线（JSON）</span>
            <el-button link type="primary" size="small" @click="showBaseline = !showBaseline">
              {{ showBaseline ? '收起' : '展开' }}
            </el-button>
          </div>
        </template>
        <pre v-if="showBaseline" class="baseline-json">{{ formatBaseline(explanation.baselineJson) }}</pre>
      </el-card>

      <div class="bottom-bar">
        <el-button @click="router.back()">返回</el-button>
        <el-button
          v-if="judgmentId"
          type="primary"
          plain
          @click="router.push(`/judgment/explanation?id=${judgmentId}`)"
        >查看规则解释</el-button>
        <el-button
          v-if="explanation.auditLogId"
          type="info"
          plain
          @click="router.push(`/ai-audit?auditLogId=${explanation.auditLogId}`)"
        >查看审计记录</el-button>
      </div>
    </template>

    <el-empty v-else-if="!loading" description="未找到 AI 解释数据">
      <el-button type="primary" @click="loadExplanation">重新加载</el-button>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  getAiJudgmentExplanation,
  getDemoAiJudgmentExplanation,
  type AiJudgmentExplanation,
  type ConfidenceLevel
} from '@/api/ai-judgment'
import type { Citation } from '@/api/rag'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const explanation = ref<AiJudgmentExplanation | null>(null)
const showBaseline = ref(false)

const judgmentId = computed(() => route.query.id as string | undefined)
const demoCode = computed(() => route.query.demoCode as string | undefined)

function confidenceLabel(level?: ConfidenceLevel) {
  const map: Record<string, string> = { HIGH: '高', MEDIUM: '中', LOW: '低' }
  return map[level || ''] || level || '未知'
}

function confidenceTagType(level?: ConfidenceLevel) {
  const map: Record<string, string> = { HIGH: 'success', MEDIUM: 'warning', LOW: 'danger' }
  return map[level || ''] || 'info'
}

function formatBaseline(json?: string) {
  if (!json) return ''
  try {
    return JSON.stringify(JSON.parse(json), null, 2)
  } catch {
    return json
  }
}

function goToRag(c: Citation) {
  const q = c.highlightText?.slice(0, 50) || c.standardName || ''
  router.push({ path: '/standard-rag', query: { q } })
}

async function loadExplanation() {
  loading.value = true
  try {
    if (demoCode.value) {
      explanation.value = await getDemoAiJudgmentExplanation(demoCode.value)
    } else if (judgmentId.value) {
      explanation.value = await getAiJudgmentExplanation(judgmentId.value)
    }
  } finally {
    loading.value = false
  }
}

onMounted(loadExplanation)
</script>

<style scoped>
.ai-explanation-page {
  padding: 16px;
  max-width: 960px;
}

.status-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}

.confidence-badge {
  font-size: 15px !important;
  padding: 8px 16px !important;
}

.content-card {
  border-radius: 12px;
  margin-bottom: 12px;
}

.narrative-text {
  font-size: 15px;
  line-height: 1.8;
  color: var(--text-heading);
  white-space: pre-wrap;
}

.citation-card {
  padding: 12px;
  border: 1px solid var(--border);
  border-radius: 8px;
  margin-bottom: 10px;
}

.citation-header {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 6px;
}

.citation-index {
  color: var(--cyan);
  font-weight: 700;
}

.citation-name {
  font-weight: 600;
  color: var(--text-heading);
}

.citation-section {
  font-size: 12px;
  color: var(--text-secondary);
}

.citation-text {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.baseline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.baseline-json {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 12px;
  font-size: 12px;
  overflow-x: auto;
  color: var(--text-secondary);
  max-height: 360px;
}

.bottom-bar {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
