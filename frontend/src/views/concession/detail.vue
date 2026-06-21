<template>
  <div class="concession-detail-page" v-loading="loading">
    <el-page-header @back="router.back()" content="让步接收详情" style="margin-bottom:16px" />

    <template v-if="detail">
      <!-- 状态机步骤 -->
      <el-card shadow="never" style="margin-bottom:12px">
        <el-steps :active="currentStep" align-center finish-status="success">
          <el-step title="内部审批" description="质量部内部审核" />
          <el-step title="待客户确认" description="发送客户等待回执" />
          <el-step :title="customerStepTitle" :description="customerStepDesc" />
          <el-step title="已批准" description="让步接收正式生效" />
        </el-steps>
      </el-card>

      <!-- 基本信息 + 有效期倒计时 -->
      <div style="display:flex;gap:12px;margin-bottom:12px">
        <el-card shadow="never" style="flex:2">
          <template #header><span style="font-weight:600">让步基本信息</span></template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="卷号">{{ detail.coilNo }}</el-descriptions-item>
            <el-descriptions-item label="批次号">{{ detail.batchNo }}</el-descriptions-item>
            <el-descriptions-item label="让步范围" :span="2">{{ detail.concessionScope }}</el-descriptions-item>
            <el-descriptions-item label="有效期">
              {{ detail.validFrom }} ~ {{ detail.validTo }}
            </el-descriptions-item>
            <el-descriptions-item label="申请人">{{ detail.applyByName }}</el-descriptions-item>
            <el-descriptions-item label="申请时间">{{ detail.applyTime }}</el-descriptions-item>
            <el-descriptions-item label="总状态">
              <el-tag :type="totalStatusTagType as any">
                {{ totalStatusLabel }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="风险描述" :span="2">{{ detail.riskDescription ?? detail.reason }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 有效期倒计时 -->
        <el-card shadow="never" style="flex:1;text-align:center">
          <template #header><span style="font-weight:600">有效期剩余</span></template>
          <div class="countdown-block">
            <div :class="['countdown-days', remainingDays <= 7 ? 'days-warning' : 'days-normal']">
              {{ remainingDays }}
            </div>
            <div class="text-muted" style="font-size:14px">天</div>
          </div>
          <div class="text-muted text-meta-sm" style="margin-top:8px">
            到期日：{{ detail.validTo }}
          </div>
          <el-tag v-if="remainingDays <= 0" type="danger" style="margin-top:8px">已过期</el-tag>
          <el-tag v-else-if="remainingDays <= 7" type="warning" style="margin-top:8px">即将到期</el-tag>
        </el-card>
      </div>

      <!-- 客户确认附件区域 -->
      <el-card shadow="never" style="margin-bottom:12px">
        <template #header><span style="font-weight:600">客户确认附件</span></template>

        <!-- 已上传 -->
        <template v-if="detail.confirmFileUrl || detail.confirmAttachmentUrl">
          <el-alert type="success" :closable="false" show-icon style="margin-bottom:12px">
            附件已上传，不可替换
          </el-alert>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="文件名">
              <el-link :href="detail.confirmFileUrl || detail.confirmAttachmentUrl" type="primary" target="_blank">
                {{ detail.confirmFileName || '查看附件' }}
              </el-link>
            </el-descriptions-item>
            <el-descriptions-item label="上传人">{{ detail.confirmUploadByName }}</el-descriptions-item>
            <el-descriptions-item label="上传时间">{{ detail.confirmUploadTime }}</el-descriptions-item>
          </el-descriptions>
          <el-tag type="danger" style="margin-top:8px">不可替换</el-tag>
        </template>

        <!-- 未上传 -->
        <template v-else>
          <el-alert type="warning" :closable="false" show-icon style="margin-bottom:12px">
            尚未上传客户确认附件，确认操作前请先上传（强制上传）
          </el-alert>
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            drag
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :file-list="uploadFileList"
            style="width:100%"
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">拖拽文件到此处，或 <em>点击上传</em></div>
            <template #tip>
              <div class="el-upload__tip" style="color:#f56c6c">强制上传 — 确认前必须提供客户回执文件</div>
            </template>
          </el-upload>
          <el-form style="margin-top:12px">
            <el-form-item label="客户确认摘要">
              <el-input
                v-model="confirmSummary"
                type="textarea"
                :rows="2"
                placeholder="客户确认摘要（可选）"
                style="max-width:500px"
              />
            </el-form-item>
          </el-form>
        </template>
      </el-card>

      <!-- 操作按钮 -->
      <div class="bottom-bar">
        <el-button @click="router.back()">返回</el-button>
        <el-button
          type="primary"
          plain
          @click="goAiAssessment"
        >AI 评估</el-button>
        <el-button
          v-if="canReject"
          type="danger"
          :loading="actionLoading"
          @click="handleReject"
        >记录客户拒绝</el-button>
        <el-button
          v-if="canInternalApprove"
          v-permission="['concession:approve:first', 'concession:approve:final']"
          type="warning"
          :loading="actionLoading"
          @click="handleInternalApprove"
        >内部审批</el-button>
        <el-button
          v-if="canConfirm"
          type="success"
          :loading="actionLoading"
          :disabled="!(detail.confirmFileUrl || detail.confirmAttachmentUrl) && !uploadFile"
          @click="handleConfirm"
        >确认客户已确认</el-button>
      </div>
    </template>

    <!-- 驳回对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="记录客户拒绝" width="440px">
      <el-form ref="rejectFormRef" :model="rejectForm" :rules="rejectRules" label-width="80px">
        <el-form-item label="拒绝原因" prop="rejectReason">
          <el-input v-model="rejectForm.rejectReason" type="textarea" :rows="3" placeholder="请输入客户拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="actionLoading" @click="submitReject">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, UploadFile } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { useDictStore } from '@/store/dict'
import { getConcessionById, confirmConcession, rejectConcession, approveConcession } from '@/api/concession'

const route = useRoute()
const router = useRouter()
const dictStore = useDictStore()

const loading = ref(false)
const actionLoading = ref(false)
const detail = ref<any>(null)
const uploadFile = ref<File | null>(null)
const uploadFileList = ref<UploadFile[]>([])
const confirmSummary = ref('')

const rejectDialogVisible = ref(false)
const rejectFormRef = ref<FormInstance>()
const rejectForm = ref({ rejectReason: '' })
const rejectRules = {
  rejectReason: [{ required: true, message: '请输入拒绝原因', trigger: 'blur' }]
}

const currentStep = computed(() => {
  if (!detail.value) return 0
  const { confirmStatus, approvalStatus } = detail.value
  if (approvalStatus === 'APPROVED') return 3
  if (confirmStatus === 'CONFIRMED' || confirmStatus === 'REJECTED') return 2
  if (confirmStatus === 'PENDING') return 1
  return 0
})

const totalStatusLabel = computed(() => {
  if (!detail.value) return '-'
  const code = detail.value.concessionStatus ?? detail.value.approvalStatus
  if (code) {
    const label = dictStore.getLabel('CONCESSION_STATUS', code)
    if (label && label !== code) return label
  }
  if (detail.value.confirmStatus === 'PENDING') {
    return dictStore.getLabel('CONFIRM_STATUS', 'PENDING') || '待确认'
  }
  return dictStore.getLabel('CONFIRM_STATUS', detail.value.confirmStatus) || '-'
})

const totalStatusTagType = computed(() => {
  if (!detail.value) return 'info'
  const code = detail.value.concessionStatus ?? detail.value.approvalStatus
  if (code) {
    return dictStore.getColorTag('CONCESSION_STATUS', code)
  }
  return dictStore.getColorTag('CONFIRM_STATUS', detail.value.confirmStatus)
})

const customerStepTitle = computed(() => {
  if (!detail.value) return '客户确认'
  return detail.value.confirmStatus === 'REJECTED' ? '客户已拒绝' : '已确认'
})

const customerStepDesc = computed(() => {
  if (!detail.value) return ''
  return detail.value.confirmStatus === 'REJECTED' ? '客户拒绝让步' : '客户同意让步接收'
})

const remainingDays = computed(() => {
  if (!detail.value?.validTo) return 0
  const diff = new Date(detail.value.validTo).getTime() - Date.now()
  return Math.max(0, Math.ceil(diff / (24 * 3600 * 1000)))
})

const canConfirm = computed(
  () => detail.value?.confirmStatus === 'PENDING' && detail.value?.approvalStatus === 'PENDING'
)
const canReject = computed(() => detail.value?.confirmStatus === 'PENDING')
const canInternalApprove = computed(() => detail.value?.confirmStatus === 'CONFIRMED')

async function loadDetail() {
  const id = route.query.id as string
  if (!id) return
  loading.value = true
  try {
    detail.value = await getConcessionById(id)
  } finally {
    loading.value = false
  }
}

function handleFileChange(file: UploadFile) {
  uploadFile.value = file.raw || null
}

function handleFileRemove() {
  uploadFile.value = null
}

async function handleConfirm() {
  if (!(detail.value.confirmFileUrl || detail.value.confirmAttachmentUrl) && !uploadFile.value) {
    ElMessage.error('请先上传客户确认附件')
    return
  }
  actionLoading.value = true
  try {
    const fd = new FormData()
    fd.append('file', uploadFile.value)
    if (confirmSummary.value.trim()) {
      fd.append('summary', confirmSummary.value.trim())
    }
    await confirmConcession(detail.value.id, fd)
    ElMessage.success('客户确认已记录')
    loadDetail()
  } finally {
    actionLoading.value = false
  }
}

function handleReject() {
  rejectForm.value.rejectReason = ''
  rejectDialogVisible.value = true
}

async function submitReject() {
  await rejectFormRef.value?.validate()
  actionLoading.value = true
  try {
    await rejectConcession(detail.value.id, { rejectReason: rejectForm.value.rejectReason })
    ElMessage.success('已记录客户拒绝')
    rejectDialogVisible.value = false
    loadDetail()
  } finally {
    actionLoading.value = false
  }
}

async function handleInternalApprove() {
  actionLoading.value = true
  try {
    await approveConcession(detail.value.id, {})
    ElMessage.success('内部审批已完成')
    loadDetail()
  } finally {
    actionLoading.value = false
  }
}

function goAiAssessment() {
  const q: Record<string, string> = {}
  if (detail.value?.judgmentId) q.judgmentId = detail.value.judgmentId
  if (detail.value?.id) q.concessionId = detail.value.id
  router.push({ path: '/concession/ai-assessment', query: q })
}

onMounted(loadDetail)
</script>

<style scoped>
.concession-detail-page {
  padding: 16px;
  max-width: 1100px;
}
.countdown-block {
  margin: 20px 0 8px;
}
.countdown-days {
  font-size: 64px;
  font-weight: 700;
  line-height: 1;
}
.days-warning {
  color: #e6a23c;
}
.days-normal {
  color: #409eff;
}
.bottom-bar {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
