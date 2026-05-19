---
name: security-xss-scan
description: PLM XSS 审计 — 扫 Vue v-html / innerHTML 直接渲染 + 后端 @ApiResponse 返回 HTML/Markdown 未 escape. 当用户说"XSS / v-html / innerHTML / 反射型 XSS / 存储型 XSS / 富文本审"时调用. 输出: 04-测试/security-audit-<module>-<date>.md §2 XSS 风险表. **security-reviewer agent 的子工具**。
---

# security-xss-scan — XSS 审 skill v0.1

**security-reviewer agent 的子工具**, 主走 §2.2 XSS 审职责。

Vue 3.5 模板默认 auto-escape (`{{ }}`), 唯一 XSS 入口是显式 `v-html` 或 JS `innerHTML`。本 skill 扫 3 类。

---

## 1. 何时调用

- 用户说 "XSS / v-html / innerHTML / 反射型 / 存储型 / 富文本审"
- security-reviewer agent §2.2 触发
- 每次 .vue 文件改动有 v-html
- Phase 03 → 04 准入前

---

## 2. 3 类风险扫描

### 2.1 Vue `v-html` 直接渲染

```bash
# 扫 .vue 文件 v-html 用法
grep -rn 'v-html' plm-frontend/src/ \
  | tee /tmp/xss-vhtml.txt
```

每个 v-html 都需审:
- 渲染的内容来源? (静态字符串 / 后端返回 / 用户输入)
- 是否已 escape (DOMPurify / 后端 markdown 渲染器 sanitize)?

### 2.2 JS / TS `innerHTML` 赋值

```bash
grep -rnE '\.innerHTML\s*=|\.outerHTML\s*=' plm-frontend/src/
```

innerHTML 赋值 + 用户输入数据 = 高危。

### 2.3 后端 @ApiResponse 含 HTML / Markdown

```bash
grep -rn '@ApiResponse' plm-backend/*/src/main/java/ | grep -iE 'html|markdown|raw'
```

如果后端返回 raw HTML, 前端渲染需 sanitize。

---

## 3. 输出 — §2 XSS 风险表

```markdown
## §2 XSS 风险

### 2.1 Vue v-html 审查

| 文件 | 行号 | 数据源 | 是否 sanitize | 风险 |
|---|---|---|---|---|
| DocumentDetail.vue | 67 | doc.content (后端返回) | ❓ 未确认 | 中 |
| NoticeBanner.vue | 23 | "<b>系统公告</b>" (静态) | N/A | 🟢 低 (静态) |

### 2.2 innerHTML 赋值审查

| 文件 | 行号 | 数据源 | 风险 |
|---|---|---|---|
| (无) | — | — | ✅ |

### 2.3 后端 raw HTML 返回

| 端点 | 字段 | 是否预先 sanitize | 建议 |
|---|---|---|---|
| GET /document/{id} | content (markdown) | 后端 commonmark-java 渲染含 raw HTML | 前端 v-html + DOMPurify |

### 风险评级
- 🟢 全部 v-html 数据源已 sanitize → ✅
- 🟡 1-2 处 v-html 未确认 sanitize → ⚠️ Phase 04 前 fix
- 🔴 ≥ 1 处 innerHTML + user input 直接赋 → ❌ P0
```

---

## 4. 推荐解法 (per OWASP)

```typescript
// 前端: 用 DOMPurify
import DOMPurify from 'dompurify';
const safeHTML = DOMPurify.sanitize(userInput);

// 或: 不用 v-html, 改 markdown 组件
import { Markdown } from '@vue-markdown/component';
<Markdown :content="doc.content" />
```

```java
// 后端: commonmark-java + html-renderer 启用 sanitize
HtmlRenderer renderer = HtmlRenderer.builder()
    .sanitize(true)
    .build();
```

---

## 5. 衔接

| 上游 | xss-scan | 下游 |
|---|---|---|
| frontend-coder Vue 改动 | → 扫 v-html | → §2 报告 |
| backend-coder ApiResponse | → 扫 raw HTML 返回 | → 前后端协作修 |

---

## 6. 反模式

- ❌ `<div v-html="userInput"></div>` 无 sanitize
- ❌ `element.innerHTML = userInput`
- ❌ 富文本编辑器 (TinyMCE / CKEditor) 后端不二次 sanitize
- ❌ 假设"内部用户不会攻击" (内部威胁照样存在)

---

## 7. 历史

| v0.1 | 2026-05-19 | 首版; security-reviewer 4 配套 之 2 |
