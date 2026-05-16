import { get, post, put } from '@/utils/request'
import type { PageResult } from '@/types'

export interface RejudgmentApplyCmd {
  originalJudgmentId: string
  targetJudgmentType: string
  rejudgmentReason: string
  affectScope: string
  newEvidenceSource?: string
  evidenceAttachmentUrl?: string
}

export interface RejudgmentApproveCmd {
  action: 'APPROVED' | 'REJECTED'
  comment?: string
}

export interface RejudgmentPageQuery {
  pageNum: number
  pageSize: number
  approvalStatus?: string
  isReverse?: boolean | number
}

export const applyRejudgment = (data: RejudgmentApplyCmd) => post('/rejudgments', data)

export const approveRejudgment = (id: string, data: RejudgmentApproveCmd) =>
  put(`/rejudgments/${id}/approve`, data)

/** 后端分页接口使用 @RequestParam */
export const pageRejudgments = (params: RejudgmentPageQuery) =>
  post<PageResult<unknown>>('/rejudgments/page', null, {
    params: {
      pageNum: params.pageNum,
      pageSize: params.pageSize,
      approvalStatus: params.approvalStatus,
      isReverse:
        params.isReverse === undefined
          ? undefined
          : params.isReverse === true || params.isReverse === 1
            ? 1
            : 0
    }
  })

export const getRejudgmentById = (id: string) => get(`/rejudgments/${id}`)
