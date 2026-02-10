#!/usr/bin/env bash

set -euo pipefail

VERSION_FILE="./VERSION"

# 初始化版本号
if [ ! -f "$VERSION_FILE" ]; then
  echo "0.0.000" > "$VERSION_FILE"
fi

VERSION=$(cat "$VERSION_FILE")
IFS='.' read -r MAJOR MINOR PATCH <<< "$VERSION"

# ---------------- 自动进位逻辑 ----------------
PATCH=$((10#$PATCH + 1))
if [ "$PATCH" -ge 1000 ]; then
  PATCH=0
  MINOR=$((MINOR + 1))
fi

if [ "$MINOR" -ge 100 ]; then
  MINOR=0
  MAJOR=$((MAJOR + 1))
fi

# PATCH 强制三位数，MAJOR、MINOR 原生显示
PATCH=$(printf "%03d" "$PATCH")

NEW_VERSION="${MAJOR}.${MINOR}.${PATCH}"
# ---------------------------------------------

echo "▶ 当前版本: $VERSION"
echo "▶ 新版本: $NEW_VERSION"

# 更新 VERSION 文件
echo "$NEW_VERSION" > "$VERSION_FILE"

# 构建 / 安装 / 启动
#if ./gradlew assembleRelease; then
if ./gradlew assembleDebug; then


  echo "✅ 构建成功"
  COMMIT_MSG="$NEW_VERSION"

else
  echo "❌ 构建失败"
  COMMIT_MSG="e$NEW_VERSION"
fi

# Git 提交 & 推送
git add .
git commit -m "$COMMIT_MSG"
#git push

echo "🚀 已提交并推送：$COMMIT_MSG"