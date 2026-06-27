<template>
  <div class="rejudgment-form-page">
    <el-page-header @back="router.back()" content="发起改判申请" style="margin-bottom:16px" />

    <!-- 原判定信息 -->
    <el-card
      shadow="never"
      class="judgment-info-card"
      :class="{ 'is-ai-loading': judgmentLoading }"
      style="margin-bottom:12px"
      v-loading="judgmentLoading"
      element-loading-text="正在加载原判定信息…"
      v-if="originalJudgment || judgmentLoading || fromRouteJudgment"
    >
      <template #header>
        <span style="font-weight:600">原判定信息</span>
        <el-button
          v-if="!fromRouteJudgment"
          link
          type="primary"
          style="float:right;margin-top:-4px"
          @click="openJudgmentPicker"
        >重新选择</el-button>
      </template>
      <el-descriptions v-if="originalJudgment" :column="3" border>
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
        <el-descriptions-item label="判定时间">{{ originalJudgment.judgeTime ?? originalJudgment.judgmentTime }}</el-descriptions-item>
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
          <el-col :span="24" v-if="!fromRouteJudgment">
            <el-form-item label="原判定记录" prop="judgmentId">
              <el-button type="primary" plain @click="openJudgmentPicker">
                {{ formData.judgmentId ? '重新选择原判定记录' : '选择原判定记录' }}
              </el-button>
              <span v-if="formData.judgmentId" class="selected-judgment-id">已选 ID：{{ formData.judgmentId }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目标结论" prop="targetJudgmentType">
              <el-select v-model="formData.targetJudgmentType" placeholder="请选择目标结论" style="width:100%" @change="checkReverse">
                <el-option
                  v-for="item in rejudgmentTargetOptions"
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

    <!-- 选择原判定记录 -->
    <el-dialog v-model="pickerVisible" title="选择原判定记录" width="960px" destroy-on-close>
      <el-form :model="pickerSearch" inline @submit.prevent="searchPickerList">
        <el-form-item label="卷号">
          <el-input v-model="pickerSearch.coilNo" placeholder="输入卷号" clearable style="width:150px" />
        </el-form-item>
        <el-form-item label="批次号">
          <el-input v-model="pickerSearch.batchNo" placeholder="输入批次号" clearable style="width:150px" />
        </el-form-item>
        <el-form-item label="判定结论">
          <el-select v-model="pickerSearch.judgmentType" placeholder="请选择" clearable style="width:140px">
            <el-option
              v-for="item in dictStore.getItems('JUDGMENT_TYPE')"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="判定时间">
          <el-date-picker
            v-model="pickerSearch.timeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD"
            style="width:220px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit" :loading="pickerLoading">查询</el-button>
          <el-button @click.prevent="resetPickerSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        ref="pickerTableRef"
        v-loading="pickerLoading"
        :data="pickerList"
        border
        stripe
        row-key="id"
        max-height="360"
        style="width:100%;margin-top:8px"
        @selection-change="onPickerSelectionChange"
      >
        <el-table-column type="selection" width="48" />
        <el-table-column prop="coilNo" label="卷号" width="130" show-overflow-tooltip />
        <el-table-column prop="batchNo" label="批次号" width="130" show-overflow-tooltip />
        <el-table-column prop="heatNo" label="炉号" width="120" show-overflow-tooltip />
        <el-table-column label="品种" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            {{ dictStore.getLabel('PRODUCT_VARIETY', row.productVariety) }}
          </template>
        </el-table-column>
        <el-table-column prop="productGrade" label="牌号" width="90" show-overflow-tooltip />
        <el-table-column label="样品类型" width="90" show-overflow-tooltip>
          <template #default="{ row }">
            {{ dictStore.getLabel('SAMPLE_TYPE', row.sampleType) || '—' }}
          </template>
        </el-table-column>
        <el-table-column label="客户" width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ formatCustomer(row.customerId) }}</template>
        </el-table-column>
        <el-table-column label="判定结论" width="110">
          <template #default="{ row }">
            <el-tag :type="dictStore.getColorTag('JUDGMENT_TYPE', row.judgmentType) as any" size="small">
              {{ dictStore.getLabel('JUDGMENT_TYPE', row.judgmentType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="judgmentTime" label="判定时间" width="160" />
        <el-table-column prop="inspector" label="检验人" width="100" show-overflow-tooltip />
      </el-table>
      <el-pagination
        v-model:current-page="pickerPageNum"
        v-model:page-size="pickerPageSize"
        :total="pickerTotal"
        :page-sizes="[10, 20]"
        layout="total, prev, pager, next"
        small
        style="margin-top:12px;justify-content:flex-end"
        @current-change="loadPickerList"
        @size-change="onPickerPageSizeChange"
      />
      <p v-if="!pickerLoading && pickerList.length === 0" class="picker-empty-hint">
        未找到符合条件的判定记录
      </p>

      <template #footer>
        <el-button @click="pickerVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!pickerSelectedId" @click="confirmPickerSelection">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, TableInstance, UploadFile } from 'element-plus'
import { WarningFilled, UploadFilled } from '@element-plus/icons-vue'
import { useDictStore } from '@/store/dict'
import { applyRejudgment } from '@/api/rejudgment'
import { getJudgmentByRecord, getJudgmentSnapshot, pageJudgments, type JudgmentListItem } from '@/api/judgment'
import { uploadFile } from '@/api/file'
import type { PageResult } from '@/types'

const route = useRoute()
const router = useRouter()
const dictStore = useDictStore()

const initialJudgmentId = (route.query.judgmentId as string) || (route.query.id as string) || ''
const initialRecordId = (route.query.recordId as string) || ''

const fromRouteJudgment = computed(
  () => !!(initialJudgmentId || initialRecordId)
)

const formRef = ref<FormInstance>()
const uploadRef = ref<any>()
const submitLoading = ref(false)
const judgmentLoading = ref(!!(initialJudgmentId || initialRecordId))
const originalJudgment = ref<any>(null)
const fileList = ref<UploadFile[]>([])

const formData = reactive({
  judgmentId: initialJudgmentId,
  recordId: initialRecordId,
  targetJudgmentType: (route.query.targetJudgmentType as string) || '',
  reason: (route.query.reason as string) || '',
  impactScope: (route.query.impactScope as string) || '',
  evidenceSource: '',
  evidenceFile: null as File | null
})

const pickerVisible = ref(false)
const pickerLoading = ref(false)
const pickerList = ref<JudgmentListItem[]>([])
const pickerPageNum = ref(1)
const pickerPageSize = ref(10)
const pickerTotal = ref(0)
const pickerSelectedId = ref('')
const pickerTableRef = ref<TableInstance>()
function formatCustomer(customerId?: string) {
  if (!customerId) return '—'
  return dictStore.getLabel('QC_CUSTOMER', customerId) || customerId
}

const pickerSearch = reactive({
  coilNo: '',
  batchNo: '',
  judgmentType: '',
  timeRange: null as [string, string] | null
})

const rejudgmentTargetOptions = computed(() =>
  dictStore.getItems('JUDGMENT_TYPE').filter((item) => item.value !== 'STANDARD_CONFLICT')
)

// 逆向改判类型：合格/让步 → 不合格/需复检
const POSITIVE_TYPES = ['QUALIFIED', 'CAN_CONCESSION', 'CONCESSION']
const NEGATIVE_TYPES = ['UNQUALIFIED', 'NEED_REINSPECTION', 'REINSPECTION']

const isReverse = computed(() => {
  if (!originalJudgment.value || !formData.targetJudgmentType) return false
  const origType = originalJudgment.value.judgmentType
  const targetType = formData.targetJudgmentType
  return POSITIVE_TYPES.includes(origType) && NEGATIVE_TYPES.includes(targetType)
})

const baseRules = {
  judgmentId: [{ required: true, message: '请选择原判定记录', trigger: 'change' }],
  targetJudgmentType: [{ required: true, message: '请选择目标结论', trigger: 'change' }],
  reason: [{ required: true, message: '请填写改判原因', trigger: 'blur' }]
}

const formRules = computed(() => {
  const rules: any = { ...baseRules }
  if (fromRouteJudgment.value) {
    delete rules.judgmentId
  }
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

function applySelectedJudgment(row: JudgmentListItem) {
  originalJudgment.value = {
    ...row,
    judgmentType: row.judgmentType,
    judgeTime: row.judgmentTime
  }
  formData.judgmentId = row.id
  formData.recordId = row.recordId || ''
  pickerSelectedId.value = row.id
}

async function loadOriginalJudgment() {
  if (!formData.judgmentId && !formData.recordId) return
  judgmentLoading.value = true
  try {
    let res: any
    if (formData.recordId) {
      res = await getJudgmentByRecord(formData.recordId)
    } else if (formData.judgmentId) {
      res = await getJudgmentSnapshot(formData.judgmentId).catch(() => null)
    }
    if (res) {
      originalJudgment.value = res
      if (res.judgmentId) {
        formData.judgmentId = res.judgmentId
      }
      if (res.recordId) {
        formData.recordId = res.recordId
      }
    }
  } catch {
    /* 列表已选时保留简要信息 */
  } finally {
    judgmentLoading.value = false
  }
}

function openJudgmentPicker() {
  pickerSelectedId.value = formData.judgmentId || ''
  pickerVisible.value = true
  loadPickerList()
}

function resetPickerSearch() {
  Object.assign(pickerSearch, { coilNo: '', batchNo: '', judgmentType: '', timeRange: null })
  pickerPageNum.value = 1
  loadPickerList()
}

function searchPickerList() {
  pickerPageNum.value = 1
  loadPickerList()
}

function onPickerPageSizeChange() {
  pickerPageNum.value = 1
  loadPickerList()
}

async function loadPickerList() {
  pickerLoading.value = true
  try {
    const params: Record<string, unknown> = {
      pageNum: pickerPageNum.value,
      pageSize: pickerPageSize.value,
      isFinal: 1,
      coilNo: pickerSearch.coilNo?.trim() || undefined,
      batchNo: pickerSearch.batchNo?.trim() || undefined,
      judgmentType: pickerSearch.judgmentType || undefined
    }
    if (pickerSearch.timeRange) {
      params.timeStart = `${pickerSearch.timeRange[0]} 00:00:00`
      params.timeEnd = `${pickerSearch.timeRange[1]} 23:59:59`
    }
    const res = await pageJudgments(params as any) as PageResult<JudgmentListItem>
    pickerList.value = res.records || []
    pickerTotal.value = res.total || 0
    await nextTick()
    syncPickerTableSelection()
  } finally {
    pickerLoading.value = false
  }
}

function syncPickerTableSelection() {
  const table = pickerTableRef.value
  if (!table) return
  table.clearSelection()
  if (!pickerSelectedId.value) return
  const row = pickerList.value.find((r) => r.id === pickerSelectedId.value)
  if (row) {
    table.toggleRowSelection(row, true)
  }
}

function onPickerSelectionChange(rows: JudgmentListItem[]) {
  if (rows.length === 0) {
    pickerSelectedId.value = ''
    return
  }
  const selected = rows[rows.length - 1]
  if (rows.length > 1) {
    pickerTableRef.value?.clearSelection()
    pickerTableRef.value?.toggleRowSelection(selected, true)
  }
  pickerSelectedId.value = selected.id
}

function confirmPickerSelection() {
  if (!pickerSelectedId.value) {
    ElMessage.warning('请选择一条判定记录')
    return
  }
  const row = pickerList.value.find((r) => r.id === pickerSelectedId.value)
  if (row) {
    applySelectedJudgment(row)
  } else {
    formData.judgmentId = pickerSelectedId.value
  }
  pickerVisible.value = false
  formRef.value?.validateField('judgmentId')
  loadOriginalJudgment()
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  if (!formData.judgmentId) {
    ElMessage.warning('请选择原判定记录')
    return
  }
  if (isReverse.value && !formData.evidenceFile) {
    ElMessage.error('逆向改判必须上传证据附件')
    return
  }
  submitLoading.value = true
  try {
    let evidenceAttachmentUrl: string | undefined
    if (isReverse.value && formData.evidenceFile) {
      evidenceAttachmentUrl = await uploadFile(formData.evidenceFile, 'rejudgment-evidence')
    }
    await applyRejudgment({
      originalJudgmentId: formData.judgmentId,
      targetJudgmentType: formData.targetJudgmentType,
      rejudgmentReason: formData.reason,
      affectScope: formData.impactScope || '-',
      ...(isReverse.value
        ? {
            newEvidenceSource: formData.evidenceSource,
            evidenceAttachmentUrl
          }
        : {})
    })
    ElMessage.success('改判申请已提交')
    router.push('/re-judgment')
  } finally {
    submitLoading.value = false
  }
}

onMounted(async () => {
  await dictStore.refreshItems('QC_CUSTOMER')
  if (formData.judgmentId || formData.recordId) {
    await loadOriginalJudgment()
  }
})
</script>

<style scoped>
.rejudgment-form-page {
  padding: 16px;
  max-width: 960px;
}
.judgment-info-card.is-ai-loading {
  min-height: 200px;
  position: relative;
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
.selected-judgment-id {
  margin-left: 12px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.picker-empty-hint {
  margin-top: 8px;
  text-align: center;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
</style>
