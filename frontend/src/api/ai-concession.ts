import { post } from '@/utils/request'
import type { ConfidenceLevel } from '@/api/ai-judgment'

export interface ConcessionAssessmentRequest {
  judgmentId: string
  concessionId?: string
}

export interface HistoricalCase {
  caseId?: string
  batchNo?: string
  riskLevel?: string
  summary?: string
}

export interface ConcessionAssessment {
  judgmentId?: string
  concessionId?: string
  riskLevel?: 'LOW' | 'MEDIUM' | 'HIGH'
  customerImpact?: string
  suggestedConditions?: string
  historicalCases?: HistoricalCase[] | string
  confidenceLevel?: ConfidenceLevel
  degraded?: boolean
  auditLogId?: string
}

export const assessConcession = (data: ConcessionAssessmentRequest) =>
  post<ConcessionAssessment>('/ai/concession/assess', data)
