import { get, post, put, del } from '@/utils/request'
import type { MenuTreeNode } from '@/types'

export const getUserMenuTree = () => get<MenuTreeNode[]>('/menus/user-tree')

export const getAdminMenuTree = () => get<MenuTreeNode[]>('/admin/menus/tree')

export interface MenuSavePayload {
  parentId?: string
  menuType: string
  menuName: string
  path?: string
  component?: string
  routeName?: string
  icon?: string
  permCode?: string
  visible?: number
  sortOrder?: number
  status?: number
  metaJson?: string
}

export const createMenu = (data: MenuSavePayload) => post<MenuTreeNode>('/admin/menus', data)

export const updateMenu = (id: string, data: MenuSavePayload) =>
  put<MenuTreeNode>(`/admin/menus/${id}`, data)

export const deleteMenu = (id: string) => del<void>(`/admin/menus/${id}`)

export const batchSortMenus = (cmds: { id: string; sortOrder: number }[]) =>
  put<void>('/admin/menus/sort', cmds)
