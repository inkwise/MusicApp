package com.inkwise.music.ui.main.navigationPage.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inkwise.music.data.prefs.PreferencesManager
import com.inkwise.music.data.prefs.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UISettingsViewModel @Inject constructor(
    private val prefs: PreferencesManager
) : ViewModel() {

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.themeMode.collect { mode ->
                _themeMode.value = mode
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            prefs.setThemeMode(mode)
        }
    }
}
