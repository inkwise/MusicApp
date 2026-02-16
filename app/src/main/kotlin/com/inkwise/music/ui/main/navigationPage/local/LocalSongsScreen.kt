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
            /*
     indicator = {
        // 直接在这个作用域调用，不要写 state -> 
        // 也不要手动传 state 参数
        
        PullToRefreshDefaults.Indicator(
            isRefreshing = isScanning,
            state = pullToRefreshState, 
            modifier = Modifier.align(Alignment.TopCenter),
            containerColor = Color.Transparent, // 去掉背景
        )*/
      /*  indicator = {
        // 计算当前下拉的透明度或缩放，让过渡更自然
        val progress = pullToRefreshState.distanceFraction
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp), // 距离顶部的偏移量
            contentAlignment = Alignment.TopCenter
        ) {
            if (isScanning) {
                // 1. 正在刷新状态：显示不断旋转的进度条
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp
                )
            } else if (progress > 0f) {
                // 2. 下拉过程中：进度条随手指下滑距离而填满
                CircularProgressIndicator(
                    progress = { progress }, // 关键：绑定下拉进度
                    modifier = Modifier.size(28.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = progress.coerceIn(0.3f, 1f)),
                    strokeWidth = 3.dp,
                    trackColor = Color.Transparent // 确保背景也是透明的
                )
            }
        }
    }*/
    indicator = {
        PullToRefreshDefaults.Indicator(
            state = pullToRefreshState,
            isRefreshing = isScanning,
            modifier = Modifier
                .align(Alignment.TopCenter)
                // 核心黑科技：通过 graphicsLayer 确保容器内容（进度条）可见，
                // 但由于 containerColor 已经是透明，shadowColor 也设为透明，背景圆圈就彻底隐身了
                .graphicsLayer {
                    // 如果你觉得还有残影，可以在这里微调缩放或透明度
                    alpha = 1f 
                },
            containerColor = Color.Transparent, // 关键：背景透明
            contentColor = MaterialTheme.colorScheme.primary, // 进度条颜色
            shadowColor = Color.Transparent, // 必须：去掉阴影
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
