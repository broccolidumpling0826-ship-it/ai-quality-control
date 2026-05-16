import { get, post, put } from '@/utils/request'

export interface IndicatorForm {
  id?: string
  indicatorName: string
  indicatorCode: string
  category: string
  unit?: string
  testMethod?: string
  remark?: string
}

export interface IndicatorPageQuery {
  pageNum?: number
  pageSize?: number
  indicatorName?: string
  category?: string
}

export const pageIndicators = (data: IndicatorPageQuery) => post('/indicators/page', data)

export const addIndicator = (data: IndicatorForm) => post('/indicators', data)

export const updateIndicator = (id: string, data: IndicatorForm) => put(`/indicators/${id}`, data)

export const updateIndicatorStatus = (id: string, status: string) =>
  put(`/indicators/${id}/status`, { status })

/** 下拉选择用（仅启用状态） */
export const listActiveIndicators = (params?: { keyword?: string; category?: string }) =>
  get('/standards/indicators', params)
