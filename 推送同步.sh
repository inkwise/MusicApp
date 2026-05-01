#!/usr/bin/env bash
set -e

BRANCH="main"
REMOTE="origin"

echo "▶ 检查当前分支…"
CURRENT_BRANCH=$(git branch --show-current)
if [ "$CURRENT_BRANCH" != "$BRANCH" ]; then
  echo "❌ 当前分支是 $CURRENT_BRANCH，请切换到 $BRANCH"
  exit 1
fi

echo "▶ 拉取远程最新状态（仅用于校验，不合并）"
git fetch $REMOTE

echo "▶ 检查工作区状态…"
if ! git diff --quiet || ! git diff --cached --quiet; then
  echo "▶ 检测到变更（包含删除），提交中…"
  git add -A
  git commit -m "sync local state"
else
  echo "✔ 工作区干净，无需提交"
fi

echo "▶ 强制同步中（本地将完全覆盖远程 $REMOTE/$BRANCH）…"
git push $REMOTE $BRANCH --force-with-lease

echo "✅ 同步完成：远程已与本地完全一致"