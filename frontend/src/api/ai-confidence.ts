import { get, put } from '@/utils/request'

export interface AiConfidenceConfig {
  id?: string
  configName: string
  ruleWeight: number
  ragWeight: number
  llmWeight: number
  highThreshold: number
  mediumThreshold: number
  lowThreshold: number
  enabled: number
  activeFlag?: number
  updatedBy?: string
  updatedAt?: string
  remark?: string
}

export const getActiveAiConfidenceConfig = () =>
  get<AiConfidenceConfig>('/ai-confidence/active')

export const updateActiveAiConfidenceConfig = (data: AiConfidenceConfig) =>
  put<AiConfidenceConfig>('/ai-confidence/active', data)
