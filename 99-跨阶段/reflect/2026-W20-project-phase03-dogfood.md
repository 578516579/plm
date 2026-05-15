# Reflect — Project 模块 Phase 03 Dogfood 复盘

> Phase A 自进化第 **2 轮** ad-hoc reflect。
> 第 1 轮在 Phase 01 → 暴露 10 处 friction → 转 0001/0002/0003 → tracking。
> 第 2 轮（本文件）在 Phase 03 → 暴露 3 处 0001-0003 未覆盖的新维度 → 转 0004/0005/0006。

---

## 头部

| 字段 | 值 |
|---|---|
| 触发场景 | Project 模块走完 Phase 03 开发 Gate + E2E 验证 |
| 执行者 | Wjl + Claude |
| 执行日期 | 2026-05-15 |
| 关联 commits | `97e7633` feat(project): scaffold + ADR adaptation / `cb195a7` E2E verification |
| 关联 Gate 实例 | [Phase03-开发-Gate-2026-05-15.md](../gate-checklists/instances/project/Phase03-开发-Gate-2026-05-15.md) |
| 上一轮反思 | [2026-W20-project-phase01-dogfood.md](2026-W20-project-phase01-dogfood.md) |

---

## 1. 观察（Observations）

### 1.1 顺手的部分 ✅

- **三维参数化模板（0001/0002/0003）立竿见影**：Phase 02 E 段豁免数从 3 降到 1，验证了第 1 轮 proposal 的有效性
- **`generateProjectNo` 落地完美**：从 PRD Q1 → Phase 02 ADR → Phase 03 实现 → E2E 验证生成 `PRJ-2026-0001`，**没有偏移**
- **状态机校验**：PRD §3.3 → API §3.3 → ProjectServiceImpl 硬编码转换矩阵 → 5/5 测试用例通过；中文 dict label 错误消息让 UX 更好
- **skill 模板渲染省 80% 时间**：9 个文件批量渲染 + sed 替换占位符；实际人工写的 < 200 行（vs 总 1116 行）
- **commit 历史叙事清晰**：每个 commit 一个明确主题，未来回看能完整复现思考路径

### 1.2 不顺手的部分 ⚠️（找到 3 处新 friction）

| # | 现象 | 影响 |
|---|---|---|
| **F-P03-01** | B.4 测试代码（Service 覆盖率 ≥ 70%）在 Phase 03 期望与 solo 单 Sprint 不现实 | 实际推迟到 Phase 04 才合理。但 Phase 03 模板写"Service ≥ 70%"是 Gate 准出条件 → 我只能 E 段豁免 |
| **F-P03-02** | C 必产出物 "Sprint 计划 / Sprint 回顾文件" 对 solo 项目造成重复 | Gate 实例 G/H/I 已含本 Sprint 工作，再产一份独立 Sprint 文档纯粹是抄一遍 |
| **F-P03-03** | B.3 "SQL 在 staging 演练成功" 对没 staging 的项目早期不适用 | 我在 E 段说"dev 环境验证"，但模板没承认这种替代方案 |

### 1.3 中间也踩了几个非流程类的坑（记录但不必转 proposal）

| 现象 | 处理 |
|---|---|
| skill 模板的 package 是"按层分包"（`...system.business.domain`），我先按 entity 分子目录建了 `system/business/project/{domain,...}` 导致包路径不匹配 | 立即 `mv` 子目录上移；同时记入 gotchas.md 候选 |
| Phase 02 设计文档（系统架构/数据库设计）描述"代码落位含 `project/` 子层"，与实际不符 | 设计文档不是 Gate 实例，可直接修订（但本次没改，作为复盘记录） |
| `mvn install` 首次失败：`Unable to rename plm-admin.jar` | 因为之前会话留下的 java PID 76520 后端进程占用 jar；杀掉后 build success |
| Redis 返回的 captcha code 带引号 `"0"`，导致 JSON 不合法 | 用 `tr -d '"'` 去引号 |
| 中文 name 在 stdout 显示乱码（`������Ŀ #1`）| 终端编码问题，数据库 utf8mb4 实际正确存储；浏览器/MySQL 客户端能正确显示 |

### 1.4 数据

- **Phase 03 实际耗时**：约 1.5 小时（含 skill 渲染、ADR 适配、SQL 写、编译验证、E2E 测试、Gate 实例填写）
- **代码净增**：1116 行（其中机械化渲染 ~900 行，手写 ~200 行）
- **commit 数**：2 个（`97e7633` 主代码 + `cb195a7` E2E 证据追加）
- **E2E 用例通过率**：5/5（generateProjectNo + 4 个状态机）
- **七项链路**：全部 ✅（MySQL/Redis/Captcha/JWT/PreAuthorize/Transactional/Log）
- **E 段豁免数**：3 处（vs Phase 02 的 1 处，反弹）

---

## 2. 诊断（Diagnoses）

### 2.1 模式 #1：模板缺少"软件成熟度 / 时机"维度

三处 friction 都源于此：

- **F-P03-01**（测试覆盖率门槛）→ 测试时机是"Phase 03 还是 Phase 04"取决于项目成熟度（早期 vs 稳定期）
- **F-P03-03**（staging 演练）→ "是否有 staging" 取决于项目部署成熟度（早期 vs 已上线）
- 这是个**新维度**（项目阶段成熟度），与"分级 / 项目类型 / 团队规模"三个现有维度正交

**根因**（5 Whys）：
1. 为什么 Phase 03 要求"Service 覆盖率 ≥ 70%"？→ 模板预设"代码合入即应附测试"
2. 为什么不允许测试后置到 Phase 04？→ 担心"测试永远拖到没人写"
3. 为什么不区分"代码骨架阶段"和"代码 + 测试稳定阶段"？→ 模板没拆 DoD 的"必须"和"应该"两类
4. 为什么不拆？→ 默认假设是"代码与测试同时产出"
5. **根因**：模板把"代码合入 DoD"和"模块上线 DoD"混在一个 Phase 03 里

### 2.2 模式 #2：solo 模式下文档冗余

- **F-P03-02** 单点
- **根因**：proposal 0002 调了"签字角色数"和"评审材料提前期"，但没动"必产出文档清单"
- 实际上 solo 模式下"Sprint 计划/回顾"应该可以**合并到 Gate 实例**，不必单独产文档

### 2.3 模式 #3：早期项目缺少专门路径

- **F-P03-03**（staging 替代）
- **根因**：proposal 0001 引入"项目类型"，但 `internal-tool` 也假设有标准的 dev → staging → prod 部署
- 早期项目（v0.1）可能只有 dev 一个环境

---

## 3. 行动（Actions）

| # | 建议 | 涉及 | 转 Proposal? | 优先级 |
|---|---|---|---|---|
| **A1** | 拆 DoD 为"代码骨架 DoD"和"模块上线 DoD"两阶段：测试码不再是 Phase 03 准出条件，而是 Phase 04 准出条件的一部分 | Phase03-开发-Gate.md §B.4 + §F + Phase04-测试-Gate.md §A | → **0004-staged-test-dod.md** | P0 |
| **A2** | solo 模式下 Sprint 计划/回顾文件不必单独产出，合到 Gate 实例 | Phase03-开发-Gate.md §C | → **0005-solo-sprint-merge.md** | P1 |
| **A3** | "项目成熟度"维度（v0.x 早期 / v1.x 稳定 / v2.x 成熟）影响 staging 演练等约束 | gate-checklists/README.md + 各 Phase 头部 | → **0006-project-maturity-stage.md** | P1 |
| A4 | 修订 Phase 02 设计文档（系统架构 / 数据库设计）里"代码落位 `system/business/project/`"的描述 → 改成与实际一致的 `system/business/{sub}/` | 02-设计/Project-系统架构.md / 02-设计/Project-数据库设计.md | 直接改即可 | P2 |
| A5 | 把"skill 模板的包路径约定"作为 gotcha 加到 [gotchas.md](../../.claude/skills/ruoyi-bootstrap/references/gotchas.md) | 文档 | 直接改即可 | P2 |
| A6 | 把"jar 文件被占用导致 mvn install fail"加到 gotchas | 文档 | 直接改即可 | P2 |

> A4/A5/A6 是文档级直接改，不需要走 proposal；A1/A2/A3 涉及流程模板改动，走 proposal。

---

## 4. 关注下一步

- [ ] 0004/0005/0006 review + accept + merge（这次比 0001-3 更快，因为流程熟练）
- [ ] **新模板首次实战 = Phase 04**：观察 0004 拆分后，Phase 04 测试 Gate 是否好用
- [ ] tracking 期延长到 W23（多吸收一轮 Phase 04 数据）

---

## 5. 链路

- 触发本反思的 commits: `97e7633` + `cb195a7`
- 由本反思衍生的 proposals: 0004 / 0005 / 0006
- 上一轮反思：[2026-W20-project-phase01-dogfood.md](2026-W20-project-phase01-dogfood.md)
- 当前 Gate 实例: [Phase03-开发-Gate](../gate-checklists/instances/project/Phase03-开发-Gate-2026-05-15.md)

---

## 6. 元复盘 — 关于"反思过程本身"

**第 2 轮 reflect 比第 1 轮更快、更聚焦**：
- 第 1 轮：花时间挖了 10 处 friction → 转 3 proposal
- 第 2 轮：3 处 friction 已经在 Phase 03 Gate §J 段当场列出，本 reflect 主要做"诊断 + 行动"

**Phase A 自进化的关键证据**：
- 不是"凭感觉" → 凭三维 tracking 指标（E 豁免数）量化定位
- 不是"全推倒重写" → 增量改（0001-3 + 0004-6，每个解决一个具体维度）
- 不是"自我宽容" → Phase 02 验证后立刻在 Phase 03 再次自查（dogfood 持续滚动）

**值得迭代的反思套路**：
1. Gate 实例中含 §Tracking + §J 章节当场识别 friction（**短期记忆**）
2. 24h 内做 reflect 把 friction 模式化（**中期沉淀**）
3. 转 proposal 走评审 merge（**长期改造**）

---

## 7. 一句话总结

**Phase A 自进化机制在第 2 轮 dogfood 中证明可持续：基础设施稳定，新维度（软件成熟度）通过 0004-6 增量补齐。预期 Phase 04 实战时 E 段豁免数能回到 ≤ 1。**
