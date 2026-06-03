<template>
  <div class="admin-users-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-title-wrap">
        <span class="page-title-icon">⊙</span>
        <div>
          <div class="page-title">账号管理</div>
          <div class="page-subtitle">ACCOUNT MANAGEMENT</div>
        </div>
      </div>
      <el-button type="primary" @click="openCreateDialog">
        <span class="btn-icon">＋</span> 新增账号
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-panel">
      <div class="search-fields">
        <div class="search-field">
          <label class="search-label">工号</label>
          <el-input
            v-model="query.userNo"
            placeholder="请输入工号"
            clearable
            class="aqc-input"
            @keyup.enter="handleSearch"
          />
        </div>
        <div class="search-field">
          <label class="search-label">姓名</label>
          <el-input
            v-model="query.username"
            placeholder="请输入姓名"
            clearable
            class="aqc-input"
            @keyup.enter="handleSearch"
          />
        </div>
        <div class="search-field">
          <label class="search-label">角色</label>
          <el-select
            v-model="query.role"
            placeholder="全部角色"
            clearable
            class="aqc-select"
          >
            <el-option
              v-for="r in roleOptions"
              :key="r.value"
              :label="r.label"
              :value="r.value"
            />
          </el-select>
        </div>
        <div class="search-field">
          <label class="search-label">状态</label>
          <el-select
            v-model="query.status"
            placeholder="全部状态"
            clearable
            class="aqc-select"
          >
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </div>
      </div>
      <div class="search-actions">
        <el-button type="primary" @click="openCreateDialog">
          <span class="btn-icon">＋</span> 新增账号
        </el-button>
        <el-button class="btn-cyan-outline" @click="handleSearch">
          <span class="btn-icon">⌕</span> 查询
        </el-button>
        <el-button class="btn-ghost" @click="handleReset">
          重置
        </el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-panel">
      <div class="table-header-bar">
        <span class="table-count">共 <em>{{ total }}</em> 条记录</span>
      </div>

      <el-table
        :data="tableData"
        v-loading="loading"
        class="aqc-table"
        row-key="id"
        :header-cell-style="headerCellStyle"
        :cell-style="cellStyle"
      >
        <el-table-column
          prop="userNo"
          label="工号"
          width="120"
          :filters="getFilters('userNo')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="username"
          label="姓名"
          width="120"
          :filters="getFilters('username')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="role"
          label="角色"
          width="160"
          :filters="getFilters('role')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        >
          <template #default="{ row }">
            <span class="role-tag">{{ getRoleLabel(row.role) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="department"
          label="部门"
          min-width="140"
          :filters="getFilters('department')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="status"
          label="状态"
          width="100"
          align="center"
          :filters="getFilters('status')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        >
          <template #default="{ row }">
            <el-tag
              :type="row.status === 1 ? 'success' : 'danger'"
              size="small"
              effect="dark"
            >
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              class="op-btn op-btn-edit"
              @click="openEditDialog(row)"
            >编辑</el-button>
            <el-button
              size="small"
              :class="row.status === 1 ? 'op-btn op-btn-disable' : 'op-btn op-btn-enable'"
              @click="handleToggleStatus(row)"
            >{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑账号' : '新增账号'"
      width="480px"
      :close-on-click-modal="false"
      class="aqc-dialog"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
        class="aqc-form"
      >
        <el-form-item label="工号" prop="userNo">
          <el-input
            v-model="formData.userNo"
            placeholder="请输入工号"
            :disabled="isEdit"
            class="aqc-input"
          />
        </el-form-item>
        <el-form-item label="姓名" prop="username">
          <el-input
            v-model="formData.username"
            placeholder="请输入姓名"
            class="aqc-input"
          />
        </el-form-item>
        <el-form-item label="角色" prop="roles">
          <el-select
            v-model="formData.roles"
            multiple
            placeholder="请选择角色（可多选）"
            class="aqc-select"
            style="width: 100%"
          >
            <el-option
              v-for="r in roleOptions"
              :key="r.value"
              :label="r.label"
              :value="r.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="部门" prop="department">
          <el-input
            v-model="formData.department"
            placeholder="请输入部门（选填）"
            class="aqc-input"
          />
        </el-form-item>
        <p v-if="!isEdit" class="form-tip">
          创建后初始密码为 <strong>Abc@1234</strong>，请通知用户首次登录后修改。
        </p>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button class="btn-ghost" @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ isEdit ? '保存' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useTableFilter } from '@/composables/use-table-filter'
import { useDictStore } from '@/store/dict'
import { userManageApi, type UserPageQuery, type UserCreateCmd } from '@/api/user-manage'
import { assignUserRoles } from '@/api/role'

const dictStore = useDictStore()

const roleOptions = computed(() =>
  dictStore.getItems('USER_ROLE').map((item) => ({
    value: item.value,
    label: item.label,
  }))
)

function getRoleLabel(role: string): string {
  return dictStore.getLabel('USER_ROLE', role) || role
}

// ─── 查询参数 ───────────────────────────────────────────────
const query = reactive<UserPageQuery & { status: number | null }>({
  userNo: '',
  username: '',
  role: '',
  status: null,
  pageNum: 1,
  pageSize: 10,
})

const total = ref(0)
const loading = ref(false)
const tableData = ref<any[]>([])
const { getFilters, filterMethod } = useTableFilter(tableData)

// ─── 表格样式 ───────────────────────────────────────────────
const headerCellStyle = {
  background: '#0B1929',
  color: 'var(--text-muted)',
  borderBottom: '1px solid #1A3A5C',
  fontFamily: 'var(--font-data, monospace)',
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

// ─── 加载数据 ───────────────────────────────────────────────
async function loadData() {
  loading.value = true
  try {
    const params: UserPageQuery = {
      userNo: query.userNo || undefined,
      username: query.username || undefined,
      role: query.role || undefined,
      status: query.status,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    }
    const res = await userManageApi.page(params)
    tableData.value = res?.records ?? []
    total.value = res?.total ?? 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNum = 1
  loadData()
}

function handleReset() {
  query.userNo = ''
  query.username = ''
  query.role = ''
  query.status = null
  query.pageNum = 1
  loadData()
}

// ─── 对话框 ─────────────────────────────────────────────────
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const editId = ref<string>('')

const formData = reactive({
  userNo: '',
  username: '',
  roles: [] as string[],
  department: '',
})

const formRules: FormRules = {
  userNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  username: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  roles: [{ required: true, type: 'array', min: 1, message: '请选择至少一个角色', trigger: 'change' }],
}

function resetFormFields() {
  formData.userNo = ''
  formData.username = ''
  formData.roles = []
  formData.department = ''
}

async function openCreateDialog() {
  isEdit.value = false
  editId.value = ''
  resetFormFields()
  dialogVisible.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

async function openEditDialog(row: any) {
  isEdit.value = true
  editId.value = row.id
  formData.userNo = row.userNo
  formData.username = row.username
  formData.roles = row.role ? [row.role] : []
  formData.department = row.department ?? ''
  dialogVisible.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try {
    const primaryRole = formData.roles[0] || ''
    if (isEdit.value) {
      await userManageApi.update(editId.value, {
        username: formData.username,
        role: primaryRole,
        department: formData.department,
      })
      await assignUserRoles(editId.value, formData.roles)
      ElMessage.success('账号信息已更新')
    } else {
      const payload: UserCreateCmd = {
        userNo: formData.userNo,
        username: formData.username,
        role: primaryRole,
        department: formData.department,
      }
      const newId = await userManageApi.create(payload)
      if (newId && formData.roles.length) {
        await assignUserRoles(newId, formData.roles)
      }
      ElMessage.success(`账号创建成功，初始密码为：Abc@1234`)
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

// ─── 切换状态 ───────────────────────────────────────────────
async function handleToggleStatus(row: any) {
  const targetStatus = row.status === 1 ? 0 : 1
  const label = targetStatus === 1 ? '启用' : '禁用'
  await ElMessageBox.confirm(
    `确定要${label}账号「${row.username}」吗？`,
    `${label}确认`,
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
  try {
    await userManageApi.toggleStatus(row.id, targetStatus)
    ElMessage.success(`账号已${label}`)
    loadData()
  } catch (e) {
    console.error(e)
  }
}

// ─── 初始化 ─────────────────────────────────────────────────
onMounted(async () => {
  await dictStore.refreshItems('USER_ROLE').catch(() => {})
  loadData()
})
</script>

<style scoped>
/* ══════════════════════════════════════════════════════════════
   账号管理页 — 工业精密风格
══════════════════════════════════════════════════════════════ */

.admin-users-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 0;
}

/* ── 页面标题 ─────────────────────────────────────────────── */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 16px;
  border-bottom: 1px solid #1A3A5C;
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
  color: var(--text-faint);
  margin-top: 2px;
}

/* ── 搜索面板 ────────────────────────────────────────────── */
.search-panel {
  background: #0B1929;
  border: 1px solid #1A3A5C;
  border-radius: 4px;
  padding: 16px 20px;
  display: flex;
  align-items: flex-end;
  gap: 16px;
  flex-wrap: wrap;
}

.search-fields {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  flex: 1;
}

.search-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 160px;
}

.search-label {
  font-family: monospace;
  font-size: 10px;
  letter-spacing: 0.08em;
  color: var(--text-faint);
  text-transform: uppercase;
}

.search-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-shrink: 0;
}

/* ── 表格面板 ─────────────────────────────────────────────── */
.table-panel {
  background: #0B1929;
  border: 1px solid #1A3A5C;
  border-radius: 4px;
  overflow: hidden;
  flex: 1;
}

.table-header-bar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 10px 16px;
  border-bottom: 1px solid #1A3A5C;
}

.table-count {
  font-family: monospace;
  font-size: 11px;
  color: var(--text-faint);
}

.table-count em {
  font-style: normal;
  color: #00D4FF;
  font-weight: 600;
}

/* 角色标签 */
.role-tag {
  font-family: monospace;
  font-size: 11px;
  color: #00D4FF;
  background: rgba(0, 212, 255, 0.08);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 3px;
  padding: 2px 8px;
  letter-spacing: 0.02em;
}

/* 操作按钮 */
.op-btn {
  border-radius: 3px;
  font-size: 12px;
  padding: 4px 10px;
  height: 26px;
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

/* ── 分页 ─────────────────────────────────────────────────── */
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding: 12px 16px;
  border-top: 1px solid #1A3A5C;
}

/* ── 按钮 ─────────────────────────────────────────────────── */
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

.btn-cyan-outline {
  background: transparent;
  border: 1px solid rgba(0, 212, 255, 0.4);
  color: #00D4FF;
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

.btn-cyan-outline:hover {
  background: rgba(0, 212, 255, 0.08);
  border-color: #00D4FF;
}

.btn-ghost {
  background: transparent;
  border: 1px solid #1A3A5C;
  color: var(--text-muted);
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

.btn-icon {
  font-size: 14px;
  line-height: 1;
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
  color: var(--text-faint);
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
  color: var(--text-muted);
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
  color: var(--text-faint);
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
  color: var(--text-faint);
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

:deep(.aqc-table .el-table__empty-block) {
  background: transparent;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.form-tip {
  margin: 0 0 0 80px;
  font-size: 12px;
  color: var(--text-faint);
  line-height: 1.5;
}

.form-tip strong {
  color: #00D4FF;
  font-weight: 600;
}
</style>
