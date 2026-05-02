package com.inkwise.music.ui.main.navigationPage.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inkwise.music.data.dao.PlaylistDao
import com.inkwise.music.data.model.PlaylistEntity
import com.inkwise.music.data.model.PlaylistSongEntity
import com.inkwise.music.data.model.PlaylistWithSongs
import com.inkwise.music.data.network.ApiService
import com.inkwise.music.data.network.model.CreatePlaylistRequest
import com.inkwise.music.data.prefs.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val api: ApiService,
    private val prefs: PreferencesManager
) : ViewModel() {

    private val _playlists = MutableStateFlow<List<PlaylistWithSongs>>(emptyList())
    val playlists: StateFlow<List<PlaylistWithSongs>> = _playlists

    init {
        viewModelScope.launch {
            playlistDao.getAllPlaylistsWithSongs().collect { list ->
                _playlists.value = list
            }
        }
    }

    fun createPlaylist(title: String) {
        viewModelScope.launch {
            val token = prefs.authToken.first()
            val isLoggedIn = !token.isNullOrEmpty()

            if (isLoggedIn) {
                // 尝试云端创建
                try {
                    val response = api.createPlaylist(
                        token = "Bearer $token",
                        request = CreatePlaylistRequest(name = title)
                    )
                    if (response.isSuccessful && response.body() != null) {
                        val cloudPlaylist = response.body()!!.playlist
                        playlistDao.insertPlaylist(
                            PlaylistEntity(
                                title = cloudPlaylist.name,
                                description = cloudPlaylist.description ?: "",
                                coverUri = cloudPlaylist.cover_url,
                                cloudId = cloudPlaylist.id
                            )
                        )
                        return@launch
                    }
                } catch (_: Exception) {
                    // 云端创建失败，降级为本地创建
                }
            }

            // 本地创建
            playlistDao.insertPlaylist(
                PlaylistEntity(
                    title = title,
                    description = "",
                    coverUri = null,
                    cloudId = null
                )
            )
        }
    }

    fun deletePlaylist(playlist: PlaylistEntity) {
        viewModelScope.launch {
            playlistDao.deletePlaylist(playlist)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            playlistDao.insertPlaylistSongs(
                listOf(PlaylistSongEntity(playlistId = playlistId, songId = songId))
            )
        }
    }
}
