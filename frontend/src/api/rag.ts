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
  /** 限定标准ID，为空则全库检索 */
  standardId?: string
  /** 限定标准类型，为空则不过滤 */
  standardType?: string
}

export interface RagQueryResult {
  found: boolean
  answer?: string
  citations?: Citation[]
  degraded?: boolean
  auditLogId?: string
}

export interface RagIngestRequest {
  /** 关联标准ID */
  standardId: string
  /** 服务器文件路径或上传相对路径 */
  filePath: string
  /** 显示文件名，为空取路径文件名 */
  fileName?: string
}

export interface RagIngestResult {
  id?: string
  standardId?: string
  fileName?: string
  fileType?: string
  chunkCount?: number
  ingestStatus?: string
  ingestTime?: string
}

export const queryRag = (data: RagQueryRequest) =>
  post<RagQueryResult>('/rag/query', data)

export const ingestDocument = (data: RagIngestRequest) =>
  post<RagIngestResult>('/rag/ingest', data)
