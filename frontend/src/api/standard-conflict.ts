import { get, post } from '@/utils/request'
import type { PageResult } from '@/types'

export interface StandardConflict {
  id: string
  conflictNo?: string
  judgmentId?: string
  recordId?: string
  conflictType?: string
  conflictLevel?: string
  status?: string
  indicatorId?: string
  indicatorName?: string
  unit?: string
  customerId?: string
  variety?: string
  grade?: string
  productSpec?: string
  inspectionDate?: string
  selectedStandardId?: string
  involvedStandardIds?: string[]
  conflictDetail?: string
  selectedPriority?: string
  decisionStandardId?: string
  decisionReason?: string
  decisionBy?: string
  decisionTime?: string
  rejudgeJudgmentId?: string
  createDateTime?: string
}

export interface StandardConflictPageQuery {
  pageNum: number
  pageSize: number
  recordId?: string
  judgmentId?: string
  conflictLevel?: string
  status?: string
  customerId?: string
  variety?: string
  grade?: string
}

export const pageStandardConflicts = (data: StandardConflictPageQuery) =>
  post<PageResult<StandardConflict>>('/standard-conflicts/page', data)

export const getStandardConflict = (id: string) =>
  get<StandardConflict>(`/standard-conflicts/${id}`)

export const resolveStandardConflict = (id: string, data: { decisionStandardId: string; decisionReason: string }) =>
  post<StandardConflict>(`/standard-conflicts/${id}/resolve`, data)
