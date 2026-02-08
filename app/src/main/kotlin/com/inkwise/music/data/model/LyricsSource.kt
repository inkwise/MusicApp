
package com.inkwise.music.data.model

enum class LyricsSource {
    LOCAL_LRC, // 本地 .lrc
    LOCAL_KRC, // 本地 .krc / .qrc
    NETWORK, // 网络获取
    EMBEDDED, // 音频内嵌歌词
    USER_PROVIDED, // 用户导入
}
