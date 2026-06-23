import { computed } from 'vue'

export type CitationTextPart =
  | { type: 'text'; value: string }
  | { type: 'citation'; index: number }

const CITATION_MARKER = /\[(\d+)\]/g

export function parseCitationText(text: string, fallback = ''): CitationTextPart[] {
  const safeText = text || fallback
  if (!safeText) {
    return [{ type: 'text', value: fallback || '' }]
  }
  const parts: CitationTextPart[] = []
  let lastIndex = 0
  let match: RegExpExecArray | null
  const regex = new RegExp(CITATION_MARKER.source, 'g')
  while ((match = regex.exec(safeText)) !== null) {
    if (match.index > lastIndex) {
      parts.push({ type: 'text', value: safeText.slice(lastIndex, match.index) })
    }
    parts.push({ type: 'citation', index: Number(match[1]) })
    lastIndex = regex.lastIndex
  }
  if (lastIndex < safeText.length) {
    parts.push({ type: 'text', value: safeText.slice(lastIndex) })
  }
  return parts.length ? parts : [{ type: 'text', value: safeText }]
}

export function useCitationParts(getText: () => string | undefined | null, fallback = '') {
  return computed(() => parseCitationText(getText() || '', fallback))
}
