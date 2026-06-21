<template>
  <div class="standard-rag-page">
    <el-card class="toolbar-card" shadow="never">
      <el-form :model="form" inline>
        <el-form-item label="问题">
          <el-input
            v-model="form.query"
            class="query-input"
            clearable
            placeholder="输入标准、客户协议或指标问题"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="来源">
          <el-select v-model="form.sourceTypes" multiple collapse-tags clearable style="width: 190px">
            <el-option label="国标" value="NATIONAL" />
            <el-option label="企标" value="ENTERPRISE" />
            <el-option label="客户协议" value="CUSTOMER" />
            <el-option label="案例/投诉" value="COMPLAINT" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户">
          <el-input v-model="form.customerId" clearable style="width: 130px" />
        </el-form-item>
        <el-form-item label="品种">
          <el-input v-model="form.variety" clearable style="width: 120px" />
        </el-form-item>
        <el-form-item label="牌号">
          <el-input v-model="form.grade" clearable style="width: 120px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" :loading="loading" @click="handleQuery">检索</el-button>
          <el-button @click="handleReset">清空</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="result-grid">
      <el-card class="answer-panel" shadow="never" v-loading="loading">
        <template #header>
          <div class="panel-head">
            <span>回答</span>
            <div class="tag-row" v-if="answer">
              <el-tag size="small" :type="confidenceType(answer.confidenceLabel)">
                {{ answer.confidenceLabel || 'UNKNOWN' }}
              </el-tag>
              <el-tag size="small" type="info">{{ answer.degradationSource || 'N/A' }}</el-tag>
              <el-tag v-if="answer.cacheHit" size="small" type="warning">CACHE</el-tag>
            </div>
          </div>
        </template>
        <el-alert
          v-if="answer?.refused"
          type="warning"
          :closable="false"
          :title="answer.refusalReason || '未找到可回答依据'"
          show-icon
        />
        <div class="answer-text">{{ answer?.answer || '输入问题后检索标准/协议来源。' }}</div>
        <div v-if="answer?.degradationReason" class="degrade-text">{{ answer.degradationReason }}</div>
      </el-card>

      <el-card class="source-panel" shadow="never">
        <template #header>
          <div class="panel-head">
            <span>来源段落</span>
            <span class="count">{{ sources.length }}</span>
          </div>
        </template>
        <el-empty v-if="!sources.length" description="暂无来源" />
        <div v-else class="source-list">
          <div v-for="item in sources" :key="item.clauseId || item.clauseNo" class="source-item">
            <div class="source-meta">
              <el-tag size="small" :type="item.referenceOnly ? 'warning' : 'success'">
                {{ item.referenceOnly ? '参考' : '命中' }}
              </el-tag>
              <span class="mono">{{ item.standardCode || '-' }}</span>
              <span>{{ item.clauseNo || '-' }}</span>
              <span v-if="item.score != null" class="mono">{{ Number(item.score).toFixed(3) }}</span>
            </div>
            <p>{{ item.paragraphText }}</p>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { queryStandardRag, type StandardRagAnswer, type StandardRagQuery } from '@/api/standard-rag'

const loading = ref(false)
const answer = ref<StandardRagAnswer | null>(null)
const form = reactive<StandardRagQuery>({
  query: '',
  sourceTypes: [],
  topK: 6
})

const sources = computed(() => answer.value?.sources || [])

async function handleQuery() {
  if (!form.query?.trim()) {
    ElMessage.warning('请输入检索问题')
    return
  }
  loading.value = true
  try {
    answer.value = await queryStandardRag({ ...form, query: form.query.trim() })
  } finally {
    loading.value = false
  }
}

function handleReset() {
  form.query = ''
  form.sourceTypes = []
  form.customerId = ''
  form.variety = ''
  form.grade = ''
  answer.value = null
}

function confidenceType(label?: string) {
  if (label === 'HIGH') return 'success'
  if (label === 'LOW') return 'danger'
  return 'warning'
}
</script>

<style scoped>
.standard-rag-page {
  padding: 16px;
}

.toolbar-card,
.answer-panel,
.source-panel {
  background: var(--bg-panel);
  border-color: var(--border-color);
}

.query-input {
  width: min(520px, 42vw);
}

.result-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 440px;
  gap: 12px;
  margin-top: 12px;
}

.panel-head,
.tag-row,
.source-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.panel-head {
  justify-content: space-between;
}

.answer-text {
  min-height: 180px;
  white-space: pre-wrap;
  line-height: 1.7;
  color: var(--text-primary);
}

.degrade-text,
.count {
  color: var(--text-secondary);
  font-size: 12px;
}

.source-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 560px;
  overflow: auto;
}

.source-item {
  padding: 10px;
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  border-radius: 4px;
}

.source-item p {
  margin: 8px 0 0;
  color: var(--text-regular);
  line-height: 1.55;
}

.mono {
  font-family: var(--font-data);
}

@media (max-width: 1100px) {
  .result-grid {
    grid-template-columns: 1fr;
  }
  .query-input {
    width: 100%;
  }
}
</style>
