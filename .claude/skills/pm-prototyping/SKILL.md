---
name: pm-prototyping
description: PLM 原型设计 — HTML wireframe / 低保真原型 / 表单元素标注 / 状态徽章设计 / 字段映射可视化。当用户说"画原型 / 做原型 / wireframe / mockup / HTML 原型 / 表单设计 / 状态徽章 / UI 草图"时调用. 输出: prd和原型/AgriPLM-DevOps-原型/agriplm_split/<module>.html (新增 / 增量). **product-manager agent 的子工具** — agent 在需要可视化字段表 / 状态徽章时调本 skill。
---

# pm-prototyping — 原型设计 skill v0.1

**product-manager agent 的子工具**, 补 PM agent §2.4 原型对齐的逆向能力 (从 PRD 反推原型, 或新模块从零产原型)。

PLM 现有 31 个原型 HTML 在 `prd和原型/AgriPLM-DevOps-原型/agriplm_split/` (per CLAUDE.md PRD/原型 SSoT)。本 skill 产新原型或修改既有原型, 必须保持 PLM 视觉一致 + 状态徽章 CSS 类规范 (.bg/.bam/.bgr/.bd, per rules.md §M.4)。

---

## 1. 何时调用

- 用户说 "画原型 / 做原型 / wireframe / mockup / 表单设计"
- product-manager agent §2.4 原型对齐发现 "PRD 提到但原型无" 元素时反向产原型
- 新模块 stub → active 升级前补原型 (per PRD-MAPPING.md §1 模块状态)
- 现有原型增量加字段 / 状态时

不调:
- 高保真视觉设计 (转 anthropic-skills:canvas-design 全局)
- 业务模块代码 → ruoyi-bootstrap skill Phase 7

---

## 2. 设计原则 (PLM 视觉规范)

| 元素 | 规范 |
|---|---|
| 状态徽章 CSS | `.bg` 绿(已确认/done) / `.bam` 黄(评审中) / `.bgr` 灰(草稿) / `.bd` 红(失败/拒绝) |
| 表单标签 | `<label>字段名 *</label>` (`*` 表必填) |
| 字段类型 | input / select / textarea / button / table / badge |
| AI 按钮 | `<button class="ai-btn">AI<功能>>` (服务端计算字段, per rules.md §M.3) |
| 按钮分组 | 主操作右上 (创建 / 提交) + 列表项操作右侧 (编辑 / 删除) |
| 字典选项 | `<select>` 选项必能对应 PRD-MAPPING.md 字典表 |

---

## 3. 5 步工作流

### Step 1: 锚定上下文

- Read PRD (`01-立项/<模块>-PRD.md`) — 找字段表 + 状态机
- Read PRD-MAPPING.md `<模块>` § — 验证字段 / 状态 / 错误码
- Read 类似模块原型 `prd和原型/.../<其他>.html` — 抄结构 / CSS 类

### Step 2: 划分页面区域

PLM 标准布局:
```
┌──────────────────────────────────────────┐
│ Header (模块名 + 创建按钮)                  │
├──────────────────────────────────────────┤
│ 筛选区 (按状态 / 时间 / 关联模块)          │
├──────────────────────────────────────────┤
│ 列表区 (状态徽章 + 关键字段 + 操作)         │
└──────────────────────────────────────────┘

详情/创建 → Dialog 弹层:
┌──────────────────────────────────────────┐
│ 基本信息 (title / 描述 / 状态徽章)         │
├──────────────────────────────────────────┤
│ 字段表单 (按 PRD 字段顺序)                 │
├──────────────────────────────────────────┤
│ AI 计算字段 (含 AI 按钮触发后端)           │
├──────────────────────────────────────────┤
│ 关联模块 (Project / Sprint / Task FK)      │
├──────────────────────────────────────────┤
│ 操作 (保存 / 取消 / 状态切换按钮)          │
└──────────────────────────────────────────┘
```

### Step 3: 列每字段 HTML element

按 PRD-MAPPING.md `<模块>` § 字段表逐行:

```html
<!-- 基本信息 -->
<label>标题 *</label>
<input type="text" name="title" maxlength="200">

<label>测试环境 *</label>
<select name="environment">
  <option value="dev">dev</option>
  <option value="staging">staging</option>
  <option value="prod">prod</option>
</select>

<!-- 状态徽章 -->
<span class="badge bgr">草稿</span>
<span class="badge bam">评审中</span>
<span class="badge bg">已确认</span>
<span class="badge bd">失败</span>

<!-- AI 字段 (服务端计算, 前端只展示) -->
<label>AI 质量门禁</label>
<button class="ai-btn">AI 质量门禁检查</button>
<span class="qg-result">通过 / 失败 (后端写)</span>
```

### Step 4: 状态机可视化 (可选, 含反向边)

复杂状态机用流程图 (mermaid 或 SVG):

```
draft ──→ review ──→ approved ──→ archived
  ↑          │
  └─ rejected┘  (反向边显式)
```

### Step 5: 输出 + 校验

输出文件: `prd和原型/AgriPLM-DevOps-原型/agriplm_split/<module>.html`

校验:
- [ ] 字段 100% 对应 PRD-MAPPING.md `<模块>` §
- [ ] 状态徽章 CSS 类 .bg/.bam/.bgr/.bd 全用
- [ ] 必填字段标 `*`
- [ ] AI 按钮单独区分, 不允许前端写入
- [ ] 字典 `<select>` 选项与 biz_<entity>_<field> 表一致
- [ ] 列表区含状态徽章 + 编辑/删除操作

---

## 4. 输出文件

`prd和原型/AgriPLM-DevOps-原型/agriplm_split/<module>.html`

PLM 现有 31 个原型作为参考模式 (per CLAUDE.md).

---

## 5. 与其他 skill / agent 衔接

| 上游 | pm-prototyping | 下游 |
|---|---|---|
| pm-prd-writer 输出的字段表 | → 可视化为 HTML | → product-manager agent §2.4 原型对齐 |
| pm-brainstorm 收敛方向 | → 低保真 wireframe | → pm-prd-writer (回填 PRD 字段) |
| tech-lead Phase 02 状态机定义 | → 状态徽章可视化 | → frontend-coder (Vue UI 实现) |
| 用户对原型反馈 | → 增量改 HTML | → PRD-MAPPING.md §2 字段表 amend |

---

## 6. 反模式

- ❌ 画原型不读 PRD-MAPPING.md (违反 SSoT)
- ❌ 状态徽章不用规范 CSS 类 (.bg/.bam/.bgr/.bd 4 类)
- ❌ AI 字段没单独标 (前端可能误以为可编辑)
- ❌ 字典 `<select>` 选项凭脑补 (应来源 biz_<entity>_<field> 表)
- ❌ 加字段不同步 PRD-MAPPING.md §2 字段表 (PRD/原型 漂移)
- ❌ 反向边 UI 不提示 (per proposal 0019)

---

## 7. 历史

| 版本 | 日期 | 改了什么 |
|---|---|---|
| v0.1 | 2026-05-19 | 首版; PM agent 配套 4 skill 之三; 5 步流程 + PLM 视觉规范 (.bg/.bam/.bgr/.bd) + 标准布局 |
