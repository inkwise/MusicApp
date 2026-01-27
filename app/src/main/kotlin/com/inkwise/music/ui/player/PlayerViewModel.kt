package com.inkwise.music.ui.player

import androidx.lifecycle.ViewModel
import android.app.Application
import androidx.lifecycle.viewModelScope
import com.inkwise.music.data.model.PlaybackState
import com.inkwise.music.data.model.Song
import com.inkwise.music.data.repository.MusicRepository
import com.inkwise.music.player.MusicPlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
	private val repository: MusicRepository
	) : ViewModel(application) {
    
    
    val playbackState: StateFlow<PlaybackState> = MusicPlayerManager.playbackState
    val playQueue: StateFlow<List<Song>> = MusicPlayerManager.playQueue
    val currentIndex: StateFlow<Int> = MusicPlayerManager.currentIndex
    
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
        MusicPlayerManager.setPlayQueue(songs, startIndex)
        MusicPlayerManager.play()
    }
    
    // 播放/暂停
    fun playPause() {
        MusicPlayerManager.playPause()
    }
    
    // 下一曲
    fun skipToNext() {
        MusicPlayerManager.skipToNext()
    }
    
    // 上一曲
    fun skipToPrevious() {
        MusicPlayerManager.skipToPrevious()
    }
    
    // 跳转到指定歌曲
    fun skipToIndex(index: Int) {
        MusicPlayerManager.skipToIndex(index)
    }
    
    // 跳转进度
    fun seekTo(position: Long) {
        MusicPlayerManager.seekTo(position)
    }
    
    // 切换随机播放
    fun toggleShuffle() {
        MusicPlayerManager.toggleShuffle()
    }
    
    // 切换循环模式
    fun toggleRepeatMode() {
        MusicPlayerManager.toggleRepeatMode()
    }
    
    // 添加到播放队列
    fun addToQueue(song: Song) {
        MusicPlayerManager.addToQueue(song)
    }
    
    // 从队列移除
    fun removeFromQueue(index: Int) {
        MusicPlayerManager.removeFromQueue(index)
    }
    
    override fun onCleared() {
        super.onCleared()
       // MusicPlayerManager.release()
    }
}

data class PlayerUiState(
    val localSongs: List<Song> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
