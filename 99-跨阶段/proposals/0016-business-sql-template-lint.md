# Proposal 0016: business-*.sql 模板 lint + schema 改动下游影响扫描

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0016 |
| 标题 | business-*.sql 模板 lint + sys_menu URL 改动→前端硬编码 grep |
| 状态 | draft |
| 类型 | 工具链(号段 0200-0299) |
| 提出人 | Claude(/reflect-weekly meta-cognitive) |
| 提出日期 | 2026-05-25 |
| 评审人 | Wjl(solo-review) |
| 评审日期 | _待定_ |
| Tracking 截止 | 2026-06-22(merged 后 4 周) |

---

## 1. 背景(What's the problem?)

2026-05-25 单日踩两类同根坑:① `81bc1ba` UED 模块 `business-ued.sql` 漏写 `INSERT INTO sys_menu` / `sys_role_menu`,前端跑出来没入口;② `5c4e70d` + `7b14807` 揭示 `menu-regroup-by-phase.sql` 把 sys_menu path 列改了之后,前端 14+ 处硬编码 `router.push('/business/<entity>')` 大面积失效,事后抽 `businessRoute.ts` 做症状治疗。共同根因:**所有 schema-driven 改动**(business-*.sql / menu-*.sql / 字典扩展)缺"下游影响清单生成器"。现有 `.githooks/commit-msg` 只校 Conventional Commits 头,**对 SQL 模板完整性与下游硬编码影响零兜底**。

---

## 2. 证据(Evidence)

- 关联 [reflect/2026-W22-modules-bulk-uplift.md](../reflect/2026-W22-modules-bulk-uplift.md):模式 2(Schema-driven N 阶导数破坏)+ 模式 3(business-*.sql 模板不完整) → 行动 A3/A4 明确指向本提案。
- 关联 commits:
  - `81bc1ba` "补 business-ued.sql 漏写的 sys_menu INSERT" — 漏菜单首例显形。
  - `5c4e70d` "dashboard 按钮 404 — 适配 menu-regroup-by-phase 后的 8 阶段 URL" — schema 改动 N 阶导数破坏首例。
  - `7b14807` "抽 entity→phase SSoT utils, 修 sprint 同类 /business 硬编码" — 同坑横向扩散证明,事后症状治疗。
- 关联 [memory/project-quirks.md](../../.claude/projects/D---12-trae--06-----------plm/memory/project-quirks.md):**Q-DB-04**(business-*.sql 漏 sys_menu / sys_role_menu INSERT)+ **Q-BIZ-04**(sys_menu URL 改动后前端硬编码失效)。
- 数据级事实:仓库 44 个 `business-*.sql` 中仅 13 个含 `INSERT INTO sys_menu`,31 个无 — 部分应豁免(字典扩展、子表 add-column、回滚脚本),但**当前无机制区分"豁免"与"漏写"**。

---

## 3. 提案(What's the change?)

### 改动文件清单

| 文件 | 改动类型 |
|---|---|
| `.githooks/pre-commit` | 新增(0 → 1 个 hook 文件) |
| `CLAUDE.md` 6-gotcha 表 | 修改(扩为 7 gotcha) |

### Diff 草案

**Diff 1:`.githooks/pre-commit`(新建,bash + Windows Git Bash 兼容)**

```bash
#!/usr/bin/env bash
# .githooks/pre-commit
# lint 1: business-*.sql 必须含 sys_menu INSERT,否则要求顶部 -- @no-menu 豁免注释
# lint 2: sql/menu-*.sql 或 sql/business-*.sql 的 path 列改动 → grep 前端硬编码 /business/ 路由
# 失败行为:exit 1 阻塞 commit;允许 git commit --no-verify bypass(进 signals 月度计数)
#
# Windows 兼容:Git for Windows 自带 bash,本脚本 sh-only,无 GNU 扩展依赖。
# 启用方式(首次 clone):git config core.hooksPath .githooks

set -e

# 收集本次 staged 文件
STAGED=$(git diff --cached --name-only --diff-filter=ACMR)

# ---------- lint 1: business-*.sql 模板完整性 ----------
SQL_FILES=$(echo "$STAGED" | grep -E '^plm-backend/sql/business-[^/]+\.sql$' || true)

if [ -n "$SQL_FILES" ]; then
  FAIL_LIST=""
  for f in $SQL_FILES; do
    # 跳过 rollback 脚本(命名约定 -rollback.sql)
    case "$f" in *-rollback.sql) continue ;; esac

    # 跳过显式豁免(顶部 50 行内出现 -- @no-menu 注释)
    if head -n 50 "$f" 2>/dev/null | grep -qE '^--\s*@no-menu\b'; then
      continue
    fi

    # 必含 sys_menu INSERT(任一即可)
    if ! grep -qE 'INSERT\s+INTO\s+sys_menu' "$f"; then
      FAIL_LIST="${FAIL_LIST}  - $f\n"
    fi
  done

  if [ -n "$FAIL_LIST" ]; then
    printf "\n[lint 1] business-*.sql 缺 sys_menu INSERT:\n" >&2
    printf "$FAIL_LIST" >&2
    cat <<'EOF' >&2

修复方式(任选一):
  a) 补 INSERT INTO sys_menu / sys_role_menu(同 business-requirement.sql 范式)
  b) 若仅扩字典/子表/回滚,在 SQL 顶部 50 行内加注释:
       -- @no-menu: 本 SQL 仅扩 <字典/子表/...>, 不挂菜单
  c) 明知故犯绕过:git commit --no-verify(将计入 signals 月度 bypass)

参考:proposals/0016 / memory/project-quirks.md Q-DB-04
EOF
    exit 1
  fi
fi

# ---------- lint 2: sys_menu URL 改动 → 前端硬编码 grep ----------
MENU_CHANGED=$(echo "$STAGED" | grep -E '^plm-backend/sql/(menu-|business-).*\.sql$' || true)

if [ -n "$MENU_CHANGED" ]; then
  # 仅当 diff 涉及 path 列(粗判:含 `path` 关键字的行)
  if git diff --cached -- $MENU_CHANGED 2>/dev/null | grep -qE "^\+.*['\"]?path['\"]?"; then
    # grep 前端硬编码 — 排除 api 层与 mocks
    HITS=$(grep -rnE "['\"]/business/[a-z][a-z0-9-]*" \
             plm-frontend/src/views 2>/dev/null \
             | grep -v -E '/(api|__mocks__)/' || true)
    if [ -n "$HITS" ]; then
      printf "\n[lint 2] sys_menu path 改动检测到,以下前端硬编码可能受影响:\n" >&2
      echo "$HITS" >&2
      printf "\n请确认是否需要同步更新(或确认无影响)。粘贴上述清单到 commit body 后重新 commit。\n" >&2
      printf "参考:proposals/0016 / memory/project-quirks.md Q-BIZ-04\n" >&2
      exit 1
    fi
  fi
fi

exit 0
```

**Diff 2:`CLAUDE.md` 6-gotcha 表扩为 7 gotcha**

```diff
--- a/CLAUDE.md
+++ b/CLAUDE.md
@@ -XX,6 +XX,7 @@ The 6 gotchas, one-liner(完整 quirks 知识库在 [memory/project-quirks.md]
 | 4 | `Failed to resolve import @/utils/ruoyi` on certain frontend pages | `vite/plugins/auto-import.ts` was missed during rename — re-run sed there | 1 |
 | 5 | `mvn install` fails: `Unable to rename '.../plm-admin.jar'` ... | Backend 在跑锁住 jar — 先 taskkill ... | 9+ |
 | 6 | Backend 抛 ServiceException 字符串但 grep 当前代码无此字符串 | Stale JVM ... | 1 |
+| 7 | sys_menu path 列改动后,前端 `/business/<entity>` 按钮 404 大面积复发 | `.githooks/pre-commit` lint 2 已自动 grep `plm-frontend/src/views/**` 给出清单;路由必须经 `src/utils/businessRoute.ts` SSoT,**禁止硬编码 `router.push('/business/...)`**。复发 2+ | 2+ |
```

---

## 4. 影响范围(Impact)

| 受众 | 影响 |
|---|---|
| 开发者 | 每次 commit 多 ~ <1s lint 等待;首次 clone 需 `git config core.hooksPath .githooks`(此条已在 CLAUDE.md "Tool-enforced" 段) |
| Claude | 写 business-*.sql 时需主动判断挂菜单 or 加 `@no-menu` 豁免;改 sys_menu path 时需先跑前端 grep。下次会话起 CLAUDE.md gotcha #7 生效。 |
| 测试 / 运维 | 无 |
| 已有代码 / 文档 | 44 个现存 business-*.sql 文件需 audit:13 个已含 sys_menu(豁免);估计 ~5 个需补 `-- @no-menu` 豁免注释(如 `business-prd-add-requirement-id.sql` 子表 add-column 类);其余为 rollback(按命名跳过)。先 audit 再回填,见 §7 Step 1。 |

---

## 5. 风险(Risks)

- **风险 1 — 误报**:字典/子表/AI 日志类 SQL 不该挂菜单,lint 漏判为"漏写"。**缓解**:`-- @no-menu: <原因>` 豁免注释机制,顶部 50 行内出现即跳过。
- **风险 2 — bypass 滥用**:`--no-verify` 用多了反而麻木,lint 形同虚设。**缓解**:bypass 计数纳入 signals 月报已有 `commit_bypass_count` 维度,> 3 次/月 触发 reflect 单独审视(见 [.claude/rules.md §L](../../.claude/rules.md))。
- **风险 3 — grep 范围误判**:`/business/xxx` 出现在 axios api 层是后端 URL,不应改。**缓解**:grep 限定 `plm-frontend/src/views/**`,排除 `/api/` 与 `/__mocks__/`(脚本里已 `grep -v -E '/(api|__mocks__)/'`)。
- **风险 4 — Windows shell 兼容**:Git for Windows bash 与 Linux bash 在 `grep -E` POSIX 字符类、`head -n 50` 边界、`set -e` 下 pipefail 行为略有差异。**缓解**:本脚本仅用 POSIX sh 子集,无 GNU 扩展;预上线 Step 4 在 Windows + Linux 双跑模拟 commit 验证。

---

## 6. 备选方案(Alternatives Considered)

- **方案 A — 不做**:已知 81bc1ba / 5c4e70d 类坑会按 W22 reflect 模式 2/3 周期性复发,与"工程化项目"定位不符。
- **方案 B — 仅做 lint 1 不做 lint 2**:成本最低但治标不治本,5c4e70d 类 schema 改动 N 阶导数破坏仍无防线。**不选**。
- **方案 C — 加 lint 3 字典 grep**:`useDict("xxx")` 全局校验。**划入候选 proposal 0018 (schema-impact-scanner)**,本期不立项 — A3+A4 落地 4 周内观察,若 schema 类下游破坏仍 ≥ 1 次再启动。
- **方案 D — 用 Claude Code PreToolUse hook 代替 git pre-commit**:仅在 Claude 写文件时拦截,人手动 commit 不拦。**不选** — 对人/AI 不一视同仁,且与 `.githooks/commit-msg` 一脉相承的路径更短。

---

## 7. 实施计划(Implementation Plan)

```
[ ] Step 1: audit 44 个现存 business-*.sql,产出"已含 menu(13)/ 应豁免(预计 ~5)/ 漏菜单(剩余待确认)"清单 — Claude(0.5h)
[ ] Step 2: 新建 .githooks/pre-commit(本提案 §3 Diff 1)+ 给 chmod +x — Claude(0.5h)
[ ] Step 3: 编辑 CLAUDE.md 6-gotcha 表 → 7 条;在 "Tool-enforced" 段强调首次 clone 必跑 `git config core.hooksPath .githooks` — Claude(0.2h)
[ ] Step 4: 本地双跑验证 — 模拟还原 81bc1ba(去掉 sys_menu INSERT)与 5c4e70d(改 menu path)两个 commit,确认均被 hook 拦下 — Claude + Wjl 联合(0.5h)
[ ] Step 5: 给 Step 1 audit 出的 ~5 个应豁免文件补 `-- @no-menu: <原因>` 注释,单独 commit `chore(sql): 标注 N 个 SQL 文件 @no-menu 豁免注释 [solo-review]` — Claude(0.3h)
[ ] Step 6: 合规范变更 PR(本 proposal 状态 draft → proposed → accepted → implementing → merged)— Wjl
[ ] Step 7: 进入 tracking 期(merged + 4 周),按 §8 信号观察
```

---

## 8. 衡量指标(How will we know it worked?)

> 跟踪期:`2026-05-25` ~ `2026-06-22`(merged 后 4 周)。

- **信号 1 — lint 触发计数**:基线 0(hook 不存在),目标 4 周内 ≥ 5 次正常触发(证明 schema 改动有日常发生且 lint 在工作)。数据源:Stop hook 增量记录 / commit body grep `[lint:`。
- **信号 2 — lint 拦下"漏菜单"类问题次数**:基线 W22 出现 1 次(81bc1ba),目标 ≥ 1 次成功拦截(证明 lint 实际预防了真问题)。数据源:开发者 commit 失败重提日志。
- **信号 3 — bypass(`--no-verify`)使用次数**:目标 ≤ 1 次/月。> 3 次触发月报审视 hook 是否过于严厉。数据源:`commit_bypass_count`(signals 已有维度)。
- **信号 4(辅)— sys_menu URL 改动后前端 404 类 bug**:基线 W22 出现 2 次(5c4e70d + 7b14807),目标 4 周内 0 复发。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| | 通过 / 有条件通过 / 不通过 | | |

---

## 10. 实施后跟踪(merged 后填)

### 实际 PR / commit
- PR: __
- 合入 commit: __
- 实际 merged 日期:YYYY-MM-DD

### Tracking 数据

| 信号 | 基线 | 目标 | 实际(周 1)| 实际(周 2)| 实际(周 3)| 实际(周 4)|
|---|---|---|---|---|---|---|
| lint 触发计数 | 0 | ≥ 5 | | | | |
| 拦下"漏菜单"次数 | 0 | ≥ 1 | | | | |
| `--no-verify` 次数 | n/a | ≤ 1/月 | | | | |
| sys_menu 改动后 404 | 2(W22) | 0 | | | | |

### 最终判定
- [ ] done(达成目标,本提案归档)
- [ ] reverted(未达成 → 走回滚 PR,并在此段写"为什么失败")

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-25 | Claude(/reflect-weekly) | V1.0 — 初稿,从 reflect/2026-W22 行动 A3+A4 派生 |
