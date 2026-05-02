package com.inkwise.music.ui.main.navigationPage.cloud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    CREATED_AT("时间排序", "created_at"),
    TITLE("首字母排序", "title")
}

data class CloudUiState(
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val sortBy: CloudSortBy = CloudSortBy.CREATED_AT,
    val sortOrderAsc: Boolean = false
)

@HiltViewModel
class CloudViewModel @Inject constructor(
    private val api: ApiService,
    private val prefs: PreferencesManager,
    private val songDao: com.inkwise.music.data.dao.SongDao
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
                val sortBy = _uiState.value.sortBy
                val sortOrder = if (_uiState.value.sortOrderAsc) "asc" else "desc"

                val response = api.getMusicList(
                    token = "Bearer ${token ?: ""}",
                    page = 1,
                    pageSize = 200,
                    sortBy = sortBy.apiField,
                    sortOrder = sortOrder
                )

                if (response.isSuccessful && response.body() != null) {
                    val songs = response.body()!!.data.map { item ->
                        mapToSong(item, serverUrl)
                    }
                    // 保存到本地数据库以获取 ID
                    songDao.insertSongs(songs)
                    _uiState.value = _uiState.value.copy(
                        songs = songs,
                        isLoading = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "加载失败: ${response.code()}"
                    )
                }
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
                val sortBy = _uiState.value.sortBy
                val sortOrder = if (_uiState.value.sortOrderAsc) "asc" else "desc"

                val response = api.getMusicList(
                    token = "Bearer ${token ?: ""}",
                    page = 1,
                    pageSize = 200,
                    sortBy = sortBy.apiField,
                    sortOrder = sortOrder
                )

                if (response.isSuccessful && response.body() != null) {
                    val songs = response.body()!!.data.map { item ->
                        mapToSong(item, serverUrl)
                    }
                    // 保存到本地数据库以获取 ID
                    songDao.insertSongs(songs)
                    _uiState.value = _uiState.value.copy(
                        songs = songs,
                        isRefreshing = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        error = "刷新失败: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = "网络错误: ${e.message}"
                )
            }
        }
    }

    fun setSortBy(sortBy: CloudSortBy) {
        if (_uiState.value.sortBy == sortBy) {
            // 切换升序/降序
            _uiState.value = _uiState.value.copy(sortOrderAsc = !_uiState.value.sortOrderAsc)
        } else {
            _uiState.value = _uiState.value.copy(sortBy = sortBy, sortOrderAsc = false)
        }
        loadSongs()
    }

    private fun mapToSong(
        item: com.inkwise.music.data.network.model.MusicItem,
        serverUrl: String
    ): Song {
        // 服务器根地址：去掉 /api/v1 前缀
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
