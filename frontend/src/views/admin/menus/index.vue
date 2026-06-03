<template>
  <div class="admin-menus-page">
    <div class="page-header">
      <div class="page-title-wrap">
        <span class="page-title-icon">☰</span>
        <div>
          <div class="page-title">菜单管理</div>
          <div class="page-subtitle">MENU MANAGEMENT</div>
        </div>
      </div>
      <el-button class="btn-cyan-sm" @click="openDialog(null)">＋ 新增菜单</el-button>
    </div>

    <el-table
      :data="flatMenus"
      v-loading="loading"
      row-key="id"
      class="aqc-table"
      default-expand-all
      :tree-props="{ children: 'children' }"
    >
      <el-table-column prop="menuName" label="菜单名称" min-width="160" />
      <el-table-column prop="menuType" label="类型" width="90" />
      <el-table-column prop="path" label="路径" min-width="140" />
      <el-table-column prop="component" label="组件" min-width="160" />
      <el-table-column prop="permCode" label="权限码" min-width="140" />
      <el-table-column prop="sortOrder" label="排序" width="70" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑菜单' : '新增菜单'" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="菜单名称"><el-input v-model="form.menuName" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.menuType" style="width:100%">
            <el-option v-for="t in menuTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="父菜单ID"><el-input v-model="form.parentId" placeholder="0=根" /></el-form-item>
        <el-form-item label="路径"><el-input v-model="form.path" /></el-form-item>
        <el-form-item label="组件">
          <el-select v-model="form.component" filterable clearable style="width:100%">
            <el-option v-for="c in componentOptions" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="路由名"><el-input v-model="form.routeName" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="form.icon" /></el-form-item>
        <el-form-item label="权限码"><el-input v-model="form.permCode" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="可见">
          <el-switch v-model="form.visible" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAdminMenuTree, createMenu, updateMenu, deleteMenu, type MenuSavePayload
} from '@/api/menu'
import { getViewModulePaths } from '@/router/dynamic'
import type { MenuTreeNode } from '@/types'

const loading = ref(false)
const menuTree = ref<MenuTreeNode[]>([])
const flatMenus = ref<MenuTreeNode[]>([])
const dialogVisible = ref(false)
const editingId = ref<string | null>(null)
const componentOptions = getViewModulePaths()
const menuTypes = ['DIR', 'MENU', 'HIDDEN', 'LINK', 'IFRAME']

const defaultForm = (): MenuSavePayload => ({
  parentId: '0',
  menuType: 'MENU',
  menuName: '',
  path: '',
  component: '',
  routeName: '',
  icon: '',
  permCode: '',
  visible: 1,
  sortOrder: 0,
  status: 1
})

const form = ref<MenuSavePayload>(defaultForm())

async function loadData() {
  loading.value = true
  try {
    menuTree.value = await getAdminMenuTree()
    flatMenus.value = menuTree.value
  } finally {
    loading.value = false
  }
}

function openDialog(row: MenuTreeNode | null) {
  if (row) {
    editingId.value = row.id
    form.value = {
      parentId: row.parentId || '0',
      menuType: row.menuType,
      menuName: row.menuName,
      path: row.path,
      component: row.component,
      routeName: row.routeName,
      icon: row.icon,
      permCode: row.permCode,
      visible: row.visible ?? 1,
      sortOrder: row.sortOrder ?? 0,
      status: 1,
      metaJson: row.metaJson
    }
  } else {
    editingId.value = null
    form.value = defaultForm()
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (editingId.value) {
    await updateMenu(editingId.value, form.value)
    ElMessage.success('更新成功')
  } else {
    await createMenu(form.value)
    ElMessage.success('创建成功')
  }
  dialogVisible.value = false
  await loadData()
}

async function handleDelete(row: MenuTreeNode) {
  await ElMessageBox.confirm(`确定删除菜单「${row.menuName}」？`, '确认')
  await deleteMenu(row.id)
  ElMessage.success('删除成功')
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.admin-menus-page { padding: 0 4px; }
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page-title-wrap { display: flex; align-items: center; gap: 12px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--text-primary, #e6edf3); }
.page-subtitle { font-size: 11px; color: var(--text-muted, #8b949e); letter-spacing: 1px; }
.page-title-icon { font-size: 22px; color: var(--cyan, #00D4FF); }
</style>
