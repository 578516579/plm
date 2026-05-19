---
name: ux-flow-design
description: PLM UX 用户流程图设计 — 关键任务流 + 异常分支 + 决策点. 用户说"用户流程 / user flow / 任务流 / 流程图 / journey"时调用. 输出: 02-设计/<模块>-UX设计.md §1 用户流程. **ux-designer agent 的子工具**。
---

# ux-flow-design — UX 流程设计 skill v0.1

## 1. 何时调用
- "用户流程 / user flow / 任务流 / journey"
- ux-designer §2.1

## 2. 步骤
1. 列每个核心任务 (e.g. "创建项目" / "提交缺陷")
2. 画起点 → 决策 → 终点的流 (mermaid)
3. 标异常分支 (e.g. 验证失败 / 网络异常 / 权限不足)
4. 每步 ≤ 3 次点击 review

## 3. 输出
```markdown
## §1 用户流程
### 1.1 创建项目
\`\`\`mermaid
flowchart TD
  A[点击 "+ 项目"] --> B{已登录?}
  B -- 否 --> L[跳登录]
  B -- 是 --> C[弹表单]
  C --> D{校验通过?}
  D -- 否 --> E[显示错误]
  D -- 是 --> F[提交]
  F --> G{后端 200?}
  G -- 否 --> H[显示错误]
  G -- 是 --> I[跳详情]
\`\`\`
```

## 4. 历史
| v0.1 | 2026-05-19 | 首版; ux-designer 配套 1/4 |
