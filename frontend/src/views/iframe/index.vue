<template>
  <div class="iframe-page">
    <iframe v-if="iframeUrl" :src="iframeUrl" class="iframe-content" frameborder="0" />
    <el-empty v-else description="未配置内嵌页面地址" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useMenuStore } from '@/store/menu'

const route = useRoute()
const menuStore = useMenuStore()

const iframeUrl = computed(() => {
  const id = route.params.id as string
  const findMenu = (nodes: typeof menuStore.menuTree): string => {
    for (const n of nodes) {
      if (n.id === id && n.metaJson) {
        try {
          const meta = JSON.parse(n.metaJson)
          return meta.iframeUrl || ''
        } catch {
          return ''
        }
      }
      if (n.children?.length) {
        const found = findMenu(n.children)
        if (found) return found
      }
    }
    return ''
  }
  return findMenu(menuStore.menuTree)
})
</script>

<style scoped>
.iframe-page {
  height: calc(100vh - 120px);
  background: var(--bg-panel, #0d1117);
}
.iframe-content {
  width: 100%;
  height: 100%;
  border: none;
}
</style>
