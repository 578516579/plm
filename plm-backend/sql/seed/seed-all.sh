#!/usr/bin/env bash
# seed-all.sh — 批量种入所有 seed 文件
# 用法: DB_PASSWORD='...' ./seed-all.sh [module1 module2 ...]
#   不带参数 → 跑所有 seed-*.sql
#   带参数  → 只跑指定模块的 seed-<module>.sql

set -euo pipefail

if [ -z "${DB_PASSWORD:-}" ]; then
  echo "❌ DB_PASSWORD 未设置" >&2
  exit 1
fi

# 自动找 mysql.exe(支持 Windows Git Bash + Linux)
MYSQL_BIN="${MYSQL_BIN:-}"
if [ -z "$MYSQL_BIN" ]; then
  if [ -x "/c/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe" ]; then
    MYSQL_BIN="/c/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe"
  elif command -v mysql >/dev/null 2>&1; then
    MYSQL_BIN="$(command -v mysql)"
  else
    echo "❌ 找不到 mysql 客户端,设 MYSQL_BIN 环境变量" >&2
    exit 1
  fi
fi

DB="${DB_NAME:-plm}"
SEED_DIR="$(cd "$(dirname "$0")" && pwd)"

run_one() {
  local file="$1"
  echo "── seed: $file"
  "$MYSQL_BIN" -uroot -p"$DB_PASSWORD" --default-character-set=utf8mb4 "$DB" < "$file"
}

if [ $# -eq 0 ]; then
  for f in "$SEED_DIR"/seed-*.sql; do
    [ "$(basename "$f")" = "seed-cleanup.sql" ] && continue
    run_one "$f"
  done
else
  for m in "$@"; do
    f="$SEED_DIR/seed-$m.sql"
    [ -f "$f" ] || { echo "❌ 找不到 $f" >&2; exit 1; }
    run_one "$f"
  done
fi

echo "✅ seed 完成"
