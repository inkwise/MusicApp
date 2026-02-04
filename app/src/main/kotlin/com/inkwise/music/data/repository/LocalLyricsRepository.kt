package com.inkwise.music.data.repository

import com.inkwise.music.data.model.Lyrics

import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext
import com.inkwise.music.data.model.*
import com.inkwise.music.data.repository.MusicRepository
import kotlinx.coroutines.flow.Flow

import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File
import android.content.Context

class LocalLyricsRepository @Inject constructor(
    private val musicRepository: MusicRepository,
    @ApplicationContext private val context: Context
) : LyricsRepository {

    private val cache = mutableMapOf<Long, Lyrics>()

    override suspend fun loadLyrics(songId: Long): Lyrics? {
    	toast("loadLyrics()")
		
        cache[songId]?.let { return it }
		toast("读取仓库")
		
        val song = musicRepository.getSongById(songId) ?: return null
		toast("正在读取内嵌歌词")
		
        // 1️⃣ 内嵌歌词（逐行）
        loadEmbeddedLyrics(song)?.let {
            cache[songId] = it
            return it
        }

        // 2️⃣ LRC（以后加）
        // loadLrcFromDisk(song)

        return null
    }

    override fun observeLyrics(songId: Long): Flow<Lyrics?> = flow {
        emit(loadLyrics(songId))
    }

    private fun loadEmbeddedLyrics(song: Song): Lyrics? {
        return try {
            val audioFile = AudioFileIO.read(File(song.path))
            val tag = audioFile.tag ?: return null

            val lyricText = tag.getFirst(FieldKey.LYRICS)
            if (lyricText.isBlank()) return null

            val lines = lyricText
                .lines()
                .filter { it.isNotBlank() }
                .mapIndexed { index, line ->
                    LyricLine(
                        timeMs = index * 5_000L, // ⚠️ 无时间戳 → 只能逐行
                        text = line,
                        tokens = null
                    )
                }

            Lyrics(
                songId = song.id,
                lines = lines,
                language = "unknown",
                source = LyricsSource.EMBEDDED,
                version = 1
            )
        } catch (e: Exception) {
            null
        }
    }
    
    //测试用
    private fun toast(msg: String) {
	    Handler(Looper.getMainLooper()).post {
	        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
	    }
	}
}