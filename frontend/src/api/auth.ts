import { get, post } from '@/utils/request'
import type { LoginForm, UserInfo } from '@/types'

/**
 * 用户登录
 * POST /auth/login
 */
export const login = (data: LoginForm) => post<UserInfo>('/auth/login', data)

/**
 * 用户登出
 * POST /auth/logout
 */
export const logout = () => post<void>('/auth/logout')

/**
 * 获取当前用户信息
 * GET /auth/user/info
 */
export const getUserInfo = () => get<UserInfo>('/auth/user/info')
