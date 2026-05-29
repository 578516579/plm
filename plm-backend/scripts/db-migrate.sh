#!/usr/bin/env bash
# =============================================================================
# DB migrate — 增量应用 plm-backend/sql/business-*.sql
#
# 痛点:branch 切换 / pull 后,新加的 business-*-(add|widen|ai-eval|review|...).sql
#       不会自动跑 → 启动后 "Table doesn't exist" / "Unknown column" (Q-DB-03 类)
#
# 解决:ledger (sql/.applied-scripts) 追踪已应用脚本;启动前比对 sql/ 目录,
#       diff 出未应用的按字母序逐个 apply。base 脚本(business-<entity>.sql)
#       第一次跑后入账,后续切 branch 只跑新增的增量。
#
# 用法:
#   ./scripts/db-migrate.sh                  # 标准:仅应用未入账脚本
#   ./scripts/db-migrate.sh --dry-run        # 只列出待应用,不执行
#   ./scripts/db-migrate.sh --init=fresh     # 首次:空库,全跑并入账
#   ./scripts/db-migrate.sh --init=existing  # 首次:已有数据,只入账不跑
#   MYSQL_BIN=/c/path/to/mysql.exe ./scripts/db-migrate.sh
#
# 跳过:*-rollback.sql (手动用)
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
SQL_DIR="$BACKEND_DIR/sql"
LEDGER="$SQL_DIR/.applied-scripts"
cd "$BACKEND_DIR"

# --- 1. 加载 .env (DB_USERNAME / DB_PASSWORD / 可选 DB_HOST/PORT/SCHEMA) ---
if [ ! -f .env ]; then
  echo "[ERR] $BACKEND_DIR/.env 不存在; 请从 .env.example 复制并填入本地值" >&2
  exit 1
fi
# shellcheck disable=SC1091
set -a; . ./.env; set +a

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_SCHEMA="${DB_SCHEMA:-plm}"
DB_USERNAME="${DB_USERNAME:-root}"   # 与 application-druid.yml 默认对齐
: "${DB_PASSWORD:?DB_PASSWORD 未在 .env 设置}"

# --- 2. 定位 mysql 客户端 ---
MYSQL=""
for cand in "${MYSQL_BIN:-}" \
            "/c/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe" \
            "/c/Program Files/MySQL/MySQL Server 8.4/bin/mysql.exe" \
            "mysql"; do
  [ -z "$cand" ] && continue
  if [ -x "$cand" ] || command -v "$cand" >/dev/null 2>&1; then
    MYSQL="$cand"
    break
  fi
done
[ -z "$MYSQL" ] && { echo "[ERR] mysql 客户端未找到; 设 MYSQL_BIN=<path>" >&2; exit 1; }

run_mysql() {
  "$MYSQL" -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD" \
    --default-character-set=utf8mb4 "$@"
}

# --- 3. 参数解析 ---
DRY_RUN=0
INIT_MODE=""
while [ $# -gt 0 ]; do
  case "$1" in
    --dry-run)       DRY_RUN=1; shift ;;
    --init=fresh)    INIT_MODE=fresh; shift ;;
    --init=existing) INIT_MODE=existing; shift ;;
    -h|--help)       sed -n '2,22p' "$0"; exit 0 ;;
    *) echo "Unknown arg: $1" >&2; exit 1 ;;
  esac
done

# --- 4. 收集 business-*.sql (alpha 序;排除 *-rollback.sql) ---
mapfile -t ALL_SCRIPTS < <(
  ls "$SQL_DIR"/business-*.sql 2>/dev/null \
    | xargs -n1 basename \
    | grep -v -- '-rollback\.sql$' \
    | sort
)
if [ "${#ALL_SCRIPTS[@]}" = "0" ]; then
  echo "[ERR] $SQL_DIR 下无 business-*.sql" >&2
  exit 1
fi

# --- 5. ledger 辅助 ---
ledger_init_header() {
  cat > "$LEDGER" <<'EOF'
# PLM DB migration ledger — 由 scripts/db-migrate.sh 管理,勿手动改
# 每行: <filename>\t<applied_at_iso>
EOF
}
ledger_record() {
  printf '%s\t%s\n' "$1" "$(date -u +%Y-%m-%dT%H:%M:%SZ)" >> "$LEDGER"
}

# --- 6. 首次初始化分支 ---
if [ ! -f "$LEDGER" ]; then
  case "$INIT_MODE" in
    fresh)
      echo "[init=fresh] 写 ledger header,即将应用全部 ${#ALL_SCRIPTS[@]} 个脚本"
      ledger_init_header
      ;;
    existing)
      echo "[init=existing] 把现有 ${#ALL_SCRIPTS[@]} 个脚本标记为已应用,不实际运行"
      ledger_init_header
      for s in "${ALL_SCRIPTS[@]}"; do ledger_record "$s"; done
      echo "[ok] ledger 初始化完毕 → $LEDGER"
      exit 0
      ;;
    *)
      cat >&2 <<EOF
[ERR] 首次运行需明确初始化模式 (ledger 不存在):
  --init=fresh     全跑 business-*.sql (适合空库 / 重置后)
  --init=existing  不跑,把现有脚本全部标记为已应用 (适合已手动建过库)

详见 ./scripts/db-migrate.sh --help
EOF
      exit 2 ;;
  esac
fi

# --- 7. diff: ledger vs 全集 ---
declare -A APPLIED
while IFS=$'\t' read -r fname _rest; do
  [[ "$fname" =~ ^# || -z "$fname" ]] && continue
  APPLIED["$fname"]=1
done < "$LEDGER"

PENDING=()
for s in "${ALL_SCRIPTS[@]}"; do
  [ -n "${APPLIED[$s]:-}" ] || PENDING+=("$s")
done

if [ "${#PENDING[@]}" = "0" ]; then
  echo "[ok] DB schema up-to-date (${#ALL_SCRIPTS[@]} 个脚本已应用)"
  exit 0
fi

echo "[plan] 待应用 ${#PENDING[@]} 个脚本:"
for s in "${PENDING[@]}"; do echo "  - $s"; done

if [ "$DRY_RUN" = "1" ]; then
  echo "[dry-run] 未执行"
  exit 0
fi

# --- 8. 逐个 apply,失败立即停止(已应用的已入账) ---
for s in "${PENDING[@]}"; do
  echo "[apply] $s"
  if ! run_mysql "$DB_SCHEMA" < "$SQL_DIR/$s"; then
    echo "[ERR] $s 应用失败; ledger 未更新该项; 修复后重跑 db-migrate.sh" >&2
    exit 3
  fi
  ledger_record "$s"
done

echo "[ok] 已应用 ${#PENDING[@]} 个脚本"
