import { get, post, AI_REQUEST_TIMEOUT } from '@/utils/request'
import type { PageResult } from '@/types'

export interface JudgmentListItem {
  id: string
  recordId?: string
  /** QUALIFIED / UNQUALIFIED / NEED_REINSPECTION / CAN_CONCESSION / STANDARD_CONFLICT */
  judgmentType?: string
  judgmentTime?: string
  coilNo?: string
  batchNo?: string
  heatNo?: string
  productVariety?: string
  productGrade?: string
  sampleType?: string
  customerId?: string
  inspector?: string
}

export interface JudgmentPageQuery {
  pageNum: number
  pageSize: number
  coilNo?: string
  batchNo?: string
  /** QUALIFIED / UNQUALIFIED / NEED_REINSPECTION / CAN_CONCESSION / STANDARD_CONFLICT */
  judgmentType?: string
  timeStart?: string
  timeEnd?: string
  isFinal?: number
}

export const getJudgmentByRecord = (recordId: string) => get(`/judgments/record/${recordId}`)
export const getJudgmentExplanation = (id: string) =>
  get(`/judgments/${id}/explanation`, undefined, { timeout: AI_REQUEST_TIMEOUT })
export const pageJudgments = (data: JudgmentPageQuery) =>
  post<PageResult<JudgmentListItem>>('/judgments/page', data)
export const getDashboardSummary = () => get('/judgments/dashboard/summary')
