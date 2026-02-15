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

    /**
     * 插入或更新单个播放列表。
     * 如果主键冲突，则替换已有记录。
     *
     * @param playlist 要插入的 PlaylistEntity 对象
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    /**
     * 批量插入或更新播放列表中的歌曲。
     * 如果主键冲突，则替换已有记录。
     *
     * @param playlistSongs 要插入的 PlaylistSongEntity 列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSongs(playlistSongs: List<PlaylistSongEntity>)

    /**
     * 查询指定播放列表及其包含的所有歌曲。
     * 使用 @Transaction 确保查询时数据一致性。
     * 返回 Flow 可以实时监听数据变化，UI 可以自动更新。
     *
     * @param playlistId 播放列表 ID
     * @return 包含播放列表和歌曲列表的 PlaylistWithSongs Flow
     */
    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    fun getPlaylistWithSongs(playlistId: Long): Flow<PlaylistWithSongs>

    /**
     * 查询所有播放列表及其包含的所有歌曲。
     * 使用 @Transaction 确保数据一致性。
     * 返回 Flow 可以实时监听列表变化。
     *
     * @return 包含所有播放列表及其歌曲列表的 Flow
     */
    @Transaction
    @Query("SELECT * FROM playlists")
    fun getAllPlaylistsWithSongs(): Flow<List<PlaylistWithSongs>>

    /**
     * 插入单个播放列表。
     * 与 insertPlaylist 类似，但此处使用默认冲突策略（通常是 ABORT）。
     *
     * @param playlist 要插入的 PlaylistEntity 对象
     */
    @Insert
    suspend fun insert(playlist: PlaylistEntity)
}
