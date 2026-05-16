import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getAllDict } from '@/api/dict'
import type { DictItem, DictMap } from '@/types'

export const useDictStore = defineStore('dict', () => {
  const dictMap = ref<DictMap>({})
  const loaded = ref(false)
  const loading = ref(false)

  /** 加载所有字典数据（GET /dict/all） */
  async function loadAll(): Promise<void> {
    if (loaded.value || loading.value) return
    loading.value = true
    try {
      const data = await getAllDict() as DictMap
      dictMap.value = data || {}
      loaded.value = true
    } catch (err) {
      console.error('[DictStore] 字典加载失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  /** 强制刷新字典 */
  async function reload(): Promise<void> {
    loaded.value = false
    await loadAll()
  }

  /**
   * 获取指定 dictCode 的字典项列表
   * @param dictCode 字典编码
   */
  function getItems(dictCode: string): DictItem[] {
    return dictMap.value[dictCode] ?? []
  }

  /**
   * 根据 value 获取对应的 label
   * @param dictCode 字典编码
   * @param value 字典值
   */
  function getLabel(dictCode: string, value: string | number | null | undefined): string {
    if (value === null || value === undefined || value === '') return '-'
    const items = getItems(dictCode)
    const found = items.find((item) => item.value === String(value))
    return found?.label ?? String(value)
  }

  /**
   * 根据 value 获取对应的 colorTag（Element Plus tag type）
   * @param dictCode 字典编码
   * @param value 字典值
   */
  function getColorTag(dictCode: string, value: string | number | null | undefined): string {
    if (value === null || value === undefined || value === '') return 'info'
    const items = getItems(dictCode)
    const found = items.find((item) => item.value === String(value))
    return found?.colorTag ?? 'info'
  }

  return {
    dictMap,
    loaded,
    loading,
    loadAll,
    reload,
    getItems,
    getLabel,
    getColorTag
  }
})
