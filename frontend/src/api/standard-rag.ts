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
  sourceFileName?: string
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
  embeddingUsed?: boolean
  retrievalMode?: string
  chatPromptSourceCount?: number
  sources?: RagSource[]
}

export interface StandardDocumentIngestCommand {
  documentId: string
  reindexExisting?: boolean
  indexName?: string
  maxChunkChars?: number
}

export interface StandardDocumentIngestResult {
  documentId?: string
  parseStatus?: string
  indexStatus?: string
  extractedPageCount?: number
  chunkCount?: number
  indexedCount?: number
  failedClauseIds?: string[]
  retrievalMode?: string
  errorCategory?: string
  errorMessage?: string
}

export const queryStandardRag = (data: StandardRagQuery) =>
  post<StandardRagAnswer>('/standard-rag/query', data)

export const ingestStandardDocument = (data: StandardDocumentIngestCommand) =>
  post<StandardDocumentIngestResult>('/standard-documents/ingest-index', data)
