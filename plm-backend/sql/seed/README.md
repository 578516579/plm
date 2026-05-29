# PLM 测试数据 seed 脚本

性能测试 / E2E 测试 / 联调演示前的批量数据准备。

## 与 `business-*.sql` 的区别

| 维度 | `sql/business-*.sql` | `sql/seed/seed-*.sql`(本目录)|
|---|---|---|
| **目的** | 业务表 DDL + 字典 + 菜单 + 权限(production 一次性导入)| 测试数据(performance / E2E 跑前批量种入)|
| **数据特征** | 真实业务数据,长期保留 | 假数据,带 `remark='seed'` 标记,可批量清理 |
| **加载频率** | 1 次(首次部署 + schema 升级)| 每次性能测试前 |
| **应用方式** | `db-migrate.sh` 走 `.applied-scripts` 台账 | `seed-all.sh` 不入台账,可重入 |

## 用法

```powershell
# === 一键种入所有 seed(11 个模块) ===
$env:DB_PASSWORD = '...'
.\seed-all.ps1

# === 只种 project + sprint ===
.\seed-all.ps1 -Modules project,sprint

# === 清理所有 seed(remark='seed' 标记的) ===
.\seed-cleanup.ps1

# === Linux / Git Bash ===
DB_PASSWORD='...' ./seed-all.sh
DB_PASSWORD='...' ./seed-cleanup.sh
```

## 当前 seed 覆盖

| 模块 | 文件 | 行数 | id 区间(便于压测脚本引用)|
|---|---|---|---|
| project | seed-project.sql | 50 | 100-149(seed-start-id 100)|
| sprint | (待补)| - | - |
| task | (待补)| - | - |
| testcase | (待补)| - | - |
| defect | (待补)| - | - |
| testreport | (待补)| - | - |
| requirement | (待补)| - | - |
| prd | (待补)| - | - |
| integration | (待补)| - | - |

## 规约

1. **id 起始 100** — 保留 1-99 给 `ry_20260417.sql` 的种子数据,避免主键冲突。
2. **必带 `remark='seed'`** — 清理时根据这个标志批量删除。
3. **`create_by='seed-runner'`** — 区分人为 / 测试数据。
4. **utf8mb4** — 加载命令必须 `--default-character-set=utf8mb4`(gotcha #2)。
5. **幂等** — 每个 seed-*.sql 顶部先 `DELETE WHERE remark='seed'`,再 INSERT。
6. **FK 关联** — seed 数据内部的外键引用必须自洽(如 sprint.project_id 引 seed 出的 project id)。

## 与 k6 性能脚本的契约

`plm-perf-test/scenarios/project-state-transition.js` 默认读 `__ENV.SEED_PROJECT_IDS = "100,...,109"`。
如调整 seed 起始 id,同步改 k6 脚本。
