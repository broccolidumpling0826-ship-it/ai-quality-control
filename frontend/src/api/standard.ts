import { get, post, put, del } from '@/utils/request'

function parseLimit(val: unknown): number | undefined {
  if (val === '' || val === null || val === undefined) return undefined
  const n = Number(val)
  return Number.isNaN(n) ? undefined : n
}

/** 将前端标准表单映射为后端 QcQualityStandardAddCmd（表单绑定字段优先） */
export function mapStandardPayload(form: Record<string, unknown>) {
  const indicators = (form.indicators as Array<Record<string, unknown>>) || []
  return {
    id: form.id != null && form.id !== '' ? String(form.id) : undefined,
    standardType: form.standardType,
    standardCode: form.standardCode,
    standardName: form.standardName,
    // 编辑时 detail 会带回 variety/grade 等 API 字段；表单 v-model 绑定的是 product* / version / description
    variety: form.productVariety ?? form.variety,
    grade: form.productGrade ?? form.grade,
    specRange: form.specRange || 'DEFAULT',
    versionNo: form.version ?? form.versionNo,
    effectiveDate: form.effectiveDate,
    expiryDate: form.expiryDate || '9999-12-31',
    customerId: form.standardType === 'CUSTOMER' ? (form.customerId as string | undefined) : undefined,
    remark: form.description ?? form.remark,
    indicators: indicators.map((ind) => ({
      indicatorId: ind.indicatorId ?? ind.id,
      upperLimit: parseLimit(ind.upperLimit),
      lowerLimit: parseLimit(ind.lowerLimit),
      isRequired: ind.isRequired ?? 1,
      concessionUpper: parseLimit(ind.concessionUpper),
      concessionLower: parseLimit(ind.concessionLower)
    }))
  }
}

export const addStandard = (data: Record<string, unknown>) => post('/standards', mapStandardPayload(data))
export const updateStandard = (data: Record<string, unknown>) => {
  const payload = mapStandardPayload(data)
  if (!payload.id) {
    return Promise.reject(new Error('标准ID不能为空'))
  }
  return put(`/standards/${payload.id}`, payload)
}
export const publishStandard = (id: string) => put(`/standards/${id}/publish`)
export const deleteStandard = (id: string) => del(`/standards/${id}`)
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

export interface StandardSourceDocumentSummary {
  documentId?: string
  sourceFileName?: string
  sourceFileType?: string
  parseStatus?: string
  indexStatus?: string
  indexedAt?: string
  parseErrorMessage?: string
  chunkCount?: number
  hasSourceFile?: boolean
}

export interface StandardDocumentIngestSummary {
  documentId?: string
  parseStatus?: string
  indexStatus?: string
  chunkCount?: number
  indexedCount?: number
  errorMessage?: string
}

export const listStandardSourceFiles = (standardId: string) =>
  get<StandardSourceDocumentSummary[]>(`/standards/${standardId}/source-files`)

export const uploadStandardSourceFile = (standardId: string, file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return post<StandardSourceDocumentSummary>(`/standards/${standardId}/source-files`, formData)
}

export async function downloadStandardSourceFile(standardId: string, documentId: string): Promise<Blob> {
  const token = localStorage.getItem('qc_token')
  const response = await fetch(`/api/v1/standards/${standardId}/source-files/${documentId}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {}
  })
  if (!response.ok) {
    const text = await response.text()
    throw new Error(text || '下载标准源文件失败')
  }
  return response.blob()
}

export const deleteStandardSourceFile = (standardId: string, documentId: string) =>
  del(`/standards/${standardId}/source-files/${documentId}`)

export const reindexStandardSourceFile = (standardId: string, documentId: string) =>
  post<StandardDocumentIngestSummary>(`/standards/${standardId}/source-files/${documentId}/reindex`)

export const reindexAllStandardSourceFiles = (standardId: string) =>
  post<StandardDocumentIngestSummary[]>(`/standards/${standardId}/source-files/reindex-all`)
