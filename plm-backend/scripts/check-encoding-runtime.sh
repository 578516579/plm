#!/usr/bin/env bash
# =============================================================================
# 字符编码运行期自检 — 部署后跑,验证 HTTP→Java→MySQL 链路 UTF-8
# 用法: ./check-encoding-runtime.sh <DB_PASSWORD> [BACKEND_PORT]
# =============================================================================
set -e

DB_PWD="${1:?缺 DB_PASSWORD}"
PORT="${2:-8081}"
BASE="http://localhost:$PORT"
MYSQL='/c/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe'
REDIS='/d/Program Files/Redis/redis-cli'

# 1. 登录 (含 captcha)
CAP=$(curl -s "$BASE/captchaImage")
UUID=$(echo "$CAP" | grep -oE '"uuid":"[^"]+"' | cut -d'"' -f4)
CODE=$("$REDIS" -h 127.0.0.1 GET "captcha_codes:$UUID" 2>/dev/null | tr -d '"')
TOKEN=$(curl -s -X POST "$BASE/login" -H "Content-Type: application/json" \
    -d "{\"username\":\"admin\",\"password\":\"admin123\",\"code\":\"$CODE\",\"uuid\":\"$UUID\"}" \
    | grep -oE '"token":"[^"]+"' | cut -d'"' -f4)

[ -z "$TOKEN" ] && { echo "❌ 登录失败"; exit 1; }

# 2. POST 含中文 + 希腊字 + emoji 的请求体（用临时文件避免 shell 转换坑）
TS=$(date +%s)
PAYLOAD_FILE="/tmp/check-encoding-$TS.json"
cat > "$PAYLOAD_FILE" <<EOF
{"projectId":1,"title":"编码自检 $TS - αβγ - 测试","description":"中文描述 + Greek αβγδε + 全角符号 ：、 + 半角 ASCII","source":"01","priority":"02"}
EOF

RESP=$(curl -s -X POST "$BASE/business/requirement" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json; charset=UTF-8" \
    --data-binary "@$PAYLOAD_FILE")
echo "[POST] $RESP"
rm -f "$PAYLOAD_FILE"

echo "$RESP" | grep -q '"code":200' || { echo "❌ POST 失败"; exit 1; }

# 3. 读 DB HEX 校验
sleep 1
HEX=$("$MYSQL" -uroot -p"$DB_PWD" --default-character-set=utf8mb4 plm -Nse \
    "SELECT HEX(title) FROM tb_requirement WHERE title LIKE '%编码自检 $TS%' ORDER BY requirement_id DESC LIMIT 1" 2>/dev/null)

# "编" = E7BC96, "码" = E7A081  → 开头应是 E7BC96E7A081
echo "[DB] HEX=$HEX"
if echo "$HEX" | grep -q "EFBFBD"; then
    echo "❌ 发现 EFBFBD (U+FFFD 替换符) — 乱码! 字节链路有问题"
    exit 1
fi
if echo "$HEX" | grep -qi "^E7BC96E7A081"; then
    echo "✅ HEX 校验通过 (UTF-8 字节正确)"
    # 清理测试数据
    "$MYSQL" -uroot -p"$DB_PWD" --default-character-set=utf8mb4 plm -e \
        "DELETE FROM tb_requirement WHERE title LIKE '%编码自检 $TS%'" 2>/dev/null
    echo "✅ 测试数据已清理"
    exit 0
else
    echo "❌ HEX 异常 (预期开头 E7BC96E7A081 = '编码'): $HEX"
    exit 1
fi
