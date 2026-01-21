package com.inkwise.music.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.inkwise.music.data.model.PlaybackState
import com.inkwise.music.data.model.Song
import com.inkwise.music.data.repository.MusicRepository
import com.inkwise.music.player.MusicPlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = MusicRepository(application)
    private val playerManager = MusicPlayerManager(application)
    
    val playbackState: StateFlow<PlaybackState> = playerManager.playbackState
    val playQueue: StateFlow<List<Song>> = playerManager.playQueue
    val currentIndex: StateFlow<Int> = playerManager.currentIndex
    
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
    
    // 加载本地歌曲
    fun loadLocalSongs() {
        viewModelScope.launch {
            repository.getLocalSongs().collect { songs ->
                _uiState.value = _uiState.value.copy(
                    localSongs = songs,
                    isLoading = false
                )
            }
        }
    }
    
    // 播放歌曲列表
    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        playerManager.setPlayQueue(songs, startIndex)
        playerManager.play()
    }
    
    // 播放/暂停
    fun playPause() {
        playerManager.playPause()
    }
    
    // 下一曲
    fun skipToNext() {
        playerManager.skipToNext()
    }
    
    // 上一曲
    fun skipToPrevious() {
        playerManager.skipToPrevious()
    }
    
    // 跳转到指定歌曲
    fun skipToIndex(index: Int) {
        playerManager.skipToIndex(index)
    }
    
    // 跳转进度
    fun seekTo(position: Long) {
        playerManager.seekTo(position)
    }
    
    // 切换随机播放
    fun toggleShuffle() {
        playerManager.toggleShuffle()
    }
    
    // 切换循环模式
    fun toggleRepeatMode() {
        playerManager.toggleRepeatMode()
    }
    
    // 添加到播放队列
    fun addToQueue(song: Song) {
        playerManager.addToQueue(song)
    }
    
    // 从队列移除
    fun removeFromQueue(index: Int) {
        playerManager.removeFromQueue(index)
    }
    
    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}

data class PlayerUiState(
    val localSongs: List<Song> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
