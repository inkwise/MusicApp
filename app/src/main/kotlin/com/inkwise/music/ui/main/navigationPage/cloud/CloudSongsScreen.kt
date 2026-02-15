package com.inkwise.music.ui.main.navigationPage.cloud

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CloudSongsScreen() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
    ) {
        LazyColumn {
            items(10) { index ->
                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                ) {
                    Text(
                        text = "云端歌曲 ${index + 1}",
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}
