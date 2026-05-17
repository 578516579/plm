# Proposal 0301 (ADR-C): Document 模块与 PRD §F5.5 "知识库" 的概念错位

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0301 |
| 标题 | ADR-C: 当前 `plm-document` 占用 F5.5 位置但实现是"文档中心",PRD §F5.5 实指"知识库" |
| 状态 | proposed |
| 类型 | 架构 |
| 提出人 | Claude (PRD-align 第二轮审计) |
| 提出日期 | 2026-05-17 |
| 评审人 | 项目负责人 + 架构师 + 产品经理 |
| 评审日期 | _(待定)_ |
| Tracking 截止 | merged 后 4 周 (架构变更需更长观察期) |

---

## 1. 背景

`plm-document` 模块在 PRD-MAPPING.md §1 占用 F5.5 位置,但实现的是"文档中心"(合并 prd/arch/dbdesign/apidesign/proposal 5 stub 的统一存储层,使用 polymorphic FK `relatedEntityType + relatedEntityId`)。

但 PRD [§F5.5 L414-417](../../prd和原型/AgriAI-PLM-完整PRD文档.md) 写的"知识库管理"实际指 **AI 语义检索 + 跨项目知识复用**(归档/检索/复用 三大能力,涉及 embedding 向量、语义搜索、复用推荐)。

**两件不同的事**:
- 当前 Document = 通用文档存储(类似 Confluence)
- PRD §F5.5 = AI 知识库(类似 Notion AI + 内部 Stack Overflow)

---

## 2. 证据

- 审计报告 [2026-05-17-12-modules-drift-audit.md §Document](../audits/2026-05-17-12-modules-drift-audit.md): 标 🟡 中等
- PRD [§F5.5 L414-417](../../prd和原型/AgriAI-PLM-完整PRD文档.md):"知识库管理:归档/检索/复用"
- 当前 Domain [Document.java](../../plm-backend/plm-document/src/main/java/cn/com/bosssfot/dv/plm/document/domain/Document.java) `relatedEntityType` polymorphic FK 设计 (无 `embeddingVector` / `semanticTags` / `reusedCount`)
- 原型 = **无独立 HTML** (Document 是脚手架合并 5 stub 的产物,从未对应原型)
- 用户请求:2026-05-17 会话明确"需 ADR-C 决策"

---

## 3. 提案

**3 个备选方案**,**推荐 Option B (拆出 knowledge-base 新模块)**。

### Option A — 重命名 Document → BusinessDoc,让出 F5.5 位置

把 `plm-document` 改名 `plm-business-doc`,作为通用文档中心(不挂 F5.5);新立 `plm-knowledge-base` 模块占 F5.5,实现 PRD §F5.5 语义检索 + 复用推荐。
- ✅ 概念清晰,两个模块各司其职
- ✅ §1 大表两行替换,逻辑明确
- ❌ **大改名**:Maven 模块名 + Java 包名 + URL 路径(`/business/document` → `/business/business-doc`) + 前端 npm 包名 + sys_menu 配置 + 字典前缀 `biz_document_*` → `biz_business_doc_*`
- ❌ 数据库表名 `tb_document` 改 `tb_business_doc` → 迁移 SQL
- ❌ 已有 28 个 PRD-aligned 模块中至少 5 个(prd/arch/dbdesign/apidesign/proposal)的合并依赖 `tb_document`,FK 引用全要改

### Option B — 拆出 knowledge-base 新模块,Document 保留承担 F5.5-doc 部分 ⭐ **推荐**

`plm-document` 保留(继续作"文档中心"承担合并 stub 的工作),把它**从 F5.5 位置移到 §1 大表的"通用基础"分组(类似 system/common)**;新立 `plm-knowledge-base` 模块占 F5.5,只做 PRD §F5.5 的 AI 检索 + 复用三件事:
1. 文档归档(可引用 `tb_document` 的现有内容,FK 关联)
2. 语义检索(Milvus 向量库 + embedding 字段)
3. 跨项目复用推荐(`reusedCount` / `recommendedTo` 字段)

- ✅ 不动现有 `plm-document` 代码,无大改名风险
- ✅ §1 大表清晰:Document 移出 F5.5 分组,新立 knowledge-base
- ✅ PRD §F5.5 真实需求(AI 语义)被独立模块承担,不被通用文档稀释
- ✅ 数据库新建 `tb_knowledge_base` + 引用 `tb_document.document_id` (Document 作为内容来源)
- ⚠️ §1 大表新增 1 行(31→32 业务模块)
- ⚠️ knowledge-base 模块需 P3 阶段实施(本提案只决策架构,具体实施列入路线图)

### Option C — Document 演化承担 F5.5,加 embedding/semantic 字段

在 `plm-document` 内部加 `embeddingVector LONGTEXT` / `semanticTags VARCHAR(500)` / `reusedCount INT` / `embeddingModel VARCHAR(50)` 字段;实现 §F5.5 三件事。
- ✅ 改动最小,1 个模块承担所有
- ✅ 单点维护
- ❌ Document 模块膨胀:既是"统一存储"又是"AI 知识库",违反单一职责
- ❌ embedding 字段巨大(向量长度~768 维 float),与现有 Document `content` 字段共表 → tb_document 单表过大,性能差
- ❌ 跨项目复用推荐需要复杂查询逻辑,与简单 CRUD 的 Document 性质冲突

### 改动文件清单 (按 Option B,本提案仅记录架构决策,实施分阶段)

**阶段 1 (本提案 merged 后立即,纯 doc commit)**:
| 文件 | 改动 |
|---|---|
| `PRD-MAPPING.md §1` | Document 行从 F5.5 移出,标"通用基础"分组(行级注释);新增 F5.5 行指向 `plm-knowledge-base` (状态 🔴 待实施) |
| `PRD-MAPPING.md §2` | 加 Document 字段表(承认其"通用文档中心"定位)+ 加 KnowledgeBase 字段表骨架(待实施) |
| `99-跨阶段/PLM-路线图.md` | knowledge-base 模块纳入 Phase 06 或 v0.2.0 路线 |

**阶段 2 (knowledge-base 模块实施,独立 PR,本提案不涵盖)**:
- 新建 `plm-knowledge-base` Maven 模块 + DDL + Domain/Mapper/Service/Controller
- AgriKB 向量库接入(Milvus 或 PostgreSQL pgvector)
- AI 检索接口 `POST /business/knowledge-base/search`

---

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| 开发者 | 阶段 1 仅文档;阶段 2 新模块开发(独立 sprint 估 2-3 周) |
| Claude | §1 大表分类微调,Document 不再代表 F5.5;ADR-C 记录到 PRD-MAPPING §10 |
| 测试 / 运维 | 阶段 1 无;阶段 2 新模块完整 E2E + 字典 |
| 已有代码 / 文档 | Document 模块代码**完全不动**;它的角色被显式重新定义 |

---

## 5. 风险

- **风险 1**: knowledge-base 模块是 P3 范围,可能因优先级被无限延期。**缓解**: 本 ADR 仅做架构决策,实施在路线图 v0.2.0 兑现;Phase 06 评审 Gate 时检查。
- **风险 2**: Option B 选定后,如未来 Document 又需要 embedding(混用模式) → 又得改架构。**缓解**: 明确"Document 不做 AI 检索"作为契约,违反此契约的需求统一走 knowledge-base。
- **风险 3**: 重新分类 Document 在 §1 中的位置可能引起团队困惑("F5.5 怎么变了?")。**缓解**: PRD-MAPPING.md §1 加注释明确历史变迁,并在 [README.md](../../README.md) 提示。

---

## 6. 备选方案

详见 §3 — Option A (Document 改名让位) / Option B (拆出 knowledge-base 新模块,**推荐**) / Option C (Document 演化承担)。

---

## 7. 实施计划

```
[ ] Step 1: 本提案 review + 评审通过 → status: accepted
[ ] Step 2: 阶段 1 commit (单 doc commit): §1 大表分类调整 + §2 Document 字段表 + KB 骨架 + 路线图
[ ] Step 3: 通知团队架构决策(Phase 05 上线评审会议)
[ ] Step 4: 阶段 2 实施划入 v0.2.0 路线,本提案 status: tracking(等阶段 2 启动)
[ ] Step 5: 阶段 2 启动时新开 proposal 0303 处理 knowledge-base 模块实现细节
```

---

## 8. 衡量指标

- **信号 1**: Document 模块的 CRUD 调用频次 (sample 4 周) — 验证它仍是 5 个 stub 模块的统一存储,不应被滥用为 AI 检索接口
- **信号 2**: PRD §F5.5 "知识库" 在路线图中的优先级状态 (路线图文档可追溯)

跟踪期:merged 后 4 周 + 阶段 2 启动节点。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| _(待用户拍板)_ | _(通过 / 改方案 / 拒绝)_ | | |

---

## 10. 实施后跟踪

_(merged 后填)_

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-17 | Claude | 初稿,Option B 推荐 (拆出 knowledge-base) |
