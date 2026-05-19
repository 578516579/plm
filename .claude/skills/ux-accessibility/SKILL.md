---
name: ux-accessibility
description: PLM UX 无障碍审 — WCAG 2.1 AA + 对比度 + 键盘导航 + 屏幕阅读器 + 触控目标. 用户说"无障碍 / a11y / WCAG / 对比度 / 键盘导航 / 屏幕阅读器"时调用. 输出: 02-设计/<模块>-UX设计.md §4 + a11y 缺陷清单. **ux-designer agent 的子工具**。
---

# ux-accessibility — UX 无障碍审 skill v0.1

## 1. 何时调用
- "无障碍 / a11y / WCAG"
- ux-designer §2.4

## 2. WCAG 2.1 AA 4 维

### 2.1 对比度
- 正文 ≥ 4.5:1
- 大字 ≥ 3:1
- 工具: WebAIM contrast checker

### 2.2 键盘导航
- 所有交互可 Tab 触达
- Focus ring 可见
- Esc 关弹窗
- Enter 提交表单

### 2.3 屏幕阅读器
- 所有 button / a 有 aria-label (无 visible text 时)
- 图标按钮 必带 aria-label
- 表单 input 必带 <label>

### 2.4 触控目标
- ≥ 44×44 px (移动端 ≥ 48×48)

## 3. 输出模板
```markdown
## §4 无障碍审

| 检查项 | 状态 | 缺陷 | 整改 |
|---|---|---|---|
| 对比度 | ⚠️ | 灰色 #999 on white 仅 2.8:1 | 改 #595959 ≥ 4.5:1 |
| 键盘导航 | ✅ | — | — |
| aria-label | ⚠️ | 删除图标按钮缺 label | 加 aria-label="删除" |
| 触控目标 | ✅ | — | — |

WCAG 2.1 AA 总评: 2 ⚠️ / 4 — 阻塞 a11y 验收
```

## 4. 历史
| v0.1 | 2026-05-19 | 首版; ux-designer 配套 4/4 (完结) |
