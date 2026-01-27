
// ui/home/HomeScreen.kt
package com.inkwise.music.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
@Composable
fun HomeScreen(
    onNavigateToLocal: () -> Unit,
    onNavigateToCloud: () -> Unit,
    onCreatePlaylist: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "欢迎来到音乐播放器",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        /* 本地 / 云端 */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedButton(
                onClick = onNavigateToLocal,
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp)
            ) {
                Icon(Icons.Default.MusicNote, null)
                Spacer(Modifier.width(8.dp))
                Text("本地歌曲")
            }

            ElevatedButton(
                onClick = onNavigateToCloud,
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp)
            ) {
                Icon(Icons.Default.Cloud, null)
                Spacer(Modifier.width(8.dp))
                Text("云端歌曲")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        /* 操作栏 */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "我的歌单",
                style = MaterialTheme.typography.titleLarge
            )

            Row {
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新")
                }
                IconButton(onClick = onCreatePlaylist) {
                    Icon(Icons.Default.Add, contentDescription = "创建歌单")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        /* 歌单列表 */
        when {
            uiState.loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.playlists.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无歌单")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.playlists) { playlist ->
                        PlaylistItem(playlist)
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistItem(playlist: Playlist) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /* TODO: 进入歌单 */ }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleMedium
            )
            if (playlist.description != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = playlist.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}