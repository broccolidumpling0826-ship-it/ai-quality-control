import { get, post } from '@/utils/request'
import type { LoginCmd, LoginForm, UserInfo } from '@/types'

/**
 * 用户登录
 * POST /auth/login
 */
/** 登录请求体字段为 userNo（非 username） */
export const login = (data: LoginCmd) => post<UserInfo>('/auth/login', data)

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
