
// ui/home/HomeScreen.kt
package com.inkwise.music.ui.main.navigationPage.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
// 放在檔案最上面，其他 import 旁邊

import androidx.compose.foundation.lazy.LazyColumn // LazyColumn
import androidx.compose.foundation.lazy.items // items(items = xxx) { }

import androidx.compose.runtime.collectAsState // collectAsState()
import androidx.compose.runtime.getValue // by xxx 的語法需要

@Composable
fun HomeScreen(
    onNavigateToLocal: () -> Unit,
    onNavigateToCloud: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val playlists by viewModel.songs.collectAsState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
    ) {
        // 本地 & 云端按钮横向排列
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ElevatedButton(
                onClick = onNavigateToLocal,
                modifier = Modifier.weight(1f).height(64.dp),
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("本地歌曲", style = MaterialTheme.typography.titleMedium)
            }

            ElevatedButton(
                onClick = onNavigateToCloud,
                modifier = Modifier.weight(1f).height(64.dp),
            ) {
                Icon(
                    Icons.Default.Cloud,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("云端歌曲", style = MaterialTheme.typography.titleMedium)
            }
        }

        // 刷新 + 创建歌单按钮
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Button(
                onClick = {
                    viewModel.refreshPlaylists()
                },
                modifier = Modifier.weight(1f).height(48.dp),
            ) {
                Text("刷新")
            }

            Button(
                onClick = {
                    viewModel.createPlaylist("测试歌单")
                },
                modifier = Modifier.weight(1f).height(48.dp),
            ) {
                Text("创建歌单")
            }
        }

        // 歌单列表
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(playlists) { playlist ->
                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 0.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // 如果有封面可以用 Image
                        // Image(painter = painterResource(id = playlist.coverRes), contentDescription = null, modifier = Modifier.size(48.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(playlist.title, style = MaterialTheme.typography.titleMedium)
                            // Text("${playlist.size} 首歌曲", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
