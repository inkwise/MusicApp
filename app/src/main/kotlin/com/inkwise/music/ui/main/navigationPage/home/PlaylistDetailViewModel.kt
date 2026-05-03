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
import com.inkwise.music.ui.main.navigationPage.components.SortMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class PlaylistDetailUiState(
    val playlist: PlaylistWithSongs? = null,
    val songs: List<Song> = emptyList(),
    val playlistTitle: String = ""
)

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playlistDao: PlaylistDao,
    private val songDao: SongDao
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
                playlistDao.getPlaylistWithSongs(playlistId),
                _sortMode
            ) { playlist, mode ->
                _rawSongs.value = playlist.songs
                val sorted = applySort(playlist.songs, mode)
                PlaylistDetailUiState(
                    playlist = playlist,
                    songs = sorted,
                    playlistTitle = playlist.playlist.title
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

    fun removeSongFromPlaylist(songId: Long) {
        viewModelScope.launch {
            playlistDao.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun removeSongsFromPlaylist(songIds: Set<Long>) {
        viewModelScope.launch {
            for (id in songIds) {
                playlistDao.removeSongFromPlaylist(playlistId, id)
            }
        }
    }

    fun deleteSongsPermanently(songs: List<Song>, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            for (song in songs) {
                try {
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
}
