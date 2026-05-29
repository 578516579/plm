# Proposal 0029: 前端 *Dict.ts 与 SQL biz_*_status 字典层漂移聚合评审

---

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0029 |
| 标题 | 前端 *Dict.ts 与 SQL biz_*_status 字典层漂移聚合评审 |
| 状态 | draft |
| 类型 | 编码规范 + 架构 |
| 提出人 | Claude (Wjl 会话) · 触发自 2026-W22 solo-review 字典抽取批次 |
| 提出日期 | 2026-05-28 |
| 评审人 | 待定 — 建议 api-contract-keeper agent + 后端 owner + 前端 owner |
| 评审日期 | _(待评审)_ |
| Tracking 截止 | _(merged 后 4 周)_ |

---

## 1. 背景(What's the problem?)

2026-W22 内集中跑完 27 个前端业务模块的 `*Dict.ts` SSoT 抽取后(95% 业务模块覆盖),系统性发现**前端字典与 SQL `biz_*_*` 字典层广泛漂移**。15 个模块存在不同程度的契约/显示/结构差异,过去无人系统化记录。本 proposal 把漂移聚合分类,提出**逐项契约决策入口**:每一处漂移要么修齐(改前端 value → SQL 码 + 后端 + E2E + 存量数据迁移),要么锁现状(保留前端约定,反向同步 SQL dict_data)。

---

## 2. 证据(Evidence)

- 关联 [reflect/2026-W22-solo-review-dict-campaign.md](../reflect/2026-W22-solo-review-dict-campaign.md):整批漂移诊断的「模式 1」+ 附录 A 27 模块抽取明细表
- 关联 commit 范围:`32a92e3` (requirement 首单) ~ `42201ef` (manual-product 收官) + 并行 session 同款扫过的 task/testcase/defect/testplan/pipeline/dbdesign/dashboard
- 漂移文档化锚点(每个模块的 spec drift describe 段,可执行测试):
  - `plm-frontend/src/views/business/dora/__tests__/doraDict.spec.ts` § ⚠ drift 段(5 处契约层漂移)
  - `plm-frontend/src/views/business/document/__tests__/documentDict.spec.ts` § ⚠ drift §1+§2
  - `plm-frontend/src/views/business/feature-flag/__tests__/featureFlagDict.spec.ts` § ⚠ drift §1~§6
  - ...等 15 个 spec.ts 文件均含 drift describe 段
- 用户请求:2026-05-28 会话中明确「继续」推进抽取,事后由 Claude 总结需起 proposal 评审

---

## 3. 提案(What's the change?)

本 proposal **不直接动业务代码**,只产出**分类决策矩阵 + 推荐处置方案** 供评审,merged 后转 N 个子任务执行(每子任务 1 PR)。

### 3.1 漂移分类矩阵

| 类型 | 模块 / 位置 | 漂移描述 | 推荐处置(默认)| 影响面 |
|---|---|---|---|---|
| **A 契约层(value 错位)** | | | | |
| A1 | task / testcase / defect priority | 前端 'P0'/'P1'/'P2' 存为 value;SQL biz_*_priority value='00'/'01'/'02' | **修齐到 SQL 码**(改前端 + 后端校验 + 存量 migrate) | 大 — 含 E2E 改写 + 数据 |
| A2 | dora metric_type | 前端 'deploy_frequency'/'change_failure_rate' vs SQL 'deploy_freq'/'change_fail_rate' (2/4 错位) | **修齐到 SQL 码** | 中 — 前端单模块 + 后端 |
| A3 | feature-flag env | 前端 'dev' vs SQL biz_ff_env 'test' | **决策需求确认**:开发环境到底叫 'dev' 还是 'test'?优先改 SQL 改回 'dev'(更通用) | 小 — SQL dict_data 一行 |
| A4 | document docType | 前端 11 短缩写(prd/hld/lld/db/api/req/arch/test/manual/changelog/other) vs SQL 12 完整名(prd/arch/db_design/api_design/proposal/ued/test_plan/test_report/api_doc/manual_product/manual_impl/manual_ops) | **修齐到 SQL 全名**(前端缩写不够规范,SQL 已与 11 个 plm-* 模块对齐) | 大 — 改前端 + 后端字段约束 + 用户教育 |
| **B 显示层(value 一致,label/tag 略异)** | | | | |
| B1 | competitive | enterprise label 「企业」(前) / 「企业级」(SQL);status '02' tag warning(前) / danger(SQL)| **走 UED 评审**,统一文案 + 颜色后改一边 | 小 — 1-2 文件 |
| B2 | ai-agent provider | mock/dify label 「Mock」/「Dify」(前) / 「Mock 占位」/「Dify 编排」(SQL)| **保留前端简化**(SQL dict_data 改对齐 — 「占位」「编排」过于细节) | 小 |
| B3 | testreport risk | 「🟢 绿灯」(前) / 「绿 (低风险)」(SQL)| **走 UED 评审**(emoji vs 描述文体)| 小 |
| B4 | release / document status '03/04' tag | 前端 danger / SQL list_class 空 | **SQL 补 list_class='danger'**(SQL 漏定义)| 小 — 4 模块 SQL |
| B5 | submission status '01' vs '02' tag | 前端 warning vs primary 与 SQL primary vs warning **互换** | **决策诊断哪边对**:推测前端早期错配;由 UED 给"已提交/质量门禁中"的颜色语义,改另一边 | 小 |
| B6 | arch archMode/stack label | 「微服务/Java SB3」(前) / 「微服务架构/Java (SpringBoot3)」(SQL)| **保留前端简化**(列表空间有限,SQL dict_data 改对齐)| 小 |
| **C 结构性(SQL dict 未被前端承载)** | | | | |
| C1 | dora biz_dora_status (3) | SQL 定义但前端 dora/index.vue 不渲染 status | **决策 UI 需求**:dora 是否需要 status?如不需要,SQL 删 biz_dora_status;如需要,前端补 status 渲染 + STATUS dict | 中 |
| C2 | dora biz_dora_period (2) | SQL 定义但前端用裸 month/quarter 字面量 | **前端补 PERIOD dict**,渲染 label | 小 |
| C3 | feature-flag biz_ff_status (2) | SQL 定义但前端用 rolloutMode 渲染 | **决策**:status vs rolloutMode 是同事 or 互补?若同事,删 status;若互补,前端补承载 | 中 |
| C4 | dora LEVEL(前端独有 elite/high/medium/low)| SQL 无 biz_dora_level + 表无 level 列 | **决策**:level 是 derived(前端态计算)还是 stored(应补 SQL 字段)?推测应补字段 | 中 — 改 DDL |
| C5 | PRD biz_prd_scene / biz_prd_target_user(各 4/3) | SQL 有 list_class 但前端仅 form 静态 option | **前端补 PRD_SCENE/PRD_TARGET_USER dict**,展示页用 tag 渲染 | 小 |

### 3.2 处置优先级

**P0(影响数据正确性,必须修)**:A1 / A2 / A4 / C4
**P1(影响 UX 一致性,UED 评审)**:B1 / B3 / B5
**P2(SQL 一边补/改即可,小工作量)**:A3 / B2 / B4 / B6 / C2 / C5
**P3(决策悬而未决,需 UI/产品确认)**:C1 / C3

### 3.3 改动文件清单(merged 后子任务展开)

| 子任务 # | 涉及文件 | 改动类型 |
|---|---|---|
| 0029.S1 | 各模块 `*Dict.ts` + `index.vue` + `api/business/*.ts` + 后端 Service + E2E spec | A1 priority 契约修齐(3 模块同范式)|
| 0029.S2 | dora 全套 + business-dora.sql dict_data | A2 metric_type 码修齐 |
| 0029.S3 | feature-flag + business-feature-flag.sql | A3 env value 决策 |
| 0029.S4 | document 全套 + business-document.sql + 用户教育 | A4 docType 修齐到 SQL 全名 |
| 0029.S5 | 各 P1/P2 子项 SQL/UED 修齐(批量小 PR)| B/C 系列 |
| 0029.S6 | PRD-MAPPING.md §6 新增「漂移矩阵」段 | 文档化 |

---

## 4. 影响范围(Impact)

| 受众 | 影响 |
|---|---|
| 前端开发 | A1/A2/A4 三处需改 form 默认值 + E2E 断言;其余子项工作量轻 |
| 后端开发 | A1 需改 Service 校验(从 'P1' 改 '01');C4 需 DDL `ALTER TABLE tb_dora_metric ADD COLUMN level VARCHAR(20)` |
| 测试 | E2E spec 中所有 priority='P1' 类断言需改 '01';task.spec.ts / defect.spec.ts / testcase.spec.ts 共 ~10 case 影响 |
| 数据 | A1 需对 tb_task / tb_testcase / tb_defect 已有数据做 UPDATE migrate(P0→00/P1→01/P2→02);A2 dora 同理 |
| 已有 Dict.ts spec | 修齐后,spec 中 "⚠ drift" describe 段改为 "✓ 已对齐" |
| Claude | rules.md §M 可加「前端 *Dict.ts 必须先校 SQL biz_* dict_data 一致后再 commit」节 |

---

## 5. 风险(Risks)

- 风险 1:**存量数据 migrate 风险**(P0 类):tb_task 等已有数据若 priority 是 'P0/P1/P2',要批量 UPDATE 改成 '00/01/02';若 migrate SQL 写错,数据可能错位。缓解:每子任务 PR 必须含 dry-run + rollback SQL,审查至少 2 人。
- 风险 2:**E2E 断言广泛改写**(A1 影响 ~10 case + 字段 alignement):风险是改 spec 时遗漏,导致 false-green。缓解:每子任务 PR merge 前跑 E2E 全套件 + diff stat 审 spec 改动行数。
- 风险 3:**部分决策需要 UI/产品 owner 拍板**(C1/C3/C4),proposal 卡在评审。缓解:proposal 不阻塞,可先跑 P0 + P2 系列,P1+P3 延后到 UED 月会决策。
- 风险 4:**子任务 PR 数量多(预估 6-10 PR)**,merge 顺序协调繁琐。缓解:用 [在途任务.md](../在途任务.md) 台账每 PR 单行追踪,merge 完成挪到「已完成」段。

---

## 6. 备选方案(Alternatives Considered)

- **方案 A:不修漂移,把漂移文档化作为「已知未决」永久标记**(reflect spec drift describe 段保留)。
  - 不选原因:漂移会持续误导新开发(看代码以为 P1 是 value,看 SQL 又以为 01 是 value),技术债越积越多;且 A1/A4 类契约漂移会导致 `selectDictLabel('biz_*', 'P1')` 永远返回 undefined,反过来让 dict 机制失效。
- **方案 B:一次性大 PR 修齐所有 15 处**。
  - 不选原因:风险集中、review 不可承受、回滚困难;且 P1/P3 决策悬而未决,会卡住整个大 PR。
- **方案 C:反向修齐(改 SQL 对齐前端,所有 SQL dict_data 改成前端 value)**。
  - 部分子项采用(B2/B4/B6);A 类不采用,因为 SQL 码是规范侧 SSoT,改前端比改 SQL+数据更稳。

---

## 7. 实施计划(Implementation Plan)

```
[ ] Step 1: proposal 评审(api-contract-keeper agent + 后端 owner + 前端 owner + UED owner)
[ ] Step 2: 评审通过 → 拆 6-10 个子任务(0029.S1 ~ 0029.S6),每子任务挂 PR
[ ] Step 3: 子任务并行/串行执行,顺序建议:S6 文档先行 → S2 dora(小)→ S1 priority(大)→ S4 document(大)→ S3 env + S5 P1/P2 批量
[ ] Step 4: 每子任务 PR merge 前必跑 E2E 全套件 + 单元测试
[ ] Step 5: 全部子任务 merged → 27 *Dict.ts spec 中 "⚠ drift" describe 改 "✓ aligned"
[ ] Step 6: 更新 PRD-MAPPING.md §6 漂移段为「漂移已清,2026-W22 ~ W2N」标记
[ ] Step 7: 通知团队(飞书 / 周报)
[ ] Step 8: 进入 tracking 期(merged 后 4 周观察)
```

---

## 8. 衡量指标(How will we know it worked?)

- 信号 1:**`selectDictLabel('biz_*_*', <stored_value>)` 命中率从 ~60% 提升到 ≥95%**(基于 tb_task / tb_dora 等表的真实 priority/type 列值统计)
- 信号 2:**漂移测试从 15 个 describe 减到 ≤3 个**(剩下 3 个允许是 UED 评审后保留的显示层差异,有正当理由)
- 信号 3:**新模块开发 dict 漂移率**:tracking 期内新加业务模块的 Dict 文件初次 commit 即与 SQL 对齐(spec 不带 drift describe),目标比例 ≥80%
- 信号 4:**生产 bug ticket** 中"字典码错位/label 不对"类 0 起(基线:本批前 P0-1b 阶段产生过 3 起类似 bug)

跟踪期:_(待 merged 起 4 周)_。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| | _(待评审)_ | | |

---

## 10. 实施后跟踪(merged 后填)

### 实际 PR / commit

- PR: _(待开)_
- 合入 commit: _(待开)_
- 实际 merged 日期: _(待开)_

### Tracking 数据

| 信号 | 基线 | 目标 | 实际(周 1)| 实际(周 2)| 实际(周 4)|
|---|---|---|---|---|---|
| selectDictLabel 命中率 | ~60% | ≥95% | | | |
| spec drift describe 数 | 15 | ≤3 | | | |
| 新模块 dict 漂移率 | _(N/A 历史)_ | ≥80% 对齐 | | | |
| 字典码错位 bug 数 | 3(P0-1b 期)| 0 | | | |

### 最终判定
- [ ] done(达成目标,本提案归档)
- [ ] reverted(未达成 → 走回滚 PR,并在此段写"为什么失败")

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-28 | Claude (Wjl 会话) | 初稿:沉淀 2026-W22 solo-review 抽取批次的 15 处漂移分类矩阵 + P0~P3 处置优先级 + 6-10 子任务实施计划 |
| 2026-05-28(后)| Claude (Wjl 会话) | **附加 root-cause 诊断**:本 proposal 自身 README 索引登记 commit `656a6a4` 二次重现 3ae00fd 同款误吞(12 文件 / 418 insertions,本应 1 文件)。`pre-commit` hook 无 `git add`、git config 无 `commit.all`、无 alias。最可能机制 = **同 working tree 与并行 session 并发 `git add` 的 TOCTOU 竞态**:`git add <path>` 与后续 `git commit` 之间,并行 session 的 `git add` 把它的 M 文件加进同一索引,我的 `git commit` 把整个索引提交。**修复方案**(应纳入协作规范 §3 +本 proposal 0029 子任务 0029.S0):用 `git commit --only <path>` / `git commit <path>` 显式只提交指定路径,绕过共享索引;或改 worktree 物理隔离。两次事故 commit 留作 git 历史佐证:`3ae00fd`(27 文件 / 827 ins)+ `656a6a4`(12 文件 / 418 ins)。|
