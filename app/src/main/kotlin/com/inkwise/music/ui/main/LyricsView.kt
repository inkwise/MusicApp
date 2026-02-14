package com.inkwise.music.ui.main

// animateItemPlacement
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.BlendMode
// ExperimentalAnimationApi 注解
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkwise.music.ui.player.PlayerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.spring
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MiniLyricsView(
    viewModel: PlayerViewModel,
    animatedThemeColor: Color,
    modifier: Modifier = Modifier,
) {
    val lyricsState by viewModel.lyricsState.collectAsState()
    val lyrics = lyricsState.lyrics?.lines.orEmpty()
    val highlight = lyricsState.highlight

    val listState = rememberLazyListState()

    // 高亮变化时，自动滚动到中间
    LaunchedEffect(highlight?.lineIndex) {
        val index = highlight?.lineIndex ?: return@LaunchedEffect
        if (index !in lyrics.indices) return@LaunchedEffect

        slowScrollToCenter(listState, index)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
    ) {
        itemsIndexed(lyrics) { index, line ->
            val isHighlighted = highlight?.lineIndex == index

            Text(
                text = line.text,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                color =
                    if (isHighlighted) {
                        animatedThemeColor
                    } else {
                        animatedThemeColor.copy(alpha = 0.5f)
                    },
                fontSize = 8.sp,
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}

// ------------------------------------------------
// 慢速滚动到居中（不使用 animationSpec）
// ------------------------------------------------
private suspend fun slowScrollToCenter(
    listState: LazyListState,
    index: Int,
) {
    val layoutInfo = listState.layoutInfo
    val viewportCenter = layoutInfo.viewportSize.height / 2

    val itemInfo =
        layoutInfo.visibleItemsInfo
            .find { it.index == index }

    val targetOffset =
        if (itemInfo != null) {
            val itemCenter = itemInfo.offset + itemInfo.size / 2
            itemCenter - viewportCenter
        } else {
            null
        }

    if (targetOffset == null) {
        listState.scrollToItem(
            index,
            -viewportCenter,
        )
        return
    }

    // 👇 手动分段慢滚
    val steps = 30 // 越大越慢
    val stepOffset = targetOffset / steps

    repeat(steps) {
        listState.scrollBy(stepOffset.toFloat())
        delay(16L) // ~60fps
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun LyricsView(
    viewModel: PlayerViewModel,
    showTranslation: Boolean,
    modifier: Modifier = Modifier,
) {
    val lyricsState by viewModel.lyricsState.collectAsState()
    val lyrics = lyricsState.lyrics?.lines.orEmpty()
    val highlight = lyricsState.highlight

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var userScrolling by remember { mutableStateOf(false) }
    var isProgrammaticScroll by remember { mutableStateOf(false) }

    // ------------------------------------------------
    // 监听用户手动滚动
    // ------------------------------------------------
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { scrolling ->
                if (scrolling && !isProgrammaticScroll) {
                    userScrolling = true
                }
            }
    }

    // ------------------------------------------------
    // 用户停止滚动 1 秒后恢复自动回中
    // ------------------------------------------------
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { scrolling ->
                if (!scrolling && userScrolling && !isProgrammaticScroll) {
                    delay(1_000)
                    userScrolling = false
                }
            }
    }

    // ------------------------------------------------
    // 自动回中（仅由高亮行变化触发）
    // ------------------------------------------------
    LaunchedEffect(highlight?.lineIndex) {
        if (highlight == null) return@LaunchedEffect
        if (userScrolling) return@LaunchedEffect

        val index = highlight.lineIndex
        if (index !in lyrics.indices) return@LaunchedEffect

        isProgrammaticScroll = true
        try {
            slowScrollToCenter(listState, index)
        } finally {
            isProgrammaticScroll = false
        }
    }

    // ------------------------------------------------
    // 边界遮罩参数
    // ------------------------------------------------
    val fadeHeightDp = 28.dp
    val density = LocalDensity.current
    val fadeHeightPx = with(density) { fadeHeightDp.toPx() }
    val surface = MaterialTheme.colorScheme.surface

    // ------------------------------------------------
    // UI（LazyColumn + drawWithContent 做顶部/底部渐变遮罩）
    // ------------------------------------------------
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    // drawWithContent 绘制内容后再画顶部/底部渐变遮罩，不会阻塞触摸
                    // 1. 必须开启渲染层合成策略，否则 BlendMode 不会作用于整个图层
                    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                    .drawWithContent {
    drawContent()

    val fadeHeightPx = fadeHeightDp.toPx()

    // 顶部：透明 -> 黑
    val topBrush = Brush.verticalGradient(
        colors = listOf(Color.Transparent, Color.Black),
        startY = 0f,
        endY = fadeHeightPx,
    )

    // 底部：黑 -> 透明
    val bottomBrush = Brush.verticalGradient(
        colors = listOf(Color.Black, Color.Transparent),
        startY = size.height - fadeHeightPx,
        endY = size.height,
    )

    // ⚠️ 关键：用 saveLayer，把两次 DstIn 放在同一个图层里
    drawIntoCanvas { canvas ->
        val paint = Paint()
        canvas.saveLayer(size.toRect(), paint)

        // 上遮罩
        drawRect(
            brush = topBrush,
            blendMode = BlendMode.DstIn,
        )

        // 下遮罩
        drawRect(
            brush = bottomBrush,
            blendMode = BlendMode.DstIn,
        )

        canvas.restore()
    }
}
                    ,
            state = listState,
            // contentPadding = PaddingValues(vertical = 8.dp),
            contentPadding = PaddingValues(vertical = 40.dp), // 增加 padding 让第一行也能被“擦除”
        ) {
            itemsIndexed(lyrics, key = { index, _ -> index }) { index, line ->
                val isHighlighted = highlight?.lineIndex == index

                // 透明度动画
                val animatedAlpha by animateFloatAsState(
                    targetValue = if (isHighlighted) 0.95f else 0.5f,
                    label = "lyrics_alpha",
                )

                // 偏移动画（px）
                val offsetX by animateFloatAsState(
                    targetValue = if (isHighlighted) 12f else 0f,
                    label = "lyrics_offset_x",
                )

                val offsetY by animateFloatAsState(
                    targetValue = if (isHighlighted) -6f else 0f,
                    label = "lyrics_offset_y",
                )

                // 每一项支持位置动画与自身尺寸动画
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .graphicsLayer {
                                translationX = offsetX
                                translationY = offsetY
                            }.animateItem(
                                fadeInSpec = null, // 如果不需要淡入动画可设为 null
                                fadeOutSpec = null, // 如果不需要淡出动画可设为 null
                                placementSpec = spring(), // 控制位置变化的动画参数
                            ).animateContentSize() // 项目自身尺寸变化平滑（展开/收缩译文）
                            .clickable {
                                viewModel.seekTo(line.timeMs)
                                scope.launch {
                                    isProgrammaticScroll = true
                                    try {
                                        slowScrollToCenter(listState, index)
                                    } finally {
                                        isProgrammaticScroll = false
                                    }
                                }
                            },
                ) {
                    // ----------------------------
                    // 原文歌词
                    // ----------------------------
                    Text(
                        text = line.text,
                        color =
                            if (isHighlighted) {
                                MaterialTheme.colorScheme.primary.copy(alpha = animatedAlpha)
                            } else {
                                Color.Black.copy(alpha = animatedAlpha)
                            },
                        fontSize = 20.sp,
                        fontWeight =
                            if (isHighlighted) FontWeight.SemiBold else FontWeight.Normal,
                    )

                    // ----------------------------
                    // 翻译歌词：使用 AnimatedVisibility 平滑展开/收缩
                    // ----------------------------
                    AnimatedVisibility(
                        visible = showTranslation && line.translation != null,
                        enter = expandVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = 500f)) + fadeIn(),
                        exit = shrinkVertically(animationSpec = spring(dampingRatio = 1f, stiffness = 800f)) + fadeOut(),
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = line.translation ?: "",
                            color =
                                if (isHighlighted) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = animatedAlpha * 0.7f)
                                } else {
                                    Color.Black.copy(alpha = animatedAlpha * 0.75f)
                                },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}
