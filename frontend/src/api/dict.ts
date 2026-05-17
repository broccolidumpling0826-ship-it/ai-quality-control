import { get } from '@/utils/request'
import type { DictItem, DictMap } from '@/types'

/**
 * 获取所有字典数据（Map 结构：dictCode -> DictItem[]）
 * GET /dict/all
 */
export const getAllDict = () => get<DictMap>('/dict/all')

/**
 * 获取指定字典编码的字典项列表
 * GET /dict/items/:dictCode
 * @param dictCode 字典编码
 */
export const getDictItems = (dictCode: string, refresh = false) =>
  get<DictItem[]>(`/dict/items/${dictCode}`, refresh ? { refresh: true } : undefined)
