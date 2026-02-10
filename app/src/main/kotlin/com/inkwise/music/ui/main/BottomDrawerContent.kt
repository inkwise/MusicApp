package com.inkwise.music.ui.main

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.Glide
import com.inkwise.music.R
import com.inkwise.music.ui.main.navigationPage.local.formatTime
import com.inkwise.music.ui.player.PlayerViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class,ExperimentalMaterial3Api::class)
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

    val pagerStateB =
        rememberPagerState(
            pageCount = { pageCount },
        )
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding() // 自动增加顶部 Padding，高度等于状态栏
                //.padding(horizontal = 16.dp)
                //.padding(bottom = 16.dp)
                .padding(28.dp) //,
    ) {
        // ---------- 顶部：歌名 / 歌手 ----------
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = currentSong?.title ?: "墨迹",
                color = animatedThemeColor,
                style = MaterialTheme.typography.titleLarge.copy(
                    // 2. 使用 fontWeight 设置加粗
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis // 建议加上，防止长歌名挤压布局
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = currentSong?.artist ?: "@inkwise",
                color = animatedThemeColor,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // ---------- 中间：左右切换页面 ----------
        CompositionLocalProvider(
            LocalOverscrollConfiguration provides null, // ❗关键
        ) {
            HorizontalPager(
                state = pagerStateB,
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),   // ✅ 只有它用 weight
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = Modifier
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
                                animatedThemeColor=animatedThemeColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)   // ✅ 明确高度（推荐）
                            )
                        }

                    }

                    1 -> {
                        // 歌词页（占位）
                        /*
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                     
                            LyricsView(playerViewModel)
                        }*/
                        LyricsPage(
					        playerViewModel = playerViewModel,
					        modifier = Modifier.fillMaxSize(),
					    )
                    }
                    
                    
 
                    ///////
                }
            }
        }

        // ---------- 进度条 ----------
        Column(modifier = Modifier.padding(top = 4.dp)) {

            Slider(
                value = if (playbackState.duration > 0) {
                    playbackState.currentPosition.toFloat() / playbackState.duration
                } else {
                    0f
                },
                onValueChange = { progress ->
                    playerViewModel.seekTo((progress * playbackState.duration).toLong())
                },
                // 1. 自定义颜色
                colors = SliderDefaults.colors(
                    activeTrackColor = animatedThemeColor,      // 已播放部分的进度条颜色
                    inactiveTrackColor = animatedThemeColor.copy(alpha = 0.24f), // 未播放部分的背景色
                    thumbColor = animatedThemeColor,            // 滑块颜色
                    activeTickColor = Color.Transparent,   // 隐藏刻度线
                    inactiveTickColor = Color.Transparent
                ),
                //隐藏滑块
                thumb = {},
                // 3. 调整轨道高度 (取消默认厚度)
                track = { sliderState ->
                    SliderDefaults.Track(
                        sliderState = sliderState,
                        modifier = Modifier.height(2.dp), // 让进度条更纤细
                        colors = SliderDefaults.colors(
                            activeTrackColor = animatedThemeColor,
                            inactiveTrackColor =animatedThemeColor.copy(alpha = 0.2f)
                        ),
                        //取消隐藏滑块后的缺口
                        thumbTrackGapSize = 0.dp,
                        // 关闭尾部小圆点
                        drawStopIndicator = null
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    formatTime(playbackState.currentPosition),
                    style = MaterialTheme.typography.bodySmall,
                    color = animatedThemeColor
                )
                Text(
                    formatTime(playbackState.duration),
                    style = MaterialTheme.typography.bodySmall,
                    color = animatedThemeColor
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
                modifier = Modifier.size(48.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_round_skip_previous_24),
                    contentDescription = "上一首",
                    tint = animatedThemeColor,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(Modifier.width(24.dp))

            IconButton(
                onClick = { playerViewModel.playPause() },
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    painter = painterResource(id = if (playbackState.isPlaying) {
                        R.drawable.ic_mini_player_pause
                    } else {
                        R.drawable.ic_mini_player_play
                    }),
                    null,
                    modifier = Modifier.size(38.dp),
                    tint = animatedThemeColor
                )
            }

            Spacer(Modifier.width(24.dp))

            IconButton(
                onClick = { playerViewModel.skipToNext() },
                modifier = Modifier.size(48.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_round_skip_next_24),
                    contentDescription = "下一首",
                    tint = animatedThemeColor,
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        // ---------- 底部五按钮 ----------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = { playerViewModel.toggleRepeatMode() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_player_circle),
                    contentDescription = "播放模式",
                    tint = animatedThemeColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = { /* 定时逻辑 */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sleep_timer),
                    contentDescription = "定时",
                    tint = animatedThemeColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(onClick = { /* 音效 */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_audio_effect),
                    contentDescription = "音效",
                    tint = animatedThemeColor,
                    modifier = Modifier.size(24.dp)
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
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = { /* 菜单 */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_player_more),
                    contentDescription = "菜单",
                    tint = animatedThemeColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun LyricsPage(
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
) {
    val lyricsState by playerViewModel.lyricsState.collectAsState()

    val hasTranslation =
        lyricsState.lyrics
            ?.lines
            ?.any { it.translation != null }
            == true

    var showTranslation by remember { mutableStateOf(true) }

    Column(modifier = modifier) {
        LyricsView(
            viewModel = playerViewModel,
            showTranslation = showTranslation && hasTranslation,
            modifier = Modifier.weight(1f),
        )
    }
}