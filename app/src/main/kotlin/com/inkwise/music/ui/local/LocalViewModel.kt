package com.inkwise.music.ui.local
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inkwise.music.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.inkwise.music.data.repository.MusicRepository

class LocalViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _localSongs = MutableStateFlow<List<Song>>(emptyList())
    val localSongs: StateFlow<List<Song>> = _localSongs.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    init {
	    observeLocalSongs()
	}
	
	private fun observeLocalSongs() {
	    viewModelScope.launch {
	        musicRepository.getLocalSongs().collect { songs ->
	            _localSongs.value = songs
	        }
	    }
	}
    /** 扫描本地音乐并更新 _localSongs */
    fun scanSongs(context: Context) {
        if (_isScanning.value) return

        viewModelScope.launch(Dispatchers.IO) {
            _isScanning.value = true
            try {
                val songs = mutableListOf<Song>()
                val projection =
                    arrayOf(
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.ALBUM_ID,
                    )
                val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
                val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

                context.contentResolver
                    .query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        selection,
                        null,
                        sortOrder,
                    )?.use { cursor ->
                        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                        val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                        val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                        val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                        val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                        val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                        while (cursor.moveToNext()) {
                            val id = cursor.getLong(idCol)
                            val title = cursor.getString(titleCol) ?: "Unknown"
                            val artist = cursor.getString(artistCol) ?: "Unknown"
                            val duration = cursor.getLong(durationCol)
                            val path = cursor.getString(dataCol) ?: ""
                            val albumId = cursor.getLong(albumIdCol)
                            val albumArtUri =
                                ContentUris
                                    .withAppendedId(
                                        Uri.parse("content://media/external/audio/albumart"),
                                        albumId,
                                    ).toString()
                            val song =
                                Song(
                                    localId = id,
                                    title = title,
                                    artist = artist,
                                    duration = duration,
                                    path = path,
                                    uri =
                                        ContentUris
                                            .withAppendedId(
                                                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                                id,
                                            ).toString(),
                                    albumArt = albumArtUri, // ⭐ 保存封面
                                )
                            songs += song
                        }
                    }

                _localSongs.value = songs
                // TODO: 保存到 Room/Repository 持久化
                musicRepository.saveScannedSongs(songs)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isScanning.value = false
            }
        }
    }
}
