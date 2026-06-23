<template>
  <div class="standard-rag-page">
    <div class="page-header">
      <div class="page-title">标准 RAG 检索</div>
      <div class="page-desc">自然语言检索标准条款，回答附带可追溯引用来源</div>
    </div>

    <div class="rag-layout">
      <!-- 左侧：智能问答 -->
      <section class="chat-panel" v-loading="loading">
        <div class="chat-main" :class="{ 'is-empty': !submittedQuery && !loading }">
          <div class="chat-messages">
            <el-empty
              v-if="!submittedQuery && !loading"
              description="输入问题后开始检索"
              :image-size="72"
            />

            <template v-else>
              <div v-if="submittedQuery" class="chat-item user-item">
                <div class="chat-bubble user-bubble">{{ submittedQuery }}</div>
              </div>

              <div v-if="answer || loading" class="chat-item ai-item">
                <div class="chat-bubble ai-bubble">
                  <div class="ai-head">
                    <el-tag size="small" :type="degradationTagType(answer?.degradationSource, answer?.cacheHit)">
                      {{ degradationLabel(answer?.degradationSource, answer?.cacheHit) }}
                    </el-tag>
                    <el-tag v-if="answer?.confidenceLabel" size="small" :type="confidenceType(answer.confidenceLabel)">
                      {{ answer.confidenceLabel }}
                    </el-tag>
                  </div>

                  <el-alert
                    v-if="answer?.refused"
                    class="refusal-alert"
                    type="warning"
                    :closable="false"
                    :title="answer.refusalReason || '在已上传的标准/协议中未找到相关依据'"
                    show-icon
                  />

                  <div v-else class="answer-text">{{ displayAnswer }}</div>
                  <div v-if="answer?.degradationReason" class="degrade-text">{{ answer.degradationReason }}</div>

                  <div v-if="sources.length" class="citation-row">
                    <button
                      v-for="(item, index) in sources"
                      :key="sourceKey(item, index)"
                      type="button"
                      class="citation-tag"
                      :class="{ active: activeSourceIndex === index }"
                      @click="focusSource(index)"
                    >
                      {{ buildRagSourceShortTitle(item, index) }}
                    </button>
                  </div>
                </div>
              </div>
            </template>
          </div>

          <div class="chat-input">
          <div class="input-toolbar">
            <el-select
              v-model="form.sourceTypes"
              multiple
              collapse-tags
              collapse-tags-tooltip
              clearable
              placeholder="标准类型（全部）"
              class="source-type-select"
            >
              <el-option label="国标" value="NATIONAL" />
              <el-option label="企标" value="ENTERPRISE" />
              <el-option label="客协" value="CUSTOMER" />
              <el-option label="案例/投诉" value="COMPLAINT" />
            </el-select>
            <el-select
              v-model="form.customerId"
              clearable
              filterable
              placeholder="客户（全部）"
              class="filter-select"
            >
              <el-option
                v-for="item in customerOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <el-select
              v-model="form.variety"
              clearable
              filterable
              placeholder="品种（全部）"
              class="filter-select short"
            >
              <el-option
                v-for="item in dictStore.getItems('PRODUCT_VARIETY')"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <el-select
              v-model="form.grade"
              clearable
              filterable
              placeholder="牌号（全部）"
              class="filter-select short"
            >
              <el-option
                v-for="item in dictStore.getItems('PRODUCT_GRADE')"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </div>
          <div class="input-row">
            <el-input
              v-model="form.query"
              type="textarea"
              :rows="2"
              resize="none"
              placeholder="例如：Q235B 抗拉强度下限是多少？"
              @keydown.ctrl.enter.prevent="handleQuery"
            />
            <el-button type="primary" :loading="loading" class="search-btn" @click="handleQuery">
              检索
            </el-button>
          </div>
          <div class="input-hint">Ctrl + Enter 发送 · 仅依据检索到的标准原文回答，不会编造限值</div>
          </div>
        </div>
      </section>

      <!-- 右侧：引用来源 -->
      <aside class="source-panel">
        <div class="source-panel-head">
          <span class="source-panel-title">引用来源</span>
          <span v-if="sources.length" class="source-count">{{ sources.length }}</span>
        </div>

        <el-empty v-if="!sources.length" description="检索后将在此展示引用标准" :image-size="64" />

        <div v-else ref="sourceListRef" class="source-list">
          <article
            v-for="(item, index) in sources"
            :key="sourceKey(item, index)"
            :ref="(el) => setSourceRef(el, index)"
            class="source-card"
            :class="{ active: activeSourceIndex === index }"
            @click="activeSourceIndex = index"
          >
            <div class="source-card-head">
              <span class="source-index">[{{ index + 1 }}]</span>
              <span class="source-name">{{ item.standardCode || item.standardName || '未知标准' }}</span>
              <el-tag size="small" :type="ragSourceTypeTagType(item.sourceType)">
                {{ ragSourceTypeLabel(item.sourceType) }}
              </el-tag>
              <el-tag v-if="item.referenceOnly" size="small" type="warning">参考</el-tag>
            </div>
            <div v-if="item.clauseNo" class="source-clause">条款 {{ item.clauseNo }}</div>

            <div
              v-if="activeSourceIndex === index"
              class="source-excerpt"
              v-html="highlightParagraph(item.paragraphText || '暂无段落内容', highlightKeywords)"
            />
            <div v-else class="source-preview">
              {{ previewText(item.paragraphText) }}
            </div>

            <div class="source-foot">
              <span v-if="item.pageNo != null" class="mono">P{{ item.pageNo }}</span>
              <span v-if="item.score != null" class="mono">score {{ Number(item.score).toFixed(3) }}</span>
              <span v-if="item.sourceFileName" class="file-name">{{ item.sourceFileName }}</span>
            </div>
          </article>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import type { ComponentPublicInstance } from 'vue'
import { ElMessage } from 'element-plus'
import { queryStandardRag, type RagSource, type StandardRagAnswer, type StandardRagQuery } from '@/api/standard-rag'
import { useDictStore } from '@/store/dict'
import type { DictItem } from '@/types'
import {
  buildRagSourceShortTitle,
  degradationLabel,
  degradationTagType,
  extractHighlightKeywords,
  highlightParagraph,
  ragSourceTypeLabel,
  ragSourceTypeTagType,
  sourceKey
} from '@/utils/standard-rag-display'

const dictStore = useDictStore()
const customerOptions = ref<DictItem[]>([])

const loading = ref(false)
const submittedQuery = ref('')
const answer = ref<StandardRagAnswer | null>(null)
const activeSourceIndex = ref(0)
const sourceListRef = ref<HTMLElement | null>(null)
const sourceCardRefs = ref<Array<HTMLElement | null>>([])

const form = reactive<StandardRagQuery>({
  query: '',
  sourceTypes: [],
  topK: 6
})

const sources = computed(() => answer.value?.sources || [])

const highlightKeywords = computed(() => {
  const queryText = submittedQuery.value || form.query || ''
  const fromAnswer = answer.value?.query || ''
  return extractHighlightKeywords(queryText || fromAnswer)
})

const displayAnswer = computed(() => {
  if (!answer.value) return loading.value ? '正在检索标准条款...' : ''
  if (answer.value.refused) return ''
  return answer.value.answer || '未生成回答，请查看右侧引用来源。'
})

function setSourceRef(el: Element | ComponentPublicInstance | null, index: number) {
  sourceCardRefs.value[index] = el instanceof HTMLElement ? el : null
}

function previewText(text?: string): string {
  if (!text) return '暂无段落内容'
  const normalized = text.replace(/\s+/g, ' ').trim()
  return normalized.length > 96 ? `${normalized.slice(0, 96)}...` : normalized
}

async function handleQuery() {
  const query = form.query?.trim()
  if (!query) {
    ElMessage.warning('请输入检索问题')
    return
  }
  loading.value = true
  submittedQuery.value = query
  try {
    const payload: StandardRagQuery = {
      ...form,
      query,
      sourceTypes: (form.sourceTypes || []).filter((item) => item)
    }
    answer.value = await queryStandardRag(payload)
    activeSourceIndex.value = 0
    sourceCardRefs.value = []
    await nextTick()
    focusSource(0, false)
  } finally {
    loading.value = false
  }
}

function focusSource(index: number, scroll = true) {
  activeSourceIndex.value = index
  if (!scroll) return
  nextTick(() => {
    sourceCardRefs.value[index]?.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
  })
}

function confidenceType(label?: string) {
  if (label === 'HIGH') return 'success'
  if (label === 'LOW') return 'danger'
  return 'warning'
}

async function loadFilterOptions() {
  try {
    if (!dictStore.loaded) {
      await dictStore.loadAll()
    }
    const customers = await dictStore.refreshItems('QC_CUSTOMER')
    customerOptions.value = customers
  } catch {
    customerOptions.value = dictStore.getItems('QC_CUSTOMER')
  }
}

onMounted(() => {
  loadFilterOptions()
})
</script>

<style scoped>
.standard-rag-page {
  padding: 16px;
  min-height: calc(100vh - 120px);
}

.page-header {
  margin-bottom: 12px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.page-desc {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-muted);
}

.rag-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(320px, 420px);
  gap: 12px;
  height: calc(100vh - 168px);
  min-height: 480px;
}

.chat-panel,
.source-panel {
  background: var(--bg-panel);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  min-height: 0;
}

.chat-panel {
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

.chat-main {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  overflow: hidden;
}

.chat-main.is-empty {
  justify-content: space-between;
}

.chat-main.is-empty .chat-messages {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-messages {
  flex: 0 1 auto;
  max-height: calc(100% - 132px);
  overflow-y: auto;
  padding: 12px 14px 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.chat-item {
  display: flex;
  flex-shrink: 0;
}

.user-item {
  justify-content: flex-end;
}

.ai-item {
  justify-content: flex-start;
}

.chat-bubble {
  max-width: 92%;
  border-radius: 4px;
  padding: 12px 14px;
}

.user-bubble {
  background: rgba(0, 212, 255, 0.12);
  border: 1px solid rgba(0, 212, 255, 0.28);
  color: var(--text-primary);
  line-height: 1.65;
  white-space: pre-wrap;
  word-break: break-word;
}

.ai-bubble {
  width: 100%;
  max-width: 100%;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
}

.ai-head {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}

.refusal-alert {
  margin-bottom: 8px;
}

.answer-text {
  color: var(--text-primary);
  line-height: 1.75;
  white-space: pre-wrap;
  word-break: break-word;
}

.degrade-text {
  margin-top: 8px;
  font-size: 12px;
  color: var(--text-muted);
}

.citation-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid var(--border-color);
}

.citation-tag {
  border: 1px solid rgba(0, 212, 255, 0.35);
  background: rgba(0, 212, 255, 0.06);
  color: var(--cyan);
  border-radius: 3px;
  padding: 4px 8px;
  font-size: 11px;
  line-height: 1.4;
  cursor: pointer;
  font-family: var(--font-data);
}

.citation-tag:hover,
.citation-tag.active {
  border-color: var(--cyan);
  background: rgba(0, 212, 255, 0.14);
}

.chat-input {
  flex-shrink: 0;
  margin-top: 8px;
  border-top: 1px solid var(--border-color);
  padding: 10px 12px 12px;
  background: var(--bg-panel);
}

.input-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}

.source-type-select {
  width: 180px;
}

.filter-select {
  width: 140px;
}

.filter-select.short {
  width: 120px;
}

.input-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  align-items: stretch;
}

.search-btn {
  min-width: 72px;
  height: auto;
}

.input-hint {
  margin-top: 8px;
  font-size: 11px;
  color: var(--text-muted);
}

.source-panel {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.source-panel-head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 14px;
  border-bottom: 1px solid var(--border-color);
}

.source-panel-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.source-count {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 10px;
  background: var(--red, #ff4757);
  color: #fff;
  font-size: 11px;
  line-height: 20px;
  text-align: center;
  font-family: var(--font-data);
}

.source-list {
  flex: 1;
  overflow: auto;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.source-card {
  padding: 10px 12px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background: var(--bg-card);
  cursor: pointer;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.source-card.active {
  border-color: rgba(0, 212, 255, 0.45);
  box-shadow: inset 0 0 0 1px rgba(0, 212, 255, 0.12);
}

.source-card-head {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
}

.source-index,
.mono {
  font-family: var(--font-data);
}

.source-index {
  color: var(--cyan);
  font-size: 12px;
  font-weight: 600;
}

.source-name {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-primary);
}

.source-clause {
  margin-top: 4px;
  font-size: 11px;
  color: var(--text-muted);
}

.source-excerpt {
  margin-top: 8px;
  padding: 10px;
  border-radius: 3px;
  background: rgba(255, 180, 0, 0.12);
  border: 1px solid rgba(255, 180, 0, 0.28);
  color: var(--text-primary);
  font-size: 12px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.source-excerpt :deep(.rag-highlight) {
  background: rgba(255, 180, 0, 0.45);
  color: inherit;
  padding: 0 2px;
  border-radius: 2px;
}

.source-preview {
  margin-top: 8px;
  font-size: 12px;
  line-height: 1.6;
  color: var(--text-secondary);
}

.source-foot {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
  font-size: 11px;
  color: var(--text-muted);
}

.file-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

@media (max-width: 1100px) {
  .rag-layout {
    grid-template-columns: 1fr;
    height: auto;
    min-height: 0;
  }

  .chat-panel {
    min-height: 420px;
  }

  .source-list {
    max-height: 360px;
  }
}
</style>
