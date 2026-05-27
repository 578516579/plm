---
name: plm-db-design
description: PLM 数据库设计编排 SOP — 当用户要「这表怎么设计」「加个字段」「改索引」「迁移脚本」「Phase 02 数据库设计准入」「schema 对得上吗」时,按数据库设计漏斗(字段来源→概念建模→物理DDL→字典→索引→迁移→评审守门→契约→应用核验)出计划、分派子 agent(prd-author/system-architect/db-modeler/db-schema-reviewer/api-contract-keeper/config-engineer/db-ops/technical-writer)、裁决"schema 设计就绪"Gate、把结果沉淀成 signals 自进化。本 skill 是 db-orchestrator agent 的执行手册;只写 DDL 不编排时用 db-modeler,只应用 SQL 时用 db-ops,只评审时用 db-schema-reviewer。
---

# plm-db-design — PLM 数据库设计编排 SOP

把"建表**之前**该怎么做库设计"固化成可重复的编排流程。**编排 + 裁决 + 沉淀**在本 skill;**角色判断**在 [`db-orchestrator` agent](../../agents/db-orchestrator.md);**全流程/角色矩阵**在 [`数据库设计工作流.md`](../../../99-跨阶段/数据库设计工作流.md)。

> 一句话边界:`plm-db-design`(本 skill)= 表怎么建/字段类型字典怎么定/索引够不够/迁移安不安全/算不算 schema 就绪;`db-modeler` = 怎么写出 DDL;`db-ops` = 怎么把 SQL 应用上去;`db-schema-reviewer` = 设计质量守门。

---

## 何时触发

| 语义 | 用户原话举例 | 走本 skill 还是子 agent |
|---|---|---|
| 设计一个模块的库 | "这模块库怎么建"、"设计 X 表" | **本 skill**(漏斗编排) |
| Phase 准入 | "数据库设计完了"、"Phase 02 库准入"、"可以建表了吗" | **本 skill**(编排+裁决 schema 就绪) |
| 字段/索引变更 | "加个字段"、"改索引"、"改字段类型" | **本 skill**(先查 PRD-MAPPING §2) |
| 契约/可追溯核查 | "schema 对得上吗"、"这列哪来的" | **本 skill**(可追溯 + 5 层契约) |
| 只写 DDL | "给 X 表写 CREATE/ALTER" | `db-modeler` |
| 只应用 SQL | "把这 sql 跑上去"、"重建 schema" | `db-ops` |
| 只评审 | "评审下这表设计" | `db-schema-reviewer` |

---

## 编排 5 步法

### Step 0 — 先查 PRD-MAPPING §2 字段对照表(MUST,红线前置)
任何建表/加字段前,读 [PRD-MAPPING.md §2](../../../PRD-MAPPING.md) 确认每列都对得上字段对照表(prd-author 的产出)。**列不在对照表里 → 停**,回 `product-orchestrator`/`prd-author` 走 §M.1,**禁止自己造列**。

### Step 1 — 判范围(设计什么)
看改动落在哪,决定设计深度:

```
加/改 1 列            → prd-author 核来源 → db-modeler 迁移 ALTER → db-schema-reviewer 核命名+charset
改索引               → db-modeler 改 idx_/uk_ → db-schema-reviewer 核覆盖查询模式
新建整表 / Phase 02 准入 → 全漏斗(强制)
仅应用已审 SQL/修一致性 → 直接 db-ops(运维,不必整漏斗)
列不在 PRD-MAPPING §2 → 停,回 §M.1
```

### Step 2 — 出漏斗计划(分几层)
按数据库设计漏斗列本次要走的层(见 [agent 漏斗图](../../agents/db-orchestrator.md)):
- **L1 字段来源** prd-author:列 ↔ PRD-MAPPING §2
- **L2 概念/逻辑** system-architect:实体关系 / 范式取舍 / 跨模块共享表
- **L3 物理 DDL** db-modeler ★:tb_<entity> / 类型 / 约束 / charset utf8mb4 / 字段标配
- **L4 字典** db-modeler:biz_<entity>_* + list_class 色
- **L5 索引** db-modeler:idx_/uk_ 覆盖查询模式 + uk_<entity>_no
- **L6 迁移** db-modeler:ALTER 幂等 + 向后兼容 + 大表锁表
- **L7 评审守门** db-schema-reviewer ★:范式/索引/charset/FK/迁移/命名/sys_menu **一票否决**
- **L8 契约** api-contract-keeper:column↔resultMap↔domain↔DTO↔interface 5 层
- **L9 应用核验** db-ops:应用 SQL(utf8mb4)+ schema 一致性

> 原则:字段对照表(prd-author 产出)先于 DDL;原型指不出的列不进 schema。

### Step 3 — 分派子 agent(谁来做)
按矩阵下发,**主 Claude 按顺序调 Agent**(子 agent 不能再 spawn):

| 子任务 | 分派 |
|---|---|
| 列来源核对 | `prd-author` |
| 实体关系/范式/跨模块表 | `system-architect` |
| DDL/字典/索引/迁移草稿 | `db-modeler` ★ |
| schema 设计评审守门 | `db-schema-reviewer` ★ |
| 5 层命名契约一致 | `api-contract-keeper` |
| charset/DataSource yml | `config-engineer` |
| 应用 SQL + 一致性验证 | `db-ops` |
| DB 设计文档 | `technical-writer` |
| 敏感字段/注入预审 | `security-reviewer` |

复杂(≥5 agent)时让 `db-orchestrator` 出 Mermaid DAG,再 `task-tracker` 拆 TodoWrite。

### Step 4 — 裁决"schema 设计就绪"Gate(算不算可建表)
逐条核对(§M.10.3),全满足才判**就绪**:

- [ ] **可追溯**:每列对得上 PRD-MAPPING §2(无凭空多出的列)
- [ ] **命名合规**:tb_/<entity>_id/<entity>_no/idx_/uk_/biz_;§M.7 跨模块同名(无 creator_id 漂移)
- [ ] **charset**:utf8mb4 + 字段长度够(gotcha #2);字段标配齐
- [ ] **索引充分**:覆盖查询模式 + uk_<entity>_no
- [ ] **迁移安全**:幂等 + 向后兼容 + 大表锁表评估
- [ ] **sys_menu**:business-*.sql 含 INSERT(gotcha #7)或 @no-menu 豁免
- [ ] **契约一致**:api-contract-keeper 确认 5 层
- [ ] **应用核验**:db-ops 确认 DB 实际 = sql 期望

任一不满足 → **驳回**,指明回 db-modeler/db-ops 修;**禁**"先建着回头补"、**禁**自己造列、**禁**放过非 utf8mb4。

### Step 5 — 沉淀 signals(自进化)
把本轮库设计结果记进 [`signals/YYYY-MM.md` 数据库设计编排段](../../../99-跨阶段/signals/README.md):
- `schema_naming_drift_count`(命名漂移被拦截)
- `index_gap_count`(查询无索引覆盖)
- `charset_violation_count`(非 utf8mb4,gotcha #2,应=0)
- `migration_unsafe_count`(非幂等/大表裸 ALTER)
- `missing_sys_menu_count`(business-*.sql 缺 sys_menu,gotcha #7)
- `schema_drift_count`(DB 实际 ≠ sql 期望)

**触发提案**(主动建议):charset_violation > 0 → P0 复盘(gotcha #2 复发);naming_drift 月≥3 → §M.7 强化/加 lint;index_gap 集中 → 补索引 checklist。

---

## 编排速查卡

```
请求 → [Step0 查 PRD-MAPPING §2] → 列不在表? → 回 prd-author(§M.1) → 停
                │ 在表
                ▼
[Step1 范围] → [Step2 漏斗分层] → [Step3 分派 DAG]
                                          │
        prd-author ─ L1 列来源 ───────────┤
        system-architect ─ L2 范式/跨模块 ┤
        db-modeler ★ ─ L3~L6 DDL/字典/索引/迁移 ┤  ← 字段表先行
        db-schema-reviewer ★ ─ L7 守门(charset/命名/sys_menu 一票否决)┤
        api-contract-keeper ─ L8 5 层契约 ┤
        db-ops ─ L9 应用+一致性验证 ───────┤
                                          ▼
        [Step4 裁决] 可追溯+命名+charset+索引+迁移+sys_menu+契约+核验
                       全过→schema 就绪(交 coder)  /  有缺→驳回
                                          ▼
        [Step5 signals] 命名漂移 / 索引缺口 / charset 违规 / 迁移不安全 / 缺 sys_menu / schema 漂移
```

---

## 反模式(一票否决)

- ❌ 跳过 Step0 查 PRD-MAPPING §2,直接建表
- ❌ 列不在字段对照表也"顺手建一列"(§M.1 红线)
- ❌ charset 非 utf8mb4 还放行(gotcha #2,P0 复发坑)
- ❌ 命名漂移(creator_id vs author_user_id)当小事(§M.7 地基)
- ❌ 大表裸 ALTER 不评估锁表 / 迁移不幂等
- ❌ business-*.sql 缺 sys_menu(gotcha #7,前端不可达)
- ❌ 编排只下发不裁决(设计了等于没设计)

---

## 引用

| 文件 | 用途 |
|---|---|
| [`.claude/agents/db-orchestrator.md`](../../agents/db-orchestrator.md) | 本 skill 的角色/裁决判断 |
| [`.claude/agents/db-modeler.md`](../../agents/db-modeler.md) | L3~L6 DDL/字典/索引/迁移执行 |
| [`.claude/agents/db-schema-reviewer.md`](../../agents/db-schema-reviewer.md) | L7 设计守门执行 |
| [`.claude/agents/db-ops.md`](../../agents/db-ops.md) | L9 应用 SQL/一致性验证执行 |
| [`.claude/rules.md` §M.10 + §M.2/M.3/M.7(DoD/命名/跨模块)+ §A](../../rules.md) | 硬卡控 |
| [`99-跨阶段/数据库设计工作流.md`](../../../99-跨阶段/数据库设计工作流.md) | 全流程 + 角色矩阵 + 进化节律 |
| [`PRD-MAPPING.md` §2 字段对照表](../../../PRD-MAPPING.md) | 列来源单一事实来源 |
| 根 CLAUDE.md Gotchas #2(utf8mb4)/ #7(sys_menu) | 一票否决依据 |

## 修订记录

| 版本 | 日期 | 变更 |
|---|---|---|
| v1.0 | 2026-05-27 | 首次创建:固化数据库设计编排 SOP(proposal 0025)|
