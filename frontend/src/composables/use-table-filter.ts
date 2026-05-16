import type { Ref } from 'vue'

export interface FilterOption {
  text: string
  value: string
}

/**
 * 为 el-table 表格列提供前端本地筛选能力。
 * 根据当前页数据自动生成每列的唯一值列表作为筛选选项。
 *
 * 用法：
 *   const { getFilters, filterMethod } = useTableFilter(tableData)
 *
 *   <el-table-column
 *     prop="variety"
 *     :filters="getFilters('variety')"
 *     :filter-method="filterMethod"
 *     filter-placement="bottom-start"
 *     :filter-search="true"
 *   />
 *
 * @param data     响应式表格数据（当前页）
 */
export function useTableFilter<T extends Record<string, any>>(data: Ref<T[]>) {
  /**
   * 从当前页数据中提取某列的唯一值列表，用于 el-table-column 的 :filters 属性。
   * @param prop       列字段名，对应 el-table-column 的 prop
   * @param formatter  可选，将原始值转换为显示文本（如翻译字典值）
   */
  function getFilters(prop: string, formatter?: (val: any) => string): FilterOption[] {
    const map = new Map<string, string>()
    for (const row of data.value) {
      const raw = row[prop]
      if (raw == null || raw === '') continue
      const key = String(raw)
      const label = formatter ? formatter(raw) : key
      if (!map.has(key)) {
        map.set(key, label)
      }
    }
    return Array.from(map.entries())
      .map(([value, text]) => ({ text, value }))
      .sort((a, b) => a.text.localeCompare(b.text, 'zh-CN'))
  }

  /**
   * el-table-column 的 :filter-method 函数。
   * El Plus 对每个选中的 filter value 调用此函数，
   * 任意一个 value 返回 true 则显示该行。
   *
   * @param value   当前筛选选项的 value（来自 getFilters 生成的 FilterOption.value）
   * @param row     当前行数据
   * @param column  列配置（含 column.property = prop 名称）
   */
  function filterMethod(value: string, row: T, column: { property: string }): boolean {
    const cellVal = String(row[column.property] ?? '')
    return cellVal === value
  }

  return { getFilters, filterMethod }
}
