# Proposal 0032: 早期项目部署链路 — staging 路径明示 + deploy.sh 自动化

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0032 |
| 标题 | early 阶段无独立 staging 时模板的明确路径 + 沉淀 `deploy.sh` 自动化上线脚本 |
| 状态 | **proposed** |
| 类型 | 流程 + 工具链（混合）|
| 提出人 | Wjl + Claude（reflect/2026-W21 批量升格）|
| 提出日期 | 2026-05-17 |
| Bundle | 本提案合并 signals 候选 **0032 + 0033** |
| 评审截止 | 2026-05-31 |
| Tracking 截止 | merged 后 2 周 |

---

## 1. 背景

Project Phase 05 §J friction：
- **0032 路径不明**：模板 §B.3 假设有 staging。early 阶段没 staging，实例只能 §I 写"dev 替代 staging"，但何时该升 staging 模板没说。
- **0033 手工部署**：当前"打 jar → scp → systemctl restart → 看 log" 全靠手敲。每个模块上线都重复同样的命令，手敲容易漏步骤（如忘备份）。

两个候选其实是一个故事：early 不该有 staging（成本不划算），但**当转入 stable 时**需要 staging + 必须 deploy.sh 自动化（避免手工漏步骤）。所以这两个候选应该 bundle 在一个"早期 → 成熟期部署链路演进路径"提案。

---

## 2. 证据

- 关联 signals: [2026-05.md](../signals/2026-05.md) 候选 0032 + 0033
- 关联 reflect: [reflect/2026-W20.md](../reflect/2026-W20.md) B3
- 关联 Gate 实例: Project Phase 05 §J friction 1 + 2

---

## 3. 提案

### 3.1 改 `99-跨阶段/gate-checklists/Phase05-上线-Gate.md` 头部 — 加"目标环境"字段

```diff
 | 计划上线时间 | YYYY-MM-DD HH:MM |
 | 实际上线时间 | YYYY-MM-DD HH:MM |
+| **目标环境（proposal 0032）** | `dev` / `staging` / `prod`（多环境用 `staging+prod`）|
 | 灰度方案 | 内部用户 → 5% → 50% → 100% |
```

### 3.2 改 §B.3 演练段 — 加环境演进路径

```diff
 - [ ] **数据库** 全部打勾（DDL 已演练 / 备份完成 / 迁移在低峰）
+- **演练环境（proposal 0032）按项目成熟度**：
+  - `early`: dev 上"演练 = 实战"（无独立 staging）
+  - `stable+`: staging 演练 → prod 上线（必须双环境）
+  - 跨阶段（early → stable 首次部署 staging）：作为"staging 接入 Gate"独立走 Phase 05 一次，§C 凭据红线在这次必须 fully 满足
```

### 3.3 加 `plm-backend/scripts/deploy.sh`（候选 0033）

```bash
#!/usr/bin/env bash
# 自动化上线脚本 (proposal 0033)
# 用法: ./deploy.sh <ENV> <VERSION>
# 例: ./deploy.sh staging 0.2.0
set -e

ENV="${1:?缺 ENV (dev/staging/prod)}"
VER="${2:?缺 VERSION}"

# 1. 备份 (early=skip / stable+=必须)
case "$ENV" in
    staging|prod) bash backup.sh "$ENV" ;;
    *) echo "[skip] backup for $ENV" ;;
esac

# 2. mvn 打包 (JDK 17)
export JAVA_HOME="${JAVA_HOME:-/c/Program Files/Microsoft/jdk-17.0.18.8-hotspot}"
mvn clean install -DskipTests --no-transfer-progress

# 3. systemctl restart (按 ENV)
# 4. 健康检查 (curl /actuator/health)
# 5. 编码运行期自检 (per proposal 0200)
bash plm-backend/scripts/check-encoding-runtime.sh "$DB_PASSWORD"

# 6. tag (stable+ 必须)
[ "$ENV" != "dev" ] && git tag -a "v$VER" -m "Release v$VER ($ENV)"

echo "✅ Deploy to $ENV v$VER succeeded"
```

（脚本结构示意，实际实现按本机部署形态细化）

### 3.4 改 `99-跨阶段/gate-checklists/Phase05-上线-Gate.md` §E — 引用 deploy.sh

```diff
 | 部署后端 | | | | |
 | 部署前端 | | | | |
+| 实际部署命令 | | `bash plm-backend/scripts/deploy.sh <ENV> <VERSION>` | | （proposal 0033）|
```

---

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| early 阶段模块 | dev=staging 路径明示，§J 不再写 friction |
| stable+ 阶段模块 | §B.3 必须 staging 实测；deploy.sh 强制使用 |
| 现有 Project Phase 05 instance | 头部"目标环境"字段补 dev；§J 标"溯及 0032/0033" |
| 业务模块生成器 | 模板默认含 dev 部署指引（待 W22 同步）|

---

## 5. 风险

- **风险 1**: deploy.sh 实现细节随基础设施变（K8s / docker / VM 不同）。**缓解**: 脚本设计成"分 stage 函数"，可按部署形态切换实现；先支持 VM + systemctl，K8s 留 W23+ 升级。
- **风险 2**: early → stable 转型 staging 接入 Gate 没做就上 prod。**缓解**: §B.3 已写"跨阶段必须独立走 Phase 05 一次"；Gate 评审人卡控。

---

## 6. 备选方案

- A: 不写 deploy.sh，靠 Runbook 手动 — 不选，手敲漏步骤多
- B: 用 GitHub Actions / GitLab CI — 不选，当前无 CI 基础设施
- C（选定）: 本地 bash 脚本作为 v1，未来可移植到 CI

---

## 7. 实施计划

```
[x] Step 1: 写 proposal
[ ] Step 2: 评审
[ ] Step 3: 改 Phase05-上线-Gate.md 头部 + §B.3 + §E
[ ] Step 4: 写 plm-backend/scripts/deploy.sh v1
[ ] Step 5: Project / 8 active 模块下次部署用 deploy.sh
[ ] Step 6: tracking 期看部署漏步骤次数
```

---

## 8. 衡量指标

| 信号 | 基线 | 目标 |
|---|---|---|
| Phase 05 §J "部署环境 / 手工漏步骤 friction" | 2 (W20 Project)| 0 |
| deploy.sh 调用次数（部署上下文）| 0 | ≥ 3（W21+ 每次部署）|
| early → stable 转型有"staging 接入 Gate" 独立实例 | 0 | 1（首次转型时）|

Tracking 期: merged 后 2 周。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| _(待)_ | | | |

---

## 10. 实施后跟踪

待 merged 后回填。

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-17 | Wjl + Claude | 首版从 signals 候选 0032+0033 bundle 升格 |
