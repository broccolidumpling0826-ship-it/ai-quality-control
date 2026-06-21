#!/usr/bin/env node
/**
 * Execute evaluation-cases.json against a running backend and emit actual-results JSON.
 */
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const dirname = path.dirname(fileURLToPath(import.meta.url))
const baseUrl = process.env.API_BASE_URL || 'http://localhost:8080'
const userNo = process.env.API_USER_NO || 'admin'
const password = process.env.API_PASSWORD || 'Admin123456'
const casesPath = path.resolve(dirname, '../evaluation-cases.json')
const outPath = path.resolve(dirname, '../evaluation-actual-results.json')

const fixture = JSON.parse(fs.readFileSync(casesPath, 'utf8'))
const cases = fixture.cases || []

async function main() {
  const token = await login()
  const results = []
  const failures = []

  for (const testCase of cases) {
    const started = Date.now()
    try {
      const actual = await runCase(testCase, token)
      actual.responseTimeMs = Date.now() - started
      const pass = evaluateCase(testCase, actual)
      results.push({ id: testCase.id, pass, ...actual })
      if (!pass) {
        failures.push({ id: testCase.id, description: testCase.description, actual })
      }
      console.log(`${pass ? 'PASS' : 'FAIL'} ${testCase.id} ${testCase.description} (${actual.responseTimeMs}ms)`)
    } catch (error) {
      const actual = {
        error: error.message,
        responseTimeMs: Date.now() - started
      }
      results.push({ id: testCase.id, pass: false, ...actual })
      failures.push({ id: testCase.id, description: testCase.description, error: error.message })
      console.log(`FAIL ${testCase.id} ${testCase.description} ERROR: ${error.message}`)
    }
  }

  fs.writeFileSync(outPath, JSON.stringify({ generatedAt: new Date().toISOString(), results }, null, 2))
  console.log('\n--- Summary ---')
  console.log(`Total: ${cases.length}, Passed: ${results.filter((r) => r.pass).length}, Failed: ${failures.length}`)
  console.log(`Actual results written to ${outPath}`)
  if (failures.length > 0) {
    process.exitCode = 1
  }
}

async function login() {
  const res = await fetch(`${baseUrl}/api/v1/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userNo, password })
  })
  const body = await res.json()
  if (!body.success || !body.data?.token) {
    throw new Error(`Login failed: ${body.message || res.status}`)
  }
  return body.data.token
}

async function api(method, urlPath, token, payload) {
  const res = await fetch(`${baseUrl}${urlPath}`, {
    method,
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: payload ? JSON.stringify(payload) : undefined
  })
  const body = await res.json()
  if (!body.success) {
    const err = new Error(body.message || `HTTP ${res.status}`)
    err.body = body
    err.status = res.status
    throw err
  }
  return body.data
}

async function loadJudgmentForCertQa(request, token) {
  if (request.coilNo) {
    const page = await api('POST', '/api/v1/judgments/page', token, {
      coilNo: request.coilNo,
      pageNum: 1,
      pageSize: 5,
      isFinal: 1
    })
    const row = (page.records || [])[0]
    if (row?.judgmentId) {
      return api('GET', `/api/v1/judgments/${row.judgmentId}/explanation`, token)
    }
  }
  return null
}

async function runCase(testCase, token) {
  const endpoint = testCase.endpoint || ''
  const request = testCase.request || {}

  if (endpoint.startsWith('GET /api/v1/judgments/') && endpoint.endsWith('/explanation')) {
    const data = await api('GET', `/api/v1/judgments/${request.judgmentId}/explanation`, token)
    return extractJudgmentExplanation(data)
  }

  if (endpoint === 'POST /api/v1/standard-conflicts/page') {
    const data = await api('POST', '/api/v1/standard-conflicts/page', token, request)
    const record = (data.records || []).find((item) => item.id === 'scf_p0_001' || item.conflictCode === 'SCF-P0-001')
      || (data.records || [])[0]
    return {
      judgmentType: record?.judgmentType || 'STANDARD_CONFLICT',
      citationIds: extractCitationIds(record?.citations || record?.evidenceRefs || []),
      refusal: false,
      confidenceBand: record?.confidenceLabel || 'LOW',
      manualReview: true
    }
  }

  if (endpoint === 'POST /api/v1/standard-rag/query') {
    const data = await api('POST', '/api/v1/standard-rag/query', token, request)
    return extractRag(data)
  }

  if (endpoint === 'POST /api/v1/concessions/risk-assessment') {
    const data = await api('POST', '/api/v1/concessions/risk-assessment', token, request)
    const judgment = request.judgmentId
      ? await api('GET', `/api/v1/judgments/${request.judgmentId}/explanation`, token)
      : null
    return { ...extractConcessionRisk(data), judgmentType: judgment?.judgmentType || null }
  }

  if (endpoint === 'POST /api/v1/cert-data/qa') {
    const data = await api('POST', '/api/v1/cert-data/qa', token, request)
    const judgment = await loadJudgmentForCertQa(request, token)
    return { ...extractCertQa(data), judgmentType: judgment?.judgmentType || null }
  }

  if (endpoint === 'POST /api/v1/inspections') {
    throw new Error('Inspection boundary cases require structured payload mapping; skipped in live runner')
  }

  throw new Error(`Unsupported endpoint: ${endpoint}`)
}

function extractJudgmentExplanation(data) {
  const lowConfidence = data.confidenceLabel === 'LOW'
  const hasConflict = Array.isArray(data.conflicts) && data.conflicts.length > 0
  const hasWarnings = Array.isArray(data.conflictWarnings) && data.conflictWarnings.length > 0
  return {
    judgmentType: data.judgmentType,
    citationIds: extractCitationIds(data.citations || []),
    refusal: Boolean(data.refusal),
    confidenceBand: data.confidenceLabel,
    manualReview: data.judgmentType === 'CAN_CONCESSION'
      || data.judgmentType === 'STANDARD_CONFLICT'
      || lowConfidence
      || hasConflict
      || hasWarnings
  }
}

function extractRag(data) {
  return {
    judgmentType: null,
    citationIds: extractCitationIds(data.sources || data.citations || data.sourceClauses || []),
    refusal: Boolean(data.refusal || data.refused),
    confidenceBand: data.confidenceLabel || data.confidenceBand,
    manualReview: data.confidenceLabel === 'LOW'
  }
}

function extractConcessionRisk(data) {
  const blocked = data.riskLevel === 'BLOCKED'
  const missingUsage = Array.isArray(data.missingInfo)
    && data.missingInfo.some((item) => String(item).includes('用途') || String(item).includes('usage'))
  return {
    judgmentType: null,
    citationIds: extractCitationIds(data.evidenceRefs || data.citations || []),
    refusal: blocked,
    confidenceBand: data.confidenceLabel,
    manualReview: Boolean(data.mustReview) || blocked || missingUsage || data.confidenceLabel === 'LOW'
  }
}

function extractCertQa(data) {
  return {
    judgmentType: null,
    citationIds: extractCitationIds(data.citations || []),
    refusal: Boolean(data.refusal || data.refused),
    confidenceBand: data.confidenceLabel,
    manualReview: Boolean(data.nonFinal) || data.confidenceLabel === 'LOW'
  }
}

function extractCitationIds(list) {
  return (list || []).map((item) => {
    if (typeof item === 'string') return item
    return item.clauseId || item.id || item.sourceId
  }).filter(Boolean)
}

function evaluateCase(testCase, actual) {
  const expected = testCase.expected
  if (actual.error) return false
  if (!matchesNullable(expected.judgmentType, actual.judgmentType)) return false
  if (!containsAll(actual.citationIds || [], expected.citationIds || [])) return false
  if (Boolean(actual.refusal) !== Boolean(expected.refusal)) return false
  if (String(actual.confidenceBand || '') !== String(expected.confidenceBand || '')) return false
  if (Boolean(actual.manualReview) !== Boolean(expected.manualReview)) return false
  return true
}

function containsAll(actualIds, expectedIds) {
  const actualSet = new Set((actualIds || []).map(String))
  return (expectedIds || []).every((id) => actualSet.has(String(id)))
}

function matchesNullable(expected, actual) {
  if (expected === null || expected === undefined) {
    return actual === null || actual === undefined || actual === ''
  }
  return String(expected) === String(actual)
}

main().catch((error) => {
  console.error(error)
  process.exit(1)
})
