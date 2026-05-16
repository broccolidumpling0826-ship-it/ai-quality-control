import { post, put } from '@/utils/request'

export interface UserPageQuery {
  userNo?: string
  username?: string
  role?: string
  status?: number | null
  pageNum: number
  pageSize: number
}

export interface UserCreateCmd {
  userNo: string
  username: string
  role: string
  department?: string
}

export interface UserVO {
  id: string
  userNo: string
  username: string
  role: string
  department: string
  status: number
  lastLoginTime: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

export const userManageApi = {
  page: (params: UserPageQuery) => post<PageResult<UserVO>>('/admin/users/page', params),
  create: (data: UserCreateCmd) => post<string>('/admin/users', data),
  update: (id: string, data: Partial<UserCreateCmd>) => put<void>(`/admin/users/${id}`, data),
  toggleStatus: (id: string, status: number) => put<void>(`/admin/users/${id}/status`, { status }),
}
