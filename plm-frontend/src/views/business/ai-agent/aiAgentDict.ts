/**
 * AI Agent 模块字典映射 SSoT — 对齐 plm-backend/sql/business-ai-agent.sql
 *
 * 收拢 Agent 类型 / Provider / 状态 三组字典 + Agent 类型 emoji 图标。
 * 三组映射必须与 business-ai-agent.sql 中 biz_aiagent_type / biz_ai_provider /
 * biz_aiagent_status 字典项逐项一致;aiAgentDict.spec.ts 失败 = 此处与 SQL 字典漂移,
 * 必须按 SQL 重新校对(参照 requirementDict.ts / inceptionDict.ts / competitiveDict.ts
 * 的 SSoT 漂移锁定模式)。
 *
 * ⚠ 已知显示层小漂移(values 完全一致,仅 label 简化,非数据契约层):
 *  1. provider 'mock' 前端 label「Mock」, SQL biz_ai_provider「Mock 占位」
 *  2. provider 'dify' 前端 label「Dify」, SQL biz_ai_provider「Dify 编排」
 * 两处均为 UI 简化(value+tag 与 SQL 一致),按前端现状锁定;UED 对齐决策走 spawn 任务卡。
 *
 * 设计扩展:agentType 在 SQL biz_aiagent_type 定义有 list_class(primary/success/warning/
 * info/danger/primary),当前 index.vue 未用 tag 渲染 type;Dict 保留 tag 字段供未来 UI
 * 增强使用(不阻塞现状,且供 spec 验证与 SQL 颜色契约一致)。
 */

export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

interface DictItem {
  label: string
  tag: TagType
}

interface TypeDictItem extends DictItem {
  icon: string  // emoji,frontend-only,SQL 无对应字段
}

/** biz_aiagent_type AI Agent 类型(6 类,贯穿研发全链路;tag = SQL list_class) */
export const AGENT_TYPE: Record<string, TypeDictItem> = {
  requirement: { label: '需求分析', tag: 'primary', icon: '📋' },
  prd:         { label: 'PRD 生成', tag: 'success', icon: '📝' },
  code:        { label: '代码审查', tag: 'warning', icon: '🔍' },
  test:        { label: '测试生成', tag: 'info',    icon: '🧪' },
  release:     { label: '发布评审', tag: 'danger',  icon: '🚀' },
  ops:         { label: '运维巡检', tag: 'primary', icon: '🛠️' }
}

/** biz_ai_provider AI Provider(4 种;mock/dify label 比 SQL 简化,见文件头) */
export const AI_PROVIDER: Record<string, DictItem> = {
  mock:      { label: 'Mock',        tag: 'info' },     // ⚠ SQL「Mock 占位」
  dify:      { label: 'Dify',        tag: 'primary' },  // ⚠ SQL「Dify 编排」
  openai:    { label: 'OpenAI 兼容', tag: 'success' },
  anthropic: { label: 'Anthropic',   tag: 'warning' }
}

/** biz_aiagent_status AI Agent 状态(3 态) */
export const AGENT_STATUS: Record<string, DictItem> = {
  '00': { label: '运行中', tag: 'success' },
  '01': { label: '已停止', tag: 'info' },
  '02': { label: '错误',   tag: 'danger' }
}

const FALLBACK_TAG: TagType = 'info'
const FALLBACK_ICON = '🤖'

/** Agent 类型 label / tag / icon(SQL biz_aiagent_type) */
export const agentTypeLabel = (t?: string): string => AGENT_TYPE[t || '']?.label || t || '-'
export const agentTypeTag = (t?: string): TagType => AGENT_TYPE[t || '']?.tag || FALLBACK_TAG
export const agentIcon = (t?: string): string => AGENT_TYPE[t || '']?.icon || FALLBACK_ICON

/**
 * Provider label —— 历史宽签名 (p?: string | number),保留兼容(实际只用 string)。
 * 行为:命中 → SQL 简化 label;未命中 → 裸值;空/undefined → '-'。
 */
export const providerLabel = (p?: string | number): string =>
  AI_PROVIDER[String(p || '')]?.label || String(p || '-')
export const providerTag = (p?: string): TagType => AI_PROVIDER[p || '']?.tag || FALLBACK_TAG

/** Agent 状态 label / tag(SQL biz_aiagent_status) */
export const agentStatusLabel = (s?: string): string => AGENT_STATUS[s || '']?.label || s || '-'
export const agentStatusTag = (s?: string): TagType => AGENT_STATUS[s || '']?.tag || FALLBACK_TAG
