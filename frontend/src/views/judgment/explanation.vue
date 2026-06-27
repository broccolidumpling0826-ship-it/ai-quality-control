<template>
  <div
    class="explanation-page page-list-full"
    :class="{ 'is-ai-loading': loading }"
    v-loading="loading"
    :element-loading-text="AI_LOADING_TEXT.judgmentExplanation"
    :element-loading-custom-class="AI_LOADING_CLASS"
  >
    <!-- 顶部英雄区 -->
    <el-card shadow="never" class="hero-card" v-if="detail">
      <div class="hero-content">
        <div class="hero-left">
          <div class="hero-coil-meta">
            卷号：<strong>{{ detail.coilNo }}</strong>
            &nbsp;&nbsp;批次号：<strong>{{ detail.batchNo }}</strong>
          </div>
          <el-tag
            :type="judgmentColor(detail.judgmentType)"
            class="judgment-badge"
            size="large"
          >
            {{ dictStore.getLabel('JUDGMENT_TYPE', detail.judgmentType) || detail.judgmentType }}
          </el-tag>
        </div>
        <el-descriptions :column="3" class="hero-meta">
          <el-descriptions-item label="炉号">{{ detail.heatNo }}</el-descriptions-item>
          <el-descriptions-item label="检验人">{{ detail.inspector }}</el-descriptions-item>
          <el-descriptions-item label="判定时间">{{ detail.judgeTime }}</el-descriptions-item>
          <el-descriptions-item label="品种">{{ dictStore.getLabel('PRODUCT_VARIETY', detail.productVariety) }}</el-descriptions-item>
          <el-descriptions-item label="牌号">{{ detail.productGrade }}</el-descriptions-item>
          <el-descriptions-item label="规格">{{ detail.specification }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <!-- 标准匹配过程 -->
    <div class="standard-match-row" v-if="detail">
      <el-card
        v-for="match in detail.standardMatches"
        :key="match.standardType"
        :class="['match-card', match.hit ? 'match-hit' : 'match-skip']"
        shadow="hover"
      >
        <div class="match-type">
          {{ dictStore.getLabel('STANDARD_TYPE', match.standardType) || match.standardType }}
        </div>
        <div class="match-status">
          <el-icon v-if="match.hit" color="#409eff" :size="24"><CircleCheckFilled /></el-icon>
          <el-icon v-else color="var(--text-muted)" :size="24"><CircleCloseFilled /></el-icon>
          <span :class="match.hit ? 'status-hit' : 'status-skip'">
            {{ match.hit ? '命中' : '跳过' }}
          </span>
        </div>
        <div v-if="match.standardName" class="match-name">{{ match.standardName }}</div>
        <div v-if="match.skipReason" class="match-reason">{{ match.skipReason }}</div>
      </el-card>

      <!-- 默认占位：当 standardMatches 为空时 -->
      <template v-if="!detail.standardMatches || detail.standardMatches.length === 0">
        <el-card
          v-for="type in ['CUSTOMER', 'ENTERPRISE', 'NATIONAL']"
          :key="type"
          class="match-card match-skip"
          shadow="hover"
        >
          <div class="match-type">{{ stdTypeLabel(type) }}</div>
          <div class="match-status">
            <el-icon color="var(--text-muted)" :size="24"><CircleCloseFilled /></el-icon>
            <span class="status-skip">-</span>
          </div>
        </el-card>
      </template>
    </div>

    <el-card shadow="never" class="ai-card" v-if="detail">
      <template #header>
        <div class="ai-head">
          <span>AI 判定解释</span>
          <div class="ai-tags">
            <el-tag :type="confidenceType(detail.confidenceLabel)" size="small">
              {{ detail.confidenceLabel || 'N/A' }}
            </el-tag>
            <el-tag type="info" size="small">{{ detail.degradationSource || 'N/A' }}</el-tag>
            <el-tag v-if="detail.aiExplanationTrace" type="warning" size="small">
              {{ explanationTraceLabel(detail.aiExplanationTrace) }}
            </el-tag>
            <el-tag v-if="detail.citationMissing" type="warning" size="small">引用缺失</el-tag>
          </div>
        </div>
      </template>
      <el-alert
        v-if="detail.conflictWarnings?.length"
        type="warning"
        show-icon
        :closable="false"
        :title="detail.conflictWarnings.join('；')"
      />
      <p class="ai-text">
        <template v-for="(part, index) in explanationParts" :key="index">
          <span v-if="part.type === 'text'">{{ part.value }}</span>
          <span v-else class="citation-mark">[{{ part.index }}]</span>
        </template>
      </p>
      <div v-if="detail.confidenceFactors?.length" class="factor-row">
        <el-tag v-for="item in detail.confidenceFactors" :key="item" size="small" type="info">{{ item }}</el-tag>
      </div>
    </el-card>

    <el-card shadow="never" class="ai-card" v-if="detail?.candidateStandards?.length">
      <template #header><span style="font-weight:600">候选标准</span></template>
      <el-table :data="detail.candidateStandards" border size="small">
        <el-table-column prop="standardCode" label="标准编号" min-width="150" />
        <el-table-column prop="standardType" label="类型" width="110" />
        <el-table-column prop="versionNo" label="版本" width="90" />
        <el-table-column prop="specRange" label="规格范围" min-width="140" show-overflow-tooltip />
        <el-table-column label="状态" width="150">
          <template #default="{ row }">
            <el-tag v-if="row.selected" type="success" size="small">选中</el-tag>
            <el-tag v-else-if="row.conflict" type="danger" size="small">冲突</el-tag>
            <el-tag v-else-if="row.suppressed" type="warning" size="small">被抑制</el-tag>
            <el-tag v-else size="small">候选</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="220" show-overflow-tooltip />
      </el-table>
    </el-card>

    <el-card shadow="never" class="ai-card" v-if="detail?.conflicts?.length || detail?.citations?.length">
      <el-tabs>
        <el-tab-pane label="标准冲突">
          <el-table :data="detail.conflicts || []" border size="small">
            <el-table-column prop="conflictType" label="类型" width="140" />
            <el-table-column prop="conflictLevel" label="级别" width="160" />
            <el-table-column prop="status" label="状态" width="110" />
            <el-table-column prop="indicatorName" label="指标" min-width="120" />
            <el-table-column prop="involvedStandardIds" label="涉及标准" min-width="220">
              <template #default="{ row }">{{ row.involvedStandardIds?.join(' / ') }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="来源引用">
          <el-table :data="detail.citations || []" border size="small" class="citation-table">
            <el-table-column label="引用" width="72" align="center" fixed>
              <template #default="{ $index }">
                <span class="citation-index">[{{ $index + 1 }}]</span>
              </template>
            </el-table-column>
            <el-table-column prop="standardCode" label="标准/协议" min-width="150" />
            <el-table-column prop="clauseNo" label="条款" width="100" />
            <el-table-column prop="pageNo" label="页码" width="80" />
            <el-table-column prop="paragraphText" label="段落" min-width="360" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 指标明细 -->
    <el-card shadow="never" style="margin-top:12px" v-if="detail">
      <template #header><span style="font-weight:600">指标明细</span></template>
      <el-table :data="detail.indicatorDetails" border style="width:100%">
        <el-table-column
          prop="indicatorName"
          label="指标名称"
          min-width="130"
          :filters="getIndicatorFilters('indicatorName')"
          :filter-method="indicatorFilterMethod"
          filter-placement="bottom-start"
        >
          <template #default="{ row }">
            <span :style="row.noStandard ? 'color:#e6a23c;font-weight:500' : ''">
              {{ row.indicatorName }}
            </span>
          </template>
        </el-table-column>
        <el-table-column
          prop="measuredValue"
          label="实测值"
          width="100"
          align="center"
          :filters="getIndicatorFilters('measuredValue')"
          :filter-method="indicatorFilterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column prop="lowerLimit" label="标准下限" width="100" align="center">
          <template #default="{ row }">{{ row.lowerLimit ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="upperLimit" label="标准上限" width="100" align="center">
          <template #default="{ row }">{{ row.upperLimit ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="让步下限" width="90" align="center">
          <template #default="{ row }">{{ row.concessionLower ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="让步上限" width="90" align="center">
          <template #default="{ row }">{{ row.concessionUpper ?? '-' }}</template>
        </el-table-column>
        <el-table-column
          prop="deviation"
          label="偏差值"
          width="100"
          align="center"
          :filters="getIndicatorFilters('deviation')"
          :filter-method="indicatorFilterMethod"
          filter-placement="bottom-start"
        >
          <template #default="{ row }">
            <span :class="deviationClass(row.deviation)">{{ row.deviation ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="triggeredRule"
          label="触发规则"
          min-width="150"
          :filters="getIndicatorFilters('triggeredRule')"
          :filter-method="indicatorFilterMethod"
          filter-placement="bottom-start"
        >
          <template #default="{ row }">
            <span class="rule-cell">{{ row.triggeredRule || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="判断结论" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.noStandard" type="warning" size="small">无标准覆盖</el-tag>
            <el-tag v-else :type="indicatorResultColor(row.indicatorResult)" size="small">
              {{ indicatorResultLabel(row.indicatorResult) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 底部操作 -->
    <div class="bottom-bar" v-if="detail">
      <el-button @click="router.back()">返回</el-button>
      <el-button type="warning" plain @click="openAdvice('reinspection')">AI复检建议</el-button>
      <el-button type="primary" plain @click="openAdvice('rejudgment')">AI改判建议</el-button>
      <el-button
        v-if="canInitiateReinspection"
        type="warning"
        @click="handleReinspection"
      >发起复检</el-button>
      <el-button
        v-if="canApplyRejudgment"
        type="primary"
        @click="handleRejudgment"
      >发起改判</el-button>
      <el-button
        v-if="canApplyConcession"
        type="success"
        @click="handleConcession"
      >发起让步申请</el-button>
    </div>

    <!-- 发起复检弹窗 -->
    <el-dialog v-model="reinspectionDialogVisible" title="发起复检" width="440px">
      <el-form ref="reinspectionFormRef" :model="reinspectionForm" :rules="reinspectionRules" label-width="80px">
        <el-form-item label="复检原因" prop="reinspectionReason">
          <el-input v-model="reinspectionForm.reinspectionReason" type="textarea" :rows="3" placeholder="请输入复检原因" />
        </el-form-item>
        <el-form-item label="责任人工号" prop="responsibleNo">
          <el-input v-model="reinspectionForm.responsibleNo" placeholder="责任人工号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reinspectionDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionLoading" @click="confirmReinspection">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="adviceDialogVisible"
      :title="adviceTitle"
      width="760px"
      destroy-on-close
      :close-on-click-modal="!adviceLoading"
      :close-on-press-escape="!adviceLoading"
      :show-close="!adviceLoading"
    >
      <div
        v-loading="adviceLoading"
        class="advice-dialog-body"
        :element-loading-text="adviceLoadingText"
        :element-loading-custom-class="AI_LOADING_CLASS"
      >
        <template v-if="!adviceLoading && advice">
          <el-alert
            v-if="advice.withheld"
            type="warning"
            :closable="false"
            show-icon
            class="advice-alert"
            :title="advice.suggestedReason"
          />
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="建议动作">{{ advice.recommendedAction || '-' }}</el-descriptions-item>
            <el-descriptions-item label="置信度">
              <el-tag :type="confidenceType(advice.confidenceLabel)" size="small">{{ advice.confidenceLabel }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="目标结论">{{ advice.targetJudgmentType || '-' }}</el-descriptions-item>
            <el-descriptions-item label="人工复核">{{ advice.mustManualReview ? '是' : '否' }}</el-descriptions-item>
            <el-descriptions-item label="影响范围" :span="2">{{ advice.affectedScope || '-' }}</el-descriptions-item>
            <el-descriptions-item label="证据摘要" :span="2">{{ advice.evidenceSummary || '-' }}</el-descriptions-item>
            <el-descriptions-item label="建议原因" :span="2">{{ advice.suggestedReason || '-' }}</el-descriptions-item>
          </el-descriptions>

          <div v-if="advice.triggerIndicators?.length" class="advice-tags">
            <span class="advice-tags-label">触发指标</span>
            <el-tag v-for="item in advice.triggerIndicators" :key="item" type="warning" size="small">{{ item }}</el-tag>
          </div>
          <div v-if="advice.missingInfo?.length" class="advice-tags">
            <span class="advice-tags-label">缺失信息</span>
            <el-tag v-for="item in advice.missingInfo" :key="item" type="info" size="small">{{ item }}</el-tag>
          </div>

          <el-table v-if="advice.evidenceRefs?.length" :data="advice.evidenceRefs" border size="small" class="advice-table">
            <el-table-column prop="standardCode" label="来源" min-width="150" />
            <el-table-column prop="clauseNo" label="条款" width="90" />
            <el-table-column prop="paragraphText" label="段落" min-width="320" show-overflow-tooltip />
          </el-table>
        </template>
      </div>
      <template #footer>
        <el-button :disabled="adviceLoading" @click="adviceDialogVisible = false">关闭</el-button>
        <el-button :loading="adviceActionLoading" :disabled="adviceLoading || !advice" @click="ignoreAdvice">忽略建议</el-button>
        <el-button type="primary" :disabled="adviceLoading || advice?.withheld" @click="adoptAdvice">采纳并预填</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { CircleCheckFilled, CircleCloseFilled } from '@element-plus/icons-vue'
import { useDictStore } from '@/store/dict'
import { useTableFilter } from '@/composables/use-table-filter'
import { useCitationParts } from '@/composables/use-citation-parts'
import { getJudgmentExplanation, getJudgmentByRecord } from '@/api/judgment'
import { getReinspectionAdvice, initiateReinspection, type WorkflowAdvice } from '@/api/reinspection'
import { getRejudgmentAdvice } from '@/api/rejudgment'
import { handleAiAssessment } from '@/api/ai-assessment'
import { AI_LOADING_TEXT, AI_LOADING_CLASS } from '@/constants/ai-loading-text'

const route = useRoute()
const router = useRouter()
const dictStore = useDictStore()

const loading = ref(false)
const detail = ref<any>(null)
const actionLoading = ref(false)
const indicatorDetails = computed(() => detail.value?.indicatorDetails ?? [])
const { getFilters: getIndicatorFilters, filterMethod: indicatorFilterMethod } = useTableFilter(indicatorDetails)

const explanationParts = useCitationParts(
  () => detail.value?.aiExplanation || detail.value?.ruleExplanation || '暂无解释',
  '暂无解释'
)

const reinspectionDialogVisible = ref(false)
const reinspectionFormRef = ref<FormInstance>()
const reinspectionForm = ref({ reinspectionReason: '', responsibleNo: '' })
const reinspectionRules = {
  reinspectionReason: [{ required: true, message: '请输入复检原因', trigger: 'blur' }]
}
const adviceDialogVisible = ref(false)
const adviceLoading = ref(false)
const adviceActionLoading = ref(false)
const advice = ref<WorkflowAdvice | null>(null)
const adviceKind = ref<'reinspection' | 'rejudgment'>('reinspection')

const adviceTitle = computed(() => adviceKind.value === 'reinspection' ? 'AI复检建议' : 'AI改判建议')
const adviceLoadingText = computed(() =>
  adviceKind.value === 'reinspection'
    ? AI_LOADING_TEXT.reinspectionAdvice
    : AI_LOADING_TEXT.rejudgmentAdvice
)

function isConcessionJudgment(type?: string) {
  return type === 'CAN_CONCESSION' || type === 'CONCESSION'
}

const canInitiateReinspection = computed(() => {
  const t = detail.value?.judgmentType
  return t === 'UNQUALIFIED' || t === 'NEED_REINSPECTION' || t === 'REINSPECTION'
})
const canApplyRejudgment = computed(() => {
  const t = detail.value?.judgmentType
  return !!t && t !== 'STANDARD_CONFLICT'
})
const canApplyConcession = computed(() => isConcessionJudgment(detail.value?.judgmentType))

function judgmentColor(type: string): any {
  const map: Record<string, string> = {
    QUALIFIED: 'success',
    UNQUALIFIED: 'danger',
    CAN_CONCESSION: 'warning',
    CONCESSION: 'warning',
    NEED_REINSPECTION: 'info',
    REINSPECTION: 'info',
    STANDARD_CONFLICT: 'danger'
  }
  return map[type] || 'info'
}

function confidenceType(label?: string): any {
  if (label === 'HIGH') return 'success'
  if (label === 'LOW') return 'danger'
  return 'warning'
}

function explanationTraceLabel(trace?: string) {
  const map: Record<string, string> = {
    MODEL_GENERATED: '已调模型·校验通过',
    MODEL_REJECTED: '已调模型·校验未通过',
    ASSESSMENT_REUSE: '未调模型·复用历史评估',
    CACHE_HIT: '未调模型·缓存命中',
    MODEL_DISABLED: '未调模型·开关关闭',
    STRUCTURED_ONLY: '未调模型·仅结构化',
    CONFLICT_REJUDGE: '冲突裁决后重判·结构化解释',
    SKIPPED: '未生成解释'
  }
  return map[trace || ''] || trace || '路径未知'
}

function stdTypeLabel(type: string) {
  const map: Record<string, string> = {
    CUSTOMER: '客户协议',
    ENTERPRISE: '企标',
    NATIONAL: '国标'
  }
  return map[type] || type
}

function deviationClass(deviation: number | null) {
  if (deviation === null || deviation === undefined) return ''
  return deviation > 0 ? 'text-danger' : ''
}

function indicatorResultLabel(result: string) {
  const dictLabel = dictStore.getLabel('INDICATOR_RESULT', result)
  if (dictLabel && dictLabel !== result) return dictLabel
  const fallback: Record<string, string> = {
    PASS: '合格',
    FAIL: '不合格',
    CONCESSION: '可让步',
    WARNING: '无标准覆盖',
    STANDARD_CONFLICT: '标准冲突'
  }
  return fallback[result] || result
}

function indicatorResultColor(result: string): any {
  const map: Record<string, string> = {
    PASS: 'success',
    FAIL: 'danger',
    CONCESSION: 'warning',
    WARNING: 'warning',
    STANDARD_CONFLICT: 'danger'
  }
  return map[result] || 'info'
}

async function loadDetail() {
  loading.value = true
  try {
    let res: any
    if (route.query.id) {
      res = await getJudgmentExplanation(route.query.id as string)
    } else if (route.query.recordId) {
      const snapshot = await getJudgmentByRecord(route.query.recordId as string)
      const judgmentId = snapshot?.judgmentId ?? snapshot?.id
      if (!judgmentId) {
        ElMessage.warning('未找到该检验记录的判定结论')
        return
      }
      res = await getJudgmentExplanation(judgmentId)
    }
    detail.value = res
  } finally {
    loading.value = false
  }
}

function handleReinspection() {
  reinspectionForm.value = { reinspectionReason: '', responsibleNo: '' }
  reinspectionDialogVisible.value = true
}

async function openAdvice(kind: 'reinspection' | 'rejudgment') {
  const judgmentId = detail.value?.judgmentId ?? detail.value?.id
  if (!judgmentId) {
    ElMessage.warning('缺少判定ID')
    return
  }
  adviceKind.value = kind
  advice.value = null
  adviceDialogVisible.value = true
  adviceLoading.value = true
  try {
    advice.value = kind === 'reinspection'
      ? await getReinspectionAdvice(judgmentId)
      : await getRejudgmentAdvice(judgmentId)
  } catch {
    ElMessage.error(kind === 'reinspection' ? 'AI 复检建议获取失败，请稍后重试' : 'AI 改判建议获取失败，请稍后重试')
    adviceDialogVisible.value = false
  } finally {
    adviceLoading.value = false
  }
}

async function ignoreAdvice() {
  if (!advice.value?.assessmentId) {
    adviceDialogVisible.value = false
    return
  }
  adviceActionLoading.value = true
  try {
    await handleAiAssessment(advice.value.assessmentId, {
      adoptionStatus: 'IGNORED',
      humanOpinion: '用户在判定解释页忽略AI流程建议'
    })
    ElMessage.success('已忽略AI建议')
    adviceDialogVisible.value = false
  } finally {
    adviceActionLoading.value = false
  }
}

async function adoptAdvice() {
  if (!advice.value || advice.value.withheld) {
    return
  }
  await markAdviceAdopted()
  if (adviceKind.value === 'reinspection') {
    reinspectionForm.value = {
      reinspectionReason: advice.value.suggestedReason || '',
      responsibleNo: ''
    }
    adviceDialogVisible.value = false
    reinspectionDialogVisible.value = true
    return
  }
  if (advice.value.recommendedAction !== 'RECOMMEND_REJUDGMENT') {
    ElMessage.warning('当前建议不支持自动预填改判目标')
    return
  }
  const jid = advice.value.judgmentId || detail.value?.judgmentId || detail.value?.id
  router.push({
    path: '/re-judgment/form',
    query: {
      judgmentId: jid,
      recordId: detail.value?.recordId || undefined,
      targetJudgmentType: advice.value.targetJudgmentType || '',
      reason: advice.value.suggestedReason || '',
      impactScope: advice.value.affectedScope || ''
    }
  })
}

async function markAdviceAdopted() {
  if (!advice.value?.assessmentId) return
  adviceActionLoading.value = true
  try {
    await handleAiAssessment(advice.value.assessmentId, {
      adoptionStatus: 'ADOPTED',
      humanOpinion: '用户在判定解释页采纳AI流程建议并进入人工流程预填'
    })
  } finally {
    adviceActionLoading.value = false
  }
}

async function confirmReinspection() {
  await reinspectionFormRef.value?.validate()
  actionLoading.value = true
  try {
    await initiateReinspection({
      originalJudgmentId: detail.value?.judgmentId ?? detail.value?.id,
      reinspectionReason: reinspectionForm.value.reinspectionReason,
      responsibleNo: reinspectionForm.value.responsibleNo
    })
    ElMessage.success('复检任务已发起')
    reinspectionDialogVisible.value = false
  } finally {
    actionLoading.value = false
  }
}

function handleRejudgment() {
  const jid = detail.value?.judgmentId ?? detail.value?.id
  if (!jid) {
    ElMessage.warning('缺少判定ID，无法发起改判')
    return
  }
  router.push({
    path: '/re-judgment/form',
    query: {
      judgmentId: jid,
      recordId: detail.value?.recordId || undefined
    }
  })
}

function handleConcession() {
  const jid = detail.value?.judgmentId ?? detail.value?.id
  router.push(`/concession/apply?judgmentId=${jid}`)
}

onMounted(loadDetail)
</script>

<style scoped>
.explanation-page {
  width: 100%;
  max-width: none;
}
.explanation-page.is-ai-loading {
  min-height: 55vh;
}
.hero-card {
  margin-bottom: 12px;
}
.hero-content {
  display: flex;
  align-items: flex-start;
  gap: 32px;
}
.hero-left {
  flex-shrink: 0;
}
.judgment-badge {
  font-size: 22px !important;
  padding: 10px 28px !important;
  height: auto !important;
  letter-spacing: 2px;
}
.hero-meta {
  flex: 1;
}
.standard-match-row {
  display: flex;
  gap: 12px;
  margin-bottom: 0;
}
.ai-card {
  margin-top: 12px;
  background: var(--bg-panel);
  border-color: var(--border-color);
}
.ai-head,
.ai-tags,
.factor-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.ai-head {
  justify-content: space-between;
}
.ai-text {
  margin: 10px 0 0;
  line-height: 1.7;
  white-space: pre-wrap;
  color: var(--text-primary);
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
.factor-row {
  flex-wrap: wrap;
  margin-top: 10px;
}
.match-card {
  flex: 1;
  text-align: center;
}
.match-type {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 8px;
}
.match-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 14px;
}
.status-hit {
  font-weight: 600;
}
.match-name {
  font-size: 12px;
  margin-top: 6px;
}
.match-reason {
  font-size: 12px;
  margin-top: 4px;
}
.bottom-bar {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.advice-dialog-body {
  min-height: 280px;
  position: relative;
}

.advice-alert,
.advice-table,
.advice-tags {
  margin-top: 12px;
}

.advice-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}

.advice-tags-label {
  color: var(--text-secondary);
  font-size: 12px;
}
.text-danger {
  color: #f56c6c;
  font-weight: 600;
}
</style>
