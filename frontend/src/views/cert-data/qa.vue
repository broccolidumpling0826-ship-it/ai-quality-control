<template>
  <div class="cert-qa-page">
    <div class="page-toolbar">
      <div>
        <div class="page-toolbar-title">质保书问答</div>
        <div class="page-toolbar-desc">基于质保书快照与判定依据回答出证问题；正式快照请在质保书数据页生成。</div>
      </div>
      <el-button plain @click="goToCertData">← 质保书数据</el-button>
    </div>

    <el-card class="query-card" shadow="never">
      <el-form :model="form" inline>
        <el-form-item label="卷号">
          <el-input v-model="form.coilNo" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="批次">
          <el-input v-model="form.batchNo" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="问题">
          <el-input
            v-model="form.question"
            class="question-input"
            clearable
            placeholder="例如：这卷为什么能出证？"
            @keyup.enter="ask"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="ChatLineSquare" :loading="loading" @click="ask">提问</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="qa-grid">
      <el-card
        class="answer-card"
        shadow="never"
        v-loading="loading"
        :element-loading-text="AI_LOADING_TEXT.certQa"
        :element-loading-custom-class="AI_LOADING_CLASS"
      >
        <template #header>
          <div class="head">
            <span>回答</span>
            <div v-if="answer" class="tags">
              <el-tag :type="answer.nonFinal ? 'warning' : 'success'" size="small">
                {{ answer.nonFinal ? 'NON_FINAL' : 'FINAL' }}
              </el-tag>
              <el-tag :type="confidenceType(answer.confidenceLabel)" size="small">
                {{ answer.confidenceLabel || 'N/A' }}
              </el-tag>
              <el-tag v-if="answer.cacheHit" type="warning" size="small">CACHE</el-tag>
              <el-tag type="info" size="small">{{ answer.degradationSource || 'N/A' }}</el-tag>
            </div>
          </div>
        </template>
        <el-alert
          v-if="answer?.refused"
          type="error"
          show-icon
          :closable="false"
          :title="answer.refusalReason || '无法回答'"
        />
        <el-alert
          v-else-if="answer?.guidanceMessage"
          type="warning"
          show-icon
          :closable="false"
          :title="answer.guidanceMessage"
          style="margin-bottom: 12px"
        />
        <p class="answer-text" :class="{ 'is-loading': loading }">
          <span v-if="loading" class="answer-loading-hint">{{ AI_LOADING_TEXT.certQa }}</span>
          <template v-else>
            <template v-for="(part, index) in answerParts" :key="index">
              <span v-if="part.type === 'text'">{{ part.value }}</span>
              <span v-else class="citation-mark">[{{ part.index }}]</span>
            </template>
          </template>
        </p>
      </el-card>

      <el-card class="basis-card" shadow="never">
        <template #header>指标依据</template>
        <el-table :data="answer?.indicatorBasis || []" border stripe size="small">
          <el-table-column prop="indicatorName" label="指标" min-width="100" />
          <el-table-column prop="testValue" label="实测" width="90" />
          <el-table-column prop="lowerLimit" label="下限" width="90" />
          <el-table-column prop="upperLimit" label="上限" width="90" />
          <el-table-column prop="deviation" label="偏差" width="90" />
          <el-table-column prop="triggerRule" label="规则" min-width="220" show-overflow-tooltip />
        </el-table>
      </el-card>
    </div>

    <el-card class="citation-card" shadow="never">
      <template #header>来源引用</template>
      <el-table :data="answer?.citations || []" border stripe size="small" class="citation-table">
        <el-table-column label="引用" width="72" align="center" fixed>
          <template #default="{ $index }">
            <span class="citation-index">[{{ $index + 1 }}]</span>
          </template>
        </el-table-column>
        <el-table-column prop="standardCode" label="标准/协议" min-width="160" />
        <el-table-column prop="clauseNo" label="条款" width="100" />
        <el-table-column prop="pageNo" label="页码" width="80" />
        <el-table-column prop="paragraphText" label="段落" min-width="360" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ChatLineSquare } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { askCertQa, type CertQaAnswer, type CertQaQuery } from '@/api/cert-data'
import { useCitationParts } from '@/composables/use-citation-parts'
import { AI_LOADING_TEXT, AI_LOADING_CLASS } from '@/constants/ai-loading-text'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const answer = ref<CertQaAnswer | null>(null)
const form = reactive<CertQaQuery>({
  coilNo: '',
  batchNo: '',
  question: ''
})

const DEFAULT_ANSWER_HINT = '按卷号或批次提问，系统会基于质保书快照和判定依据回答。'

const answerParts = useCitationParts(
  () => {
    const text = answer.value?.answer
    const guidance = answer.value?.guidanceMessage
    if (!text) return DEFAULT_ANSWER_HINT
    if (guidance && text.startsWith(guidance)) {
      return text.slice(guidance.length).replace(/^\s+/, '') || DEFAULT_ANSWER_HINT
    }
    return text
  },
  DEFAULT_ANSWER_HINT
)

async function ask() {
  if (!form.coilNo && !form.batchNo) {
    ElMessage.warning('请输入卷号或批次号')
    return
  }
  if (!form.question.trim()) {
    ElMessage.warning('请输入问题')
    return
  }
  loading.value = true
  try {
    answer.value = await askCertQa({ ...form, question: form.question.trim() })
  } finally {
    loading.value = false
  }
}

function confidenceType(label?: string) {
  if (label === 'HIGH') return 'success'
  if (label === 'LOW') return 'danger'
  return 'warning'
}

function goToCertData() {
  router.push({
    path: '/cert-data',
    query: {
      ...(form.coilNo ? { coilNo: form.coilNo } : {}),
      ...(form.batchNo ? { batchNo: form.batchNo } : {})
    }
  })
}

function applyRouteQuery() {
  const coilNo = typeof route.query.coilNo === 'string' ? route.query.coilNo : ''
  const batchNo = typeof route.query.batchNo === 'string' ? route.query.batchNo : ''
  const question = typeof route.query.question === 'string' ? route.query.question : ''
  if (coilNo) form.coilNo = coilNo
  if (batchNo) form.batchNo = batchNo
  if (question) form.question = question
}

onMounted(() => {
  applyRouteQuery()
  if (form.question.trim() && (form.coilNo || form.batchNo)) {
    void ask()
  }
})
</script>

<style scoped>
.cert-qa-page {
  padding: 16px;
}

.page-toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.page-toolbar-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.page-toolbar-desc {
  margin-top: 4px;
  font-size: 13px;
  color: var(--text-secondary, #909399);
}

.query-card,
.answer-card,
.basis-card,
.citation-card {
  background: var(--bg-panel);
  border-color: var(--border-color);
}

.question-input {
  width: min(520px, 42vw);
}

.qa-grid {
  display: grid;
  grid-template-columns: minmax(0, 0.9fr) minmax(460px, 1.1fr);
  gap: 12px;
  margin-top: 12px;
}

.citation-card {
  margin-top: 12px;
}

.head,
.tags {
  display: flex;
  align-items: center;
  gap: 8px;
}

.head {
  justify-content: space-between;
}

.answer-text {
  min-height: 160px;
  margin: 0;
  line-height: 1.7;
  white-space: pre-wrap;
  color: var(--text-primary);
}

.answer-text.is-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.answer-loading-hint {
  color: var(--text-secondary, #909399);
  max-width: 320px;
}

.citation-mark,
.citation-index {
  display: inline-block;
  padding: 0 4px;
  border-radius: 4px;
  font-weight: 600;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.citation-table :deep(.citation-index) {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}

@media (max-width: 1100px) {
  .qa-grid {
    grid-template-columns: 1fr;
  }
  .question-input {
    width: 100%;
  }
}
</style>
