#!/usr/bin/env bash
#
# Embedding service environment template for local backend startup.
# This may use a separate API key from chat even when served by the same platform.
#
# Expected HTTP contract:
#   POST ${EMBEDDING_BASE_URL}/embeddings
#   {
#     "model": "${EMBEDDING_MODEL}",
#     "input": ["text"]
#   }
#
# The response must be OpenAI-compatible:
#   {
#     "data": [
#       { "index": 0, "embedding": [0.1, 0.2, ...] }
#     ],
#     "model": "..."
#   }

export EMBEDDING_ENABLED=true
export EMBEDDING_BASE_URL="https://api.siliconflow.cn/v1"

export EMBEDDING_API_KEY="sk-your-siliconflow-embedding-api-key"

# Default SiliconFlow Chinese/multilingual embedding model. Its vector dimension is 1024.
# The ES dense_vector dims must match this model.
export EMBEDDING_MODEL="BAAI/bge-m3"
export EMBEDDING_PROVIDER="SILICONFLOW"
export EMBEDDING_TIMEOUT_MILLIS=15000
