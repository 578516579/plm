# PRD: Document 模块 — 通用文档中心 (F5.5 部分,合并 5 stub)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.2 (完整实质,2026-05-17 由 Claude 基于 9 个范本批量升级) |
| 作者 | Wjl |
| PRD § | F5.5 部分(AgriAI-PLM-完整PRD文档.md §F5.5 知识库管理 — 仅承担归档/查询/复用引用部分) |
| 原型 HTML | (无独立 HTML,从合并 5 stub 派生) |
| 评审状态 | pending (待 Phase 01 Gate 评审) |
| 关联 ADR | [ADR-C](../99-跨阶段/proposals/0301-adr-c-document-vs-knowledge-base.md) Document vs AgriKB 边界 |
| 关联 OKR | _2026 Q2-O5-KR5: Document 模块上线,文档归档覆盖率 ≥ 80%_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "Document (F5.5 部分)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 系统当前有 5 个 stub 文档模块(prd / arch / dbdesign / apidesign / proposal),5 个具体问题:

1. **5 个 stub 模块字段相同但代码重复**:每个 stub 都有 title/content/version/status/author,**重复 5 套 mapper+service+controller**,维护成本高。
2. **跨文档引用断链**:某需求关联 PRD + UED + Arch 3 个文档,**当前没统一关联表**,只能在每个表里查 projectId,无法做"按需求看所有相关文档"。
3. **PRD §F5.5 知识库的归档查询能力缺位**:F5.5 提到"AI 语义检索 + 跨项目复用",但 **AgriKB + Milvus 向量被路线图剥离**(详 ADR-C),归档与基础查询能力则必须由本模块承接。
4. **文档版本演进无统一**:5 个 stub 自己写 version 字段,**没有"按 docType 看全项目同类文档"的能力**。

### 1.2 目标 (北极星指标)

**目标**:6 个月内合并 5 stub 进 1 个 Document 模块,通过 12 值 docType 字典 + 多态 relatedEntity 关联,做"通用归档 + 查询 + 引用追溯"。

**衡量指标**:
- **5 stub 完全合并 100%**(prd/arch/dbdesign/apidesign/proposal 全走 Document)
- **文档归档覆盖率 ≥ 80%**(每个发布的实体文档都进 Document)
- **跨文档引用查询响应时间 ≤ 200ms**(基于 relatedEntityType + relatedEntityId 索引)
- **tags 标签归档分类率 ≥ 70%**(每份文档至少 1 个 tag)
- **docType 12 值字典使用率分布**(prd/arch/api_doc/test_plan 应是前 4 名)

### 1.3 不做的事 (Out of Scope)

本期**不做**(对应 ADR-C / 路线图剥离清单):
- **AI 语义检索**(基于 embedding + Milvus 向量库)— 路线图剥离 "永不做清单" 第 1 项
- **跨项目语义复用推荐**(基于历史文档相似度)— 路线图剥离
- **AgriKB 知识库**(智能问答 / 知识图谱)— 路线图剥离
- **多人协作实时编辑**(类飞书文档多光标)— 留 v0.5+
- **文档版本对比 Diff** — 留 v0.3
- **文档评审多级工作流** — 仅单评审人,留 v0.3

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **所有业务用户** | CRUD 自己创建的 Document | 按 docType 归档文档 / 加标签 |
| **管理员** | 全 CRUD + 跨 docType 查询 | 维护标签库 / 跨项目复盘 |
| **评审人 (reviewer)** | 评审 + 决策 | 走 4 态状态机 |

### 2.2 典型场景

**S1 PRD 模块归档到 Document**(最高频)
> Prd 模块 status 推 '02 已确认' → Service 自动同步:在 Document 表 INSERT 1 条 → docType='prd' + title=PRD title + content=Markdown 全文 + version=PRD version + relatedEntityType='requirement' + relatedEntityId=REQ-89 + tags="灌溉,v1.0,prd" + status='02 已发布'

**S2 跨文档引用查询**(关键流程)
> PM 想看 "REQ-89 关联的所有文档" → GET /business/document/list?relatedEntityType=requirement&relatedEntityId=89 → 返回 PRD + UED + Arch + Test Plan + API Doc 多份关联文档列表

**S3 按 docType 全项目复盘**(归档能力)
> 管理员要复盘 "全项目的所有提案 proposal" → GET /business/document/list?docType=proposal → 返回所有 docType='proposal' 文档,带 tags 过滤

**S4 评审流程**(关键流程)
> 文档 status='00→01 待评审' → 评审 → status='01→02 已发布' 或 status='01→00 草稿'(反向边打回)

**S5 文档归档**(终态)
> 文档过时 → status='02→03 已归档' → 列表过滤但保留可查

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "Document (F5.5 部分)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: documentId / documentNo (`DOC-<TYPE>-YYYY-NNNN`,ADR-0007)/ projectId(FK 必)
- 多态关联: relatedEntityType / relatedEntityId(配对使用,联合索引)
- 鉴别字段: docType(12 值字典)
- 用户输入: title / content(Markdown)/ version
- 流程: status(4 态含反向边)/ authorUserId / reviewerUserId
- 归档: tags(CSV,承载"知识库归档分类")

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) document 行:4 态含反向边。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 待评审} | 默认初始 |
| 01 | 待评审 | {00 草稿(打回), 02 已发布} | 反向边 01→00 评审打回 |
| 02 | 已发布 | {01 待评审, 03 已归档} | 终态分支;重新评审 或 归档 |
| 03 | 已归档 | {} | 终态 |

**特殊规则**:
- 新建强制 `status='00'`(违反抛 601)
- docType 12 值字典白名单(prd/arch/db_design/api_design/proposal/ued/test_plan/test_report/api_doc/manual_product/manual_impl/manual_ops,604)
- 反向边 01→00 必填 reviewNote(602)
- FK 校验:projectId 必,relatedEntityId 联合 relatedEntityType 校验(702)
- documentNo 自动生成,前端不可写

---

## 5. AI 能力

### 5.1 当前状态

**Document 模块本期无独立 AI 端点**。

PRD §F5.5 提到的 "AI 语义检索 / 跨项目复用 / Milvus 向量库" 全部走 [PLM-路线图.md "永不做清单"](../PLM-路线图.md) 第 1 项剥离,详 ADR-C。

### 5.2 当前承担能力

| 能力 | 是否实现 |
|---|---|
| 通用文档存储(合并 5 stub) | ✅ |
| docType 12 值分类 | ✅ |
| relatedEntity 多态关联 | ✅ |
| tags 标签归档 | ✅ |
| 简单关键词搜索(MySQL LIKE) | ✅ |
| AI 语义检索 | 🔴 剥离 |
| 跨项目复用推荐 | 🔴 剥离 |

### 5.3 路线图

- v0.5+: 简单 BM25 全文搜索(MySQL 全文索引 / Elasticsearch)
- v1.0+: 重启 AgriKB 时,引入 embedding + 向量库

---

## 6. 验收标准

**PRD §F5.5 部分验收**(归档/查询/复用引用部分):
- ⏳ **5 stub 合并 1 个 Document 模块**(本期 docType 12 值字典就位)
- ⏳ **跨文档引用追溯**(relatedEntityType + relatedEntityId 索引就位)
- ⏳ **tags 标签归档分类**(本期就位)

**模块特有验收**(本会话已落地):
- 4 态状态机 + 反向边 01→00 单测覆盖
- docType 12 值字典白名单(604)
- documentNo `DOC-<TYPE>-YYYY-NNNN` 编号规则(ADR-0007)
- FK 校验:projectId 必、relatedEntity 联合校验(702)
- 反向边必填 reviewNote(602)

---

## 7. 不做的事 — 详 §1.3

- AI 语义检索 / 跨项目复用 / AgriKB 知识库 / 多人协作 / Diff / 多级审批

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Document-数据库设计.md](../02-设计/Document-数据库设计.md)
- API 设计: [Document-API设计.md](../02-设计/Document-API设计.md)
- 测试计划: [Document-测试计划-2026-05-17.md](../04-测试/Document-测试计划-2026-05-17.md)
- 发布计划: [Document-发布计划-2026-05-17.md](../05-上线/Document-发布计划-2026-05-17.md)
- ADR: [proposal 0301 ADR-C](../99-跨阶段/proposals/0301-adr-c-document-vs-knowledge-base.md)
- 路线图: [PLM-路线图.md "永不做清单" 第 1 项](../PLM-路线图.md)
- AgriAI PRD: [§F5.5](../prd和原型/AgriAI-PLM-完整PRD文档.md)
- 关联模块: 所有可被归档的业务模块(prd/arch/dbdesign/apidesign/ued/testplan/testreport/apidoc/manualproduct/manualimpl/manualops)
