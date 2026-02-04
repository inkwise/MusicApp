package com.inkwise.music.data.model

data class Lyrics(
    val songId: Long,
    val lines: List<LyricLine>,
    val language: String,
    val source: LyricsSource,
    val version: Int
)