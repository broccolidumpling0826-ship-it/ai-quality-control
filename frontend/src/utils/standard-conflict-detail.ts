export interface ConflictStandardSnapshot {
  standardId?: string
  standardType?: string
  standardCode?: string
  versionNo?: string
  specRange?: string
  effectiveDate?: string
  expiryDate?: string
  lowerLimit?: number | null
  upperLimit?: number | null
  concessionLower?: number | null
  concessionUpper?: number | null
}

export interface ParsedConflictDetail {
  reason?: string
  blocking?: boolean
  standards: ConflictStandardSnapshot[]
}

function asSnapshot(raw: unknown): ConflictStandardSnapshot | null {
  if (!raw || typeof raw !== 'object') return null
  const item = raw as Record<string, unknown>
  return {
    standardId: stringOrUndefined(item.standardId),
    standardType: stringOrUndefined(item.standardType),
    standardCode: stringOrUndefined(item.standardCode),
    versionNo: stringOrUndefined(item.versionNo),
    specRange: stringOrUndefined(item.specRange),
    effectiveDate: stringOrUndefined(item.effectiveDate),
    expiryDate: stringOrUndefined(item.expiryDate),
    lowerLimit: numberOrNull(item.lowerLimit),
    upperLimit: numberOrNull(item.upperLimit),
    concessionLower: numberOrNull(item.concessionLower),
    concessionUpper: numberOrNull(item.concessionUpper)
  }
}

function stringOrUndefined(value: unknown): string | undefined {
  if (value === null || value === undefined || value === '') return undefined
  return String(value)
}

function numberOrNull(value: unknown): number | null {
  if (value === null || value === undefined || value === '') return null
  const num = Number(value)
  return Number.isFinite(num) ? num : null
}

export function parseConflictDetail(raw?: string | null): ParsedConflictDetail {
  if (!raw) {
    return { standards: [] }
  }
  try {
    const data = JSON.parse(raw) as Record<string, unknown>
    const standards: ConflictStandardSnapshot[] = []
    if (Array.isArray(data.standards)) {
      for (const item of data.standards) {
        const snapshot = asSnapshot(item)
        if (snapshot) standards.push(snapshot)
      }
    } else {
      const left = asSnapshot(data.leftStandard)
      const right = asSnapshot(data.rightStandard)
      if (left) standards.push(left)
      if (right) standards.push(right)
    }
    return {
      reason: stringOrUndefined(data.reason),
      blocking: data.blocking === true,
      standards
    }
  } catch {
    return { reason: raw, standards: [] }
  }
}

export function formatLimitValue(value?: number | null): string {
  if (value === null || value === undefined) return '—'
  return String(value)
}

export function standardTypeLabel(type?: string): string {
  const map: Record<string, string> = {
    NATIONAL: '国标',
    ENTERPRISE: '企标',
    CUSTOMER: '客协'
  }
  return type ? (map[type] || type) : '标准'
}

export function standardTypeTagType(type?: string): 'success' | 'warning' | 'info' | '' {
  if (type === 'NATIONAL') return 'info'
  if (type === 'ENTERPRISE') return 'success'
  if (type === 'CUSTOMER') return 'warning'
  return 'info'
}

export function buildStandardDisplayName(
  snapshot: ConflictStandardSnapshot,
  fallbackId?: string
): string {
  const code = snapshot.standardCode || snapshot.standardId || fallbackId || '未知标准'
  return snapshot.versionNo ? `${code} ${snapshot.versionNo}` : code
}

export function buildInvolvedStandardsSummary(standards: ConflictStandardSnapshot[]): string {
  if (!standards.length) return '—'
  return standards.map((item) => buildStandardDisplayName(item, item.standardId)).join(' / ')
}

export function buildStandardTitle(
  snapshot: ConflictStandardSnapshot,
  grade?: string,
  fallbackId?: string
): string {
  const typeLabel = standardTypeLabel(snapshot.standardType)
  const name = buildStandardDisplayName(snapshot, fallbackId)
  const gradePart = grade ? `${grade} ` : ''
  return `${gradePart}${typeLabel}（${name}）`
}

export function buildEffectiveWindow(snapshot: ConflictStandardSnapshot): string {
  const start = snapshot.effectiveDate || '—'
  const end = snapshot.expiryDate || '—'
  return `${start} ~ ${end}`
}
