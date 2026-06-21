<template>
  <div class="standard-conflict-detail-page" v-loading="loading">
    <el-card class="detail-card" shadow="never" v-if="detail">
      <template #header>
        <div class="head">
          <div>
            <span class="mono">{{ detail.conflictNo }}</span>
            <el-tag class="ml8" :type="detail.conflictLevel === 'BLOCKING' ? 'danger' : 'warning'">
              {{ detail.conflictLevel }}
            </el-tag>
            <el-tag class="ml8" :type="detail.status === 'PENDING' ? 'danger' : 'success'">
              {{ detail.status }}
            </el-tag>
          </div>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <el-descriptions :column="3" border size="small">
        <el-descriptions-item label="指标">{{ detail.indicatorName || detail.indicatorId }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ detail.customerId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="品种/牌号">{{ detail.variety }} / {{ detail.grade }}</el-descriptions-item>
        <el-descriptions-item label="规格">{{ detail.productSpec || '-' }}</el-descriptions-item>
        <el-descriptions-item label="检验日期">{{ detail.inspectionDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="重判ID">{{ detail.rejudgeJudgmentId || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-alert class="mt12" type="warning" :closable="false" show-icon>
        <template #title>涉及标准：{{ detail.involvedStandardIds?.join(' / ') || '-' }}</template>
      </el-alert>

      <pre class="json-box">{{ prettyDetail }}</pre>

      <el-form v-if="detail.status === 'PENDING'" class="resolve-form" :model="form" label-width="110px">
        <el-form-item label="控制标准">
          <el-select v-model="form.decisionStandardId" placeholder="选择裁决后控制标准" style="width: 320px">
            <el-option
              v-for="id in detail.involvedStandardIds || []"
              :key="id"
              :label="id"
              :value="id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="裁决理由">
          <el-input v-model="form.decisionReason" type="textarea" :rows="3" maxlength="1000" show-word-limit />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Check" :loading="submitting" @click="submitResolve">提交裁决并重判</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getStandardConflict, resolveStandardConflict, type StandardConflict } from '@/api/standard-conflict'

const route = useRoute()
const loading = ref(false)
const submitting = ref(false)
const detail = ref<StandardConflict | null>(null)
const form = reactive({ decisionStandardId: '', decisionReason: '' })

const prettyDetail = computed(() => {
  if (!detail.value?.conflictDetail) return '{}'
  try {
    return JSON.stringify(JSON.parse(detail.value.conflictDetail), null, 2)
  } catch {
    return detail.value.conflictDetail
  }
})

async function loadDetail() {
  const id = String(route.query.id || '')
  if (!id) return
  loading.value = true
  try {
    detail.value = await getStandardConflict(id)
    form.decisionStandardId = detail.value.decisionStandardId || ''
  } finally {
    loading.value = false
  }
}

async function submitResolve() {
  if (!detail.value || !form.decisionStandardId || !form.decisionReason.trim()) {
    ElMessage.warning('请选择控制标准并填写裁决理由')
    return
  }
  submitting.value = true
  try {
    detail.value = await resolveStandardConflict(detail.value.id, {
      decisionStandardId: form.decisionStandardId,
      decisionReason: form.decisionReason.trim()
    })
    ElMessage.success('裁决完成')
  } finally {
    submitting.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.standard-conflict-detail-page {
  padding: 16px;
}

.detail-card {
  background: var(--bg-panel);
  border-color: var(--border-color);
}

.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mono,
.json-box {
  font-family: var(--font-data);
}

.ml8 {
  margin-left: 8px;
}

.mt12,
.resolve-form {
  margin-top: 12px;
}

.json-box {
  margin: 12px 0 0;
  padding: 12px;
  max-height: 300px;
  overflow: auto;
  color: var(--text-regular);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 4px;
}
</style>
