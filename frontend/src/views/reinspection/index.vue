<template>
  <div class="reinspection-page">
    <!-- 搜索 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width:140px">
            <el-option
              v-for="item in dictStore.getItems('REINSPECTION_STATUS')"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="责任人">
          <el-input v-model="searchForm.responsiblePerson" placeholder="输入责任人" clearable style="width:150px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
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
      >
        <el-table-column label="原判定记录" min-width="200">
          <template #default="{ row }">
            <div>卷号：{{ row.coilNo }}</div>
            <div style="color:#666;font-size:12px">
              判定：
              <el-tag :type="dictStore.getColorTag('JUDGMENT_TYPE', row.originalJudgmentType) as any" size="small">
                {{ dictStore.getLabel('JUDGMENT_TYPE', row.originalJudgmentType) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="reason"
          label="复检原因"
          min-width="180"
          show-overflow-tooltip
          :filters="getFilters('reason')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="responsiblePerson"
          label="责任人"
          width="110"
          :filters="getFilters('responsiblePerson')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="createTime"
          label="发起时间"
          width="160"
          :filters="getFilters('createTime')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="status"
          label="状态"
          width="110"
          :filters="getFilters('status')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        >
          <template #default="{ row }">
            <el-tag :type="dictStore.getColorTag('REINSPECTION_STATUS', row.status) as any">
              {{ dictStore.getLabel('REINSPECTION_STATUS', row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="新检验记录" width="130">
          <template #default="{ row }">
            <span v-if="row.newRecordId">
              <el-button link type="primary" @click="viewNewRecord(row)">{{ row.newRecordId }}</el-button>
            </span>
            <span v-else style="color:#c0c4cc">未关联</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              link
              type="success"
              @click="handleComplete(row)"
            >完成复检</el-button>
            <el-button link type="primary" @click="viewOriginal(row)">查看原判定</el-button>
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

    <!-- 完成复检弹窗 -->
    <el-dialog v-model="completeDialogVisible" title="完成复检" width="440px">
      <el-form ref="completeFormRef" :model="completeForm" :rules="completeRules" label-width="120px">
        <el-form-item label="新检验记录ID" prop="newRecordId">
          <el-input v-model="completeForm.newRecordId" placeholder="输入新检验记录ID" />
        </el-form-item>
        <p style="color:#999;font-size:12px;margin-left:120px">填入完成复检后生成的检验记录ID</p>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="completeLoading" @click="confirmComplete">确认完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useDictStore } from '@/store/dict'
import { useTableFilter } from '@/composables/use-table-filter'
import { pageReinspections, completeReinspection } from '@/api/reinspection'
import type { PageResult } from '@/types'

const router = useRouter()
const dictStore = useDictStore()

const searchForm = reactive({ status: '', responsiblePerson: '' })
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const loading = ref(false)
const tableData = ref<any[]>([])
const { getFilters, filterMethod } = useTableFilter(tableData)

const completeDialogVisible = ref(false)
const completeLoading = ref(false)
const completeTarget = ref<any>(null)
const completeFormRef = ref<FormInstance>()
const completeForm = reactive({ newRecordId: '' })
const completeRules = {
  newRecordId: [{ required: true, message: '请输入新检验记录ID', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await pageReinspections({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: searchForm.status || undefined,
      responsiblePerson: searchForm.responsiblePerson || undefined
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
  Object.assign(searchForm, { status: '', responsiblePerson: '' })
  pageNum.value = 1
  loadData()
}

function handleComplete(row: any) {
  completeTarget.value = row
  completeForm.newRecordId = ''
  completeDialogVisible.value = true
}

async function confirmComplete() {
  await completeFormRef.value?.validate()
  completeLoading.value = true
  try {
    await completeReinspection(completeTarget.value.id, completeForm.newRecordId)
    ElMessage.success('复检已完成')
    completeDialogVisible.value = false
    loadData()
  } finally {
    completeLoading.value = false
  }
}

function viewOriginal(row: any) {
  router.push(`/judgment/explanation?id=${row.originalJudgmentId}`)
}

function viewNewRecord(row: any) {
  router.push(`/judgment/explanation?recordId=${row.newRecordId}`)
}

onMounted(loadData)
</script>

<style scoped>
.reinspection-page {
  padding: 16px;
}
.search-card :deep(.el-card__body) {
  padding: 16px 16px 0;
}
</style>
