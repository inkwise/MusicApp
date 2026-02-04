/*package com.inkwise.music.data.dao


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
}*/
package com.inkwise.music.data.dao

import androidx.room.*
import com.inkwise.music.data.model.PlaylistEntity
import com.inkwise.music.data.model.PlaylistSongEntity
import com.inkwise.music.data.model.PlaylistWithSongs
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSongs(playlistSongs: List<PlaylistSongEntity>)

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    fun getPlaylistWithSongs(playlistId: Long): Flow<PlaylistWithSongs>

    @Transaction
    @Query("SELECT * FROM playlists")
    fun getAllPlaylistsWithSongs(): Flow<List<PlaylistWithSongs>>

    @Insert
    suspend fun insert(playlist: PlaylistEntity)
}
