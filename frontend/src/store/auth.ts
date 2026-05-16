import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as apiLogin, logout as apiLogout } from '@/api/auth'
import type { UserInfo, LoginForm } from '@/types'

const TOKEN_KEY = 'qc_token'
const USER_INFO_KEY = 'qc_user_info'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>('')
  const userInfo = ref<UserInfo | null>(null)

  /** 从 localStorage 恢复会话状态 */
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

  /** 登录：调用接口，持久化 token 和用户信息 */
  async function login(form: LoginForm): Promise<void> {
    // 后端字段名为 userNo，前端表单用 username，此处做映射
    const data = await apiLogin({ userNo: form.username, password: form.password }) as UserInfo
    token.value = data.token
    userInfo.value = data
    localStorage.setItem(TOKEN_KEY, data.token)
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(data))
  }

  /** 登出：调用接口，清理本地状态 */
  async function logout(): Promise<void> {
    try {
      await apiLogout()
    } catch {
      // 即使接口失败也要清理本地状态
    } finally {
      clearSession()
    }
  }

  /** 清除本地会话 */
  function clearSession() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_INFO_KEY)
  }

  /** 更新用户信息 */
  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(info))
  }

  return {
    token,
    userInfo,
    init,
    login,
    logout,
    clearSession,
    setUserInfo
  }
})
