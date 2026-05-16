# Proposal 0027: 业务模块生成器 — 一命令产出完整脚手架（追溯）

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0027 |
| 标题 | `plm-backend/scripts/new-business-module.sh` 业务模块标准模板抽取器 |
| 状态 | **merged → tracking**（追溯，User-requested-bypass per §L.2）|
| 类型 | 工具链 |
| 提出人 | Wjl + Claude（reflect/2026-W20 追溯补录）|
| 提出日期（追溯）| 2026-05-16（实际实施日）|
| 提案补录日期 | 2026-05-17 |
| 评审人 | （事后追溯，无 review）|
| 实际 merged commit | `75b3233 feat(v0.4): plm-document + Phase 06 day7 + module generator + Stage 2 placeholder` |
| Tracking 截止 | 2026-05-30（merged 后 2 周）|

---

## 1. 背景

W20 周四 (05-15) Project 模块手工抄了 ~1116 行代码做骨架。周六 (05-16) 启动 v0.2 三模块 (Req / Sprint / Task)、紧接 v0.4 六模块 (Document / 5 stubs) 时，手工抄 4 份再加 6 份 = 10 份骨架不可持续。

signals 候选 0027 (架构重构 v0.3 触发) 和 0034 (v0.4 触发) 描述同一需求: **抽取业务模块骨架到一命令生成**。

实际操作: 用户在 v0.4 启动会话中明确要求"先做生成器再继续"，Claude 直接产出 `plm-backend/scripts/new-business-module.sh`（586 行 bash 脚本，含 pom + Domain + Mapper.xml + Service + ServiceImpl + Controller + 前端 api + types + views + SQL 占位 + Gate 实例引导）。**未走 proposal 评审** — 属于 §L.2 "User-requested-bypass" 例外条款。

---

## 2. 证据

- 关联 signals: [2026-05.md](../signals/2026-05.md) 候选 0027（原描述）+ 候选 0034（v0.4 落地描述）
- 关联 merged commit: `75b3233`
- 实际产物: `plm-backend/scripts/new-business-module.sh` (586 行)
- 用户请求原话（追溯，根据 signals 修订记录推断）: "每加 active 模块手抄 ~20 文件,模板化降到 1 命令"
- 关联 reflect: [reflect/2026-W20.md](../reflect/2026-W20.md) F-W20-02

---

## 3. 提案（追溯描述已落地的变更）

### 实际改动文件

| 文件 | 改动类型 | 行数 |
|---|---|---|
| `plm-backend/scripts/new-business-module.sh` | 新增 | +586 |
| `plm-frontend/vite/plugins/auto-business-modules.ts` | 新增（Stage 2 占位）| +N |
| `plm-frontend/Stubs-Roadmap.md` | 新增（v0.4/v0.5/deferred 排期）| +N |

### 行为变化

新建一个业务模块从"手抄 20 个文件、4 小时"变成:

```bash
cd plm-backend
bash scripts/new-business-module.sh <entity>
# 自动产出:
#   plm-<entity>/{pom.xml, Domain.java, Mapper.{java,xml}, Service.{java,impl/...}, Controller.java}
#   plm-frontend/packages/business-<entity>/{api.ts, types.ts, views/...}
#   plm-backend/sql/business-<entity>.sql 占位
#   99-跨阶段/gate-checklists/instances/<entity>/Phase01-...md 占位
```

---

## 4. 影响范围（已经发生的）

| 受众 | 实际影响 |
|---|---|
| 开发者 | 新业务模块落地从 ~4h 降到 ~30min |
| 已用脚本生成的模块 | Document (合 5 stub) + 6 v0.4 stub + 周日 PRD-align 重写都基于此脚本 |
| Claude | 默认在用户要"加 XX 模块"时优先建议 `bash scripts/new-business-module.sh <entity>` |

---

## 5. 已观察到的副作用

- **正面**: v0.4 6 模块脚手架批量产出 ≈ 同等 1 小时（vs Project 模块 4 小时手工）
- **正面**: 生成的代码同 Project / Defect / TestCase 命名规范 100% 一致
- **疑虑**: 脚本占位符如果不更新会随业务模块演进 drift（当前没 versioning）
- **疑虑**: 字段对照表（PRD-MAPPING.md §2）依然要手写，脚本不能自动产出

---

## 6. 备选方案（追溯记录）

- **方案 A（落地）**: 一个大 bash 脚本，参数 = entity name，输出全套文件
- **方案 B**: Maven archetype — 不选，原因：bash 更直接，archetype 升级链长
- **方案 C**: 升级 ruoyi-bootstrap skill 把 Phase 7 模板化 — 已纳入 skill 路线图，与脚本互补

---

## 7. 实施（追溯记录）

```
[x] 写 bash 脚本（约 4h）
[x] 在 v0.4 6 模块 + 周日 PRD-align 重写中实战验证
[x] 写入 commit `75b3233`
[ ] 补 unit test / 自身回归测试 — 待 W21+ 安排
[ ] 把脚本沉淀回 ruoyi-bootstrap skill Phase 7 — 待 W22+ 安排
```

---

## 8. 衡量指标（tracking 阶段观察）

| 信号 | 基线 (W20 前)| 目标 | 实际 W21（待填）|
|---|---|---|---|
| 单业务模块脚手架耗时 | ~4h (手抄) | ≤ 30min | (待观察新模块) |
| `bash new-business-module.sh` 调用次数 | 0 | ≥ 3（W21 计划至少 3 个新模块）| (待填) |
| 命名 drift 复发 | N/A | 0 | (待填) |
| 生成代码 mvn install BUILD SUCCESS 一次过率 | N/A | ≥ 95% | (待填) |

Tracking 期: 2026-05-17 ~ 2026-05-30。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl | ✅ User-requested-bypass | 2026-05-16 | 紧急场景下事后追溯补录 |
| Claude | ✅ 实施 | 2026-05-16 | 用户明确说"先做生成器再继续" |

---

## 10. 实施后跟踪（已 merged）

### 实际 PR / commit
- 合入 commit: `75b3233`
- 实际 merged 日期：2026-05-16
- bypass 类型：User-requested-bypass per [rules.md §L.2 例外](../../.claude/rules.md)

### Tracking 数据

| 信号 | 基线 | 目标 | 实际（W20 周末）| 实际（W21）| 实际（W22）|
|---|---|---|---|---|---|
| 单业务模块脚手架耗时 | ~4h | ≤ 30min | ~30min (Document 模块作为首次实战) | （待填）| （待填）|
| 调用次数 | 0 | ≥ 3 | 7+（Document + 6 v0.4 stub）| （待填）| （待填）|

### 最终判定（W22 末确认）
- [ ] done
- [ ] reverted

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-16 | Wjl + Claude | 实际实施 `75b3233`（未走 proposal）|
| 2026-05-17 | Wjl + Claude | 追溯补录本 proposal（解决 §L.2 silent-merge 反模式）|
