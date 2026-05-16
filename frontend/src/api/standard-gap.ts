import { post, put } from '@/utils/request'

export interface StandardGapVO {
  id: string
  variety: string
  grade: string
  indicatorId: string
  indicatorName: string
  firstFoundTime: string
  relatedRecordId: string
  isResolved: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

export const standardGapApi = {
  page: (params: {
    pageNum: number
    pageSize: number
    variety?: string
    grade?: string
    isResolved?: number | null
  }) => post<PageResult<StandardGapVO>>('/standard-gaps/page', null, { params }),

  resolve: (id: string) => put<void>(`/standard-gaps/${id}/resolve`),
}
