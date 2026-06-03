import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

import './styles/global.css'

import App from './App.vue'
import router from './router'
import { useDictStore } from './store/dict'
import { useAuthStore } from './store/auth'
import { permissionDirective } from './directives/permission'

async function bootstrap() {
  const app = createApp(App)

  for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
  }

  const pinia = createPinia()
  app.use(pinia)

  const authStore = useAuthStore()
  authStore.init()

  // 必须在 app.use(router) 和 mount 之前注册动态路由，否则刷新会命中 404 通配路由
  if (authStore.token) {
    try {
      await authStore.loadSession()
    } catch (err) {
      console.warn('[Auth] 会话恢复失败:', err)
      authStore.clearSession()
    }
  }

  app.use(router)
  app.use(ElementPlus, { locale: zhCn, size: 'default' })
  app.directive('permission', permissionDirective)

  await router.isReady()

  const dictStore = useDictStore()
  dictStore.loadAll().catch((err) => {
    console.warn('[Dict] 字典加载失败:', err)
  })

  app.mount('#app')
}

bootstrap()
