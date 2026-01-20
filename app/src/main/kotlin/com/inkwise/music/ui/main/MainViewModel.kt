package com.inkwise.music.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inkwise.music.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MainUiState(
    val bottomDrawerOpen: Boolean = false,
    val sidebarOpen: Boolean = false,
    val currentRoute: String = "home",
)

class MainViewModel(
    private val repository: MusicRepository = MusicRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun toggleBottomDrawer() {
        _uiState.value =
            _uiState.value.copy(
                bottomDrawerOpen = !_uiState.value.bottomDrawerOpen,
            )
    }

    fun closeBottomDrawer() {
        _uiState.value = _uiState.value.copy(bottomDrawerOpen = false)
    }

    fun toggleSidebar() {
        _uiState.value =
            _uiState.value.copy(
                sidebarOpen = !_uiState.value.sidebarOpen,
            )
    }

    fun closeSidebar() {
        _uiState.value = _uiState.value.copy(sidebarOpen = false)
    }

    fun navigateTo(route: String) {
        _uiState.value = _uiState.value.copy(currentRoute = route)
    }
}
