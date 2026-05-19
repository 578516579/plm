---
name: adr-writer
description: PLM ADR (架构决策记录) 编写 — Michael Nygard 格式, 与已有 ADR-0001~0007 风格一致。当用户说"写 ADR / architecture decision / 架构决策 / 选型记录 / 不可逆决策 / 编号规则 / 状态机 ADR / 文档化决策"时调用。输出: 03-开发/ADR/NNNN-<标题>.md。**tech-lead agent 的子工具** — agent §2.2 触发。
---

# adr-writer — ADR 编写 skill v0.1

**tech-lead agent 的子工具**, 主走 §2.2 ADR 主写职责。

历史 ADR (作标杆): 0001 项目编号 / 0002 ??? / 0003 ??? / 0004 Sprint 编号 / 0005 plm-defect 状态机 / 0006 TestCase 编号 / 0007 文档 type 累加。本 skill 保持与历史 ADR 同款 Michael Nygard 格式。

---

## 1. 何时调用

- 用户说 "写 ADR / 选型记录 / 架构决策 / 不可逆决定"
- tech-lead agent §2.2 触发
- 任何"6 个月后回来看会不知道为啥这么做"的决定
- proposal 0300-0399 架构类必走 ADR

---

## 2. 何时该写 ADR (per tech-lead §2.2)

- 技术选型 (Redis Lettuce vs Jedis / MySQL vs PG / @Version vs 悲观锁)
- 编号规则 (PRJ-YYYY-NNNN / SPR-YYYY-NNNN / DEF-YYYY-NNNN)
- 状态机设计 (M×M 转换矩阵 + 反向边)
- 错误码段分配 (700-799 数据 / 800-899 安全)
- 文档分类规则 (per ADR-0007 type 累加)
- 跨模块约定 (e.g. FK 校验走 Service.checkExists)
- 部署架构 (单体 vs 微服务 / 单库 vs 分库)

---

## 3. ADR 模板 (Michael Nygard 风格)

```markdown
# ADR-NNNN: <一句话标题>

## 元信息
| 字段 | 值 |
|---|---|
| 编号 | NNNN (递增, 不复用) |
| 状态 | proposed / **accepted** / superseded / deprecated |
| 决策日 | YYYY-MM-DD |
| 决策人 | tech-lead agent + <相关角色> |
| 关联 proposal | (如有, 链 99-跨阶段/proposals/NNNN-*.md) |
| 关联代码 commit | <hash> (落地后回填) |

## Context (背景)

当前面临什么决策。1-3 段。描述:
- 触发原因 (PRD 要求 / 现有问题 / 性能 / 安全)
- 选择空间 (有哪些可能方案)
- 约束 (PLM 已有架构 / solo 团队 / Java 17)

## Decision (决策)

**选 X (不选 Y/Z)**, 具体方案:
- 实施步骤
- 影响文件
- 配套配置
- 是否需先迁移现存代码 (per [proposal 0041](../99-跨阶段/proposals/0041-meta-rule-grep-existing-code.md) 第 4 checkbox)

## Consequences (后果)

### 正面
- 优势 1
- 优势 2

### 负面
- 劣势 / 成本 1
- 劣势 / 成本 2

### 风险缓解
- 风险 1: <描述>; 缓解: <对策>

## Alternatives Considered (备选方案)

### 方案 A: <名称>
- 描述
- 优势
- **不选原因**: <精炼>

### 方案 B: <名称>
- 描述
- 不选原因

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| YYYY-MM-DD | tech-lead agent (via adr-writer) | 首版 accepted |
```

---

## 4. 5 步工作流

```
[Step 1] 选 ADR 编号
  ls 03-开发/ADR/ | grep -oE "^[0-9]{4}" | sort -n | tail -1
  # 取最大 + 1, 不复用历史编号

[Step 2] 调查 (per 0040 §3.1 + 0041 §3.1)
  - Read 现有代码 (grep 现状)
  - Read 类似 ADR (抄结构)
  - AskUserQuestion 列方案选项

[Step 3] 写 ADR (用 §3 模板)
  必含 Context / Decision / Consequences / Alternatives 4 段

[Step 4] 自检
  - [ ] ≥ 2 备选方案
  - [ ] 每方案有"不选原因"
  - [ ] Consequences 正反 + 风险缓解齐
  - [ ] 若约束代码层, 已 grep 现存代码

[Step 5] 输出 + 关联
  - 输出 03-开发/ADR/NNNN-<标题>.md
  - 若 ADR 触发规范变更 → 转 /proposal skill 升 proposal
  - 若涉及 PRD-MAPPING.md §3/§4 状态机/错误码 → 同步更新
```

---

## 5. 输出

`03-开发/ADR/NNNN-<标题>.md` (kebab-case 标题)

例: `03-开发/ADR/0008-fk-validation-checkexists-pattern.md`

---

## 6. 衔接

| 上游 | adr-writer | 下游 |
|---|---|---|
| tech-lead §2 各职责发现需决策 | → ADR | → /proposal skill (规范类) |
| pm-prd-writer 留的"开放问题" | → 一个个 ADR 决议 | → backend/frontend-coder 实施 |
| reflect-quarterly §A ADR 审计 | → amend / superseded 旧 ADR | → 修订记录 |

---

## 7. 反模式

- ❌ ADR 只描述决策, 不列备选方案 (无对比)
- ❌ "不选原因" 写"不合适" (不精炼)
- ❌ Consequences 只列正面 (确认偏误)
- ❌ 风险缓解空 (假装没风险)
- ❌ 编号复用历史 (违反不变性)
- ❌ amend 后 status 仍 accepted (应走 superseded by NNNN)
- ❌ 凭记忆写, 不 grep 现存代码 (per 0041)

---

## 8. 历史

| v0.1 | 2026-05-19 | 首版; tech-lead 配套 4 skill 之一 |
