---
name: cs-faq-builder
description: PLM FAQ 文档构建 — 高频问题 + 答案模板 + 截图. 用户说"FAQ / 常见问题 / 高频问题"时调用. 输出: 06-运营/cs-faq.md. **customer-support agent 的子工具**。
---

# cs-faq-builder — FAQ 文档构建 skill v0.1

## 1. 何时调用
- "FAQ / 常见问题"
- customer-support §2.1
- Phase 06 cycle 累 ≥ 5 类用户问题时

## 2. 结构

| 字段 | 说明 |
|---|---|
| Q | 用户问句 (用户语言, 不用技术术语) |
| A | 步骤 + 截图 + 链接 |
| Category | 登录 / 项目 / 任务 / 缺陷 / ... |
| Frequency | 月均触发次数 |

## 3. 输出模板
```markdown
# PLM FAQ

## 登录类

### Q: 忘记密码怎么办?
A: 1. 点登录页 "忘记密码"; 2. 输入邮箱; 3. 收邮件改; 4. 用新密码登录。
Category: 登录 | Frequency: 5 次 / 月

### Q: admin/admin123 登录失败?
A: 默认密码上线后已禁用, 请联系运维 (Wjl@example) 重置。
Category: 登录 | Frequency: 1 次 / 月

## 项目类
...
```

## 4. 历史
| v0.1 | 2026-05-19 | 首版; customer-support 配套 1/4 |
