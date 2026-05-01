#!/data/data/com.termux/files/usr/bin/bash

while inotifywait -r -e modify,create,delete app/src; do
    echo "文件发生变化，开始编译..."
    termux-toast  "文件发生变化，开始编译..."
    ./run.sh
done