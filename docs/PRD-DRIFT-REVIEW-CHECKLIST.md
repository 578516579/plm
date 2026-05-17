# PRD 对齐 — Review Checklist

> 这份 checklist 用来 review 我跟原型 1:1 对齐的 7 个 Vue 页面是否真的对得上。
>
> 用法: 一边开原型 HTML (`prd和原型/AgriPLM-DevOps-原型/agriplm_split/*.html`),
>      一边开我的 Vue (`plm-frontend/src/views/business/*/index.vue`),
>      逐项打勾。看到 ☐ 没勾的就是没对上、要让我修。
>
> 如果要 live 跑起来看,先把 commit `00c35fa` 的 `business-prd-align-menus.sql`
> 灌到 plm 库 (需要 DB_PASSWORD), 然后启动后端 + 前端,登录后侧边栏会出现这些菜单。

---

## 1. inception (项目立项) — commit `3f19cd1`

**原型**: `inception.html` + `agriplm.js` line 696-729 (`runInceptionAI`)
**我的**: `plm-frontend/src/views/business/inception/index.vue`

### 布局对应
| 原型元素 | 我的实现 | 检查 |
|---|---|---|
| 顶部 `<div class="ph">` 标题 "项目立项" + 副标题 "AI辅助完成立项建议书" | `<div class="pt">` "项目立项" + `<div class="ps">` 副标题 | ☐ |
| 右上 `btn-ai` "✨ AI生成立项建议书" | 顶部右 button-group: "📁 我的立项单" + "📝 新建立项" (AI 按钮挪左栏) | ☐ |
| 左栏 card "📝 立项信息录入" | `<el-card>` 左栏第一张 | ☐ |
| 项目名称 input #inc-name (默认值 "农业病虫害智能识别...") | `form.projectName` el-input,placeholder 同 | ☐ |
| 业务线 select #inc-biz (4 选项 植保/精准/农资/质量) | `form.businessLine` el-select 接 biz_inception_biz_line | ☐ |
| 项目类型 select #inc-type (4 选项 新产品/迭代/重构/平台) | `form.inceptionType` 接 biz_inception_type | ☐ |
| 背景 textarea #inc-bg | `form.background` el-input type=textarea rows=5 | ☐ |
| 预计工期 #inc-dur (默认 6) | `form.estimatedDurationMonths` el-input-number 默认 6 | ☐ |
| 预计团队 #inc-team | `form.estimatedTeam` el-input | ☐ |
| 左栏 "AI风险识别" card 条件显示 | `<el-card v-if="risks.length > 0">` ⚠️ AI 风险识别 | ☐ |
| 右栏 "📋 立项建议书预览" card | 右栏 `<el-card>` 标题相同 | ☐ |
| 右栏未生成 placeholder "🚀 点击..." | `v-if="form.aiGenerated !== 'Y'"` div 文案相同 | ☐ |
| AI 生成后渲染 4 段 `<h4>`: 项目背景/市场机会/ROI预估/建议决策 | `<h4>` 一/二/三/四 + 内容渲染 form.aiBackground 等 | ☐ |

### AI 输出对齐 (点 "AI 分析并生成立项建议书" 后)
| 原型值 | 我的填充 | 检查 |
|---|---|---|
| 一、项目背景: "20-30% 减产, 经济损失超千亿元" | form.aiBackground 含同样数字 | ☐ |
| 二、市场机会: 580 亿元市场, 8% 渗透率, 95%+ AI 准确率 | form.aiMarketOpportunity + el-statistic 580 亿/8% | ☐ |
| 三、ROI 预估: 180 万开发成本/3000 万首年营收/16.7 倍 ROI | form.aiRoiEstimate + 3 个 el-statistic 卡 | ☐ |
| 四、建议决策: "✅ 建议立项, P1, Q3 启动, 分3期" | form.aiRecommendDecision + el-alert success + 3 el-tag | ☐ |
| 风险 1 (warning): 数据集风险 — 训练数据可能不足 | risks[0] 黄色块 + 数据集风险 | ☐ |
| 风险 2 (critical): 监管合规风险 — 农药推荐需资质 | risks[1] 红色块 + 监管合规 | ☐ |

### 交互
| 原型行为 | 我的实现 | 检查 |
|---|---|---|
| `incApprove()` → notify "立项申请已提交审批, 将通过飞书推送" + 0.5s 跳 competitive | 状态 00→01 update + 0.5s `$router.push('/business/competitive')` | ☐ |
| 状态 03 (已批准) → 可"转项目" | `<el-button v-if="form.status === '03'">` "✅ 转项目" | ☐ |
| 状态 04 (已驳回) → 打回重写 (反向边 04→00) | `reworkRejected()` 把 status 改回 '00' | ☐ |

---

## 2. prd (AI PRD 生成) — commit `9b01816`

**原型**: `prd.html` + `agriplm.js` (`generatePRD`)
**我的**: `plm-frontend/src/views/business/prd/index.vue`

| 原型元素 | 我的实现 | 检查 |
|---|---|---|
| 左栏 form 5 字段 | `el-form` 5 字段: projectId/title/description/sceneTemplate/targetUser | ☐ |
| 右栏 PRD 预览 `<div class="prd">` 4 段 | 4 个 `<h4>` | ☐ |
| `<h3>` PRD 标题 含 v1.0 | `📄 {form.title} · PRD v{form.version || '1.0'}` | ☐ |
| 一、背景与目标 含 20% 灌溉用水节约 | form.aiBackground 含 "20%" | ☐ |
| 二、用户故事 (3 条 "作为...我想要...以便...") | parseUserStories 渲染 3 个灰色块 "作为 X, 我想要 Y, 以便 Z" | ☐ |
| 三、核心功能 (F1/F2/F3 code+name+desc) | el-table 渲染 coreFeatures, code 列用 el-tag | ☐ |
| 四、验收标准 (4 条 category/criterion/target) | el-table 渲染 acceptance, target 用 success tag | ☐ |
| 完整度 89% badge | `<el-tag type="success">完整度 {{ completenessScore }}%</el-tag>` (应该 ≥80) | ☐ |
| 状态机操作 (送评审/打回/确认) | 3 个 el-button 按 status 条件显示 | ☐ |

---

## 3. ued (UED 设计协同) — commit `9b01816`

**原型**: `ued.html` + `agriplm.js` (`runUEDCheck`)
**我的**: `plm-frontend/src/views/business/ued/index.vue`

| 原型元素 | 我的实现 | 检查 |
|---|---|---|
| 左栏 form: title/designType/platform/figmaFileKey/figmaUrl/version/description | 全部 7 字段 | ☐ |
| Figma 同步按钮 (input append button) | `<el-input>` 配 `<template #append>` "📤 同步" 按钮 | ☐ |
| 右栏 "🔍 AI 规范评审报告" | el-card 标题相同 | ☐ |
| 评分 88 (规范遵从度 ≥80 通过) | `<el-tag :type="...">` 显示 88/100 + ✅ 通过 | ☐ |
| 6 条评审项 (✅ 颜色 / ✅ 字体 / ⚠️ 间距 / ⚠️ 无障碍 / ⚠️ 骨架屏 / ✅ 组件) | parseReviewItems 渲染 6 条彩色块 | ☐ |
| 顶部 3 个 statistic 通过/警告/失败 | el-statistic 3 个: passCount/warningCount/failCount | ☐ |
| 每条 item 有 category + message + suggestion (warning/fail 时) | div 显示 category + message, v-if suggestion 显示 💡 | ☐ |

---

## 4. arch (架构设计) — commit `9b01816`

**原型**: `archdesign.html` + `agriplm.js` (`genArchDesign`)
**我的**: `plm-frontend/src/views/business/arch/index.vue`

| 原型元素 | 我的实现 | 检查 |
|---|---|---|
| 左栏 6 个 select: archMode/techStack/dbStack/aiOrchestration/deployMode/iotProtocol | `<el-row>` 3 行 × 2 col = 6 个 el-select | ☐ |
| 右栏 AI 推荐: 4 timeline + C4 图 + NFR | el-steps 4 步 + NFR 4 色卡 + AI 报告 textarea | ☐ |
| 4 步 timeline: 架构模式 → 技术选型 → IoT 接入 → 部署方案 | `<el-steps :active="4">` 4 个 el-step (从 aiTimelineJson) | ☐ |
| NFR 4 项 (性能/可用性/安全/扩展性) | 4 个色卡: 蓝/绿/红/黄, 显示 nfrPerformance 等 | ☐ |
| 性能: API P99 < 200ms + IoT 10 万设备并发 | nfrPerformance 文本含 "P99 < 200ms" "10 万设备" | ☐ |
| 可用性: SLA 99.9% | nfrAvailability 含 "SLA 99.9%" | ☐ |
| 安全: TLS 1.3 + RBAC | nfrSecurity 含 "TLS 1.3" | ☐ |
| 扩展性: 支持 5 年 10 倍增长 | nfrScalability 含 "5 年 10 倍" | ☐ |

---

## 5. competitive (竞品情报) — commit `9b01816`

**原型**: `competitive.html` + `agriplm.js` (`renderCompetitive`)
**我的**: `plm-frontend/src/views/business/competitive/index.vue`

| 原型元素 | 我的实现 | 检查 |
|---|---|---|
| 3 个 tab: 矩阵 / 监控 / SWOT | `<el-tabs>` 3 个 `<el-tab-pane>` | ☐ |
| Tab 1 矩阵: 15 行维度 × 5 列竞品 | el-table 15 行, 5 个 vendor column + 维度名列 | ☐ |
| 维度名 (15 项: 立项管理/AI竞品分析/.../私有化部署) | matrix.dimensions 数组 (从 matrixJson 反序列化) | ☐ |
| 竞品列 (5 个: 禅道/LigaAI/Jira+Rovo/Copilot WS/本品★) | matrix.vendors,本品 isOurProduct=true 加 ★ 前缀 | ☐ |
| 评分 0/0.5/1 → ✗/△/✓ (本品 ★) | scoreIcon() 转换, scoreStyle() 颜色: 红/黄/绿+紫 | ☐ |
| 本品列高亮淡紫背景 | cellStyle() 给 columnIndex===lastVendorIdx 加 #f5f3ff | ☐ |
| Tab 2 监控: 4 行 (vendor/news/threat/date) | el-table 4 行, threat tag 颜色 high=danger/mid=warning/low=info | ☐ |
| 监控数据: 禅道(中)/LigaAI(中)/PingCode(高)/Jira+Rovo(低) | monitors 数组 4 项 | ☐ |
| Tab 3 SWOT: 4 个色块 (绿优势/红劣势/蓝机会/黄威胁) | 2×2 grid 的 el-card, body-style 4 种背景色 | ☐ |
| 优势 4 条 (含 "唯一覆盖农业全生命周期") | ourSwot.strengths 4 条 bullet | ☐ |
| 劣势 3 条 (含 "品牌知名度较低") | ourSwot.weaknesses 3 条 | ☐ |
| 机会 3 条 (含 "农业数字化转型加速") | ourSwot.opportunities 3 条 | ☐ |
| 威胁 3 条 (含 "通用竞品快速AI化") | ourSwot.threats 3 条 | ☐ |

---

## 6. analytics (效能分析) — commit `6b521b8`

**原型**: `analytics.html`
**我的**: `plm-frontend/src/views/business/analytics/index.vue`

| 原型元素 | 我的实现 | 检查 |
|---|---|---|
| 顶部 4 stat 卡 | 4 个 el-card stat-card 布局 (含 emoji icon + 数字 + 标签 + 趋势) | ☐ |
| 需求吞吐量 34 (↑18%) | stat 第 1 个,显示 requirementThroughput, ↑ 18% trend | ☐ |
| 迭代准时率 87% (↑12%) | stat 第 2 个 sprintOnTimeRate + ↑ 12% | ☐ |
| 缺陷密度 1.8 (条/KLOC) | stat 第 3 个 defectDensity + 评级 (≤2 优/≤5 中/>5 差) | ☐ |
| AI 节省工时 284h | stat 第 4 个 aiHoursSaved | ☐ |
| AI 提效柱状图 (5 阶段) | el-progress 5 行: 需求/PRD/架构/用例/数据 | ☐ |
| 节省 284 小时, 平均提效 83% | mock 显示 (后端字段未存这个细节,前端 mock 写死) | ☐ |
| 项目健康度评分 | el-progress 3 项目 (mock,后端未存) | ☐ |
| AI 改进建议卡 | el-card 显示 form.aiRecommendations 的 Markdown | ☐ |
| 时间粒度 (本月/季度/年) 切换 | 顶部 el-select periodFilter | ☐ |
| **新增 DORA 4 指标卡 (原型没有, 我的增量)** | 部署频率/前置时间/MTTR/变更失败率 + Elite/High/Medium 等级 tag | ☐ |

> ⚠️ **原型 analytics.html 没有 DORA 4 指标**,这块是我从 PRD-MAPPING 里挪过来的 (因为我们 Domain 含 deploymentFrequency 等 DORA 字段)。如果你觉得不该出现在 analytics 页, 应该挪到独立的 dora 模块页面里, 告诉我。

---

## 7. dashboard (工作台) — commit `6b521b8`

**原型**: `dashboard.html`
**我的**: `plm-frontend/src/views/business/dashboard/index.vue`

| 原型元素 | 我的实现 | 检查 |
|---|---|---|
| 欢迎: "早上好, 张总, 您有 3 个待办, 2 个风险项目" | hello div + dynamic 待办/缺陷数 | ☐ |
| 4 stat 卡: 在办项目 7 / AI文档 142 / 当前缺陷 18 / 自动化 76% | 4 el-card (mock 后端 aggregate 返回) | ☐ |
| AI Assistant 紫色面板 | el-card body-style 紫色 gradient | ☐ |
| 快捷指令 6 个 (立项/竞品/PRD/用例/数据/手册) | 8 个 el-button (我多加了 UED/架构) | ☐ |
| 在办项目进度 widget (含 "查看全部→") | el-card "在办项目进度" + el-progress 3 项目 | ☐ |
| 我的待办 widget | el-card 列表 + 优先级 tag + 日期 | ☐ |
| 项目生命周期 17 阶段 swimlane | 横向 div + 数字徽章 + 阶段名 (17 个) | ☐ |
| 本迭代质量快照 (placeholder) | 我做实了: 3 el-statistic (缺陷/通过率/覆盖率) | ☐ |
| AI 改进指标 (原型没明确) | 我做实了: 节省工时 + 文档数 + recommendations bullet | ☐ |
| ⚙️ 自定义 widget 弹窗 | settingsOpen dialog + checkbox-group 6 选项 (mock 保存) | ☐ |

---

## 共同问题清单 (整体过一遍)

每个页面看看这些:

- ☐ 没用对的 dict_type (字典 key 跟 sys_dict_data 里的 dict_type 对不上, 选项不渲染)
- ☐ 按钮文案 emoji 跟原型不一致 (原型用 ✨ 🎨 🚀 等)
- ☐ 颜色调性: 原型主色是紫色 #5b21b6 + 绿色 #4CAF50 (农业绿), 我的应该一致
- ☐ 字号: 原型 标题 18px / 副标题 13px / 正文 12.5-14px, 我的 .ps/.pt class 应该匹配
- ☐ 农业场景的中文文案 (土壤墒情/灌溉/IoT 设备/作物等) 是否准确

---

## 如果发现不对劲

直接告诉我哪个页面 (1-7) 哪一项 ☐ 没勾、原型实际是什么样,我修。

或者你截屏发给我也行 — 我用截屏对比修。

如果整体方向都不对路 (比如布局根本不该是 form+preview), 那就先指出来再说,我别再继续了避免做无用功。
