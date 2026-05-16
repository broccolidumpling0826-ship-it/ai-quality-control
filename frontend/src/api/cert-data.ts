import { post } from '@/utils/request'

export const generateCertData = (data: any) => post('/cert-data/generate', data)
export const pageCertData = (data: any) => post('/cert-data/page', data)
