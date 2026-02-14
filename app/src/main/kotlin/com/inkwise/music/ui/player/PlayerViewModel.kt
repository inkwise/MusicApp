package com.inkwise.music.ui.player

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inkwise.music.data.lyrics.LyricsSynchronizer
import com.inkwise.music.data.model.LyricHighlight
import com.inkwise.music.data.model.LyricLine
import com.inkwise.music.data.model.Lyrics
import com.inkwise.music.data.model.LyricsSource
import com.inkwise.music.data.model.LyricsUiState
import com.inkwise.music.data.model.PlaybackState
import com.inkwise.music.data.model.Song
import com.inkwise.music.data.repository.LyricsRepository
import com.inkwise.music.data.repository.MusicRepository
import com.inkwise.music.player.MusicPlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel
    @Inject
    constructor(
        private val repository: MusicRepository,
        private val lyricsRepository: LyricsRepository,
    ) : ViewModel() {
        val playbackState: StateFlow<PlaybackState> = MusicPlayerManager.playbackState
        val playQueue: StateFlow<List<Song>> = MusicPlayerManager.playQueue
        val currentIndex: StateFlow<Int> = MusicPlayerManager.currentIndex

        private val _uiState = MutableStateFlow(PlayerUiState())
        val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
		
        // 歌词

        private val _lyricsState = MutableStateFlow(LyricsUiState())
        val lyricsState: StateFlow<LyricsUiState> = _lyricsState.asStateFlow()
        private var synchronizer: LyricsSynchronizer? = null

        // 当前歌曲对象
        val currentSong: StateFlow<Song?> =
            combine(playQueue, currentIndex) { queue, index ->
                queue.getOrNull(index)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null,
            )

        init {
            observeCurrentSong()
            observePlayback()
        }

        private fun observeCurrentSong() {
            viewModelScope.launch {
                currentSong.collect { song ->
                    if (song == null) {
                        _lyricsState.value = LyricsUiState()
                        synchronizer = null
                        return@collect
                    }

                    // 加载歌词
                    val lyrics = lyricsRepository.loadLyrics(song.id)
                    synchronizer = lyrics?.let { LyricsSynchronizer(it) }
                    _lyricsState.value = _lyricsState.value.copy(lyrics = lyrics, highlight = null)
                }
            }
        }

        private fun observePlayback() {
            viewModelScope.launch {
                playbackState.collect { state ->
                    val sync = synchronizer ?: return@collect
                    val highlight = sync.findHighlight(state.currentPosition) // ✅ Long 类型
                    _lyricsState.value = _lyricsState.value.copy(highlight = highlight)
                }
            }
        }

        // 加载本地歌曲
        fun loadLocalSongs() {
            viewModelScope.launch {
                repository.getLocalSongs().collect { songs ->
                    _uiState.value =
                        _uiState.value.copy(
                            localSongs = songs,
                            isLoading = false,
                        )
                }
            }
        }

        // 播放歌曲列表
        fun playSongs(
            songs: List<Song>,
            startIndex: Int = 0,
        ) {
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
    val error: String? = null,
)
