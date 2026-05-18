# PRD: UED 模块 — UED 设计协同 (F2.3)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.2 (完整实质,2026-05-17 由 Claude 基于 9 个范本批量升级) |
| 作者 | Wjl |
| PRD § | F2.3 (AgriAI-PLM-完整PRD文档.md §F2.3 UED 设计协同) |
| 原型 HTML | [ued.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/ued.html) (openFigmaSync + uedVersions + uedReview + 农业 UI 组件库标签) |
| 评审状态 | pending (待 Phase 01 Gate 评审) |
| 关联 ADR | — |
| 关联 OKR | _2026 Q2-O2-KR3: UED 设计稿评审 AI 评分均值 ≥ 80,设计-开发返工率降 30%_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "Ued (F2.3)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 团队设计师与开发的 Figma 协同走"分享链接 + 飞书消息",4 个具体问题:

1. **Figma 设计稿散落无版本管理**:同一界面经常有 "v1 / v2_final / v2_final_真的_final" 3 个 Figma 文件,**开发不知道哪个是最新的**,Q1 出现过 2 次开发实现的版本与最终设计稿不一致需要重做。
2. **设计规范遵从度无数据**:间距/颜色/字体是否遵从 design tokens 全靠人肉看,**Q1 上线后 UE Review 提出 27 处不规范点**,设计师重新调整 + 开发重新实现耗费 12 工时。
3. **可用性问题事后才发现**:农业场景下"农户在 IoT 大屏上要单手操作 / 手套触屏"等专项可用性问题,**只在用户测试时发现**(已经上线),返工成本高;原型 ued.html 提到的"农业 UI 组件库"(大字大按钮/IoT 设备图标等)无沉淀。
4. **AI 设计评审能力缺位**:原型有 uedReview 区域和评分卡(reviewScore 0-100),但**当前所有评分都是手填**,没有 AI 自动对比设计规范的能力。

### 1.2 目标 (北极星指标)

**目标**:6 个月内沉淀 ≥ 80 份 UED 设计稿数据,做到"Figma 同步 + AI 评审 + 农业组件标签"三件套,设计-开发返工率降 30%。

**衡量指标**:
- **UED AI 评审分数均值 ≥ 80**(本期 mock 固定 82.0)
- **Figma URL 关联率 ≥ 90%**(每份 UED 必填 figmaUrl)
- **农业组件标签使用率 ≥ 50%**(`agriComponentTags` 至少 1 个)
- **可用性问题发现前置率 ≥ 60%**(设计评审阶段发现 vs 上线后发现)
- **设计-开发返工率降 30%**(基线 Q1 27 处不规范,目标 ≤ 19 处)

### 1.3 不做的事 (Out of Scope)

本期**不做**:
- **Figma MCP 双向写回**(AI 评审建议直接改 Figma 文件)— 仅单向同步 Figma → PLM,留 v0.3
- **设计稿 → 代码自动生成**(类 Figma to Code)— 留 v0.5+,本期 designer 仍需开发实现
- **可用性测试录屏分析**(集成 Hotjar/FullStory)— 留 v0.5+
- **设计规范库管理**(让团队维护自己的 design tokens)— 仅消费 design tokens,管理留 v0.3 走独立模块
- **农业 UI 组件库源码**(开源/Figma 库)— 仅 agriComponentTags 标签字段承载,组件源码留 v0.3 走前端工程
- **多设计稿对比 Diff**(同一界面 v1 vs v2 像素级 Diff)— 留 v0.3

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **UED 设计师 (designer)** | CRUD 自己项目下的 UED | 上传 Figma URL / 维护版本 / 触发 AI 评审 |
| **评审人 (reviewer)** | 评审 + 决策(02 已确认 / 01→00 打回) | 走完整评审流 |
| **开发 (Dev)** | 查看 + 评论 | 用 figma url + 标注作为实现依据 |
| **产品经理 (PM)** | 查看 + 评论 | 验证设计是否满足需求(关联 requirementId) |

### 2.2 典型场景

**S1 Figma 同步设计稿**(最高频)
> 张设计师完成 "AgriPLM 工作台首页 v2.0" → 进入 UED 菜单 → 新建 → title "AgriPLM 工作台 v2.0" + figmaUrl="https://figma.com/file/xxx" + figmaFileKey="xxx" + versionLabel="v2.0" + previewUrl(自动生成缩略图) + agriComponentTags="big_button,iot_chart,touch_optimized" → 保存 → status='00 草稿' → 系统通过 Figma MCP API 拉取标注内容(annotationContent JSON)

**S2 AI 评审**(关键流程,F2.3 核心)
> 张设计师点 "AI 评审" → POST /business/ued/ai/review/{id} → mock 输出 reviewReport(7 段:整体评价/规范遵从/可用性/农业适配/优势/改进建议/分数)+ reviewScore=82.0 + complianceCheck(JSON {间距:Y, 颜色:Y, 字体:N, 农业大按钮:Y}) + usabilityIssues("登录按钮宽度 < 44pt 不利单手操作")

**S3 PM 联动需求验证**(关键流程)
> 王 PM 把本 UED 关联到 requirementId=REQ-89(AI 灌溉推荐引擎)→ 验证"设计稿是否覆盖需求场景" → 评论 "建议增加灌溉量大数字显示"

**S4 评审打回**(反向边路径)
> reviewer 发现 "AI 评分 82 但农业适配项分数 70(< 80) " → 改 status='01→00 草稿'(**反向边**)+ 备注 "需要更明确的 IoT 大屏适配设计" → 张设计师修改后重新提交

**S5 已确认 → 开发认领**(终态分支)
> 评审通过 → status='01→02 已确认' → 开发参照本 UED 实现 → 完成后 UED 标 status='02→03 已归档'

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "Ued (F2.3)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: uedId / uedNo / projectId(FK 必)
- Figma 集成: figmaUrl / figmaFileKey(MCP 集成入口)/ previewUrl / versionLabel
- 用户输入: title / annotationContent(JSON 标注:间距/颜色/字体)/ agriComponentTags(CSV 4 种:big_button/iot_chart/touch_optimized/offline_friendly)
- AI 输出: reviewReport(Markdown)/ reviewScore(0-100)/ complianceCheck(JSON)/ usabilityIssues
- 关联: requirementId(可空 FK)/ designerUserId / reviewerUserId
- 流程: status(4 态)/ aiGenerated / aiGeneratedAt

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) ued 行:同 arch 4 态模式。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 评审中} | 默认初始 |
| 01 | 评审中 | {00 草稿(打回), 02 已确认} | 反向边 01→00 评审打回 |
| 02 | 已确认 | {03 已归档} | 终态分支;开发可参照实现 |
| 03 | 已归档 | {} | 终态 |

**特殊规则**:
- 新建强制 `status='00'`(违反抛 601)
- 反向边 01→00 必填 reviewNote(602)
- agriComponentTags 字段白名单校验(604,4 值合法)
- FK 校验:projectId 必,requirementId 可空但若填必须存在(702)
- reviewScore 由 ai/review 端点计算,不接受前端写入

---

## 5. AI 能力

### 5.1 AI 端点

`POST /business/ued/ai/review/{id}` — 调用 §F2.3 `ued-review-flow` Dify 工作流。

### 5.2 当前阶段实现 (mock)

🔴 未实现(详 [PRD-MAPPING.md §6](../PRD-MAPPING.md) F2.3 行)— 本期占位 mock。

mock 输出结构:
- reviewReport(Markdown 7 段:整体评价/规范遵从/可用性/农业适配/优势/改进建议/分数依据)
- reviewScore mock 固定 82.0
- complianceCheck JSON:`{"spacing":"Y","color":"Y","font":"N","agri_big_button":"Y","iot_chart_clarity":"Y"}`
- usabilityIssues:基于 agriComponentTags 反推潜在问题(如缺 `touch_optimized` 则提示"未做单手适配")

### 5.3 路线图

- v0.3: Figma MCP 双向集成(读 Figma 设计稿元素 + AI 写评审建议回 Figma 评论)
- v0.3: design tokens 自动对比(对比设计稿与设计规范库)
- v0.5+: Figma to Code(直接出 Vue/React 组件代码)

---

## 6. 验收标准

**PRD §F2.3 验收**:
- ⏳ **Figma 设计稿同步**(本期 figmaUrl + figmaFileKey 字段就位,MCP 拉标注内容 mock)
- ⏳ **AI 设计评审报告 + 评分**(本期 mock 82.0)
- ⏳ **农业组件库标签 4 种**(本期 agriComponentTags 字典就位)

**模块特有验收**(本会话已落地):
- 4 态状态机合法转换 + 反向边 01→00 单测覆盖
- agriComponentTags 4 值白名单(604)
- reviewScore 服务端计算,前端写入被忽略
- FK 校验:projectId 必、requirementId 可空(702)
- 反向边必填 reviewNote(602)

---

## 7. 不做的事 — 详 §1.3

- MCP 双向写回 / 自动生成代码 / 录屏分析 / 设计规范库管理 / 农业组件源码 / 多版本 Diff

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Ued-数据库设计.md](../02-设计/Ued-数据库设计.md)
- API 设计: [Ued-API设计.md](../02-设计/Ued-API设计.md)
- 测试计划: [Ued-测试计划-2026-05-17.md](../04-测试/Ued-测试计划-2026-05-17.md)
- 发布计划: [Ued-发布计划-2026-05-17.md](../05-上线/Ued-发布计划-2026-05-17.md)
- 原型: [ued.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/ued.html)
- AgriAI PRD: [§F2.3](../prd和原型/AgriAI-PLM-完整PRD文档.md)
- 关联模块: [Requirement-PRD.md](Requirement-PRD.md)(UED.requirementId FK)/ [Prd-PRD.md](Prd-PRD.md)
