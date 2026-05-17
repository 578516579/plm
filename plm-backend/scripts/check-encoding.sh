#!/usr/bin/env bash
# =============================================================================
# 字符编码自检 — commit 前 / CI 必跑
# 关联：03-开发/字符编码规范.md
#
# 用法:
#   bash check-encoding.sh             # 默认: 全仓库扫描 (CI / 手工 dry-run)
#   bash check-encoding.sh --staged    # pre-commit: 仅扫 git diff --cached 的文件 (BL-2026-009)
# =============================================================================
set -e

REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null)"
if [ -z "$REPO_ROOT" ]; then
    REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
fi
cd "$REPO_ROOT"

FAIL=0
STAGED=0
if [ "$1" = "--staged" ]; then
    STAGED=1
fi
MODE_LABEL=$( [ "$STAGED" = "1" ] && echo "staged" || echo "all" )

# [1/4] 源码文件 UTF-8 / 无 BOM 校验 — 按模式选 file list
echo "[1/4] 源码文件必须 UTF-8 无 BOM (mode=$MODE_LABEL)..."
if [ "$STAGED" = "1" ]; then
    # pre-commit: 仅 staged 且匹配后缀的文件
    FILE_LIST=$(git diff --cached --name-only --diff-filter=AM 2>/dev/null \
        | grep -E '\.(java|xml|yml|yaml|sql|md|properties)$' || true)
else
    # 全仓库扫描: 排除 target / node_modules / .git
    FILE_LIST=$(find . -type f \( -name "*.java" -o -name "*.xml" -o -name "*.yml" -o -name "*.yaml" -o -name "*.sql" -o -name "*.md" -o -name "*.properties" \) \
        ! -path '*/target/*' ! -path '*/node_modules/*' ! -path '*/.git/*' \
        | sed 's|^\./||')
fi

SCANNED=0
while IFS= read -r f; do
    [ -z "$f" ] && continue
    [ ! -f "$f" ] && continue
    SCANNED=$((SCANNED + 1))
    if head -c 3 "$f" 2>/dev/null | od -An -tx1 | head -1 | grep -q "ef bb bf"; then
        echo "  ❌ BOM 头: $f"
        FAIL=1
    fi
    if command -v file >/dev/null 2>&1; then
        cs=$(file -i "$f" 2>/dev/null | sed -n 's/.*charset=//p')
        case "$cs" in
            utf-8|us-ascii|binary) ;;
            *) echo "  ❌ 非 UTF-8: $f (charset=$cs)" && FAIL=1 ;;
        esac
    fi
done <<< "$FILE_LIST"

if [ "$STAGED" = "1" ] && [ "$SCANNED" = "0" ]; then
    echo "  (no staged source files matching .java/.xml/.yml/.yaml/.sql/.md/.properties)"
fi

# [2/4]/[3/4]/[4/4] 配置检查 — 与文件模式无关, 始终全跑
echo "[2/4] application.yml 必须含 server.servlet.encoding.force=true..."
# 注: 原 grep -A2 仅覆盖 +2 行, 漏掉 force: true 位于 +3 处 → 改 -A5 / 或宽容 grep
if ! grep -A5 "encoding:" plm-backend/plm-admin/src/main/resources/application.yml 2>/dev/null | grep -q "force: true"; then
    echo "  ❌ application.yml 缺 force: true (encoding 段 +5 行内未找到)"
    FAIL=1
fi

echo "[3/4] pom.xml 必须配 sourceEncoding=UTF-8..."
if ! grep -q "<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>" plm-backend/pom.xml 2>/dev/null; then
    echo "  ❌ pom.xml 缺 sourceEncoding=UTF-8"
    FAIL=1
fi

echo "[4/4] JDBC URL 必须含 characterEncoding=utf8..."
if ! grep -q "characterEncoding=utf8" plm-backend/plm-admin/src/main/resources/application-druid.yml 2>/dev/null; then
    echo "  ❌ JDBC URL 缺 characterEncoding=utf8"
    FAIL=1
fi

if [ "$FAIL" -eq 0 ]; then
    echo "✅ 字符编码静态检查全过 (mode=$MODE_LABEL, scanned=$SCANNED 文件)"
    exit 0
else
    echo "❌ 有违规,见上方"
    exit 1
fi
