package com.inkwise.music.ui.local


class LocalViewModel(private val repository: MusicRepository) : ViewModel() {
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs = _songs.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    // 加载缓存或执行扫描
    fun loadOrScanSongs() {
        viewModelScope.launch {
            val localData = repository.getCachedSongs()
            if (localData.isEmpty()) {
                scanSongs()
            } else {
                _songs.value = localData
            }
        }
    }

    // 执行媒体库扫描
    fun scanSongs() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // 假设你有一个 MediaScanner 工具类
            val scannedSongs = repository.scanLocalMedia() 
            _songs.value = scannedSongs
            _isRefreshing.value = false
        }
    }
}
