package com.inkwise.music.data.model


import android.net.Uri

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val duration: Long,
    val uri: Uri,  // 歌曲文件路径
    val albumArt: Uri? = null,  // 专辑封面
    val isLocal: Boolean = true
)