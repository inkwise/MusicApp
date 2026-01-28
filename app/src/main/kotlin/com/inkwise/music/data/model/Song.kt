package com.inkwise.music.data.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index
@Entity(tableName = "songs")
data class Song(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,            // Room 自增主键
	//本地媒体id
	@ColumnInfo(name = "local_id")
    val localId: Long? = null,,
    //云端id
	@ColumnInfo(name = "cloud_id")
    val cloudId: Long? = null,,
	//标题
    @ColumnInfo(name = "title")
    val title: String,
	//歌手
    @ColumnInfo(name = "artist")
    val artist: String,
	//时长
    @ColumnInfo(name = "duration")
    val duration: Long,           // 毫秒
	//url
    @ColumnInfo(name = "uri")
    val uri: String,              // Content URI 或文件路径
	//文件路径
    @ColumnInfo(name = "path")
    val path: String,             // 本地文件路径
	//封面url
    @ColumnInfo(name = "album_art")
    val albumArt: String? = null, // 专辑封面 URI 转 String 存储
	//是否本地
    @ColumnInfo(name = "is_local")
    val isLocal: Boolean = true   // 本地标记
)