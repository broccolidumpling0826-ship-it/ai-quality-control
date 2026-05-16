import { get, post, put } from '@/utils/request'

export const applyConcession = (data: any) => post('/concessions', data)
export const confirmConcession = (id: string, formData: FormData) => post(`/concessions/${id}/confirm`, formData)
export const rejectConcession = (id: string, data: any) => put(`/concessions/${id}/reject`, data)
export const approveConcession = (id: string, data: any) => put(`/concessions/${id}/approve`, data)
export const pageConcessions = (data: any) => post('/concessions/page', data)
export const getConcessionById = (id: string) => get(`/concessions/${id}`)
