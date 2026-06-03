import { get, post, put, del } from '@/utils/request'
import type { PermissionInfo, RoleInfo } from '@/types'

export const listRoles = () => get<RoleInfo[]>('/admin/roles')

export const createRole = (data: Partial<RoleInfo>) => post<RoleInfo>('/admin/roles', data)

export const updateRole = (id: string, data: Partial<RoleInfo>) =>
  put<RoleInfo>(`/admin/roles/${id}`, data)

export const deleteRole = (id: string) => del<void>(`/admin/roles/${id}`)

export const getRoleMenus = (roleId: string) => get<string[]>(`/admin/roles/${roleId}/menus`)

export const assignRoleMenus = (roleId: string, menuIds: string[]) =>
  put<void>(`/admin/roles/${roleId}/menus`, { menuIds })

export const getRolePermissions = (roleId: string) =>
  get<string[]>(`/admin/roles/${roleId}/permissions`)

export const assignRolePermissions = (roleId: string, permCodes: string[]) =>
  put<void>(`/admin/roles/${roleId}/permissions`, { permCodes })

export const listPermissions = () => get<PermissionInfo[]>('/admin/roles/permissions/all')

export const assignUserRoles = (userId: string, roleCodes: string[]) =>
  put<void>(`/admin/users/${userId}/roles`, { roleCodes })
