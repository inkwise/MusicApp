package com.inkwise.music.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.inkwise.music.data.dao.PlaylistDao
import com.inkwise.music.data.dao.SongDao
import com.inkwise.music.data.model.PlaylistEntity
import com.inkwise.music.data.model.PlaylistSongEntity
import com.inkwise.music.data.model.Song

@Database(
    entities = [
        PlaylistEntity::class,
        Song::class,
        PlaylistSongEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun songDao(): SongDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE playlist_song ADD COLUMN sort_order INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): MusicDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                MusicDatabase::class.java,
                "music_database"
            )
                .addMigrations(MIGRATION_3_4)
                .build()
        }
    }
}
