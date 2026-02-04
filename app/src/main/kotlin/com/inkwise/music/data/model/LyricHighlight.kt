package com.inkwise.music.data.model

data class LyricHighlight(
    val lineIndex: Int,
    val tokenIndex: Int? = null,
    val tokenProgress: Float? = null // 0f ~ 1f
)