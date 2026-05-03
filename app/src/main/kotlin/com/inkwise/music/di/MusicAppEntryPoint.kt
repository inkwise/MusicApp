package com.inkwise.music.di

import com.inkwise.music.data.dao.SongDao
import com.inkwise.music.data.prefs.PreferencesManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MusicAppEntryPoint {
    val prefsManager: PreferencesManager
    val songDao: SongDao
}
