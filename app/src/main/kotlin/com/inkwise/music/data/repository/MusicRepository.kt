package com.inkwise.music.data.repository

import android.content.Context
import android.net.Uri
import com.inkwise.music.data.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MusicRepository(private val context: Context) {
    
    // 获取本地歌曲列表
    fun getLocalSongs(): Flow<List<Song>> = flow {
        // 这里应该扫描本地音乐文件
        // 示例数据
        emit(
            listOf(
                Song(
                    id = "1",
                    title = "本地歌曲 1",
                    artist = "歌手 A",
                    duration = 180000,
                    uri = Uri.parse("/storage/emulated/0/Documents/音乐/Whatya Want From Me - Dunn.mp3"),
                    isLocal = true
                ),
                Song(
                    id = "2",
                    title = "本地歌曲 2",
                    artist = "歌手 B",
                    duration = 200000,
                    uri = Uri.parse("/storage/emulated/0/Documents/音乐/9420 - 麦小兜.flac"),
                    isLocal = true
                )
            )
        )
    }
    
    // 获取云端歌曲
    fun getCloudSongs(): Flow<List<Song>> = flow {
        emit(
            listOf(
                Song(
                    id = "3",
                    title = "云端歌曲 1",
                    artist = "歌手 C",
                    duration = 190000,
                    uri = Uri.parse("/storage/emulated/0/Documents/音乐/爱您不需要理由 - 李克勤.flac"),
                    isLocal = false
                )
            )
        )
    }
}