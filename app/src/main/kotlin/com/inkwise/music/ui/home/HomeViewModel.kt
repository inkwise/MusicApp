package com.inkwise.music.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.inkwise.music.data.repository.MusicRepository

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = MusicRepository(application)

    // 主页的业务逻辑
}