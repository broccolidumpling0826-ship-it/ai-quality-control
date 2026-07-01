import { get, post, AI_REQUEST_TIMEOUT } from '@/utils/request'
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
  /** QUALIFIED / UNQUALIFIED / NEED_REINSPECTION / CAN_CONCESSION / STANDARD_CONFLICT */
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
  /** QUALIFIED / UNQUALIFIED / NEED_REINSPECTION / CAN_CONCESSION / STANDARD_CONFLICT */
  finalJudgmentType?: string
  status?: string
  generateTime?: string
  generatedBy?: string
  indicators?: CertIndicatorSnapshot[]
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

export async function downloadCertPdf(id: string): Promise<Blob> {
  const token = localStorage.getItem('qc_token')
  const response = await fetch(`/api/v1/cert-data/${id}/pdf`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {}
  })
  if (!response.ok) {
    const text = await response.text()
    throw new Error(text || '导出质保书PDF失败')
  }
  return response.blob()
}

/** 后端分页接口使用 @RequestParam */
export const pageCertData = (params: CertDataPageQuery) =>
  post<PageResult<CertDataDetail>>('/cert-data/page', null, { params })

export interface CertQaQuery {
  coilNo?: string
  batchNo?: string
  question: string
}

export interface CertQaAnswer {
  answer?: string
  refused?: boolean
  refusalReason?: string
  nonFinal?: boolean
  certificateSnapshotFound?: boolean
  guidanceMessage?: string
  concessionApproved?: boolean
  cacheHit?: boolean
  confidenceLabel?: string
  degradationSource?: string
  degradationReason?: string
  citations?: Array<{
    clauseId?: string
    standardCode?: string
    standardName?: string
    clauseNo?: string
    pageNo?: number
    paragraphText?: string
    score?: number
  }>
  indicatorBasis?: Array<{
    indicatorName?: string
    indicatorCode?: string
    unit?: string
    testValue?: number | string
    upperLimit?: number | string | null
    lowerLimit?: number | string | null
    deviation?: number | string | null
    triggerRule?: string
    isPassed?: number
  }>
}

export const askCertQa = (data: CertQaQuery) =>
  post<CertQaAnswer>('/cert-data/qa', data, { timeout: AI_REQUEST_TIMEOUT })
