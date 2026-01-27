package com.inkwise.music.data.model


import android.net.Uri
import androidx.room.Entity 
import androidx.room.ForeignKey 
import androidx.room.Index
 import androidx.room.PrimaryKey

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val duration: Long,
    val uri: Uri,  // 歌曲文件路径
    val albumArt: Uri? = null,  // 专辑封面
    val isLocal: Boolean = true
)
