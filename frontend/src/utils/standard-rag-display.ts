import type { RagSource } from '@/api/standard-rag'

export { degradationLabel, degradationTagType } from './ai-degradation-display'

export function ragSourceTypeLabel(type?: string): string {
  const map: Record<string, string> = {
    NATIONAL: '国标',
    ENTERPRISE: '企标',
    CUSTOMER: '客协',
    COMPLAINT: '案例'
  }
  return type ? (map[type] || type) : '标准'
}

export function ragSourceTypeTagType(type?: string): 'success' | 'warning' | 'info' | '' {
  if (type === 'NATIONAL') return 'info'
  if (type === 'ENTERPRISE') return 'success'
  if (type === 'CUSTOMER') return 'warning'
  return 'info'
}

export function buildRagSourceTitle(source: RagSource, index: number): string {
  const code = source.standardCode || source.standardName || '未知标准'
  const version = source.versionNo ? ` (${source.versionNo})` : ''
  const clause = source.clauseNo ? ` ${source.clauseNo}` : ''
  return `[${index + 1}] ${code} ${ragSourceTypeLabel(source.sourceType)}${version}${clause}`
}

export function buildRagSourceShortTitle(source: RagSource, index: number): string {
  const code = source.standardCode || source.standardName || '未知标准'
  const version = source.versionNo ? ` (${source.versionNo})` : ''
  return `[${index + 1}] ${code} ${ragSourceTypeLabel(source.sourceType)}${version}`
}

export function extractHighlightKeywords(query: string): string[] {
  const tokens = query
    .split(/[\s,，。；;：:？?！!、/\\|]+/)
    .map((item) => item.trim())
    .filter((item) => item.length >= 2)
  return Array.from(new Set(tokens)).slice(0, 8)
}

export function escapeHtml(text: string): string {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

export function highlightParagraph(text: string, keywords: string[]): string {
  if (!text) return ''
  let html = escapeHtml(text)
  for (const keyword of keywords) {
    if (!keyword) continue
    const pattern = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
    html = html.replace(new RegExp(pattern, 'gi'), (match) => `<mark class="rag-highlight">${match}</mark>`)
  }
  return html
}

export function sourceKey(source: RagSource, index: number): string {
  return source.clauseId || source.clauseNo || `${source.standardCode || 'source'}-${index}`
}
