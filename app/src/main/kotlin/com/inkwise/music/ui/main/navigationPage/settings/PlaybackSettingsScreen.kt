package com.inkwise.music.ui.main.navigationPage.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackSettingsScreen(
    onBack: () -> Unit,
    viewModel: PlaybackSettingsViewModel = hiltViewModel(),
) {
    val audioFocusEnabled by viewModel.audioFocusEnabled.collectAsState()
    val fadeEnabled by viewModel.fadeEnabled.collectAsState()
    val cacheEnabled by viewModel.cacheEnabled.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("播放设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // 音频焦点
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("音频焦点", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "开启后其他应用播放时自动暂停音乐",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = audioFocusEnabled,
                    onCheckedChange = { viewModel.setAudioFocusEnabled(it) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // 淡入淡出
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("播放/暂停淡入淡出", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "播放时音量渐入，暂停时音量渐出",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = fadeEnabled,
                    onCheckedChange = { viewModel.setFadeEnabled(it) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // 边听边存
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("边听边存", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "播放时逐步缓存到本地，减少重复加载",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = cacheEnabled,
                    onCheckedChange = { viewModel.setCacheEnabled(it) }
                )
            }
        }
    }
}
