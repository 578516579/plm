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

    # 无任何风险信号(非 bulk + 单文件脏 + 无认领冲突)→ 静默放行,不啰嗦
    if [ "$bulk" = 0 ] && [ "$n" -lt 2 ] && [ -z "$hits" ]; then exit 0; fi

    echo "🔀 [session-guard] 多-session 暂存守门(协作规范 §2/§4):working tree 现有 $n 个改动文件。" >&2
    if [ "$bulk" = 1 ]; then
      echo "   ⚠ 高危:命令里用了  git add . / -A / -u / commit -a  这类批量暂存 —— 共享工作树里它会卷进**别人未提交的 WIP**!" >&2
      echo "     → 改用显式路径:git add <你自己的文件1> <文件2> …,然后 git commit(不加 -a)。" >&2
    fi
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
