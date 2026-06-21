#!/usr/bin/env node
/**
 * Index P0 prepared standard clauses into Elasticsearch via backend API.
 *
 * Usage (from backend/):
 *   node scripts/index-p0-clauses.mjs
 *
 * Env:
 *   API_BASE_URL  default http://localhost:8080
 *   API_USER_NO   default admin
 *   API_PASSWORD  default Admin123456
 */
const BASE = process.env.API_BASE_URL || 'http://localhost:8080'
const USER = process.env.API_USER_NO || 'admin'
const PASS = process.env.API_PASSWORD || 'Admin123456'

const forceReindex = process.argv.includes('--force')

async function main() {
  const token = await login()
  const groups = [
    'P0_STANDARD',
    'P0_CUSTOMER',
    'P0_CONFLICT',
    'P0_CASE'
  ]
  let totalIndexed = 0
  for (const relevanceGroup of groups) {
    const result = await indexGroup(token, relevanceGroup)
    console.log(`${relevanceGroup}: success=${result.success}, indexed=${result.indexedCount || 0}, failed=${(result.failedClauseIds || []).length}`)
    totalIndexed += result.indexedCount || 0
  }
  const all = await indexGroup(token, null)
  console.log(`ALL${forceReindex ? ' (force)' : ''}: success=${all.success}, indexed=${all.indexedCount || 0}`)
  totalIndexed += all.indexedCount || 0
  console.log(`Done. Total indexed in this run: ${totalIndexed}`)
}

async function login() {
  const res = await fetch(`${BASE}/api/v1/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userNo: USER, password: PASS })
  })
  const body = await res.json()
  if (!body.success) {
    throw new Error(`Login failed: ${body.message}`)
  }
  return body.data.token
}

async function indexGroup(token, relevanceGroup) {
  const payload = { onlyPending: !forceReindex }
  if (relevanceGroup) {
    payload.relevanceGroup = relevanceGroup
  }
  const res = await fetch(`${BASE}/api/v1/standard-documents/clauses/index-prepared`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(payload)
  })
  const body = await res.json()
  if (!body.success) {
    throw new Error(body.message || `Index failed for ${relevanceGroup || 'ALL'}`)
  }
  return body.data
}

main().catch((error) => {
  console.error(error)
  process.exit(1)
})
