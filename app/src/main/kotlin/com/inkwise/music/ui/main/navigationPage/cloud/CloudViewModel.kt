package com.inkwise.music.ui.main.navigationPage.cloud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inkwise.music.data.dao.SongDao
import com.inkwise.music.data.model.Song
import com.inkwise.music.data.network.ApiService
import com.inkwise.music.data.prefs.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CloudSortBy(val label: String, val apiField: String) {
    CUSTOM("自定义", ""),
    TITLE("标题首字母", "title"),
    CREATED_ASC("添加时间正序", "created_at"),
    CREATED_DESC("添加时间倒序", "created_at")
}

data class CloudUiState(
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val sortBy: CloudSortBy = CloudSortBy.CUSTOM,
    val sortOrderAsc: Boolean = true
)

@HiltViewModel
class CloudViewModel @Inject constructor(
    private val api: ApiService,
    private val prefs: PreferencesManager,
    private val songDao: SongDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(CloudUiState())
    val uiState: StateFlow<CloudUiState> = _uiState.asStateFlow()

    init {
        loadSongs()
    }

    fun loadSongs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val token = prefs.authToken.first()
                val serverUrl = prefs.serverUrl.first()
                val songs = fetchAndSaveSongs(token, serverUrl)
                _uiState.value = _uiState.value.copy(
                    songs = songs,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "网络错误: ${e.message}"
                )
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            try {
                val token = prefs.authToken.first()
                val serverUrl = prefs.serverUrl.first()
                val songs = fetchAndSaveSongs(token, serverUrl)
                _uiState.value = _uiState.value.copy(
                    songs = songs,
                    isRefreshing = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = "网络错误: ${e.message}"
                )
            }
        }
    }

    private suspend fun fetchAndSaveSongs(token: String?, serverUrl: String): List<Song> {
        val sortBy = _uiState.value.sortBy
        val sortOrder = if (_uiState.value.sortOrderAsc) "asc" else "desc"
        val sortField = if (sortBy == CloudSortBy.CUSTOM) null else sortBy.apiField

        val response = api.getMusicList(
            token = "Bearer ${token ?: ""}",
            page = 1,
            pageSize = 200,
            sortBy = sortField,
            sortOrder = sortOrder
        )

        if (response.isSuccessful && response.body() != null) {
            val mappedSongs = response.body()!!.data.map { item -> mapToSong(item, serverUrl) }

            // 为每个歌曲确保 DB 中有记录，获取有效 ID
            return mappedSongs.map { song ->
                if (song.cloudId != null) {
                    val existing = songDao.getSongByCloudId(song.cloudId!!)
                    if (existing != null) {
                        existing
                    } else {
                        val newId = songDao.insertSong(song)
                        song.copy(id = newId)
                    }
                } else {
                    val newId = songDao.insertSong(song)
                    song.copy(id = newId)
                }
            }
        } else {
            throw Exception("加载失败: ${response.code()}")
        }
    }

    fun setSortBy(sortBy: CloudSortBy) {
        val asc = when (sortBy) {
            CloudSortBy.CUSTOM -> true
            CloudSortBy.TITLE -> true
            CloudSortBy.CREATED_ASC -> true
            CloudSortBy.CREATED_DESC -> false
        }
        _uiState.value = _uiState.value.copy(sortBy = sortBy, sortOrderAsc = asc)
        loadSongs()
    }

    fun deleteCloudSongs(songIds: List<Long>) {
        viewModelScope.launch {
            try {
                val token = prefs.authToken.first()
                val cloudIds = songIds.mapNotNull { id ->
                    songDao.getSongById(id)?.cloudId
                }
                if (cloudIds.isNotEmpty()) {
                    api.deleteMusic(
                        token = "Bearer ${token ?: ""}",
                        request = com.inkwise.music.data.network.model.DeleteMusicRequest(ids = cloudIds)
                    )
                }
                // 删除本地记录
                for (id in songIds) {
                    songDao.deleteSongById(id)
                }
            } catch (_: Exception) {
            }
            loadSongs()
        }
    }

    private fun mapToSong(
        item: com.inkwise.music.data.network.model.MusicItem,
        serverUrl: String
    ): Song {
        val baseUrl = serverUrl.removeSuffix("/api/v1")

        val streamPath = item.stream_url ?: ""
        val fullStreamUrl = if (streamPath.startsWith("http")) {
            streamPath
        } else {
            baseUrl.trimEnd('/') + streamPath
        }

        val coverPath = item.cover_url ?: ""
        val fullCoverUrl = if (coverPath.isBlank()) {
            null
        } else if (coverPath.startsWith("http")) {
            coverPath
        } else {
            baseUrl.trimEnd('/') + coverPath
        }

        return Song(
            localId = null,
            cloudId = item.id,
            title = item.title,
            artist = item.artist?.name ?: "未知艺术家",
            album = item.album ?: "未知专辑",
            duration = (item.duration * 1000).toLong(),
            codec = item.codec ?: "",
            sampleRate = item.sample_rate ?: 0,
            bitDepth = 0,
            channels = item.channels ?: 0,
            bitrate = item.bitrate ?: 0,
            uri = fullStreamUrl,
            path = fullStreamUrl,
            albumArt = fullCoverUrl,
            isLocal = false
        )
    }
}
