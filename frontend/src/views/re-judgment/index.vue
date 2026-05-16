<template>
  <div class="rejudgment-page">
    <!-- 搜索 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="审批状态">
          <el-select v-model="searchForm.approvalStatus" placeholder="请选择" clearable style="width:140px">
            <el-option
              v-for="item in dictStore.getItems('APPROVAL_STATUS')"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="改判类型">
          <el-select v-model="searchForm.isReverse" placeholder="请选择" clearable style="width:130px">
            <el-option label="常规改判" value="false" />
            <el-option label="逆向改判" value="true" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleApply">发起改判申请</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-card shadow="never" style="margin-top:12px">
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width:100%"
        :row-class-name="rowClassName"
      >
        <el-table-column label="判定变更" width="200">
          <template #default="{ row }">
            <div style="display:flex;align-items:center;gap:4px">
              <el-tag :type="dictStore.getColorTag('JUDGMENT_TYPE', row.originalJudgmentType) as any" size="small">
                {{ dictStore.getLabel('JUDGMENT_TYPE', row.originalJudgmentType) }}
              </el-tag>
              <el-icon><ArrowRight /></el-icon>
              <el-tag :type="dictStore.getColorTag('JUDGMENT_TYPE', row.targetJudgmentType) as any" size="small">
                {{ dictStore.getLabel('JUDGMENT_TYPE', row.targetJudgmentType) }}
              </el-tag>
            </div>
            <div style="font-size:12px;color:#666;margin-top:4px">卷号：{{ row.coilNo }}</div>
          </template>
        </el-table-column>
        <el-table-column
          prop="reason"
          label="改判原因"
          min-width="180"
          show-overflow-tooltip
          :filters="getFilters('reason')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column label="逆向改判" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isReverse" type="danger" size="small" style="font-weight:600">逆向改判</el-tag>
            <span v-else style="color:#c0c4cc">-</span>
          </template>
        </el-table-column>
        <el-table-column label="审批级别" width="110">
          <template #default="{ row }">
            {{ row.approvalLevel || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="审批状态" width="110">
          <template #default="{ row }">
            <el-tag :type="dictStore.getColorTag('APPROVAL_STATUS', row.approvalStatus) as any">
              {{ dictStore.getLabel('APPROVAL_STATUS', row.approvalStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="applyTime"
          label="申请时间"
          width="160"
          :filters="getFilters('applyTime')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="applyByName"
          label="申请人"
          width="90"
          :filters="getFilters('applyByName')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">查看详情</el-button>
            <el-button
              v-if="row.approvalStatus === 'PENDING'"
              link
              type="warning"
              @click="handleApprove(row)"
            >审批</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top:16px;justify-content:flex-end"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <!-- 审批弹窗 -->
    <el-dialog v-model="approveDialogVisible" title="改判审批" width="500px">
      <div v-if="approveTarget" style="margin-bottom:12px">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="卷号">{{ approveTarget.coilNo }}</el-descriptions-item>
          <el-descriptions-item label="改判方向">
            {{ dictStore.getLabel('JUDGMENT_TYPE', approveTarget.originalJudgmentType) }}
            →
            {{ dictStore.getLabel('JUDGMENT_TYPE', approveTarget.targetJudgmentType) }}
          </el-descriptions-item>
          <el-descriptions-item label="改判原因" :span="2">{{ approveTarget.reason }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <el-form ref="approveFormRef" :model="approveForm" :rules="approveRules" label-width="80px">
        <el-form-item label="审批意见" prop="comment">
          <el-input v-model="approveForm.comment" type="textarea" :rows="3" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="approveLoading" @click="submitApprove('REJECTED')">驳回</el-button>
        <el-button type="success" :loading="approveLoading" @click="submitApprove('APPROVED')">批准</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { ArrowRight } from '@element-plus/icons-vue'
import { useDictStore } from '@/store/dict'
import { useTableFilter } from '@/composables/use-table-filter'
import { pageRejudgments, approveRejudgment } from '@/api/rejudgment'
import type { PageResult } from '@/types'

const router = useRouter()
const dictStore = useDictStore()

const searchForm = reactive({ approvalStatus: '', isReverse: '' })
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const loading = ref(false)
const tableData = ref<any[]>([])
const { getFilters, filterMethod } = useTableFilter(tableData)

const approveDialogVisible = ref(false)
const approveLoading = ref(false)
const approveTarget = ref<any>(null)
const approveFormRef = ref<FormInstance>()
const approveForm = reactive({ comment: '' })
const approveRules = {
  comment: [{ required: true, message: '请输入审批意见', trigger: 'blur' }]
}

function rowClassName({ row }: { row: any }) {
  return row.isReverse ? 'reverse-row' : ''
}

async function loadData() {
  loading.value = true
  try {
    const res = await pageRejudgments({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      approvalStatus: searchForm.approvalStatus || undefined,
      isReverse: searchForm.isReverse !== '' ? searchForm.isReverse === 'true' : undefined
    }) as PageResult<any>
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handleReset() {
  Object.assign(searchForm, { approvalStatus: '', isReverse: '' })
  pageNum.value = 1
  loadData()
}

function handleApply() {
  router.push('/re-judgment/form')
}

function viewDetail(row: any) {
  router.push(`/re-judgment/detail?id=${row.id}`)
}

function handleApprove(row: any) {
  approveTarget.value = row
  approveForm.comment = ''
  approveDialogVisible.value = true
}

async function submitApprove(decision: string) {
  await approveFormRef.value?.validate()
  approveLoading.value = true
  try {
    await approveRejudgment(approveTarget.value.id, {
      decision,
      comment: approveForm.comment
    })
    ElMessage.success(decision === 'APPROVED' ? '审批通过' : '已驳回')
    approveDialogVisible.value = false
    loadData()
  } finally {
    approveLoading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.rejudgment-page {
  padding: 16px;
}
.search-card :deep(.el-card__body) {
  padding: 16px 16px 0;
}
:deep(.reverse-row) {
  background-color: #fff0f0 !important;
}
:deep(.reverse-row:hover td) {
  background-color: #ffe4e4 !important;
}
</style>
