#!/usr/bin/env bash
# rule-health.sh — signals-collect v0.4 (skeleton)
#
# 输入:
#   - 30 天 signals/.../*.md + supplementary/.md (Type 1-7 数据)
#   - .claude/logs/tools/*.log (PostToolUse log)
#   - .claude/rules.md / 03-开发/开发规范.md (规则源)
#
# 输出: 自动建议表 (markdown)
#   | 规则 ID | 现级别 | 30d 违反次数 | override 比 | 建议 | 证据 |
#
# 算法:
#   downgrade MUST → SHOULD: 违反 ≥ 3 / 月 AND override > 50% AND 无 P0/P1 bug
#   upgrade SHOULD → MUST: 违反 ≤ 1 / 月 AND 有 P0/P1 bug 相关 AND 修复成本高
#   保持: 其余
#
# 激活条件 (v0.4 真正运行需满足):
#   1. signals 数据连续覆盖 ≥ 30 天 (PLM 2026-05-15 起, 早于 2026-06-15 不算成熟)
#   2. PostToolUse hook 日志 ≥ 30 天 (本 commit 2026-05-19 起, 早于 2026-06-19 不算成熟)
#   3. ≥ 2 个完整月度 reflect (用以校准基线)
#
# 当前状态 (2026-05-19): skeleton 跑通 + 输出 "数据未成熟, 建议忽略 — Phase D v0.4 真正激活 ≥ 2026-06-19"
#
# 使用:
#   bash .claude/skills/signals-collect/scripts/rule-health.sh
#
# 退出码: 0 正常, 1 数据缺失

set -o pipefail

SIGNALS_DIR="${SIGNALS_DIR:-99-跨阶段/signals}"
LOG_DIR="${LOG_DIR:-.claude/logs/tools}"
RULES_FILE="${RULES_FILE:-.claude/rules.md}"
DEV_RULES_FILE="${DEV_RULES_FILE:-03-开发/开发规范.md}"

WINDOW_DAYS="${1:-30}"
TODAY=$(date +%Y-%m-%d)
WINDOW_START=$(date -d "$TODAY - $WINDOW_DAYS days" +%Y-%m-%d 2>/dev/null || echo "$TODAY")

# 数据成熟度检查
check_data_maturity() {
    local issues=0
    # signals data 覆盖天数
    local signals_days=0
    if [ -d "$SIGNALS_DIR" ]; then
        signals_days=$(find "$SIGNALS_DIR" -maxdepth 1 -name "*.md" -newermt "$WINDOW_START" 2>/dev/null | wc -l)
    fi
    # log 覆盖天数
    local log_days=0
    if [ -d "$LOG_DIR" ]; then
        log_days=$(find "$LOG_DIR" -maxdepth 1 -name "*.log" -newermt "$WINDOW_START" 2>/dev/null | wc -l)
    fi

    echo "数据成熟度检查:"
    echo "- signals 文件覆盖最近 ${WINDOW_DAYS}d: $signals_days 份 (期望 ≥ $((WINDOW_DAYS / 7)))"
    echo "- PostToolUse log 覆盖最近 ${WINDOW_DAYS}d: $log_days 份 (期望 ≥ $((WINDOW_DAYS / 2)))"

    if [ "$signals_days" -lt $((WINDOW_DAYS / 7)) ]; then
        echo "- ⚠️ signals 数据不足 (按周维护建议 ≥ $((WINDOW_DAYS / 7)) 份)"
        issues=$((issues + 1))
    fi
    if [ "$log_days" -lt 7 ]; then
        echo "- ⚠️ PostToolUse log 不足 (建议 ≥ 7 份)"
        issues=$((issues + 1))
    fi
    return $issues
}

# 规则清单 (v0.4 首版 hardcoded 5 条代表性规则; 后续 v0.5 可解析 rules.md 自动提取)
list_rules() {
    cat <<'RULES'
RULE-001|MUST|.claude/rules.md §B.2|Conventional Commits 格式|grep '^feat\|^fix\|^docs' git log
RULE-002|MUST=禁|.claude/rules.md §L.2|--no-verify 绕过 hook|grep no-verify in tool log
RULE-003|MUST|.claude/rules.md §D|文件编码 UTF-8 BOM 拒收|grep encoding fix commit
RULE-004|MUST|.claude/rules.md §G|Phase 切换前签 Gate Checklist|count Phase commits without docs(gate)
RULE-005|MUST|.claude/rules.md §L|spec 改前先写 proposal|count Edit canonical without proposal
RULES
}

# 每条规则的违反检测 — v0.4 skeleton
# 输入: rule_id
# 输出: violations|overrides|p_bug
check_rule() {
    local rule_id="$1"
    local violations=0 overrides=0 p_bug=0
    case "$rule_id" in
    RULE-001)
        # commit-msg hook 拒收数 — 历史不可 trace, 简化 0
        violations=0
        # bypass --no-verify 数
        overrides=$(find "$LOG_DIR" -name "*.log" -newermt "$WINDOW_START" 2>/dev/null \
            | xargs cat 2>/dev/null | awk -F'\t' '$2=="Bash" {print $3}' | grep -cE "no-verify|skip.hook")
        ;;
    RULE-002)
        violations=$(find "$LOG_DIR" -name "*.log" -newermt "$WINDOW_START" 2>/dev/null \
            | xargs cat 2>/dev/null | awk -F'\t' '$2=="Bash" {print $3}' | grep -cE "no-verify")
        overrides=$violations   # 完全是 override
        ;;
    RULE-003)
        # fix(encoding) commits 在窗内
        violations=$(git log --since="$WINDOW_START" --until="$TODAY 23:59" --grep="^fix(encoding)" --oneline 2>/dev/null | wc -l)
        overrides=0
        # P0/P1 是否标注关联编码
        p_bug=$(git log --since="$WINDOW_START" --until="$TODAY 23:59" --grep="^fix(encoding)" --grep="P0\|P1" --all-match --oneline 2>/dev/null | wc -l)
        ;;
    RULE-004)
        # Phase commit 数 vs docs(gate) commit 数
        local phase_total docs_gate
        phase_total=$(git log --since="$WINDOW_START" --until="$TODAY 23:59" --grep="Phase 0[1-6]" --oneline 2>/dev/null | wc -l)
        docs_gate=$(git log --since="$WINDOW_START" --until="$TODAY 23:59" --grep="^docs(gate)" --oneline 2>/dev/null | wc -l)
        # 启发式: 缺 gate commit 数
        violations=$((phase_total - docs_gate > 0 ? phase_total - docs_gate : 0))
        overrides=0
        ;;
    RULE-005)
        # Edit canonical 但同 commit 无 proposals/ 文件 — 启发式简化为 grep "silent merge"
        violations=$(grep -lc "silent merge" "$SIGNALS_DIR"/*.md 2>/dev/null | tail -1 | awk -F: '{print $2}')
        violations=${violations:-0}
        overrides=$violations
        ;;
    esac
    echo "$violations|$overrides|$p_bug"
}

# 算法: 给定 violations/overrides/p_bug → 建议
suggest() {
    local current="$1" v="$2" o="$3" p="$4"
    local override_ratio=0
    if [ "$v" -gt 0 ]; then
        override_ratio=$(awk "BEGIN { printf \"%.0f\", $o * 100 / $v }")
    fi

    case "$current" in
    MUST|MUST=禁)
        if [ "$v" -ge 3 ] && [ "$override_ratio" -gt 50 ] && [ "$p" -eq 0 ]; then
            echo "⬇ 降级 MUST → SHOULD (违反高+override 多+无 P0/P1)"
        elif [ "$v" -le 1 ] && [ "$p" -eq 0 ]; then
            echo "✅ 保持 (违反低, 规则有效)"
        else
            echo "⚠️ 复盘 (违反 $v, P0/P1 $p — 需人工评估)"
        fi
        ;;
    SHOULD)
        if [ "$p" -ge 1 ] && [ "$v" -le 1 ]; then
            echo "⬆ 升级 SHOULD → MUST (相关 P0/P1 发生 + 违反低)"
        else
            echo "✅ 保持"
        fi
        ;;
    *)
        echo "? 未知级别"
        ;;
    esac
}

main() {
    echo "# Rule Health Audit — $TODAY (window: last ${WINDOW_DAYS}d, from $WINDOW_START)"
    echo ""

    if ! check_data_maturity; then
        echo ""
        echo "## ⚠️ 数据未成熟 — 建议忽略本次输出"
        echo ""
        echo "Phase D v0.4 激活条件 (per scripts/rule-health.sh 头注释):"
        echo "1. signals 文件 ≥ $((WINDOW_DAYS / 7)) 份"
        echo "2. PostToolUse log ≥ 7 份"
        echo "3. ≥ 2 个完整月度 reflect"
        echo ""
        echo "PLM 当前 (2026-05-19): signals 起于 2026-05-15, log 起于 2026-05-19。"
        echo "**预计成熟期: 2026-06-19** (1 月连续数据)。本次仅做 skeleton 验证。"
        echo ""
    fi

    echo "## 规则健康度建议表"
    echo ""
    echo "| 规则 ID | 位置 | 现级别 | 描述 | 30d 违反 | override | P0/P1 | 建议 |"
    echo "|---|---|---|---|---|---|---|---|"

    while IFS='|' read -r rule_id level location desc detection; do
        [ -z "$rule_id" ] && continue
        IFS='|' read -r v o p < <(check_rule "$rule_id")
        local sugg
        sugg=$(suggest "$level" "$v" "$o" "$p")
        echo "| $rule_id | $location | $level | $desc | $v | $o | $p | $sugg |"
    done < <(list_rules)

    echo ""
    echo "## 算法说明"
    echo ""
    echo "- **降级 MUST → SHOULD**: 30d 违反 ≥ 3 AND override 比 > 50% AND 无 P0/P1"
    echo "- **升级 SHOULD → MUST**: 30d 违反 ≤ 1 AND 有 P0/P1 相关"
    echo "- **保持**: 其余"
    echo "- 建议是**输入到 reflect-monthly**, 由人决策是否走 proposal 升降级"
    echo ""
    echo "## v0.4 → v0.5 路线"
    echo ""
    echo "- v0.4 skeleton: 5 条 hardcoded 规则 (commit 格式 / no-verify / 编码 / Gate 签字 / proposal-first)"
    echo "- v0.5: 自动解析 .claude/rules.md / 开发规范.md MUST/SHOULD 段 (≥ 30 条规则)"
    echo "- v0.6: 跨项目移植 — 规则清单按项目类型差异化加载"
}

main "$@"
