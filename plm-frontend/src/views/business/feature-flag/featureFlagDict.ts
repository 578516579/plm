/**
 * Feature Flag 模块字典映射 SSoT — 对齐 plm-backend/sql/business-feature-flag.sql
 *
 * 2 组字典:环境(env) + 灰度模式(rolloutMode/strategy)。
 *
 * ⚠ 已知契约漂移(featureFlagDict.spec.ts ⚠ drift 段详尽锁定):
 *  §1 env value 错位:前端 'dev' vs SQL biz_ff_env 'test'
 *  §2 env label 风格:前端「开发/预发/生产」(中文)vs SQL「TEST/STAGING/PROD」(英文大写)
 *  §3 env tag 'dev/test' 颜色:前端 info,SQL list_class primary
 *  §4 rolloutMode all_off tag:前端 danger(强调"关闭"危险),SQL list_class info
 *  §5 mode label 简化:前端「全量/灰度/关闭」, SQL「全量开启 (100%)/灰度 (1-99%)/关闭 (0%)」
 *  §6 biz_ff_status(00开启/01关闭)SQL 定义但前端 index.vue 用 rolloutMode 渲染未用 status
 */

export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

interface DictItem {
  label: string
  tag: TagType
}

/**
 * 环境(⚠ value+label+tag 均与 SQL 漂移,见文件头 §1-§3)
 * 前端 3 项:dev / staging / prod
 * SQL  3 项:test / staging / prod
 */
export const FF_ENV: Record<string, DictItem> = {
  dev:     { label: '开发', tag: 'info' },     // ⚠ SQL value='test', label='TEST', tag='primary'
  staging: { label: '预发', tag: 'warning' },  // ⚠ SQL label='STAGING'
  prod:    { label: '生产', tag: 'danger' }    // ⚠ SQL label='PROD'
}

/**
 * 灰度模式(⚠ tag+label 部分与 SQL 漂移,见文件头 §4+§5)
 * 3 项:all_on / all_off / canary (values 与 SQL biz_ff_strategy 一致)
 */
export const FF_MODE: Record<string, DictItem> = {
  all_on:  { label: '全量', tag: 'success' },   // SQL label='全量开启 (100%)' (前端简化)
  all_off: { label: '关闭', tag: 'danger' },    // ⚠ SQL label='关闭 (0%)', list_class='info' (前端 danger 更醒目)
  canary:  { label: '灰度', tag: 'warning' }    // SQL label='灰度 (1-99%)' (前端简化)
}

const FALLBACK_TAG: TagType = 'info'

/** 环境 label */
export const envLabel = (v?: string): string => FF_ENV[v || '']?.label || v || '-'

/** 环境 tag */
export const envTag = (v?: string): TagType => FF_ENV[v || '']?.tag || FALLBACK_TAG

/** 灰度模式 label */
export const modeLabel = (v?: string): string => FF_MODE[v || '']?.label || v || '-'

/** 灰度模式 tag */
export const modeTag = (v?: string): TagType => FF_MODE[v || '']?.tag || FALLBACK_TAG
