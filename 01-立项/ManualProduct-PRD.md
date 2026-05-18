# PRD: ManualProduct 模块 — 产品手册 (F5.1)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.2 (完整实质,2026-05-17 由 Claude 基于 9 个范本批量升级) |
| 作者 | Wjl |
| PRD § | F5.1 (AgriAI-PLM-完整PRD文档.md L392-396 产品手册 + 多格式输出) |
| 原型 HTML | [productmanual.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/productmanual.html) (5 includeModules checkbox + 截图区 + 多格式选项) |
| 评审状态 | pending (待 Phase 01 Gate 评审) |
| 关联 ADR | 反向边 02→00 由 Service 层校验 |
| 关联 OKR | _2026 Q2-O5-KR1: ManualProduct 模块上线,AI 手册生成时间 ≤ 15 分钟_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "ManualProduct (F5.1)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 团队当前产品手册撰写走 Word + 飞书文档,4 个具体问题:

1. **手册撰写耗时极长**:一份完整产品手册(系统概述 / 快速上手 / 功能详细 / FAQ / 视频教程)平均 **2-3 天**,Q1 5 个项目耗费手册工时 ≥ 80h。
2. **截图维护负担重**:UI 改动后所有手册截图都要重做,**Q1 出现过 4 次"手册截图与上线版本不一致"的客户投诉**。
3. **多格式输出能力缺位**:客户要 PDF / 网页内嵌 / H5 移动版 / Word 各种格式,**当前只能复制 Markdown 手动转**,每多一种格式多耗 1 天。
4. **手册版本无管理**:产品 v1.0 / v2.0 / v2.1 的手册混在一个文档,**客户用 v1.5 但看到 v2.1 手册,投诉"功能不一致"**。

### 1.2 目标 (北极星指标)

**目标**:6 个月内沉淀 ≥ 20 份产品手册数据,做"AI 一键生成 + 5 模块勾选 + 4 格式输出 + 版本管理"四件套。

**衡量指标**:
- **AI 手册生成时间 ≤ 15 分钟**(本期 mock 即时返回 5 模块全 Markdown)
- **手册撰写工时降 90%**(2-3 天 → 2-3 小时人工微调)
- **多格式输出覆盖率 ≥ 80%**(word/pdf/html/h5 至少 3 种被使用)
- **截图与上线版本一致率 100%**(productVersion 字段强约束)
- **客户投诉率降低 50%**(基线 Q1 4 次/季度)

### 1.3 不做的事 (Out of Scope)

本期**不做**:
- **手册多语言支持**(英文 / 简繁中)— 仅简体中文,留 v0.5+
- **截图自动抓取**(对接 UI 自动截图工具)— 仅手工上传 screenshotsUrls,留 v0.3
- **视频教程嵌入**(视频上传 + 在线播放)— 仅 includeModules 含 "video_tutorial" 字符串占位,真实视频留 v0.5+
- **手册评论 / 反馈收集** — 留 v0.3
- **手册搜索引擎 SEO 优化** — 留 v0.5+
- **手册访问统计 / 客户阅读时长分析** — 留 v0.3 走 Analytics 模块

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **产品经理 (PM)** | CRUD 自己项目的 manual | 触发 AI 生成 / 上传截图 / 选格式 |
| **技术写作 (Tech Writer)** | 协作编辑 | 微调 content / 优化 FAQ |
| **管理员** | 全 CRUD + 发布 | 评审 + 发布 + 版本管理 |
| **客户 / 用户** | 仅阅读已发布手册 | 通过 outputFormats 下载或在线看 |

### 2.2 典型场景

**S1 AI 生成产品手册**(最高频)
> PM 项目 v1.0 要发布手册 → 进入产品手册菜单 → 新建 → title "AgriPLM v1.0 用户手册" + productVersion="v1.0" + includeModules 5 项全勾(overview/quickstart/features/faq/video_tutorial)+ 上传 8 张截图 → 点 "AI 生成手册" → mock 输出 content Markdown 全文(5 模块各 1-2 段)+ aiGenerated='Y' + generatedAt=NOW() → status='00→01 生成中→02 已生成'

**S2 PM 微调 + 选格式**(关键流程)
> PM 看 mock 觉得 FAQ 部分不够具体 → 微调 → 选 outputFormats="word,pdf,h5"(3 选)→ 系统按 3 格式渲染 → 提供下载链接

**S3 评审发布**(终态)
> 管理员评审 → 改 status='02→03 已发布' → 客户可访问

**S4 反向边重新生成**(关键反向边,ADR)
> PM 发现 mock 内容不对 → status='02→00 草稿'(**反向边 02→00**,重新生成)→ 调整 includeModules 或截图后重新点 AI 生成

**S5 版本演进**(关键流程)
> 产品 v1.0 → v2.0,新建新的 manual + productVersion="v2.0",老版本 v1.0 标 status='03 已发布' 保留作历史可查

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "ManualProduct (F5.1)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: manualproductId / manualproductNo (`PM-YYYY-NNNN`) / projectId(FK 必)
- 用户输入: title / productVersion / includeModules(5 值 CSV 字典)
- 截图: screenshotsUrls(CSV URL 列表)/ screenshotsCount(冗余)
- 输出: outputFormats(4 值 CSV:word/pdf/html/h5,默认 pdf)
- AI: content(Markdown)/ aiGenerated / generatedAt
- 流程: status(4 态含反向边 02→00)/ authorUserId

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) manual-product 行:4 态含反向边 02→00。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 生成中} | 默认初始 |
| 01 | 生成中 | {02 已生成} | AI 生成 ing |
| 02 | 已生成 | {00 草稿(反向), 03 已发布} | 反向边 02→00 重新生成;终态分支 |
| 03 | 已发布 | {} | 终态;客户可见 |

**特殊规则**:
- 新建强制 `status='00'`(违反抛 601)
- includeModules 5 值字典每个值都在 overview/quickstart/features/faq/video_tutorial(604)
- outputFormats 4 值字典每个值都在 word/pdf/html/h5(604)
- 反向边 02→00 必填 reason(602,本期可放宽)
- FK 校验:projectId 必(702)
- generatedAt 服务端计算

---

## 5. AI 能力

### 5.1 AI 端点

`POST /business/manual-product/ai/generate` — 调用 §F5.1 `product-manual-flow` Dify 工作流。

### 5.2 当前阶段实现 (mock)

🟡 字段已留位(详 [PRD-MAPPING.md §6](../PRD-MAPPING.md) F5.1 行)— 本期占位 mock。

mock 输出策略(基于 includeModules + screenshotsUrls):
- overview 段:基于 projectId 关联项目名生成 1 段产品概述
- quickstart 段:3 步快速上手(注册 / 登录 / 首次操作)
- features 段:按 includeModules 输出对应章节
- faq 段:10 个标准 FAQ + 农业场景专项 3 个
- video_tutorial:视频占位链接

### 5.3 路线图

- v0.3: 真实 AI 接入 / 截图自动抓取
- v0.3: 视频教程上传 + 在线播放
- v0.5+: 多语言 / SEO / 阅读统计

---

## 6. 验收标准

**PRD §F5.1 验收**:
- ⏳ **AI 一键生成产品手册**(本期 mock 5 模块 Markdown)
- ⏳ **多格式输出**(本期 4 格式字典就位)
- ⏳ **5 模块选项**(本期 includeModules 5 值字典)

**模块特有验收**(本会话已落地):
- 4 态状态机 + 反向边 02→00 单测覆盖
- includeModules / outputFormats CSV 字典白名单(604)
- FK 校验:projectId 必(702)
- generatedAt 服务端计算

---

## 7. 不做的事 — 详 §1.3

- 多语言 / 截图自动 / 视频嵌入 / 评论 / SEO / 阅读统计

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [ManualProduct-数据库设计.md](../02-设计/ManualProduct-数据库设计.md)
- API 设计: [ManualProduct-API设计.md](../02-设计/ManualProduct-API设计.md)
- 测试计划: [ManualProduct-测试计划-2026-05-17.md](../04-测试/ManualProduct-测试计划-2026-05-17.md)
- 发布计划: [ManualProduct-发布计划-2026-05-17.md](../05-上线/ManualProduct-发布计划-2026-05-17.md)
- 原型: [productmanual.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/productmanual.html)
- AgriAI PRD: [§F5.1 L392-396](../prd和原型/AgriAI-PLM-完整PRD文档.md)
- 关联模块: [ManualImpl-PRD.md](ManualImpl-PRD.md) / [ManualOps-PRD.md](ManualOps-PRD.md)(姊妹模块) / [Document-PRD.md](Document-PRD.md)(归档)
