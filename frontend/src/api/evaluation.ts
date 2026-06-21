import { get, post } from '@/utils/request'

export type EvaluationCategory =
  | 'NORMAL'
  | 'BOUNDARY'
  | 'LOW_CONFIDENCE'
  | 'INJECTION'

export interface EvaluationRunRequest {
  promptVersion?: string
  categories?: EvaluationCategory[]
}

export interface EvaluationRunResult {
  runNo: string
  promptVersion?: string
  modelName?: string
  status?: 'RUNNING' | 'COMPLETED' | 'FAILED'
  totalSamples?: number
  passedSamples?: number
  rulePassRate?: number
  aiAccuracyRate?: number
  citationHitRate?: number
  avgLatencyMs?: number
  manualReviewHitRate?: number
  reportJson?: string
  finishedTime?: string
}

export interface EvaluationFailureSample {
  sampleCode?: string
  category?: string
  reason?: string
  expected?: string
  actual?: string
}

export const runEvaluation = (data?: EvaluationRunRequest) =>
  post<EvaluationRunResult>('/evaluation/run', data ?? {})

export const getEvaluationRun = (runNo: string) =>
  get<EvaluationRunResult>(`/evaluation/runs/${runNo}`)
