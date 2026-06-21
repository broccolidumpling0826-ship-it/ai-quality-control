import { post } from '@/utils/request'

export interface StandardRagQuery {
  query: string
  sourceTypes?: string[]
  customerId?: string
  variety?: string
  grade?: string
  indicatorCode?: string
  effectiveDate?: string
  topK?: number
}

export interface RagSource {
  clauseId?: string
  documentId?: string
  sourceType?: string
  standardCode?: string
  standardName?: string
  versionNo?: string
  clauseNo?: string
  pageNo?: number
  paragraphText?: string
  score?: number
  referenceOnly?: boolean
}

export interface StandardRagAnswer {
  query?: string
  answer?: string
  refused?: boolean
  refusalReason?: string
  confidenceLabel?: string
  confidenceScore?: number
  degradationSource?: string
  degradationReason?: string
  cacheHit?: boolean
  sources?: RagSource[]
}

export const queryStandardRag = (data: StandardRagQuery) =>
  post<StandardRagAnswer>('/standard-rag/query', data)
