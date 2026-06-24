<template>
  <div
    class="concession-form-page"
    :class="{ 'is-ai-loading': judgmentLoading }"
    v-loading="judgmentLoading"
    :element-loading-text="AI_LOADING_TEXT.judgmentExplanation"
    :element-loading-custom-class="AI_LOADING_CLASS"
  >
    <el-page-header @back="router.back()" content="发起让步申请" style="margin-bottom:16px" />

    <el-card shadow="never" style="margin-bottom:12px" v-if="judgmentDetail">
      <template #header><span style="font-weight:600">关联判定信息</span></template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="卷号">{{ judgmentDetail.coilNo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="批次号">{{ judgmentDetail.batchNo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="炉号">{{ judgmentDetail.heatNo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="品种">
          {{ dictStore.getLabel('PRODUCT_VARIETY', judgmentDetail.productVariety) }}
        </el-descriptions-item>
        <el-descriptions-item label="牌号">{{ judgmentDetail.productGrade || '—' }}</el-descriptions-item>
        <el-descriptions-item label="判定结论">
          <el-tag :type="dictStore.getColorTag('JUDGMENT_TYPE', judgmentDetail.judgmentType) as any" size="small">
            {{ dictStore.getLabel('JUDGMENT_TYPE', judgmentDetail.judgmentType) }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never">
      <template #header><span style="font-weight:600">让步申请信息</span></template>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="让步范围" prop="concessionScope">
          <el-input
            v-model="formData.concessionScope"
            type="textarea"
            :rows="3"
            placeholder="说明允许让步的指标范围及限值，如：抗拉强度允许在让步范围内接收"
          />
        </el-form-item>
        <el-form-item label="风险描述" prop="riskDescription">
          <el-input
            v-model="formData.riskDescription"
            type="textarea"
            :rows="3"
            placeholder="说明让步使用的场景、风险及控制措施"
          />
        </el-form-item>
        <el-form-item label="生效日期" prop="effectiveDate">
          <el-date-picker
            v-model="formData.effectiveDate"
            type="date"
            placeholder="选择生效日期"
            value-format="YYYY-MM-DD"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="到期日期" prop="expiryDate">
          <el-date-picker
            v-model="formData.expiryDate"
            type="date"
            placeholder="选择到期日期"
            value-format="YYYY-MM-DD"
            style="width:100%"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <div class="bottom-bar">
      <el-button @click="router.back()">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交申请</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useDictStore } from '@/store/dict'
import { getJudgmentExplanation } from '@/api/judgment'
import { applyConcession } from '@/api/concession'
import { AI_LOADING_TEXT, AI_LOADING_CLASS } from '@/constants/ai-loading-text'

const route = useRoute()
const router = useRouter()
const dictStore = useDictStore()

const formRef = ref<FormInstance>()
const submitLoading = ref(false)
const judgmentLoading = ref(false)
const judgmentDetail = ref<any>(null)

const formData = reactive({
  judgmentId: (route.query.judgmentId as string) || '',
  concessionScope: '',
  riskDescription: '',
  effectiveDate: '',
  expiryDate: ''
})

const formRules = {
  concessionScope: [{ required: true, message: '请填写让步范围', trigger: 'blur' }],
  riskDescription: [{ required: true, message: '请填写风险描述', trigger: 'blur' }],
  effectiveDate: [{ required: true, message: '请选择生效日期', trigger: 'change' }],
  expiryDate: [{ required: true, message: '请选择到期日期', trigger: 'change' }]
}

function formatDate(d: Date) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function buildDefaultConcessionScope(detail: any) {
  const items = (detail?.indicatorDetails ?? []).filter(
    (i: any) => i.indicatorResult === 'CONCESSION' && !i.noStandard
  )
  if (!items.length) return ''
  return items
    .map((i: any) => {
      const unit = i.unit ? ` ${i.unit}` : ''
      const range =
        i.concessionLower != null || i.concessionUpper != null
          ? `，让步范围 ${i.concessionLower ?? '—'} ~ ${i.concessionUpper ?? '—'}${unit}`
          : ''
      return `${i.indicatorName}实测 ${i.testValue ?? '—'}${unit}${range}`
    })
    .join('；')
}

async function loadJudgment() {
  if (!formData.judgmentId) {
    ElMessage.warning('缺少判定结论 ID')
    return
  }
  judgmentLoading.value = true
  try {
    const res = await getJudgmentExplanation(formData.judgmentId)
    judgmentDetail.value = res
    if (!formData.concessionScope) {
      formData.concessionScope = buildDefaultConcessionScope(res)
    }
  } catch {
    /* handled by interceptor */
  } finally {
    judgmentLoading.value = false
  }
}

async function handleSubmit() {
  await formRef.value?.validate()
  if (formData.expiryDate && formData.effectiveDate && formData.expiryDate < formData.effectiveDate) {
    ElMessage.warning('到期日期不能早于生效日期')
    return
  }
  submitLoading.value = true
  try {
    await applyConcession({
      judgmentId: formData.judgmentId,
      concessionScope: formData.concessionScope,
      riskDescription: formData.riskDescription,
      effectiveDate: formData.effectiveDate,
      expiryDate: formData.expiryDate
    })
    ElMessage.success('让步申请已提交')
    router.push('/concession')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  const today = new Date()
  const nextMonth = new Date(today)
  nextMonth.setMonth(nextMonth.getMonth() + 1)
  formData.effectiveDate = formatDate(today)
  formData.expiryDate = formatDate(nextMonth)
  loadJudgment()
})
</script>

<style scoped>
.concession-form-page {
  padding: 16px;
  max-width: 900px;
}
.concession-form-page.is-ai-loading {
  min-height: 45vh;
}
.bottom-bar {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
