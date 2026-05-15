# Runbook — 应急操作手册

线上常见故障的诊断与处置脚本。pager 响起时按这本翻。

## 1. 系统起不来
- 后端日志看 `Started PlmApplication` 是否出现
- 数据库连接：[gotchas #3 Redis IPv6](../CLAUDE.md) 之外，看 Druid 连接池是否打满
- 端口冲突：`netstat -ano | grep <port>`

## 2. 后端 503 / 大量超时
- 看 Druid 监控台慢 SQL 排行
- 看 Redis：`redis-cli info clients` / `info memory`
- 看 JVM：`jstack <pid>` / GC 日志
- 临时降级：把流量切到上一个 tag 的副本

## 3. 数据库异常
- 主从延迟：`SHOW SLAVE STATUS`
- 锁等待：`SELECT * FROM performance_schema.data_locks;`
- 误操作恢复：从最近备份 + binlog 时间点恢复（提前演练过）

## 4. 缓存击穿 / 雪崩
- 临时方案：服务降级、限流
- 长期：加二级缓存 / 互斥锁

## 5. 紧急回滚
```bash
# 后端
kubectl set image deployment/plm-admin plm-admin=plm-admin:<上一个 tag>
# 数据库（仅当本次发布有 DDL）
mysql -u... -p... plm < sql/rollback_<本次发布>.sql
```

## 6. 联系人
| 角色 | 主 | 备 |
|---|---|---|
| oncall | Wjl | — |
| DBA | Wjl | — |
| 网络 / 云厂商支持 | N/A（本机开发） | — |

---

## 7. Project 模块（v0.1.0）回滚

> 适用场景：v0.1.0 上线后发现 P0 故障（关键路径不可用、数据污染、状态机错误等），需要彻底撤回 Project 模块。
>
> 因 v0.1.0 是首个业务版本，**回滚 = 完全撤销**（无上一业务版可切，仅剩 P0 后的空脚手架）。

### 7.1 回滚顺序（务必从前端 → 后端 → 数据库的反向）

```bash
# === Step 1: 前端 ===
# Vite dev 模式下，停 npm run dev 进程即可（生产环境则把 dist 切到上一版本）
# 这里以本地为例
taskkill /F /IM node.exe   # Windows
# pkill -f "vite"          # Linux/Mac

# === Step 2: 后端进程停 ===
# 用 netstat 找占用 8081 的 PID
netstat -ano | findstr :8081    # Windows
# lsof -i :8081                  # Linux/Mac
taskkill /F /PID <pid>           # Windows
# kill -9 <pid>                  # Linux/Mac

# === Step 3: 代码 git 回退到 P0 commit ===
cd /d/【12-trae】/06-项目全生命周期管理/plm
git log --oneline -10            # 找到 P0 commit 2679a61
git checkout 2679a61 -- plm-backend/ plm-frontend/
# 或者直接切回 P0 tag（若已打）

# === Step 4: 数据库回滚 SQL ===
MYSQL='/c/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe'
"$MYSQL" -uroot -p"$DB_PASSWORD" --default-character-set=utf8mb4 plm <<'SQL'
-- 卸载菜单与角色授权
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 2000 AND 2015;
DELETE FROM sys_menu WHERE menu_id BETWEEN 2000 AND 2015;
-- 卸载字典
DELETE FROM sys_dict_data WHERE dict_type IN ('biz_project_type', 'biz_project_status');
DELETE FROM sys_dict_type WHERE dict_type IN ('biz_project_type', 'biz_project_status');
-- 卸载业务表（含全部 PRJ-2026-XXXX 数据，无法恢复，确认后执行）
DROP TABLE IF EXISTS tb_project;
SQL

# === Step 5: 重新编译启动（用 P0 版本）===
export JAVA_HOME="/c/Program Files/Microsoft/jdk-17.0.18.8-hotspot"
cd plm-backend
mvn clean install -DskipTests -T 4 --no-transfer-progress
export DB_PASSWORD='...'
export REDIS_HOST=127.0.0.1
java -jar plm-admin/target/plm-admin.jar --server.port=8081
```

### 7.2 回滚前必查
- [ ] `tb_project` 是否已有真实业务数据？若有，先 `mysqldump` 备份
  ```bash
  "$MYSQL"dump -uroot -p"$DB_PASSWORD" --default-character-set=utf8mb4 plm tb_project > backup-tb_project-$(date +%Y%m%d-%H%M).sql
  ```
- [ ] 是否已通知所有可能受影响的使用方（v0.1.0 期 = 仅 Wjl 本人，可跳过）
- [ ] 回滚的根因是否明确？避免回滚后再上线又出同样问题

### 7.3 回滚后验证
- [ ] 浏览器登录 admin/admin123，不应再有「业务管理 / 项目管理」菜单
- [ ] curl `http://localhost:8081/business/project/list` 应返回 404（路由已撤销）
- [ ] `SHOW TABLES FROM plm LIKE 'tb_%';` 应为空

### 7.4 触发条件（满足任一即应回滚）
- 关键路径（登录 / 项目列表 / 状态推进）出现 P0 错误
- 后端日志出现新增的 NPE / DB 连接异常 / OOM（区分本次发布前后）
- `tb_project` 数据被异常写入（例如脏数据批量产生、状态机被绕过）
- 业务方主动反馈核心功能不可用

> early 项目无 SLA 监控，靠人工巡检 + `grep ERROR plm-admin.log` 兜底。
