# UED 规范

AgriPLM·AI 用户体验设计规范。**所有前端视觉、交互实现必须以本文档为准**；设计稿出具时必须引用相应章节号；CR 时须对照本文件检查。

> 关联规则：
> - Claude 执行约束 → [.claude/rules.md §N](../.claude/rules.md)（Claude 自动加载）
> - 原型 SSoT → `prd和原型/AgriPLM-DevOps-原型/agriplm_split/agriplm.css` + 各页 HTML
> - 前端代码规范 → [开发规范.md §2](开发规范.md)

---

## 0. 规范速查（Quick Reference）

| 类别 | 关键规则 |
|---|---|
| 颜色 | 主色 `#2d7a4f`（`--gp`），强调绿 `#4caf78`（`--gl`），一律走 CSS 变量 |
| 字体 | `-apple-system / PingFang SC / Microsoft YaHei`，基础 14px |
| 圆角 | 卡片 12px / 按钮 8px / 徽章 20px（全圆） / 输入框 8px |
| 阴影 | 仅悬浮交互层使用：`0 3px 10px rgba(0,0,0,.1)` |
| 间距 | 4 的整数倍：4/8/12/14/16/18/20px；内容区 padding 20px |
| 动效 | transition `all .15s`（默认）；滑入通知 `.3s ease`；禁止超过 .5s |
| AI 标签 | 紫蓝渐变背景；AI 按钮用 `btn-ai` 类；AI 结果区用 `.aip` 容器 |
| 农业品牌色 | 深绿 `#1a5235` 用于侧边栏背景；`#4caf78` 用于 Logo 图标/激活态 |

---

## 1. 设计 Token（Design Tokens）

所有颜色、尺寸均以 CSS 变量定义（来源：`agriplm.css :root`），**前端禁止使用裸十六进制颜色**，必须引用变量。

### 1.1 颜色 Token

```css
/* === 品牌色 — 农业绿 === */
--gp:    #2d7a4f   /* Primary：主操作按钮、激活边框、链接 */
--gl:    #4caf78   /* Light：Logo、激活指示器、进度条 */
--gpale: #e8f5ee   /* Pale：AI命令栏背景、悬浮填充 */
--gd:    #1a5235   /* Dark：侧边栏背景、深色文字强调 */

/* === 功能色 === */
--am:    #f59e0b   /* Amber：警告、待处理、中间状态 */
--aml:   #fef3c7   /* Amber Light：警告背景块 */
--bl:    #3b82f6   /* Blue：信息、链接、方法徽章GET/POST */
--bll:   #eff6ff   /* Blue Light：信息背景块 */
--rd:    #ef4444   /* Red：错误、删除、严重缺陷 */
--rdl:   #fef2f2   /* Red Light：错误背景块 */
--pu:    #7c3aed   /* Purple：AI功能标识、AI按钮渐变起点 */

/* === 中性色（灰阶）=== */
--g50:   #f9fafb   /* 表头背景、悬浮行 */
--g100:  #f3f4f6   /* 页面背景、看板列背景 */
--g200:  #e5e7eb   /* 分割线、输入框边框 */
--g300:  #d1d5db   /* 占位符边框、禁用态边框 */
--g400:  #9ca3af   /* 占位符文字、图标静止态 */
--g500:  #6b7280   /* 次要文字、说明性文字 */
--g600:  #4b5563   /* 表单 label、次级标题 */
--g700:  #374151   /* 正文、表格单元格 */
--g800:  #1f2937   /* 主要文字、卡片标题 */
--g900:  #111827   /* 最深层级文字、页面大标题 */
```

### 1.2 尺寸 Token

```css
--sw:  220px   /* 侧边栏宽度（Sidebar Width） */
--th:  56px    /* 顶部栏高度（TopBar Height） */
```

### 1.3 间距系统

采用 4px 基准，固定档位（不使用任意值）：

| Token | 值 | 用途 |
|---|---|---|
| `space-1` | 4px | 图标与文字间隔、紧凑内边距 |
| `space-2` | 8px | 按钮内 gap、小元素间隙 |
| `space-3` | 12px | 表单行间距、卡片标题下间距 |
| `space-4` | 14px | 列表项间距、卡片内边距（紧凑） |
| `space-5` | 16px | 模态框内边距基准 |
| `space-6` | 18px | 卡片默认内边距 |
| `space-7` | 20px | 内容区默认内边距 |
| `space-8` | 24px | 模态框内边距（标准） |

---

## 2. 排版（Typography）

### 2.1 字体栈

```css
font-family: -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif;
```

中文使用 PingFang SC（macOS/iOS）或 Microsoft YaHei（Windows），英文走系统字体。**禁止**引入额外中文 Web Font（性能代价过高）。

### 2.2 字号层级

| 角色 | 大小 | 权重 | 颜色变量 | 使用场景 |
|---|---|---|---|---|
| 页面大标题（`.pt`）| 18px | 700 | `--g900` | 每页顶部 H1 |
| 卡片标题（`.ct`）| 14px | 600 | `--g800` | Card header |
| 顶栏标题（`.tb-title`）| 15px | 700 | `--g900` | TopBar |
| 正文 / 表格单元格 | 14px | 400 | `--g700` | 默认 |
| 次级文字（`.ps`）| 12.5px | 400 | `--g500` | 页面副标题 |
| 表头（`.tbl th`）| 11px | 700 | `--g500` | 大写字母 |
| 小标签 / 徽章 | 11px | 600 | 各功能色 | badge |
| 代码块（`.code`）| 11.5px | 400 | `#a3e635` | 深色背景 |

### 2.3 行高

- 正文段落：`line-height: 1.75`
- 卡片/表格行：`line-height: 1.4`
- 标题：`line-height: 1.15`（大标题）/ `1.3`（小标题）

---

## 3. 颜色使用规则

### 3.1 主色使用

- `--gp`（#2d7a4f）：主要操作按钮（`.btn-p`）、表单 focus 边框、Tab 激活线、侧边栏激活项左边框
- `--gl`（#4caf78）：Logo 图标、进度条填充、激活指示、成功态标记
- `--gpale`（#e8f5ee）：AI 命令栏背景、上传区悬浮、次级高亮填充
- `--gd`（#1a5235）：侧边栏整体背景，不用于内容区

### 3.2 功能色约束

| 功能色 | 允许场景 | 禁止场景 |
|---|---|---|
| `--rd` | 错误提示、删除按钮、P0/P1 缺陷严重度 | 成功/完成态 |
| `--am` | 警告、评审中状态、待处理徽章 | 错误态（用红） |
| `--bl` | 信息提示、GET 方法徽章、蓝色进度 | 主操作按钮（用绿） |
| `--pu` | 所有 AI 相关功能：按钮渐变起点、AI 标签、AI Panel 背景渐变 | 非 AI 功能 |

### 3.3 对比度要求（无障碍 WCAG AA）

- 正文文字对背景：≥ 4.5:1
- 大文字（18px+/14px bold+）对背景：≥ 3:1
- 禁止使用浅灰（`--g300` 或更浅）作为文字色

---

## 4. 布局系统

### 4.1 整体布局

```
┌─────────────────────────────────────────┐
│          TopBar (height: 56px)          │
├─────────┬───────────────────────────────┤
│         │                               │
│Sidebar  │    Content Area               │
│(220px)  │    padding: 20px              │
│dark-bg  │    overflow-y: auto           │
│         │                               │
└─────────┴───────────────────────────────┘
```

- 侧边栏固定宽 220px，深绿背景，**不随内容区滚动**
- TopBar 固定高 56px，白色背景，`border-bottom: 1px solid --g200`
- 内容区独立滚动，padding 20px

### 4.2 栅格系统

| 类名 | 列数 | 间距 | 用途 |
|---|---|---|---|
| `.grid2` | 2 等宽列 | 14px | 常规双列（卡片/表单） |
| `.grid3` | 3 等宽列 | 14px | 统计数字区（Stat Cards）|
| `.grid4` | 4 等宽列 | 12px | 小卡片列表 |

响应式：所有栅格在 `max-width: 768px` 时折叠为单列或双列（见 §8 响应式）。

### 4.3 卡片（Card）

```
┌─────────────────────────────┐
│  卡片标题（.ct）  14px 600  │  ← padding-bottom 12px
├─────────────────────────────┤
│  内容区                     │
└─────────────────────────────┘
background: #fff
border-radius: 12px
border: 1px solid --g200
padding: 18px
margin-bottom: 14px
```

**规则**：
- 卡片之间间距固定 14px（`margin-bottom: 14px`）
- 卡片不使用 `box-shadow`（除悬浮状态）
- 卡片内的次级分块用 `background: var(--g50); border-radius: 8px`

---

## 5. 组件规范

### 5.1 按钮（Button）

```
高度：auto（文字自然高度）
padding：7px 14px（标准）/ 4px 10px（小号 .btn-sm）
border-radius：8px
font-size：12.5px（标准）/ 11.5px（小号）
font-weight：500
transition：all .15s
```

| 变体 | 类名 | 背景 | 文字 | 用途 |
|---|---|---|---|---|
| 主要 | `.btn-p` | `--gp` | `#fff` | 确认、保存、主操作 |
| 次要 | `.btn-s` | `#fff` | `--g700` | 取消、次要操作 |
| AI | `.btn-ai` | `linear-gradient(135deg, --pu, --bl)` | `#fff` | AI 触发操作 |
| 危险 | `.btn-rd` | `--rd` | `#fff` | 删除、驳回 |

**规则**：
- 同一操作区内，主按钮最多 1 个；危险按钮必须是最后一个或单独区域
- AI 按钮（`.btn-ai`）**仅**用于触发 AI 工作流，不用于普通保存/确认
- 按钮文字前可加 emoji 图标（如 `✅ 保存`、`✨ AI生成`），但须与 `.btn-ai` 的 `✨` 区分——`✨` 专属 AI 按钮
- 禁用态：`opacity: 0.5; cursor: not-allowed`（不改变颜色）

### 5.2 状态徽章（Status Badge）

```
padding：2px 8px
border-radius：20px（全圆）
font-size：11px
font-weight：600
```

| 语义 | 类名 | 背景 | 文字色 | 场景 |
|---|---|---|---|---|
| 成功/已确认/已完成 | `.b.bg` | `#dcfce7` | `#166534` | 状态:完成/已确认 |
| 警告/评审中 | `.b.bam` | `--aml` | `#92400e` | 状态:评审中/待处理 |
| 错误/失败 | `.b.brd` | `--rdl` | `#991b1b` | 状态:失败/拒绝 |
| 信息/蓝色 | `.b.bbl` | `--bll` | `#1d4ed8` | 类型标签 |
| 默认/草稿 | `.b.bgr` | `--g100` | `--g600` | 状态:草稿/未开始 |
| AI 专属 | `.b.bai` | 紫蓝渐变 | `#5b21b6` | AI 生成内容标识 |

**状态机映射规则**（与 PRD §M.4 联动）：
- `草稿` → `.bgr`（灰）
- `评审中` → `.bam`（黄）
- `已确认` → `.bg`（绿）
- `失败/拒绝` → `.brd`（红）
- 所有状态颜色必须与 PRD-MAPPING.md 中的状态机一致，不允许自定义颜色

### 5.3 表单（Form）

**输入框（Input / Select / Textarea）**

```
padding：8px 11px
border：1px solid --g300
border-radius：8px
font-size：12.5px
transition：border .15s

focus: border-color --gp; box-shadow 0 0 0 2px rgba(45,122,79,.15)
```

**标签（Label）**

```
font-size：12px
color：--g600
font-weight：600
display：block
margin-bottom：4px
```

**必填字段**：在 label 末尾加 ` *`（空格+星号），**不加红色**（避免颜色依赖无障碍问题）。

**表单行（form-row）**：双列 `1fr 1fr`，列间距 12px，行间距 12px。

**规则**：
- 表单内容宽度不超过容器 100%
- Textarea 默认 `min-height: 70px`，允许用户竖向拉伸（`resize: vertical`），禁止 `resize: horizontal`
- Select 必须有默认 placeholder 选项（首选项不能是实际数据）
- 表单中日期字段用 `type="date"`，格式 `YYYY-MM-DD`

### 5.4 表格（Table）

```
表头 th：
  padding：9px 11px
  background：--g50
  color：--g500
  font-size：11px
  font-weight：700
  text-transform：uppercase
  letter-spacing：.4px
  border-bottom：1px solid --g200

单元格 td：
  padding：10px 11px
  border-bottom：1px solid --g100
  color：--g700
  font-size：12.5px
  vertical-align：middle

行悬浮：tr:hover td → background --g50
末行：tr:last-child td → border-bottom none
```

**规则**：
- 表格操作列（编辑/删除）固定在最右列，按钮使用 `.btn-sm`
- 状态列必须用 Status Badge，**不用纯文字**
- 数字/日期列右对齐，文字列左对齐
- 空数据状态：居中显示 icon + 说明文字（如 `📭 暂无数据`）

### 5.5 模态框（Modal）

```
宽度：600px（标准）/ 800px（.modal-lg）
max-width：94vw
border-radius：14px
padding：24px
box-shadow：0 20px 60px rgba(0,0,0,.2)
遮罩：background rgba(0,0,0,.45)

头部（.mh）：
  flex 布局，左标题右关闭按钮
  margin-bottom：18px

标题（.mht）：16px 700

关闭按钮（.mclose）：22px，颜色 --g400，悬浮 --g700
```

**规则**：
- 模态框内容超出高度时独立滚动（`max-height: 88vh; overflow-y: auto`）
- 操作按钮行（`.btn-row`）置于模态框底部，主操作在左，取消在右
- 危险操作（删除）按钮放在主操作右侧，用 `.btn-rd.btn-sm`
- 多 Tab 内容的模态框：Tab 导航置于标题下方（见 §5.8）
- 禁止模态框嵌套（Modal 内不弹 Modal）

### 5.6 通知（Notification/Toast）

```
位置：右上角，top 20px，right 20px，z-index 2000
宽度：最大 300px
padding：12px 16px
border-radius：10px
box-shadow：0 4px 20px rgba(0,0,0,.15)
border-left：4px solid 功能色
animation：slideIn .3s ease（进入），fadeOut（退出）
```

| 类型 | 边框色 | 场景 |
|---|---|---|
| 默认/成功 | `--gp` | 操作成功 |
| 错误 | `.err` → `--rd` | 操作失败 |
| 警告 | `.warn` → `--am` | 需注意的状态 |
| 信息 | `.info` → `--bl` | 纯提示 |

**规则**：
- 自动消失时间：成功 3s，错误 5s，警告 4s
- 同时最多显示 3 条通知，超出时顶掉最旧的
- 通知不阻断用户操作（`pointer-events: none` 除了通知本身）

### 5.7 Tab 导航

```
容器：display flex，border-bottom 2px solid --g200，margin-bottom 16px

Tab 项（.tab）：
  padding：9px 18px
  color：--g500
  font-size：12.5px
  font-weight：500
  border-bottom：2px solid transparent（margin-bottom -2px 与容器重叠）
  transition：all .15s

  hover：color --gp
  active（.tab.active）：color --gp，border-bottom-color --gp，font-weight 700
```

**规则**：
- Tab 数量：3～5 个（少于 3 个用段落分组，多于 5 个考虑折叠或侧边导航）
- Tab 标题前可加 emoji 图标（`📝 基本信息`），保持与原型一致
- 切换 Tab 不触发路由跳转（仅 display 切换），不产生浏览器历史记录

### 5.8 进度条（Progress Bar）

```
容器（.pb）：height 5px，background --g200，border-radius 3px，overflow hidden
填充（.pf）：height 100%，border-radius 3px，transition width .4s

颜色变体：
.pfg  → --gl（绿：完成/正常）
.pfam → --am（黄：进行中/警告）
.pfrd → --rd（红：异常/危险）
.pfbl → --bl（蓝：信息性进度）
.pfpu → --pu（紫：AI处理中）
```

---

## 6. 导航规范

### 6.1 侧边栏（Sidebar）

```
width：220px
background：--gd（深绿 #1a5235）
固定，不滚动主内容
```

**分组标签（`.sb-sec`）**：`10px，rgba(255,255,255,.35)，uppercase，letter-spacing .8px`

**导航项（`.ni`）**：

```
padding：8px 16px
color：rgba(255,255,255,.7)
font-size：12.5px
border-left：3px solid transparent

hover：background rgba(255,255,255,.07)，color #fff
active：background rgba(255,255,255,.14)，color #fff，border-left-color --gl
```

**规则**：
- 每个导航项左侧有固定宽 16px 的图标区（`.ni-icon`），图标使用 emoji
- 当前页面对应的导航项必须有 `.active` 类（由 JS 根据 URL 自动判断）
- 侧边栏不折叠（桌面端固定展开，响应式另处理）
- 导航分组顺序严格遵守原型 HTML（不允许自行调整顺序）

### 6.2 顶部栏（TopBar）

```
height：56px
background：#fff
border-bottom：1px solid --g200
padding：0 20px
display：flex，align-items center，gap 10px
```

固定元素从左到右：
1. 页面标题（`.tb-title`，`font-weight 700，font-size 15px，--g900`）
2. 弹性空白（`.tb-sp`，`flex: 1`）
3. AI 命令栏（`.ai-cmdbar`，宽 300px）
4. 分割竖线
5. 通知铃铛（带红点角标）
6. 用户头像（`.av`，30px 圆形，绿色背景）

---

## 7. AI 功能 UI 规范

本系统 AI 功能是核心差异化特性，UI 上须明确区分"AI 生成 / AI 驱动 / 人工输入"。

### 7.1 AI 命令栏（AI Command Bar）

```css
.ai-cmdbar {
  background: var(--gpale);
  border: 1.5px solid var(--gl);
  border-radius: 24px;
  padding: 6px 14px;
  width: 300px;
  cursor: pointer;
}
.ai-cmdbar:hover {
  box-shadow: 0 0 0 3px rgba(76,175,120,.2);
}
```

- 始终位于 TopBar 右侧
- 占位文字：`让AI帮我… 例如"生成气象预警PRD"`（示例要与业务相关）
- 点击打开 AI 指令中心 Modal
- 快捷键 `⌘K` 标注在右侧（背景 `--g200`，文字 `--g500`）

### 7.2 AI Panel（AI 输入/结果区）

```css
.aip {
  background: linear-gradient(135deg, #1e1b4b, #1e3a5f);
  border-radius: 12px;
  padding: 18px;
  color: #fff;
}
```

- 深色渐变背景（深紫→深蓝）区分于白色卡片
- 头像图标（`.aip-av`）：34px，紫蓝渐变，圆角 10px，显示 AI robot emoji
- 输入区（`.ai-in`）：半透明白色背景，白色边框
- 快捷指令标签（`.ai-q`）：半透明白色背景，圆角 20px，hover 加深

### 7.3 AI 按钮规则

- 所有触发 AI 工作流的按钮必须使用 `.btn-ai`（紫蓝渐变）
- 按钮文字前缀固定为 `✨`（sparkle emoji）
- AI 按钮的 loading 状态：文字变为脉冲动点（`.dot-anim`）
- AI 生成完成后通知：`.notif-item.info`（蓝色边框）

### 7.4 AI 标签（AI Badge）

```css
.b.bai {
  background: linear-gradient(135deg, #ede9fe, #dbeafe);
  color: #5b21b6;
  border: 1px solid #c4b5fd;
}
```

- 所有 AI 生成的内容条目，右侧显示 `AI` 徽章（`.b.bai`）
- 模块卡片上的 AI 能力标识：`<span class="mod-tag ai">AI</span>`（右上角定位）

### 7.5 AI 生成状态机

| 状态 | 视觉表现 |
|---|---|
| 待触发 | AI 按钮正常显示，灰色结果占位（`color: --g400`） |
| 生成中 | 按钮变 `.dot-anim` 脉冲，结果区显示骨架屏 |
| 生成完成 | 结果区渲染内容，顶部显示 `.b.bai` 徽章 |
| 生成失败 | 通知 `.notif-item.err`，结果区保持原内容 |

---

## 8. 响应式规范

本系统定位**桌面端优先**，以 1280px 宽度为设计基准。以下为已知的响应式断点：

| 断点 | 处理方式 |
|---|---|
| `max-width: 768px`（平板/小屏）| 标题 32px（原 52px）；侧栏 + 内容区折叠；栅格从 `auto-fill minmax(200px,1fr)` 自然换行 |
| `< 768px`（手机）| 侧边栏不显示（需 Hamburger 菜单，待实现）；内容区 padding 减为 `20px`（左右各） |

**现阶段实现要求**：
- 内容区 `.grid2 / .grid3 / .grid4` 在小屏时允许自然换行（不强制单列）
- 模态框：`max-width: 94vw`（已覆盖）
- 暂不要求完整手机端适配（PLM 工具以桌面端为主）

---

## 9. 交互规范

### 9.1 悬浮（Hover）

| 元素 | Hover 效果 |
|---|---|
| 导航项 | 背景 `rgba(255,255,255,.07)` 加深 |
| 卡片（可点击）| `transform: translateY(-2px); box-shadow 0 8px 24px rgba(0,0,0,.3)` |
| 看板卡片（`.kbcard`）| `box-shadow 0 3px 10px rgba(0,0,0,.1); transform translateY(-1px)` |
| 主按钮 | 背景色加深（`--gd`）|
| 次要按钮 | 背景 `--g50` |
| AI 按钮 | `opacity: .9` |
| 表格行 | 背景 `--g50` |
| 输入框 | 边框色不变（focus 才变绿）|

### 9.2 焦点（Focus）

- 所有交互元素必须有 `:focus-visible` 状态
- 输入框 focus：`border-color: --gp; box-shadow: 0 0 0 2px rgba(45,122,79,.15)`
- 按钮 focus（键盘）：`outline: 2px solid --gp; outline-offset: 2px`

### 9.3 动效时长约束

| 场景 | 时长 |
|---|---|
| 颜色/边框/背景切换 | `0.15s` |
| 卡片 translate / shadow | `0.2s` |
| 进度条填充 | `0.4s` |
| 通知 slideIn | `0.3s ease` |
| **禁止超过** | `0.5s`（避免迟滞感）|

### 9.4 加载态

- **数据加载中**：表格/列表区域显示骨架屏（灰色矩形块），不显示 spinner
- **AI 生成中**：按钮使用 `.dot-anim`；文字输出区可模拟打字机效果（逐字显示）
- **文件上传中**：进度条 `.pb > .pf.pfbl`（蓝色），百分比文字

---

## 10. 农业领域 UI 规范

AgriPLM 面向农业信息化场景，部分 UI 组件需体现农业特色。

### 10.1 UI 组件库分类（原型 §ued.html）

| 类型 | 图标 | 描述 |
|---|---|---|
| 农情大屏组件 | 📊 | 全屏数据可视化，深色背景，大字体数字 |
| 移动端农事记录 | 📱 | 卡片式表单，大触控区域（min 44px） |
| IoT 数据看板 | 🌡️ | 实时数据流，含告警阈值线 |
| 地块地图组件 | 🗺️ | GIS 地图嵌入，绿色地块覆盖层 |
| 农作物生长周期 | 🌾 | 时间轴形式，阶段性节点 |

### 10.2 数据可视化规范

- **图表配色**：优先使用品牌绿系（`--gp, --gl, --gpale`），次用蓝（`--bl`）和琥珀（`--am`）
- **告警阈值线**：`--rd`（红色虚线），标注阈值文字
- **农业数据单位**：必须在图表轴标签中注明（如 `温度(°C)`、`含水率(%)`）
- **空数据**：图表区域显示 `暂无数据` 文字，不显示空坐标轴

### 10.3 Figma 集成规范

（配合 UED 模块 Figma MCP 同步功能）

- 每个设计稿版本必须有：版本号、状态（草稿/评审中/已确认）、Figma 链接
- 设计稿状态与代码实现状态保持同步（参见 `02-设计/设计稿（Figma 链接）/README.md`）
- 设计稿导出标注时，颜色值必须使用 Token 名（如 `--gp`），不使用裸十六进制

---

## 11. 无障碍（Accessibility）

### 11.1 最低要求（WCAG AA）

- [ ] 所有颜色对比度 ≥ 4.5:1（正文）/ 3:1（大文字）
- [ ] 所有交互元素有 `:focus-visible` 样式
- [ ] 图片/图标有 `alt` 属性或 `aria-label`
- [ ] 表单 input 有对应 `<label>`（或 `aria-label`）
- [ ] 模态框打开时焦点锁定在 Modal 内

### 11.2 中文语境特殊处理

- 不纯依赖颜色传递信息（如缺陷严重度同时用颜色 + 文字 `P0/P1/P2/P3`）
- 状态变化（如流程推进）同时提供通知文字反馈
- AI 生成内容明确标注来源（`.b.bai` 徽章），不混淆"AI 生成"和"人工录入"

---

## 12. 设计资产管理

### 12.1 目录约定

```
02-设计/
  UED规范.md                   ← 本文件（SSoT）
  设计稿（Figma 链接）/
    README.md                  ← 各模块 Figma 文件链接 + 状态
  用户旅程图/
    <模块>.md                  ← 每个核心流程的旅程图
```

### 12.2 设计稿版本命名

| 模式 | 示例 |
|---|---|
| 初稿 | `v0.1-draft` |
| 评审版 | `v0.2-review` |
| 确认版 | `v1.0-final` |
| 迭代修改 | `v1.1-fix-<issue>` |

### 12.3 设计交付物 DoD

新功能或模块设计完成（可进入开发）的 6 项：

```
□ 1. Figma 文件中存在对应 Frame，版本 ≥ v1.0-final
□ 2. 所有颜色使用 Figma Style（对应本规范 Token）
□ 3. 交互说明（hover/focus/loading/empty/error 状态全覆盖）
□ 4. 组件标注含间距数值（与 §1.2 间距系统吻合）
□ 5. 状态机流转图附在 Figma 旁边页
□ 6. PRD-MAPPING.md 对应模块的字段对照表已更新
```

---

## 13. UED 评审 Checklist（CR 时使用）

代码 PR 中涉及 UI 改动时，PR 描述中必须包含以下自检项：

```markdown
### UED 自检
- [ ] 颜色使用 CSS 变量（无裸十六进制）
- [ ] 间距值在规范档位内（4px 倍数）
- [ ] 新增状态徽章与 PRD-MAPPING.md 状态机一致
- [ ] AI 按钮使用 .btn-ai + ✨ 前缀，非 AI 操作未使用 .btn-ai
- [ ] 新增表单字段有对应 label（非 placeholder 代替）
- [ ] 悬浮/焦点样式已实现
- [ ] 空数据、加载中、错误状态已处理
- [ ] 在 768px 宽度下布局未破坏
```

---

## 附录 A：CSS 类名速查

| 类名 | 用途 |
|---|---|
| `.btn .btn-p .btn-s .btn-ai .btn-rd .btn-sm` | 按钮变体 |
| `.b .bg .bam .brd .bbl .bgr .bai` | 状态徽章 |
| `.card .ct` | 白色卡片 + 标题 |
| `.sc .sl .sv .sd` | 统计卡片（Stat Card）|
| `.grid2 .grid3 .grid4` | 栅格布局 |
| `.tbl` | 标准表格 |
| `.modal .modal-lg .modal-bg .mh .mht .mclose` | 模态框 |
| `.tabs .tab` | Tab 导航 |
| `.pb .pf .pfg .pfam .pfrd .pfbl .pfpu` | 进度条 |
| `.aip .aip-av .ai-in .ai-q .ai-cmdbar` | AI UI 组件 |
| `.tl .tli .tld .tlc` | 时间轴 |
| `.kb .kbc .kbcard` | 看板 |
| `.notif .notif-item` | 通知 Toast |
| `.code` | 代码块 |
| `.upload-btn` | 上传按钮（虚线边框）|
| `.form-group .form-row .form-label .form-input .form-select .form-textarea` | 表单组件 |

---

## 附录 B：状态颜色速查

| 状态文案 | 徽章类 | 备注 |
|---|---|---|
| 草稿 | `.b.bgr` | 灰色 |
| 待评审 / 待处理 | `.b.bam` | 琥珀黄 |
| 评审中 | `.b.bam` | 琥珀黄 |
| 已确认 / 已完成 / 已关闭 | `.b.bg` | 绿色 |
| 开发中 / 进行中 | `.b.bbl` | 蓝色 |
| 失败 / 拒绝 / 已取消 | `.b.brd` | 红色 |
| AI 生成 | `.b.bai` | 紫蓝渐变 |
| P0 缺陷 | `.sev-p0`（严重度专属）| 深红 |
| P1 缺陷 | `.sev-p1` | 橙红 |
| P2 缺陷 | `.sev-p2` | 黄色 |
| P3 缺陷 | `.sev-p3` | 浅绿 |
