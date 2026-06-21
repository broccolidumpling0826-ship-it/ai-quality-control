<template>
  <div class="ai-assessment-page" v-loading="pageLoading">
    <el-page-header @back="router.back()" content="AI 让步风险评估" style="margin-bottom:16px" />

    <el-card shadow="never" class="search-card">
      <el-form :model="form" inline @submit.prevent="runAssessment">
        <el-form-item label="判定ID">
          <el-input v-model="form.judgmentId" placeholder="判定结论 ID" clearable style="width:220px" />
        </el-form-item>
        <el-form-item label="让步申请ID">
          <el-input v-model="form.concessionId" placeholder="可选" clearable style="width:200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="assessLoading" @click="runAssessment">生成评估</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <template v-if="assessment">
      <div class="status-bar">
        <el-tag
          :type="riskTagType(assessment.riskLevel)"
          size="large"
          effect="dark"
        >
          风险等级：{{ riskLabel(assessment.riskLevel) }}
        </el-tag>
        <el-tag
          v-if="assessment.confidenceLevel"
          :type="confidenceTagType(assessment.confidenceLevel)"
          size="large"
        >
          置信度：{{ confidenceLabel(assessment.confidenceLevel) }}
        </el-tag>
        <el-tag v-if="assessment.degraded" type="warning" size="large" effect="plain">
          降级模式（规则 Checklist）
        </el-tag>
      </div>

      <el-row :gutter="16">
        <el-col :xs="24" :lg="12">
          <el-card shadow="never" class="content-card">
            <template #header><span style="font-weight:600">客户影响分析</span></template>
            <p class="block-text">{{ assessment.customerImpact || '暂无分析' }}</p>
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="12">
          <el-card shadow="never" class="content-card">
            <template #header><span style="font-weight:600">建议接收条件</span></template>
            <p class="block-text">{{ assessment.suggestedConditions || '暂无建议' }}</p>
          </el-card>
        </el-col>
      </el-row>

      <el-card v-if="historicalCases.length" shadow="never" class="content-card">
        <template #header><span style="font-weight:600">历史类似案例</span></template>
        <el-table :data="historicalCases" border size="small">
          <el-table-column prop="batchNo" label="批次号" width="140" />
          <el-table-column prop="riskLevel" label="风险等级" width="100">
            <template #default="{ row }">
              <el-tag :type="riskTagType(row.riskLevel)" size="small">{{ riskLabel(row.riskLevel) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="summary" label="案例摘要" show-overflow-tooltip />
        </el-table>
      </el-card>

      <div class="bottom-bar">
        <el-button @click="router.back()">返回</el-button>
        <el-button
          v-if="assessment.auditLogId"
          type="info"
          plain
          @click="router.push(`/ai-audit?auditLogId=${assessment.auditLogId}`)"
        >查看审计</el-button>
      </div>
    </template>

    <el-empty v-else-if="!pageLoading && !assessLoading" description="输入判定 ID 后点击「生成评估」" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  assessConcession,
  type ConcessionAssessment,
  type HistoricalCase
} from '@/api/ai-concession'
import type { ConfidenceLevel } from '@/api/ai-judgment'

const route = useRoute()
const router = useRouter()

const pageLoading = ref(false)
const assessLoading = ref(false)
const assessment = ref<ConcessionAssessment | null>(null)

const form = reactive({
  judgmentId: (route.query.judgmentId as string) || '',
  concessionId: (route.query.concessionId as string) || ''
})

const historicalCases = computed<HistoricalCase[]>(() => {
  const raw = assessment.value?.historicalCases
  if (!raw) return []
  if (Array.isArray(raw)) return raw
  try {
    return JSON.parse(raw as string)
  } catch {
    return []
  }
})

function riskLabel(level?: string) {
  const map: Record<string, string> = { LOW: '低', MEDIUM: '中', HIGH: '高' }
  return map[level || ''] || level || '—'
}

function riskTagType(level?: string) {
  const map: Record<string, string> = { LOW: 'success', MEDIUM: 'warning', HIGH: 'danger' }
  return map[level || ''] || 'info'
}

function confidenceLabel(level?: ConfidenceLevel) {
  const map: Record<string, string> = { HIGH: '高', MEDIUM: '中', LOW: '低' }
  return map[level || ''] || level || '—'
}

function confidenceTagType(level?: ConfidenceLevel) {
  const map: Record<string, string> = { HIGH: 'success', MEDIUM: 'warning', LOW: 'danger' }
  return map[level || ''] || 'info'
}

async function runAssessment() {
  if (!form.judgmentId.trim()) {
    ElMessage.warning('请输入判定 ID')
    return
  }
  assessLoading.value = true
  try {
    assessment.value = await assessConcession({
      judgmentId: form.judgmentId.trim(),
      concessionId: form.concessionId.trim() || undefined
    })
  } finally {
    assessLoading.value = false
  }
}

onMounted(() => {
  if (form.judgmentId) {
    runAssessment()
  }
})
</script>

<style scoped>
.ai-assessment-page {
  padding: 16px;
  max-width: 1000px;
}

.search-card {
  margin-bottom: 16px;
  border-radius: 12px;
}

.search-card :deep(.el-card__body) {
  padding: 16px 16px 0;
}

.status-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}

.content-card {
  border-radius: 12px;
  margin-bottom: 12px;
}

.block-text {
  font-size: 14px;
  line-height: 1.8;
  color: var(--text-heading);
  white-space: pre-wrap;
}

.bottom-bar {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
