# Proposal 0010: 前端 .d.ts ambient vs module augmentation 强制规则

> User-requested-bypass: 2026-05-20 会话用户明确指令"做成强制规范,写到规则里面,以防二次发生"。
> 按 [.claude/rules.md §L.2 例外条款](../../.claude/rules.md) 直接落地 + 事后补录本 proposal。

---

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0010 |
| 标题 | 前端 .d.ts ambient vs module augmentation 强制规则 |
| 状态 | merged(User-requested-bypass) |
| 类型 | 编码规范 |
| 提出人 | Claude(代笔)/ 用户(指令发起) |
| 提出日期 | 2026-05-20 |
| 评审人 | 用户(直接授权) |
| 评审日期 | 2026-05-20 |
| Tracking 截止 | 2026-06-10(merged 后 3 周) |

---

## 1. 背景(What's the problem?)

2026-05-20 在 `feat/ai-multi-provider-v1-v3` 分支跑 `npx vue-tsc --noEmit`,得到 227 个错误,其中 130+ 个是 `TS2305: Module '"vue"' has no exported member 'ref/reactive/computed/onMounted/createApp/App'`,另外 74 个是因 ref/reactive 推断失败派生的 `TS7006: implicit any`。

排查共花费 1+ 小时穿越 2 个错误方向(怀疑 `auto-imports.d.ts`、怀疑 vue 包损坏),最后通过"建立 src/test-vue.ts 验证 include 范围内的 vue 导入"才定位真凶:`src/types/global.d.ts` 因为顶部只有 `import type { DefineComponent } from 'vue'`(type-only import 不算 module),被 TS 判为 ambient global script,导致其中的 `declare module 'vue' { interface ComponentInternalInstance { proxy: any } }` 不是 augmentation 而是"重新声明 vue 模块",覆盖了 vue 真实的所有 named exports。

这个陷阱**在 TS 5.x + moduleResolution=bundler 下复发概率高**,且报错信息**极具误导性**(指向 vue 包/auto-imports,而真因在某个看似无关的 ambient .d.ts 文件)。若不固化为规则,下次重构 / 升级 / 新人加 shim 时几乎必然踩坑,且诊断成本同样高。

---

## 2. 证据(Evidence)

- **事故复盘**:本次 vue-tsc 227 个错误,排查耗时 1+ 小时。诊断日志显示走过 2 个错误方向才定位真因。
- **用户请求**:用户在 2026-05-20 会话中明确表达:「做成强制规范,写到规则里面,以防二次发生」(原话引用)。
- **gotcha 沉淀**:已记入 [memory/project-quirks.md Q-CODE-03](../../memory/project-quirks.md) + [memory/frontend_ts_vue_augmentation.md](../../../C:/Users/Wjl/.claude/projects/D---12-trae--06-----------plm/memory/frontend_ts_vue_augmentation.md)。
- **契约缺口**:同时发现 [开发规范.md §2.4](../../03-开发/开发规范.md) 明文要求 `common.ts 含 BaseEntity 与 PageQuery`,但代码里只有 `PageDomain` — 13 个 packages 因此连环报错,印证"规范文档与代码契约不同步"是已存在的二次复发风险。

---

## 3. 提案(What's the change?)

### 改动文件清单

| 文件 | 改动类型 | 说明 |
|---|---|---|
| `.claude/rules.md` | 新增 § P | MUST 级别强制规范:.d.ts 文件分工 + 改前必查 + 自验最小测试 + auto-imports.d.ts 边界 + 与开发规范.md 契约同步 |
| `03-开发/开发规范.md` §2.4 类型规范 | 扩展 | 新增 module vs ambient 分工规则 + 必跑自验命令 |
| `memory/project-quirks.md` | 新增 Q-CODE-03 | 完整根因 + 误判方向 + 修复路径 + 战果数据 |
| `plm-frontend/src/types/global.d.ts` | 重构 | 顶部加 `export {}`;只保留 vue augmentation + ImportMeta;移除 ambient shim |
| `plm-frontend/src/types/shims-vue.d.ts` | 新建 | Ambient 文件,放 `*.vue` 通配 + 8 个第三方库 shim(js-cookie/nprogress/file-saver/jsencrypt/sortablejs/fuse.js/vue-cropper/splitpanes/vuedraggable) |
| `plm-frontend/src/types/api/common.ts` | 新增 PageQuery 别名 | `export type PageQuery = PageDomain` 修复 13 个 packages import |

### Diff 要点

`.claude/rules.md` 加 § P 节(MUST),5 个子条款:
- P.1 .d.ts 文件分工(module vs ambient 二选一,禁止混用)
- P.2 改动 .d.ts 前必查(grep 头部 + 改完跑 vue-tsc)
- P.3 自验最小测试(临时 src/__type-check.ts 验证 vue 6 个 named exports)
- P.4 与 unplugin-auto-import 的边界(auto-imports.d.ts 是 generated,禁手改)
- P.5 开发规范契约同步(common.ts ↔ 开发规范.md 双向锁)

`03-开发/开发规范.md §2.4` 加 2 条 bullet:
- module vs ambient 分工(MUST)
- 改后必跑 vue-tsc grep TS2305 = 0

---

## 4. 影响范围(Impact)

| 受众 | 影响 |
|---|---|
| 开发者 | 改 `src/types/*.d.ts` 时需先看文件头判分类;改后必跑一条 vue-tsc 命令。习惯成本低,1 分钟。 |
| Claude | 下次会话起 § P 自动加载;遇到 `src/types/*.d.ts` 改动会主动检查并跑自验 |
| 测试 / 运维 | 无影响 |
| 已有代码 | 已修复 `global.d.ts` + 新建 `shims-vue.d.ts` + `common.ts` 加 PageQuery 别名;其他 .d.ts 不需 migration |

---

## 5. 风险(Risks)

- 风险 1:开发者忽视新规则,新加 .d.ts 时仍混用 → 现象明显(全 program TS2305),会立即被 vue-tsc 抓出来;缓解:CI 加 vue-tsc 步骤(目前 typecheck 未进 CI,见衡量指标)
- 风险 2:`shims-vue.d.ts` 改名时新人不知规则,可能误删 → 文件头注释已写明"本文件不得包含顶层 import/export";缓解:在 § P.1 表中明确文件名约定
- 风险 3:Vue 升级导致 augmentation 语法变化 → 该规则是 TS 语言层面的(module vs ambient),Vue 版本无关;缓解:无需特殊缓解
- 风险 4:本 proposal 是 User-requested-bypass,未经标准评审 → 缓解:tracking 期(3 周)内若发现新问题,可走回滚 PR

---

## 6. 备选方案(Alternatives Considered)

- **方案 A:不立规则,只修代码** — 不选。此 quirk 极易复发,下次踩坑诊断成本同样高(1+ 小时)
- **方案 B:只更新 quirks.md,不进 .claude/rules.md** — 不选。quirks 是"事后参考",rules 是"事前约束"。Claude 加载 rules 自动遵守,不再依赖人翻 quirks
- **方案 C:加 ESLint 规则强制检测 .d.ts 头部** — 暂未选。需要写自定义 ESLint plugin,工时 ≥ 4h。规则文档先行,若 tracking 期发现复发再升级到 lint
- **方案 D:把 `tsconfig.json` 改成 `moduleResolution: node16` 规避问题** — 不选。需要全项目类型/import 重审,影响面巨大;且 bundler 是 Vite 6.4 推荐模式

---

## 7. 实施计划(Implementation Plan)

```
[x] Step 1: 修代码 — global.d.ts + shims-vue.d.ts + common.ts(2026-05-20 已完成,vue-tsc 227 → 0 模板化错误)
[x] Step 2: 写规则 — .claude/rules.md § P + 开发规范.md §2.4(2026-05-20 已完成)
[x] Step 3: 沉淀 quirk — Q-CODE-03 + memory/frontend_ts_vue_augmentation.md(2026-05-20 已完成)
[x] Step 4: 补录 proposal — 本文件(2026-05-20 已完成)
[ ] Step 5: 通知团队 — 在下次周会 / Sprint 同步本规则
[ ] Step 6: 进入 tracking 期 — 至 2026-06-10
[ ] Step 7: tracking 期后评估是否升级到 ESLint 自动化(方案 C)
```

---

## 8. 衡量指标(How will we know it worked?)

- **信号 1**:tracking 期(3 周)内 `npx vue-tsc --noEmit` 报 TS2305 vue named exports 错误次数 — 基线 130+ → 目标 0
- **信号 2**:tracking 期内"因 .d.ts 模块污染"导致的 troubleshooting 会话次数 — 基线 1(本次) → 目标 0
- **信号 3**:Claude 在改 `src/types/*.d.ts` 前自觉跑 `grep -nE "^(import\|export)" <file>` 的比例 — 目标 100%(可通过 transcript 抽样验证)

跟踪期:2026-05-20 ~ 2026-06-10(merged 后 3 周)。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| 用户 | 通过(指令式授权) | 2026-05-20 | 原话:「做成强制规范,写到规则里面,以防二次发生」 |
| Claude(代笔) | 自评通过 | 2026-05-20 | 已按 §L.2 例外条款标注 User-requested-bypass |

---

## 10. 实施后跟踪(merged 后填)

### 实际 PR / commit

- PR: __(待用户 commit + push 后补录 PR URL)__
- 合入 commit: __(待 git commit 后补录)__
- 实际 merged 日期:2026-05-20(直接落地)

### Tracking 数据

| 信号 | 基线 | 目标 | 实际(周 1)| 实际(周 2)| 实际(周 3)|
|---|---|---|---|---|---|
| TS2305 vue named exports 数 | 130+ | 0 | __ | __ | __ |
| .d.ts 模块污染 troubleshoot 次数 | 1 | 0 | __ | __ | __ |
| Claude 改 .d.ts 前 grep 比例 | 未知 | 100% | __ | __ | __ |

### 最终判定

- [ ] done(达成目标,本提案归档)
- [ ] reverted(未达成 → 走回滚 PR,并在此段写"为什么失败")

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-20 | Claude(代笔)/ 用户 | 首次创建,User-requested-bypass 补录 |
