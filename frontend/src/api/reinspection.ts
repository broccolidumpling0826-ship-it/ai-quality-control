import { post, put } from '@/utils/request'
import type { PageResult } from '@/types'

export interface ReinspectionPageQuery {
  pageNum: number
  pageSize: number
  status?: string
  /** 责任人工号（后端字段 responsibleNo） */
  responsibleNo?: string
}

export const initiateReinspection = (data: {
  originalJudgmentId: string
  reinspectionReason: string
  responsibleNo?: string
}) => post('/reinspections', data)

export const completeReinspection = (id: string, newRecordId: string) =>
  put(`/reinspections/${id}/complete`, { newRecordId })

/** 后端分页接口使用 @RequestParam，参数走 query string */
export const pageReinspections = (params: ReinspectionPageQuery & { responsiblePerson?: string }) =>
  post<PageResult<unknown>>('/reinspections/page', null, {
    params: {
      pageNum: params.pageNum,
      pageSize: params.pageSize,
      status: params.status,
      responsibleNo: params.responsibleNo ?? params.responsiblePerson
    }
  })
