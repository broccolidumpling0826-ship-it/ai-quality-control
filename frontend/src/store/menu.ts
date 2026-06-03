import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import { getUserMenuTree } from '@/api/menu'
import type { MenuTreeNode } from '@/types'
import { buildRoutesFromMenus, getSidebarMenus } from '@/router/dynamic'
import { getRouter } from '@/router/instance'

const addedRouteNames: string[] = []
const INDEX_REDIRECT_NAME = 'MainIndexRedirect'

export const useMenuStore = defineStore('menu', () => {
  const menuTree = ref<MenuTreeNode[]>([])
  const sidebarMenus = ref<MenuTreeNode[]>([])
  const routesLoaded = ref(false)

  function removeDynamicRoutes() {
    const router = getRouter()
    if (router.hasRoute(INDEX_REDIRECT_NAME)) {
      router.removeRoute(INDEX_REDIRECT_NAME)
    }
    for (const name of addedRouteNames) {
      if (router.hasRoute(name)) {
        router.removeRoute(name)
      }
    }
    addedRouteNames.length = 0
  }

  function registerIndexRedirect(routes: RouteRecordRaw[]) {
    const router = getRouter()
    const dashboard = routes.find((r) => r.name === 'dashboard' || r.path === 'dashboard')
    if (dashboard?.name) {
      router.addRoute('Main', {
        path: '',
        name: INDEX_REDIRECT_NAME,
        redirect: { name: dashboard.name as string }
      })
    }
  }

  async function fetchAndRegisterRoutes(): Promise<void> {
    const router = getRouter()
    removeDynamicRoutes()

    const tree = await getUserMenuTree()
    menuTree.value = tree
    sidebarMenus.value = getSidebarMenus(tree)

    const dynamicRoutes = buildRoutesFromMenus(tree)
    dynamicRoutes.forEach((route) => {
      router.addRoute('Main', route as RouteRecordRaw)
      if (route.name) {
        addedRouteNames.push(route.name as string)
      }
    })

    registerIndexRedirect(dynamicRoutes)
    routesLoaded.value = true
  }

  function reset() {
    removeDynamicRoutes()
    menuTree.value = []
    sidebarMenus.value = []
    routesLoaded.value = false
  }

  return {
    menuTree,
    sidebarMenus,
    routesLoaded,
    fetchAndRegisterRoutes,
    reset
  }
})
