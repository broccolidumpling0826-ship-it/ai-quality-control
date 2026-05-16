import { get, post, put } from '@/utils/request'

export const addStandard = (data: any) => post('/standards', data)
export const updateStandard = (data: any) => put(`/standards/${data.id}`, data)
export const publishStandard = (id: string) => put(`/standards/${id}/publish`)
export const pageStandards = (data: any) => post('/standards/page', data)
export const getStandardById = (id: string) => get(`/standards/${id}`)
export const listIndicators = (params?: any) => get('/standards/indicators', params)
