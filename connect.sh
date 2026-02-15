#!/bin/bash

# 固定设备 IP
DEVICE_IP="172.19.0.1"

# 获取端口参数
if [ -z "$1" ]; then
    echo "Usage: $0 <PORT>"
    exit 1
fi

PORT="$1"

echo "Trying to connect to $DEVICE_IP:$PORT ..."

# 尝试连接 ADB
adb connect $DEVICE_IP:$PORT
if [ $? -eq 0 ]; then
    echo "✅ Connected to $DEVICE_IP:$PORT"
    adb devices
else
    echo "❌ Failed to connect to $DEVICE_IP:$PORT"
fi