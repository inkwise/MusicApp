package com.inkwise.music.data.repository

import com.inkwise.music.data.dao.SongDao
import com.inkwise.music.data.dao.PlaylistDao
import com.inkwise.music.data.model.Song
import com.inkwise.music.data.model.PlaylistEntity
import com.inkwise.music.data.model.PlaylistSongEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.inkwise.music.data.model.PlaylistWithSongs
class MusicRepository @Inject constructor(
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao
) {

    fun getLocalSongs(): Flow<List<Song>> = songDao.getAllSongs()

    fun getSongsByPlaylist(playlistId: Long): Flow<List<Song>> =
        playlistDao.getPlaylistWithSongs(playlistId)
            .map { it.songs }

    fun getAllPlaylists(): Flow<List<PlaylistWithSongs>> =
        playlistDao.getAllPlaylistsWithSongs()

    suspend fun insertSongs(songs: List<Song>) {
        songDao.insertSongs(songs)
    }

    suspend fun insertPlaylist(title: String, description: String? = null) {
        val playlist = PlaylistEntity(title = title, description = description ?: "", coverUri = null)
        playlistDao.insertPlaylist(playlist)
    }

    suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) {
        val playlistSongs = songIds.map { songId ->
            PlaylistSongEntity(playlistId = playlistId, songId = songId)
        }
        playlistDao.insertPlaylistSongs(playlistSongs)
    }
}