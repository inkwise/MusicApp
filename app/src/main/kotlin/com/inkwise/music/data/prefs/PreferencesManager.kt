package com.inkwise.music.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class SavedPlaybackState(
    val queueIds: List<Long> = emptyList(),
    val currentIndex: Int = 0,
    val lastPosition: Long = 0L
)

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_SERVER_URL = stringPreferencesKey("server_url")
        private val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_USERNAME = stringPreferencesKey("username")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_USER_ID = stringPreferencesKey("user_id")

        private val KEY_QUEUE_IDS = stringPreferencesKey("queue_ids")
        private val KEY_QUEUE_INDEX = intPreferencesKey("queue_index")
        private val KEY_LAST_POSITION = longPreferencesKey("last_position")

        const val DEFAULT_SERVER_URL = "http://10.0.2.2:8080/api/v1"
    }

    val serverUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SERVER_URL] ?: DEFAULT_SERVER_URL
    }

    val authToken: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTH_TOKEN]
    }

    val username: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USERNAME]
    }

    val email: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_EMAIL]
    }

    val userId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_ID]
    }

    val isLoggedIn: Flow<Boolean> = authToken.map { !it.isNullOrEmpty() }

    // ── 登录需求事件 ──
    private val _loginRequiredEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val loginRequiredEvents: SharedFlow<Unit> = _loginRequiredEvents.asSharedFlow()

    fun requireLogin() {
        _loginRequiredEvents.tryEmit(Unit)
    }

    suspend fun isLoggedInNow(): Boolean = isLoggedIn.first()

    suspend fun setServerUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SERVER_URL] = url.trimEnd('/')
        }
    }

    suspend fun saveAuthData(token: String, username: String, email: String?, userId: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTH_TOKEN] = token
            prefs[KEY_USERNAME] = username
            if (email != null) prefs[KEY_EMAIL] = email
            prefs[KEY_USER_ID] = userId.toString()
        }
    }

    suspend fun clearAuthData() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_AUTH_TOKEN)
            prefs.remove(KEY_USERNAME)
            prefs.remove(KEY_EMAIL)
            prefs.remove(KEY_USER_ID)
        }
    }

    // ── 播放状态持久化 ──

    val savedPlaybackState: Flow<SavedPlaybackState> = context.dataStore.data.map { prefs ->
        val idsStr = prefs[KEY_QUEUE_IDS] ?: ""
        val ids = if (idsStr.isBlank()) emptyList() else idsStr.split(",").mapNotNull { it.toLongOrNull() }
        SavedPlaybackState(
            queueIds = ids,
            currentIndex = prefs[KEY_QUEUE_INDEX] ?: 0,
            lastPosition = prefs[KEY_LAST_POSITION] ?: 0L
        )
    }

    suspend fun savePlaybackState(state: SavedPlaybackState) {
        context.dataStore.edit { prefs ->
            if (state.queueIds.isEmpty()) {
                prefs.remove(KEY_QUEUE_IDS)
                prefs.remove(KEY_QUEUE_INDEX)
                prefs.remove(KEY_LAST_POSITION)
            } else {
                prefs[KEY_QUEUE_IDS] = state.queueIds.joinToString(",")
                prefs[KEY_QUEUE_INDEX] = state.currentIndex
                prefs[KEY_LAST_POSITION] = state.lastPosition
            }
        }
    }
}
