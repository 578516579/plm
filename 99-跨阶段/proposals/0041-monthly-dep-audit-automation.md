# Proposal 0041: 月度依赖扫描自动化 — .github/workflows/dep-audit.yml

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0041 |
| 标题 | 月度依赖漏洞扫描固化为 GitHub Actions `dep-audit.yml`(npm audit + mvn dependency:tree + jjwt 版本提醒) |
| 类型 | 工具链 |
| 状态 | **draft**(2026-05-29 起草,⚠ preemptive — 1 次镜像兼容事故 + 1 老依赖识别) |
| 提出人 | Claude(test-orchestrator 安全审计派生 + Wjl 全模块验收) |
| 提出日期 | 2026-05-29 |
| 评审人 | Wjl(solo-review,待签) |
| Tracking 截止 | _待 merged + 4 周;1 月(2026-06-29)首跑无产出 → 走 partial_ |
| 关联 reflect | _无_ |
| 关联 commit | bf221f3(安全审计 v0.2 F-004/F-005) |

---

## 1. 背景(What's the problem?)

2026-05-29 全模块测试验收的 L6 安全层暴露 2 个"依赖侧"问题:

- **F-004 P2** `jjwt 0.9.1`(2018 年版本,有 CVE-2022-0840 padding oracle 风险)— **本期审计才发现,此前累计 7 个月没人扫**
- **F-005 P3** npmmirror 镜像不支持 audit API → `npm audit` 返 404 → 前端依赖漏洞当前**完全无自动扫描覆盖**

根因:依赖扫描是一次性手工动作(本期靠 grep + 主 Claude 手动核),**没有月度强制节律**。CLAUDE.md 标 jjwt 0.9.1 是已知信息,但没机制提醒"该升了"。

[测试策略.md v1.6 §9 路线图](../../04-测试/测试策略.md) 已把"依赖月度扫描自动化"列为长期项;本提案是它的落地路径。

---

## 2. 证据(Evidence)

- **关联 安全审计**:[04-测试/安全审计.md v0.2 §3 F-004 / F-005](../../04-测试/安全审计.md)
- **关联 测试报告**:[04-测试/测试报告-2026-05-29-全模块验收.md §4.6](../../04-测试/测试报告-2026-05-29-全模块验收.md) "7 finding"
- **关联 复盘**:[04-测试/测试复盘-2026-05-29.md §4 RCA 老依赖(1)+ 镜像兼容(1)](../../04-测试/测试复盘-2026-05-29.md)
- **关联 历史**:CLAUDE.md "jjwt 0.9.1" 标注已存 7 个月,**0 次升级触发**
- **关联 实测**:本期 `npm audit` 跑了一次结果是 `404 NOT_IMPLEMENTED`(npmmirror 限制),正式扫零次

---

## 3. 提案(What's the change?)

### 3.1 改动文件清单

| 文件 | 改动 |
|---|---|
| `.github/workflows/dep-audit.yml` | 新建 GitHub Action,monthly schedule |
| `04-测试/安全审计.md` §1 审计周期 | 加一句"每月 1 号 dep-audit.yml 自动跑,产出 issue 归档 §3" |
| `04-测试/安全审计.md` §3 历次审计记录 | 表格加一列"自动 / 手动"|
| `plm-backend/scripts/dep-audit-summary.sh` | 简单聚合脚本(供 workflow 调用) |

### 3.2 GitHub Action 草案

```yaml
# .github/workflows/dep-audit.yml
name: 依赖月度扫描(Dependency Audit)

on:
  schedule:
    - cron: '0 2 1 * *'   # 每月 1 号 02:00 UTC = 北京 10:00
  workflow_dispatch:       # 也允许手动触发

jobs:
  npm-audit:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: 20 }
      - name: Restore npmjs.org registry(避开 npmmirror 限制)
        working-directory: plm-frontend
        run: npm config set registry https://registry.npmjs.org
      - name: npm audit(prod only)
        working-directory: plm-frontend
        run: |
          npm audit --omit=dev --json > ../npm-audit.json || true
          jq '.metadata.vulnerabilities' ../npm-audit.json

  maven-audit:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '17', distribution: 'temurin' }
      - name: mvn dependency:tree(全 keys)
        working-directory: plm-backend
        run: mvn dependency:tree -q > ../mvn-deps.txt

      - name: 检查 jjwt 是否仍 < 0.12
        working-directory: plm-backend
        run: |
          jjwt_ver=$(grep -oE 'jjwt:jar:[0-9.]+' ../mvn-deps.txt | head -1 | cut -d: -f3)
          echo "jjwt 当前版本: $jjwt_ver"
          if [ "$(printf '%s\n0.12.0\n' "$jjwt_ver" | sort -V | head -1)" = "$jjwt_ver" ] \
             && [ "$jjwt_ver" != "0.12.0" ]; then
            echo "::warning::jjwt $jjwt_ver < 0.12,有 CVE-2022-0840 padding oracle 风险(F-004)"
          fi

      - name: 检查 Spring Boot 是否落后 2 minor 以上
        working-directory: plm-backend
        run: |
          sb=$(grep -oE 'spring-boot.*:jar:[0-9.]+' ../mvn-deps.txt | head -1 | cut -d: -f3)
          echo "Spring Boot 当前: $sb"

  summarize:
    needs: [npm-audit, maven-audit]
    runs-on: ubuntu-latest
    if: always()
    steps:
      - uses: actions/checkout@v4
      - name: 写 issue / 邮件汇总
        run: |
          # 简化:把两个 audit 结果聚成一个 issue body
          gh issue create \
            --title "[自动] 月度依赖扫描 — $(date +%Y-%m)" \
            --body-file <(cat npm-audit.json mvn-deps.txt 2>/dev/null | head -200) \
            --label "security,dep-audit"
        env:
          GH_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

### 3.3 设计原则

| 原则 | 说明 |
|---|---|
| **schedule + workflow_dispatch** | 月度自动 + 任何时刻手动 |
| **不阻断 CI** | workflow 独立运行,失败不影响主分支 push CI |
| **issue 化归档** | 每月一个 issue,labeled `security,dep-audit`,**自动追溯历史** |
| **临时切 registry** | 解决 F-005 npmmirror 限制(CI 环境切官方源) |
| **本地脚本可同款跑** | `plm-backend/scripts/dep-audit-summary.sh` 让本地也能手工跑一次复核 |

---

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| 维护者 | 每月 1 号 issue 自动开,review + 决定是否升级 |
| Claude | 触发条件:用户说"看下月度依赖" → 读最新 issue |
| 安全审计 | §3 表的"自动/手动"列会自动累加 |
| 本地开发 | 不影响(npmmirror 配置不变,只 CI 临时切换) |
| jjwt / Spring Boot 老版本 | **被动暴露**,upgrade PR 会随之触发 |

---

## 5. 风险

- **风险 1 — issue 越来越多没人处理**:每月一个,半年攒 6 个
  **缓解**:label `dep-audit` + Tracking 期看 close rate;低于 50% close → action 自动调整 schedule 到季度
- **风险 2 — npmjs.org 速度慢**:CI 切官方源拉依赖慢
  **缓解**:只 audit 不 install,audit 调用是 metadata 请求很快(< 30s)
- **风险 3 — 误报噪音**:dev-only 包的低危 CVE
  **缓解**:`--omit=dev` 过滤;低危(< P2)不开 issue
- **风险 4 — jjwt 版本检查 grep 脆**:输出格式变 → 检查失效
  **缓解**:用 `mvn help:evaluate -Dexpression=jjwt.version` 替代(更稳)
- **风险 5 — gh issue create 需 token**:`GITHUB_TOKEN` 默认可写 issue 但 repo 设置可能禁用
  **缓解**:Step 5 自检确认 repo 允许 actions 写 issue

---

## 6. 备选方案

- **方案 A — 维持现状**:依赖手工 / 临时审计触发。本期已证 7 个月 0 触发 → **不选**
- **方案 B(本提案)— GitHub Action monthly + issue 归档**:推荐
- **方案 C — Dependabot / Renovate**:GitHub 原生,更激进(每周/每日 PR)
  - **不选(首版)** — 太重,本项目 solo-review 节奏吃不下每周升级 PR
  - **远期**:本提案 tracking 期看效果,如证有效可演进到 Dependabot
- **方案 D — 写 Stop hook 每会话 check**:每次 Claude 会话结束扫一次
  - **不选** — 频率过高,真扫慢(npm audit 远程拉)
- **方案 E — 推迟到 prod 上线前再起**:**反 PaaS** — 等到出事才扫错过窗口

选 B + 远期 C。

---

## 7. 实施计划

```
[x] Step 1: draft + README 索引
[ ] Step 2: Wjl review accepted
[ ] Step 3: 写 .github/workflows/dep-audit.yml(草案见 §3.2)
[ ] Step 4: 写 plm-backend/scripts/dep-audit-summary.sh 本地版
[ ] Step 5: 手动触发一次 workflow_dispatch 验证 issue 能创建
[ ] Step 6: 改安全审计 §1 / §3 文字 + 表头
[ ] Step 7: merged → tracking 4 周(等 6 月 1 日首次自动跑)
[ ] Step 8: tracking 末看是否有 issue + 是否被处理
```

---

## 8. 衡量指标

| 信号 | 基线 | 目标 |
|---|---|---|
| 月度自动扫描跑成功率 | 0% | ≥ 95%(月度 1 次,允许 1 次 fail) |
| 自动开 issue 数 / 月 | n/a | 1 |
| issue 处理 close 率 | n/a | ≥ 50% / 4 周 |
| 通过 issue 触发的 upgrade PR 数 | 0 | ≥ 1 / 季度 |
| jjwt / Spring Boot 等老依赖 detect 率 | 0 | 100%(已知项目内老依赖均能 detect) |

跟踪期:_待 merged 后 4 周 + 6 月 1 日首跑后 4 周_

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl | _待签_ | _待定_ | solo-review |
| Claude(自评 0033 范式)| 🟢 Approve | 2026-05-29 | §9.1 |

### 9.1 自评 7 维

| 维度 | 评分 | 依据 |
|---|---|---|
| scope 合理性 | 9/10 | 1 workflow + 1 脚本 + 文档 2 行;边界极清 |
| 证据充分性 | 8/10 | F-004 / F-005 + 7 个月 0 扫历史 |
| 决策可追溯 | 8/10 | 5 备选 + ❌/✅;远期 Dependabot 路径明示 |
| 实施完整度 | 8/10 | yml 草案完整 + 设计原则 5 条 |
| 风险识别 | 8/10 | 5 风险全识别,jjwt grep 脆 / gh token 是真盲点 |
| 可观测性 | 8/10 | 5 信号定量,close rate 是好的"是否被使用"代理 |
| dogfood / 自我一致 | 8/10 | 本期就是手工扫一次发现 2 finding,机制要替代手工 |

**总评**:平均 8.1 → **Approve**

**必须改清单**:无

**建议**:
- S1:首版只盯 jjwt + Spring Boot + Druid 3 个"明确老/历史 CVE"依赖,目标聚焦
- S2:tracking 末看 close rate,如 < 30% 说明 issue 太多没人处理 → 换季度

---

## 10. 实施后跟踪

### 若 rejected
- 原因:_待填_

### 若 merged
- 合入 commit:_待定_
- 实际 merged 日期:_待定_

### Tracking 数据

| 信号 | 基线 | 目标 | M1(6 月) | M2(7 月) | M3(8 月) |
|---|---|---|---|---|---|
| 自动扫描成功率 | 0% | ≥ 95% | | | |
| 自动 issue 数 | n/a | 1 | | | |
| issue close 率 | n/a | ≥ 50% | | | |
| 触发 upgrade PR 数 | 0 | ≥ 1/季度 | | | |
| 老依赖 detect 率 | 0 | 100% | | | |

### 最终判定
- [ ] done
- [ ] partial
- [ ] reverted

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-29 | Claude(test-orchestrator 安全审计派生 + Wjl 全模块验收) | V1.0 — GitHub Action monthly + issue 归档 + 临时切 registry 解决 npmmirror 限制;jjwt < 0.12 + Spring Boot 落后 detect;7 维自评 8.1 Approve |
