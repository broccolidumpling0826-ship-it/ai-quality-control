import { get, post } from '@/utils/request'
import type { PageResult } from '@/types'
import type { Citation } from '@/api/rag'

export interface StandardConflictItem {
  id: string
  variety?: string
  grade?: string
  indicatorId?: string
  indicatorName?: string
  standardIdA?: string
  standardIdB?: string
  standardNameA?: string
  standardNameB?: string
  limitAUpper?: number
  limitALower?: number
  limitBUpper?: number
  limitBLower?: number
  conflictStatus?: string
  resolutionNote?: string
  resolvedBy?: string
  resolvedTime?: string
  demoFlag?: number
}

export interface StandardConflictDetail extends StandardConflictItem {
  citations?: Citation[]
  chunkRefA?: string
  chunkRefB?: string
}

export interface StandardConflictPageQuery {
  pageNum: number
  pageSize: number
  status?: string
  variety?: string
  grade?: string
}

export interface StandardConflictResolveCmd {
  conflictStatus: string
  resolutionNote?: string
}

export const pageStandardConflicts = (params: StandardConflictPageQuery) =>
  get<PageResult<StandardConflictItem>>('/standard-conflicts', params as Record<string, unknown>)

export const getStandardConflict = (id: string) =>
  get<StandardConflictDetail>(`/standard-conflicts/${id}`)

export const resolveStandardConflict = (id: string, data: StandardConflictResolveCmd) =>
  post(`/standard-conflicts/${id}/resolve`, data)
