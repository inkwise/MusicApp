package com.inkwise.music.data.repository

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.inkwise.music.data.model.LyricLine
import com.inkwise.music.data.model.Lyrics
import com.inkwise.music.data.model.LyricsSource
import com.inkwise.music.data.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File
import javax.inject.Inject

class LocalLyricsRepository
@Inject
constructor(
    private val musicRepository: MusicRepository,
    @ApplicationContext private val context: Context,
) : LyricsRepository {
    private val cache = mutableMapOf<Long, Lyrics>()

    // 翻译分隔符：Unicode THIN SPACE (U+2009)
    private companion object {
        const val THIN_SPACE: Char = '\u2009'
    }

    override suspend fun loadLyrics(songId: Long): Lyrics? {
        cache[songId]?.let { return it }

        val song = musicRepository.getSongById(songId) ?: return null

        // 1️⃣ 内嵌歌词（逐行，可含翻译）
        loadEmbeddedLyrics(song)?.let {
            cache[songId] = it
            return it
        }

        // 2️⃣ LRC（以后加）
        // loadLrcFromDisk(song)

        return null
    }


    override fun observeLyrics(songId: Long): Flow<Lyrics?> =
        flow {
            emit(loadLyrics(songId))
        }

    private fun loadEmbeddedLyrics(song: Song): Lyrics? {
        return try {
            val audioFile = AudioFileIO.read(File(song.path))
            val tag = audioFile.tag ?: return null

            val lyricText = tag.getFirst(FieldKey.LYRICS)
            if (lyricText.isBlank()) return null

            val timeLineRegex = Regex("""\[(\d{2}):(\d{2})\.(\d{2})]""")

            val lines =
                lyricText
                    .lines()
                    .mapNotNull { rawLine ->
                        val match = timeLineRegex.find(rawLine) ?: return@mapNotNull null

                        val (mm, ss, xx) = match.destructured
                        val timeMs =
                            mm.toLong() * 60_000 +
                                ss.toLong() * 1_000 +
                                xx.toLong() * 10

                        // 去掉时间戳
                        val content =
                            rawLine
                                .replace(timeLineRegex, "")
                                .trim()

                        if (content.isBlank()) return@mapNotNull null

                        // === 核心改动：按 \u2009 拆分 原文 / 翻译 ===
                        val parts = content.split(THIN_SPACE)

                        val originalText =
                            parts
                                .first()
                                .trim()

                        val translationText =
                            parts
                                .drop(1)
                                .joinToString(THIN_SPACE.toString())
                                .trim()
                                .takeIf { it.isNotBlank() }

                        LyricLine(
                            timeMs = timeMs,
                            text = originalText,
                            tokens = null,
                            translation = translationText,
                        )
                    }.sortedBy { it.timeMs }

            if (lines.isEmpty()) return null

            Lyrics(
                songId = song.id,
                lines = lines,
                language = "unknown",
                source = LyricsSource.EMBEDDED,
                version = 1,
            )
        } catch (e: Exception) {
            null
        }
    }

    // 测试用
    private fun toast(msg: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
