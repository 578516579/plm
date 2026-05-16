#!/usr/bin/env bash
# =============================================================================
# 字符编码自检 — commit 前 / CI 必跑
# 关联：03-开发/字符编码规范.md
# =============================================================================
set -e

cd "$(dirname "$0")/.."

FAIL=0

echo "[1/4] 源码文件必须 UTF-8 无 BOM..."
while IFS= read -r f; do
    # BOM 检查
    if head -c 3 "$f" 2>/dev/null | od -An -tx1 | head -1 | grep -q "ef bb bf"; then
        echo "  ❌ BOM 头: $f"
        FAIL=1
    fi
    # charset 检查（仅在 file 命令可用时）
    if command -v file >/dev/null 2>&1; then
        cs=$(file -i "$f" 2>/dev/null | sed -n 's/.*charset=//p')
        case "$cs" in
            utf-8|us-ascii|binary) ;;
            *) echo "  ❌ 非 UTF-8: $f (charset=$cs)" && FAIL=1 ;;
        esac
    fi
done < <(find . -type f \( -name "*.java" -o -name "*.xml" -o -name "*.yml" -o -name "*.yaml" -o -name "*.sql" -o -name "*.md" -o -name "*.properties" \) ! -path "*/target/*" ! -path "*/node_modules/*" ! -path "*/.git/*")

echo "[2/4] application.yml 必须含 server.servlet.encoding.force=true..."
if ! grep -A2 "encoding:" plm-admin/src/main/resources/application.yml | grep -q "force: true"; then
    echo "  ❌ application.yml 缺 force: true"
    FAIL=1
fi

echo "[3/4] pom.xml 必须配 sourceEncoding=UTF-8..."
if ! grep -q "<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>" pom.xml 2>/dev/null; then
    echo "  ❌ pom.xml 缺 sourceEncoding=UTF-8"
    FAIL=1
fi

echo "[4/4] JDBC URL 必须含 characterEncoding=utf8..."
if ! grep -q "characterEncoding=utf8" plm-admin/src/main/resources/application-druid.yml; then
    echo "  ❌ JDBC URL 缺 characterEncoding=utf8"
    FAIL=1
fi

if [ "$FAIL" -eq 0 ]; then
    echo "✅ 字符编码静态检查全过"
    exit 0
else
    echo "❌ 有违规,见上方"
    exit 1
fi
