import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

// AQC 工业精密控制室深色主题（必须在 element-plus css 之后引入以确保覆盖）
import './styles/global.css'

import App from './App.vue'
import router from './router'
import { useDictStore } from './store/dict'
import { useAuthStore } from './store/auth'

const app = createApp(App)

// 注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: zhCn, size: 'default' })

// 初始化 auth 状态（从 localStorage 恢复 token）
const authStore = useAuthStore()
authStore.init()

// 应用启动时预加载所有字典数据
const dictStore = useDictStore()
dictStore.loadAll().catch((err) => {
  console.warn('[Dict] 字典加载失败:', err)
})

app.mount('#app')
