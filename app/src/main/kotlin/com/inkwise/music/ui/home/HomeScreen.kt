
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
import androidx.hilt.navigation.compose.hiltViewModel
// 放在檔案最上面，其他 import 旁邊

import androidx.compose.foundation.lazy.LazyColumn          // LazyColumn
import androidx.compose.foundation.lazy.items               // items(items = xxx) { }

import androidx.compose.runtime.collectAsState              // collectAsState()
import androidx.compose.runtime.getValue                    // by xxx 的語法需要

@Composable
fun HomeScreen(
    onNavigateToLocal: () -> Unit,
    onNavigateToCloud: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
	val playlists by viewModel.playlists.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "欢迎来到音乐播放器",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 48.dp)
        )
        
        // 本地歌曲按钮
        ElevatedButton(
            onClick = onNavigateToLocal,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(80.dp)
        ) {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text("本地歌曲", style = MaterialTheme.typography.titleLarge)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 云歌曲按钮
        ElevatedButton(
            onClick = onNavigateToCloud,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(80.dp)
        ) {
            Icon(
                Icons.Default.Cloud,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text("云端歌曲", style = MaterialTheme.typography.titleLarge)
        }
        
        LazyColumn {
	        items(playlists) {
	            Text(it.title, Modifier.padding(16.dp))
	        }
	    }
    }
}
