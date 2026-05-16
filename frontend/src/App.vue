<template>
  <router-view />
</template>

<script setup lang="ts">
// 根组件：仅渲染路由视图，布局由各页面自行控制
// 全局深色主题已通过 src/styles/global.css 注入
</script>

<style>
/* ── App 级全局样式（补充 global.css 中不宜用 :root 覆盖的部分） ── */

/* 确保整个 #app 使用深色基底 */
#app {
  height: 100%;
  background-color: var(--bg-base);
  color: var(--text-primary);
  font-family: var(--font-ui);
}

/* ── 路由过渡动画 ─────────────────────────────────────── */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(8px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-8px);
}
</style>
