package com.inkwise.music.data.model

data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentSong: Song? = null,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val playbackSpeed: Float = 1f,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleMode: Boolean = false,
)

enum class RepeatMode {
    OFF, // 不循环
    ONE, // 单曲循环
    ALL, // 列表循环
}
