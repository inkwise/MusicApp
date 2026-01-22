package com.inkwise.music
import android.app.Application
import com.inkwise.music.player.MusicPlayerManager

class MusicApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MusicPlayerManager.init(this)
    }
}