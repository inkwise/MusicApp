// ui/home/HomeScreen.kt
package com.inkwise.music.ui.main.navigationPage.home

// 放在檔案最上面，其他 import 旁邊
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inkwise.music.ui.theme.LocalAppDimens

@Composable
fun HomeScreen(
    onNavigateToLocal: () -> Unit,
    onNavigateToCloud: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val playlists by viewModel.playlists.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    val dimens = LocalAppDimens.current
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    // start = 24.dp,
                    // top = 24.dp,
                    // end = 24.dp,
                    bottom = dimens.sheetPeekHeightDp,
                ),
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
                modifier =
                    Modifier
                        .weight(1f)
                        .height(64.dp),
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
                modifier =
                    Modifier
                        .weight(1f)
                        .height(64.dp),
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
                    // viewModel.refreshPlaylists()
                    showDialog = true
                },
                modifier =
                    Modifier
                        .weight(1f)
                        .height(48.dp),
            ) {
                Text("刷新")
            }

            Button(
                onClick = {
                    // viewModel.createPlaylist("测试歌单")
                    showDialog = true
                },
                modifier =
                    Modifier
                        .weight(1f)
                        .height(48.dp),
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
                            Text(
                                playlist.playlist.title,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            // Text("${playlist.size} 首歌曲", style = MaterialTheme.typography.bodySmall)
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
            },
        )
    }
}
