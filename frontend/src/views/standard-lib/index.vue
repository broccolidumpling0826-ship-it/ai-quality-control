<template>
  <div class="standard-lib-page">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="标准类型">
          <el-select v-model="searchForm.standardType" placeholder="请选择" clearable style="width:140px">
            <el-option
              v-for="item in dictStore.getItems('STANDARD_TYPE')"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="品种">
          <el-select v-model="searchForm.productVariety" placeholder="请选择" clearable style="width:140px">
            <el-option
              v-for="item in dictStore.getItems('PRODUCT_VARIETY')"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="牌号">
          <el-input v-model="searchForm.productGrade" placeholder="输入牌号" clearable style="width:140px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width:120px">
            <el-option
              v-for="item in dictStore.getItems('STANDARD_STATUS')"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">新增标准</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表区域 -->
    <el-card shadow="never" style="margin-top:12px">
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width:100%"
      >
        <el-table-column
          prop="standardCode"
          label="标准编号"
          width="150"
          :filters="getFilters('standardCode')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="standardType"
          label="标准类型"
          width="120"
          :filters="getFilters('standardType')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        >
          <template #default="{ row }">
            <el-tag :type="dictStore.getColorTag('STANDARD_TYPE', row.standardType) as any">
              {{ dictStore.getLabel('STANDARD_TYPE', row.standardType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="standardName"
          label="标准名称"
          min-width="180"
          :filters="getFilters('standardName')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="productVariety"
          label="品种"
          width="100"
          :filters="getFilters('productVariety')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        >
          <template #default="{ row }">
            {{ dictStore.getLabel('PRODUCT_VARIETY', row.productVariety) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="productGrade"
          label="牌号"
          width="120"
          :filters="getFilters('productGrade')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="version"
          label="版本号"
          width="80"
          :filters="getFilters('version')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="effectiveDate"
          label="生效日期"
          width="110"
          :filters="getFilters('effectiveDate')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="expiryDate"
          label="失效日期"
          width="110"
          :filters="getFilters('expiryDate')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        >
          <template #default="{ row }">
            <span :class="{ 'text-warning': isExpiringSoon(row.expiryDate) }">
              {{ row.expiryDate || '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column
          prop="status"
          label="状态"
          width="90"
          :filters="getFilters('status')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        >
          <template #default="{ row }">
            <el-tag :type="dictStore.getColorTag('STANDARD_STATUS', row.status) as any">
              {{ dictStore.getLabel('STANDARD_STATUS', row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button link type="warning" @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status !== 'PUBLISHED'"
              link
              type="success"
              @click="handlePublish(row)"
            >发布</el-button>
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

    <!-- 新增/编辑 Drawer -->
    <el-drawer
      v-model="drawerVisible"
      :title="drawerMode === 'add' ? '新增标准' : (drawerMode === 'edit' ? '编辑标准' : '查看标准')"
      size="800px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        :disabled="drawerMode === 'view'"
      >
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="标准编号" prop="standardCode">
              <el-input v-model="formData.standardCode" placeholder="请输入标准编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标准名称" prop="standardName">
              <el-input v-model="formData.standardName" placeholder="请输入标准名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标准类型" prop="standardType">
              <el-select v-model="formData.standardType" placeholder="请选择" style="width:100%">
                <el-option
                  v-for="item in dictStore.getItems('STANDARD_TYPE')"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品种" prop="productVariety">
              <el-select v-model="formData.productVariety" placeholder="请选择" style="width:100%">
                <el-option
                  v-for="item in dictStore.getItems('PRODUCT_VARIETY')"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="牌号" prop="productGrade">
              <el-input v-model="formData.productGrade" placeholder="请输入牌号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="版本号" prop="version">
              <el-input v-model="formData.version" placeholder="如：V1.0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生效日期" prop="effectiveDate">
              <el-date-picker
                v-model="formData.effectiveDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width:100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="失效日期" prop="expiryDate">
              <el-date-picker
                v-model="formData.expiryDate"
                type="date"
                placeholder="选择日期（可为空）"
                value-format="YYYY-MM-DD"
                style="width:100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="描述">
              <el-input v-model="formData.description" type="textarea" :rows="2" placeholder="标准描述（选填）" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 指标列表 -->
        <div style="margin-top:16px">
          <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:8px">
            <span style="font-weight:600;font-size:14px">指标列表</span>
            <el-button
              v-if="drawerMode !== 'view'"
              type="primary"
              size="small"
              @click="addIndicatorRow"
            >添加指标</el-button>
          </div>
          <el-table :data="formData.indicators" border size="small">
            <el-table-column label="指标名称" min-width="130">
              <template #default="{ row }">
                <el-input v-if="drawerMode !== 'view'" v-model="row.indicatorName" size="small" placeholder="指标名称" />
                <span v-else>{{ row.indicatorName }}</span>
              </template>
            </el-table-column>
            <el-table-column label="指标代码" width="110">
              <template #default="{ row }">
                <el-input v-if="drawerMode !== 'view'" v-model="row.indicatorCode" size="small" placeholder="代码" />
                <span v-else>{{ row.indicatorCode }}</span>
              </template>
            </el-table-column>
            <el-table-column label="类别" width="110">
              <template #default="{ row }">
                <el-select v-if="drawerMode !== 'view'" v-model="row.category" size="small" style="width:100%">
                  <el-option
                    v-for="item in dictStore.getItems('INDICATOR_CATEGORY')"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
                <el-tag v-else :type="dictStore.getColorTag('INDICATOR_CATEGORY', row.category) as any" size="small">
                  {{ dictStore.getLabel('INDICATOR_CATEGORY', row.category) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="单位" width="70">
              <template #default="{ row }">
                <el-input v-if="drawerMode !== 'view'" v-model="row.unit" size="small" placeholder="单位" />
                <span v-else>{{ row.unit }}</span>
              </template>
            </el-table-column>
            <el-table-column label="下限" width="90">
              <template #default="{ row }">
                <el-input v-if="drawerMode !== 'view'" v-model="row.lowerLimit" size="small" placeholder="-" />
                <span v-else>{{ row.lowerLimit ?? '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="上限" width="90">
              <template #default="{ row }">
                <el-input v-if="drawerMode !== 'view'" v-model="row.upperLimit" size="small" placeholder="-" />
                <span v-else>{{ row.upperLimit ?? '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column v-if="drawerMode !== 'view'" label="操作" width="70" align="center">
              <template #default="{ $index }">
                <el-button link type="danger" size="small" @click="removeIndicatorRow($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-form>

      <template #footer>
        <div v-if="drawerMode !== 'view'" style="display:flex;justify-content:flex-end;gap:8px">
          <el-button @click="drawerVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
        </div>
        <div v-else style="display:flex;justify-content:flex-end">
          <el-button @click="drawerVisible = false">关闭</el-button>
        </div>
      </template>
    </el-drawer>

    <!-- 发布确认对话框 -->
    <el-dialog v-model="publishDialogVisible" title="发布确认" width="440px">
      <p>确认发布标准 <strong>{{ publishTarget?.standardName }}</strong>？</p>
      <el-form style="margin-top:12px">
        <el-form-item label="为旧版本设置失效日期">
          <el-date-picker
            v-model="publishExpiryDate"
            type="date"
            placeholder="可选：选择旧版本失效日期"
            value-format="YYYY-MM-DD"
            style="width:100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="success" :loading="publishLoading" @click="confirmPublish">确认发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useDictStore } from '@/store/dict'
import { useTableFilter } from '@/composables/use-table-filter'
import { pageStandards, addStandard, updateStandard, publishStandard, getStandardById } from '@/api/standard'
import type { PageResult } from '@/types'

const dictStore = useDictStore()

// 搜索
const searchForm = reactive({
  standardType: '',
  productVariety: '',
  productGrade: '',
  status: ''
})

// 分页
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const loading = ref(false)
const tableData = ref<any[]>([])
const { getFilters, filterMethod } = useTableFilter(tableData)

// 抽屉
const drawerVisible = ref(false)
const drawerMode = ref<'add' | 'edit' | 'view'>('add')
const formRef = ref<FormInstance>()
const submitLoading = ref(false)

const defaultForm = () => ({
  id: '',
  standardCode: '',
  standardName: '',
  standardType: '',
  productVariety: '',
  productGrade: '',
  version: '',
  effectiveDate: '',
  expiryDate: '',
  description: '',
  indicators: [] as any[]
})

const formData = reactive(defaultForm())

const formRules = {
  standardCode: [{ required: true, message: '请输入标准编号', trigger: 'blur' }],
  standardName: [{ required: true, message: '请输入标准名称', trigger: 'blur' }],
  standardType: [{ required: true, message: '请选择标准类型', trigger: 'change' }],
  effectiveDate: [{ required: true, message: '请选择生效日期', trigger: 'change' }]
}

// 发布
const publishDialogVisible = ref(false)
const publishLoading = ref(false)
const publishTarget = ref<any>(null)
const publishExpiryDate = ref('')

function isExpiringSoon(date: string) {
  if (!date) return false
  const diff = new Date(date).getTime() - Date.now()
  return diff > 0 && diff < 3 * 24 * 3600 * 1000
}

async function loadData() {
  loading.value = true
  try {
    const res = await pageStandards({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      ...searchForm
    }) as PageResult<any>
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch {
    // handled by request interceptor
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handleReset() {
  Object.assign(searchForm, { standardType: '', productVariety: '', productGrade: '', status: '' })
  pageNum.value = 1
  loadData()
}

function handleAdd() {
  Object.assign(formData, defaultForm())
  drawerMode.value = 'add'
  drawerVisible.value = true
}

async function handleEdit(row: any) {
  try {
    const detail = await getStandardById(row.id) as any
    Object.assign(formData, { ...defaultForm(), ...detail, indicators: detail.indicators || [] })
  } catch {
    Object.assign(formData, { ...defaultForm(), ...row, indicators: row.indicators || [] })
  }
  drawerMode.value = 'edit'
  drawerVisible.value = true
}

async function handleView(row: any) {
  try {
    const detail = await getStandardById(row.id) as any
    Object.assign(formData, { ...defaultForm(), ...detail, indicators: detail.indicators || [] })
  } catch {
    Object.assign(formData, { ...defaultForm(), ...row, indicators: row.indicators || [] })
  }
  drawerMode.value = 'view'
  drawerVisible.value = true
}

function handlePublish(row: any) {
  publishTarget.value = row
  publishExpiryDate.value = ''
  publishDialogVisible.value = true
}

async function confirmPublish() {
  if (!publishTarget.value) return
  publishLoading.value = true
  try {
    await publishStandard(publishTarget.value.id)
    ElMessage.success('标准发布成功')
    publishDialogVisible.value = false
    loadData()
  } finally {
    publishLoading.value = false
  }
}

function addIndicatorRow() {
  formData.indicators.push({
    indicatorName: '',
    indicatorCode: '',
    category: '',
    unit: '',
    lowerLimit: '',
    upperLimit: ''
  })
}

function removeIndicatorRow(index: number) {
  formData.indicators.splice(index, 1)
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (drawerMode.value === 'add') {
      await addStandard(formData)
      ElMessage.success('新增成功')
    } else {
      await updateStandard(formData)
      ElMessage.success('保存成功')
    }
    drawerVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.standard-lib-page {
  padding: 16px;
}
.search-card :deep(.el-card__body) {
  padding: 16px 16px 0;
}
.text-warning {
  color: #e6a23c;
}
</style>
