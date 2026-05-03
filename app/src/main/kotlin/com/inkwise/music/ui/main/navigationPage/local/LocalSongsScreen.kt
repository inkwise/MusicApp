package com.inkwise.music.ui.main.navigationPage.local

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.inkwise.music.R
import com.inkwise.music.data.model.Song
import com.inkwise.music.hasAllFilesPermission
import com.inkwise.music.requestAllFilesPermission
import com.inkwise.music.ui.main.navigationPage.components.SongActionSheet
import com.inkwise.music.ui.main.navigationPage.home.HomeViewModel
import com.inkwise.music.ui.player.PlayerViewModel

private val mediaPermission =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

@Composable
fun LocalSongsScreen(
    playerViewModel: PlayerViewModel = hiltViewModel(),
    localViewModel: LocalViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val playbackState by playerViewModel.playbackState.collectAsState()
    val songs by localViewModel.localSongs.collectAsState()
    val isScanning by localViewModel.isScanning.collectAsState()
    val playlists by homeViewModel.playlists.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    var actionSong by remember { mutableStateOf<Song?>(null) }
    var showScanDialog by remember { mutableStateOf(false) }

    val mediaPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                localViewModel.scanSongs(context)
            } else {
                Toast.makeText(context, "需要音频权限才能扫描本地音乐", Toast.LENGTH_SHORT).show()
            }
        }

    // ── 权限辅助 ──
    fun hasMediaPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, mediaPermission) == PackageManager.PERMISSION_GRANTED

    fun requestScanOrPermission() {
        if (hasMediaPermission()) {
            localViewModel.scanSongs(context)
        } else {
            mediaPermissionLauncher.launch(mediaPermission)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (songs.isEmpty() && !isScanning) {
            // ── 空态：居中扫描按钮 ──
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Button(onClick = { showScanDialog = true }) {
                    Text("扫描本地歌曲")
                }
            }
        } else {
            // ── 工具栏 ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { playerViewModel.playSongsShuffle(songs) },
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
                    Text(text = songs.size.toString())
                }
                Row {
                    IconButton(onClick = {}, modifier = Modifier.size(24.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sort),
                            contentDescription = "排序",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
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

            // ── 下拉刷新列表 ──
            PullToRefreshBox(
                isRefreshing = isScanning,
                onRefresh = { requestScanOrPermission() },
                modifier = Modifier.fillMaxSize(),
                state = pullToRefreshState,
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = pullToRefreshState,
                        isRefreshing = isScanning,
                        modifier = Modifier.align(Alignment.TopCenter),
                        containerColor = Color.White,
                        color = Color.Blue
                    )
                }
            ) {
                if (isScanning && songs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn {
                        itemsIndexed(songs) { index, song ->
                            SongItem(
                                song = song,
                                isPlaying = playbackState.currentSong?.id == song.id,
                                onClick = { playerViewModel.playSongs(songs, index) },
                                addToQueue = { playerViewModel.addToQueue(song) },
                                onMoreClick = { actionSong = song }
                            )
                        }
                    }
                }
            }
        }
    }

    // ── 扫描方式选择对话框 ──
    if (showScanDialog) {
        AlertDialog(
            onDismissRequest = { showScanDialog = false },
            title = { Text("选择扫描方式") },
            text = { Text("媒体库扫描：快速读取系统媒体库\n详细扫描：扫描整个存储空间") },
            confirmButton = {
                TextButton(onClick = {
                    showScanDialog = false
                    requestScanOrPermission()
                }) {
                    Text("媒体库扫描")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showScanDialog = false
                    if (hasAllFilesPermission()) {
                        localViewModel.detailedScan(context)
                    } else {
                        requestAllFilesPermission(context)
                    }
                }) {
                    Text("详细扫描")
                }
            }
        )
    }

    // ── 歌曲操作菜单 ──
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
                localViewModel.deleteSong(song)
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
