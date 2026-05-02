package com.inkwise.music.ui.main.navigationPage.home

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inkwise.music.data.model.Song
import com.inkwise.music.ui.main.navigationPage.components.SongActionSheet
import com.inkwise.music.ui.main.navigationPage.local.SongItem
import com.inkwise.music.ui.main.navigationPage.local.formatTime
import com.inkwise.music.ui.player.PlayerViewModel

@Composable
fun PlaylistDetailScreen(
    playerViewModel: PlayerViewModel = hiltViewModel(),
    detailViewModel: PlaylistDetailViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by detailViewModel.uiState.collectAsState()
    val playbackState by playerViewModel.playbackState.collectAsState()
    val playlists by homeViewModel.playlists.collectAsState()
    val context = LocalContext.current

    var actionSong by remember { mutableStateOf<Song?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = uiState.playlistTitle,
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (uiState.songs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("歌单为空", color = androidx.compose.ui.graphics.Color.Gray)
            }
        } else {
            LazyColumn {
                itemsIndexed(uiState.songs) { index, song ->
                    SongItem(
                        song = song,
                        isPlaying = playbackState.currentSong?.id == song.id,
                        onClick = { playerViewModel.playSongs(uiState.songs, index) },
                        addToQueue = { playerViewModel.addToQueue(song) },
                        onMoreClick = { actionSong = song }
                    )
                }
            }
        }
    }

    // 歌曲操作菜单
    actionSong?.let { song ->
        SongActionSheet(
            song = song,
            playlists = playlists,
            isInPlaylist = true,
            onDismiss = { actionSong = null },
            onPlayNext = {
                playerViewModel.addToQueue(song)
                Toast.makeText(context, "已添加到下一首", Toast.LENGTH_SHORT).show()
            },
            onShowInfo = {
                Toast.makeText(
                    context,
                    "${song.title} - ${song.artist}\n时长: ${formatTime(song.duration)}\n采样率: ${song.sampleRate}Hz\n比特率: ${song.bitrate}bps",
                    Toast.LENGTH_LONG
                ).show()
            },
            onDelete = {
                detailViewModel.deleteSong(song)
                Toast.makeText(context, "已删除: ${song.title}", Toast.LENGTH_SHORT).show()
            },
            onAddToPlaylist = { targetPlaylistId ->
                detailViewModel.addToPlaylist(targetPlaylistId, song.id)
                Toast.makeText(context, "已添加到歌单", Toast.LENGTH_SHORT).show()
            },
            onRemoveFromPlaylist = {
                detailViewModel.removeSongFromPlaylist(song.id)
                Toast.makeText(context, "已从歌单中移除", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
