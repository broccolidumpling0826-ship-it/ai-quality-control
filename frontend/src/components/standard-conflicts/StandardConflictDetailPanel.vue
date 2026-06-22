<template>
  <div class="conflict-detail-panel" v-loading="loading">
    <template v-if="detail">
      <div class="panel-section">
        <div class="section-head">
          <span class="section-title">冲突对比</span>
          <div class="section-tags">
            <el-tag size="small" :type="detail.conflictLevel === 'BLOCKING' ? 'danger' : 'warning'">
              {{ conflictLevelLabel }}
            </el-tag>
            <el-tag size="small" :type="detail.status === 'PENDING' ? 'warning' : 'success'">
              {{ statusLabel }}
            </el-tag>
          </div>
        </div>

        <div class="meta-row">
          <span>品种：<strong>{{ detail.variety || '—' }}</strong></span>
          <span>牌号：<strong class="mono">{{ detail.grade || '—' }}</strong></span>
          <span>冲突指标：<strong class="mono">{{ detail.indicatorName || detail.indicatorId || '—' }}</strong></span>
        </div>

        <el-alert
          v-if="comparisonStandards.length"
          class="involved-alert"
          type="warning"
          :closable="false"
          show-icon
        >
          <template #title>
            涉及标准：<span class="mono">{{ involvedStandardsSummary }}</span>
          </template>
        </el-alert>

        <div v-if="comparisonStandards.length" class="compare-list">
          <div
            v-for="(item, index) in comparisonStandards"
            :key="item.standardId || index"
            class="compare-card"
            :class="{ 'decision-highlight': isDecisionStandard(item.standardId) }"
          >
            <div class="compare-card-head">
              <span class="compare-card-title mono">{{ buildStandardDisplayName(item, item.standardId) }}</span>
              <div class="compare-card-tags">
                <el-tag v-if="isDecisionStandard(item.standardId)" size="small" type="warning">最终裁定</el-tag>
<!--                <el-tag size="small" :type="standardTypeTagType(item.standardType)">
                  {{ standardTypeLabel(item.standardType) }}
                </el-tag>-->
              </div>
            </div>
            <div class="limit-grid">
              <div class="limit-item">
                <span class="limit-label">上限</span>
                <span class="limit-value mono">{{ formatLimitValue(item.upperLimit) }}</span>
              </div>
              <div class="limit-item">
                <span class="limit-label">下限</span>
                <span class="limit-value mono highlight">{{ formatLimitValue(item.lowerLimit) }}</span>
              </div>
            </div>
            <div class="compare-meta mono">有效期 {{ buildEffectiveWindow(item) }}</div>
            <div v-if="item.specRange" class="compare-meta">规格 {{ item.specRange }}</div>
          </div>
        </div>

        <el-alert
          v-else
          type="warning"
          :closable="false"
          show-icon
          title="冲突标准明细不完整，请查看来源引用或联系管理员补充标准快照。"
        />
      </div>

      <div class="panel-section">
        <div class="section-head">
          <span class="section-title">来源引用</span>
        </div>
        <div v-if="parsedDetail.reason" class="source-block">
          <div class="source-head">冲突说明</div>
          <p class="source-text">{{ parsedDetail.reason }}</p>
        </div>
        <div
          v-for="(item, index) in comparisonStandards"
          :key="item.standardId || `source-${index}`"
          class="source-block"
        >
          <div class="source-head">
            <span class="mono">{{ buildStandardDisplayName(item, item.standardId) }}</span>
            <el-tag size="small" :type="standardTypeTagType(item.standardType)">
              {{ standardTypeLabel(item.standardType) }}
            </el-tag>
          </div>
          <p class="source-text">
            有效期 {{ buildEffectiveWindow(item) }}；
            指标限值：下限 {{ formatLimitValue(item.lowerLimit) }}，
            上限 {{ formatLimitValue(item.upperLimit) }}
            <template v-if="detail.unit"> {{ detail.unit }}</template>。
            <template v-if="item.specRange">适用规格 {{ item.specRange }}。</template>
          </p>
        </div>
        <el-empty v-if="!parsedDetail.reason && !comparisonStandards.length" description="暂无来源引用" :image-size="56" />
      </div>

      <div v-if="detail.status === 'PENDING'" class="panel-section resolve-section">
        <div class="section-head">
          <span class="section-title">裁定</span>
        </div>
        <el-form :model="form" label-position="top" class="resolve-form">
          <el-form-item label="裁定结果" required>
            <el-radio-group v-model="form.decisionStandardId" class="decision-group">
              <el-radio
                v-for="item in decisionOptions"
                :key="item.standardId"
                :label="item.standardId"
                class="decision-radio"
              >
                {{ item.label }}
              </el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="裁定备注" required>
            <el-input
              v-model="form.decisionReason"
              type="textarea"
              :rows="4"
              maxlength="1000"
              show-word-limit
              placeholder="请输入裁定说明"
            />
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="form.auditConfirmed">
              我已确认本次裁决定将写入审计日志，并触发重新判定
            </el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button
              v-if="canResolve"
              type="primary"
              :icon="Check"
              :loading="submitting"
              :disabled="!form.auditConfirmed"
              @click="submitResolve"
            >
              提交裁定
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div v-else class="panel-section resolved-section">
        <div class="section-head">
          <span class="section-title">裁定结果</span>
        </div>
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="控制标准">
            <span class="mono">{{ detail.decisionStandardId || '—' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="裁定说明">{{ detail.decisionReason || '—' }}</el-descriptions-item>
          <el-descriptions-item label="裁定人">{{ detail.decisionBy || '—' }}</el-descriptions-item>
          <el-descriptions-item label="裁定时间">{{ detail.decisionTime || '—' }}</el-descriptions-item>
          <el-descriptions-item v-if="detail.rejudgeJudgmentId" label="重判记录">
            <span class="mono">{{ detail.rejudgeJudgmentId }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </template>

    <el-empty v-else description="请选择左侧冲突记录查看详情" :image-size="72" />
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { resolveStandardConflict, type StandardConflict } from '@/api/standard-conflict'
import { hasPermission } from '@/directives/permission'
import {
  buildEffectiveWindow,
  buildStandardDisplayName,
  buildInvolvedStandardsSummary,
  formatLimitValue,
  parseConflictDetail,
  standardTypeLabel,
  standardTypeTagType,
  type ConflictStandardSnapshot
} from '@/utils/standard-conflict-detail'

const props = defineProps<{
  detail: StandardConflict | null
  loading?: boolean
}>()

const emit = defineEmits<{
  resolved: [detail: StandardConflict]
}>()

const submitting = ref(false)
const form = reactive({
  decisionStandardId: '',
  decisionReason: '',
  auditConfirmed: false
})

const canResolve = computed(() => hasPermission('standard-conflict:resolve'))

const parsedDetail = computed(() => parseConflictDetail(props.detail?.conflictDetail))

const comparisonStandards = computed(() => {
  const parsed = parsedDetail.value.standards
  if (parsed.length) return parsed
  return (props.detail?.involvedStandardIds || []).map((id) => ({ standardId: id }))
})

const involvedStandardsSummary = computed(() =>
  buildInvolvedStandardsSummary(comparisonStandards.value)
)

function isDecisionStandard(standardId?: string): boolean {
  if (!standardId || !props.detail?.decisionStandardId) return false
  return props.detail.decisionStandardId === standardId
}

const decisionOptions = computed(() => {
  return comparisonStandards.value.map((item) => ({
    standardId: item.standardId || '',
    label: resolveDecisionLabel(item)
  })).filter((item) => item.standardId)
})

const conflictLevelLabel = computed(() => {
  if (props.detail?.conflictLevel === 'BLOCKING') return '阻断冲突'
  if (props.detail?.conflictLevel === 'PRIORITY_RESOLVABLE') return '优先级可解'
  return props.detail?.conflictLevel || '—'
})

const statusLabel = computed(() => {
  if (props.detail?.status === 'PENDING') return '待裁定'
  if (props.detail?.status === 'RESOLVED') return '已裁定'
  return props.detail?.status || '—'
})

watch(
  () => props.detail,
  (value) => {
    form.decisionStandardId = value?.decisionStandardId || decisionOptions.value[0]?.standardId || ''
    form.decisionReason = value?.decisionReason || ''
    form.auditConfirmed = false
  },
  { immediate: true }
)

function resolveDecisionLabel(item: ConflictStandardSnapshot): string {
  const type = item.standardType
  if (type === 'CUSTOMER') return `以客协为准（${buildStandardDisplayName(item, item.standardId)}）`
  if (type === 'ENTERPRISE') return `以企标为准（${buildStandardDisplayName(item, item.standardId)}）`
  if (type === 'NATIONAL') return `以国标为准（${buildStandardDisplayName(item, item.standardId)}）`
  return `以 ${buildStandardDisplayName(item, item.standardId)} 为准`
}

async function submitResolve() {
  if (!props.detail) return
  if (!form.decisionStandardId || !form.decisionReason.trim()) {
    ElMessage.warning('请选择裁定结果并填写裁定备注')
    return
  }
  if (!form.auditConfirmed) {
    ElMessage.warning('请确认审计提示后再提交')
    return
  }
  submitting.value = true
  try {
    const result = await resolveStandardConflict(props.detail.id, {
      decisionStandardId: form.decisionStandardId,
      decisionReason: form.decisionReason.trim()
    })
    ElMessage.success('裁定完成，已触发重新判定')
    emit('resolved', result)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.conflict-detail-panel {
  min-height: 420px;
  padding: 14px 16px 18px;
  background: var(--bg-panel);
  border: 1px solid var(--border-color);
  border-radius: 4px;
}

.panel-section + .panel-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.section-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: 0.04em;
}

.section-tags {
  display: flex;
  gap: 6px;
}

.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 14px 20px;
  margin-bottom: 10px;
  font-size: 12px;
  color: var(--text-secondary);
}

.involved-alert {
  margin-bottom: 12px;
}

.involved-alert :deep(.el-alert__title) {
  font-size: 12px;
  line-height: 1.6;
}

.meta-row strong {
  color: var(--text-primary);
  font-weight: 600;
}

.mono {
  font-family: var(--font-data);
}

.compare-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
  gap: 10px;
}

.compare-card {
  padding: 12px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 4px;
}

.compare-card.decision-highlight {
  border-color: rgba(255, 180, 0, 0.65);
  background: linear-gradient(180deg, rgba(255, 180, 0, 0.12) 0%, rgba(255, 180, 0, 0.03) 100%);
  box-shadow: inset 0 0 0 1px rgba(255, 180, 0, 0.2), 0 0 12px rgba(255, 180, 0, 0.08);
}

.compare-card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 10px;
}

.compare-card-tags {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  flex-shrink: 0;
}

.compare-card-title {
  flex: 1;
  min-width: 0;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.5;
  word-break: break-all;
}

.limit-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.limit-item {
  padding: 8px;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 3px;
}

.limit-label {
  display: block;
  font-size: 11px;
  color: var(--text-muted);
  margin-bottom: 4px;
}

.limit-value {
  font-size: 18px;
  color: var(--text-primary);
}

.limit-value.highlight {
  color: var(--gold, #ffb400);
}

.compare-meta {
  margin-top: 6px;
  font-size: 11px;
  color: var(--text-muted);
  line-height: 1.5;
}

.source-head {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  font-size: 12px;
  font-weight: 600;
  color: var(--cyan);
  margin-bottom: 6px;
}

.source-block {
  padding: 10px 12px;
  margin-bottom: 8px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 4px;
}

.source-text {
  margin: 0;
  font-size: 12px;
  line-height: 1.65;
  color: var(--text-secondary);
}

.resolve-form :deep(.el-form-item__label) {
  font-size: 12px;
  color: var(--text-secondary);
}

.decision-group {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}

.decision-radio {
  margin-right: 0;
}

.resolved-section :deep(.el-descriptions__label) {
  width: 88px;
}
</style>
