#!/usr/bin/env node

import { mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import crypto from 'node:crypto';

const __dirname = dirname(fileURLToPath(import.meta.url));
const projectRoot = resolve(__dirname, '..');
const defaultDocPath = resolve(__dirname, 'demo-documents/semantic-rag-smoke-q235b.md');
const appConfigPath = resolve(projectRoot, 'src/main/resources/application.yml');

const docPath = resolve(process.argv[2] || defaultDocPath);
const dryRun = process.argv.includes('--dry-run');

const appConfigText = safeRead(appConfigPath);

const config = {
  chatBaseUrl: envOrYamlDefault('AI_MODEL_BASE_URL', 'https://api.siliconflow.cn/v1'),
  chatApiKey: envOrYamlDefault('AI_MODEL_API_KEY', ''),
  chatModel: envOrYamlDefault('AI_MODEL_CHAT_MODEL', 'deepseek-ai/DeepSeek-V4-Flash'),
  embeddingBaseUrl: envOrYamlDefault('EMBEDDING_BASE_URL', 'https://api.siliconflow.cn/v1'),
  embeddingApiKey: envOrYamlDefault('EMBEDDING_API_KEY', ''),
  embeddingModel: envOrYamlDefault('EMBEDDING_MODEL', 'BAAI/bge-m3'),
  esHost: envOrYamlDefault('ES_HOST', 'http://localhost:9200'),
  esUsername: envOrYamlDefault('ES_USERNAME', 'elastic'),
  esPassword: envOrYamlDefault('ES_PASSWORD', ''),
  esIndex: envOrYamlDefault('ES_STANDARD_INDEX', 'quality-standard-clauses'),
};

main().catch((error) => {
  console.error(error?.stack || error);
  process.exit(1);
});

async function main() {
  assertConfig();
  logStep(`读取Markdown文档: ${docPath}`);
  const markdown = readFileSync(docPath, 'utf8');
  const { metadata, body } = parseFrontMatter(markdown);
  logStep(`调用Chat模型做语义切割: ${config.chatModel}`);
  const chunks = await semanticChunk(metadata, body);
  if (!chunks.length) {
    throw new Error('Chat model returned no semantic chunks.');
  }
  logStep(`语义切割完成: ${chunks.length} 个条款块`);
  const embeddingInputs = chunks.map((chunk) => [
    metadata.standardCode,
    metadata.standardName,
    chunk.clauseNo,
    chunk.title,
    chunk.retrievalKeywords,
    chunk.paragraphText,
  ].filter(Boolean).join('\n'));
  logStep(`调用Embedding模型生成向量: ${config.embeddingModel}`);
  const embeddings = await embedTexts(embeddingInputs);
  logStep(`向量生成完成: ${embeddings.length} 条，维度 ${embeddings[0]?.length || 0}`);
  const docs = chunks.map((chunk, index) => toEsDocument(metadata, chunk, embeddings[index], index));

  mkdirSync(resolve(__dirname, 'generated'), { recursive: true });
  const generatedJson = resolve(__dirname, 'generated/semantic-rag-smoke-docs.json');
  writeFileSync(generatedJson, JSON.stringify(docs, null, 2), 'utf8');
  logStep(`已生成本地调试JSON: ${generatedJson}`);

  if (!dryRun) {
    logStep(`检查ES索引: ${config.esHost}/${config.esIndex}`);
    await ensureIndexExists();
    logStep(`写入ES: ${docs.length} 条文档`);
    await bulkIndex(docs);
    logStep('ES写入完成');
  } else {
    logStep('dry-run模式：跳过ES写入');
  }

  console.log(JSON.stringify({
    dryRun,
    document: docPath,
    generatedJson,
    index: config.esIndex,
    chunkCount: docs.length,
    embeddingModel: config.embeddingModel,
    chatModel: config.chatModel,
    embeddingDimensions: docs[0]?.embedding?.length || 0,
    sampleClauseIds: docs.slice(0, 3).map((doc) => doc.clauseId),
  }, null, 2));
}

async function semanticChunk(metadata, body) {
  const prompt = [
    '你是钢铁质量标准文档语义切割助手。',
    '请将输入Markdown按完整业务语义切割成条款块，不要逐句碎切。',
    '每个块必须能单独作为RAG来源段落引用。',
    '只输出JSON数组，不要输出Markdown，不要解释。',
    '数组元素字段：clauseNo,title,paragraphText,indicatorCode,indicatorName,retrievalKeywords。',
    'clauseNo使用原文标题编号，例如1、2、3；若无法识别则用S1、S2。',
    'paragraphText必须保留原文关键限值、条件和禁止项。',
    '',
    '文档元数据：',
    JSON.stringify(metadata, null, 2),
    '',
    'Markdown正文：',
    body,
  ].join('\n');

  const response = await postJson(`${trimSlash(config.chatBaseUrl)}/chat/completions`, config.chatApiKey, {
    model: config.chatModel,
    messages: [
      {
        role: 'system',
        content: '你只负责标准文档语义切割。输出必须是可解析JSON数组。',
      },
      {
        role: 'user',
        content: prompt,
      },
    ],
    temperature: 0,
    max_tokens: 1800,
    enable_thinking: false,
  });
  const content = response?.choices?.[0]?.message?.content;
  const parsed = parseJsonArray(content);
  return parsed.map((item, index) => ({
    clauseNo: stringOrDefault(item.clauseNo, `S${index + 1}`),
    title: stringOrDefault(item.title, `语义条款${index + 1}`),
    paragraphText: stringOrDefault(item.paragraphText, ''),
    indicatorCode: emptyToNull(item.indicatorCode),
    indicatorName: emptyToNull(item.indicatorName),
    retrievalKeywords: stringOrDefault(item.retrievalKeywords, ''),
  })).filter((item) => item.paragraphText.length > 0);
}

async function embedTexts(inputTexts) {
  const response = await postJson(`${trimSlash(config.embeddingBaseUrl)}/embeddings`, config.embeddingApiKey, {
    model: config.embeddingModel,
    input: inputTexts,
  });
  const data = Array.isArray(response?.data) ? response.data : [];
  const vectors = [];
  for (const item of data) {
    const index = Number.isInteger(item.index) ? item.index : vectors.length;
    vectors[index] = item.embedding;
  }
  if (vectors.length !== inputTexts.length || vectors.some((v) => !Array.isArray(v) || !v.length)) {
    throw new Error(`Embedding count mismatch. input=${inputTexts.length}, output=${vectors.length}`);
  }
  return vectors;
}

async function ensureIndexExists() {
  const exists = await esFetch(`/${encodeURIComponent(config.esIndex)}`, { method: 'HEAD' });
  if (exists.status === 404) {
    throw new Error(`ES index ${config.esIndex} does not exist. Run backend/scripts/init-es-standard-clauses.devtools first.`);
  }
  if (!exists.ok) {
    throw new Error(`Failed to check ES index. status=${exists.status}, body=${await exists.text()}`);
  }
}

async function bulkIndex(docs) {
  const lines = [];
  for (const doc of docs) {
    lines.push(JSON.stringify({ index: { _index: config.esIndex, _id: doc.clauseId } }));
    lines.push(JSON.stringify(doc));
  }
  const body = `${lines.join('\n')}\n`;
  const response = await esFetch('/_bulk', {
    method: 'POST',
    headers: { 'content-type': 'application/x-ndjson' },
    body,
  });
  const text = await response.text();
  if (!response.ok) {
    throw new Error(`ES bulk index failed. status=${response.status}, body=${text}`);
  }
  const result = JSON.parse(text);
  if (result.errors) {
    const failed = result.items
      .filter((item) => item.index?.error)
      .slice(0, 5)
      .map((item) => item.index);
    throw new Error(`ES bulk index has item errors: ${JSON.stringify(failed, null, 2)}`);
  }
}

function toEsDocument(metadata, chunk, embedding, index) {
  const stableKey = `${metadata.documentId}:${chunk.clauseNo}:${index + 1}`;
  return {
    clauseId: `smoke_clause_${hash(stableKey).slice(0, 16)}`,
    documentId: metadata.documentId,
    clauseKey: stableKey,
    clauseNo: chunk.clauseNo,
    pageNo: index + 1,
    paragraphText: chunk.paragraphText,
    sourceType: metadata.standardType || metadata.documentType || 'STANDARD',
    standardType: metadata.standardType || null,
    standardCode: metadata.standardCode || metadata.documentCode || null,
    standardName: metadata.standardName || metadata.documentName || null,
    versionNo: metadata.versionNo || null,
    customerId: metadata.customerId || null,
    variety: metadata.variety || null,
    grade: metadata.grade || null,
    specRange: metadata.specRange || null,
    usageScope: metadata.usageScope || null,
    indicatorId: null,
    indicatorCode: chunk.indicatorCode || null,
    indicatorName: chunk.indicatorName || null,
    effectiveDate: metadata.effectiveDate || null,
    expiryDate: metadata.expiryDate || null,
    retrievalKeywords: chunk.retrievalKeywords || `${metadata.variety || ''} ${metadata.grade || ''} ${chunk.title}`,
    embedding,
    metadata: {
      ingestion: 'semantic-rag-smoke',
      sourceFile: docPath,
      chunkTitle: chunk.title,
      chatModel: config.chatModel,
      embeddingModel: config.embeddingModel,
    },
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  };
}

async function postJson(url, apiKey, body) {
  const response = await fetchWithTimeout(url, {
    method: 'POST',
    headers: {
      'content-type': 'application/json',
      authorization: `Bearer ${apiKey}`,
    },
    body: JSON.stringify(body),
  }, 120000);
  const text = await response.text();
  if (!response.ok) {
    throw new Error(`HTTP ${response.status} for ${url}: ${text}`);
  }
  return JSON.parse(text);
}

function esFetch(path, init = {}) {
  const headers = new Headers(init.headers || {});
  if (config.esUsername && config.esPassword) {
    headers.set('authorization', `Basic ${Buffer.from(`${config.esUsername}:${config.esPassword}`).toString('base64')}`);
  }
  return fetchWithTimeout(`${trimSlash(config.esHost)}${path}`, { ...init, headers }, 30000);
}

async function fetchWithTimeout(url, init, timeoutMs) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeoutMs);
  try {
    return await fetch(url, { ...init, signal: controller.signal });
  } finally {
    clearTimeout(timer);
  }
}

function parseFrontMatter(markdown) {
  if (!markdown.startsWith('---')) {
    return { metadata: {}, body: markdown };
  }
  const end = markdown.indexOf('\n---', 3);
  if (end < 0) {
    return { metadata: {}, body: markdown };
  }
  const rawMeta = markdown.slice(3, end).trim();
  const body = markdown.slice(end + 4).trim();
  const metadata = {};
  for (const line of rawMeta.split(/\r?\n/)) {
    const idx = line.indexOf(':');
    if (idx < 0) continue;
    const key = line.slice(0, idx).trim();
    const value = line.slice(idx + 1).trim();
    metadata[key] = value || null;
  }
  return { metadata, body };
}

function parseJsonArray(content) {
  if (!content) {
    throw new Error('Chat model returned empty content.');
  }
  const trimmed = content.trim();
  const fenced = trimmed.match(/```(?:json)?\s*([\s\S]*?)```/i);
  const jsonText = fenced ? fenced[1].trim() : trimmed.slice(trimmed.indexOf('['), trimmed.lastIndexOf(']') + 1);
  const parsed = JSON.parse(jsonText);
  if (!Array.isArray(parsed)) {
    throw new Error('Chat model output is not a JSON array.');
  }
  return parsed;
}

function envOrYamlDefault(name, fallback) {
  if (process.env[name]) {
    return process.env[name];
  }
  const escaped = name.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const match = appConfigText.match(new RegExp(`\\$\\{${escaped}:([^}]+)\\}`));
  return match?.[1] || fallback;
}

function assertConfig() {
  if (!config.chatApiKey) {
    throw new Error('Missing AI_MODEL_API_KEY.');
  }
  if (!config.embeddingApiKey) {
    throw new Error('Missing EMBEDDING_API_KEY.');
  }
}

function safeRead(path) {
  try {
    return readFileSync(path, 'utf8');
  } catch {
    return '';
  }
}

function trimSlash(value) {
  return String(value || '').replace(/\/+$/, '');
}

function hash(value) {
  return crypto.createHash('sha1').update(value).digest('hex');
}

function stringOrDefault(value, fallback) {
  return typeof value === 'string' && value.trim() ? value.trim() : fallback;
}

function emptyToNull(value) {
  return typeof value === 'string' && value.trim() ? value.trim() : null;
}

function logStep(message) {
  console.error(`[semantic-rag-smoke] ${message}`);
}
