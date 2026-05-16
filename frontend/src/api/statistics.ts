import { get } from '@/utils/request'

export const getStatisticsOverview = (params: any) => get('/statistics/overview', params)
export const getIndicatorDistribution = (params: any) => get('/statistics/indicator-distribution', params)
