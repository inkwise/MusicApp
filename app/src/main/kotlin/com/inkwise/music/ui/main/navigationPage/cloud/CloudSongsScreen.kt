package com.inkwise.music.ui.main.navigationPage.cloud

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inkwise.music.R
import com.inkwise.music.ui.main.navigationPage.local.SongItem
import com.inkwise.music.ui.player.PlayerViewModel

@Composable
fun CloudSongsScreen(
    playerViewModel: PlayerViewModel = hiltViewModel(),
    cloudViewModel: CloudViewModel = hiltViewModel()
) {
    val uiState by cloudViewModel.uiState.collectAsState()
    val playbackState by playerViewModel.playbackState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    var showSortMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部操作栏
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
                // 排序按钮
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
                                    Text(
                                        text = "时间排序",
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (uiState.sortBy == CloudSortBy.CREATED_AT) {
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
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
                                    Text(
                                        text = "首字母排序",
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (uiState.sortBy == CloudSortBy.TITLE) {
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
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

                IconButton(
                    onClick = {},
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

        Spacer(modifier = Modifier.padding(top = 6.dp))

        // 下拉刷新
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
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null && uiState.songs.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.error ?: "",
                                color = Color.Red
                            )
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
                                isPlaying = playbackState.currentSong?.let { current ->
                                    current.cloudId != null && current.cloudId == song.cloudId
                                } ?: false,
                                onClick = { playerViewModel.playSongs(uiState.songs, index) },
                                addToQueue = { playerViewModel.addToQueue(song) }
                            )
                        }
                    }
                }
            }
        }
    }
}
