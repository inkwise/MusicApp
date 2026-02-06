package com.inkwise.music.ui.queue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inkwise.music.ui.player.PlayerViewModel
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState

import androidx.compose.ui.geometry.Offset

import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayQueueBottomSheet(
    playerViewModel: PlayerViewModel,
) {
    val playQueue by playerViewModel.playQueue.collectAsState()
    val currentIndex by playerViewModel.currentIndex.collectAsState()
    val playbackState by playerViewModel.playbackState.collectAsState()
	// ✅ 和 LazyColumn 绑定
    val listState = rememberLazyListState()
    val isAtTop by remember {
	    derivedStateOf {
	        listState.firstVisibleItemIndex == 0 &&
	        listState.firstVisibleItemScrollOffset == 0
	    }
	}
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
    ) {
        // 标题栏
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "播放队列",
                style = MaterialTheme.typography.titleLarge,
            )

            Row {
                // 清空队列
                TextButton(onClick = { /* 清空队列*/ }) {
                    Text("清空")
                }
            }
        }

        Text(
            text = "${playQueue.size} 首歌曲",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Divider()

        // 播放队列列表
        LazyColumn(
        	state = listState,
        	modifier = Modifier
            	//.fillMaxSize()
            	.weight(1f)
            .pointerInput(isAtTop) {
            detectVerticalDragGestures(
                onVerticalDrag = { change, dragAmount ->
                    if (isAtTop && dragAmount > 0f) {
                        // 👇 在顶部向下拉：不消费，给 Pager
                        return@detectVerticalDragGestures
                    }
                    // 👇 其他情况：列表自己处理
                    change.consume()
                }
            )
        }
        ) {
            itemsIndexed(playQueue) { index, song ->
                val isCurrentSong = index == currentIndex

                QueueItem(
                    song = song,
                    isPlaying = isCurrentSong && playbackState.isPlaying,
                    isCurrent = isCurrentSong,
                    onClick = {
                        playerViewModel.skipToIndex(index)
                    },
                    onRemove = {
                        playerViewModel.removeFromQueue(index)
                    },
                )
            }
        }
    }
}

@Composable
fun QueueItem(
    song: com.inkwise.music.data.model.Song,
    isPlaying: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 播放指示器或序号
        Box(
            modifier = Modifier.width(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (isPlaying) {
                Icon(
                    Icons.Default.GraphicEq,
                    contentDescription = "正在播放",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            } else if (isCurrent) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "当前歌曲",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // 歌曲信息
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                color =
                    if (isCurrent) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }

        // 移除按钮
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Default.Close,
                contentDescription = "移除",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
