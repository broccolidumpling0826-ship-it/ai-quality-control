import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const dirname = path.dirname(fileURLToPath(import.meta.url))
const defaultCasesPath = path.resolve(dirname, '../evaluation-cases.json')

const args = parseArgs(process.argv.slice(2))
const casesPath = path.resolve(process.cwd(), args.cases || defaultCasesPath)
const actualPath = args.actual ? path.resolve(process.cwd(), args.actual) : ''

const fixture = readJson(casesPath)
const cases = Array.isArray(fixture.cases) ? fixture.cases : []
const actualResults = actualPath ? normalizeActualResults(readJson(actualPath)) : new Map()
const validationErrors = validateCases(cases)
const coverage = buildCoverage(cases)
const metrics = actualResults.size > 0 ? calculateMetrics(cases, actualResults) : null

const report = {
  change: fixture.change,
  version: fixture.version,
  generatedAt: new Date().toISOString(),
  casesPath,
  actualPath: actualPath || null,
  totalCases: cases.length,
  validation: {
    passed: validationErrors.length === 0,
    errors: validationErrors
  },
  coverage,
  metricsSchema: [
    'businessRulePassRate',
    'aiConclusionAccuracy',
    'citationHitRate',
    'refusalAccuracy',
    'confidenceBandAccuracy',
    'averageResponseTimeMs',
    'manualReviewHitRate'
  ],
  metrics,
  note: metrics
    ? 'Metrics are calculated from actual result JSON.'
    : 'No actual result file supplied. This report validates fixture coverage only.'
}

console.log(JSON.stringify(report, null, 2))

if (validationErrors.length > 0) {
  process.exitCode = 1
}

function parseArgs(argv) {
  const result = {}
  for (let i = 0; i < argv.length; i += 1) {
    const arg = argv[i]
    if (arg === '--cases') {
      result.cases = argv[i + 1]
      i += 1
    } else if (arg === '--actual') {
      result.actual = argv[i + 1]
      i += 1
    }
  }
  return result
}

function readJson(filePath) {
  return JSON.parse(fs.readFileSync(filePath, 'utf8'))
}

function normalizeActualResults(payload) {
  const list = Array.isArray(payload) ? payload : payload.results
  const map = new Map()
  if (!Array.isArray(list)) {
    return map
  }
  for (const item of list) {
    if (item && item.id) {
      map.set(item.id, item)
    }
  }
  return map
}

function validateCases(list) {
  const errors = []
  const ids = new Set()
  const requiredExpectedFields = [
    'judgmentType',
    'citationIds',
    'refusal',
    'confidenceBand',
    'manualReview',
    'numericTolerance'
  ]

  list.forEach((item, index) => {
    if (!item.id) {
      errors.push(`case[${index}] missing id`)
      return
    }
    if (ids.has(item.id)) {
      errors.push(`${item.id} duplicate id`)
    }
    ids.add(item.id)
    for (const field of ['category', 'surface', 'description', 'endpoint', 'request', 'expected']) {
      if (!(field in item)) {
        errors.push(`${item.id} missing ${field}`)
      }
    }
    if (item.expected) {
      for (const field of requiredExpectedFields) {
        if (!(field in item.expected)) {
          errors.push(`${item.id} missing expected.${field}`)
        }
      }
      if (!Array.isArray(item.expected.citationIds)) {
        errors.push(`${item.id} expected.citationIds must be array`)
      }
    }
  })

  const categoryCounts = countBy(list, (item) => item.category)
  if ((categoryCounts.normal || 0) < 10) {
    errors.push('normal category must contain at least 10 cases')
  }
  if ((categoryCounts.boundary_abnormal || 0) < 10) {
    errors.push('boundary_abnormal category must contain at least 10 cases')
  }
  if ((categoryCounts.low_confidence_refusal || 0) < 5) {
    errors.push('low_confidence_refusal category must contain at least 5 cases')
  }
  if ((categoryCounts.prompt_injection_security || 0) < 5) {
    errors.push('prompt_injection_security category must contain at least 5 cases')
  }

  return errors
}

function buildCoverage(list) {
  return {
    byCategory: countBy(list, (item) => item.category),
    bySurface: countBy(list, (item) => item.surface),
    expectedConfidenceBands: countBy(list, (item) => item.expected.confidenceBand),
    expectedManualReview: countBy(list, (item) => String(item.expected.manualReview)),
    expectedRefusal: countBy(list, (item) => String(item.expected.refusal))
  }
}

function calculateMetrics(list, actualMap) {
  let compared = 0
  let businessRulePass = 0
  let aiConclusionPass = 0
  let citationPass = 0
  let refusalPass = 0
  let confidencePass = 0
  let manualReviewPass = 0
  let responseTimeTotal = 0
  let responseTimeCount = 0
  const missingActual = []

  for (const item of list) {
    const actual = actualMap.get(item.id)
    if (!actual) {
      missingActual.push(item.id)
      continue
    }
    compared += 1
    if (matchesNullable(item.expected.judgmentType, actual.judgmentType)) {
      businessRulePass += 1
      aiConclusionPass += 1
    }
    if (containsAll(actual.citationIds || actual.citations || [], item.expected.citationIds)) {
      citationPass += 1
    }
    if (Boolean(actual.refusal) === Boolean(item.expected.refusal)) {
      refusalPass += 1
    }
    if (String(actual.confidenceBand || actual.confidenceLabel || '') === item.expected.confidenceBand) {
      confidencePass += 1
    }
    if (Boolean(actual.manualReview) === Boolean(item.expected.manualReview)) {
      manualReviewPass += 1
    }
    if (typeof actual.responseTimeMs === 'number') {
      responseTimeTotal += actual.responseTimeMs
      responseTimeCount += 1
    }
  }

  return {
    compared,
    missingActual,
    businessRulePassRate: rate(businessRulePass, compared),
    aiConclusionAccuracy: rate(aiConclusionPass, compared),
    citationHitRate: rate(citationPass, compared),
    refusalAccuracy: rate(refusalPass, compared),
    confidenceBandAccuracy: rate(confidencePass, compared),
    averageResponseTimeMs: responseTimeCount === 0 ? null : Number((responseTimeTotal / responseTimeCount).toFixed(2)),
    manualReviewHitRate: rate(manualReviewPass, compared)
  }
}

function countBy(list, getter) {
  return list.reduce((acc, item) => {
    const key = getter(item) || 'UNKNOWN'
    acc[key] = (acc[key] || 0) + 1
    return acc
  }, {})
}

function containsAll(actualIds, expectedIds) {
  const actualSet = new Set(actualIds.map(String))
  return expectedIds.every((id) => actualSet.has(String(id)))
}

function matchesNullable(expected, actual) {
  if (expected === null || expected === undefined) {
    return actual === null || actual === undefined || actual === ''
  }
  return String(expected) === String(actual)
}

function rate(pass, total) {
  if (total === 0) {
    return null
  }
  return Number((pass / total).toFixed(4))
}
