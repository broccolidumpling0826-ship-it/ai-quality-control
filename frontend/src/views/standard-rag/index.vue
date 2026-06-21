<template>
  <div class="standard-rag-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">标准 RAG 检索</h2>
        <p class="page-desc">自然语言检索标准条款，回答附带可追溯引用来源</p>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :xs="24" :lg="14">
        <el-card shadow="never" class="content-card">
          <template #header>
            <div class="card-title">
              <el-icon class="title-icon"><ChatDotRound /></el-icon>
              <span>智能问答</span>
            </div>
          </template>

          <div class="chat-area" ref="chatAreaRef">
            <div v-if="messages.length === 0" class="chat-empty">
              <el-empty description="输入问题开始检索标准条款" :image-size="80" />
            </div>
            <div
              v-for="(msg, idx) in messages"
              :key="idx"
              class="chat-item"
              :class="msg.role"
            >
              <div class="chat-bubble">
                <div v-if="msg.degraded" class="degraded-tag">
                  <el-tag type="warning" size="small" effect="dark">降级模式</el-tag>
                </div>
                <p class="chat-text">{{ msg.content }}</p>
                <div v-if="msg.citations?.length" class="citation-chips">
                  <el-tag
                    v-for="(c, ci) in msg.citations"
                    :key="ci"
                    size="small"
                    effect="plain"
                    class="citation-chip"
                    @click="selectCitation(c, ci)"
                  >
                    [{{ ci + 1 }}] {{ c.standardName || c.sectionRef || '引用' }}
                  </el-tag>
                </div>
              </div>
            </div>
            <div v-if="queryLoading" class="chat-item assistant">
              <div class="chat-bubble loading-bubble">
                <el-icon class="is-loading"><Loading /></el-icon>
                正在检索标准库…
              </div>
            </div>
          </div>

          <div class="input-area">
            <el-form :model="queryForm" inline @submit.prevent="handleQuery">
              <el-form-item label="标准类型" style="margin-bottom:8px">
                <el-select v-model="queryForm.standardType" placeholder="全部" clearable style="width:130px">
                  <el-option
                    v-for="item in dictStore.getItems('STANDARD_TYPE')"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-form>
            <div class="input-row">
              <el-input
                v-model="queryForm.question"
                type="textarea"
                :rows="2"
                placeholder="例如：Q235B 抗拉强度下限是多少？"
                @keydown.ctrl.enter="handleQuery"
              />
              <el-button type="primary" :loading="queryLoading" @click="handleQuery">检索</el-button>
            </div>
            <p class="input-hint">Ctrl + Enter 发送 · 无命中时将拒答，不会虚构数值</p>
          </div>
        </el-card>

        <el-card shadow="never" class="content-card ingest-card">
          <template #header>
            <div class="card-title">
              <el-icon class="title-icon"><Upload /></el-icon>
              <span>文档入库</span>
            </div>
          </template>
          <el-form :model="ingestForm" label-width="80px" size="small">
            <el-form-item label="标准 ID">
              <el-input v-model="ingestForm.standardId" placeholder="如 STD-001" clearable />
            </el-form-item>
            <el-form-item label="文件路径">
              <el-input v-model="ingestForm.filePath" placeholder="服务端文件路径，如 seed-documents/gb-t-700.md" clearable />
            </el-form-item>
            <el-form-item label="显示名称">
              <el-input v-model="ingestForm.fileName" placeholder="留空则取路径文件名" clearable />
            </el-form-item>
            <el-form-item>
              <el-button
                type="success"
                :loading="ingestLoading"
                :disabled="!ingestForm.standardId || !ingestForm.filePath"
                @click="handleIngest"
              >
                解析入库
              </el-button>
            </el-form-item>
          </el-form>
          <el-alert
            type="info"
            :closable="false"
            show-icon
            title="支持 PDF / TXT / MD，文件需预先上传至服务器指定目录"
            style="margin-top:4px"
          />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="10">
        <el-card shadow="never" class="content-card citation-panel">
          <template #header>
            <div class="card-title">
              <el-icon class="title-icon"><Document /></el-icon>
              <span>引用溯源</span>
              <el-badge v-if="activeCitations.length" :value="activeCitations.length" class="badge" />
            </div>
          </template>

          <div v-if="!selectedCitation" class="citation-empty">
            <el-empty description="点击回答中的引用标签查看原文高亮" :image-size="60" />
          </div>
          <template v-else>
            <div class="citation-meta">
              <p class="citation-standard">{{ selectedCitation.standardName || '标准文档' }}</p>
              <p v-if="selectedCitation.sectionRef" class="citation-section">{{ selectedCitation.sectionRef }}</p>
            </div>
            <div class="citation-highlight">
              <mark>{{ selectedCitation.highlightText }}</mark>
            </div>
            <el-divider />
            <div class="citation-list">
              <div
                v-for="(c, i) in activeCitations"
                :key="i"
                class="citation-list-item"
                :class="{ active: selectedCitation === c }"
                @click="selectedCitation = c"
              >
                <span class="citation-index">[{{ i + 1 }}]</span>
                <span class="citation-snippet">{{ truncate(c.highlightText, 80) }}</span>
              </div>
            </div>
          </template>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound, Document, Upload, Loading
} from '@element-plus/icons-vue'
import { useDictStore } from '@/store/dict'
import { queryRag, ingestDocument, type Citation, type RagQueryResult } from '@/api/rag'

const dictStore = useDictStore()

interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  citations?: Citation[]
  degraded?: boolean
}

const queryForm = reactive({
  question: '',
  standardType: ''
})

const messages = ref<ChatMessage[]>([])
const queryLoading = ref(false)
const chatAreaRef = ref<HTMLElement>()

const activeCitations = ref<Citation[]>([])
const selectedCitation = ref<Citation | null>(null)

const ingestForm = reactive({ standardId: '', filePath: '', fileName: '' })
const ingestLoading = ref(false)

function truncate(text: string, len: number) {
  if (!text) return ''
  return text.length > len ? text.slice(0, len) + '…' : text
}

function selectCitation(c: Citation, _idx: number) {
  selectedCitation.value = c
}

async function scrollToBottom() {
  await nextTick()
  if (chatAreaRef.value) {
    chatAreaRef.value.scrollTop = chatAreaRef.value.scrollHeight
  }
}

async function handleQuery() {
  const q = queryForm.question.trim()
  if (!q) {
    ElMessage.warning('请输入检索问题')
    return
  }

  messages.value.push({ role: 'user', content: q })
  queryForm.question = ''
  queryLoading.value = true
  await scrollToBottom()

  try {
    const res: RagQueryResult = await queryRag({
      question: q,
      standardType: queryForm.standardType || undefined
    })
    const content = res.found
      ? (res.answer || '未找到有效回答')
      : '未找到相关信息，无法给出结论'
    messages.value.push({
      role: 'assistant',
      content,
      citations: res.citations,
      degraded: res.degraded
    })
    activeCitations.value = res.citations || []
    selectedCitation.value = activeCitations.value[0] || null
  } catch {
    messages.value.push({
      role: 'assistant',
      content: '检索服务暂不可用，请稍后重试'
    })
  } finally {
    queryLoading.value = false
    scrollToBottom()
  }
}

async function handleIngest() {
  if (!ingestForm.standardId || !ingestForm.filePath) return
  ingestLoading.value = true
  try {
    const res = await ingestDocument({
      standardId: ingestForm.standardId,
      filePath: ingestForm.filePath,
      fileName: ingestForm.fileName || undefined
    })
    ElMessage.success(`文档入库成功，共 ${res.chunkCount ?? 0} 个段落`)
    ingestForm.filePath = ''
    ingestForm.fileName = ''
  } finally {
    ingestLoading.value = false
  }
}
</script>

<style scoped>
.standard-rag-page {
  padding: 16px;
  min-height: calc(100vh - 100px);
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

.content-card {
  border-radius: 12px;
  margin-bottom: 16px;
}

.content-card :deep(.el-card__header) {
  padding: 14px 20px;
  border-bottom: 1px solid var(--border);
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

.chat-area {
  min-height: 320px;
  max-height: 420px;
  overflow-y: auto;
  padding: 12px 4px;
  margin-bottom: 16px;
}

.chat-item {
  display: flex;
  margin-bottom: 12px;
}

.chat-item.user {
  justify-content: flex-end;
}

.chat-item.assistant {
  justify-content: flex-start;
}

.chat-bubble {
  max-width: 85%;
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
}

.chat-item.user .chat-bubble {
  background: rgba(61, 158, 255, 0.15);
  color: var(--text-heading);
  border: 1px solid rgba(61, 158, 255, 0.3);
}

.chat-item.assistant .chat-bubble {
  background: var(--bg-card);
  border: 1px solid var(--border);
  color: var(--text-heading);
}

.loading-bubble {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
}

.degraded-tag {
  margin-bottom: 8px;
}

.citation-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}

.citation-chip {
  cursor: pointer;
}

.citation-chip:hover {
  border-color: var(--cyan);
  color: var(--cyan);
}

.input-area {
  border-top: 1px solid var(--border);
  padding-top: 12px;
}

.input-row {
  display: flex;
  gap: 10px;
  align-items: flex-end;
}

.input-row .el-textarea {
  flex: 1;
}

.input-hint {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 6px;
}

.citation-panel {
  position: sticky;
  top: 16px;
}

.citation-empty {
  padding: 40px 0;
}

.citation-meta {
  margin-bottom: 12px;
}

.citation-standard {
  font-weight: 600;
  font-size: 15px;
  color: var(--text-heading);
}

.citation-section {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.citation-highlight {
  padding: 16px;
  background: rgba(255, 193, 7, 0.1);
  border-left: 4px solid var(--orange);
  border-radius: 6px;
  font-size: 14px;
  line-height: 1.7;
  color: var(--text-heading);
}

.citation-highlight mark {
  background: rgba(255, 193, 7, 0.35);
  padding: 2px 0;
}

.citation-list-item {
  display: flex;
  gap: 8px;
  padding: 10px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  color: var(--text-secondary);
  border: 1px solid transparent;
  margin-bottom: 6px;
}

.citation-list-item:hover,
.citation-list-item.active {
  background: rgba(0, 212, 255, 0.08);
  border-color: var(--cyan);
  color: var(--text-heading);
}

.citation-index {
  flex-shrink: 0;
  color: var(--cyan);
  font-weight: 600;
}

.citation-snippet {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
