import { get, post } from '@/utils/request'
import type { PageResult } from '@/types'

export interface CertIndicatorSnapshot {
  indicatorName?: string
  indicatorCode?: string
  unit?: string
  testValue?: number | string
  upperLimit?: number | string | null
  lowerLimit?: number | string | null
  isPassed?: number | boolean
  /** PASS / FAIL / CONCESSION / WARNING */
  indicatorResult?: string
  finalJudgmentType?: string
}

export interface CertDataDetail {
  id?: string
  coilNo?: string
  batchNo?: string
  heatNo?: string
  productVariety?: string
  productGrade?: string
  customerId?: string
  finalJudgmentType?: string
  status?: string
  generateTime?: string
  generatedBy?: string
  aiSummaryText?: string
  indicators?: CertIndicatorSnapshot[]
}

export interface CertAiSummaryResult {
  batchNo?: string
  aiSummaryText?: string
  degraded?: boolean
  auditLogId?: string
}

export interface CertDataPageQuery {
  pageNum: number
  pageSize: number
  coilNo?: string
  batchNo?: string
  startTime?: string
  endTime?: string
}

export interface CertGeneratePayload {
  queryType: 'COIL' | 'BATCH'
  coilNo?: string
  batchNo?: string
}

/** 映射为后端 QcQualityCertGenerateCmd（queryType 必填） */
export function mapCertGeneratePayload(form: {
  coilNo?: string
  batchNo?: string
}): CertGeneratePayload {
  const coilNo = form.coilNo?.trim()
  const batchNo = form.batchNo?.trim()
  if (coilNo) {
    return { queryType: 'COIL', coilNo }
  }
  return { queryType: 'BATCH', batchNo: batchNo! }
}

export const generateCertData = (data: CertGeneratePayload) =>
  post<CertDataDetail>('/cert-data/generate', data)

export const getCertDataById = (id: string) => get<CertDataDetail>(`/cert-data/${id}`)

/** 后端分页接口使用 @RequestParam */
export const pageCertData = (params: CertDataPageQuery) =>
  post<PageResult<CertDataDetail>>('/cert-data/page', null, { params })

export const generateAiSummary = (batchNo: string) =>
  post<CertAiSummaryResult>(`/cert-data/${encodeURIComponent(batchNo)}/ai-summary`)
