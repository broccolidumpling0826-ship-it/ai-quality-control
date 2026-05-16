import { get, post, put } from '@/utils/request'

export const addInspection = (data: any) => post('/inspections', data)
export const voidInspection = (id: string, reason: string) => put(`/inspections/${id}/void`, { reason })
export const pageInspections = (data: any) => post('/inspections/page', data)
export const getInspectionById = (id: string) => get(`/inspections/${id}`)
