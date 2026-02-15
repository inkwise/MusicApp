package com.inkwise.music.ui.main.navigationPage.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inkwise.music.data.dao.PlaylistDao
import com.inkwise.music.data.model.PlaylistEntity
import com.inkwise.music.data.model.PlaylistWithSongs
import com.inkwise.music.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel
@Inject
constructor(
    private val repository: PlaylistRepository,
    private val playlistdao: PlaylistDao, // ✅ 变成成员属性
) : ViewModel() {

    //歌单列表
    private val _playlists = MutableStateFlow<List<PlaylistWithSongs>>(emptyList())
    val playlists: StateFlow<List<PlaylistWithSongs>> = _playlists

    init {
        // 启动协程收集数据库变化
        viewModelScope.launch {
            repository.getAllPlaylistsWithSongs()
                .collect { list ->
                    _playlists.value = list
                }
        }
    }

    fun refreshPlaylists() {
        // TODO: 重新从数据库或者网络加载数据
    }

    fun createPlaylist(title: String) {
        viewModelScope.launch {
            // TODO: 调用 DAO 插入新歌单
            val newPlaylist =
                PlaylistEntity(
                    title = title,
                    coverUri = "测试图片",
                    description = "测试说明",
                )
            playlistdao.insert(newPlaylist)
        }
    }
}
