import { get, post, put } from '@/utils/request'

export const applyRejudgment = (data: any) => post('/rejudgments', data)
export const approveRejudgment = (id: string, data: any) => put(`/rejudgments/${id}/approve`, data)
export const pageRejudgments = (data: any) => post('/rejudgments/page', data)
export const getRejudgmentById = (id: string) => get(`/rejudgments/${id}`)
