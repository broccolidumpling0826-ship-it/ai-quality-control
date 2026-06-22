import type { RouteRecordRaw } from 'vue-router'
import type { MenuTreeNode } from '@/types'

// 使用相对路径 glob，避免 Windows 下 @ 别名解析异常导致动态路由为空
const viewModules = import.meta.glob('../views/**/*.vue')

function resolveComponent(component?: string): RouteRecordRaw['component'] | undefined {
  if (!component) return undefined
  const normalized = component.replace(/\\/g, '/').replace(/^\//, '')
  const suffix = `/views/${normalized}.vue`
  const entry = Object.entries(viewModules).find(([key]) => {
    const path = key.replace(/\\/g, '/')
    return path.endsWith(suffix)
  })
  if (!entry) {
    console.warn(`[DynamicRoute] 组件未找到: ${component}`)
    return undefined
  }
  return entry[1]
}

function flattenMenus(nodes: MenuTreeNode[]): MenuTreeNode[] {
  const result: MenuTreeNode[] = []
  for (const node of nodes) {
    if (node.menuType === 'DIR') {
      if (node.children?.length) {
        result.push(...flattenMenus(node.children))
      }
    } else if (node.menuType === 'MENU' || node.menuType === 'HIDDEN' || node.menuType === 'IFRAME') {
      result.push(node)
    }
  }
  return result
}

export function buildRoutesFromMenus(menuTree: MenuTreeNode[]): RouteRecordRaw[] {
  const flat = flattenMenus(menuTree)
  const routes: RouteRecordRaw[] = []
  flat
    .filter((m) => m.path && m.component)
    .forEach((m) => {
      const component = resolveComponent(m.component)
      if (!component) return
      routes.push({
        path: m.path!.startsWith('/') ? m.path!.slice(1) : m.path!,
        name: m.routeName || m.id,
        component,
        meta: {
          title: m.menuName,
          icon: m.icon,
          permCode: m.permCode,
          requiresAuth: true
        }
      })
    })
  return routes
}

export function getSidebarMenus(menuTree: MenuTreeNode[]): MenuTreeNode[] {
  return menuTree
    .map(filterSidebarNode)
    .filter((n): n is MenuTreeNode => n !== null)
}

function filterSidebarNode(node: MenuTreeNode): MenuTreeNode | null {
  if (node.menuType === 'HIDDEN') {
    return null
  }
  if (node.menuType === 'DIR') {
    const children = (node.children || [])
      .map(filterSidebarNode)
      .filter((c): c is MenuTreeNode => c !== null)
    if (children.length === 0) {
      return null
    }
    return { ...node, children }
  }
  if (node.visible === 0) {
    return null
  }
  return node
}

export function getViewModulePaths(): string[] {
  return Object.keys(viewModules)
    .map((k) => k.replace(/\\/g, '/').replace(/^.*\/views\//, '').replace('.vue', ''))
    .sort()
}
