<template>
  <div class="admin-roles-page">
    <div class="page-header">
      <div class="page-title-wrap">
        <span class="page-title-icon">⊕</span>
        <div>
          <div class="page-title">角色管理</div>
          <div class="page-subtitle">ROLE MANAGEMENT</div>
        </div>
      </div>
      <el-button class="btn-cyan-sm" @click="openRoleDialog(null)">＋ 新增角色</el-button>
    </div>

    <el-table :data="roles" v-loading="loading" class="aqc-table">
      <el-table-column prop="roleCode" label="角色编码" min-width="140" />
      <el-table-column prop="roleName" label="角色名称" min-width="120" />
      <el-table-column prop="description" label="描述" min-width="180" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openAssignDrawer(row)">分配权限</el-button>
          <el-button link type="primary" @click="openRoleDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="roleDialogVisible" :title="editingRoleId ? '编辑角色' : '新增角色'" width="480px">
      <el-form :model="roleForm" label-width="90px">
        <el-form-item label="角色编码"><el-input v-model="roleForm.roleCode" :disabled="!!editingRoleId" /></el-form-item>
        <el-form-item label="角色名称"><el-input v-model="roleForm.roleName" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="roleForm.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveRole">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="drawerVisible" title="分配菜单与权限" size="480px">
      <div class="drawer-section">
        <div class="drawer-label">菜单</div>
        <el-tree
          ref="menuTreeRef"
          :data="menuTree"
          show-checkbox
          node-key="id"
          :props="{ label: 'menuName', children: 'children' }"
          default-expand-all
        />
      </div>
      <div class="drawer-section">
        <div class="drawer-label">权限点</div>
        <el-checkbox-group v-model="selectedPerms">
          <el-checkbox v-for="p in permissions" :key="p.permCode" :label="p.permCode">
            {{ p.permName }} ({{ p.permCode }})
          </el-checkbox>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button type="primary" @click="handleAssign">保存分配</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { ElTree } from 'element-plus'
import {
  listRoles, createRole, updateRole, deleteRole,
  getRoleMenus, assignRoleMenus, getRolePermissions, assignRolePermissions, listPermissions
} from '@/api/role'
import { getAdminMenuTree } from '@/api/menu'
import type { MenuTreeNode, PermissionInfo, RoleInfo } from '@/types'

const loading = ref(false)
const roles = ref<RoleInfo[]>([])
const menuTree = ref<MenuTreeNode[]>([])
const permissions = ref<PermissionInfo[]>([])
const roleDialogVisible = ref(false)
const drawerVisible = ref(false)
const editingRoleId = ref<string | null>(null)
const currentRoleId = ref('')
const selectedPerms = ref<string[]>([])
const menuTreeRef = ref<InstanceType<typeof ElTree>>()

const roleForm = ref({ roleCode: '', roleName: '', description: '' })

async function loadData() {
  loading.value = true
  try {
    roles.value = await listRoles()
    menuTree.value = await getAdminMenuTree()
    permissions.value = await listPermissions()
  } finally {
    loading.value = false
  }
}

function openRoleDialog(row: RoleInfo | null) {
  if (row) {
    editingRoleId.value = row.id
    roleForm.value = { roleCode: row.roleCode, roleName: row.roleName, description: row.description || '' }
  } else {
    editingRoleId.value = null
    roleForm.value = { roleCode: '', roleName: '', description: '' }
  }
  roleDialogVisible.value = true
}

async function handleSaveRole() {
  if (editingRoleId.value) {
    await updateRole(editingRoleId.value, roleForm.value)
  } else {
    await createRole(roleForm.value)
  }
  ElMessage.success('保存成功')
  roleDialogVisible.value = false
  await loadData()
}

async function openAssignDrawer(row: RoleInfo) {
  currentRoleId.value = row.id
  selectedPerms.value = await getRolePermissions(row.id)
  drawerVisible.value = true
  setTimeout(async () => {
    const menuIds = await getRoleMenus(row.id)
    menuTreeRef.value?.setCheckedKeys(menuIds, false)
  }, 100)
}

async function handleAssign() {
  const menuIds = menuTreeRef.value?.getCheckedKeys(false) as string[] || []
  const halfIds = menuTreeRef.value?.getHalfCheckedKeys() as string[] || []
  await assignRoleMenus(currentRoleId.value, [...menuIds, ...halfIds])
  await assignRolePermissions(currentRoleId.value, selectedPerms.value)
  ElMessage.success('分配成功')
  drawerVisible.value = false
}

async function handleDelete(row: RoleInfo) {
  await ElMessageBox.confirm(`确定删除角色「${row.roleName}」？`, '确认')
  await deleteRole(row.id)
  ElMessage.success('删除成功')
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.admin-roles-page { padding: 0 4px; }
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
.drawer-section { margin-bottom: 24px; }
.drawer-label { font-weight: 600; margin-bottom: 8px; color: var(--text-primary, #e6edf3); }
.el-checkbox { display: block; margin-bottom: 6px; }
</style>
