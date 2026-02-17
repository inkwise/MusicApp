package com.inkwise.music.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.inkwise.music.data.model.PlaybackState
import com.inkwise.music.data.model.RepeatMode
import com.inkwise.music.data.model.Song
import com.inkwise.music.service.MusicService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object MusicPlayerManager {
    // 进度条协程
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var progressJob: Job? = null
    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _playQueue = MutableStateFlow<List<Song>>(emptyList())
    val playQueue: StateFlow<List<Song>> = _playQueue.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private lateinit var appContext: Context

    fun init(context: Context) {
        if (!::appContext.isInitialized) {
            appContext = context.applicationContext
            initializeController()
        }
    }

    private fun initializeController() {
        val sessionToken =
            SessionToken(
                appContext,
                ComponentName(appContext, MusicService::class.java),
            )

        controllerFuture = MediaController.Builder(appContext, sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            mediaController?.addListener(PlayerListener())
        }, MoreExecutors.directExecutor())
    }

    // 设置播放队列
    fun setPlayQueue(
        songs: List<Song>,
        startIndex: Int = 0,
    ) {
        _playQueue.value = songs
        _currentIndex.value = startIndex

        val mediaItems =
            songs.map { song ->
                MediaItem
                    .Builder()
                    .setMediaId(song.id.toString())
                    .setUri(song.uri)
                    .setMediaMetadata(
                        MediaMetadata
                            .Builder()
                            .setTitle(song.title)
                            .setArtist(song.artist)
                            .setArtworkUri(song.albumArt?.let { Uri.parse(it) })
                            .build(),
                    ).build()
            }

        mediaController?.apply {
            setMediaItems(mediaItems, startIndex, 0)
            prepare()
        }
    }

    // 播放/暂停
    fun playPause() {
        mediaController?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }

    // 播放
    fun play() {
        mediaController?.play()
    }

    // 暂停
    fun pause() {
        mediaController?.pause()
    }

    // 下一曲
    fun skipToNext() {
        mediaController?.seekToNext()
    }

    // 上一曲
    fun skipToPrevious() {
        mediaController?.seekToPrevious()
    }

    // 跳转到指定歌曲
    fun skipToIndex(index: Int) {
        mediaController?.seekToDefaultPosition(index)
    }

    // 跳转到指定位置
    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    // 切换随机播放
    /*
    fun toggleShuffle() {
        mediaController?.let {
            it.shuffleModeEnabled = !it.shuffleModeEnabled
            /*_playbackState.value =
                _playbackState.value.copy(
                    shuffleMode = it.shuffleModeEnabled,
                )*/
        }
    }*/

    // 切换循环模式
    fun toggleRepeatMode() {
        mediaController?.let { controller ->
            val newMode =
                when (controller.repeatMode) {
                    Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                    Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                    else -> Player.REPEAT_MODE_OFF
                }
            controller.repeatMode = newMode

            _playbackState.value =
                _playbackState.value.copy(
                    repeatMode =
                        when (newMode) {
                            Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                            Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                            else -> RepeatMode.OFF
                        },
                )
        }
    }

    // 添加歌曲到队列
    fun addToQueue(song: Song) {
        val currentQueue = _playQueue.value.toMutableList()
        currentQueue.add(song)
        _playQueue.value = currentQueue

        val mediaItem =
            MediaItem
                .Builder()
                .setMediaId(song.id.toString())
                .setUri(song.uri)
                .setMediaMetadata(
                    MediaMetadata
                        .Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .build(),
                ).build()

        mediaController?.addMediaItem(mediaItem)
    }

    // 从队列移除歌曲
    fun removeFromQueue(index: Int) {
        val currentQueue = _playQueue.value.toMutableList()
        if (index in currentQueue.indices) {
            currentQueue.removeAt(index)
            _playQueue.value = currentQueue
            mediaController?.removeMediaItem(index)
        }
    }

    // 播放器监听器
    private class PlayerListener : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                startProgressUpdates()
            } else {
                stopProgressUpdates()
            }
            updatePlaybackState()
        }

        override fun onMediaItemTransition(
            mediaItem: MediaItem?,
            reason: Int,
        ) {
            mediaController?.currentMediaItemIndex?.let { index ->
                _currentIndex.value = index
                updatePlaybackState()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            updatePlaybackState()
        }
    }

    private fun updatePlaybackState() {
        mediaController?.let { controller ->
            val currentSong = _playQueue.value.getOrNull(controller.currentMediaItemIndex)

            _playbackState.value =
                PlaybackState(
                    isPlaying = controller.isPlaying,
                    currentSong = currentSong,
                    currentPosition = controller.currentPosition,
                    duration = controller.duration.coerceAtLeast(0),
                    playbackSpeed = controller.playbackParameters.speed,
                    repeatMode =
                        when (controller.repeatMode) {
                            Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                            Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                            else -> RepeatMode.OFF
                        },
                    shuffleMode = controller.shuffleModeEnabled,
                )
        }
    }

    private fun startProgressUpdates() {
        if (progressJob != null) return

        progressJob =
            scope.launch {
                while (isActive) {
                    updatePlaybackState()
                    delay(1000) // 200~500ms 都可以
                }
            }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stopProgressUpdates()
        scope.cancel()
        mediaController?.release()
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
    }
}
