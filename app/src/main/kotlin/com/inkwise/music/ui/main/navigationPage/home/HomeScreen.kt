package com.inkwise.music.ui.main.navigationPage.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HomeScreen(
    onNavigateToLocal: () -> Unit,
    onNavigateToCloud: () -> Unit,
    onNavigateToPlaylist: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val playlists by viewModel.playlists.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedButton(
                onClick = onNavigateToLocal,
                modifier = Modifier.weight(1f).height(64.dp)
            ) {
                Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("本地歌曲", style = MaterialTheme.typography.titleMedium)
            }

            ElevatedButton(
                onClick = onNavigateToCloud,
                modifier = Modifier.weight(1f).height(64.dp)
            ) {
                Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("云端歌曲", style = MaterialTheme.typography.titleMedium)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { viewModel.createPlaylist("测试歌单") },
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Text("刷新歌单")
            }
            Button(
                onClick = { showDialog = true },
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Text("创建歌单")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(playlists) { playlist ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToPlaylist(playlist.playlist.id) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 云端/本地图标区分
                        val isCloud = playlist.playlist.cloudId != null
                        Icon(
                            imageVector = if (isCloud) Icons.Default.Cloud else Icons.Default.PhoneAndroid,
                            contentDescription = if (isCloud) "云端歌单" else "本地歌单",
                            tint = if (isCloud) Color(0xFF2196F3) else Color(0xFF4CAF50),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                playlist.playlist.title,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "${playlist.songs.size} 首歌曲 · ${if (isCloud) "云端" else "本地"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        PlaylistTitleDialog(
            visible = showDialog,
            onDismiss = { showDialog = false },
            onConfirm = { title ->
                viewModel.createPlaylist(title)
            }
        )
    }
}
