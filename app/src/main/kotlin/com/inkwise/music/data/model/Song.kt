package com.inkwise.music.data.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "songs",
	foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("playlistId")]
)
data class Song(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,            // Room 自增主键

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "artist")
    val artist: String,

    @ColumnInfo(name = "duration")
    val duration: Long,           // 毫秒

    @ColumnInfo(name = "uri")
    val uri: String,              // Content URI 或文件路径

    @ColumnInfo(name = "path")
    val path: String,             // 本地文件路径

    @ColumnInfo(name = "album_art")
    val albumArt: String? = null, // 专辑封面 URI 转 String 存储

    @ColumnInfo(name = "is_local")
    val isLocal: Boolean = true   // 本地标记
)