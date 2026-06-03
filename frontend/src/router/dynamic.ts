import type { RouteRecordRaw } from 'vue-router'
import type { MenuTreeNode } from '@/types'

const viewModules = import.meta.glob('@/views/**/*.vue')

function resolveComponent(component?: string) {
  if (!component) return undefined
  const key = `/src/views/${component}.vue`
  const loader = viewModules[key]
  if (!loader) {
    console.warn(`[DynamicRoute] 组件未找到: ${component}`)
    return undefined
  }
  return loader
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
  return flat
    .filter((m) => m.path && m.component)
    .map((m) => ({
      path: m.path!.startsWith('/') ? m.path!.slice(1) : m.path!,
      name: m.routeName || m.id,
      component: resolveComponent(m.component),
      meta: {
        title: m.menuName,
        icon: m.icon,
        permCode: m.permCode,
        requiresAuth: true
      }
    }))
    .filter((r) => r.component)
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
    .map((k) => k.replace('/src/views/', '').replace('.vue', ''))
    .sort()
}
