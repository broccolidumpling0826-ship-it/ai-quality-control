/** Element Plus `v-loading` 文案：触发后端 AI/模型调用的页面统一使用 */
export const AI_LOADING_TEXT = {
  judgmentExplanation: 'AI 正在分析判定依据并生成解释，请稍候…',
  reinspectionAdvice: 'AI 正在分析判定依据并生成复检建议，请稍候…',
  rejudgmentAdvice: 'AI 正在分析判定依据并生成改判建议，请稍候…',
  concessionRisk: 'AI 正在分析让步风险并生成评估，请稍候…',
  certQa: 'AI 正在分析质保书与判定依据，请稍候…',
  standardRag: 'AI 正在检索标准条款并生成回答，请稍候…',
  analyzing: 'AI 正在分析中，请稍候…'
} as const

/** Element Plus v-loading 自定义类：spinner 与文案垂直水平居中 */
export const AI_LOADING_CLASS = 'ai-loading-center'
