export function degradationLabel(source?: string, cacheHit?: boolean): string {
  if (cacheHit) return '缓存回答'
  const map: Record<string, string> = {
    GENERATED: 'AI生成',
    CACHE: '缓存回答',
    RULE_TEMPLATE: '规则模板',
    RAW_RETRIEVAL: '仅展示条款',
    UNAVAILABLE: '无引用依据',
    SECURITY_REFUSAL: '安全拒答',
    CONFLICT_REJUDGE: '冲突重判'
  }
  return source ? (map[source] || source) : '未知'
}

export function degradationTagType(
  source?: string,
  cacheHit?: boolean
): 'warning' | 'info' | 'danger' | 'success' {
  if (cacheHit || source === 'CACHE') return 'warning'
  if (source === 'GENERATED') return 'success'
  if (source === 'UNAVAILABLE') return 'danger'
  if (source === 'SECURITY_REFUSAL') return 'danger'
  if (source === 'CONFLICT_REJUDGE') return 'warning'
  if (source === 'RAW_RETRIEVAL') return 'info'
  return 'info'
}

export function explanationTraceLabel(trace?: string): string {
  const map: Record<string, string> = {
    MODEL_GENERATED: '已调模型·引用校验通过',
    MODEL_REJECTED: '已调模型·引用校验未通过',
    ASSESSMENT_REUSE: '未调模型·复用历史评估',
    CACHE_HIT: '未调模型·缓存命中',
    MODEL_DISABLED: '未调模型·AI未启用',
    STRUCTURED_ONLY: '未调模型·仅规则模板',
    CONFLICT_REJUDGE: '冲突裁决后重判·规则模板',
    SKIPPED: '无来源引用·未调模型'
  }
  return map[trace || ''] || trace || '路径未知'
}
