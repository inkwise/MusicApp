package com.inkwise.music.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.un4seen.bass.BASS
import com.inkwise.music.data.audio.AudioEffectManager
import com.inkwise.music.data.model.PlayMode
import com.inkwise.music.data.model.PlaybackState
import com.inkwise.music.data.model.SleepMode
import com.inkwise.music.data.model.Song
import com.inkwise.music.data.prefs.PreferencesManager
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
    var appPrefs: PreferencesManager? = null
        private set
    var audioEffectManager: AudioEffectManager? = null
        private set

    var exoPlayer: ExoPlayer? = null

    // 睡眠定时
    private var sleepJob: Job? = null
    private var sleepMode: SleepMode = SleepMode.STOP_IMMEDIATELY
    private var exitAppCallback: (() -> Unit)? = null

    // 定时器剩余时间
    private val _sleepRemaining = MutableStateFlow<Long?>(null)
    val sleepRemaining: StateFlow<Long?> = _sleepRemaining

    // 待恢复的进度（controller 就绪后执行 seek）
    private var pendingSeekPosition: Long = -1L

    fun init(context: Context, prefs: PreferencesManager? = null, effectManager: AudioEffectManager? = null) {
        if (!::appContext.isInitialized) {
            appContext = context.applicationContext
            if (prefs != null) appPrefs = prefs
            if (effectManager != null) {
                audioEffectManager = effectManager
                // Reload track when tempo mode changes (normal ↔ tempo stream)
                effectManager.onTempoModeChanged = { loadCurrentTrackIntoBass() }
            }
            BassEngine.init(appContext)
            BassEngine.setOnEndCallback { onBassTrackEnded() }
            initializeController()
        }
    }

    fun restorePlaybackState(
        songs: List<Song>,
        index: Int,
        position: Long,
    ) {
        if (songs.isEmpty()) return
        val safeIndex = index.coerceIn(0, songs.lastIndex)
        setPlayQueue(songs, safeIndex)
        if (position > 0) {
            pendingSeekPosition = position
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
            // 如果恢复时有队列但 controller 还没收到，现在推送
            val queue = _playQueue.value
            if (queue.isNotEmpty() && mediaController?.mediaItemCount == 0) {
                val mediaItems = queue.map { song ->
                    MediaItem.Builder()
                        .setMediaId(song.id.toString())
                        .setUri(song.uri)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(song.title)
                                .setArtist(song.artist)
                                .setArtworkUri(song.albumArt?.let { Uri.parse(it) })
                                .build()
                        ).build()
                }
                val startPos = if (pendingSeekPosition > 0) pendingSeekPosition else 0L
                mediaController?.setMediaItems(mediaItems, _currentIndex.value, startPos)
                mediaController?.prepare()
                pendingSeekPosition = -1L
            }
        }, MoreExecutors.directExecutor())
    }

    fun startSleepTimer(
        durationMillis: Long,
        mode: SleepMode,
        onExitApp: () -> Unit,
    ) {
        cancelSleepTimer()

        sleepMode = mode
        exitAppCallback = onExitApp

        sleepJob =
            scope.launch {
                var remaining = durationMillis
                _sleepRemaining.value = remaining

                while (remaining > 0) {
                    delay(1000)
                    remaining -= 1000
                    _sleepRemaining.value = remaining
                }

                _sleepRemaining.value = null

                when (sleepMode) {
                    SleepMode.STOP_IMMEDIATELY -> stopAndExit()
                    SleepMode.STOP_AFTER_SONG -> waitForSongFinishThenExit()
                }
            }
    }

    private suspend fun waitForSongFinishThenExit() {
        val controller = mediaController ?: return

        while (true) {
            val remaining = controller.duration - controller.currentPosition
            if (remaining <= 1000) break
            delay(1000)
        }

        stopAndExit()
    }

    private fun stopAndExit() {
        mediaController?.stop()
        BassEngine.stop()
        exitAppCallback?.invoke()
    }

    fun cancelSleepTimer() {
        sleepJob?.cancel()
        sleepJob = null
        _sleepRemaining.value = null
    }

    // 设置播放队列
    fun setPlayQueue(
        songs: List<Song>,
        startIndex: Int = 0,
    ) {
        _playQueue.value = songs
        _currentIndex.value = startIndex

        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
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

        loadCurrentTrackIntoBass()
    }

    private fun loadCurrentTrackIntoBass() {
        val song = _playQueue.value.getOrNull(_currentIndex.value) ?: return
        val fx = audioEffectManager
        audioEffectManager?.onChannelFreeing()

        // Apply config-level settings BEFORE stream creation
        fx?.dsdGain?.let { BassEngine.setDSDGain(it) }

        // Only use tempo stream when DSP speed or anti-alias filter is enabled
        // (tempo streams add overhead and can cause playback issues)
        val floatEnabled = fx?.isFloatDecodeEnabled ?: false
        val tempoNeeded = fx?.isTempoNeeded ?: false
        val flags = if (floatEnabled) BASS.BASS_SAMPLE_FLOAT else 0
        val ok = BassEngine.load(song.uri, flags, useTempo = tempoNeeded)
        if (ok) {
            fx?.onChannelReady()
            if (pendingSeekPosition > 0) {
                BassEngine.seekTo(pendingSeekPosition)
                pendingSeekPosition = -1L
            }
            // Resume BASS playback if ExoPlayer is already playing (skip next/prev)
            if (mediaController?.isPlaying == true) {
                BassEngine.play()
            }
        }
    }

    private fun onBassTrackEnded() {
        scope.launch {
            skipToNextInternal()
        }
    }

    private fun skipToNextInternal() {
        mediaController?.let { controller ->
            val currentIndex = controller.currentMediaItemIndex
            val count = controller.mediaItemCount
            if (count <= 1) {
                controller.seekTo(0)
            } else if (currentIndex < count - 1) {
                controller.seekToDefaultPosition(currentIndex + 1)
            } else {
                controller.seekToDefaultPosition(0)
            }
        }
    }

    // 播放/暂停（带淡入淡出）
    fun playPause() {
        mediaController?.let { controller ->
            if (controller.isPlaying) {
                if (appPrefs?.fadeEnabled == true) {
                    scope.launch {
                        fadeVolume(controller, controller.volume, 0f, 150L)
                        BassEngine.pause()
                        controller.pause()
                        controller.setVolume(1f)
                    }
                } else {
                    BassEngine.pause()
                    controller.pause()
                }
            } else {
                if (appPrefs?.fadeEnabled == true) {
                    controller.setVolume(0f)
                    controller.play()
                    BassEngine.play()
                    scope.launch {
                        fadeVolume(controller, 0f, 1f, 200L)
                    }
                } else {
                    controller.play()
                    BassEngine.play()
                }
            }
            applyAudioFocus()
        }
    }

    // 播放
    fun play() {
        mediaController?.let { controller ->
            if (appPrefs?.fadeEnabled == true) {
                controller.setVolume(0f)
                controller.play()
                BassEngine.play()
                scope.launch {
                    fadeVolume(controller, 0f, 1f, 200L)
                }
            } else {
                controller.play()
                BassEngine.play()
            }
            applyAudioFocus()
        }
    }

    // 暂停
    fun pause() {
        mediaController?.let { controller ->
            if (appPrefs?.fadeEnabled == true) {
                scope.launch {
                    fadeVolume(controller, controller.volume, 0f, 150L)
                    BassEngine.pause()
                    controller.pause()
                    controller.setVolume(1f)
                }
            } else {
                BassEngine.pause()
                controller.pause()
            }
        }
    }

    private suspend fun fadeVolume(controller: MediaController, from: Float, to: Float, durationMs: Long) {
        val steps = 10
        val stepTime = durationMs / steps
        for (i in 1..steps) {
            val volume = from + (to - from) * (i.toFloat() / steps)
            controller.setVolume(volume)
            delay(stepTime)
        }
    }

    fun applyAudioFocus() {
        val handleFocus = appPrefs?.audioFocusEnabled ?: true
        exoPlayer?.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            handleFocus,
        )
    }

    // 下一曲
    fun skipToNext() {
        skipToNextInternal()
    }

    // 上一曲
    fun skipToPrevious() {
        mediaController?.let { controller ->
            val currentIndex = controller.currentMediaItemIndex
            val count = controller.mediaItemCount
            if (count <= 1) {
                controller.seekTo(0)
            } else if (currentIndex > 0) {
                controller.seekToDefaultPosition(currentIndex - 1)
            } else {
                controller.seekToDefaultPosition(count - 1)
            }
        }
    }

    // 跳转到指定歌曲
    fun skipToIndex(index: Int) {
        mediaController?.seekToDefaultPosition(index)
    }

    // 跳转到指定位置
    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
        BassEngine.seekTo(position)
    }

    // 切换随机播放
    fun togglePlayMode() {
        mediaController?.let { controller ->
            val newMode =
                when (_playbackState.value.playMode) {
                    PlayMode.LIST -> PlayMode.SINGLE
                    PlayMode.SINGLE -> PlayMode.SHUFFLE
                    PlayMode.SHUFFLE -> PlayMode.LIST
                }

            when (newMode) {
                PlayMode.LIST -> {
                    controller.repeatMode = Player.REPEAT_MODE_ALL
                    controller.shuffleModeEnabled = false
                }

                PlayMode.SINGLE -> {
                    controller.repeatMode = Player.REPEAT_MODE_ONE
                    controller.shuffleModeEnabled = false
                }

                PlayMode.SHUFFLE -> {
                    controller.repeatMode = Player.REPEAT_MODE_ALL
                    controller.shuffleModeEnabled = true
                }
            }

            _playbackState.value = _playbackState.value.copy(playMode = newMode)
        }
    }

    fun setPlayQueueShuffle(songs: List<Song>) {
        if (songs.isEmpty()) return

        _playQueue.value = songs

        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setArtworkUri(song.albumArt?.let { Uri.parse(it) })
                        .build(),
                ).build()
        }

        val randomIndex = (songs.indices).random()
        _currentIndex.value = randomIndex

        mediaController?.apply {
            setMediaItems(mediaItems, randomIndex, 0)
            prepare()
            shuffleModeEnabled = true
            repeatMode = Player.REPEAT_MODE_ALL
            _playbackState.value = _playbackState.value.copy(playMode = PlayMode.SHUFFLE)
            play()
        }

        loadCurrentTrackIntoBass()
    }

    fun addToQueue(song: Song) {
        val controller = mediaController ?: return

        val currentIndex = controller.currentMediaItemIndex
        val insertIndex = currentIndex + 1

        val currentQueue = _playQueue.value.toMutableList()
        if (insertIndex <= currentQueue.size) {
            currentQueue.add(insertIndex, song)
        } else {
            currentQueue.add(song)
        }
        _playQueue.value = currentQueue

        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(song.uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .setArtworkUri(song.albumArt?.let { Uri.parse(it) })
                    .build(),
            ).build()

        controller.addMediaItem(insertIndex, mediaItem)
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
                if (BassEngine.getChannelHandle() != 0) {
                    BassEngine.play()
                }
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
            loadCurrentTrackIntoBass()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            updatePlaybackState()
        }
    }

    private fun updatePlaybackState() {
        mediaController?.let { controller ->
            val currentSong = _playQueue.value.getOrNull(controller.currentMediaItemIndex)

            val playMode =
                when {
                    controller.shuffleModeEnabled -> PlayMode.SHUFFLE
                    controller.repeatMode == Player.REPEAT_MODE_ONE -> PlayMode.SINGLE
                    else -> PlayMode.LIST
                }

            // Use BASS position when available (reflects actual audio output)
            val pos = if (BassEngine.getChannelHandle() != 0) {
                BassEngine.getPosition()
            } else {
                controller.currentPosition
            }

            _playbackState.value = PlaybackState(
                isPlaying = controller.isPlaying,
                currentSong = currentSong,
                currentPosition = pos,
                duration = controller.duration.coerceAtLeast(0),
                bufferedPosition = controller.bufferedPosition.coerceAtLeast(0),
                playbackSpeed = controller.playbackParameters.speed,
                playMode = playMode,
            )
        }
    }

    private fun startProgressUpdates() {
        if (progressJob != null) return
        progressJob = scope.launch {
            while (isActive) {
                updatePlaybackState()
                delay(200)
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
        BassEngine.release()
    }
}
