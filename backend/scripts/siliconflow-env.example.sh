#!/usr/bin/env bash
#
# SiliconFlow model environment template for local backend startup.
# Chat and embedding use the same platform but separate API keys.
# Do not commit real API keys.

# Chat completions: POST ${AI_MODEL_BASE_URL}/chat/completions
export AI_MODEL_PROVIDER="SILICONFLOW"
export AI_MODEL_ENABLED=true
export AI_MODEL_BASE_URL="https://api.siliconflow.cn/v1"
export AI_MODEL_API_KEY="your-chat-api-key"
export AI_MODEL_CHAT_MODEL="deepseek-ai/DeepSeek-V4-Flash"

# SiliconFlow supports provider-specific thinking parameters on supported models.
# Keep disabled for grounded RAG/explanation output unless you explicitly need it.
export AI_MODEL_THINKING_ENABLED=false
export AI_MODEL_REASONING_EFFORT="high"
export AI_MODEL_TIMEOUT_MILLIS=60000

# Vision OCR for scanned standard source images: POST ${AI_MODEL_BASE_URL}/chat/completions
export AI_VISION_ENABLED=true
export AI_VISION_MODEL="deepseek-ai/DeepSeek-OCR"
export AI_VISION_OCR_PROMPT=$'<image>\n<|grounding|>Convert the document to markdown.'
export AI_VISION_IMAGE_DETAIL="high"
export AI_VISION_TIMEOUT_MILLIS=60000

# Embeddings: POST ${EMBEDDING_BASE_URL}/embeddings
export EMBEDDING_PROVIDER="SILICONFLOW"
export EMBEDDING_ENABLED=true
export EMBEDDING_BASE_URL="https://api.siliconflow.cn/v1"
export EMBEDDING_API_KEY="your-embedding-api-key"
export EMBEDDING_MODEL="BAAI/bge-m3"
export EMBEDDING_TIMEOUT_MILLIS=15000

# Elasticsearch vector store (RAG retrieval)
export ES_VECTOR_ENABLED=true
export ES_HOST="http://localhost:9200"
export ES_USERNAME="elastic"
export ES_PASSWORD="your-es-password"
export ES_STANDARD_INDEX="quality-standard-clauses"
export ES_TIMEOUT_MILLIS=5000
