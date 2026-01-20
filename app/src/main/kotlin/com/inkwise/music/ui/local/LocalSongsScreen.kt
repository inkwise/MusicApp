package com.inkwise.music.ui.local

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LocalSongsScreen() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
    ) {
        Text(
            text = "本地歌曲",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        LazyColumn {
            items(10) { index ->
                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                ) {
                    Text(
                        text = "本地歌曲 ${index + 1}",
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}
