package com.inkwise.music.data.repository

import com.inkwise.music.data.model.Lyrics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LocalLyricsRepository : LyricsRepository {

    private val cache = mutableMapOf<Long, Lyrics>()

    override suspend fun loadLyrics(songId: Long): Lyrics? {
        cache[songId]?.let { return it }

        val lyrics = loadFromDisk(songId)
        if (lyrics != null) {
            cache[songId] = lyrics
        }
        return lyrics
    }

    override fun observeLyrics(songId: Long): Flow<Lyrics?> = flow {
        val lyrics = loadLyrics(songId)
        emit(lyrics)
    }

    private suspend fun loadFromDisk(songId: Long): Lyrics? {
        // TODO: 以后实现 lrc / krc 解析
        return null
    }
}