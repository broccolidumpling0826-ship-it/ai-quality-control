import { get, post, put } from '@/utils/request'
import type { PageResult } from '@/types'

export interface AiAssessment {
  id: string
  assessmentType?: string
  businessType?: string
  businessId?: string
  relatedJudgmentId?: string
  inputSnapshot?: string
  referencesJson?: string
  modelProvider?: string
  modelName?: string
  promptVersion?: string
  rawOutput?: string
  structuredOutput?: string
  riskLevel?: string
  confidenceScore?: number
  confidenceLabel?: string
  confidenceFactors?: string
  degradationSource?: string
  cacheHit?: number
  cacheKey?: string
  adoptionStatus?: string
  humanOpinion?: string
  handledBy?: string
  handledTime?: string
  createUserNo?: string
  createDateTime?: string
}

export interface AiAssessmentPageQuery {
  pageNum: number
  pageSize: number
  assessmentType?: string
  businessType?: string
  businessId?: string
  relatedJudgmentId?: string
  riskLevel?: string
  confidenceLabel?: string
  degradationSource?: string
  adoptionStatus?: string
}

export const pageAiAssessments = (data: AiAssessmentPageQuery) =>
  post<PageResult<AiAssessment>>('/ai-assessments/page', data)

export const getAiAssessment = (id: string) =>
  get<AiAssessment>(`/ai-assessments/${id}`)

export const handleAiAssessment = (id: string, data: { adoptionStatus: 'ADOPTED' | 'IGNORED'; humanOpinion?: string }) =>
  put<AiAssessment>(`/ai-assessments/${id}/handle`, data)
