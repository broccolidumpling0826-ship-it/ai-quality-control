import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import type { ApiResult } from '@/types'

const TOKEN_KEY = 'qc_token'

/** 默认 API 超时（毫秒） */
export const DEFAULT_REQUEST_TIMEOUT = 15000

/** AI 相关接口超时：需覆盖后端模型调用（默认 60s） */
export const AI_REQUEST_TIMEOUT = 90000

const service: AxiosInstance = axios.create({
  baseURL: '/api/v1',
  timeout: DEFAULT_REQUEST_TIMEOUT,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器：注入 Authorization Token
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(TOKEN_KEY)
    if (token && config.headers) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    // FormData 须由浏览器自动设置 multipart boundary，不能沿用默认 application/json
    if (config.data instanceof FormData && config.headers) {
      delete config.headers['Content-Type']
    }
    return config
  },
  (error) => {
    console.error('[Request Error]', error)
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理业务码和错误
service.interceptors.response.use(
  (response: AxiosResponse<ApiResult<unknown>>) => {
    const res = response.data

    // 业务成功码
    if (res.code === 2000) {
      return res.data as any
    }

    // 401 未授权 -> 跳转登录
    if (res.code === 4010 || response.status === 401) {
      localStorage.removeItem(TOKEN_KEY)
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
      return Promise.reject(new Error('Unauthorized'))
    }

    // 其他业务错误
    const msg = res.message || '请求失败'
    ElMessage.error(msg)
    return Promise.reject(new Error(msg))
  },
  (error) => {
    if ((error.config as { skipGlobalErrorHandler?: boolean } | undefined)?.skipGlobalErrorHandler) {
      return Promise.reject(error)
    }
    const status = error.response?.status
    let msg = '网络请求失败，请稍后重试'

    if (status === 401) {
      localStorage.removeItem(TOKEN_KEY)
      router.push('/login')
      msg = '登录已过期，请重新登录'
    } else if (status === 403) {
      msg = '权限不足，拒绝访问'
    } else if (status === 404) {
      msg = '请求的资源不存在'
    } else if (status === 500) {
      msg = '服务器内部错误'
    } else if (status === 503) {
      msg = '服务暂不可用'
    } else if (error.code === 'ECONNABORTED') {
      msg = '请求超时，请检查网络'
    }

    ElMessage.error(msg)
    console.error('[Response Error]', error)
    return Promise.reject(error)
  }
)

// ─── 便捷方法 ───────────────────────────────────────────

export function get<T = unknown>(url: string, params?: Record<string, unknown>, config?: AxiosRequestConfig): Promise<T> {
  return service.get(url, { params, ...config })
}

export function post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
  return service.post(url, data, config)
}

export function put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
  return service.put(url, data, config)
}

export function del<T = unknown>(url: string, params?: Record<string, unknown>, config?: AxiosRequestConfig): Promise<T> {
  return service.delete(url, { params, ...config })
}

export default service
