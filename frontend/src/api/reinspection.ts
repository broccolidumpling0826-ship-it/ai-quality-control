import { post, put } from '@/utils/request'

export const initiateReinspection = (data: any) => post('/reinspections', data)
export const completeReinspection = (id: string, newRecordId: string) => put(`/reinspections/${id}/complete`, { newRecordId })
export const pageReinspections = (data: any) => post('/reinspections/page', data)
