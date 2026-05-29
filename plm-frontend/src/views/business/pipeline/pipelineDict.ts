/**
 * CI/CD 流水线模块字典映射 SSoT — 对齐 plm-backend/sql/business-pipeline.sql
 *
 * 收拢 CI 工具 / 触发方式 / 上次执行结果 3 组字典。
 *
 * ⚠ 本模块发现**多处跨层契约漂移**(同 dora 量级,需 api-contract 评审):
 *
 *  1. CI 工具(biz_pipeline_tool)values 错位 3/4:
 *     前端 'gitlab_ci'      / 'github_actions' / 'drone'
 *     SQL  'gitlab'         / 'github'         / 'gitea'
 *     (jenkins 一致)
 *
 *  2. CI 工具 label 简化:前端「GitLab / GHA」, SQL「GitLab CI / GitHub Actions」
 *
 *  3. 触发方式(biz_pipeline_trigger):
 *     前端 5 项 (push / pr / tag / cron / manual)
 *     SQL  4 项 (manual / push / cron / tag),无 'pr'
 *
 *  4. 上次执行结果(biz_pipeline_result):
 *     前端 4 项 (success / running / failed / never)
 *     SQL  4 项 (success / failed / running / skipped)
 *     —— 前端有 'never' (未执行) SQL 无,SQL 有 'skipped' 前端无
 *
 *  5. lastRunStatus 'running' tag 颜色:前端 'primary' (蓝), SQL list_class 'warning' (黄)
 *
 *  6. biz_pipeline_status(00 启用 / 01 停用):SQL 定义但 index.vue 未渲染
 *     (页面表格无"状态"列;DDL/Domain 有 status 字段但前端不展示)
 *
 * 全部锁当前前端约定 + spec 显式记录;修复需跨层(前端+后端+E2E+存量数据),
 * 走 api-contract 评审 + spawn 任务卡。pipelineDict.spec.ts ⚠ drift 段详尽锁定。
 */

export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

interface LabelItem {
  label: string
}

interface FullItem extends LabelItem {
  tag: TagType
}

/**
 * CI 工具(前端键名,⚠ 见文件头 drift §1 + §2)
 * — value 与 SQL biz_pipeline_tool 3/4 错位
 * — label 比 SQL 简化
 */
export const CI_TOOL: Record<string, LabelItem> = {
  jenkins:        { label: 'Jenkins' },     // ✓ value 对齐;SQL label 同
  gitlab_ci:      { label: 'GitLab' },      // ⚠ SQL value='gitlab',  label='GitLab CI'
  github_actions: { label: 'GHA' },         // ⚠ SQL value='github',  label='GitHub Actions'
  drone:          { label: 'Drone' }        // ⚠ SQL value='gitea',   label='Gitea Actions'
}

/**
 * 触发方式(前端键名,⚠ 见文件头 drift §3)
 * — 前端多一项 'pr' (Merge Request),SQL 无
 */
export const TRIGGER_TYPE: Record<string, LabelItem> = {
  push:   { label: 'Push' },
  pr:     { label: 'PR' },        // ⚠ SQL 无 'pr' 选项
  tag:    { label: 'Tag' },
  cron:   { label: '定时' },       // ⚠ SQL label='定时触发'
  manual: { label: '手动' }        // ⚠ SQL label='手动触发'
}

/**
 * 上次执行结果(⚠ 见文件头 drift §4 + §5)
 * — 前端有 'never' SQL 无;SQL 有 'skipped' 前端无
 * — 'running' tag 前端 primary / SQL warning
 */
export const LAST_RUN_STATUS: Record<string, FullItem> = {
  success: { label: '成功',   tag: 'success' },
  running: { label: '运行中', tag: 'primary' },   // ⚠ SQL list_class='warning'
  failed:  { label: '失败',   tag: 'danger' },
  never:   { label: '未执行', tag: 'info' }       // ⚠ SQL 无 'never' (但有 'skipped')
}

const FALLBACK_TAG: TagType = 'info'

/** CI 工具 label(未命中返回裸值) */
export const ciToolLabel = (v?: string): string => CI_TOOL[v || '']?.label || v || '-'

/** 触发方式 label(未命中返回裸值) */
export const triggerLabel = (v?: string): string => TRIGGER_TYPE[v || '']?.label || v || '-'

/**
 * 上次执行结果 label(注意:旧实现 fallback 是 '-' 而非裸值)
 */
export const lastRunLabel = (v?: string): string => LAST_RUN_STATUS[v || '']?.label || '-'

/** 上次执行结果 tag(fallback 'info') */
export const lastRunTag = (v?: string): TagType => LAST_RUN_STATUS[v || '']?.tag || FALLBACK_TAG
