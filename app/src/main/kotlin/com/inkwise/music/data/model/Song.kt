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
    val uri: String,  // 歌曲文件路径
    val path: String,
    val albumArt: Uri? = null,  // 专辑封面
    val isLocal: Boolean = true
)
