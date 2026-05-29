#!/usr/bin/env bash
# =============================================================================
# 本地启动后端 (gitignored .env + 默认 port 8081)
# 用法:
#   cd plm-backend
#   ./scripts/local-start-backend.sh                # 前台 (启动前自动 db-migrate)
#   ./scripts/local-start-backend.sh --bg           # 后台,日志写 logs/backend.log
#   ./scripts/local-start-backend.sh --port 8082    # 指定端口
#   ./scripts/local-start-backend.sh --skip-migrate # 跳过 schema 增量检查 (Q-DB-05)
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$BACKEND_DIR"

# --- 1. 加载 .env (gitignored) ---
if [ ! -f .env ]; then
  echo "[ERR] $BACKEND_DIR/.env 不存在; 请从 .env.example 复制并填入本地值" >&2
  exit 1
fi
# shellcheck disable=SC1091
set -a; . ./.env; set +a

# --- 2. 强制 JDK 17 (Q-BUILD-01) ---
if [ -z "${JAVA_HOME:-}" ] || ! "$JAVA_HOME/bin/java" -version 2>&1 | grep -q '"17'; then
  export JAVA_HOME="/c/Program Files/Microsoft/jdk-17.0.18.8-hotspot"
fi
if [ ! -x "$JAVA_HOME/bin/java" ]; then
  echo "[ERR] JDK 17 not found at $JAVA_HOME" >&2
  exit 1
fi

# --- 3. 解析参数 ---
PORT=8081
BG=0
LOG_FILE="logs/backend.log"
SKIP_MIGRATE=0
while [ $# -gt 0 ]; do
  case "$1" in
    --bg) BG=1; shift ;;
    --port) PORT="$2"; shift 2 ;;
    --log) LOG_FILE="$2"; shift 2 ;;
    --skip-migrate) SKIP_MIGRATE=1; shift ;;
    *) echo "Unknown arg: $1" >&2; exit 1 ;;
  esac
done

JAR="plm-admin/target/plm-admin.jar"
if [ ! -f "$JAR" ]; then
  echo "[ERR] $JAR not found; run: mvn clean install -DskipTests -T 4" >&2
  exit 1
fi

# --- 3.5 DB migrate 前置 (Q-DB-05): 应用未入账的 business-*.sql ---
if [ "$SKIP_MIGRATE" = "0" ]; then
  echo "[info] 检查 DB schema 增量..."
  if ! "$SCRIPT_DIR/db-migrate.sh"; then
    echo "[ERR] db-migrate.sh 失败 — schema 未就绪, 启动中止" >&2
    echo "      首次运行需要 --init=fresh (空库) 或 --init=existing (已有数据)" >&2
    echo "      细节: ./scripts/db-migrate.sh --help" >&2
    echo "      跳过校验: ./scripts/local-start-backend.sh --skip-migrate" >&2
    exit 1
  fi
fi

mkdir -p logs
echo "[info] JAVA_HOME=$JAVA_HOME"
echo "[info] DB_PASSWORD=*** REDIS_HOST=${REDIS_HOST:-?} MCP_ENCRYPT_KEY=***"
echo "[info] port=$PORT bg=$BG"

if [ "$BG" = "1" ]; then
  nohup "$JAVA_HOME/bin/java" -jar "$JAR" --server.port="$PORT" > "$LOG_FILE" 2>&1 &
  echo "[info] backend PID=$! log=$LOG_FILE"
else
  exec "$JAVA_HOME/bin/java" -jar "$JAR" --server.port="$PORT"
fi
