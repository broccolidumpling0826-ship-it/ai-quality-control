import { get, post, put } from '@/utils/request'

export const getUnreadCount = () => get('/notifications/unread-count')
export const pageNotifications = (data: any) => post('/notifications/page', data)
export const markRead = (id: string) => put(`/notifications/${id}/read`)
export const markAllRead = () => put('/notifications/read-all')
