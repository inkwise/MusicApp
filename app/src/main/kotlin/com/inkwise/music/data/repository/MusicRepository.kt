package com.inkwise.music.data.repository

import com.inkwise.music.data.model.Song
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MusicRepository {
    // 获取本地歌曲
    fun getLocalSongs(): Flow<List<Song>> =
        flow {
            delay(500) // 模拟加载
            emit(
                listOf(
                    Song("1", "本地歌曲 1", "歌手 A", 180000, true),
                    Song("2", "本地歌曲 2", "歌手 B", 200000, true),
                    Song("3", "本地歌曲 3", "歌手 C", 210000, true),
                ),
            )
        }

    // 获取云端歌曲
    fun getCloudSongs(): Flow<List<Song>> =
        flow {
            delay(500) // 模拟网络请求
            emit(
                listOf(
                    Song("4", "云端歌曲 1", "歌手 D", 190000, false),
                    Song("5", "云端歌曲 2", "歌手 E", 220000, false),
                    Song("6", "云端歌曲 3", "歌手 F", 195000, false),
                ),
            )
        }
}
