<template>
  <div class="explanation-page" v-loading="loading">
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

    <!-- 指标明细 -->
    <el-card shadow="never" style="margin-top:12px" v-if="detail">
      <template #header><span style="font-weight:600">指标明细</span></template>
      <el-table :data="detail.indicatorDetails" border>
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
              {{ dictStore.getLabel('INDICATOR_RESULT', row.indicatorResult) || row.indicatorResult }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 底部操作 -->
    <div class="bottom-bar" v-if="detail">
      <el-button @click="router.back()">返回</el-button>
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
import { getJudgmentExplanation, getJudgmentByRecord } from '@/api/judgment'
import { initiateReinspection } from '@/api/reinspection'

const route = useRoute()
const router = useRouter()
const dictStore = useDictStore()

const loading = ref(false)
const detail = ref<any>(null)
const actionLoading = ref(false)
const indicatorDetails = computed(() => detail.value?.indicatorDetails ?? [])
const { getFilters: getIndicatorFilters, filterMethod: indicatorFilterMethod } = useTableFilter(indicatorDetails)

const reinspectionDialogVisible = ref(false)
const reinspectionFormRef = ref<FormInstance>()
const reinspectionForm = ref({ reinspectionReason: '', responsibleNo: '' })
const reinspectionRules = {
  reinspectionReason: [{ required: true, message: '请输入复检原因', trigger: 'blur' }]
}

const canInitiateReinspection = computed(() =>
  detail.value?.judgmentType === 'UNQUALIFIED' || detail.value?.judgmentType === 'REINSPECTION'
)
const canApplyRejudgment = computed(() => !!detail.value?.judgmentType)
const canApplyConcession = computed(() =>
  detail.value?.judgmentType === 'UNQUALIFIED'
)

function judgmentColor(type: string): any {
  const map: Record<string, string> = {
    QUALIFIED: 'success',
    UNQUALIFIED: 'danger',
    CONCESSION: 'warning',
    REINSPECTION: 'info'
  }
  return map[type] || 'info'
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

function indicatorResultColor(result: string): any {
  const map: Record<string, string> = {
    PASS: 'success',
    FAIL: 'danger',
    WARNING: 'warning'
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
      res = await getJudgmentByRecord(route.query.recordId as string)
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
  router.push(`/re-judgment/form?judgmentId=${jid}`)
}

function handleConcession() {
  const jid = detail.value?.judgmentId ?? detail.value?.id
  router.push(`/concession/apply?judgmentId=${jid}`)
}

onMounted(loadDetail)
</script>

<style scoped>
.explanation-page {
  padding: 16px;
  max-width: 1200px;
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
.text-danger {
  color: #f56c6c;
  font-weight: 600;
}
</style>
