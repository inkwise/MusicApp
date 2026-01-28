package com.inkwise.music.ui.local

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inkwise.music.ui.player.PlayerViewModel
// clickable
import androidx.compose.foundation.clickable

import androidx.hilt.navigation.compose.hiltViewModel

import androidx.compose.material3.pulltorefresh.PullToRefreshBox
@Composable
fun LocalSongsScreen(
    playerViewModel: PlayerViewModel = hiltViewModel(),
    mainViewModel: com.inkwise.music.ui.main.MainViewModel = hiltViewModel(),
    localViewModel: LocalViewModel = viewModel()
) {
    val context = LocalContext.current
    val playbackState by playerViewModel.playbackState.collectAsState()
    val songs by localViewModel.localSongs.collectAsState()
    val isScanning by localViewModel.isScanning.collectAsState()

    // 首次加载
    LaunchedEffect(Unit) {
        localViewModel.loadLocalSongsFromStore()
        localViewModel.scanSongs(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { localViewModel.scanSongs(context) }, // 点击标题扫描
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "扫描",
                style = MaterialTheme.typography.headlineMedium
            )

            if (songs.isNotEmpty()) {
                FilledTonalButton(
                    onClick = { playerViewModel.playSongs(songs) }
                ) {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("播放全部")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 下拉刷新封装
        PullToRefreshBox(
            isRefreshing = isScanning,
            onRefresh = { localViewModel.scanSongs(context) },
            modifier = Modifier.fillMaxSize()
        ) {
            if (isScanning && songs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn {
                    itemsIndexed(songs) { index, song ->
                        SongItem(
                            song = song,
                            isPlaying = playbackState.currentSong?.loaclId == song.loaclId,
                            onClick = { playerViewModel.playSongs(songs, index) },
                            onMoreClick = { /* 更多操作 */ }
                        )
                    }
                }
            }
        }
    }
}