import { post } from '@/utils/request'
import type { PageResult } from '@/types'

export interface CertDataPageQuery {
  pageNum: number
  pageSize: number
  coilNo?: string
  batchNo?: string
}

export const generateCertData = (data: unknown) => post('/cert-data/generate', data)

/** 后端分页接口使用 @RequestParam */
export const pageCertData = (params: CertDataPageQuery) =>
  post<PageResult<unknown>>('/cert-data/page', null, { params })
