package com.inkwise.music.data.model

data class LyricLine(
    val timeMs: Long,
    val text: String,
    // 可选：逐字/逐词
    val tokens: List<LyricToken>? = null,
)
