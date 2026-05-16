# Proposal 0029: curl 中文请求体必走 `--data-binary @file.json`（追溯）

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0029 |
| 标题 | curl 测试含中文请求体禁止使用内联 `-d '{...}'`，必须 `--data-binary @file.json` |
| 状态 | **merged → tracking**（追溯，与 0028 同次 commit 落地的子规则）|
| 类型 | 编码规范 |
| 提出人 | Wjl + Claude（reflect/2026-W20 追溯）|
| 提出日期（追溯）| 2026-05-16 |
| 提案补录日期 | 2026-05-17 |
| 实际 merged commit | `913d431` |
| Tracking 截止 | 2026-05-30 |

---

## 1. 背景

0028 的子规则之一，但拆出独立 proposal 是因为本条仅 Claude / 测试同学需要遵守（影响面不同于 0028 的 JVM/HTTP/SQL 全栈防御）。

W20 周六事故发现根因之一: **MSYS bash 在执行 `curl -d '{"name":"中文"}'` 时会按 GBK 解释内联参数字节**，即便 JVM/HTTP/SQL 全 UTF-8，进入 curl 那一步已经被改字节。换成 `--data-binary @file.json` 后 curl 不再 reparse，按字节流传送。

---

## 2. 证据

- 关联 signals: [2026-05.md](../signals/2026-05.md) 候选 0029
- 关联 事故: 周六 curl POST `/business/project` body 含中文 name 报 `EFBFBD` 入库
- 关联 reflect: [reflect/2026-W20.md](../reflect/2026-W20.md) F-W20-02
- 实际 rules.md 落地: §D #6 当前内容

---

## 3. 提案（追溯描述）

### 已落地的 .claude/rules.md §D #6

```
6. **curl 测试含中文请求体一定走 `--data-binary @file.json`**（不能用内联 `-d '{"x":"中文"}'`，MSYS bash 会改字节）
```

### 配套（已落地）

- `03-开发/字符编码规范.md` 含"curl 安全调用 SOP"段，含示例
- E2E 测试和 manual smoke test 改用 `--data-binary @<tmp>.json`（参考 `e89cdfb test(project): add playwright e2e browser tests` 的 fixtures）

---

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| Claude | 自动加载 rules.md，下次产 curl 命令时默认 `--data-binary @file` |
| 测试 / smoke test | 旧脚本如有 `-d '{"中文"}'` 需要改成 `--data-binary @body.json` |

---

## 5. 已观察到的副作用

- **正面**: 周六事故修复后 ≥ 20 个 curl 测试 + 后续业务模块 0 复发
- **疑虑**: 纯 ASCII body 仍可用 `-d`（rules 没禁），但人难判断"哪些纯 ASCII"，可能误用 — 缓解：在 SOP 文档明示。

---

## 6. 备选方案

- **方案 A（落地）**: rules.md 写硬规则，配 SOP 文档
- **方案 B**: 提供一个 `safe-curl.sh` wrapper 强制走 file — 不选，原因：增加学习成本
- **方案 C**: 全部测试改 jmeter / playwright — 已并行做（playwright e2e suite），但 manual curl 仍是 first-line tool

---

## 7. 实施（已完成）

```
[x] rules.md §D #6 写入 — `913d431`
[x] 字符编码规范.md 含 SOP — `913d431`
[x] playwright fixtures 用 file-based body — `e89cdfb`
[ ] 抽样 W21 新增 curl 测试，验证 90%+ 用 `--data-binary` 形式 — tracking 期任务
```

---

## 8. 衡量指标

| 信号 | 基线 | 目标 | W20 末 |
|---|---|---|---|
| `curl ... -d '...中文...'` 调用次数 | 1（事故）| 0 | **0**（修复后零复发）|
| Claude 主动产出 curl 命令中含 `--data-binary @` 的比例（中文 body）| N/A | 100% | 100%（rules 已加载）|

Tracking 期: 2026-05-17 ~ 2026-05-30。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl | ✅ User-requested-bypass | 2026-05-16 | 与 0028 同次落地 |

---

## 10. 实施后跟踪

### 实际 commit
- 合入 commit: `913d431`（与 0028 同次）
- 实际 merged 日期：2026-05-16
- bypass 类型：User-requested-bypass per rules.md §L.2

### Tracking 数据

| 信号 | 基线 | 目标 | W20 末 | W21 | W22 |
|---|---|---|---|---|---|
| 中文 body `-d` 误用 | 1 | 0 | 0 | 待填 | 待填 |

### 最终判定
- [ ] done
- [ ] reverted

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-16 | Wjl + Claude | 实际实施（与 0028 同次 commit）|
| 2026-05-17 | Wjl + Claude | 追溯补录本 proposal |
