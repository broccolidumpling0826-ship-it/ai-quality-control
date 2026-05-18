import { get, post, put } from '@/utils/request'
import type { PageResult } from '@/types'

export interface ConcessionPageQuery {
  pageNum: number
  pageSize: number
  confirmStatus?: string
  approvalStatus?: string
  coilNo?: string
}

export interface ConcessionApplyCmd {
  judgmentId: string
  concessionScope: string
  riskDescription: string
  effectiveDate?: string
  expiryDate?: string
}

export const applyConcession = (data: ConcessionApplyCmd) => post<string>('/concessions', data)

export const confirmConcession = (id: string, formData: FormData) => {
  const body = new FormData()
  const file = formData.get('file') ?? formData.get('confirmFile')
  if (file instanceof Blob) {
    body.append('file', file)
  }
  const summary = formData.get('summary') ?? formData.get('confirmNote')
  const note = summary != null ? String(summary).trim() : ''
  body.append(
    'cmd',
    new Blob([JSON.stringify({ confirmNote: note || undefined })], { type: 'application/json' })
  )
  return put(`/concessions/${id}/confirm`, body)
}

export const rejectConcession = (id: string, data: { rejectReason?: string; rejectNote?: string }) =>
  put(`/concessions/${id}/reject`, null, {
    params: { rejectNote: data.rejectReason ?? data.rejectNote }
  })

export const approveConcession = (id: string, data: { comment?: string }) =>
  put(`/concessions/${id}/approve`, null, { params: { comment: data.comment } })

/** 后端分页接口使用 @RequestParam；总状态对应 approvalStatus */
export const pageConcessions = (params: ConcessionPageQuery & { concessionStatus?: string }) =>
  post<PageResult<unknown>>('/concessions/page', null, {
    params: {
      pageNum: params.pageNum,
      pageSize: params.pageSize,
      confirmStatus: params.confirmStatus,
      approvalStatus: params.approvalStatus ?? params.concessionStatus,
      coilNo: params.coilNo
    }
  })

export const getConcessionById = (id: string) => get(`/concessions/${id}`)
