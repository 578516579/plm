---
description: Playwright Inspector 单步调试某个 spec。看着浏览器跑、断点调试用。
argument-hint: <spec文件> [-g <case名模糊匹配>]
---

# /e2e-debug — Playwright 单步调试

启动 Playwright Inspector 调试某个 spec。

## 用法

```bash
cd plm-frontend
npx playwright test $ARGUMENTS --debug
```

例：
- `/e2e-debug sprint.spec.ts` — 单步调 sprint
- `/e2e-debug task.spec.ts -g "F004"` — 调 task 反向边那个 case
- `/e2e-debug encoding.spec.ts -g "中文"` — 调编码守门员

## 调试技巧

1. **Playwright Inspector** 自动打开。点 ▶️ "Resume" 一次跑一行。
2. 浏览器开发者工具同时打开 — 可以在 Console 里测 selector：`document.querySelector('.app-container')`。
3. 看 Locator 高亮：每行 `await page.locator(...)` 跑时 Inspector 红框高亮。
4. 如果某 selector 不稳，改用 `getByText / getByRole`（Element Plus 常用：`getByRole('button', { name: '新增' })`）。

## 不要做的事

- ❌ debug 完别忘 commit 之前去掉 `test.only(...)`（如果加过）— 不然 CI 只跑那一个。
- ❌ 别让 Inspector 一直占着 — 会持续锁 chromium 进程。
