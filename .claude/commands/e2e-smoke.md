---
description: 跑 PLM E2E 冒烟测试 (encoding + navigation, ~15s)。typo / 小改动后用。
---

# /e2e-smoke — E2E 冒烟测试

只跑 [encoding.spec.ts](plm-frontend/e2e/encoding.spec.ts) + [navigation.spec.ts](plm-frontend/e2e/navigation.spec.ts)。

## 用法

```bash
cd plm-frontend
npm run test:e2e:smoke
```

期望：~14 case passed，<20s。

## 何时用

- typo 修复后
- 改了 README / 文档后想确认没误伤代码
- 改了某个 utils 函数想快速回归
- **不是** Phase 03→04 准入手段 — 那个必须跑全套件 `/e2e-run`

## 失败

冒烟失败说明前置环境坏了（后端没起、Redis 没起、菜单结构破了）。按 [plm-e2e skill](.claude/skills/plm-e2e/SKILL.md) 前置检查 5 项排查。
