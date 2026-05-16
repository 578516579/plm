# E2E 测试运行手册

| 字段 | 值 |
|---|---|
| 版本 | v1.0 |
| 框架 | Playwright 1.60+ |
| 目标读者 | 开发 / 测试 / Claude |

---

## 1. 三分钟跑通

```bash
# === 0. 前置检查 ===
which mvn java node redis-cli mysql
# 期望: 全部找到

# === 1. 启动 MySQL + Redis (一次性) ===
# Windows: 服务管理器启 MySQL + Redis (或确认已运行)
netstat -ano | grep -E ":3306|:6379"   # 必须 LISTENING

# === 2. 启动后端 (CRITICAL: 必须带 file.encoding=UTF-8) ===
cd plm-backend
export JAVA_HOME="/c/Program Files/Microsoft/jdk-17.0.18.8-hotspot"
export DB_PASSWORD='your-password'
export REDIS_HOST=127.0.0.1
nohup java \
  -Dfile.encoding=UTF-8 \
  -Dsun.jnu.encoding=UTF-8 \
  -Dstdout.encoding=UTF-8 \
  -Dstderr.encoding=UTF-8 \
  -jar plm-admin/target/plm-admin.jar \
  --server.port=8081 > /tmp/plm-backend.log 2>&1 &

# 等启动完成 (~60s)
tail -f /tmp/plm-backend.log | grep "Started PlmApplication"

# === 3. 启动前端 (单独终端) ===
cd plm-frontend
export VITE_BACKEND_URL=http://localhost:8081
npm run dev      # 或 pnpm run dev
# 等 "Local: http://localhost/"

# === 4. 跑 E2E (单独终端) ===
cd plm-frontend
export DB_PASSWORD='your-password'
npm run test:e2e
# 期望: 41 passed (~80s)
```

---

## 2. 命令大全

| 命令 | 场景 |
|---|---|
| `npm run test:e2e` | **全部 41 case** (默认 headless,最常用) |
| `npm run test:e2e:headed` | 浏览器可见(看着跑) |
| `npm run test:e2e:debug` | 单步调试 Playwright Inspector |
| `npm run test:e2e:smoke` | **冒烟测试** — encoding + navigation (~15s) |
| `npm run test:e2e:encoding` | 仅 encoding 套件 (~20s) |
| `npm run test:e2e:business` | 4 业务模块 (~60s) |
| `npm run test:e2e:report` | 打开上次跑的 HTML 报告 |

### 按用例名过滤

```bash
# 跑业务硬规则 703 的全部 case
npx playwright test -g "703"

# 跑某个 spec
npx playwright test sprint.spec.ts

# 跑某 spec 的某 case
npx playwright test sprint.spec.ts -g "TC-Spr-F003"
```

### 失败重跑

```bash
# 只重跑失败的
npx playwright test --last-failed

# 看 trace (失败时自动保存)
npx playwright show-trace test-results/<test-name>/trace.zip
```

---

## 3. 环境变量

| 变量 | 默认 | 说明 |
|---|---|---|
| `DB_PASSWORD` | (必填) | MySQL root 密码,用于 HEX 校验 |
| `E2E_BACKEND_URL` | `http://localhost:8081` | 后端地址 |
| `PLAYWRIGHT_BASE_URL` | `http://localhost` | 前端地址 |
| `REDIS_HOST` | `127.0.0.1` | Redis 主机 (拿 captcha code) |
| `REDIS_PORT` | `6379` | Redis 端口 |
| `REDIS_CLI` | (auto-detect) | redis-cli 路径 |
| `MYSQL_CLI` | `C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe` | mysql 路径 |
| `MYSQL_USER` | `root` | DB 用户 |
| `DB_NAME` | `plm` | DB 名 |

---

## 4. 失败排查

### 4.1 "captcha 失败"

- 检查 Redis 在跑:`redis-cli -h 127.0.0.1 PING` 应返 PONG
- 检查后端启动正常:`curl http://localhost:8081/captchaImage`
- 检查 redis-cli 在 PATH:`which redis-cli`

### 4.2 "登录失败 验证码已失效"

- captcha 5 分钟过期,确保 helper 写法 (`auth.ts`) 用同一个 uuid
- 检查后端日志:有没有 401/500

### 4.3 "Fixture { request } from beforeAll cannot be reused in a test"

- spec 文件用了错误模式;参考已有 spec:在 beforeAll 里 `await playwright.request.newContext()`,afterAll 里 dispose

### 4.4 "Locator resolved but hidden"

- Element Plus 渲染但 0 高度(列表空时常见)
- 改用 `.app-container` 容器选择器,或 `.first()` 加 `state: 'attached'`

### 4.5 "Test timed out"

- 后端响应慢(冷启动)或 DB 锁
- 单独跑 `curl ${BACKEND}/business/project/list -H "Authorization: Bearer ${TOKEN}"` 看是否快

### 4.6 "DB HEX 含 EFBFBD" (乱码回归触发)

**这是必须解决的 P0**。检查:
1. 后端启动是否带 `-Dfile.encoding=UTF-8` (`ps -ef | grep file.encoding`)
2. `application.yml` 是否含 `server.servlet.encoding.force: true`
3. JDBC URL 是否含 `characterEncoding=utf8`
4. 表 / 库 charset utf8mb4

详见 [`03-开发/字符编码规范.md`](../../03-开发/字符编码规范.md)

---

## 5. CI 集成参考

### 5.1 GitHub Actions

```yaml
name: e2e

on: [push, pull_request]

jobs:
  e2e:
    runs-on: ubuntu-latest
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: testpass
          MYSQL_DATABASE: plm
        ports: ['3306:3306']
      redis:
        image: redis:7
        ports: ['6379:6379']

    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          distribution: 'microsoft'
          java-version: 17

      - uses: actions/setup-node@v4
        with:
          node-version: 20
          cache: 'npm'
          cache-dependency-path: plm-frontend/package-lock.json

      - name: Build backend
        run: |
          cd plm-backend
          mvn package -DskipTests -T 4

      - name: Import SQL
        run: |
          mysql -h127.0.0.1 -uroot -ptestpass --default-character-set=utf8mb4 plm < plm-backend/sql/ry_20260417.sql
          mysql -h127.0.0.1 -uroot -ptestpass --default-character-set=utf8mb4 plm < plm-backend/sql/quartz.sql
          mysql -h127.0.0.1 -uroot -ptestpass --default-character-set=utf8mb4 plm < plm-backend/sql/business-project.sql
          mysql -h127.0.0.1 -uroot -ptestpass --default-character-set=utf8mb4 plm < plm-backend/sql/business-requirement.sql
          mysql -h127.0.0.1 -uroot -ptestpass --default-character-set=utf8mb4 plm < plm-backend/sql/business-sprint.sql
          mysql -h127.0.0.1 -uroot -ptestpass --default-character-set=utf8mb4 plm < plm-backend/sql/business-task.sql

      - name: Start backend
        run: |
          export DB_PASSWORD=testpass
          export REDIS_HOST=127.0.0.1
          cd plm-backend
          nohup java -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 \
            -jar plm-admin/target/plm-admin.jar --server.port=8081 &
          # wait for backend
          for i in {1..60}; do
            if curl -s http://localhost:8081/captchaImage >/dev/null; then break; fi
            sleep 2
          done

      - name: Install playwright
        run: |
          cd plm-frontend
          npm ci
          npx playwright install --with-deps chromium

      - name: Start frontend
        run: |
          cd plm-frontend
          export VITE_BACKEND_URL=http://localhost:8081
          nohup npm run dev &
          for i in {1..30}; do
            if curl -s http://localhost/ >/dev/null; then break; fi
            sleep 2
          done

      - name: Run E2E
        run: |
          cd plm-frontend
          export DB_PASSWORD=testpass
          npm run test:e2e

      - uses: actions/upload-artifact@v4
        if: failure()
        with:
          name: playwright-report
          path: plm-frontend/playwright-report
```

---

## 6. 跑通验收 Checklist

每次代码改动后跑 E2E 套件,期望:

- [ ] `npm run test:e2e:encoding` ✅ 6/6 passed (乱码守门员)
- [ ] `npm run test:e2e:smoke` ✅ 14/14 passed (encoding + navigation)
- [ ] `npm run test:e2e:business` ✅ 28/28 passed (4 模块)
- [ ] `npm run test:e2e` ✅ **41/41 passed** (全套 ~80s)
- [ ] **任何 DB HEX 含 EFBFBD 视为 P0 阻塞,必须修复才能 commit**
- [ ] 失败时保留 `playwright-report/` 和 `test-results/` 用于排查

---

## 7. 修订记录

| 版本 | 日期 | 变更 |
|---|---|---|
| v1.0 | 2026-05-16 | 首次创建,Phase 03→04 准入手册 |
