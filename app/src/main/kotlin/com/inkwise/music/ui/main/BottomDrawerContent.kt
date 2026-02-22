package com.inkwise.music.ui.main

import android.app.Activity
import android.widget.ImageView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.Glide
import com.inkwise.music.R
import com.inkwise.music.data.model.LyricsSource
import com.inkwise.music.data.model.PlayMode
import com.inkwise.music.ui.main.navigationPage.local.formatTime
import com.inkwise.music.ui.player.PlayerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BottomDrawerContent(
    pagerState: PagerState,
    animatedThemeColor: Color,
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val playbackState by playerViewModel.playbackState.collectAsState()
    val currentSong = playbackState.currentSong
    val pageCount = 2
    val coverUri = currentSong?.albumArt
    val scope = rememberCoroutineScope()
    var showSleepSheet by remember { mutableStateOf(false) }
    val pagerStateB =
        rememberPagerState(
            pageCount = { pageCount },
        )

    val context = LocalContext.current
    // 定时器
    val sleepRemaining by playerViewModel.sleepRemaining.collectAsState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding() // 自动增加顶部 Padding，高度等于状态栏
                // .padding(horizontal = 16.dp)
                // .padding(bottom = 16.dp)
                .padding(28.dp), // ,
    ) {
        // ---------- 顶部：歌名 / 歌手 ----------
        Column(
            modifier =
                Modifier
                    .fillMaxWidth(),
            // .padding(top = 24.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = currentSong?.title ?: "墨迹",
                color = animatedThemeColor,
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        // 2. 使用 fontWeight 设置加粗
                        fontWeight = FontWeight.Bold,
                    ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis, // 建议加上，防止长歌名挤压布局
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = currentSong?.artist ?: "@inkwise",
                color = animatedThemeColor,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // ---------- 中间：左右切换页面 ----------
        CompositionLocalProvider(
            LocalOverscrollConfiguration provides null, // ❗关键
        ) {
            HorizontalPager(
                state = pagerStateB,
                beyondViewportPageCount = 2,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                userScrollEnabled = true, // 👈 明确打开
            ) { page ->
                when (page) {
                    0 -> {
                        // 封面页

                        Column(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            // -------------------------------
                            // 封面区域（固定占剩余空间）
                            // -------------------------------
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                // ✅ 只有它用 weight
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier =
                                        Modifier
                                            .aspectRatio(1f)
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
                                                Glide.with(imageView).load(uri).into(imageView)
                                            } else {
                                                imageView.setImageDrawable(null)
                                            }
                                        },
                                    )

                                    if (coverUri == null) {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = null,
                                            modifier = Modifier.size(32.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }

                            // -------------------------------
                            // 歌词区域（不影响封面）
                            // -------------------------------
                            MiniLyricsView(
                                viewModel = playerViewModel,
                                animatedThemeColor = animatedThemeColor,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(60.dp), // ✅ 明确高度（推荐）
                            )
                        }
                    }

                    1 -> {
                        // 歌词页
                        LyricsPage(
                            playerViewModel = playerViewModel,
                            animatedThemeColor = animatedThemeColor,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }

        // ---------- 进度条 ----------
        Column(modifier = Modifier.padding(top = 4.dp)) {
            Slider(
                value =
                    if (playbackState.duration > 0) {
                        playbackState.currentPosition.toFloat() / playbackState.duration
                    } else {
                        0f
                    },
                onValueChange = { progress ->
                    playerViewModel.seekTo((progress * playbackState.duration).toLong())
                },
                // 1. 自定义颜色
                colors =
                    SliderDefaults.colors(
                        activeTrackColor = animatedThemeColor, // 已播放部分的进度条颜色
                        inactiveTrackColor = animatedThemeColor.copy(alpha = 0.24f), // 未播放部分的背景色
                        thumbColor = animatedThemeColor, // 滑块颜色
                        activeTickColor = Color.Transparent, // 隐藏刻度线
                        inactiveTickColor = Color.Transparent,
                    ),
                // 隐藏滑块
                thumb = {},
                // 3. 调整轨道高度 (取消默认厚度)
                track = { sliderState ->
                    SliderDefaults.Track(
                        sliderState = sliderState,
                        modifier = Modifier.height(2.dp), // 让进度条更纤细
                        colors =
                            SliderDefaults.colors(
                                activeTrackColor = animatedThemeColor,
                                inactiveTrackColor = animatedThemeColor.copy(alpha = 0.2f),
                            ),
                        // 取消隐藏滑块后的缺口
                        thumbTrackGapSize = 0.dp,
                        // 关闭尾部小圆点
                        drawStopIndicator = null,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    formatTime(playbackState.currentPosition),
                    style = MaterialTheme.typography.bodySmall,
                    color = animatedThemeColor,
                )
                Text(
                    formatTime(playbackState.duration),
                    style = MaterialTheme.typography.bodySmall,
                    color = animatedThemeColor,
                )
            }
        }

        // ---------- 播放控制 ----------
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { playerViewModel.skipToPrevious() },
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_round_skip_previous_24),
                    contentDescription = "上一首",
                    tint = animatedThemeColor,
                    modifier = Modifier.size(38.dp),
                )
            }

            Spacer(Modifier.width(24.dp))

            IconButton(
                onClick = { playerViewModel.playPause() },
                modifier = Modifier.size(52.dp),
            ) {
                Icon(
                    painter =
                        painterResource(
                            id =
                                if (playbackState.isPlaying) {
                                    R.drawable.ic_mini_player_pause
                                } else {
                                    R.drawable.ic_mini_player_play
                                },
                        ),
                    null,
                    modifier = Modifier.size(32.dp),
                    tint = animatedThemeColor,
                )
            }

            Spacer(Modifier.width(24.dp))

            IconButton(
                onClick = { playerViewModel.skipToNext() },
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_round_skip_next_24),
                    contentDescription = "下一首",
                    tint = animatedThemeColor,
                    modifier = Modifier.size(38.dp),
                )
            }
        }

        // ---------- 底部五按钮 ----------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = { playerViewModel.togglePlayMode() }) {
                Icon(
                    painter =
                        when (playbackState.playMode) {
                            PlayMode.LIST -> painterResource(id = R.drawable.ic_player_circle)
                            PlayMode.SINGLE -> painterResource(id = R.drawable.ic_player_repeat_one)
                            PlayMode.SHUFFLE -> painterResource(id = R.drawable.ic_player_random)
                        },
                    contentDescription = "播放模式",
                    tint = animatedThemeColor,
                    modifier = Modifier.size(24.dp),
                )
            }
            // 循环模式按钮
            Column {
                IconButton(onClick = { showSleepSheet = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_sleep_timer),
                        contentDescription = "定时",
                        tint = animatedThemeColor,
                        modifier = Modifier.size(24.dp),
                    )
                }
                sleepRemaining?.let { millis ->

                    val totalSeconds = millis / 1000
                    val minutes = totalSeconds / 60
                    val seconds = totalSeconds % 60

                    Text(
                        text = "剩余 ${minutes}分${seconds}秒",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            IconButton(onClick = { /* 音效 */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_audio_effect),
                    contentDescription = "音效",
                    tint = animatedThemeColor,
                    modifier = Modifier.size(24.dp),
                )
            }
            IconButton(onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(1)
                }
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_play_queue),
                    contentDescription = "播放队列",
                    tint = animatedThemeColor,
                    modifier = Modifier.size(24.dp),
                )
            }
            IconButton(onClick = { /* 菜单 */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_player_more),
                    contentDescription = "菜单",
                    tint = animatedThemeColor,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }

    if (showSleepSheet) {
        SleepTimerBottomSheet(
            onDismiss = { showSleepSheet = false },
            onConfirm = { minutes, stopAfterSong ->
                playerViewModel.startSleepTimer(
                    minutes = minutes,
                    stopAfterSong = stopAfterSong,
                ) {
                    (context as? Activity)?.finishAffinity()
                }
                showSleepSheet = false
            },
        )
    }
}

@Composable
fun LyricsPage(
    playerViewModel: PlayerViewModel,
    animatedThemeColor: Color,
    modifier: Modifier = Modifier,
) {
    val lyricsState by playerViewModel.lyricsState.collectAsState()

    val hasTranslation =
        lyricsState.lyrics
            ?.lines
            ?.any { line -> line.translation != null }
            ?: false

    var showTranslation by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        // ----------------------------
        // 歌词主体
        // ----------------------------
        Box(
            modifier = Modifier.weight(1f),
        ) {
            LyricsView(
                viewModel = playerViewModel,
                animatedThemeColor = animatedThemeColor,
                showTranslation = showTranslation && hasTranslation,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // ----------------------------
        // 底部工具栏
        // ----------------------------
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // ⬅️ 左侧：歌词来源
            Text(
                text =
                    lyricsState.lyrics?.source?.let {
                        when (it) {
                            LyricsSource.LOCAL_LRC -> "本地 LRC"
                            LyricsSource.LOCAL_KRC -> "本地 KRC"
                            LyricsSource.EMBEDDED -> "内嵌歌词"
                            LyricsSource.NETWORK -> "网络歌词"
                            LyricsSource.USER_PROVIDED -> "用户歌词"
                        }
                    } ?: "",
                fontSize = 12.sp,
                color = Color.Gray,
            )

            Spacer(modifier = Modifier.weight(1f))

            // ➡️ 右侧：翻译开关（仅在有翻译时显示）
            if (hasTranslation) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "翻译",
                        fontSize = 12.sp,
                        color = Color.Gray,
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Switch(
                        checked = showTranslation,
                        onCheckedChange = { showTranslation = it },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (Int, Boolean) -> Unit,
) {
    var minutes by remember { mutableStateOf(30f) }
    var stopAfterSong by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
        ) {
            Text(
                text = "睡眠定时",
                style = MaterialTheme.typography.titleLarge,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "${minutes.toInt()} 分钟",
                style = MaterialTheme.typography.bodyLarge,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = minutes,
                onValueChange = { minutes = it },
                valueRange = 0f..120f,
                steps = 119,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Switch(
                    checked = stopAfterSong,
                    onCheckedChange = { stopAfterSong = it },
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("播完整首再退出")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        onConfirm(minutes.toInt(), stopAfterSong)
                    },
                    enabled = minutes > 0,
                ) {
                    Text("确定")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
