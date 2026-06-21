#!/usr/bin/env bash
#
# SiliconFlow model environment template for local backend startup.
# Chat and embedding use the same platform but separate API keys.
# Do not commit real API keys.

# Chat completions: POST ${AI_MODEL_BASE_URL}/chat/completions
export AI_MODEL_PROVIDER="SILICONFLOW"
export AI_MODEL_ENABLED=true
export AI_MODEL_BASE_URL="https://api.siliconflow.cn/v1"
export AI_MODEL_API_KEY="sk-your-siliconflow-chat-api-key"
export AI_MODEL_CHAT_MODEL="Pro/zai-org/GLM-4.7"

# SiliconFlow supports provider-specific thinking parameters on supported models.
# Keep disabled for grounded RAG/explanation output unless you explicitly need it.
export AI_MODEL_THINKING_ENABLED=false
export AI_MODEL_REASONING_EFFORT="high"
export AI_MODEL_TIMEOUT_MILLIS=15000

# Embeddings: POST ${EMBEDDING_BASE_URL}/embeddings
export EMBEDDING_PROVIDER="SILICONFLOW"
export EMBEDDING_ENABLED=true
export EMBEDDING_BASE_URL="https://api.siliconflow.cn/v1"
export EMBEDDING_API_KEY="sk-your-siliconflow-embedding-api-key"
export EMBEDDING_MODEL="BAAI/bge-m3"
export EMBEDDING_TIMEOUT_MILLIS=15000
