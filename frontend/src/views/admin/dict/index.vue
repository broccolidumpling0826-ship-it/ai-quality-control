<template>
  <div class="admin-dict-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-title-wrap">
        <span class="page-title-icon">◈</span>
        <div>
          <div class="page-title">数据字典</div>
          <div class="page-subtitle">DATA DICTIONARY</div>
        </div>
      </div>
    </div>

    <!-- 主体分栏 -->
    <div class="dict-body">
      <!-- ── 左侧：字典分类 ── -->
      <div class="dict-left">
        <div class="panel-header">
          <span class="panel-title">字典分类</span>
          <el-button class="btn-cyan-sm" @click="openCategoryDialog(null)">
            <span>＋</span> 新增分类
          </el-button>
        </div>

        <div class="panel-content">
          <el-table
            :data="categoryList"
            v-loading="categoryLoading"
            class="aqc-table category-table"
            row-key="id"
            highlight-current-row
            :header-cell-style="headerCellStyle"
            :cell-style="cellStyle"
            @row-click="handleCategoryClick"
          >
            <el-table-column
              prop="dictCode"
              label="字典编码"
              min-width="110"
              :filters="getCategoryFilters('dictCode')"
              :filter-method="categoryFilterMethod"
              filter-placement="bottom-start"
            >
              <template #default="{ row }">
                <span class="code-text">{{ row.dictCode }}</span>
              </template>
            </el-table-column>
            <el-table-column
              prop="dictName"
              label="字典名称"
              min-width="110"
              :filters="getCategoryFilters('dictName')"
              :filter-method="categoryFilterMethod"
              filter-placement="bottom-start"
            />
            <el-table-column
              prop="status"
              label="状态"
              width="70"
              align="center"
              :filters="getCategoryFilters('status')"
              :filter-method="categoryFilterMethod"
              filter-placement="bottom-start"
            >
              <template #default="{ row }">
                <el-tag
                  :type="row.status === 1 ? 'success' : 'danger'"
                  size="small"
                  effect="dark"
                >{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ row }">
                <span v-if="row.isSystem === 1" class="lock-icon" title="系统字典不可停用">🔒</span>
                <el-button
                  v-else
                  size="small"
                  :class="row.status === 1 ? 'op-btn op-btn-disable' : 'op-btn op-btn-enable'"
                  @click.stop="handleCategoryToggle(row)"
                >{{ row.status === 1 ? '停用' : '启用' }}</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="cat-pagination">
            <el-pagination
              v-model:current-page="catQuery.pageNum"
              v-model:page-size="catQuery.pageSize"
              :total="catTotal"
              layout="prev, pager, next"
              small
              background
              @current-change="loadCategories"
            />
          </div>
        </div>
      </div>

      <!-- ── 右侧：字典项 ── -->
      <div class="dict-right">
        <div class="panel-header">
          <div class="panel-title-wrap">
            <span class="panel-title">字典项</span>
            <span v-if="selectedCategory" class="panel-subtitle">
              — {{ selectedCategory.dictCode }} / {{ selectedCategory.dictName }}
            </span>
          </div>
          <el-button
            class="btn-cyan-sm"
            :disabled="!selectedCategory"
            @click="openItemDialog(null)"
          >
            <span>＋</span> 新增字典项
          </el-button>
        </div>

        <div class="panel-content">
          <div v-if="!selectedCategory" class="empty-hint">
            <span class="empty-icon">◧</span>
            <p>请选择左侧字典分类查看字典项</p>
          </div>

          <el-table
            v-else
            :data="itemList"
            v-loading="itemLoading"
            class="aqc-table"
            row-key="id"
            :header-cell-style="headerCellStyle"
            :cell-style="cellStyle"
          >
            <el-table-column
              prop="itemValue"
              label="字典值"
              width="120"
              :filters="getItemFilters('itemValue')"
              :filter-method="itemFilterMethod"
              filter-placement="bottom-start"
            >
              <template #default="{ row }">
                <span class="code-text">{{ row.itemValue }}</span>
              </template>
            </el-table-column>
            <el-table-column
              prop="itemLabel"
              label="显示文本"
              min-width="120"
              :filters="getItemFilters('itemLabel')"
              :filter-method="itemFilterMethod"
              filter-placement="bottom-start"
            />
            <el-table-column
              prop="colorTag"
              label="标签颜色"
              width="110"
              align="center"
              :filters="getItemFilters('colorTag')"
              :filter-method="itemFilterMethod"
              filter-placement="bottom-start"
            >
              <template #default="{ row }">
                <el-tag
                  v-if="row.colorTag"
                  :type="row.colorTag"
                  size="small"
                  effect="dark"
                >{{ row.itemLabel }}</el-tag>
                <span v-else class="muted-text">—</span>
              </template>
            </el-table-column>
            <el-table-column
              prop="sortNo"
              label="排序"
              width="70"
              align="center"
              :filters="getItemFilters('sortNo')"
              :filter-method="itemFilterMethod"
              filter-placement="bottom-start"
            />
            <el-table-column
              prop="status"
              label="状态"
              width="80"
              align="center"
              :filters="getItemFilters('status')"
              :filter-method="itemFilterMethod"
              filter-placement="bottom-start"
            >
              <template #default="{ row }">
                <el-tag
                  :type="row.status === 1 ? 'success' : 'danger'"
                  size="small"
                  effect="dark"
                >{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ row }">
                <el-button
                  size="small"
                  class="op-btn op-btn-edit"
                  @click="openItemDialog(row)"
                >编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!-- ── 字典分类对话框 ── -->
    <el-dialog
      v-model="categoryDialogVisible"
      :title="editCategoryId ? '编辑字典分类' : '新增字典分类'"
      width="440px"
      :close-on-click-modal="false"
      class="aqc-dialog"
    >
      <el-form
        ref="categoryFormRef"
        :model="categoryForm"
        :rules="categoryRules"
        label-width="80px"
        class="aqc-form"
      >
        <el-form-item label="字典编码" prop="dictCode">
          <el-input
            v-model="categoryForm.dictCode"
            placeholder="如 ORDER_STATUS"
            :disabled="!!editCategoryId"
            class="aqc-input"
          />
        </el-form-item>
        <el-form-item label="字典名称" prop="dictName">
          <el-input
            v-model="categoryForm.dictName"
            placeholder="如 订单状态"
            class="aqc-input"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="categoryForm.remark"
            placeholder="备注说明（选填）"
            class="aqc-input"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button class="btn-ghost" @click="categoryDialogVisible = false">取消</el-button>
          <el-button class="btn-primary-cyan" :loading="categorySubmitting" @click="handleCategorySubmit">
            {{ editCategoryId ? '保存' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ── 字典项对话框 ── -->
    <el-dialog
      v-model="itemDialogVisible"
      :title="editItemId ? '编辑字典项' : '新增字典项'"
      width="440px"
      :close-on-click-modal="false"
      class="aqc-dialog"
    >
      <el-form
        ref="itemFormRef"
        :model="itemForm"
        :rules="itemRules"
        label-width="80px"
        class="aqc-form"
      >
        <el-form-item v-if="!editItemIsSystem" label="字典值" prop="itemValue">
          <el-input
            v-model="itemForm.itemValue"
            placeholder="如 1 或 ACTIVE"
            :disabled="!!editItemId"
            class="aqc-input"
          />
        </el-form-item>
        <el-form-item label="显示文本" prop="itemLabel">
          <el-input
            v-model="itemForm.itemLabel"
            placeholder="如 已激活"
            class="aqc-input"
          />
        </el-form-item>
        <el-form-item label="标签颜色">
          <el-select
            v-model="itemForm.colorTag"
            placeholder="不设置颜色"
            clearable
            class="aqc-select"
            style="width: 100%"
          >
            <el-option value="success">
              <el-tag type="success" size="small" effect="dark">success 绿色</el-tag>
            </el-option>
            <el-option value="danger">
              <el-tag type="danger" size="small" effect="dark">danger 红色</el-tag>
            </el-option>
            <el-option value="warning">
              <el-tag type="warning" size="small" effect="dark">warning 橙色</el-tag>
            </el-option>
            <el-option value="info">
              <el-tag type="info" size="small" effect="dark">info 灰色</el-tag>
            </el-option>
            <el-option value="primary">
              <el-tag type="primary" size="small" effect="dark">primary 主色</el-tag>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input
            v-model.number="itemForm.sortNo"
            type="number"
            placeholder="数字越小越靠前"
            class="aqc-input"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="itemForm.remark"
            placeholder="备注说明（选填）"
            class="aqc-input"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button class="btn-ghost" @click="itemDialogVisible = false">取消</el-button>
          <el-button class="btn-primary-cyan" :loading="itemSubmitting" @click="handleItemSubmit">
            {{ editItemId ? '保存' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useTableFilter } from '@/composables/use-table-filter'
import { dictManageApi } from '@/api/dict-manage'

// ─── 表格样式 ───────────────────────────────────────────────
const headerCellStyle = {
  background: '#0B1929',
  color: '#7A9BBE',
  borderBottom: '1px solid #1A3A5C',
  fontFamily: 'monospace',
  fontSize: '11px',
  letterSpacing: '0.05em',
  textTransform: 'uppercase',
}

const cellStyle = {
  background: 'transparent',
  color: '#C8D8E8',
  borderBottom: '1px solid rgba(26,58,92,0.5)',
  fontSize: '13px',
}

// ─── 字典分类 ───────────────────────────────────────────────
const catQuery = reactive({ pageNum: 1, pageSize: 20 })
const catTotal = ref(0)
const categoryList = ref<any[]>([])
const { getFilters: getCategoryFilters, filterMethod: categoryFilterMethod } = useTableFilter(categoryList)
const categoryLoading = ref(false)
const selectedCategory = ref<any>(null)

async function loadCategories() {
  categoryLoading.value = true
  try {
    const res = await dictManageApi.pageCategories({
      pageNum: catQuery.pageNum,
      pageSize: catQuery.pageSize,
    })
    categoryList.value = res?.records ?? []
    catTotal.value = res?.total ?? 0
  } catch (e) {
    console.error(e)
  } finally {
    categoryLoading.value = false
  }
}

function handleCategoryClick(row: any) {
  selectedCategory.value = row
  loadItems(row.dictCode)
}

async function handleCategoryToggle(row: any) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await dictManageApi.updateCategory(row.id, { ...row, status: newStatus })
    ElMessage.success('状态已更新')
    loadCategories()
  } catch (e) {
    console.error(e)
  }
}

// ─── 字典分类对话框 ─────────────────────────────────────────
const categoryDialogVisible = ref(false)
const editCategoryId = ref<string>('')
const categorySubmitting = ref(false)
const categoryFormRef = ref<FormInstance>()

const categoryForm = reactive({
  dictCode: '',
  dictName: '',
  remark: '',
})

const categoryRules: FormRules = {
  dictCode: [{ required: true, message: '请输入字典编码', trigger: 'blur' }],
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
}

function openCategoryDialog(row: any) {
  if (row) {
    editCategoryId.value = row.id
    categoryForm.dictCode = row.dictCode
    categoryForm.dictName = row.dictName
    categoryForm.remark = row.remark ?? ''
  } else {
    editCategoryId.value = ''
    categoryForm.dictCode = ''
    categoryForm.dictName = ''
    categoryForm.remark = ''
  }
  categoryDialogVisible.value = true
}

async function handleCategorySubmit() {
  if (!categoryFormRef.value) return
  await categoryFormRef.value.validate()
  categorySubmitting.value = true
  try {
    if (editCategoryId.value) {
      await dictManageApi.updateCategory(editCategoryId.value, { ...categoryForm })
      ElMessage.success('字典分类已更新')
    } else {
      await dictManageApi.createCategory({ ...categoryForm })
      ElMessage.success('字典分类已创建')
    }
    categoryDialogVisible.value = false
    loadCategories()
  } catch (e) {
    console.error(e)
  } finally {
    categorySubmitting.value = false
  }
}

// ─── 字典项 ─────────────────────────────────────────────────
const itemList = ref<any[]>([])
const { getFilters: getItemFilters, filterMethod: itemFilterMethod } = useTableFilter(itemList)
const itemLoading = ref(false)

/** 兼容下拉接口(value/label)与管理端完整字段 */
function normalizeDictItem(row: Record<string, unknown>) {
  return {
    ...row,
    id: row.id as string,
    itemValue: (row.itemValue ?? row.value ?? '') as string,
    itemLabel: (row.itemLabel ?? row.label ?? '') as string,
    colorTag: (row.colorTag ?? '') as string,
    sortNo: (row.sortNo ?? 0) as number,
    status: row.status !== undefined && row.status !== null ? Number(row.status) : 1,
    isSystem: row.isSystem ?? 0,
    remark: (row.remark ?? '') as string,
  }
}

async function loadItems(dictCode: string) {
  itemLoading.value = true
  try {
    const res = await dictManageApi.getManageItems(dictCode)
    itemList.value = Array.isArray(res) ? res.map((row) => normalizeDictItem(row as unknown as Record<string, unknown>)) : []
  } catch (e) {
    console.error(e)
  } finally {
    itemLoading.value = false
  }
}

// ─── 字典项对话框 ───────────────────────────────────────────
const itemDialogVisible = ref(false)
const editItemId = ref<string>('')
const editItemIsSystem = ref(false)
const itemSubmitting = ref(false)
const itemFormRef = ref<FormInstance>()

const itemForm = reactive({
  itemValue: '',
  itemLabel: '',
  colorTag: '',
  sortNo: 0,
  remark: '',
})

const itemRules: FormRules = {
  itemValue: [{ required: true, message: '请输入字典值', trigger: 'blur' }],
  itemLabel: [{ required: true, message: '请输入显示文本', trigger: 'blur' }],
}

function openItemDialog(row: any) {
  if (row) {
    editItemId.value = row.id
    editItemIsSystem.value = row.isSystem === 1
    itemForm.itemValue = row.itemValue
    itemForm.itemLabel = row.itemLabel
    itemForm.colorTag = row.colorTag ?? ''
    itemForm.sortNo = row.sortNo ?? 0
    itemForm.remark = row.remark ?? ''
  } else {
    editItemId.value = ''
    editItemIsSystem.value = false
    itemForm.itemValue = ''
    itemForm.itemLabel = ''
    itemForm.colorTag = ''
    itemForm.sortNo = 0
    itemForm.remark = ''
  }
  itemDialogVisible.value = true
}

async function handleItemSubmit() {
  if (!itemFormRef.value) return
  await itemFormRef.value.validate()
  itemSubmitting.value = true
  try {
    if (editItemId.value) {
      await dictManageApi.updateItem(editItemId.value, {
        itemLabel: itemForm.itemLabel,
        colorTag: itemForm.colorTag,
        sortNo: itemForm.sortNo,
        remark: itemForm.remark,
      })
      ElMessage.success('字典项已更新')
    } else {
      await dictManageApi.createItem({
        dictCode: selectedCategory.value?.dictCode,
        itemValue: itemForm.itemValue,
        itemLabel: itemForm.itemLabel,
        colorTag: itemForm.colorTag,
        sortNo: itemForm.sortNo,
        remark: itemForm.remark,
      })
      ElMessage.success('字典项已创建')
    }
    itemDialogVisible.value = false
    if (selectedCategory.value) {
      loadItems(selectedCategory.value.dictCode)
    }
  } catch (e) {
    console.error(e)
  } finally {
    itemSubmitting.value = false
  }
}

// ─── 初始化 ─────────────────────────────────────────────────
onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
/* ══════════════════════════════════════════════════════════════
   数据字典页 — 工业精密风格
══════════════════════════════════════════════════════════════ */

.admin-dict-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
}

/* ── 页面标题 ─────────────────────────────────────────────── */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 16px;
  border-bottom: 1px solid #1A3A5C;
  flex-shrink: 0;
}

.page-title-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title-icon {
  font-size: 20px;
  color: #00D4FF;
  line-height: 1;
}

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #E8F4FF;
  line-height: 1.3;
}

.page-subtitle {
  font-family: monospace;
  font-size: 9px;
  letter-spacing: 0.15em;
  color: #4A6A8A;
  margin-top: 2px;
}

/* ── 主体分栏 ─────────────────────────────────────────────── */
.dict-body {
  display: flex;
  gap: 16px;
  flex: 1;
  overflow: hidden;
  min-height: 400px;
}

/* ── 左侧 ─────────────────────────────────────────────────── */
.dict-left {
  width: 440px;
  min-width: 340px;
  flex-shrink: 0;
  background: #0B1929;
  border: 1px solid #1A3A5C;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ── 右侧 ─────────────────────────────────────────────────── */
.dict-right {
  flex: 1;
  background: #0B1929;
  border: 1px solid #1A3A5C;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

/* ── 面板公共 ─────────────────────────────────────────────── */
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #1A3A5C;
  flex-shrink: 0;
  background: #0F2137;
}

.panel-title {
  font-size: 13px;
  font-weight: 600;
  color: #C8D8E8;
  letter-spacing: 0.05em;
}

.panel-title-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.panel-subtitle {
  font-family: monospace;
  font-size: 11px;
  color: #00D4FF;
}

.panel-content {
  flex: 1;
  overflow: auto;
  display: flex;
  flex-direction: column;
}

/* 空状态 */
.empty-hint {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #2A4A6C;
}

.empty-icon {
  font-size: 36px;
  line-height: 1;
  opacity: 0.4;
}

.empty-hint p {
  font-size: 13px;
  color: #2A4A6C;
  margin: 0;
}

/* ── 分类分页 ─────────────────────────────────────────────── */
.cat-pagination {
  display: flex;
  justify-content: center;
  padding: 10px;
  border-top: 1px solid #1A3A5C;
  flex-shrink: 0;
}

/* ── 字段样式 ─────────────────────────────────────────────── */
.code-text {
  font-family: monospace;
  font-size: 12px;
  color: #00D4FF;
  letter-spacing: 0.03em;
}

.muted-text {
  color: #2A4A6C;
}

.lock-icon {
  font-size: 13px;
  opacity: 0.6;
}

/* ── 按钮 ─────────────────────────────────────────────────── */
.btn-cyan-sm {
  background: rgba(0, 212, 255, 0.1);
  border: 1px solid rgba(0, 212, 255, 0.3);
  color: #00D4FF;
  font-size: 12px;
  height: 28px;
  padding: 0 12px;
  border-radius: 3px;
  cursor: pointer;
  transition: all 0.15s;
  display: flex;
  align-items: center;
  gap: 4px;
}

.btn-cyan-sm:hover:not(:disabled) {
  background: rgba(0, 212, 255, 0.18);
  border-color: #00D4FF;
}

.btn-cyan-sm:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.btn-primary-cyan {
  background: linear-gradient(135deg, #00D4FF 0%, #0088CC 100%);
  border: none;
  color: #060E1C;
  font-weight: 600;
  font-size: 13px;
  height: 34px;
  padding: 0 16px;
  border-radius: 3px;
  cursor: pointer;
  transition: all 0.15s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-primary-cyan:hover {
  opacity: 0.9;
  box-shadow: 0 0 12px rgba(0, 212, 255, 0.4);
}

.btn-ghost {
  background: transparent;
  border: 1px solid #1A3A5C;
  color: #7A9BBE;
  font-size: 13px;
  height: 34px;
  padding: 0 16px;
  border-radius: 3px;
  cursor: pointer;
  transition: all 0.15s;
}

.btn-ghost:hover {
  border-color: #2A5A8C;
  color: #C8D8E8;
}

/* 操作按钮 */
.op-btn {
  border-radius: 3px;
  font-size: 12px;
  padding: 3px 8px;
  height: 24px;
  border: 1px solid;
  cursor: pointer;
  transition: all 0.15s;
}

.op-btn-edit {
  background: rgba(0, 212, 255, 0.08);
  border-color: rgba(0, 212, 255, 0.3);
  color: #00D4FF;
}

.op-btn-edit:hover {
  background: rgba(0, 212, 255, 0.15);
  border-color: #00D4FF;
}

.op-btn-disable {
  background: rgba(255, 59, 92, 0.08);
  border-color: rgba(255, 59, 92, 0.3);
  color: #FF3B5C;
}

.op-btn-disable:hover {
  background: rgba(255, 59, 92, 0.15);
  border-color: #FF3B5C;
}

.op-btn-enable {
  background: rgba(22, 201, 116, 0.08);
  border-color: rgba(22, 201, 116, 0.3);
  color: #16C974;
}

.op-btn-enable:hover {
  background: rgba(22, 201, 116, 0.15);
  border-color: #16C974;
}

/* ── 对话框 ───────────────────────────────────────────────── */
:deep(.aqc-dialog .el-dialog) {
  background: #0B1929;
  border: 1px solid #1A3A5C;
  border-radius: 6px;
}

:deep(.aqc-dialog .el-dialog__header) {
  background: #0F2137;
  padding: 14px 20px;
  border-bottom: 1px solid #1A3A5C;
  margin: 0;
}

:deep(.aqc-dialog .el-dialog__title) {
  color: #E8F4FF;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.05em;
}

:deep(.aqc-dialog .el-dialog__headerbtn .el-icon) {
  color: #4A6A8A;
}

:deep(.aqc-dialog .el-dialog__headerbtn:hover .el-icon) {
  color: #00D4FF;
}

:deep(.aqc-dialog .el-dialog__body) {
  padding: 24px 20px;
}

:deep(.aqc-dialog .el-dialog__footer) {
  padding: 12px 20px;
  border-top: 1px solid #1A3A5C;
}

/* ── 表单 ─────────────────────────────────────────────────── */
:deep(.aqc-form .el-form-item__label) {
  color: #7A9BBE;
  font-size: 13px;
}

:deep(.aqc-input .el-input__wrapper) {
  background: #060E1C;
  border: 1px solid #1A3A5C;
  box-shadow: none;
  border-radius: 3px;
}

:deep(.aqc-input .el-input__wrapper:hover),
:deep(.aqc-input .el-input__wrapper.is-focus) {
  border-color: #00D4FF;
  box-shadow: 0 0 0 1px rgba(0, 212, 255, 0.2);
}

:deep(.aqc-input .el-input__inner) {
  color: #C8D8E8;
  font-size: 13px;
}

:deep(.aqc-input .el-input__inner::placeholder) {
  color: #2A4A6C;
}

:deep(.aqc-select .el-select__wrapper) {
  background: #060E1C;
  border: 1px solid #1A3A5C;
  box-shadow: none;
  border-radius: 3px;
  color: #C8D8E8;
}

:deep(.aqc-select .el-select__wrapper:hover),
:deep(.aqc-select .el-select__wrapper.is-focused) {
  border-color: #00D4FF;
}

:deep(.aqc-select .el-select__placeholder) {
  color: #2A4A6C;
}

/* ── 表格覆盖 ─────────────────────────────────────────────── */
:deep(.aqc-table) {
  background: transparent;
}

:deep(.aqc-table .el-table__inner-wrapper) {
  background: transparent;
}

:deep(.aqc-table tr) {
  background: transparent;
}

:deep(.aqc-table .el-table__row:hover > td) {
  background: rgba(0, 212, 255, 0.04) !important;
}

:deep(.aqc-table .el-table__row.current-row > td) {
  background: rgba(0, 212, 255, 0.08) !important;
}

:deep(.aqc-table .el-table__empty-block) {
  background: transparent;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
