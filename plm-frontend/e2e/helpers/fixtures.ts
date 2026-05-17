/**
 * E2E 测试数据夹具（fixtures）
 *
 * 设计原则:
 * 1. **每个测试自包含**: 用 timestamp 后缀避免数据冲突;测试末尾清理自己的数据
 * 2. **覆盖编码边界**: 中文 / 全角符号 / 希腊字母 / emoji / 半角 ASCII 混合
 * 3. **可重入**: 多次跑同一测试不互相干扰
 *
 * 关联文档: 04-测试/测试用例库/E2E-测试数据.md
 */

/** 测试运行 ID,用于隔离并发跑;格式 'YYYYMMDD-HHMMSS-rand4' */
export const RUN_ID = generateRunId()

function generateRunId(): string {
  const d = new Date()
  const pad = (n: number) => String(n).padStart(2, '0')
  const ts = `${d.getFullYear()}${pad(d.getMonth() + 1)}${pad(d.getDate())}-${pad(d.getHours())}${pad(d.getMinutes())}${pad(d.getSeconds())}`
  const rand = Math.random().toString(36).slice(2, 6)
  return `${ts}-${rand}`
}

/** 编码 round-trip 校验用样本 — 必须能完整往返不损坏 */
export const ENCODING_SAMPLES = {
  /** 标准中文 (E7BC96 E7A081 = 编码) */
  cn: '编码自检测试',
  /** 中文 + 全角标点 */
  cnPunct: '需求标题：测试，结束。',
  /** 希腊字母 (CEB1 CEB2 CEB3 = αβγ) */
  greek: 'αβγδε',
  /** emoji */
  emoji: '🚀✨🎯',
  /** ASCII */
  ascii: 'Hello World',
  /** 混合 */
  mixed: `中文-${'αβγ'}-${'Hello'}-${'🎯'}`,
  /** SQL 注入安全字符 (不会被引擎当 SQL 注入) */
  edge: `Test"O'Brien & <tag>`
}

/** 期望的 UTF-8 HEX 前缀（前 6 字节,3 个汉字） */
export const ENCODING_HEX_PREFIX = {
  cn: 'E7BC96E7A081E887AA', // 编码自
  cnPunct: 'E99C80E6B182E6A087', // 需求标
  arch: 'E9878DE69E84' // 重构
}

/** 编码替换符（U+FFFD）的 UTF-8 字节 — 出现就代表乱码 */
export const MOJIBAKE_HEX = 'EFBFBD'

/** 业务模块测试数据生成器 (v2 PRD-align,引用 PRD-MAPPING §2 commit 20b5bb6) */
export function makeProjectData(suffix?: string) {
  const tag = suffix || RUN_ID
  return {
    projectName: `E2E 测试项目-${tag}`,
    businessLine: 'precision_agri',     // PRD §F1.2 必填,4 值之一
    projectType: 'rnd',                 // PRD §F1.2 可选
    priority: 'P2',                     // PRD §F1.2 可选
    managerUserId: 1,
    startDate: '2026-05-16',
    endDate: '2026-12-31',
    progress: 0,
    health: 'green',
    description: `E2E 自动化测试-${tag} αβγ`
  }
}

export function makeRequirementData(projectId: number, suffix?: string) {
  const tag = suffix || RUN_ID
  return {
    projectId,
    title: `E2E 需求-${tag}`,
    description: `自动测试需求描述 ${tag}`,
    source: '01',
    priority: '01',
    aiValue: 'M'   // 中价值,可选字段,PRD-MAPPING §2 Requirement
  }
}

export function makeSprintData(projectId: number, suffix?: string) {
  const tag = suffix || RUN_ID
  return {
    projectId,
    name: `E2E Sprint-${tag}`,
    goal: `自动化测试目标 ${tag}`,
    plannedStartDate: '2026-05-16',
    plannedEndDate: '2026-05-29'
  }
}

export function makeTaskData(projectId: number, sprintId?: number, requirementId?: number, suffix?: string) {
  const tag = suffix || RUN_ID
  return {
    projectId,
    sprintId,
    requirementId,
    title: `E2E 任务-${tag}`,
    description: `自动测试 ${tag}`,
    priority: '02',
    assigneeUserId: 1,
    estimatedHours: 2.0
  }
}

/** Project 总状态机测试用例 (v2 PRD-align,4 态 + 两位数,PRD-MAPPING §3) */
export const PROJECT_STATUS_TRANSITIONS = {
  legal: [
    { from: '00', to: '01', name: '进行中→暂停' },
    { from: '01', to: '00', name: '暂停→进行中(反向边)' },
    { from: '00', to: '02', name: '进行中→已完成' },
    { from: '00', to: '03', name: '进行中→已取消' },
    { from: '01', to: '03', name: '暂停→已取消' }
  ],
  illegal: [
    { from: '01', to: '02', name: '暂停→已完成(需先恢复)' },
    { from: '02', to: '00', name: '已完成→进行中(终态保护)' },
    { from: '03', to: '00', name: '已取消→进行中(终态保护)' }
  ]
}

/** Project 交付阶段状态机测试用例 (仅 status=00 时演进) */
export const PROJECT_PHASE_TRANSITIONS = {
  legal: [
    { from: '00', to: '01', name: '规划→研发' },
    { from: '01', to: '00', name: '研发→规划(反向边)' },
    { from: '01', to: '02', name: '研发→测试' },
    { from: '02', to: '01', name: '测试→研发(反向边)' },
    { from: '02', to: '03', name: '测试→验收' },
    { from: '03', to: '02', name: '验收→测试(反向边)' }
  ],
  illegal: [
    { from: '00', to: '02', name: '规划→测试(跨级)' },
    { from: '00', to: '03', name: '规划→验收(跨级)' },
    { from: '03', to: '00', name: '验收→规划(跨级反向)' }
  ]
}

export const REQUIREMENT_STATUS_TRANSITIONS = {
  legal: [
    { from: '00', to: '01', name: '待评审→开发中' },
    { from: '01', to: '00', name: '开发中→待评审（打回）' },
    { from: '01', to: '02', name: '开发中→已完成' },
    { from: '00', to: '03', name: '待评审→已取消' }
  ],
  illegal: [
    { from: '00', to: '02', name: '待评审→已完成（跨级）' },
    { from: '02', to: '01', name: '已完成→开发中（终态）' }
  ]
}

export const SPRINT_STATUS_TRANSITIONS = {
  legal: [
    { from: '00', to: '01', name: '计划中→进行中' },
    { from: '01', to: '02', name: '进行中→已完成' },
    { from: '01', to: '03', name: '进行中→已取消' }
  ],
  illegal: [
    { from: '00', to: '02', name: '计划中→已完成（跨级）' },
    { from: '02', to: '01', name: '已完成→进行中（终态）' }
  ]
}

export const TASK_STATUS_TRANSITIONS = {
  legal: [
    { from: '00', to: '01', name: '待开发→开发中' },
    { from: '01', to: '02', name: '开发中→代码评审' },
    { from: '02', to: '01', name: '代码评审→开发中（反向边·评审打回）' },
    { from: '02', to: '03', name: '代码评审→测试中' },
    { from: '03', to: '02', name: '测试中→代码评审（反向边·测试打回）' },
    { from: '03', to: '04', name: '测试中→已完成' }
  ],
  illegal: [
    { from: '00', to: '02', name: '待开发→代码评审（跨级）' },
    { from: '00', to: '03', name: '待开发→测试中（跨级）' },
    { from: '04', to: '01', name: '已完成→开发中（终态）' }
  ]
}

/** 业务硬规则码 */
export const ERROR_CODES = {
  ENCODING_OK: 200,
  STATUS_VIOLATION: 601,
  REQUIRED_FIELD: 602,
  FIELD_FORMAT: 604,
  NO_UNIQUE: 701,
  FK_NOT_EXISTS: 702,
  SPRINT_SINGLE_ACTIVE: 703, // 业务硬规则 703
  SPRINT_HAS_TASKS: 704
}
