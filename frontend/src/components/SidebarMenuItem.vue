<template>
  <div class="sidebar-menu-root">
    <template v-for="item in menus" :key="item.id">
      <!-- 可折叠目录分组 -->
      <div v-if="item.menuType === 'DIR'" class="nav-group">
        <div
          v-if="!collapsed"
          class="nav-group-header"
          :class="{ expanded: isExpanded(item.id) }"
          @click="toggleExpand(item.id)"
        >
          <span class="nav-group-chevron">{{ isExpanded(item.id) ? '▾' : '▸' }}</span>
          <span class="nav-group-title">{{ item.menuName }}</span>
        </div>
        <div v-else class="nav-group-divider" />
        <div
          v-show="collapsed || isExpanded(item.id)"
          class="nav-group-body"
          :class="{ collapsed: collapsed }"
        >
          <SidebarMenuItem
            v-if="item.children?.length"
            :menus="item.children"
            :collapsed="collapsed"
            :depth="depth + 1"
          />
        </div>
      </div>

      <!-- 外链 -->
      <div
        v-else-if="item.menuType === 'LINK'"
        class="nav-item"
        :class="itemClass"
        :title="collapsed ? item.menuName : ''"
        @click="openExternal(item)"
      >
        <span class="nav-icon">{{ iconChar(item.icon) }}</span>
        <span v-if="!collapsed" class="nav-label">{{ item.menuName }}</span>
      </div>

      <!-- 内部路由 -->
      <router-link
        v-else-if="item.path"
        :to="resolvePath(item)"
        custom
        v-slot="{ isActive, navigate }"
      >
        <div
          class="nav-item"
          :class="[itemClass, { active: isActive || isPathActive(item.path!) }]"
          @click="navigate"
          :title="collapsed ? item.menuName : ''"
        >
          <span class="nav-icon">{{ iconChar(item.icon) }}</span>
          <span v-if="!collapsed" class="nav-label">{{ item.menuName }}</span>
        </div>
      </router-link>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import SidebarMenuItem from '@/components/SidebarMenuItem.vue'
import type { MenuTreeNode } from '@/types'

const props = defineProps<{
  menus: MenuTreeNode[]
  collapsed?: boolean
  depth?: number
}>()

const route = useRoute()
const depth = computed(() => props.depth ?? 0)
const expandedDirs = ref<Record<string, boolean>>({})

const itemClass = computed(() => ({
  'nav-item-root': depth.value === 0,
  'nav-item-child': depth.value > 0
}))

watch(
  () => props.menus,
  (list) => initExpanded(list),
  { immediate: true, deep: true }
)

function initExpanded(list: MenuTreeNode[]) {
  for (const item of list) {
    if (item.menuType === 'DIR') {
      if (expandedDirs.value[item.id] === undefined) {
        expandedDirs.value[item.id] = true
      }
      if (item.children?.length) {
        initExpanded(item.children)
      }
    }
  }
}

function isExpanded(id: string): boolean {
  return expandedDirs.value[id] !== false
}

function toggleExpand(id: string) {
  expandedDirs.value[id] = !isExpanded(id)
}

function isPathActive(path: string): boolean {
  const full = path.startsWith('/') ? path : `/${path}`
  return route.path === full || route.path.startsWith(full + '/')
}

function resolvePath(item: MenuTreeNode): string {
  if (!item.path) return '/'
  return item.path.startsWith('/') ? item.path : `/${item.path}`
}

function iconChar(icon?: string): string {
  const map: Record<string, string> = {
    DataBoard: '⬡', Document: '◉', TrendCharts: '◧', Warning: '⚠',
    EditPen: '✎', Stamp: '◈', RefreshRight: '⟳', Edit: '↻',
    Check: '◎', Tickets: '⊞', PieChart: '▦', Lock: '◆',
    User: '⊛', List: '≡', Menu: '☰', Avatar: '⊕', Link: '↗'
  }
  return icon ? (map[icon] || '•') : '•'
}

function openExternal(item: MenuTreeNode) {
  if (!item.metaJson) return
  try {
    const meta = JSON.parse(item.metaJson)
    if (meta.externalUrl) window.open(meta.externalUrl, '_blank')
  } catch {
    // ignore
  }
}
</script>

<style scoped>
.sidebar-menu-root {
  width: 100%;
}

/* ── 分组容器 ── */
.nav-group {
  margin: 6px 10px 10px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.04);
  overflow: hidden;
}

.nav-group-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  cursor: pointer;
  user-select: none;
  transition: background 0.15s ease;
}

.nav-group-header:hover {
  background: rgba(0, 212, 255, 0.06);
}

.nav-group-chevron {
  font-size: 10px;
  color: var(--cyan, #00d4ff);
  width: 12px;
  flex-shrink: 0;
}

.nav-group-title {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.08em;
  color: var(--text-secondary, #8b949e);
  text-transform: uppercase;
}

.nav-group-header.expanded .nav-group-title {
  color: var(--cyan, #00d4ff);
}

.nav-group-body {
  padding: 2px 0 6px;
}

.nav-group-body.collapsed {
  padding: 0;
}

.nav-group-divider {
  height: 1px;
  background: var(--border, rgba(255, 255, 255, 0.08));
  margin: 8px 12px;
}

/* ── 菜单项 ── */
.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 2px 8px;
  padding: 10px 12px;
  border-radius: 5px;
  cursor: pointer;
  transition: all 0.15s ease;
  color: var(--text-secondary, #adbac7);
  font-size: 13px;
  line-height: 1.4;
  user-select: none;
  white-space: nowrap;
  overflow: hidden;
  border-left: 2px solid transparent;
}

.nav-item-root {
  margin: 4px 10px;
  padding: 11px 14px;
}

.nav-item-child {
  margin: 1px 8px 1px 12px;
  padding: 9px 12px 9px 16px;
  font-size: 12.5px;
  color: var(--text-muted, #8b949e);
  position: relative;
}

.nav-item-child::before {
  content: '';
  position: absolute;
  left: 6px;
  top: 50%;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--border, rgba(255, 255, 255, 0.15));
  transform: translateY(-50%);
  transition: background 0.15s ease;
}

.nav-item:hover {
  background: var(--bg-hover, rgba(255, 255, 255, 0.05));
  color: var(--text-primary, #e6edf3);
}

.nav-item-child:hover::before {
  background: var(--cyan, #00d4ff);
}

.nav-item.active {
  background: linear-gradient(90deg, rgba(0, 212, 255, 0.14) 0%, rgba(0, 212, 255, 0.02) 100%);
  border-left-color: var(--cyan, #00d4ff);
  color: var(--text-primary, #e6edf3);
  font-weight: 500;
}

.nav-item-child.active::before {
  background: var(--cyan, #00d4ff);
  box-shadow: 0 0 6px rgba(0, 212, 255, 0.5);
}

.nav-icon {
  font-size: 14px;
  width: 20px;
  text-align: center;
  flex-shrink: 0;
  font-style: normal;
}

.nav-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
