# Document 文档管理 — PRD（合并 5 stub）

| 字段 | 值 |
|---|---|
| 版本 | v1.0 |
| AgriPLM 来源 | 模块 #3 (Inception 立项书) + #6 (PRD) + #8 (Arch) + #9 (DB Design) + #10 (API Design) + 后续可扩展 (UED / 手册 / 测试报告 / API doc) |
| 模块定位 | `plm-document` Maven + `@plm/document` npm — 合并 5 个 stub 为一个文档实体 |
| 替换的 stub | plm-proposal / plm-prd / plm-arch / plm-dbdesign / plm-apidesign |
| 路线图 | v0.4.0 P0 |

---

## 1. 设计动机

5 个文档类 stub 共享同一数据模型（标题/内容/版本/状态/作者/审核人），只是 `doc_type` 不同。把它们退化为单个 `plm-document` Maven 模块 + `doc_type` 字段，能：

- 表数量 5 → 1 (`tb_document`)
- 字典 15+ → 3 (`biz_doc_type` / `biz_doc_status` / `biz_doc_category`)
- API 端点 30 → 6
- 后期"文档列表 / 文档全文搜索 / 关联文档"等聚合查询 1 张表搞定

---

## 2. doc_type 取值（12 种）

| value | label | 替代 stub | 路线 |
|---|---|---|---|
| `prd` | PRD 产品需求 | plm-prd | v0.4 |
| `arch` | 系统架构 | plm-arch | v0.4 |
| `db_design` | 数据库设计 | plm-dbdesign | v0.4 |
| `api_design` | API 详细设计 | plm-apidesign | v0.4 |
| `proposal` | 立项建议书 | plm-proposal | v0.4 |
| `ued` | UED 设计稿 | plm-ued | v0.5 |
| `test_plan` | 测试方案 | plm-testplan | v0.4 |
| `test_report` | 测试报告 | plm-testreport | v0.4 |
| `api_doc` | API 文档 | plm-apidoc | v0.4 |
| `manual_product` | 产品手册 | plm-manual-product | v0.4 |
| `manual_impl` | 实施手册 | plm-manual-impl | v0.5 |
| `manual_ops` | 运维手册 | plm-manual-ops | v0.5 |

后期增加新 doc_type 仅需 dict 加一行 + 路由加一项,不动 Maven。

---

## 3. 字段（13 + 6 通用）

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `document_id` | bigint PK | | |
| `document_no` | varchar(32) unique | ✅ | `DOC-<TYPE>-YYYY-NNNN`（ADR-0007） |
| `project_id` | bigint | ✅ | FK→tb_project |
| `related_entity_type` | varchar(20) | ⬜ | requirement/sprint/task (引用业务实体类型) |
| `related_entity_id` | bigint | ⬜ | 引用业务实体 ID |
| `doc_type` | varchar(20) | ✅ | 字典 `biz_doc_type` (12 取值) |
| `title` | varchar(200) | ✅ | 文档标题 |
| `content` | longtext | ⬜ | Markdown 全文 |
| `version` | varchar(20) | ✅ | 如 `v1.0` / `v1.2.3` |
| `status` | varchar(2) | ✅ | 字典 `biz_doc_status` (4 状态) |
| `author_user_id` | bigint | ✅ | 作者 |
| `reviewer_user_id` | bigint | ⬜ | 审核人 |
| `tags` | varchar(200) | ⬜ | CSV |
| + 通用 6 字段 | | | |

## 4. 4×4 状态机

| 当前\到 | 00 草稿 | 01 待评审 | 02 已发布 | 03 已归档 |
|---|---|---|---|---|
| **00 草稿** | — | ✅ | ❌ | ❌ |
| **01 待评审** | ✅（反向边·打回） | — | ✅ | ❌ |
| **02 已发布** | ❌ | ✅（再次评审）| — | ✅ |
| **03 已归档** | ❌ | ❌ | ❌ | — |

**反向边**: 01→00 (打回修改) + 02→01 (重新审批)

## 5. 错误码

| Code | 场景 |
|---|---|
| 200 | 成功 |
| 404 | 文档不存在 |
| 601 | 状态机违规 |
| 602 | 必填字段空 (title/version/docType/projectId) |
| 604 | doc_type / status 字典值不合法 |
| 701 | document_no 重复 |
| 702 | FK 不存在 (project / related_entity) |
| 707 | 进入"已发布"必填 reviewer_user_id |

## 6. 端点（6 个）

| # | Method | Path | 权限 |
|---|---|---|---|
| 1 | GET | `/business/document/list` | list |
| 2 | POST | `/business/document/export` | export |
| 3 | GET | `/business/document/{id}` | query |
| 4 | POST | `/business/document` | add |
| 5 | PUT | `/business/document` | edit |
| 6 | DELETE | `/business/document/{ids}` | remove |

## 7. ADR-0007 编号规则

```
DOC-<TYPE_UPPER>-YYYY-NNNN
例: DOC-PRD-2026-0001
    DOC-ARCH-2026-0001 (流水号按 type 分别累加)
```

## 8. 验收

- [ ] 创建文档,doc_type=prd 时生成 `DOC-PRD-2026-0001`
- [ ] 4×4 状态机含反向边 (01→00 + 02→01) 全过
- [ ] 进入 02 必填 reviewer_user_id (707)
- [ ] E2E 8 case 全过

## 9. 修订记录

| 版本 | 日期 | 变更 |
|---|---|---|
| v1.0 | 2026-05-16 | 首次创建,5 stubs → 1 module 合并方案 |
