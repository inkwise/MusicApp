package com.inkwise.music.ui.main.navigationPage.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inkwise.music.data.network.ApiService
import com.inkwise.music.data.network.model.LoginRequest
import com.inkwise.music.data.network.model.RegisterRequest
import com.inkwise.music.data.prefs.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val username: String = "",
    val password: String = "",
    val email: String = "",
    val isLoggedIn: Boolean = false,
    val displayName: String = "",
    val isLoading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val api: ApiService,
    private val prefs: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val token = prefs.authToken.first()
            val username = prefs.username.first()
            if (!token.isNullOrEmpty() && !username.isNullOrEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = true,
                    displayName = username
                )
            }
        }
    }

    fun onUsernameChanged(value: String) {
        _uiState.value = _uiState.value.copy(username = value, message = null)
    }

    fun onPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(password = value, message = null)
    }

    fun onEmailChanged(value: String) {
        _uiState.value = _uiState.value.copy(email = value, message = null)
    }

    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.username.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(message = "用户名和密码不能为空", isError = true)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = api.login(LoginRequest(state.username, state.password))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    prefs.saveAuthData(body.token, body.user.username, body.user.email, body.user.id)
                    _uiState.value = _uiState.value.copy(
                        isLoggedIn = true,
                        displayName = body.user.username,
                        isLoading = false,
                        message = "登录成功",
                        isError = false
                    )
                    onSuccess()
                } else {
                    val errorMsg = try {
                        response.errorBody()?.string() ?: "登录失败"
                    } catch (_: Exception) {
                        "登录失败，状态码: ${response.code()}"
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = errorMsg,
                        isError = true
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "网络错误: ${e.message}",
                    isError = true
                )
            }
        }
    }

    fun register(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.username.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(message = "用户名和密码不能为空", isError = true)
            return
        }
        if (state.password.length < 6) {
            _uiState.value = state.copy(message = "密码至少6个字符", isError = true)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val email = state.email.takeIf { it.isNotBlank() }
                val response = api.register(RegisterRequest(state.username, state.password, email))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    prefs.saveAuthData(body.token, body.user.username, body.user.email, body.user.id)
                    _uiState.value = _uiState.value.copy(
                        isLoggedIn = true,
                        displayName = body.user.username,
                        isLoading = false,
                        message = "注册成功",
                        isError = false
                    )
                    onSuccess()
                } else {
                    val errorMsg = try {
                        response.errorBody()?.string() ?: "注册失败"
                    } catch (_: Exception) {
                        "注册失败，状态码: ${response.code()}"
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = errorMsg,
                        isError = true
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "网络错误: ${e.message}",
                    isError = true
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            prefs.clearAuthData()
            _uiState.value = AuthUiState()
        }
    }
}
