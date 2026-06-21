<template>
  <div class="ai-confidence-page">
    <el-card class="config-card" shadow="never" v-loading="loading">
      <template #header>
        <div class="card-head">
          <div>
            <div class="card-title">AI置信度配置</div>
            <div class="card-subtitle">规则分层仍为判定依据，权重仅用于解释排序和复核提示</div>
          </div>
          <el-button type="primary" :icon="Refresh" @click="loadConfig">刷新</el-button>
        </div>
      </template>

      <el-alert
        v-if="!formValid"
        type="warning"
        :closable="false"
        show-icon
        class="state-alert"
        :title="validationMessage"
      />

      <el-form :model="form" label-width="120px" class="config-form">
        <el-form-item label="配置名称">
          <el-input v-model="form.configName" maxlength="100" show-word-limit />
        </el-form-item>

        <div class="section-title">权重配置</div>
        <el-row :gutter="12">
          <el-col :xs="24" :md="8">
            <el-form-item label="规则权重">
              <el-input-number v-model="form.ruleWeight" :min="0" :max="1" :step="0.05" :precision="4" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="8">
            <el-form-item label="RAG权重">
              <el-input-number v-model="form.ragWeight" :min="0" :max="1" :step="0.05" :precision="4" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="8">
            <el-form-item label="LLM权重">
              <el-input-number v-model="form.llmWeight" :min="0" :max="1" :step="0.05" :precision="4" />
            </el-form-item>
          </el-col>
        </el-row>
        <div class="summary-line">
          <span>权重合计</span>
          <strong :class="{ danger: !weightValid }">{{ weightTotal.toFixed(4) }}</strong>
        </div>

        <div class="section-title">阈值配置</div>
        <el-row :gutter="12">
          <el-col :xs="24" :md="8">
            <el-form-item label="高置信阈值">
              <el-input-number v-model="form.highThreshold" :min="0" :max="1" :step="0.05" :precision="4" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="8">
            <el-form-item label="中置信阈值">
              <el-input-number v-model="form.mediumThreshold" :min="0" :max="1" :step="0.05" :precision="4" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="8">
            <el-form-item label="低置信阈值">
              <el-input-number v-model="form.lowThreshold" :min="0" :max="1" :step="0.05" :precision="4" />
            </el-form-item>
          </el-col>
        </el-row>
        <div class="summary-line">
          <span>阈值顺序</span>
          <strong :class="{ danger: !thresholdValid }">
            {{ thresholdValid ? 'HIGH > MEDIUM > LOW' : '阈值顺序不合法' }}
          </strong>
        </div>

        <el-form-item label="启用状态">
          <el-switch v-model="enabledSwitch" active-text="启用" inactive-text="停用" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>

      <div class="meta-row">
        <span>当前配置：{{ form.id || '-' }}</span>
        <span>更新人：{{ form.updatedBy || '-' }}</span>
        <span>更新时间：{{ form.updatedAt || '-' }}</span>
      </div>

      <div class="actions">
        <el-button type="primary" :icon="Check" :loading="saving" :disabled="!formValid" @click="saveConfig">
          保存配置
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, Refresh } from '@element-plus/icons-vue'
import {
  getActiveAiConfidenceConfig,
  updateActiveAiConfidenceConfig,
  type AiConfidenceConfig
} from '@/api/ai-confidence'
import { ref } from 'vue'

const loading = ref(false)
const saving = ref(false)
const form = reactive<AiConfidenceConfig>({
  configName: '',
  ruleWeight: 0.6,
  ragWeight: 0.3,
  llmWeight: 0.1,
  highThreshold: 0.85,
  mediumThreshold: 0.65,
  lowThreshold: 0,
  enabled: 1,
  remark: ''
})

const enabledSwitch = computed({
  get: () => form.enabled === 1,
  set: (value: boolean) => {
    form.enabled = value ? 1 : 0
  }
})

const weightTotal = computed(() => Number(form.ruleWeight || 0) + Number(form.ragWeight || 0) + Number(form.llmWeight || 0))
const weightValid = computed(() => Math.abs(weightTotal.value - 1) < 0.0001)
const thresholdValid = computed(() =>
  Number(form.highThreshold) > Number(form.mediumThreshold) &&
  Number(form.mediumThreshold) > Number(form.lowThreshold)
)
const formValid = computed(() => Boolean(form.configName?.trim()) && weightValid.value && thresholdValid.value)
const validationMessage = computed(() => {
  if (!form.configName?.trim()) return '配置名称不能为空'
  if (!weightValid.value) return '规则/RAG/LLM 权重合计必须等于 1.0000'
  if (!thresholdValid.value) return '高置信阈值必须大于中置信阈值，中置信阈值必须大于低置信阈值'
  return ''
})

async function loadConfig() {
  loading.value = true
  try {
    const data = await getActiveAiConfidenceConfig()
    Object.assign(form, normalizeConfig(data))
  } finally {
    loading.value = false
  }
}

async function saveConfig() {
  if (!formValid.value) {
    ElMessage.warning(validationMessage.value)
    return
  }
  saving.value = true
  try {
    const data = await updateActiveAiConfidenceConfig(normalizeConfig(form))
    Object.assign(form, normalizeConfig(data))
    ElMessage.success('AI置信度配置已更新')
  } finally {
    saving.value = false
  }
}

function normalizeConfig(data: AiConfidenceConfig): AiConfidenceConfig {
  return {
    ...data,
    ruleWeight: Number(data.ruleWeight),
    ragWeight: Number(data.ragWeight),
    llmWeight: Number(data.llmWeight),
    highThreshold: Number(data.highThreshold),
    mediumThreshold: Number(data.mediumThreshold),
    lowThreshold: Number(data.lowThreshold),
    enabled: Number(data.enabled ?? 1)
  }
}

onMounted(loadConfig)
</script>

<style scoped>
.ai-confidence-page {
  padding: 16px;
}

.config-card {
  max-width: 1180px;
  background: var(--bg-panel);
  border-color: var(--border-color);
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.card-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-heading);
}

.card-subtitle {
  margin-top: 4px;
  color: var(--text-secondary);
  font-size: 12px;
}

.state-alert {
  margin-bottom: 14px;
}

.config-form {
  max-width: 1040px;
}

.section-title {
  margin: 16px 0 12px;
  padding-left: 8px;
  border-left: 3px solid var(--cyan);
  color: var(--text-heading);
  font-weight: 600;
  font-size: 14px;
}

.summary-line {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin: 0 0 8px;
  color: var(--text-secondary);
  font-size: 13px;
}

.summary-line strong {
  color: var(--green);
  font-family: var(--font-data);
}

.summary-line strong.danger {
  color: var(--red);
}

.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--border-color);
  color: var(--text-muted);
  font-size: 12px;
  font-family: var(--font-data);
}

.actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
