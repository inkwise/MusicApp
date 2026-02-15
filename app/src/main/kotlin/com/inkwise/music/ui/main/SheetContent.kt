package com.inkwise.music.ui.main

import android.annotation.SuppressLint
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.Glide
import com.inkwise.music.ui.player.PlayerViewModel
import com.inkwise.music.ui.theme.LocalAppDimens

// 手柄区域
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun controlContent(
    modifier: Modifier,
    onClick: () -> Unit,
    showPlayQueue: () -> Unit,
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val dimens = LocalAppDimens.current

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(dimens.sheetPeekHeightDp)

                .clickable(
                    indication = null, // 🚫 去掉波纹
                    interactionSource = remember { MutableInteractionSource() },
                ) {
                    onClick()
                },
    ) {
        // 滑动控件
        ReboundHorizontalDrag(
            onPrev = { playerViewModel.skipToPrevious() },
            onNext = { playerViewModel.skipToNext() },
        )
        // 控制层
        controlContent2(showPlayQueue = showPlayQueue)
    }

}

@Composable
fun controlContent2(
    modifier: Modifier = Modifier,
    onIcon1Click: () -> Unit = {},
    onIcon2Click: () -> Unit = {},
    showPlayQueue: () -> Unit = {},
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val playbackState by playerViewModel.playbackState.collectAsState()
    val currentSong = playbackState.currentSong
    val coverUri = currentSong?.albumArt

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                // .height(56.dp)
                .fillMaxHeight()
                .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            AndroidView(
                modifier = Modifier.matchParentSize(),
                factory = { context ->
                    ImageView(context).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                },
                update = { imageView ->
                    val uri = coverUri
                    if (uri != null) {
                        Glide
                            .with(imageView)
                            .load(uri)
                            .into(imageView)
                    } else {
                        // 没有封面时，清空 ImageView，避免残影
                        imageView.setImageDrawable(null)
                    }
                },
            )

            // 🎵 Icon 占位（只在没封面时显示）
            if (coverUri == null) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        // 中间撑开
        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = { playerViewModel.playPause() }) {
            Icon(
                if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (playbackState.isPlaying) "暂停" else "播放",
                modifier = Modifier.size(32.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))

        // 右侧第二个 Icon
        Icon(
            imageVector = Icons.Default.SkipNext,
            contentDescription = "下一首",
            modifier =
                Modifier
                    .size(28.dp)
                    .clickable {
                        showPlayQueue()
                    },
        )
    }
}
