import { get, post, put } from '@/utils/request'

export const addStandard = (data: any) => post('/standards', data)
export const updateStandard = (data: any) => put(`/standards/${data.id}`, data)
export const publishStandard = (id: string) => put(`/standards/${id}/publish`)
export const pageStandards = (data: any) => post('/standards/page', data)
export const getStandardById = (id: string) => get(`/standards/${id}`)
export const listIndicators = (params?: any) => get('/standards/indicators', params)

export interface SpecRangeOption {
  value: string
  label: string
  standardId: string
}

/** 查询有效规格范围下拉选项（D-016：product_spec 强制下拉，禁止自由文本） */
export const getSpecRanges = (params: {
  variety: string
  grade: string
  customerId?: string
}) => get<SpecRangeOption[]>('/standards/spec-ranges', params as Record<string, unknown>)
