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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import com.inkwise.music.ui.main.navigationPage.components.SongActionSheet
import com.inkwise.music.ui.main.navigationPage.home.HomeViewModel
import com.inkwise.music.ui.main.navigationPage.local.SongItem
import com.inkwise.music.ui.main.navigationPage.local.formatTime
import com.inkwise.music.ui.player.PlayerViewModel

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
    var showSortMenu by remember { mutableStateOf(false) }
    var actionSong by remember { mutableStateOf<Song?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
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
                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sort),
                            contentDescription = "排序",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("时间排序", modifier = Modifier.weight(1f))
                                    if (uiState.sortBy == CloudSortBy.CREATED_AT) {
                                        Icon(Icons.Default.ArrowDropDown, null, Modifier.size(16.dp))
                                    }
                                }
                            },
                            onClick = {
                                cloudViewModel.setSortBy(CloudSortBy.CREATED_AT)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("首字母排序", modifier = Modifier.weight(1f))
                                    if (uiState.sortBy == CloudSortBy.TITLE) {
                                        Icon(Icons.Default.ArrowDropDown, null, Modifier.size(16.dp))
                                    }
                                }
                            },
                            onClick = {
                                cloudViewModel.setSortBy(CloudSortBy.TITLE)
                                showSortMenu = false
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = {}, modifier = Modifier.size(24.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_multiple_choice),
                        contentDescription = "选择",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.padding(top = 6.dp))

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { cloudViewModel.refresh() },
            modifier = Modifier.fillMaxSize(),
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
                                onMoreClick = { actionSong = song }
                            )
                        }
                    }
                }
            }
        }
    }

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
                Toast.makeText(context, "云端歌曲暂不支持删除", Toast.LENGTH_SHORT).show()
            },
            onAddToPlaylist = { playlistId ->
                homeViewModel.addSongToPlaylist(playlistId, song.id)
                Toast.makeText(context, "已添加到歌单", Toast.LENGTH_SHORT).show()
            },
            onRemoveFromPlaylist = {}
        )
    }
}
