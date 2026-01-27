package com.inkwise.music.data.dao


import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import com.inkwise.music.data.model.PlaylistEntity
import com.inkwise.music.data.model.PlaylistWithSongs
@Dao
interface PlaylistDao {

    @Query("SELECT * FROM playlists")
    fun observePlaylists(): Flow<List<PlaylistEntity>>

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylist(id: Long): PlaylistWithSongs
    
    @Insert
    suspend fun insert(playlist: PlaylistEntity)
}