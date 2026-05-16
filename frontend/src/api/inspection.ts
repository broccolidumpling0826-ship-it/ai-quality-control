import { get, post, put } from '@/utils/request'
import type { PageResult } from '@/types'

export interface InspectionAddPayload {
  heatNo: string
  coilNo: string
  customerId?: string
  productVariety: string
  productGrade: string
  productSpec: string
  sampleType: string
  testTime: string
  testerNo: string
  values: Array<{
    indicatorId: string
    testValue?: number | string
    valueText?: string
  }>
}

export interface InspectionPageQuery {
  pageNum: number
  pageSize: number
  coilNo?: string
  heatNo?: string
  status?: string
  sampleType?: string
  testTimeStart?: string
  testTimeEnd?: string
}

/** 将前端表单字段映射为后端 QcInspectionRecordAddCmd */
export function mapInspectionAddPayload(form: Record<string, unknown>): InspectionAddPayload {
  const indicators = (form.indicators as Array<Record<string, unknown>>) || []
  return {
    heatNo: String(form.heatNo ?? ''),
    coilNo: String(form.coilNo ?? ''),
    customerId: (form.customerId ?? form.customer) as string | undefined,
    productVariety: String(form.productVariety ?? ''),
    productGrade: String(form.productGrade ?? ''),
    productSpec: String(form.productSpec ?? form.specification ?? ''),
    sampleType: String(form.sampleType ?? ''),
    testTime: String(form.testTime ?? form.inspectionTime ?? ''),
    testerNo: String(form.testerNo ?? form.inspector ?? ''),
    values: indicators.map((row) => {
      const raw = row.measuredValue ?? row.testValue
      const num = raw !== '' && raw != null ? Number(raw) : NaN
      return {
        indicatorId: String(row.indicatorId ?? ''),
        ...(Number.isFinite(num) ? { testValue: num } : { valueText: String(raw ?? '') })
      }
    })
  }
}

export const addInspection = (data: InspectionAddPayload) => post('/inspections', data)

export const voidInspection = (id: string, reason: string) => put(`/inspections/${id}/void`, { reason })

export const pageInspections = (data: InspectionPageQuery) => post<PageResult<unknown>>('/inspections/page', data)

export const getInspectionById = (id: string) => get(`/inspections/${id}`)
