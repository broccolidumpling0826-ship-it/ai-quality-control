<template>
  <div class="rejudgment-detail-page" v-loading="loading">
    <el-page-header @back="router.back()" content="改判申请详情" style="margin-bottom:16px" />

    <template v-if="detail">
      <!-- 申请基本信息 -->
      <el-card shadow="never" style="margin-bottom:12px">
        <template #header>
          <div style="display:flex;align-items:center;justify-content:space-between">
            <span style="font-weight:600">申请信息</span>
            <el-tag :type="dictStore.getColorTag('APPROVAL_STATUS', detail.approvalStatus) as any" size="large">
              {{ dictStore.getLabel('APPROVAL_STATUS', detail.approvalStatus) }}
            </el-tag>
          </div>
        </template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="卷号">{{ detail.coilNo }}</el-descriptions-item>
          <el-descriptions-item label="批次号">{{ detail.batchNo }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ detail.applyByName }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ detail.applyTime }}</el-descriptions-item>
          <el-descriptions-item label="改判类型">
            <el-tag v-if="detail.isReverse" type="danger" size="small">逆向改判</el-tag>
            <el-tag v-else type="info" size="small">常规改判</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审批级别">{{ detail.approvalLevel || '-' }}</el-descriptions-item>
          <el-descriptions-item label="原判定结论">
            <el-tag :type="dictStore.getColorTag('JUDGMENT_TYPE', detail.originalJudgmentType) as any" size="small">
              {{ dictStore.getLabel('JUDGMENT_TYPE', detail.originalJudgmentType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="目标结论">
            <el-tag :type="dictStore.getColorTag('JUDGMENT_TYPE', detail.targetJudgmentType) as any" size="small">
              {{ dictStore.getLabel('JUDGMENT_TYPE', detail.targetJudgmentType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="影响范围">{{ detail.affectScope ?? detail.impactScope ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="改判原因" :span="3">{{ detail.rejudgmentReason ?? detail.reason }}</el-descriptions-item>
          <el-descriptions-item v-if="detail.isReverse" label="新证据来源">
            {{ dictStore.getLabel('NEW_EVIDENCE_SOURCE', detail.evidenceSource) || detail.evidenceSource }}
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.evidenceFileUrl" label="证据附件" :span="2">
            <el-link :href="detail.evidenceFileUrl" type="primary" target="_blank">
              {{ detail.evidenceFileName || '查看附件' }}
            </el-link>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 审批历史时间线 -->
      <el-card shadow="never">
        <template #header><span style="font-weight:600">审批历史</span></template>
        <el-timeline v-if="(detail.approvalRecords ?? detail.approvalHistory)?.length">
          <el-timeline-item
            v-for="(item, index) in (detail.approvalRecords ?? detail.approvalHistory)"
            :key="index"
            :type="timelineItemType(item.decision ?? item.approvalAction)"
            :timestamp="item.approveTime ?? item.approvalTime"
            placement="top"
          >
            <el-card shadow="never" style="padding:0">
              <div style="display:flex;align-items:center;gap:8px;margin-bottom:6px">
                <strong>{{ item.approverName ?? item.approverNo }}</strong>
                <el-tag :type="timelineItemType(item.decision ?? item.approvalAction)" size="small">
                  {{ decisionLabel(item.decision ?? item.approvalAction) }}
                </el-tag>
                <span v-if="item.approvalLevel" class="text-muted" style="font-size:12px">
                  [{{ item.approvalLevel }}]
                </span>
              </div>
              <p class="text-secondary" style="margin:0">{{ item.comment ?? item.approvalComment ?? '无审批意见' }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无审批记录" />
      </el-card>
    </template>

    <el-empty v-if="!loading && !detail" description="未找到改判申请记录" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDictStore } from '@/store/dict'
import { getRejudgmentById } from '@/api/rejudgment'

const route = useRoute()
const router = useRouter()
const dictStore = useDictStore()

const loading = ref(false)
const detail = ref<any>(null)

function timelineItemType(decision: string): any {
  const map: Record<string, string> = {
    APPROVED: 'success',
    REJECTED: 'danger',
    PENDING: 'warning'
  }
  return map[decision] || 'info'
}

function decisionLabel(decision: string): string {
  const map: Record<string, string> = {
    APPROVED: '批准',
    REJECTED: '驳回',
    PENDING: '待审批'
  }
  return map[decision] || decision
}

async function loadDetail() {
  const id = route.query.id as string
  if (!id) return
  loading.value = true
  try {
    detail.value = await getRejudgmentById(id)
  } finally {
    loading.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.rejudgment-detail-page {
  padding: 16px;
  max-width: 900px;
}
</style>
