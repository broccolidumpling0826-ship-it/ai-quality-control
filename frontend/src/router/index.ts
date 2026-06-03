import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { useMenuStore } from '@/store/menu'
import { hasPermission as checkPerm } from '@/directives/permission'
import { ElMessage } from 'element-plus'
import type { MenuTreeNode } from '@/types'
import { setRouter } from '@/router/instance'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    name: 'Main',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: []
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/not-found.vue'),
    meta: { title: '页面不存在', requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ left: 0, top: 0 })
})

setRouter(router)

function findFirstMenuPath(menus: MenuTreeNode[]): string | null {
  for (const item of menus) {
    if (item.menuType === 'MENU' && item.path) {
      return item.path.startsWith('/') ? item.path : `/${item.path}`
    }
    if (item.children?.length) {
      const found = findFirstMenuPath(item.children)
      if (found) return found
    }
  }
  return null
}

/** 目标路径是否已有 Main 下的动态子路由 */
function hasMainChildRoute(path: string): boolean {
  const resolved = router.resolve(path)
  return resolved.matched.some((r) => r.name === 'Main') && resolved.name !== 'NotFound'
}

router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore()
  const menuStore = useMenuStore()
  const requiresAuth = to.meta.requiresAuth !== false

  if (requiresAuth && !authStore.token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  if (to.path === '/login' && authStore.token) {
    const home = findFirstMenuPath(menuStore.sidebarMenus)
    next(home ? { path: home, replace: true } : { name: 'NotFound', replace: true })
    return
  }

  if (requiresAuth && authStore.token && !menuStore.routesLoaded) {
    try {
      await authStore.loadSession()
      next({ ...to, replace: true })
    } catch {
      authStore.clearSession()
      next({ path: '/login', query: { redirect: to.fullPath } })
    }
    return
  }

  // 刷新页面：动态路由已注册但首次解析误命中 NotFound 通配路由
  if (
    requiresAuth &&
    authStore.token &&
    menuStore.routesLoaded &&
    to.name === 'NotFound' &&
    to.path !== '/login'
  ) {
    if (hasMainChildRoute(to.fullPath)) {
      next({ path: to.fullPath, replace: true })
      return
    }
  }

  if (to.path === '/' && menuStore.routesLoaded) {
    const home = findFirstMenuPath(menuStore.sidebarMenus)
    next(home ? { path: home, replace: true } : { name: 'NotFound', replace: true })
    return
  }

  const permCode = to.meta.permCode as string | undefined
  if (permCode && !checkPerm(permCode)) {
    ElMessage.warning('权限不足，无法访问该页面')
    const fallback = findFirstMenuPath(menuStore.sidebarMenus)
    if (fallback && to.path !== fallback) {
      next({ path: fallback, replace: true })
    } else {
      next({ name: 'NotFound', replace: true })
    }
    return
  }

  next()
})

router.afterEach((to) => {
  const title = to.meta.title as string | undefined
  document.title = title ? `${title} - 质量管理系统` : '质量管理系统'
})

export default router
