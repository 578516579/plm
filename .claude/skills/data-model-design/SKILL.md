---
name: data-model-design
description: PLM 跨模块数据建模 — Star schema (fact + dim 表) / 数据 lineage / 计算逻辑. 用户说"维度建模 / fact 表 / dim 表 / Star schema / 跨模块建模 / OLAP 设计"时调用. 输出: 02-设计/<dashboard>-数据建模.md. **data-engineer agent 的子工具**。
---

# data-model-design — 跨模块数据建模 skill v0.1

**data-engineer agent §2.1 配套**, Kimball 维度建模视角。区别于 tech-lead db-design 的单表 ER。

## 1. 何时调用
- "维度建模 / fact 表 / dim 表 / Star schema / 跨模块汇总 / OLAP"
- data-engineer §2.1 触发, Phase 02 dashboard 模块设计

## 2. 步骤

### 2.1 识别业务事实
- 哪些事件需要被分析? (任务完成 / 缺陷修复 / 上线发布 / cycle 转换)
- 每事件的量度 (count / 时长 / 状态变化次数)

### 2.2 设计 fact 表
- `fact_<event>` (e.g. fact_task_complete, fact_defect_fix)
- 列: 时间 PK + 维度 FK + measure 数值

### 2.3 设计 dim 表
- `dim_project / dim_sprint / dim_user / dim_time / dim_priority`
- 含: 自然键 + 业务键 + 退化属性 + (有效期 SCD 类型 1/2)

### 2.4 数据 lineage
- 业务表 (tb_*) → fact / dim 表的来源 SQL
- 转换规则 (e.g. 状态映射, 时区调整)

## 3. 输出模板

```markdown
# <dashboard> 数据建模

## 业务问题
(要回答什么报表问题)

## fact 表
| 表名 | 粒度 | 列 | 量度 |
|---|---|---|---|

## dim 表
| 表名 | SCD 类型 | 来源 |
|---|---|---|

## 数据 lineage
(business table → fact/dim 的 SQL)
```

## 4. 衔接
- 上游: product-manager dashboard 模块 PRD
- 下游: tech-lead db-design 落具体 DDL + backend-coder ETL job

## 5. 历史
| v0.1 | 2026-05-19 | 首版; data-engineer 配套 1/4 |
