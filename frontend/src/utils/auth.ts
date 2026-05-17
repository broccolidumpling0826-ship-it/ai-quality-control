import { useAuthStore } from '@/store/auth'

/** 系统管理员拥有全部操作权限 */
export function isAdmin(): boolean {
  const authStore = useAuthStore()
  return authStore.userInfo?.role === 'ADMIN'
}

/** 是否拥有指定角色；ADMIN 恒为 true */
export function hasRole(role: string): boolean {
  if (isAdmin()) return true
  const authStore = useAuthStore()
  return authStore.userInfo?.role === role
}
