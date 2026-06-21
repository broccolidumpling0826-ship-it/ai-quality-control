import { get, put } from '@/utils/request'
import type { PageResult } from '@/types'
import type { Citation } from '@/api/rag'

export interface AiAuditDashboard {
  callCount?: number
  totalTokens?: number
  avgLatencyMs?: number
  errorRate?: number
  degradedCount?: number
  bySource?: Record<string, number>
  cacheUpdatedAt?: string
}

export interface AiAuditLogItem {
  id: string
  callSource?: string
  promptKey?: string
  promptVersion?: string
  modelName?: string
  inputSummary?: string
  outputSummary?: string
  totalTokens?: number
  latencyMs?: number
  success?: boolean
  degraded?: boolean
  errorType?: string
  traceId?: string
  bizRefId?: string
  createDateTime?: string
}

export interface AiAuditReplay {
  auditLogId?: string
  inputSummary?: string
  outputSummary?: string
  citations?: Citation[]
  degraded?: boolean
  promptVersion?: string
  promptKey?: string
  modelName?: string
  errorType?: string
  latencyMs?: number
  totalTokens?: number
}

export interface AiPromptVersion {
  promptKey: string
  versionNo: string
  description?: string
  isActive?: boolean
  filePath?: string
}

export interface AiAuditLogPageQuery {
  pageNum: number
  pageSize: number
  callSource?: string
  success?: boolean
  degraded?: boolean
}

export const getAiAuditDashboard = () =>
  get<AiAuditDashboard>('/ai/audit/dashboard')

export const pageAiAuditLogs = (params: AiAuditLogPageQuery) =>
  get<PageResult<AiAuditLogItem>>('/ai/audit/logs', params as Record<string, unknown>)

export const replayAiAuditLog = (auditLogId: string) =>
  get<AiAuditReplay>(`/ai/audit/logs/${auditLogId}/replay`)

export const listPromptVersions = (promptKey?: string) =>
  get<AiPromptVersion[]>('/ai/prompts', promptKey ? { promptKey } : undefined)

export const activatePromptVersion = (promptKey: string, versionNo: string) =>
  put(`/ai/prompts/${promptKey}/activate`, { versionNo })
