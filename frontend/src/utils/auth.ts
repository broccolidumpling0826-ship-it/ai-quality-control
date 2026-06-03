import { useAuthStore } from '@/store/auth'

/** 系统管理员拥有全部操作权限 */
export function isAdmin(): boolean {
  const authStore = useAuthStore()
  const roles = authStore.userInfo?.roles || []
  if (roles.includes('ADMIN')) return true
  return authStore.userInfo?.role === 'ADMIN'
}

/** 是否拥有指定角色；ADMIN 恒为 true */
export function hasRole(role: string): boolean {
  if (isAdmin()) return true
  const authStore = useAuthStore()
  const roles = authStore.userInfo?.roles || []
  if (roles.includes(role)) return true
  return authStore.userInfo?.role === role
}

/** 是否拥有指定权限；通配符 * 或 ADMIN 视为全部权限 */
export function hasPermission(code: string): boolean {
  if (isAdmin()) return true
  const authStore = useAuthStore()
  const permissions = authStore.userInfo?.permissions || []
  return permissions.includes('*') || permissions.includes(code)
}
