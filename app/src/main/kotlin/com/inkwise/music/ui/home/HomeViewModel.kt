package com.inkwise.music.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inkwise.music.data.dao.PlaylistDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.inkwise.music.data.model.PlaylistEntity

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dao: PlaylistDao   // ✅ 变成成员属性
) : ViewModel() {

    val playlists = dao.observePlaylists()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
        
    fun refreshPlaylists() {
    // TODO: 重新从数据库或者网络加载数据
	}
	
	fun createPlaylist(title: String) {
	    viewModelScope.launch {
	        // TODO: 调用 DAO 插入新歌单
	        val newPlaylist = PlaylistEntity(
	        	title = title,
	        	coverUri="测试图片",
	        	description="测试说明")
        	dao.insert(newPlaylist)
	    }
	}
}