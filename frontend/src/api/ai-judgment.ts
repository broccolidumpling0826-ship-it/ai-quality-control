import { get } from '@/utils/request'
import type { Citation } from '@/api/rag'

export type ConfidenceLevel = 'HIGH' | 'MEDIUM' | 'LOW'

export interface AiJudgmentExplanation {
  judgmentId: string
  narrativeText?: string
  confidenceLevel?: ConfidenceLevel
  manualReviewRequired?: boolean
  degraded?: boolean
  baselineJson?: string
  citations?: Citation[]
  auditLogId?: string
}

export const getAiJudgmentExplanation = (judgmentId: string) =>
  get<AiJudgmentExplanation>(`/judgments/${judgmentId}/ai-explanation`)

export const getDemoAiJudgmentExplanation = (demoCode: string) =>
  get<AiJudgmentExplanation>(`/judgments/demo/${demoCode}/ai-explanation`)
