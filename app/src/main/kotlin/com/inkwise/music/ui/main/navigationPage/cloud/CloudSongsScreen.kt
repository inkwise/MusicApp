package com.inkwise.music.ui.main.navigationPage.cloud

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inkwise.music.R
import com.inkwise.music.data.model.Song
import com.inkwise.music.ui.main.navigationPage.components.MultiSelectBottomBar
import com.inkwise.music.ui.main.navigationPage.components.PlaylistPickerSheet
import com.inkwise.music.ui.main.navigationPage.components.SongActionSheet
import com.inkwise.music.ui.main.navigationPage.components.SortBottomSheet
import com.inkwise.music.ui.main.navigationPage.components.SortMode
import com.inkwise.music.ui.main.navigationPage.home.HomeViewModel
import com.inkwise.music.ui.main.navigationPage.local.SongItem
import com.inkwise.music.ui.main.navigationPage.local.formatTime
import com.inkwise.music.ui.player.PlayerViewModel

private fun CloudSortBy.toSortMode(): SortMode = when (this) {
    CloudSortBy.CUSTOM -> SortMode.CUSTOM
    CloudSortBy.TITLE -> SortMode.TITLE
    CloudSortBy.CREATED_ASC -> SortMode.ADDED_ASC
    CloudSortBy.CREATED_DESC -> SortMode.ADDED_DESC
}

private fun SortMode.toCloudSortBy(): CloudSortBy = when (this) {
    SortMode.CUSTOM -> CloudSortBy.CUSTOM
    SortMode.TITLE -> CloudSortBy.TITLE
    SortMode.ADDED_ASC -> CloudSortBy.CREATED_ASC
    SortMode.ADDED_DESC -> CloudSortBy.CREATED_DESC
}

@Composable
fun CloudSongsScreen(
    playerViewModel: PlayerViewModel = hiltViewModel(),
    cloudViewModel: CloudViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by cloudViewModel.uiState.collectAsState()
    val playbackState by playerViewModel.playbackState.collectAsState()
    val playlists by homeViewModel.playlists.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    val context = LocalContext.current

    var showSortSheet by remember { mutableStateOf(false) }
    var actionSong by remember { mutableStateOf<Song?>(null) }

    // ── 多选状态 ──
    var multiSelectMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var showPlaylistPicker by remember { mutableStateOf(false) }

    fun toggleSelectAll() {
        selectedIds = if (selectedIds.size == uiState.songs.size) emptySet() else uiState.songs.map { it.id }.toSet()
    }

    fun exitMultiSelect() {
        multiSelectMode = false
        selectedIds = emptySet()
    }

    val selectedSongs = uiState.songs.filter { it.id in selectedIds }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── 工具栏 ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (multiSelectMode) {
                TextButton(onClick = { toggleSelectAll() }) {
                    Text(
                        if (selectedIds.size == uiState.songs.size) "取消全选" else "全选",
                        color = Color.Black
                    )
                }
                Text(text = "已选 ${selectedIds.size} 首", color = MaterialTheme.colorScheme.primary)
                TextButton(onClick = { exitMultiSelect() }) {
                    Text("取消", color = Color.Black)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { playerViewModel.playSongsShuffle(uiState.songs) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_player_random),
                            contentDescription = "随机播放",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = uiState.songs.size.toString())
                }
                Row {
                    IconButton(
                        onClick = { showSortSheet = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sort),
                            contentDescription = "排序",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { multiSelectMode = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_multiple_choice),
                            contentDescription = "选择",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.padding(top = 6.dp))

        // ── 列表 ──
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { cloudViewModel.refresh() },
            modifier = Modifier.weight(1f),
            state = pullToRefreshState,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = uiState.isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = Color.White,
                    color = Color.Blue
                )
            }
        ) {
            when {
                uiState.isLoading && uiState.songs.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null && uiState.songs.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = uiState.error ?: "", color = Color.Red)
                            Spacer(modifier = Modifier.padding(top = 8.dp))
                            Text(
                                text = "点击重试",
                                color = Color.Blue,
                                modifier = Modifier.clickable { cloudViewModel.loadSongs() }
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn {
                        itemsIndexed(uiState.songs) { index, song ->
                            SongItem(
                                song = song,
                                isPlaying = playbackState.currentSong?.id == song.id,
                                onClick = { playerViewModel.playSongs(uiState.songs, index) },
                                addToQueue = { playerViewModel.addToQueue(song) },
                                onMoreClick = { actionSong = song },
                                multiSelectMode = multiSelectMode,
                                isSelected = song.id in selectedIds,
                                onToggleSelect = {
                                    selectedIds = if (song.id in selectedIds)
                                        selectedIds - song.id
                                    else
                                        selectedIds + song.id
                                }
                            )
                        }
                    }
                }
            }
        }

        // ── 多选底部栏 ──
        if (multiSelectMode) {
            MultiSelectBottomBar(
                selectedCount = selectedIds.size,
                onDelete = {
                    cloudViewModel.deleteCloudSongs(selectedIds.toList())
                    Toast.makeText(context, "已删除 ${selectedIds.size} 首", Toast.LENGTH_SHORT).show()
                    exitMultiSelect()
                },
                onAddToPlaylist = { showPlaylistPicker = true },
                onPlaySelected = {
                    playerViewModel.playSongs(selectedSongs, 0)
                    exitMultiSelect()
                }
            )
        }
    }

    // ── 排序面板 ──
    if (showSortSheet) {
        SortBottomSheet(
            currentMode = uiState.sortBy.toSortMode(),
            onSelect = { mode ->
                cloudViewModel.setSortBy(mode.toCloudSortBy())
                showSortSheet = false
            },
            onDismiss = { showSortSheet = false }
        )
    }

    // ── 歌单选择器 ──
    if (showPlaylistPicker) {
        PlaylistPickerSheet(
            playlists = playlists,
            onSelect = { playlistId ->
                selectedIds.forEach { songId ->
                    homeViewModel.addSongToPlaylist(playlistId, songId)
                }
                Toast.makeText(context, "已添加 ${selectedIds.size} 首到歌单", Toast.LENGTH_SHORT).show()
                showPlaylistPicker = false
            },
            onDismiss = { showPlaylistPicker = false }
        )
    }

    // ── 单曲操作 ──
    actionSong?.let { song ->
        SongActionSheet(
            song = song,
            playlists = playlists,
            isInPlaylist = false,
            onDismiss = { actionSong = null },
            onPlayNext = {
                playerViewModel.addToQueue(song)
                Toast.makeText(context, "已添加到下一首", Toast.LENGTH_SHORT).show()
            },
            onShowInfo = {
                Toast.makeText(
                    context,
                    "${song.title} - ${song.artist}\n" +
                        "时长: ${formatTime(song.duration)}\n" +
                        "采样率: ${song.sampleRate}Hz\n" +
                        "比特率: ${song.bitrate}bps\n" +
                        "编码: ${song.codec}",
                    Toast.LENGTH_LONG
                ).show()
            },
            onDelete = {
                cloudViewModel.deleteCloudSongs(listOf(song.id))
                Toast.makeText(context, "已删除: ${song.title}", Toast.LENGTH_SHORT).show()
            },
            onAddToPlaylist = { playlistId ->
                homeViewModel.addSongToPlaylist(playlistId, song.id)
                Toast.makeText(context, "已添加到歌单", Toast.LENGTH_SHORT).show()
            },
            onRemoveFromPlaylist = {}
        )
    }
}
