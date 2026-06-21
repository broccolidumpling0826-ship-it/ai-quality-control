#!/usr/bin/env bash
#
# DeepSeek environment template for local backend startup.
# Copy the export commands into your shell before running:
#   mvn spring-boot:run -Dspring.profiles.active=dev
#
# Do not commit real API keys.

export DEEPSEEK_ENABLED=true
export DEEPSEEK_API_KEY="sk-your-deepseek-api-key"

# Official OpenAI-compatible base URL. The backend appends /chat/completions.
export DEEPSEEK_BASE_URL="https://api.deepseek.com"

# Recommended current chat models:
# - deepseek-v4-flash: lower latency/cost, suitable for standard RAG wording and explanations.
# - deepseek-v4-pro: higher quality, use when demo quality is more important than latency/cost.
export DEEPSEEK_MODEL="deepseek-v4-flash"

# Disable thinking by default for grounded quality explanations so output is shorter and easier to cite.
# Set true only when you intentionally want reasoning-mode responses.
export DEEPSEEK_THINKING_ENABLED=false
export DEEPSEEK_REASONING_EFFORT="high"

export DEEPSEEK_TIMEOUT_MILLIS=15000

# The project keeps an embedding model slot for future vectorization.
# Confirm the provider and output dimension before enabling true ES dense_vector retrieval.
export DEEPSEEK_EMBEDDING_MODEL="text-embedding-v1"
