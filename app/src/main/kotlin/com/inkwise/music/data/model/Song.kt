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

import androidx.room.Entity import androidx.room.ForeignKey import androidx.room.Index import androidx.room.PrimaryKey

@Entity( tableName = "songs", 
	foreignKeys = [ ForeignKey( entity = Playlist::class, parentColumns = ["id"], childColumns = ["playlistId"], onDelete = ForeignKey.CASCADE ) ], indices = [Index("playlistId")] ) 
data class SongEntity( 
	@PrimaryKey(autoGenerate = true)
	 val id: Long = 0, 
	 val playlistId: Long, 
	 val title: String,
	  val artist: String? = null
)
