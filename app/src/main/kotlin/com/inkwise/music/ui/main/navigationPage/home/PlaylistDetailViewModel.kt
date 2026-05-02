package com.inkwise.music.ui.main.navigationPage.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inkwise.music.data.dao.PlaylistDao
import com.inkwise.music.data.dao.SongDao
import com.inkwise.music.data.model.PlaylistSongEntity
import com.inkwise.music.data.model.PlaylistWithSongs
import com.inkwise.music.data.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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

    private val playlistId: Long = savedStateHandle.get<Long>("playlistId") ?: 0L

    private val _uiState = MutableStateFlow(PlaylistDetailUiState())
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState

    init {
        viewModelScope.launch {
            playlistDao.getPlaylistWithSongs(playlistId).collect { playlist ->
                _uiState.value = PlaylistDetailUiState(
                    playlist = playlist,
                    songs = playlist.songs,
                    playlistTitle = playlist.playlist.title
                )
            }
        }
    }

    fun removeSongFromPlaylist(songId: Long) {
        viewModelScope.launch {
            playlistDao.removeSongFromPlaylist(playlistId, songId)
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
