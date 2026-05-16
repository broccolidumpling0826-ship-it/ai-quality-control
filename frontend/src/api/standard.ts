import { get, post, put } from '@/utils/request'

/** 将前端标准表单映射为后端 QcQualityStandardAddCmd */
export function mapStandardPayload(form: Record<string, unknown>) {
  const indicators = (form.indicators as Array<Record<string, unknown>>) || []
  return {
    id: form.id as string | undefined,
    standardType: form.standardType,
    variety: form.variety ?? form.productVariety,
    grade: form.grade ?? form.productGrade,
    specRange: form.specRange ?? form.standardName ?? 'DEFAULT',
    versionNo: form.versionNo ?? form.version,
    effectiveDate: form.effectiveDate,
    expiryDate: form.expiryDate,
    customerId: form.customerId,
    remark: form.remark ?? form.description,
    indicators: indicators.map((ind) => ({
      indicatorId: ind.indicatorId ?? ind.id,
      upperLimit: ind.upperLimit,
      lowerLimit: ind.lowerLimit,
      isRequired: ind.isRequired,
      concessionUpper: ind.concessionUpper,
      concessionLower: ind.concessionLower
    }))
  }
}

export const addStandard = (data: Record<string, unknown>) => post('/standards', mapStandardPayload(data))
export const updateStandard = (data: Record<string, unknown>) =>
  put(`/standards/${data.id}`, mapStandardPayload(data))
export const publishStandard = (id: string) => put(`/standards/${id}/publish`)
export const pageStandards = (data: any) => post('/standards/page', data)
export const getStandardById = (id: string) => get(`/standards/${id}`)
export const listIndicators = (params?: any) => get('/standards/indicators', params)

export interface SpecRangeOption {
  value: string
  label: string
  standardId: string
}

/** 查询有效规格范围下拉选项（D-016：product_spec 强制下拉，禁止自由文本） */
export const getSpecRanges = (params: {
  variety: string
  grade: string
  customerId?: string
}) => get<SpecRangeOption[]>('/standards/spec-ranges', params as Record<string, unknown>)
