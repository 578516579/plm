# Proposal 0028: 编码事故触发的运行期硬规则 — JVM/HTTP/SQL UTF-8 + EFBFBD 检测（追溯）

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0028 |
| 标题 | `.claude/rules.md §D #5-7` 编码硬规则 + `plm-backend/scripts/check-encoding*.sh` 检测脚本 |
| 状态 | **merged → tracking**（追溯，User-requested-bypass per §L.2）|
| 类型 | 编码规范 + 工具链 |
| 提出人 | Wjl + Claude（reflect/2026-W20 追溯补录）|
| 提出日期（追溯）| 2026-05-16（实际实施日）|
| 提案补录日期 | 2026-05-17 |
| 评审人 | （事后追溯，无 review）|
| 实际 merged commit | `913d431 fix(encoding): force UTF-8 across HTTP/JVM/SQL and add encoding SOP` |
| Tracking 截止 | 2026-05-30 |

---

## 1. 背景

W20 周六 (05-16) 发生 **乱码事故**: Windows JDK 默认 GBK 把 HTTP 请求体里的中文字符存进 MySQL utf8mb4 列时变 `EFBFBD` 替换符。原 4 条 §D gotcha 不足以预防（gotcha 4 只覆盖前端 auto-import.ts；没覆盖 JVM file.encoding / curl 内联 -d / runtime hex 检测）。

事故根治后用户要求把"防御性硬规则"直接写进 [.claude/rules.md §D](../../.claude/rules.md):

- §D #5: 后端启动必带 4 个 `-Dfile.encoding` 标志
- §D #6: curl 中文 body 必走 `--data-binary @file.json`
- §D #7: DB 字段 HEX 含 `EFBFBD` 视为 P0 编码污染

未走 proposal — 属 §L.2 "User-requested-bypass"。同时产出 `plm-backend/scripts/check-encoding.sh` + `check-encoding-runtime.sh` + `03-开发/字符编码规范.md` 配套基础设施。

---

## 2. 证据

- 关联 signals: [2026-05.md](../signals/2026-05.md) 候选 0028 (运行期断言纳 PreToolUse hook) 与 0029 (curl --data-binary)
- 关联 事故: 周六会话的"中文 name 在 stdout 显示乱码"事件 + DB HEX 含 `EFBFBD`
- 关联 merged commit: `913d431`
- 关联 reflect: [reflect/2026-W20.md](../reflect/2026-W20.md) F-W20-02
- 实际产物: `.claude/rules.md §D #5-7` + `03-开发/字符编码规范.md` + 2 个 check-encoding shell 脚本

---

## 3. 提案（追溯描述已落地的变更）

### 实际改动文件

| 文件 | 改动类型 |
|---|---|
| `.claude/rules.md` §D | 加 #5 / #6 / #7（已落地，见当前 rules.md 第 44-46 行）|
| `03-开发/字符编码规范.md` | 新增（SOP 文档）|
| `plm-backend/scripts/check-encoding.sh` | 新增（静态扫描脚本）|
| `plm-backend/scripts/check-encoding-runtime.sh` | 新增（运行期断言脚本）|
| `plm-backend/plm-admin/src/main/resources/application*.yml` | 加 `spring.servlet.encoding` 等 servlet/http 层 UTF-8 强制 |
| `plm-backend/sql/business-*.sql` | 已显式 `DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci` |

### 已落地的 rules.md §D 实际内容

```
5. **后端启动一定带** `-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8`
6. **curl 测试含中文请求体一定走 `--data-binary @file.json`**（不能用内联 `-d '{"x":"中文"}'`，MSYS bash 会改字节）
7. 任何"DB 字段 HEX 含 `EFBFBD`"视为 P0 编码污染，先停下来排查再继续
```

---

## 4. 影响范围（已经发生的）

| 受众 | 实际影响 |
|---|---|
| Claude | rules.md 自动加载 → 后续所有会话发起 `java -jar` / `curl` 命令默认带保护参数 |
| 开发者 | CLAUDE.md "Gotchas" 1-行表已更新到 7 条 |
| 已有代码 | yml 加了 `spring.servlet.encoding`；plm-admin/.../PlmApplication.java 已确保启动入口 UTF-8 |
| 后续业务模块 | `business-*.sql` 模板已含 charset 子句 |

---

## 5. 已观察到的副作用

- **正面**: 周六事故根治后 ≥ 20 个 curl 测试 + 后续 8 个业务模块启动 0 复发
- **正面**: `check-encoding-runtime.sh` 已经能在 db 写入后核对 hex（每个 active 模块手工跑过 1 次）
- **疑虑**: `check-encoding-runtime.sh` 没纳入 PreToolUse hook 自动跑 → signals 候选 0028 提议自动化，留 W21+ proposal
- **疑虑**: 团队转入 small+ 后新成员不知道这些坑 — 需要培训 / 链接到 README

---

## 6. 备选方案（追溯记录）

- **方案 A（落地）**: 4 层防御（JVM / servlet / DB charset / curl 写法）+ 运行期检测脚本
- **方案 B**: 单纯靠 JVM `-Dfile.encoding` — 不选，已证明不够（servlet / curl 都会绕过）
- **方案 C**: docker 化把环境固定 — 不选，本机开发无法强制

---

## 7. 实施（追溯记录）

```
[x] 4 层防御落地 — `913d431`
[x] rules.md §D #5-7 更新
[x] 字符编码规范.md SOP 文档
[x] 2 个 shell 脚本
[ ] 把 check-encoding-runtime.sh 纳入 PreToolUse hook — 留独立 proposal 0030（W21）
```

---

## 8. 衡量指标（tracking 阶段观察）

| 信号 | 基线（事故前）| 目标 | 实际 W20 末 |
|---|---|---|---|
| `EFBFBD` 字节复发次数 | 1（事故）| 0 | **0**（事故修复后未复发）|
| 新模块 yml 含 `spring.servlet.encoding` 比例 | N/A | 100% | 100%（8 个 active 模块全含）|
| 新 curl 测试用 `--data-binary @file` 比例 | 0% | ≥ 90%（除非纯 ASCII） | （待 W21 抽样）|

Tracking 期: 2026-05-17 ~ 2026-05-30。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl | ✅ User-requested-bypass | 2026-05-16 | 事故紧急根治场景 |
| Claude | ✅ 实施 | 2026-05-16 | 同 commit `913d431` |

---

## 10. 实施后跟踪（已 merged）

### 实际 commit
- 合入 commit: `913d431`
- 实际 merged 日期：2026-05-16
- bypass 类型：User-requested-bypass per [rules.md §L.2 例外](../../.claude/rules.md)

### Tracking 数据

| 信号 | 基线 | 目标 | W20 末 | W21 | W22 |
|---|---|---|---|---|---|
| `EFBFBD` 复发 | 1 | 0 | **0** | 待填 | 待填 |
| §D #5-7 命中（rules 被 Claude 自动加载）| N/A | 100% | 100%（Claude 会话已自动含）| 待填 | 待填 |

### 最终判定（W22 末确认）
- [ ] done
- [ ] reverted

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-16 | Wjl + Claude | 实际实施 `913d431`（未走 proposal）|
| 2026-05-17 | Wjl + Claude | 追溯补录本 proposal（解决 §L.2 silent-merge 反模式）|
