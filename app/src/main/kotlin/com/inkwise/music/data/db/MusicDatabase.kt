package com.inkwise.music.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.inkwise.music.data.dao.PlaylistDao
import com.inkwise.music.data.dao.SongDao

import com.inkwise.music.data.model.PlaylistEntity
import com.inkwise.music.data.model.Song
import com.inkwise.music.data.model.PlaylistSongEntity



@Database(
    entities = [
        PlaylistEntity::class,
        Song::class,
        PlaylistSongEntity::class
        
    ],
    version = 1,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {

	abstract fun playlistDao(): PlaylistDao
	abstract fun songDao(): SongDao
	
}