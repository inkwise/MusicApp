package com.inkwise.music.data.repository

import com.inkwise.music.data.model.Lyrics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LocalLyricsRepository @Inject constructor(): LyricsRepository {

    private val cache = mutableMapOf<Long, Lyrics>()

    override suspend fun loadLyrics(songId: Long): Lyrics? {
	    val song = songRepository.getSong(songId) ?: return null
	
	    // 1️⃣ 优先 LRC（可同步）
	    loadLrcFromDisk(song)?.let { return it }
	
	    // 2️⃣ 内嵌歌词（不可同步）
	    loadEmbeddedLyrics(song)?.let { return it }
	
	    // 3️⃣ 在线歌词（以后扩展）
	    // loadFromNetwork(song)
	
	    return null
	}

    override fun observeLyrics(songId: Long): Flow<Lyrics?> = flow {
        val lyrics = loadLyrics(songId)
        emit(lyrics)
    }

    private suspend fun loadFromDisk(songId: Long): Lyrics? {
        // TODO: 以后实现 lrc / krc 解析
        return null
    }
    private fun loadEmbeddedLyrics(song: Song): Lyrics? {
	    return try {
	        val audioFile = AudioFileIO.read(File(song.path))
	        val tag = audioFile.tag ?: return null
	
	        val lyricText = tag.getFirst(FieldKey.LYRICS)
	        if (lyricText.isBlank()) return null
	
	        Lyrics(
	            lines = lyricText
	                .lines()
	                .filter { it.isNotBlank() }
	                .mapIndexed { index, line ->
	                    LyricLine(
	                        timeMs = index * 5_000L, // ❗ 无时间戳，只能伪造
	                        text = line
	                    )
	                }
	        )
	    } catch (e: Exception) {
	        null
	    }
	}
}