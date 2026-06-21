import { get, post, put } from '@/utils/request'
import type { ConfidenceLevel } from '@/api/ai-judgment'
import type { PageResult } from '@/types'

export interface InspectionRecordCandidate {
  id: string
  heatNo?: string
  coilNo?: string
  batchNo?: string
  customerId?: string
  productVariety?: string
  productGrade?: string
  testTime?: string
  sampleType?: string
}

export interface ReinspectionCandidateQuery {
  heatNo?: string
  coilNo?: string
  batchNo?: string
  customerId?: string
  excludeRecordId?: string
  pageNum?: number
  pageSize?: number
}

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

export const listReinspectionCandidateInspections = (
  reinspectionId: string,
  body: ReinspectionCandidateQuery
) =>
  post<PageResult<InspectionRecordCandidate>>(
    `/reinspections/${reinspectionId}/candidate-inspections/query`,
    body
  )

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

export interface AiSuggestion {
  suggestionType?: string
  refId?: string
  recommendedAction?: 'YES' | 'NO' | 'CONDITIONAL'
  focusIndicators?: string[] | string
  reasonText?: string
  confidenceLevel?: ConfidenceLevel
  degraded?: boolean
  auditLogId?: string
}

export const getAiSuggestion = (judgmentId: string) =>
  get<AiSuggestion>(`/reinspections/${judgmentId}/ai-suggestion`)
