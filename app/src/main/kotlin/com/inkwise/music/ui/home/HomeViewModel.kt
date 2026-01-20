package com.inkwise.music.ui.home

import androidx.lifecycle.ViewModel
import com.inkwise.music.data.repository.MusicRepository

class HomeViewModel(
    private val repository: MusicRepository = MusicRepository(),
) : ViewModel() {
    // 主页的业务逻辑
}
