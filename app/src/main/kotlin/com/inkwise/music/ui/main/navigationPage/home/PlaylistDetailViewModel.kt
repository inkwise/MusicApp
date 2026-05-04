package com.inkwise.music.ui.main.navigationPage.home

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inkwise.music.data.dao.PlaylistDao
import com.inkwise.music.data.dao.SongDao
import com.inkwise.music.data.model.PlaylistSongEntity
import com.inkwise.music.data.model.PlaylistWithSongs
import com.inkwise.music.data.model.Song
import com.inkwise.music.data.network.ApiService
import com.inkwise.music.data.prefs.PreferencesManager
import com.inkwise.music.ui.main.navigationPage.components.SortMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class PlaylistDetailUiState(
    val playlist: PlaylistWithSongs? = null,
    val songs: List<Song> = emptyList(),
    val playlistTitle: String = "",
    val isRefreshing: Boolean = false
)

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playlistDao: PlaylistDao,
    private val songDao: SongDao,
    private val api: ApiService,
    private val prefs: PreferencesManager,
) : ViewModel() {

    val playlistId: Long = savedStateHandle.get<Long>("playlistId") ?: 0L

    private val _uiState = MutableStateFlow(PlaylistDetailUiState())
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState

    private val _sortMode = MutableStateFlow(SortMode.CUSTOM)
    val sortMode: StateFlow<SortMode> = _sortMode
    private val _rawSongs = MutableStateFlow<List<Song>>(emptyList())

    init {
        viewModelScope.launch {
            combine(
                playlistDao.getPlaylistSongsOrdered(playlistId),
                playlistDao.getPlaylistWithSongs(playlistId),
                _sortMode
            ) { songs, playlistWithSongs, mode ->
                _rawSongs.value = songs
                val sorted = applySort(songs, mode)
                PlaylistDetailUiState(
                    playlist = playlistWithSongs,
                    songs = sorted,
                    playlistTitle = playlistWithSongs.playlist.title
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setSortMode(mode: SortMode) {
        _sortMode.value = mode
    }

    private fun applySort(songs: List<Song>, mode: SortMode): List<Song> =
        when (mode) {
            SortMode.CUSTOM -> songs
            SortMode.TITLE -> songs.sortedBy { it.title.lowercase() }
            SortMode.ADDED_ASC -> songs.sortedBy { it.id }
            SortMode.ADDED_DESC -> songs.sortedByDescending { it.id }
        }

    // 从歌单移除（云端歌单同步调用 API）
    fun removeSongFromPlaylist(songId: Long) {
        viewModelScope.launch {
            val playlist = playlistDao.getPlaylistById(playlistId)
            val song = songDao.getSongById(songId)
            // 云端歌单同步到服务端
            if (playlist?.cloudId != null && song?.cloudId != null) {
                try {
                    val token = prefs.authToken.first()
                    api.removeMusicFromPlaylist(
                        token = "Bearer $token",
                        playlistId = playlist.cloudId,
                        musicId = song.cloudId
                    )
                } catch (_: Exception) {
                }
            }
            playlistDao.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun removeSongsFromPlaylist(songIds: Set<Long>) {
        viewModelScope.launch {
            for (id in songIds) {
                removeSongFromPlaylist(id)
            }
        }
    }

    // 永久删除（云端歌曲调用 API）
    fun deleteSongsPermanently(songs: List<Song>, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            for (song in songs) {
                try {
                    // 云端歌曲从服务端删除
                    if (!song.isLocal && song.cloudId != null) {
                        try {
                            val token = prefs.authToken.first()
                            api.deleteMusic(
                                token = "Bearer $token",
                                musicId = song.cloudId
                            )
                        } catch (_: Exception) {
                        }
                    }
                    // 本地文件删除
                    if (song.isLocal && song.localId != null) {
                        val uri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            song.localId
                        )
                        context.contentResolver.delete(uri, null, null)
                    }
                    if (song.path.isNotEmpty()) {
                        val file = File(song.path)
                        if (file.exists()) file.delete()
                    }
                } catch (_: Exception) {
                }
                songDao.deleteSong(song)
            }
        }
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch {
            // 云端歌曲从服务端删除
            if (!song.isLocal && song.cloudId != null) {
                try {
                    val token = prefs.authToken.first()
                    api.deleteMusic(
                        token = "Bearer $token",
                        musicId = song.cloudId
                    )
                } catch (_: Exception) {
                }
            }
            songDao.deleteSong(song)
        }
    }

    fun addToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            playlistDao.insertPlaylistSongs(
                listOf(PlaylistSongEntity(playlistId = playlistId, songId = songId))
            )
        }
    }

    // 拖拽排序 — 稳定回调版本，供 DragReorderState 使用
    fun reorderSongsByIndex(from: Int, to: Int) {
        val currentSongs = _uiState.value.songs.toMutableList()
        val item = currentSongs.removeAt(from)
        currentSongs.add(to, item)
        reorderSongs(currentSongs)
    }

    // 拖拽排序（本地 + 云端 API）
    fun reorderSongs(reorderedSongs: List<Song>) {
        viewModelScope.launch {
            // 更新本地顺序：清除旧关联，按新顺序重建（带 sort_order）
            playlistDao.clearPlaylistSongs(playlistId)
            for ((sortOrder, song) in reorderedSongs.withIndex()) {
                playlistDao.insertPlaylistSongs(
                    listOf(PlaylistSongEntity(playlistId = playlistId, songId = song.id, sortOrder = sortOrder))
                )
            }

            // 云端歌单同步排序
            val playlist = playlistDao.getPlaylistById(playlistId)
            if (playlist?.cloudId != null) {
                try {
                    val token = prefs.authToken.first()
                    api.reorderPlaylistSongs(
                        token = "Bearer $token",
                        playlistId = playlist.cloudId,
                        request = com.inkwise.music.data.network.model.ReorderPlaylistRequest(
                            music_ids = reorderedSongs.mapNotNull { it.cloudId }
                        )
                    )
                } catch (_: Exception) {
                }
            }
        }
    }

    // 从服务端刷新歌单歌曲列表
    fun refreshSongs() {
        viewModelScope.launch {
            val playlist = playlistDao.getPlaylistById(playlistId) ?: return@launch
            val cloudId = playlist.cloudId ?: return@launch
            if (!prefs.isLoggedInNow()) return@launch

            _uiState.value = _uiState.value.copy(isRefreshing = true)
            try {
                val token = prefs.authToken.first()
                val response = api.getPlaylistSongs(
                    token = "Bearer $token",
                    playlistId = cloudId
                )
                if (response.isSuccessful && response.body() != null) {
                    val serverSongs = response.body()!!.songs
                    // 清除旧的关联，按 cloudId 重建
                    playlistDao.clearPlaylistSongs(playlistId)
                    for (item in serverSongs) {
                        val localSong = songDao.getSongByCloudId(item.id)
                        if (localSong != null) {
                            playlistDao.insertPlaylistSongs(
                                listOf(PlaylistSongEntity(playlistId = playlistId, songId = localSong.id))
                            )
                        }
                    }
                }
            } catch (_: Exception) {
            } finally {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }
}
