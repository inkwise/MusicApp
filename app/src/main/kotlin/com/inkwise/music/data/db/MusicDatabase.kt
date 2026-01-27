package com.inkwise.music.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.inkwise.music.data.dao.PlaylistDao
import com.inkwise.music.data.model.PlaylistEntity
import com.inkwise.music.data.model.Song


@Database(
    entities = [
        PlaylistEntity::class,
        Song::class
        
    ],
    version = 1,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {

	abstract fun playlistDao(): PlaylistDao
	
}