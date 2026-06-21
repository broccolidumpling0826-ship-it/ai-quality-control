import { post } from '@/utils/request'

export interface Citation {
  chunkId: string
  standardId?: string
  sectionRef?: string
  highlightText: string
  standardName?: string
}

export interface RagQueryRequest {
  question: string
  standardType?: string
  context?: string
}

export interface RagQueryResult {
  found: boolean
  answer?: string
  message?: string
  citations?: Citation[]
  degraded?: boolean
  auditLogId?: string
}

export interface RagIngestResult {
  documentId?: string
  chunkCount?: number
  fileName?: string
}

export const queryRag = (data: RagQueryRequest) =>
  post<RagQueryResult>('/rag/query', data)

export const ingestRagDocument = (formData: FormData) =>
  post<RagIngestResult>('/rag/ingest', formData)
