#!/usr/bin/env bash
# phase-duration.sh — signals-collect v0.2
#
# 输入: 99-跨阶段/gate-checklists/instances/<module>/Phase*-*-Gate-*.md
# 输出: per-module Phase 时间表 + 跨模块汇总 + 异常段 + 4D 期望对照
#
# 调用:
#   bash .claude/skills/signals-collect/scripts/phase-duration.sh
#   bash .claude/skills/signals-collect/scripts/phase-duration.sh project   # 单模块
#
# 算法 (per Phase D v0.2 设计):
#   文件名 Phase{NN}-<name>-Gate-{YYYY-MM-DD}.md (Phase 01-06)
#   Phase 06 含 "day7" / "closure" → exit, 否则 entry
#   entry = 该 Phase 最早日期; exit = 该 Phase 最晚日期
#   within = exit - entry (天); gap = 本 Phase entry - 上 Phase entry (天)
#   异常: within > 7d (P01-05) / > 14d (P06) / gap > 7d / P01-03 缺失
#   4D 期望: per proposal 0007/0010/0011/0012 §B 表 (solo + early)
#
# 性能说明: 单次 calc_all 把所有模块写到 cache 文件; summary 从 cache 计算, 避免重复 spawn.

set -o pipefail

INSTANCES_DIR="${INSTANCES_DIR:-99-跨阶段/gate-checklists/instances}"
SINGLE_MODULE="${1:-}"
CACHE=$(mktemp -t phase-cache.XXXXXX)
trap 'rm -f "$CACHE"' EXIT

MODULES_ALL="defect document project requirement sprint task testcase"

# 工具: 计算 2 日期间天数
days_between() {
    local d1="$1" d2="$2"
    if [ "$d1" = "—" ] || [ "$d2" = "—" ]; then echo "N/A"; return; fi
    local s1 s2
    s1=$(date -d "$d1" +%s 2>/dev/null) || { echo "N/A"; return; }
    s2=$(date -d "$d2" +%s 2>/dev/null) || { echo "N/A"; return; }
    echo $(( (s2 - s1) / 86400 ))
}

# 提取所有 instance 文件元数据一次性, 写到 cache (避免重复 spawn)
# 格式: module|phase|date|kind
build_cache() {
    : > "$CACHE"
    for module in $MODULES_ALL; do
        local module_dir="$INSTANCES_DIR/$module"
        [ -d "$module_dir" ] || continue
        find "$module_dir" -maxdepth 1 -name "Phase*-*-Gate-*.md" -type f 2>/dev/null \
        | while read -r f; do
            local name phase d kind
            name=$(basename "$f")
            phase=$(echo "$name" | sed -E 's/^Phase([0-9]{2}).*$/\1/')
            d=$(echo "$name" | sed -E 's/^.*-([0-9]{4}-[0-9]{2}-[0-9]{2})\.md$/\1/')
            kind="entry"
            case "$name" in *day7*|*closure*) kind="exit" ;; esac
            echo "$module|$phase|$d|$kind" >> "$CACHE"
        done
    done
    sort -u "$CACHE" -o "$CACHE"
}

# 从 cache 计算 per-module rows. 输出 module|Pxx|entry|exit|within|gap
calc_from_cache() {
    local module_filter="$1"
    awk -F'|' -v MOD="$module_filter" '
    {
        m = $1; p = $2; d = $3; k = $4
        if (MOD != "" && m != MOD) next
        key = m "|" p
        if (k == "entry") {
            if (!(key in entry) || d < entry[key]) entry[key] = d
            if (!(key in exit_d) || d > exit_d[key]) exit_d[key] = d
        } else {
            if (!(key in exit_d) || d > exit_d[key]) exit_d[key] = d
        }
        mods[m] = 1
    }
    END {
        n = 0
        for (m in mods) modlist[n++] = m
        # sort modules: alphabetical via simple insertion sort
        for (i = 1; i < n; i++) {
            for (j = i; j > 0 && modlist[j] < modlist[j-1]; j--) {
                t = modlist[j]; modlist[j] = modlist[j-1]; modlist[j-1] = t
            }
        }
        for (i = 0; i < n; i++) {
            m = modlist[i]
            for (p = 1; p <= 6; p++) {
                pp = sprintf("%02d", p)
                key = m "|" pp
                e = (key in entry) ? entry[key] : "—"
                x = (key in exit_d) ? exit_d[key] : "—"
                # 输出 module|Pxx|entry|exit, 后处理填 within/gap
                printf "%s|P%s|%s|%s\n", m, pp, e, x
            }
        }
    }' "$CACHE"
}

# 单 row 后处理: 行末追加 within / gap (调用 days_between)
postprocess_rows() {
    local module_filter="$1"
    declare -A prev_entry
    while IFS='|' read -r m p e x; do
        [ -z "$m" ] && continue
        local within="—" gap="—"
        if [ "$e" != "—" ] && [ "$x" != "—" ]; then
            within=$(days_between "$e" "$x")"d"
        fi
        local p_idx="${p#P}"
        local prev_pn=$((10#$p_idx - 1))
        local prev_idx
        prev_idx=$(printf "P%02d" "$prev_pn")
        local prev_e="${prev_entry[$m|$prev_idx]:-}"
        if [ -n "$prev_e" ] && [ "$prev_e" != "—" ] && [ "$e" != "—" ]; then
            gap=$(days_between "$prev_e" "$e")"d"
        fi
        prev_entry[$m|$p]="$e"
        echo "$m|$p|$e|$x|$within|$gap"
    done < <(calc_from_cache "$module_filter")
}

# 跨模块汇总: 从所有 rows 算 per-Phase 统计
summary_from_rows() {
    local rows_file="$1"
    for p in P01 P02 P03 P04 P05 P06; do
        awk -F'|' -v PHASE="$p" '
        $2 == PHASE {
            w = $5; sub(/d$/, "", w)
            g = $6; sub(/d$/, "", g)
            if (w ~ /^[0-9]+$/) { wsum += w; wcount++; wvals[wcount] = w + 0 }
            if (g ~ /^[0-9]+$/) { gsum += g; gcount++ }
        }
        END {
            if (wcount > 0) {
                # 排序求中位
                for (i = 1; i < wcount; i++) {
                    for (j = i; j > 0 && wvals[j] < wvals[j-1]; j--) {
                        t = wvals[j]; wvals[j] = wvals[j-1]; wvals[j-1] = t
                    }
                }
                wavg = sprintf("%.1f", wsum / wcount)
                wmed = wvals[int((wcount + 1) / 2)]
            } else { wavg = "—"; wmed = "—" }
            if (gcount > 0) gavg = sprintf("%.1f", gsum / gcount); else gavg = "—"
            printf "%s|%d|%sd|%sd|%sd\n", PHASE, wcount, wavg, wmed, gavg
        }' "$rows_file"
    done
}

# 异常段: 从 rows 算
anomalies_from_rows() {
    local rows_file="$1"
    awk -F'|' '
    {
        m = $1; p = $2; e = $3; x = $4; w = $5; g = $6
        wn = w; sub(/d$/, "", wn)
        gn = g; sub(/d$/, "", gn)
        threshold = (p == "P06") ? 14 : 7
        if (wn ~ /^[0-9]+$/ && wn + 0 > threshold) {
            printf "- [%s / %s] within=%s > %dd (Phase 异常长) entry=%s exit=%s\n", m, p, w, threshold, e, x
        }
        if (gn ~ /^[0-9]+$/ && gn + 0 > 7) {
            printf "- [%s / %s] gap=%s > 7d (前 Phase 到本 Phase 间隔异常)\n", m, p, g
        }
        if ((p == "P01" || p == "P02" || p == "P03") && e == "—") {
            printf "- [%s / %s] 缺失 instance (P01-03 必产)\n", m, p
        }
    }' "$rows_file"
}

main() {
    if [ ! -d "$INSTANCES_DIR" ]; then
        echo "ERROR: $INSTANCES_DIR not found" >&2
        exit 1
    fi

    build_cache

    local ROWS
    ROWS=$(mktemp -t phase-rows.XXXXXX)
    trap 'rm -f "$CACHE" "$ROWS"' EXIT
    postprocess_rows "$SINGLE_MODULE" > "$ROWS"

    echo "## 3. Phase 耗时 (v0.2 auto-compute)"
    echo ""
    echo "### 3.1 各模块 Phase 时间表"
    echo ""
    echo "| 模块 | Phase | entry | exit | within | gap |"
    echo "|---|---|---|---|---|---|"
    awk -F'|' 'BEGIN{OFS=" | "} {print "| " $1, $2, $3, $4, $5, $6 " |"}' "$ROWS"

    echo ""
    echo "### 3.2 跨模块汇总"
    echo ""
    echo "| Phase | 完成模块数 | 平均 within | 中位 within | 平均 gap |"
    echo "|---|---|---|---|---|"
    local SUM
    SUM=$(summary_from_rows "$ROWS")
    echo "$SUM" | awk -F'|' 'BEGIN{OFS=" | "} {print "| " $1, $2, $3, $4, $5 " |"}'

    echo ""
    echo "### 3.3 异常 / 缺失"
    echo ""
    local ANOM
    ANOM=$(anomalies_from_rows "$ROWS")
    if [ -z "$ANOM" ]; then
        echo "- ✅ 无异常 (所有 Phase within ≤ 阈值, gap ≤ 7d, P01-03 instance 全)"
    else
        echo "$ANOM"
    fi

    echo ""
    echo "### 3.4 4D 参数化期望对照 (per proposal 0007/0010/0011/0012)"
    echo ""
    local p01_med p05_med p06_med p01_n p05_n p01_status="❌" p05_status="❌"
    p01_med=$(echo "$SUM" | awk -F'|' '$1=="P01" {print $4}')
    p05_med=$(echo "$SUM" | awk -F'|' '$1=="P05" {print $4}')
    p06_med=$(echo "$SUM" | awk -F'|' '$1=="P06" {print $4}')
    p01_n=$(echo "$p01_med" | sed -E 's/d$//')
    p05_n=$(echo "$p05_med" | sed -E 's/d$//')
    if echo "$p01_n" | grep -qE '^[0-9]+$' && [ "$p01_n" -le 2 ]; then p01_status="✅"; fi
    if echo "$p05_n" | grep -qE '^[0-9]+$' && [ "$p05_n" -le 5 ]; then p05_status="✅"; fi
    echo "| 维度 | 期望 within | 实际中位 | 状态 |"
    echo "|---|---|---|---|"
    echo "| solo + early × Phase 01-03 | ≤ 2d | ${p01_med} (P01) | ${p01_status} |"
    echo "| solo + early × Phase 04-05 | ≤ 5d | ${p05_med} (P05) | ${p05_status} |"
    echo "| solo + early × Phase 06 cycle | = 7d (per proposal 0012) | ${p06_med} (P06) | 见 §3.3 (cycle1 day0→day7 完整模块) |"
    echo ""
    echo "**说明**: 期望值按 PLM 当前状态 (solo dev / early maturity) 取自 proposal 0007/0010/0011/0012 §B 表。"
}

main "$@"
