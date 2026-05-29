import { describe, it, expect } from 'vitest'
import {
  DOCUMENT_STATUS, DOC_TYPE,
  statusTagFor, docTypeLabel, docTypeTag,
  type TagType
} from '../documentDict'

describe('documentDict — 文档状态 biz_doc_status (4 态机)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['00', '草稿',   'info'],
    ['01', '待评审', 'warning'],
    ['02', '已发布', 'success'],
    ['03', '已归档', 'danger']    // ⚠ SQL list_class 空,见末段 drift §2
  ]
  it.each(cases)('状态 %s → label「%s」/ type「%s」', (code, label, type) => {
    expect(DOCUMENT_STATUS[code]).toEqual({ label, tag: type })
    expect(statusTagFor(code)).toEqual({ label, type })
  })
  it('共 4 码,values+labels 与 SQL 完整对齐', () => {
    expect(Object.keys(DOCUMENT_STATUS)).toEqual(['00', '01', '02', '03'])
  })
  it('未知/空 → "-" + info', () => {
    expect(statusTagFor(undefined)).toEqual({ label: '-', type: 'info' })
    expect(statusTagFor('99')).toEqual({ label: '99', type: 'info' })
  })
})

describe('documentDict — 文档类型(⚠ 前端 11 项缩写 vs SQL 12 项完整名,见末段 drift §1)', () => {
  const cases: Array<[string, string, string, TagType]> = [
    ['prd',       'PRD',       '📄', 'primary'],
    ['hld',       'HLD',       '🏗️', 'success'],
    ['lld',       'LLD',       '🔌', 'warning'],
    ['db',        'DB',        '🗄️', 'info'],
    ['api',       'API',       '📗', 'primary'],
    ['req',       '需求',      '📋', 'warning'],
    ['arch',      '架构',      '🏛️', 'success'],
    ['test',      '测试',      '🧪', 'primary'],
    ['manual',    '手册',      '📖', 'info'],
    ['changelog', 'Changelog', '📝', 'info'],
    ['other',     '其他',      '📁', 'info']
  ]
  it.each(cases)('%s → label「%s」/ icon「%s」/ tag「%s」', (code, label, icon, tag) => {
    expect(DOC_TYPE[code]).toEqual({ label, icon, tag })
    expect(docTypeLabel(code)).toBe(`${icon} ${label}`)
    expect(docTypeTag(code)).toBe(tag)
  })
  it('共 11 项(锁前端短缩写;⚠ SQL 是 12 项完整名 prd/arch/db_design/api_design/...)', () => {
    expect(Object.keys(DOC_TYPE).length).toBe(11)
  })
  it('未知/空 type → 裸值 + info', () => {
    expect(docTypeLabel('readme')).toBe('readme')
    expect(docTypeLabel(undefined)).toBe('-')
    expect(docTypeTag('readme')).toBe('info')
  })
})

describe('⚠ 已知契约漂移(api-contract 评审走 spawn 任务卡)', () => {
  it('drift §1 docType 完全异构:前端 11 短缩写 vs SQL 12 完整名,仅 prd/arch 2 项 1:1 对应', () => {
    expect('prd' in DOC_TYPE).toBe(true)         // ✓ SQL 同
    expect('arch' in DOC_TYPE).toBe(true)        // ✓ SQL 同
    expect('db_design' in DOC_TYPE).toBe(false)  // ⚠ SQL 有
    expect('api_design' in DOC_TYPE).toBe(false) // ⚠ SQL 有
    expect('test_plan' in DOC_TYPE).toBe(false)  // ⚠ SQL 有
    expect('manual_product' in DOC_TYPE).toBe(false) // ⚠ SQL 有
    // SQL value 集: prd/arch/db_design/api_design/proposal/ued/test_plan/test_report/
    //              api_doc/manual_product/manual_impl/manual_ops
    // 见 plm-backend/sql/business-document.sql:41-52
  })
  it('drift §2 status 03 已归档 tag:前端 danger / SQL list_class 空 (同 release/defect 范式)', () => {
    expect(DOCUMENT_STATUS['03'].tag).toBe('danger')
    // SQL 真值: list_class='' — 见 plm-backend/sql/business-document.sql:59
  })
})
