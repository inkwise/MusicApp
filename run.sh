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

# 先构建，根据结果决定后续操作
if ./gradlew assembleDebug; then
  echo "✅ 构建成功"

  # 构建成功后才更新 VERSION 文件
  echo "$NEW_VERSION" > "$VERSION_FILE"

  # 安装并启动 App
  adb install -r ./app/build/outputs/apk/debug/app-debug.apk && adb shell am start -n com.inkwise.music/.MainActivity

  # Git 提交 & 推送
  git add .
  git commit -m "$NEW_VERSION"
  git push

  echo "🚀 已提交并推送：$NEW_VERSION"
else
  echo "❌ 构建失败"
#  termux-toast "❌ 构建失败"
  # 不更新 VERSION，不 commit，不 push
  exit 1
fi