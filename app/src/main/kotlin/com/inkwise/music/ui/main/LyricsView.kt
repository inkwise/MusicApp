package com.inkwise.music.ui.main

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                color = if (isHighlighted) {
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
/*
@Composable
fun LyricsView(
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
    showTranslation: Boolean, // 👈 外部控制
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
    // 用户停止滚动 1 秒后，恢复自动回中
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
    // 自动回中（只由高亮行变化触发）
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
    // UI
    // ------------------------------------------------
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
    ) {
        itemsIndexed(lyrics) { index, line ->
            val isHighlighted = highlight?.lineIndex == index

            val animatedFontSize by animateFloatAsState(
                targetValue = if (isHighlighted) 30f else 20f,
                label = "lyrics_font_size",
            )

            val animatedAlpha by animateFloatAsState(
                targetValue = if (isHighlighted) 0.82f else 0.5f,
                label = "lyrics_alpha",
            )

            Text(
                text = line.text,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
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
                color = Color.Black.copy(alpha = animatedAlpha),
                fontSize = animatedFontSize.sp,
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}

*/
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


@Composable
fun LyricsView(
    viewModel: PlayerViewModel,
    showTranslation: Boolean, // 👈 外部控制是否显示翻译
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
    // 用户停止滚动 1 秒后，恢复自动回中
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
    // 自动回中（只由高亮行变化触发）
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
    // UI
    // ------------------------------------------------
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
    ) {
        itemsIndexed(lyrics) { index, line ->
            val isHighlighted = highlight?.lineIndex == index

            val animatedFontSize by animateFloatAsState(
                targetValue = if (isHighlighted) 30f else 20f,
                label = "lyrics_font_size",
            )

            val animatedAlpha by animateFloatAsState(
                targetValue = if (isHighlighted) 0.82f else 0.5f,
                label = "lyrics_alpha",
            )

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
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
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                // ----------------------------
                // 原文歌词
                // ----------------------------
                Text(
                    text = line.text,
                    color = Color.Black.copy(alpha = animatedAlpha),
                    fontSize = animatedFontSize.sp,
                    fontWeight =
                        if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                )

                // ----------------------------
                // 翻译歌词（外部 Boolean 控制）
                // ----------------------------
                if (showTranslation && line.translation != null) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = line.translation,
                        color = Color.Black.copy(alpha = animatedAlpha * 0.75f),
                        fontSize = (animatedFontSize * 0.6f).sp,
                        fontWeight = FontWeight.Normal,
                    )
                }
            }
        }
    }
}