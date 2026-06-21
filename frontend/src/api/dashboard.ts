import { get } from '@/utils/request'
import type { PendingItem, SystemMessage } from '@/types'

export interface DemoScenario {
  demoCode: string
  title: string
  judgmentType: string
  judgmentId?: string
  recordId?: string
  routePath?: string
}

export interface DashboardOverview {
  pendingJudgmentCount: number
  unqualifiedCount: number
  pendingReinspectionCount: number
  pendingConcessionApprovalCount: number
  cacheUpdatedAt?: string
  demoLinks?: DemoScenario[]
  aiRiskAlertCount?: number
}

export const getDashboardOverview = () =>
  get<DashboardOverview>('/dashboard/overview')

export const getDemoScenarios = () =>
  get<DemoScenario[]>('/dashboard/demo-scenarios')

export const getDashboardPendingItems = () =>
  get<PendingItem[]>('/dashboard/pending-items')

export const getDashboardMessages = (userNo?: string) =>
  get<SystemMessage[]>('/dashboard/messages')
