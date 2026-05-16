<template>
  <div class="indicator-page">
    <!-- 搜索 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="指标名称">
          <el-input v-model="searchForm.indicatorName" placeholder="输入指标名称" clearable style="width:180px" />
        </el-form-item>
        <el-form-item label="类别">
          <el-select v-model="searchForm.category" placeholder="请选择" clearable style="width:140px">
            <el-option
              v-for="item in dictStore.getItems('INDICATOR_CATEGORY')"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">新增指标</el-button>
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
        <el-table-column
          prop="indicatorName"
          label="指标名称"
          min-width="160"
          :filters="getFilters('indicatorName')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="indicatorCode"
          label="指标代码"
          width="130"
          :filters="getFilters('indicatorCode')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="category"
          label="类别"
          width="120"
          :filters="getFilters('category')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        >
          <template #default="{ row }">
            <el-tag :type="dictStore.getColorTag('INDICATOR_CATEGORY', row.category) as any">
              {{ dictStore.getLabel('INDICATOR_CATEGORY', row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="unit"
          label="单位"
          width="80"
          :filters="getFilters('unit')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="testMethod"
          label="检测方法"
          min-width="150"
          :filters="getFilters('testMethod')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="status"
          label="状态"
          width="90"
          :filters="getFilters('status')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        >
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.status === 'ACTIVE' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button
              link
              :type="row.status === 'ACTIVE' ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >{{ row.status === 'ACTIVE' ? '停用' : '启用' }}</el-button>
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

    <!-- 新增/编辑 Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingRow ? '编辑指标' : '新增指标'"
      width="520px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="指标名称" prop="indicatorName">
          <el-input v-model="formData.indicatorName" placeholder="请输入指标名称" />
        </el-form-item>
        <el-form-item label="指标代码" prop="indicatorCode">
          <el-input v-model="formData.indicatorCode" placeholder="如：C_CONTENT" />
        </el-form-item>
        <el-form-item label="类别" prop="category">
          <el-select v-model="formData.category" placeholder="请选择" style="width:100%">
            <el-option
              v-for="item in dictStore.getItems('INDICATOR_CATEGORY')"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="formData.unit" placeholder="如：%、MPa" />
        </el-form-item>
        <el-form-item label="检测方法">
          <el-input v-model="formData.testMethod" placeholder="检测方法（选填）" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="备注（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useDictStore } from '@/store/dict'
import { useTableFilter } from '@/composables/use-table-filter'
import {
  pageIndicators,
  addIndicator,
  updateIndicator,
  updateIndicatorStatus
} from '@/api/indicator'

const dictStore = useDictStore()

const searchForm = reactive({ indicatorName: '', category: '' })
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const loading = ref(false)
const tableData = ref<any[]>([])
const { getFilters, filterMethod } = useTableFilter(tableData)

const dialogVisible = ref(false)
const editingRow = ref<any>(null)
const formRef = ref<FormInstance>()
const submitLoading = ref(false)

const formData = reactive({
  id: '',
  indicatorName: '',
  indicatorCode: '',
  category: '',
  unit: '',
  testMethod: '',
  remark: ''
})

const formRules = {
  indicatorName: [{ required: true, message: '请输入指标名称', trigger: 'blur' }],
  indicatorCode: [{ required: true, message: '请输入指标代码', trigger: 'blur' }],
  category: [{ required: true, message: '请选择类别', trigger: 'change' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await pageIndicators({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      ...searchForm
    }) as any
    tableData.value = (res.records || []).map((row: any) => ({
      ...row,
      category: row.category || row.indicatorCategory
    }))
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
  Object.assign(searchForm, { indicatorName: '', category: '' })
  pageNum.value = 1
  loadData()
}

function handleAdd() {
  editingRow.value = null
  Object.assign(formData, { id: '', indicatorName: '', indicatorCode: '', category: '', unit: '', testMethod: '', remark: '' })
  dialogVisible.value = true
}

function handleEdit(row: any) {
  editingRow.value = row
  Object.assign(formData, {
    ...row,
    category: row.category || row.indicatorCategory,
    remark: row.remark ?? row.description ?? ''
  })
  dialogVisible.value = true
}

async function toggleStatus(row: any) {
  const newStatus = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  try {
    await updateIndicatorStatus(row.id, newStatus)
    row.status = newStatus
    ElMessage.success('操作成功')
  } catch {}
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    const payload = {
      indicatorName: formData.indicatorName,
      indicatorCode: formData.indicatorCode,
      category: formData.category,
      unit: formData.unit,
      testMethod: formData.testMethod,
      remark: formData.remark
    }
    if (editingRow.value) {
      await updateIndicator(formData.id, payload)
      ElMessage.success('更新成功')
    } else {
      await addIndicator(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.indicator-page {
  padding: 16px;
}
.search-card :deep(.el-card__body) {
  padding: 16px 16px 0;
}
</style>
