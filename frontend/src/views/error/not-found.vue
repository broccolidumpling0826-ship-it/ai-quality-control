<template>
  <div class="not-found">
    <div class="code">404</div>
    <p>页面不存在或暂无访问权限</p>
    <el-button type="primary" @click="goHome">返回首页</el-button>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useMenuStore } from '@/store/menu'

const router = useRouter()
const menuStore = useMenuStore()

function goHome() {
  const path = findFirstMenuPath(menuStore.sidebarMenus) || '/login'
  router.push(path)
}

function findFirstMenuPath(menus: typeof menuStore.sidebarMenus): string | null {
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
</script>

<style scoped>
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 320px;
  color: var(--text-muted, #8b949e);
}
.code {
  font-size: 48px;
  font-weight: 700;
  color: var(--cyan, #00d4ff);
  margin-bottom: 8px;
}
</style>
