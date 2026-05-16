import { get, post } from '@/utils/request'

export const getJudgmentByRecord = (recordId: string) => get(`/judgments/record/${recordId}`)
export const getJudgmentExplanation = (id: string) => get(`/judgments/${id}/explanation`)
export const pageJudgments = (data: any) => post('/judgments/page', data)
export const getDashboardSummary = () => get('/judgments/dashboard/summary')
