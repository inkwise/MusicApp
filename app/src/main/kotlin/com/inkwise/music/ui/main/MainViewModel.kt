package com.inkwise.music.ui.main

import androidx.lifecycle.ViewModel
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.inkwise.music.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

data class MainUiState(
    val bottomDrawerOpen: Boolean = false,
    val sidebarOpen: Boolean = false,
    val currentRoute: String = "home",
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MusicRepository
) : ViewModel() {

    //private val repository = MusicRepository(application)
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
