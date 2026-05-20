---
description: 跑 PLM 全套 E2E 测试 (Playwright)。前置自检 → headless 跑 → 失败定位 → 通过证据落档。
argument-hint: [模块名|smoke|encoding|business]
---

# /e2e-run — 跑 PLM E2E 测试套件

按 [plm-e2e skill](.claude/skills/plm-e2e/SKILL.md) 的流程执行。**先做前置自检**，再跑测试，跑完按结果操作。

## 输入参数（$ARGUMENTS）

| 值 | 行为 |
|---|---|
| (空) | 跑 `npm run test:e2e` 全套件（默认） |
| `smoke` | 跑 `npm run test:e2e:smoke`（encoding + navigation，~15s） |
| `encoding` | 跑 `npm run test:e2e:encoding`（6 case，~20s） |
| `business` | 跑 `npm run test:e2e:business`（4 大模块） |
| `<模块名>` | 跑 `npx playwright test <模块>.spec.ts` |

## 必做的前置自检（顺序不能跳）

1. `netstat -ano | grep -E ":3306|:6379|:8081|:80"` —— 4 个端口都 LISTENING；少任一停下报错。
2. `echo "$DB_PASSWORD"` —— 非空。如果用户没 export，让用户提供（**不要把密码写进文件**）。
3. `curl -s http://localhost:8081/captchaImage | head -c 100` —— 期望 JSON 含 `"code":200,"uuid":"..."`。
4. `curl -s http://localhost/ | head -c 100` —— 期望 HTML，不是 nginx 默认页。

任何一项异常 → 报告用户并询问"是否帮我启动 {后端|前端|MySQL|Redis}"，**不要静默跑测试**。

## 跑测试

```bash
cd plm-frontend
npm run test:e2e[:<param>]  # 按 $ARGUMENTS 替换
```

## 结果处理

### 全绿（X passed）

1. 把最后一行输出粘到回复里。
2. 询问用户："要我把这个证据写进哪个模块的 Phase 03 Gate 实例？" — 列出最近改动的模块。

### 有 fail

1. 不要慌；不要修，先排查。
2. `npx playwright show-report` 看 HTML 报告。
3. 按 [E2E-运行手册.md §4](04-测试/测试用例库/E2E-运行手册.md) 失败排查表归类：
   - DB HEX 含 `EFBFBD` → **P0 阻塞**，立即停 → 检查后端 `-Dfile.encoding=UTF-8` 4 个标志。
   - `captcha 失败` → Redis 没起或 redis-cli 不在 PATH。
   - `Test timed out` → 后端慢；单独 curl 验证 API。
   - 业务断言失败 → 大概率代码 bug，**不是测试 bug** → 列出 diff，告诉用户最近改了什么。
4. 给用户 2-3 个候选下一步，让用户选。
