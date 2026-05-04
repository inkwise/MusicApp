package com.inkwise.music.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Clock
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import com.inkwise.music.MainActivity
import com.inkwise.music.data.model.Song
import com.inkwise.music.player.MusicPlayerManager
import java.io.File

class MusicService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    companion object {
        private const val CACHE_MAX_BYTES = 512L * 1024 * 1024 // 512 MB
        private var cache: SimpleCache? = null
        private var cacheDataSourceFactory: DataSource.Factory? = null
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        initCache()

        player = buildPlayer()

        MusicPlayerManager.exoPlayer = player
        MusicPlayerManager.applyAudioFocus()

        mediaSession =
            MediaSession
                .Builder(this, player)
                .setSessionActivity(createPendingIntent())
                .setCallback(MediaSessionCallback())
                .build()
    }

    private fun initCache() {
        if (cache == null) {
            val cacheDir = File(cacheDir, "exoplayer_cache")
            cache = SimpleCache(cacheDir, LeastRecentlyUsedCacheEvictor(CACHE_MAX_BYTES))
        }
        if (cacheDataSourceFactory == null) {
            val upstreamFactory = DefaultDataSource.Factory(this)
            cacheDataSourceFactory = CacheDataSource.Factory()
                .setCache(cache!!)
                .setUpstreamDataSourceFactory(upstreamFactory)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        }
    }

    private fun buildPlayer(): ExoPlayer {
        val wasCachingEnabled = MusicPlayerManager.appPrefs?.cacheEnabled ?: false

        return ExoPlayer
            .Builder(this)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(cacheDataSourceFactory!!)
            )
            .setLoadControl(
                androidx.media3.exoplayer.DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                        if (wasCachingEnabled) 15_000 else 50_000,
                        if (wasCachingEnabled) 30_000 else 120_000,
                        if (wasCachingEnabled) 2_500 else 5_000,
                        if (wasCachingEnabled) 5_000 else 15_000,
                    )
                    .setPrioritizeTimeOverSizeThresholds(true)
                    .build()
            )
            .setTrackSelector(DefaultTrackSelector(this))
            .build()
            .apply {
                // Mute ExoPlayer — BASS handles actual audio output with effects
                volume = 0f
                addListener(
                    object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            // handled in MusicPlayerManager
                        }

                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            // handled in MusicPlayerManager
                        }
                    },
                )
            }
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        MusicPlayerManager.exoPlayer = null
        super.onDestroy()
    }

    // MediaSession 回调
    private inner class MediaSessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): MediaSession.ConnectionResult {
            val sessionCommands =
                MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS
                    .buildUpon()
                    .add(SessionCommand("TOGGLE_SHUFFLE", Bundle.EMPTY))
                    .add(SessionCommand("TOGGLE_REPEAT", Bundle.EMPTY))
                    .build()

            return MediaSession.ConnectionResult
                .AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle,
        ): com.google.common.util.concurrent.ListenableFuture<androidx.media3.session.SessionResult> {
            when (customCommand.customAction) {
                "TOGGLE_SHUFFLE" -> {
                    player.shuffleModeEnabled = !player.shuffleModeEnabled
                }

                "TOGGLE_REPEAT" -> {
                    player.repeatMode =
                        when (player.repeatMode) {
                            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                            else -> Player.REPEAT_MODE_OFF
                        }
                }
            }
            return com.google.common.util.concurrent.Futures.immediateFuture(
                androidx.media3.session.SessionResult(androidx.media3.session.SessionResult.RESULT_SUCCESS),
            )
        }
    }
}
