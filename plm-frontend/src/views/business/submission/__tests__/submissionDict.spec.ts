import { describe, it, expect } from 'vitest'
import {
  SUBMISSION_STATUS,
  statusTagFor,
  type TagType
} from '../submissionDict'

describe('submissionDict — 提测状态 biz_submission_status (5 态机;对齐 business-submission.sql)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['00', '草稿',       'info'],
    ['01', '已提交',     'warning'],   // ⚠ SQL list_class='primary', 见末段 drift §1
    ['02', '质量门禁中', 'primary'],   // ⚠ SQL list_class='warning', 见末段 drift §2
    ['03', '已通过',     'success'],
    ['04', '已退回',     'danger']
  ]
  it.each(cases)('状态 %s → label「%s」/ type「%s」', (code, label, type) => {
    expect(SUBMISSION_STATUS[code]).toEqual({ label, tag: type })
    expect(statusTagFor(code)).toEqual({ label, type })
  })
  it('共 5 码,与 SQL biz_submission_status value+label 完整对齐', () => {
    expect(Object.keys(SUBMISSION_STATUS)).toEqual(['00', '01', '02', '03', '04'])
  })
  it('回归锁: 空/undefined 状态默认落「草稿」(00) — 保持旧 statusTagFor(s||"00") 行为', () => {
    expect(statusTagFor(undefined)).toEqual({ label: '草稿', type: 'info' })
    expect(statusTagFor('')).toEqual({ label: '草稿', type: 'info' })
  })
  it('未知状态码 → 裸码 + info fallback', () => {
    expect(statusTagFor('99')).toEqual({ label: '99', type: 'info' })
  })
})

describe('⚠ 已知显示层小漂移(tag 颜色 2 处互换,锁当前前端约定,UED 评审走 spawn 任务卡)', () => {
  it('drift §1 status 01 已提交:前端 tag「warning」(黄), SQL list_class「primary」(蓝)', () => {
    expect(SUBMISSION_STATUS['01'].tag).toBe('warning')
    // SQL 真值: list_class='primary' — 见 plm-backend/sql/business-submission.sql:47
  })
  it('drift §2 status 02 质量门禁中:前端 tag「primary」(蓝), SQL list_class「warning」(黄)', () => {
    expect(SUBMISSION_STATUS['02'].tag).toBe('primary')
    // SQL 真值: list_class='warning' — 见 plm-backend/sql/business-submission.sql:48
    // 注:01 与 02 颜色「互换」,疑似前端早期约定与 SQL 不一致(语义上「质量门禁中」用蓝表示进行中更符合
    // 业内习惯)。修复需联动 SQL+UED+E2E,不擅自变更
  })
})
