import { post } from '@/utils/request'

export const pageAuditLogs = (data: any) => post('/audit-logs/page', data)
