<template>
  <div class="rejudgment-form-page">
    <el-page-header @back="router.back()" content="发起改判申请" style="margin-bottom:16px" />

    <!-- 原判定信息 -->
    <el-card shadow="never" style="margin-bottom:12px" v-if="originalJudgment">
      <template #header>
        <span style="font-weight:600">原判定信息</span>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="卷号">{{ originalJudgment.coilNo }}</el-descriptions-item>
        <el-descriptions-item label="批次号">{{ originalJudgment.batchNo }}</el-descriptions-item>
        <el-descriptions-item label="炉号">{{ originalJudgment.heatNo }}</el-descriptions-item>
        <el-descriptions-item label="品种">
          {{ dictStore.getLabel('PRODUCT_VARIETY', originalJudgment.productVariety) }}
        </el-descriptions-item>
        <el-descriptions-item label="牌号">{{ originalJudgment.productGrade }}</el-descriptions-item>
        <el-descriptions-item label="当前判定">
          <el-tag :type="dictStore.getColorTag('JUDGMENT_TYPE', originalJudgment.judgmentType) as any">
            {{ dictStore.getLabel('JUDGMENT_TYPE', originalJudgment.judgmentType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="判定时间">{{ originalJudgment.judgeTime }}</el-descriptions-item>
        <el-descriptions-item label="检验人">{{ originalJudgment.inspector }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 逆向改判警告横幅 -->
    <div v-if="isReverse" class="reverse-warning-banner">
      <el-icon :size="18" style="margin-right:8px"><WarningFilled /></el-icon>
      逆向改判 — 此操作将升级至质量部长/总工审批，请确认证据充分后再提交
    </div>

    <!-- 申请表单 -->
    <el-card shadow="never">
      <template #header><span style="font-weight:600">改判申请信息</span></template>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12" v-if="!route.query.judgmentId">
            <el-form-item label="原判定记录ID" prop="judgmentId">
              <el-input v-model="formData.judgmentId" placeholder="输入原判定记录ID" @blur="loadOriginalJudgment" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目标结论" prop="targetJudgmentType">
              <el-select v-model="formData.targetJudgmentType" placeholder="请选择目标结论" style="width:100%" @change="checkReverse">
                <el-option
                  v-for="item in dictStore.getItems('JUDGMENT_TYPE')"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="改判原因" prop="reason">
              <el-input
                v-model="formData.reason"
                type="textarea"
                :rows="4"
                placeholder="请详细说明改判原因"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="影响范围">
              <el-input
                v-model="formData.impactScope"
                placeholder="描述改判影响的产品批次范围（选填）"
              />
            </el-form-item>
          </el-col>

          <!-- 逆向改判附加字段 -->
          <template v-if="isReverse">
            <el-col :span="12">
              <el-form-item label="新证据来源" prop="evidenceSource">
                <el-select v-model="formData.evidenceSource" placeholder="请选择证据来源" style="width:100%">
                  <el-option
                    v-for="item in dictStore.getItems('NEW_EVIDENCE_SOURCE')"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="证据附件" prop="evidenceFile">
                <el-upload
                  ref="uploadRef"
                  :auto-upload="false"
                  :limit="1"
                  :on-change="handleFileChange"
                  :on-remove="handleFileRemove"
                  :file-list="fileList"
                  drag
                  style="width:100%"
                >
                  <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
                  <div class="el-upload__text">
                    将文件拖到此处，或 <em>点击上传</em>
                  </div>
                  <template #tip>
                    <div class="el-upload__tip" style="color:#f56c6c">
                      逆向改判必须上传证据附件
                    </div>
                  </template>
                </el-upload>
              </el-form-item>
            </el-col>
          </template>
        </el-row>
      </el-form>
    </el-card>

    <div class="bottom-bar">
      <el-button @click="router.back()">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交申请</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, UploadFile } from 'element-plus'
import { WarningFilled, UploadFilled } from '@element-plus/icons-vue'
import { useDictStore } from '@/store/dict'
import { applyRejudgment } from '@/api/rejudgment'
import { getJudgmentExplanation } from '@/api/judgment'

const route = useRoute()
const router = useRouter()
const dictStore = useDictStore()

const formRef = ref<FormInstance>()
const uploadRef = ref<any>()
const submitLoading = ref(false)
const originalJudgment = ref<any>(null)
const fileList = ref<UploadFile[]>([])

const formData = reactive({
  judgmentId: (route.query.judgmentId as string) || '',
  targetJudgmentType: '',
  reason: '',
  impactScope: '',
  evidenceSource: '',
  evidenceFile: null as File | null
})

// 逆向改判类型：合格/让步 → 不合格/需复检
const POSITIVE_TYPES = ['QUALIFIED', 'CONCESSION']
const NEGATIVE_TYPES = ['UNQUALIFIED', 'REINSPECTION']

const isReverse = computed(() => {
  if (!originalJudgment.value || !formData.targetJudgmentType) return false
  const origType = originalJudgment.value.judgmentType
  const targetType = formData.targetJudgmentType
  return POSITIVE_TYPES.includes(origType) && NEGATIVE_TYPES.includes(targetType)
})

const baseRules = {
  judgmentId: [{ required: true, message: '请输入原判定记录ID', trigger: 'blur' }],
  targetJudgmentType: [{ required: true, message: '请选择目标结论', trigger: 'change' }],
  reason: [{ required: true, message: '请填写改判原因', trigger: 'blur' }]
}

const formRules = computed(() => {
  const rules: any = { ...baseRules }
  if (isReverse.value) {
    rules.evidenceSource = [{ required: true, message: '逆向改判必须选择证据来源', trigger: 'change' }]
    rules.evidenceFile = [{ required: true, message: '逆向改判必须上传证据附件', trigger: 'change' }]
  }
  return rules
})

function checkReverse() {
  // computed isReverse 自动更新
}

function handleFileChange(file: UploadFile) {
  formData.evidenceFile = file.raw || null
}

function handleFileRemove() {
  formData.evidenceFile = null
}

async function loadOriginalJudgment() {
  if (!formData.judgmentId) return
  try {
    const res = await getJudgmentExplanation(formData.judgmentId) as any
    originalJudgment.value = res
  } catch {}
}

async function handleSubmit() {
  await formRef.value?.validate()
  if (isReverse.value && !formData.evidenceFile) {
    ElMessage.error('逆向改判必须上传证据附件')
    return
  }
  submitLoading.value = true
  try {
    const fd = new FormData()
    fd.append('judgmentId', formData.judgmentId)
    fd.append('targetJudgmentType', formData.targetJudgmentType)
    fd.append('reason', formData.reason)
    fd.append('impactScope', formData.impactScope || '')
    fd.append('isReverse', String(isReverse.value))
    if (isReverse.value) {
      fd.append('evidenceSource', formData.evidenceSource)
      if (formData.evidenceFile) {
        fd.append('evidenceFile', formData.evidenceFile)
      }
    }
    await applyRejudgment(Object.fromEntries(fd.entries()))
    ElMessage.success('改判申请已提交')
    router.push('/re-judgment')
  } finally {
    submitLoading.value = false
  }
}

onMounted(async () => {
  if (formData.judgmentId) {
    await loadOriginalJudgment()
  }
})
</script>

<style scoped>
.rejudgment-form-page {
  padding: 16px;
  max-width: 900px;
}
.reverse-warning-banner {
  background: #fff0f0;
  border: 1px solid #f56c6c;
  border-radius: 4px;
  color: #f56c6c;
  font-weight: 600;
  font-size: 14px;
  padding: 12px 16px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}
.bottom-bar {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
