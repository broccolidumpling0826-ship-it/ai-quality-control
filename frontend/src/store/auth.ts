import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as apiLogin, logout as apiLogout, getUserInfo } from '@/api/auth'
import { useMenuStore } from '@/store/menu'
import type { UserInfo, LoginForm } from '@/types'

const TOKEN_KEY = 'qc_token'
const USER_INFO_KEY = 'qc_user_info'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>('')
  const userInfo = ref<UserInfo | null>(null)

  function init() {
    const savedToken = localStorage.getItem(TOKEN_KEY)
    const savedUser = localStorage.getItem(USER_INFO_KEY)
    if (savedToken) {
      token.value = savedToken
    }
    if (savedUser) {
      try {
        userInfo.value = JSON.parse(savedUser) as UserInfo
      } catch {
        userInfo.value = null
      }
    }
  }

  async function login(form: LoginForm): Promise<void> {
    const data = await apiLogin({ userNo: form.username, password: form.password }) as UserInfo
    token.value = data.token
    userInfo.value = data
    localStorage.setItem(TOKEN_KEY, data.token)
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(data))
  }

  /** 登录后或刷新时：拉取用户信息 + 注册动态路由 */
  async function loadSession(): Promise<void> {
    if (!token.value) return
    const info = await getUserInfo()
    userInfo.value = info as UserInfo
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(info))
    const menuStore = useMenuStore()
    await menuStore.fetchAndRegisterRoutes()
  }

  async function logout(): Promise<void> {
    try {
      await apiLogout()
    } catch {
      // ignore
    } finally {
      clearSession()
    }
  }

  function clearSession() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_INFO_KEY)
    useMenuStore().reset()
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(info))
  }

  return {
    token,
    userInfo,
    init,
    login,
    loadSession,
    logout,
    clearSession,
    setUserInfo
  }
})
