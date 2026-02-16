package com.inkwise.music.ui.main.navigationPage.local

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
import com.inkwise.music.ui.player.PlayerViewModel
// clickable
import androidx.compose.foundation.clickable

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.graphics.Color
import com.inkwise.music.R
import androidx.compose.ui.res.painterResource
@Composable
fun LocalSongsScreen(
    playerViewModel: PlayerViewModel = hiltViewModel(),
    mainViewModel: com.inkwise.music.ui.main.MainViewModel = hiltViewModel(),
    localViewModel: LocalViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val playbackState by playerViewModel.playbackState.collectAsState()
    val songs by localViewModel.localSongs.collectAsState()
    val isScanning by localViewModel.isScanning.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    // 首次加载
    LaunchedEffect(Unit) {
        // localViewModel.loadLocalSongsFromStore()
        //  localViewModel.scanSongs(context)
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(10.dp)
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { localViewModel.scanSongs(context) },
            // 点击标题扫描
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
        Row{
            IconButton(
                onClick = {},
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_player_random),
                    contentDescription = "随机播放",
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = songs.size.toString()
            )}

        /*    if (songs.isNotEmpty()) {
                FilledTonalButton(
                    onClick = { playerViewModel.playSongs(songs) },
                ) {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("播放全部")
                }
            }*/
            
            Row{
                IconButton(
                onClick = {},
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sort),
                    contentDescription = "排序",
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp),
                )
            }
                        Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = {},
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_multiple_choice),
                    contentDescription = "选择",
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp),
                )
            }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 下拉刷新封装
        PullToRefreshBox(
            isRefreshing = isScanning,
            onRefresh = { localViewModel.scanSongs(context) },
            modifier = Modifier.fillMaxSize(),
            state = pullToRefreshState, 
     indicator = {
        // 直接在这个作用域调用，不要写 state -> 
        // 也不要手动传 state 参数
        PullToRefreshDefaults.Indicator(
            isRefreshing = isScanning,
            state = pullToRefreshState, 
            modifier = Modifier.align(Alignment.TopCenter),
            containerColor = Color.Transparent, // 去掉背景
            color = MaterialTheme.colorScheme.primary,
            // 注意：如果这里还报错说需要 state，
            // 请查看下方“如果仍然报错”的部分
        )
    }
        ) {
            if (isScanning && songs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn {
                    itemsIndexed(songs) { index, song ->
                        SongItem(
                            song = song,
                            isPlaying = playbackState.currentSong?.localId == song.localId,
                            onClick = { playerViewModel.playSongs(songs, index) },
                            onMoreClick = { /* 更多操作 */ },
                        )
                    }
                }
            }
        }
    }
}
