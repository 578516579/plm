---
name: plm-e2e
description: PLM 项目的 Playwright E2E 自动化测试 — 启动后端/前端/Redis/MySQL 前置检查，按"冒烟→编码守门员→业务模块→全套件"分级跑测试，失败时定位排查，把通过证据落进 Phase 04 Gate 实例。当用户要"跑 E2E / 跑自动化测试 / Phase 03→04 准入 / 测乱码 / playwright test / npm run test:e2e / 模块开发完毕"时触发。
---

# plm-e2e — PLM 项目 E2E 自动化测试

本 skill 把 PLM 项目"开发完→Phase 04 准入"的 E2E 测试流程固化为可重复脚本。**真正的运行手册** 在 [`04-测试/测试用例库/E2E-运行手册.md`](../../../04-测试/测试用例库/E2E-运行手册.md)，本 skill 只负责"什么时候用、怎么排查、怎么把结果落进 Gate"。

---

## 何时触发本 skill

任何下列语义都应该自动触发：

| 触发语义 | 用户原话举例 |
|---|---|
| 显式要求 | "跑一遍 E2E"、"跑 playwright"、"npm run test:e2e" |
| 开发声明完毕 | "XX 模块开发完了"、"准备进 Phase 04"、"提测" |
| 字符编码相关 | "测一下中文"、"看看会不会乱码"、"DB 是不是 utf8" |
| 状态机变更 | "改了状态流转"、"加了反向边"、"补了 FK 校验" |
| Gate 卡控 | "Phase 03 → 04 Gate" |

不要被动等。**用户说"做完了"未跑 E2E 就推进下一步 = §G.4 违规**。

---

## 3 步快速决策树

```
用户来了。
  │
  ├─ "改了 1 行 / 改了 typo / 没改业务逻辑"
  │   → 推 `npm run test:e2e:smoke` (14 case, ~15s)
  │
  ├─ "改了某模块业务字段 / 状态机 / FK"
  │   → 推 `npm run test:e2e:<module>` + 强制 `:encoding`
  │   → 然后 `npm run test:e2e` 全套件
  │
  └─ "Phase 03 → Phase 04 准入"
      → 必须跑完 `npm run test:e2e` 全套件 + 把通过证据写进 Gate 实例 §I
```

---

## 前置检查清单（每次跑测试前自检）

按顺序，**任何一项失败先修了再跑测试**，不要带病跑：

1. **服务在跑吗？** — `netstat -ano | grep -E ":3306|:6379|:8081|:80"`，4 个端口都必须 `LISTENING`。
2. **后端带 UTF-8 标志启的吗？** — 跑 `ps -ef | grep file.encoding` 或看 `/tmp/plm-backend.log` 是否含 `-Dfile.encoding=UTF-8`。没有 = 乱码测试必挂。
3. **环境变量在吗？** — `echo "$DB_PASSWORD"` 不能为空（[helpers/db.ts](../../../plm-frontend/e2e/helpers/db.ts) 会抛错）。
4. **redis-cli 找得到吗？** — `which redis-cli` 或检查 `D:\Program Files\Redis\redis-cli.exe`（参见 [helpers/auth.ts:74](../../../plm-frontend/e2e/helpers/auth.ts) 自动探测）。
5. **`tb_*` 业务表都建了吗？** — `mysql ... -e "SHOW TABLES LIKE 'tb_%'"` 看 13 张 PRD-aligned 表（tb_project / tb_requirement / tb_sprint / tb_task / tb_defect / tb_testcase / tb_document / tb_submission / tb_release / tb_testplan / tb_testreport / tb_apidoc / tb_manual_product）齐不齐。

任何一项异常 → **停下来排查**，不要跑测试浪费 80 秒后才知道环境问题。

---

## 分级跑测策略

| 命令 | 文件 | case 数 | 耗时 | 何时用 |
|---|---|---|---|---|
| `npm run test:e2e:smoke` | encoding + navigation | ~14 | ~15s | typo / 非业务改动 |
| `npm run test:e2e:encoding` | encoding.spec.ts | 6 | ~20s | 改了 yml encoding 配置 / JDBC URL / mybatis 配置 |
| `npm run test:e2e:business` | project + requirement + sprint + task | ~28 | ~60s | 改了 4 大核心模块之一 |
| `npm run test:e2e` | **全部 spec** | **80+** | **~3min** | **Phase 03 → 04 准入（强制）** |
| `npx playwright test <file>` | 单 spec | 1-10 | <30s | 调试单个模块 |
| `npx playwright test -g <pattern>` | 按 case 名过滤 | varies | varies | 定位具体 case |

---

## 失败时排查路径

| 症状 | 第一步 | 详细 |
|---|---|---|
| `captcha 失败` / `redis 拿不到 captcha code` | `redis-cli PING` | [E2E-运行手册.md §4.1](../../../04-测试/测试用例库/E2E-运行手册.md) |
| `登录失败 验证码已失效` | 检查 5min 超时；后端日志看 401 | §4.2 |
| `DB HEX 含 EFBFBD` | **P0 阻塞**；检查后端 4 个 `-D` 标志 | [字符编码规范.md](../../../03-开发/字符编码规范.md) |
| `Locator resolved but hidden` | Element Plus 列表空；用 `.app-container` | §4.4 |
| `Test timed out` | 后端慢/锁；curl 单独验 API | §4.5 |
| `Fixture { request } from beforeAll cannot be reused` | 用 `playwright.request.newContext()` 自建 | §4.3 |

trace 文件保留：`plm-frontend/test-results/<test>/trace.zip`，跑 `npx playwright show-trace <path>` 看时间线。

---

## 把通过证据落进 Gate 实例（**§G.4 强制**）

Phase 03 完成后跑过 `npm run test:e2e` 全套件 100% 通过的话：

1. 拷贝最后一行输出（例如 `83 passed (3.2m)`）。
2. 打开对应模块的 Phase 03 Gate 实例：`99-跨阶段/gate-checklists/instances/<模块>/Phase03-开发-Gate-YYYY-MM-DD.md`。
3. 在 §I「进入 Phase 04 准出确认」段落追加：
   ```
   - [x] E2E 全套件通过：`83 passed (3.2m)` — 2026-05-17 09:32 by Claude
   - [x] encoding 套件：6/6 — DB HEX 全检无 EFBFBD
   ```
4. commit 一笔 `test(e2e): <模块> Phase 03→04 准入证据落档`。

**不要做的事**：
- ❌ 抢跑过快：套件没全绿就声明"通过"。
- ❌ 假证据：copy 历史输出贴进 Gate（hook 会拒；commit 会记 `Claude bypass` 信号）。
- ❌ 跳过 encoding：encoding 6 case 缺 1 = 视为不通过（乱码守门员是 P0 必过项）。

---

## 新增模块时

按 [`.claude/rules.md §G.4`](../../rules.md) 与 [`PRD-MAPPING.md §M.2 DoD §8`](../../../PRD-MAPPING.md) 要求：

1. 在 `plm-frontend/e2e/` 复制最近一个深度 spec（例如 [defect.spec.ts](../../../plm-frontend/e2e/defect.spec.ts) 或 [testcase.spec.ts](../../../plm-frontend/e2e/testcase.spec.ts)）为模板。
2. 必须覆盖：**CRUD 4 端点 + 状态机合法/非法 + FK 校验 + 编码 HEX 校验 + UI 菜单可达性**。
3. 如果模块有专用状态机或字典，把 fixture 提到 `helpers/fixtures-<module>.ts`（参考 [fixtures-defect.ts](../../../plm-frontend/e2e/helpers/fixtures-defect.ts)）。
4. 把新 case 登记进 [E2E-测试矩阵.md §2](../../../04-测试/测试用例库/E2E-测试矩阵.md)。
5. 在 [package.json](../../../plm-frontend/package.json) `scripts` 段加 `test:e2e:<module>` 单跑命令。

---

## 一票否决项（不允许跳过）

| 项 | 检查方式 |
|---|---|
| **编码守门员** | `npm run test:e2e:encoding` 必须 6/6 过 |
| **后端 4 个 UTF-8 标志** | `ps -ef \| grep file.encoding` 或 [`pgrep -af java`](#) |
| **DB charset = utf8mb4** | `SELECT @@character_set_database;` 必须 = `utf8mb4` |
| **任何字段 HEX 含 EFBFBD** | 视为 P0 阻塞，必须停下排查（[E2E-测试矩阵.md §3 业务点 13](../../../04-测试/测试用例库/E2E-测试矩阵.md)） |

---

## 引用文件

| 文件 | 用途 |
|---|---|
| [04-测试/测试用例库/E2E-运行手册.md](../../../04-测试/测试用例库/E2E-运行手册.md) | 命令参考 + 失败排查 + CI 集成 |
| [04-测试/测试用例库/E2E-测试矩阵.md](../../../04-测试/测试用例库/E2E-测试矩阵.md) | 当前用例总览 + 业务点对应表 |
| [04-测试/测试用例库/E2E-测试数据.md](../../../04-测试/测试用例库/E2E-测试数据.md) | RUN_ID 隔离机制 + 编码样本 + 状态机矩阵 |
| [.claude/rules.md §G.4](../../rules.md) | E2E 是 Phase 03→04 硬卡控 |
| [03-开发/字符编码规范.md](../../../03-开发/字符编码规范.md) | 乱码事故的根本规约 |
| [plm-frontend/e2e/helpers/](../../../plm-frontend/e2e/helpers/) | auth/api/db/fixtures 4 个工具模块 |

---

## 修订记录

| 版本 | 日期 | 变更 |
|---|---|---|
| v1.0 | 2026-05-17 | 首次创建：固化 E2E 流程为 Claude 可触发 skill |
