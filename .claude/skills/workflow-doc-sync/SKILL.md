---
name: workflow-doc-sync
description: PLM 三件套同步审 — .claude/rules.md + 03-开发/开发规范.md + 99-跨阶段/模块工作流.md 4 维 coherence 差异. 用户说"三件套同步 / rules vs 开发规范 / 工作流 coherence / 跨文档一致"时调用. 输出: 99-跨阶段/knowledge-audit-trio-<date>.md + 补丁 diff. **knowledge-curator agent 的子工具**。
---

# workflow-doc-sync — 三件套同步审 skill v0.1

## 1. 何时调用
- "三件套同步 / rules vs 开发规范 / coherence"
- knowledge-curator §2.3
- reflect-quarterly 时

## 2. 4 维 coherence 检查

per reflect-quarterly v0.1 设计 4 维:

### 2.1 命名一致性
- rules.md "plm-*" / 开发规范.md "plm-*" / 模块工作流.md "plm-*" → 一致 ✅

### 2.2 流程定义一致性
- Phase 01-06 在三件套都有定义? 名称 / 准入准出条件 / 签字角色一致?

### 2.3 角色定义一致性
- product-manager / tech-lead / tester / ops / ... 角色定义在哪儿? 一致吗?

### 2.4 MUST/SHOULD 一致性
- rules.md §X.Y MUST 但 开发规范.md 同处仅 SHOULD → 不一致

## 3. 输出模板
```markdown
# 三件套同步审 — 2026-XX-XX

## 维度 1: 命名
- ✅ "plm-*" 三处一致

## 维度 2: 流程
- ⚠️ Phase 03 准入条件 rules.md 有 / 模块工作流.md 缺

## 维度 3: 角色
- ✅ product-manager 角色 3 处都引用 .claude/agents/product-manager.md

## 维度 4: MUST/SHOULD
- ⚠️ 编码 UTF-8 BOM: rules.md MUST / 开发规范.md SHOULD → 不一致, 建议提升为 MUST

## 补丁 diff
(diff 段, 含 3 文件改动)
```

## 4. 历史
| v0.1 | 2026-05-19 | 首版; knowledge-curator 配套 3/4 |
