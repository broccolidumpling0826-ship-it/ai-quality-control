import { post, put, get, del } from '@/utils/request'

export interface DictCategoryVO {
  id: string
  dictCode: string
  dictName: string
  description: string
  isSystem: number
  sortNo: number
  status: number
}

export interface DictItemVO {
  id: string
  dictCode: string
  itemValue: string
  itemLabel: string
  colorTag: string
  sortNo: number
  status: number
  isSystem: number
  remark: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

export const dictManageApi = {
  createCategory: (data: any) => post<string>('/dict', data),
  updateCategory: (id: string, data: any) => put<void>(`/dict/${id}`, data),
  deleteCategory: (id: string) => del<void>(`/dict/${id}`),
  createItem: (data: any) => post<string>('/dict/items', data),
  updateItem: (id: string, data: any) => put<void>(`/dict/items/${id}`, data),
  pageCategories: (params: any) => post<PageResult<DictCategoryVO>>('/dict/page', params),
  /** 管理端：返回完整字典项（itemValue/itemLabel/status/id） */
  getManageItems: (dictCode: string) => get<DictItemVO[]>(`/dict/items/${dictCode}/manage`),
}
