package com.inkwise.music.ui.main.navigationPage.cloud

import androidx.lifecycle.ViewModel
import com.inkwise.music.data.dao.PlaylistDao
import com.inkwise.music.data.dao.SongDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class CloudViewModel
@Inject
constructor(
    private val dao: PlaylistDao, // ✅ 变成成员属性
    private val songDao: SongDao,
) : ViewModel() {



}
