#!/bin/sh
# session-guard.sh — 多-session 主动防护 (协作规范 §2/§4/§19)
#
# 由 .claude/settings.json 的 PreToolUse 钩子调用,两种模式:
#   sh session-guard.sh git    # Bash 工具: 守 git add/commit/push 的暂存范围
#   sh session-guard.sh edit   # Edit/Write 工具: 守"他人已认领的文件"
#
# 设计约定(与本仓库其它 PreToolUse 钩子一致):
#   - 只往 stderr 输出"提示",**永远 exit 0**(nudge,不硬拦),坏了也不能阻断正常工具调用。
#   - 注册表缺失/格式不符 → 静默放行。
#   - 没有真实风险信号时 → 静默放行(降噪)。
#
# 它防的就是 2026-05-27 真踩的坑:共享工作树里两个 session 改同一文件,
# `git add .` / `git commit -a` 会把别人未提交的 WIP 一并提交。

mode="$1"
dir="${CLAUDE_PROJECT_DIR:-.}"
reg="$dir/99-跨阶段/active-sessions.md"

# 读注册表 CLAIMS 标记块里的认领文本(整块返回,供 grep)
claims_block() {
  [ -f "$reg" ] || return 0
  awk '/CLAIMS:START/{f=1;next}/CLAIMS:END/{f=0}f' "$reg" 2>/dev/null
}

# 当前 working tree 脏文件(含 untracked),输出 repo 相对路径,每行一个
# -c core.quotepath=false:否则 git 会把中文路径转成 "\350\267..." 八进制,匹配不上
dirty_files() {
  ( cd "$dir" 2>/dev/null && git -c core.quotepath=false status --porcelain 2>/dev/null | cut -c4- )
}

case "$mode" in
  # ───────────────────────────── git add / commit / push ─────────────────────
  git)
    cmd="${CLAUDE_TOOL_INPUT_command:-}"
    case "$cmd" in
      *"git add"*|*"git commit"*|*"git push"*) : ;;
      *) exit 0 ;;
    esac

    # push:整分支同步提醒(2026-05-27 推送顺带带走了并行 session 的 4 个 commit)
    case "$cmd" in
      *"git push"*)
        echo "🔀 [session-guard] git push 会把整条分支(含其它 session 在此分支的 commit)一起推到远端。" >&2
        echo "   先确认  git log @{u}..HEAD --oneline  只列你自己的 commit;不是的就先沟通。" >&2
        exit 0 ;;
    esac

    df=$(dirty_files)
    n=$(printf '%s\n' "$df" | sed '/^$/d' | wc -l | tr -d ' ')
    n=${n:-0}

    # 是否 bulk 暂存(高危)
    bulk=0
    case "$cmd" in
      *"git add ."*|*"git add -A"*|*"git add --all"*|*"git add -u"*|*"git add ./"*) bulk=1 ;;
    esac
    case "$cmd" in
      *"commit -a"*|*"commit -am"*|*"commit -ma"*|*"commit -a "*|*" -a -m"*|*" -am "*|*" -ma "*) bulk=1 ;;
    esac

    # 被他人认领 且 当前脏 的文件
    cb=$(claims_block)
    hits=""
    if [ -n "$cb" ] && [ "$n" -ge 1 ]; then
      hits=$(printf '%s\n' "$df" | while IFS= read -r f; do
        [ -n "$f" ] || continue
        if printf '%s' "$cb" | grep -qF -- "$f" 2>/dev/null; then printf '      • %s\n' "$f"; fi
      done)
    fi

    # 显式后门(proposal 0030):本次 Bash 调用内 export CLAUDE_BULK_OK="<≥10 字符 reason>"
    # 适用 epic 多模块批量 commit / bulk-refactor / 模板化改造等合法 bulk 场景。
    # reason 会进 stderr 日志,后续 signals 月度统计 bulk_ok_count(>5/月 触发审视)。
    if [ "$bulk" = 1 ] && [ -n "$CLAUDE_BULK_OK" ] && [ "${#CLAUDE_BULK_OK}" -ge 10 ]; then
      echo "ℹ️  [session-guard] CLAUDE_BULK_OK 后门生效:" >&2
      echo "   reason: $CLAUDE_BULK_OK" >&2
      echo "   放行本次 bulk add。注意 reason 会进 signals 月度统计。" >&2
      exit 0
    fi

    # 一次性绕过(proposal 0030):export CLAUDE_BYPASS_SESSION_GUARD=1
    # 计入 signals 月度 bypass(同 git commit --no-verify 等价语义)
    if [ "$bulk" = 1 ] && [ "$CLAUDE_BYPASS_SESSION_GUARD" = "1" ]; then
      echo "⚠️  [session-guard] CLAUDE_BYPASS_SESSION_GUARD=1 一次性绕过(计入 signals bypass)。" >&2
      exit 0
    fi

    # 无任何风险信号(非 bulk + 单文件脏 + 无认领冲突)→ 静默放行,不啰嗦
    if [ "$bulk" = 0 ] && [ "$n" -lt 2 ] && [ -z "$hits" ]; then exit 0; fi

    # ─── proposal 0030 硬拦核心:bulk add + working tree 有脏文件 = race 必然路径 ───
    if [ "$bulk" = 1 ] && [ "$n" -ge 1 ]; then
      echo "" >&2
      echo "⛔ [session-guard] HARD-BLOCK: bulk add 在共享 working tree 是协作 race 源头。" >&2
      echo "   ❗ working tree 有 $n 个改动文件,bulk add 会卷进所有 — 含别 session 的 WIP。" >&2
      echo "   📜 历史事故:3ae00fd(P0-1 22 文件被偷)/ 656a6a4(P0-2C 11 文件被偷)" >&2
      if [ -n "$hits" ]; then
        echo "   🚨 其中以下文件在 active-sessions.md 里被他人认领:" >&2
        printf '%s\n' "$hits" >&2
      fi
      echo "" >&2
      echo "   修复方式(任选):" >&2
      echo "   a) 推荐:用显式路径 — git add <文件1> <文件2> ..." >&2
      echo "   b) epic / bulk-refactor 合法 bulk:" >&2
      echo "      export CLAUDE_BULK_OK=\"<reason 不少于 10 字>\" && <原命令>" >&2
      echo "   c) 一次性绕过(计入 signals bypass):" >&2
      echo "      export CLAUDE_BYPASS_SESSION_GUARD=1 && <原命令>" >&2
      echo "" >&2
      echo "   参考:99-跨阶段/proposals/0030 / project-quirks Q-COLLAB-01" >&2
      exit 2
    fi

    # bulk=0 但有他人认领冲突 → 保留原 nudge(不升级硬拦,避免误杀人工只动单文件场景)
    echo "🔀 [session-guard] 多-session 暂存守门(协作规范 §2/§4):working tree 现有 $n 个改动文件。" >&2
    if [ -n "$hits" ]; then
      echo "   ⛔ 下列脏文件在 active-sessions.md 里被认领,**勿 stage**(除非确认是你这条 session 的认领):" >&2
      printf '%s\n' "$hits" >&2
      echo "     → 同一文件含多 session 改动时用「拆开」:临时移除他人段 → 提交你的 → byte-perfect 还原(§19)。" >&2
    fi
    echo "   ✔ 正确姿势:只提交自己改的文件,用显式路径 add;开工前在 active-sessions.md 登记认领。" >&2
    exit 0 ;;

  # ───────────────────────────── Edit / Write ────────────────────────────────
  edit)
    fp="${CLAUDE_TOOL_INPUT_file_path:-}"
    [ -n "$fp" ] || exit 0
    cb=$(claims_block)
    [ -n "$cb" ] || exit 0
    base=$(basename "$(printf '%s' "$fp" | sed 's#\\#/#g')")
    [ -n "$base" ] || exit 0
    if printf '%s' "$cb" | grep -qF -- "$base" 2>/dev/null; then
      line=$(printf '%s' "$cb" | grep -F -- "$base" 2>/dev/null | head -1)
      echo "🔀 [session-guard] 你要改的「$base」在 active-sessions.md 里被认领:" >&2
      echo "   $line" >&2
      echo "   不是你这条 session 的认领 → 按协作规范 §4 协调,别撞改;是你的 → 忽略本提示。" >&2
    fi
    exit 0 ;;

  *) exit 0 ;;
esac
