package com.inkwise.music.data.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val duration: Long,
    val isLocal: Boolean,
)
