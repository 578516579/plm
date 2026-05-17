# Proposal 0302 (ADR-D): Defect 状态机统一 — 4 方分歧的口径决议

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0302 |
| 标题 | ADR-D: Defect 状态机在 PRD / 原型 / Domain / MAPPING §3 四方不一致,需统一口径 |
| 状态 | proposed |
| 类型 | 架构 |
| 提出人 | Claude (PRD-align 第二轮审计) |
| 提出日期 | 2026-05-17 |
| 评审人 | 项目负责人 + QA 角色 + 测试经理 |
| 评审日期 | _(待定)_ |
| Tracking 截止 | merged 后 2 周 |

---

## 1. 背景

`plm-defect` 模块的状态机在 4 个事实来源中各不相同:

| 来源 | 状态机 |
|---|---|
| PRD [§F4.6 L367-371](../../prd和原型/AgriAI-PLM-完整PRD文档.md) | **5 态**:发现 → 确认 → 修复 → 验证 → 关闭 |
| 原型 [defects.html L167](../../prd和原型/AgriPLM-DevOps-原型/agriplm_split/defects.html) `dem-status` select | **4 态**:待确认 / 修复中 / 待验证 / 已关闭 |
| 当前 Domain SQL `biz_defect_status` 字典 | **5 态**:新建 / 已确认 / 处理中 / 已解决 / 已关闭 (label 不同) |
| PRD-MAPPING.md §3 状态机汇总 | **6 态**:"6 态 (新建→确认→修复→验证→关闭→重开),重开是反向边" |

**4 个版本都不同**。这是 SSoT 失效的典型信号,与 Requirement 模块的"PRD 6 态 vs 原型 4 态"分歧同类。

附带:Domain 缺 `module` 字段(原型 `dem-module` 所属模块)。

---

## 2. 证据

- 审计报告 [2026-05-17-12-modules-drift-audit.md §Defect](../audits/2026-05-17-12-modules-drift-audit.md): 标 🟡 中等
- PRD [§F4.6 L367-371](../../prd和原型/AgriAI-PLM-完整PRD文档.md)
- 原型 [defects.html L167](../../prd和原型/AgriPLM-DevOps-原型/agriplm_split/defects.html) modal-newdefect + L329-345 modal-defectedit `dem-status`
- 当前 SQL [plm-backend/sql/business-defect.sql](../../plm-backend/sql/business-defect.sql) `biz_defect_status` 字典
- PRD-MAPPING.md §3 当前 defect 行
- 用户请求:2026-05-17 会话"需 ADR 统一状态机"

---

## 3. 提案

**3 个备选方案**,**推荐 Option A (原型 5 态 + 重开反向边,与 PRD-MAPPING §3 既定描述对齐)**。

### Option A — 5 态 + 重开反向边 (= MAPPING §3 当前描述 - 1 态) ⭐ **推荐**

字典 5 态 + 1 反向边:`00 待确认 / 01 修复中 / 02 待验证 / 03 已关闭` + `04 已关闭后重开` → 实际是 4 主态 + 1 状态"重开"(语义上回到 00 待确认)。

更精确表达:**4 主态 + 反向边 03→00**(重开 = 把状态从 03 已关闭打回 00 待确认,不需要独立的"重开"态)。

| dict_value | dict_label | 转换 |
|---|---|---|
| 00 | 待确认 | → {01, 03} (转入修复 / 直接关闭如重复) |
| 01 | 修复中 | → {02, 00} (修复完转验证 / 验证失败回退) |
| 02 | 待验证 | → {03, 01} (验证通过关闭 / 验证失败回修复) |
| 03 | 已关闭 | → {00} (反向边:重开 — Severity 升级或客户重报) |

- ✅ 与原型 `dem-status` 4 选项 100% 对齐 (label 完全一致)
- ✅ "重开"作为反向边而非独立态,语义清晰,数据简单
- ✅ 与 PRD-MAPPING.md §3 的"含重开"描述一致(只是 §3 误标了 6 态)
- ✅ 与 PRD §F4.6 5 态对齐(把 PRD 的"发现→确认"合并为"待确认",符合实际工作流;"修复→验证→关闭" 1:1 对齐)
- ⚠️ 需要修正 PRD-MAPPING.md §3 把 "6 态" 改为 "4 态 + 重开反向边"

### Option B — 严格 PRD 5 态(独立"发现"态)

字典 5 态:`00 发现 / 01 待确认 / 02 修复中 / 03 待验证 / 04 已关闭`。
- ✅ 与 PRD §F4.6 100% 对齐
- ❌ 与原型不一致(原型无独立"发现"态,默认新建即"待确认")
- ❌ 与现有项目惯例"原型优先"冲突(ADR-A Requirement 已经定了这个原则)
- ❌ "发现"和"待确认"区分不大,实际工作中常合并

### Option C — 现 Domain 字典 5 态(新建/已确认/处理中/已解决/已关闭)

保持现状,只改原型 + MAPPING §3 对齐 Domain。
- ✅ 改动最小(代码不动)
- ❌ 违反"原型优先"原则
- ❌ "新建/已确认" 与 PRD 的"发现/确认" 语义重复,显得啰嗦
- ❌ "已解决/已关闭" 区分不清,实际是冗余

### 改动文件清单 (按 Option A)

| 文件 | 改动 |
|---|---|
| `plm-backend/sql/business-defect.sql` | `biz_defect_status` 字典改 4 值(label 对齐原型) |
| `plm-backend/plm-defect/.../Defect.java` | 加 `module` 字段(原型 `dem-module` 所属模块) |
| `plm-backend/plm-defect/.../DefectMapper.xml` | 同步 module 字段 |
| `plm-backend/plm-defect/.../DefectServiceImpl.java` | 状态机改 4 态 + 反向边 03→00;加 module 处理 |
| `plm-backend/plm-defect/.../DefectServiceImplTest.java` (如已有) | 状态机测试更新 |
| `plm-frontend/packages/plm-defect/src/{types,views}` | 加 module 字段 |
| `plm-frontend/e2e/helpers/fixtures-defect.ts` | makeDefectData 加 module |
| `plm-frontend/e2e/defect.spec.ts` | 状态机测试用例更新 |
| `PRD-MAPPING.md §2` | 加 Defect (F4.6) 字段表 + 决策记录 D1 (ADR-D) |
| `PRD-MAPPING.md §3` | defect 行从"6 态"改为"4 态 + 反向边 03→00";F4.6 加粗 |
| `PRD-MAPPING.md §1` | defect 行 🟢 已对齐 → 🟢 **PRD-aligned** |

---

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| 开发者 | defect 模块状态机重写(类似 Requirement ADR-A 范例,中等改动)|
| Claude | ADR-D 落地后,defect 模块 PRD-align 三方一致 |
| 测试 / 运维 | E2E `defect.spec.ts` 状态机测试需更新;字典 label 变化需通知 QA |
| 已有代码 / 文档 | dev 环境 Defect 历史数据(status='00'~'04')需 UPDATE 映射:旧"新建"→新"00 待确认",旧"已确认"→新"00 待确认",旧"处理中"→新"01 修复中",旧"已解决"→新"02 待验证"(语义最接近),旧"已关闭"→新"03 已关闭" |

---

## 5. 风险

- **风险 1**: 历史数据语义映射可能损失(尤其"已解决" → "待验证" 不完全对等)。**缓解**: dev 环境 DROP+重建;生产部署前写 migration SQL + 业务方确认每条 mapping。
- **风险 2**: "重开"作为反向边而非独立态,可能让用户感到"状态机更复杂"。**缓解**: 前端 UI 在 status=03 时显式提供"重新打开"按钮 → trigger 03→00 转换 + 自动加 remark "(重开) <reason>"。
- **风险 3**: PRD §F4.6 描述的"发现"独立态被裁掉,可能某些客户需要追溯"发现 vs 确认"时间点。**缓解**: 加 `discoveredAt` / `confirmedAt` 时间戳字段(数据维度承载),不在状态机层。

---

## 6. 备选方案

详见 §3 — Option A (4 主态 + 反向边,**推荐**) / Option B (严格 PRD 5 态) / Option C (保持 Domain 现状)。

---

## 7. 实施计划

```
[ ] Step 1: 本提案 review + 评审通过 → status: accepted
[ ] Step 2: Commit 1 (docs): PRD-MAPPING §2 加 Defect 字段表 + ADR-D 决策记录;§3 状态机表修正
[ ] Step 3: Commit 2 (feat): SQL + Domain (加 module) + Mapper + Service (状态机 + 白名单) + Test + 前端 (引用 Commit 1 hash)
[ ] Step 4: Commit 3 (docs): §1 大表 defect 行 🟡→🟢 PRD-aligned;统计 28→29 (与 ADR-B/TestCase 不冲突,可并行)
[ ] Step 5: 通知 QA 团队状态机变更 + label 重命名
[ ] Step 6: 进入 tracking 期 (2 周观察重开率 + 状态分布)
```

---

## 8. 衡量指标

- **信号 1**: Defect 状态分布 (sample 4 周后,各状态占比合理 — 待确认+修复中+待验证 = active ~70%,已关闭 ~30%,反向重开 ≤5%)
- **信号 2**: PRD-align 进度 28→29 (PRD-MAPPING.md §1 统计自动反映)
- **信号 3**: E2E `defect.spec.ts` 全绿;状态机相关 case 至少 6 个(合法/非法/反向边/终态保护/重开/module 字段)

跟踪期:merged 后 2 周。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| _(待用户拍板)_ | _(通过 / 改方案 / 拒绝)_ | | |

---

## 10. 实施后跟踪

_(merged 后填)_

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-17 | Claude | 初稿,Option A 推荐 (4 态 + 反向边,与原型对齐) |
