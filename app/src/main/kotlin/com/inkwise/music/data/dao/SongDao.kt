package com.inkwise.music.data.dao

import androidx.room.*
import com.inkwise.music.data.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    /** 获取所有本地歌曲，按标题升序 */
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<Song>>

    /** 根据 id 查询单条歌曲 */
    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    suspend fun getSongById(id: Long): Song?

    /** 插入单条歌曲，如果冲突则替换 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song)

    /** 插入多条歌曲 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<Song>)

    /** 删除单条歌曲 */
    @Delete
    suspend fun deleteSong(song: Song)

    /** 根据 id 删除歌曲 */
    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSongById(id: Long)

    /** 清空所有歌曲 */
    @Query("DELETE FROM songs")
    suspend fun clearSongs()

    @Query("SELECT * FROM songs WHERE path = :path LIMIT 1")
    suspend fun getSongByPath(path: String): Song?
}
