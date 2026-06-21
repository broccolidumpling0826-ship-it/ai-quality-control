#!/usr/bin/env bash
#
# Provider-neutral chat model environment template for local backend startup.
# The backend appends /chat/completions to AI_MODEL_BASE_URL.
#
# Do not commit real API keys.

export AI_MODEL_PROVIDER="SILICONFLOW"
export AI_MODEL_ENABLED=true
export AI_MODEL_BASE_URL="https://api.siliconflow.cn/v1"
export AI_MODEL_API_KEY="sk-your-model-api-key"
export AI_MODEL_CHAT_MODEL="Pro/zai-org/GLM-4.7"

# Provider-specific optional knobs. Keep disabled unless the selected provider
# supports them and the current feature needs reasoning-mode output.
export AI_MODEL_THINKING_ENABLED=false
export AI_MODEL_REASONING_EFFORT="high"

export AI_MODEL_TIMEOUT_MILLIS=15000
