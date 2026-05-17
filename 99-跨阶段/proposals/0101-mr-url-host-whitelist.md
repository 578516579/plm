# Proposal 0101: Task MR URL 字段加 host 白名单校验，防钓鱼链接

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0101 |
| 标题 | Task / Defect / 其他业务模块的"外部 URL" 字段（MR / PR / issue link 等）必须走 host 白名单校验 |
| 状态 | **merged → tracking** |
| 类型 | 编码规范（安全相关）|
| 提出人 | Wjl + Claude（reflect/2026-W21 批量升格）|
| 提出日期 | 2026-05-17 |
| 来源 | signals 候选 **0025** |
| 评审截止 | 2026-05-24 |
| Tracking 截止 | merged 后 2 周 |

---

## 1. 背景

Task 模块 Phase 03 §J friction 1：Task 实体的 `mergeRequestUrl` 字段当前只做 URL 格式校验（含 `://`），不限制 host。

风险：
- 内部 PM 把任意外部链接（钓鱼站 / 恶意页）填到 MR URL 字段
- 其他人在 Task 详情页点击跳转 → 中招
- 内部 PLM 系统变成钓鱼跳板

PLM 适用场景：MR URL 只应指向团队 git 平台（GitLab / Gitee / GitHub 内部组织），白名单 host 即可阻断 99% 钓鱼场景。

---

## 2. 证据

- 关联 signals: [2026-05.md](../signals/2026-05.md) 候选 0025
- 关联 reflect: [reflect/2026-W20.md](../reflect/2026-W20.md) B2
- 关联 Gate 实例: Task Phase 03 §J friction 1
- 适用扩展：Defect 实体的 `referenceUrl`、Document `externalLink` 等任何业务字段含 URL 都适用

---

## 3. 提案

### 3.1 改 `03-开发/开发规范.md` 加 §Y "外部 URL 字段白名单"

```diff
+## §Y. 外部 URL 字段白名单（强制，proposal 0101）
+
+任何业务实体含"外部 URL"字段（命名后缀 `Url`/`Link`/`Href`）必须：
+
+1. 在 application.yml 配 `plm.url.allowed-hosts: <host-list>`（默认空，强制配置）
+2. ServiceImpl 入口（add / edit）校验 URL host ∈ allowed-hosts
+3. 校验失败抛 `ServiceException(code=708, "URL host 不在白名单")`
+4. 配置项分环境（dev/staging/prod），dev 可放宽（如 + `localhost`）
+
+错误码 708 已在 [PRD-MAPPING.md §4](../PRD-MAPPING.md) 登记。
+
+### 适用清单（当前业务模块）
+
+| 模块 | URL 字段 | 默认白名单（example）|
+|---|---|---|
+| Task | `mergeRequestUrl` | gitlab.内网域 / gitee.com |
+| Defect | `referenceUrl` | 同上 |
+| Document | `externalLink` | 团队 wiki 域 |
+
+### 工具
+
+- ServiceImpl 复用 `cn.com.bosssfot.dv.plm.common.utils.UrlValidator.checkHost(url, allowedHosts)`（待实现，已记入 Sprint backlog）
```

### 3.2 同步更新 `.claude/rules.md` §C（凭据 / 安全段）— 加 1 行

```diff
 - **永远不要**把真实 password / token / API key / JWT secret 写进 yml 或代码。
 ...
+- 业务实体含外部 URL 字段（命名后缀 Url/Link/Href）必须 host 白名单校验（per [proposal 0101](../99-跨阶段/proposals/0101-mr-url-host-whitelist.md)）
```

---

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| Task / Defect / Document 等已含 URL 字段的模块 | ServiceImpl 加 host 校验；application.yml 加 `plm.url.allowed-hosts` |
| 业务模块生成器 | 模板默认含 URL 字段时自动产 host 校验代码（W22 验证） |
| dev 环境 | 默认白名单含 localhost，避免开发期 friction |

---

## 5. 风险

- **风险 1**: 白名单配置遗漏 → 业务方加内部新 git 平台时不在白名单。**缓解**: 校验失败的 error message 提示"运维更新 allowed-hosts"。
- **风险 2**: 子域名匹配复杂（`*.gitlab.com` vs `gitlab.com`）。**缓解**: utils 用现成库（如 `InternetDomainName.from(host).topPrivateDomain()`）。

---

## 6. 备选方案

- A: 客户端 (前端) 校验 — 不选，客户端可绕过
- B: 黑名单 — 不选，黑名单永远不全
- C（选定）: 服务端白名单 + 错误码 708 显式拒绝

---

## 7. 实施计划

```
[x] Step 1: 写 proposal
[x] Step 2: 评审 — 2026-05-17 [solo-review] (per 0040 §3.5)
[x] Step 3a: 按 0041 §3.1 第 4 checkbox 执行 grep 现存 URL 字段:
    - Task.mrUrl ✓ (proposal §Y 字段名错 - 实际 mrUrl 非 mergeRequestUrl)
    - ManualProduct.screenshotsUrls ✓ (proposal §Y 未列, CSV 形态)
    - Defect.referenceUrl ❌ (不存在)
    - Document.externalLink ❌ (不存在)
[x] Step 3b: 落地规范 (含 grep 修正后的实际字段表):
    - 03-开发/开发规范.md §1.10 (单值 + CSV 两种校验形态)
    - .claude/rules.md §C 加 1 行
[ ] Step 4: 实现 UrlValidator 工具类 + Task/ManualProduct Service 加校验 → BL-2026-007 (P1, W22)
[ ] Step 5: 配 application*.yml 白名单 → BL-2026-008 (P1, W22, 与 007 同 Sprint)
[ ] Step 6: tracking 期看新业务模块 URL 字段是否 100% 加校验
```

---

## 8. 衡量指标

| 信号 | 基线 | 目标 |
|---|---|---|
| 业务实体 URL 字段含 host 校验比例 | 0%（Task/Defect 当前 0）| 100%（含 Task/Defect/Document）|
| 错误码 708 命中次数（W22+ 抽样）| N/A | ≥ 1（验证白名单生效）|

Tracking 期: merged 后 2 周。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl `[solo-review]` | ✅ 通过 | 2026-05-17 | 编码规范-安全类首落地; 同次 commit 验证 0041 §3.1 第 4 checkbox (grep 现存代码), 捕获 spec drift (3 个想象字段 → 2 个真实字段); 规范层通过, 代码实施延后 BL |
| Claude | ✅ 实施 (规范层) | 2026-05-17 | 按 0040 §3.1 先 Read 03-开发/开发规范.md (§1.9 后/§2 前) + .claude/rules.md §C 起首行。**按 0041 §3.1 第 4 checkbox 先 grep 现存代码** → 修正 §Y 适用清单 |

> Solo 单签理由：URL 白名单是显然必要的安全规范，无争议；W21 grep 验证现存代码后清单从想象走向事实。UrlValidator 实现 + Task/ManualProduct host 校验依赖运维白名单决策 → 延后 BL，规范本身 W21 就 lock。

---

## 10. 实施后跟踪（已 merged 规范层）

### 实际合入
- 合入 commit: 待 commit 后回填
- 实际 merged 日期：2026-05-17（规范层；代码实施延后）

### 派生迁移项 (per 0041 §3.1 第 4 checkbox)

| BL ID | 任务 | 优先级 | 工作量估 | 责任人 |
|---|---|---|---|---|
| BL-2026-007 | 实现 `cn.com.bosssfot.dv.plm.common.utils.UrlValidator` + Task/ManualProduct Service 加 host 校验 | P1 | M (含 unit test) | TBD |
| BL-2026-008 | 配 application-dev/staging/prod.yml 的 `plm.url.allowed-hosts.task` / `.manualProduct` 白名单 | P1 | S (含与运维同步) | TBD |

### Tracking 数据

| 信号 | 基线 | 目标 | W21 | W22 | W23 |
|---|---|---|---|---|---|
| 业务实体 URL 字段含 host 校验比例 | 0% (Task/ManualProduct 当前 0) | 100% | 规范已建, 代码待 BL | 待填 | 待填 |
| 错误码 708 命中次数 | 0 | ≥ 1 (验证白名单生效) | 0 (UrlValidator 待实现) | 待填 | 待填 |
| 新业务模块 URL 字段加校验比例 | N/A | 100% | 业务模块生成器待更新 | 待填 | 待填 |

Tracking 期: 2026-05-17 ~ 2026-05-31。

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-17 | Wjl + Claude | 首版从 signals 候选 0025 升格 |
| 2026-05-17 | Wjl `[solo-review]` + Claude | 同日 solo-review accept + apply (规范层) per 0040 §3.5. **按 0041 §3.1 第 4 checkbox grep 现存代码捕获 drift**: §Y 原列 Defect.referenceUrl + Document.externalLink **均不存在**; Task 字段名 mergeRequestUrl 实为 mrUrl; 漏列 ManualProduct.screenshotsUrls (CSV)。03-开发/开发规范.md §1.10 含 grep 修正后的事实字段表 + CSV 形态校验; .claude/rules.md §C 加 1 行。代码实施 → BL-2026-007/008 (W22)。状态 proposed → merged → tracking |
