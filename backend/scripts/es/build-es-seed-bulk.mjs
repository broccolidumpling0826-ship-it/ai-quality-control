#!/usr/bin/env node
/**
 * 将 p0-clauses-seed.json 转为 Elasticsearch bulk NDJSON。
 * Usage: node backend/scripts/es/build-es-seed-bulk.mjs
 */
import { readFileSync, writeFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const indexName = process.env.ES_STANDARD_INDEX || 'quality-standard-clauses';
const seedPath = resolve(__dirname, 'p0-clauses-seed.json');
const outPath = resolve(__dirname, 'init-es-seed-data-only.bulk.ndjson');

const docs = JSON.parse(readFileSync(seedPath, 'utf8'));
const lines = [];
for (const doc of docs) {
  const id = doc._id || doc.clauseId;
  const body = { ...doc };
  delete body._id;
  lines.push(JSON.stringify({ index: { _index: indexName, _id: id } }));
  lines.push(JSON.stringify(body));
}
writeFileSync(outPath, `${lines.join('\n')}\n`, 'utf8');
console.log(`Wrote ${docs.length} docs -> ${outPath}`);
