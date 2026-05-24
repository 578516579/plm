---
name: plm-troubleshoot
description: PLM 项目故障排查 SOP。遇到 E2E fail / backend ServiceException / "Table doesn't exist" / "正在加载系统资源" / mojibake 中文乱码 / 启动慢 / jar 锁 / stale JVM 等典型现象时使用。按 5 层根因模型逐步定位 → 一步步给出修复路径,避免胡乱试。
---

# PLM 故障排查 SOP

## 启动条件

当出现任何下列信号时,加载本 skill:
- E2E 多个测试失败
- backend log 持续抛 ServiceException
- 浏览器 stuck 在 "正在加载系统资源,请耐心等待"
- mysql 报 "Table 'plm.xxx' doesn't exist"
- mvn install 失败,jar 锁
- backend 抛 "AI模型不能为空"等字符串,但 grep 源码无此字符串
- 看到乱码:`AIģ�Ͳ���Ϊ��`(典型 console encoding)

## 5 层根因模型

按下面顺序排查,**不要跳层**:

### Layer 1 — 拿到原始错误

```bash
# E2E 失败拿 error-context
cat test-results/<test-dir>/error-context.md | head -40

# Backend log 同时间窗
grep ERROR /d/tmp/plm-backend.log | grep "<HH:MM>:" | tail -10
```

### Layer 2 — 错误信息 grep 源码

```bash
# 错误消息(中文也可,文件可能带 mojibake 编码)
grep -rn "<错误关键词>" plm-*/src/main/java/

# 找到了 → 进 Layer 3
# 没找到 → 跳 Layer 4 (stale JVM)
```

### Layer 3 — 代码逻辑确认

打开 grep 到的文件,看异常抛出条件:
```java
throw new ServiceException("<message>", <code>);
```

`code` 解码:
- 404 — 资源不存在
- 601 — 状态机错
- 602 — 字段必填
- 604 — 枚举值非法
- 701 — 业务规则冲突(如同名同版本)
- 702 — 关联资源不存在
- 708 — AI 调用失败

### Layer 4 — JVM stale 检查

如果 grep 没找到错误字符串,但 backend 在抛 → 进程加载了旧字节码:

```bash
# 1. 当前 jar mtime
ls -la plm-backend/plm-admin/target/plm-admin.jar

# 2. 进程启动时间
PID=$(netstat -ano | grep LISTENING | grep ":8081 " | awk '{print $NF}' | head -1)
wmic process where "ProcessId=$PID" get CreationDate

# 3. 如果 process startTime < jar mtime,stale!
```

修复:
```bash
taskkill //PID $PID //F
sleep 3
mvn install -DskipTests -T 4
nohup java -jar plm-admin/target/plm-admin.jar --server.port=8081 > /d/tmp/plm-backend.log 2>&1 &
until curl -fs http://localhost:8081/captchaImage > /dev/null 2>&1; do sleep 3; done
```

### Layer 5 — Schema / Branch 一致性

```bash
# 当前 branch
git branch --show-current

# 表名实际存在
mysql -uroot -p"<pwd>" -e "USE plm; SHOW TABLES LIKE 'tb_<entity>%';"

# 对应 branch 的 sql 文件
grep -l "tb_<entity>\|CREATE TABLE tb_<entity>" plm-backend/sql/business-*.sql
```

如果表名不匹配(代码用 `tb_dora_metric` 但 DB 只有 `tb_dora`):
```bash
mysql -uroot -p"<pwd>" --default-character-set=utf8mb4 --force plm < plm-backend/sql/business-<entity>.sql
```

`--force` 跳过字典 duplicate 错误。

## 现象 → 根因 → 修复 速查

### "正在加载系统资源,请耐心等待"

| 根因 | 修复 |
|---|---|
| vite import.meta.glob 未识别新 view | 重启 vite dev |
| sys_menu 表 component 字段错 | 修菜单 component path |
| 后端 /getRouters 接口挂 | 看 backend log + JWT |

### "Table 'plm.xxx' doesn't exist"

- 重跑对应 sql:`mysql ... --force plm < sql/business-<entity>.sql`
- 切 branch 后 always 重跑

### "AI模型不能为空" 但 grep 源码无

- stale JVM,kill + rebuild

### 中文 mojibake `AIģ�Ͳ���Ϊ��`

- Console encoding 不是真乱码
- 把字符串复制到 VS Code / 浏览器 看真值
- backend 日志文件本身是 utf8

### mvn install 失败 jar 锁

```
Unable to rename '.../plm-admin.jar' to '.../plm-admin.jar.original'
```

- backend 还在跑,需先 kill PID

### C 盘满

```
C:              301G  298G  3.0G 100% /c
```

- `setx` 持久化 D:/tmp(参考 environment-setup agent)

### Login timeout E2E flake

```
TimeoutError: apiRequestContext.post: ... /login
```

- backend 启动初期慢,加 `--retries=1`

## 字段名常见对齐错

| 前端(错) | 后端 domain | 备注 |
|---|---|---|
| `modelProvider` | `provider` | V2 修过 |
| `systemPrompt` | `promptTemplate` | V2 修过 |
| `lastCallAt` | `lastInvokedAt` | V2 修过 |
| `metricNo` | `doraNo` | DORA 模块特例 |

不知道时,**后端 domain 为准**,grep `getXxx` 找真名:
```bash
grep -n "public String get\|public Long get" plm-<module>/src/main/java/.../<Entity>.java
```

## 与其他 SOP 关联

- 环境层(C 盘满 / JAVA_HOME) → 见 `environment-setup` agent
- 构建层(mvn / vite build) → 见 `build-deployer` agent
- DB 层(schema / 字典) → 见 `db-modeler` agent
- Git 层(branch 切换 / cherry-pick) → 见 `git-workflow` agent

## 反模式

- ❌ 不看 backend log 直接改代码
- ❌ 改前不 grep 错误字符串就猜根因
- ❌ stale JVM 时只重 build 不 kill 进程
- ❌ schema 不一致时只 ALTER 一张表(应该重跑该 entity 的完整 sql)
- ❌ mojibake 当作真乱码去改字符集设置
