---
name: orchestrator-meta-rule-audit
description: PLM 元规则审 — 检查 proposal 0040 (写前 Read / bundle / solo same-day) + 0041 (grep 现存代码) 被遵守情况. 用户说"元规则审 / proposal 0040 审 / proposal 0041 审 / meta rule audit"时调用. **self-evolution-orchestrator agent 的子工具**。
---

# orchestrator-meta-rule-audit — 元规则审 skill v0.1

## 1. 何时调用
- "元规则审 / 0040 审 / 0041 审 / meta rule audit"
- self-evolution-orchestrator §2.4
- 每月 1 次

## 2. 元规则 (来源)

### 2.1 proposal 0040 — 5 条
- §3.1 "写前 Read" (写 spec 前必先 Read 当前文件)
- §3.2 partial state 处理
- §3.3 bundle 判据
- §3.4 Sprint backlog 降级通道
- §3.5 solo same-day propose-accept-merge

### 2.2 proposal 0041 — 1 条
- §3.1 grep 现存代码 (写 spec 前 grep 4 checklist 类)

## 3. 审计方式

对每个本月新增 proposal:
- 翻 §9 评审记录, 看是否标 "已 Read 当前文件" + "已 grep 现存代码"
- 如未标, 翻提案内容看是否含 partial state / bundle / SSoT 字段映射等元规则关键字
- 与目标文件 git log 对账, 看 Edit 顺序: Read 先 / Edit 后?

## 4. 输出
```markdown
# Meta Rule Audit — YYYY-MM

## 0040 合规度
| Proposal | §3.1 写前 Read | §3.3 bundle 判据 | §3.5 same-day | 合规 |
|---|---|---|---|---|
| 0202 | ✅ (评审记录已标) | N/A (单 proposal) | ✅ | ✅ |

## 0041 合规度
| Proposal | §3.1 grep 现存代码 | 合规 |
|---|---|---|
| 0202 | ✅ (JSON 配置, 4 checkbox 标 N/A) | ✅ |

## 违反清单
- (空, 或列出违反 proposal + 整改建议)
```

## 5. 历史
| v0.1 | 2026-05-19 | 首版; self-evolution-orchestrator 配套 4/4 (完结) |
