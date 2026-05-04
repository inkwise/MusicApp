package com.inkwise.music.ui.main

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkwise.music.data.model.LyricLine
import com.inkwise.music.ui.player.PlayerViewModel
import kotlin.math.abs

// ── Display mode enum ────────────────────────────────────────────────

enum class LyricsDisplayMode {
    /** Only show current line centered */
    OnlyCurrentLine,
    /** Expand all lines with auto-scroll (default) */
    ExpandDocument,
    /** Always show all lines */
    Always
}

// ── MiniLyricsView2 (2-line slide animation) ─────────────────────────

@Composable
fun MiniLyricsView2(
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
) {
    val lyricsState by viewModel.lyricsState.collectAsState()
    val lyrics = lyricsState.lyrics?.lines.orEmpty()
    val currentIndex = lyricsState.highlight?.lineIndex ?: 0

    val currentLine = lyrics.getOrNull(currentIndex)?.text.orEmpty()
    val nextLine = lyrics.getOrNull(currentIndex + 1)?.text.orEmpty()

    val offsetY = remember { Animatable(0f) }

    LaunchedEffect(currentIndex) {
        offsetY.snapTo(0f)
        offsetY.animateTo(targetValue = -30f, animationSpec = tween(1200))
        offsetY.snapTo(0f)
    }

    Box(
        modifier = modifier.fillMaxWidth().height(30.dp).clipToBounds(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.offset(y = offsetY.value.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = currentLine, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            Text(text = nextLine, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}

// ── MiniLyricsView (compact list) ────────────────────────────────────

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
    val lineHeight = 28.dp
    var containerHeight by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val fadeHeightDp = 5.dp
    val fadeHeightPx = with(density) { fadeHeightDp.toPx() }

    Box(modifier = modifier.onSizeChanged { containerHeight = it.height }) {
        if (containerHeight > 0) {
            val centerPadding = with(density) { ((containerHeight.toDp() / 2) - (lineHeight / 2)).coerceAtLeast(0.dp) }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    .drawWithContent {
                        drawContent()
                        val height = size.height
                        val gradient = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0f to Color.Transparent,
                                fadeHeightPx / height to Color.Black,
                                1f - (fadeHeightPx / height) to Color.Black,
                                1f to Color.Transparent,
                            ),
                        )
                        drawRect(brush = gradient, blendMode = BlendMode.DstIn)
                    },
            ) {
                LazyColumn(state = listState, contentPadding = PaddingValues(vertical = centerPadding)) {
                    itemsIndexed(items = lyrics, key = { i, _ -> i }) { index, line ->
                        val isHighlighted = highlight?.lineIndex == index
                        Text(
                            text = line.text,
                            modifier = Modifier.fillMaxWidth(),
                            color = if (isHighlighted) animatedThemeColor
                                    else animatedThemeColor.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                }
            }

            LaunchedEffect(highlight?.lineIndex) {
                highlight?.lineIndex?.let { index ->
                    if (index in lyrics.indices) {
                        listState.animateScrollToItem(index)
                    }
                }
            }
        }
    }
}

// ── LyricLineItem ────────────────────────────────────────────────────

@Composable
fun LyricLineItem(
    line: LyricLine,
    isHighlighted: Boolean,
    animatedThemeColor: Color,
    showTranslation: Boolean,
    alpha: Float,
    fontSize: Int = 20,
    isBold: Boolean = false,
    isCentered: Boolean = true,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Text(
            text = line.text,
            color = if (isHighlighted) animatedThemeColor.copy(alpha = alpha)
                    else Color.Black.copy(alpha = alpha),
            fontSize = fontSize.sp,
            fontWeight = if (isHighlighted || isBold) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = if (isCentered) TextAlign.Center else TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
        )

        if (showTranslation && line.translation != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = line.translation!!,
                color = if (isHighlighted) MaterialTheme.colorScheme.primary.copy(alpha = alpha * 0.7f)
                        else Color.Black.copy(alpha = alpha * 0.75f),
                fontSize = (fontSize * 0.7f).sp,
                textAlign = if (isCentered) TextAlign.Center else TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Full LyricsView (with display modes) ────────────────────────────

@Composable
fun LyricsView(
    viewModel: PlayerViewModel,
    animatedThemeColor: Color,
    showTranslation: Boolean,
    modifier: Modifier = Modifier,
    displayMode: LyricsDisplayMode = LyricsDisplayMode.ExpandDocument,
    fontSize: Int = 20,
    isBold: Boolean = false,
    isCentered: Boolean = true,
) {
    val lyricsState by viewModel.lyricsState.collectAsState()
    val lyrics = lyricsState.lyrics?.lines.orEmpty()
    val highlight = lyricsState.highlight
    val currentIndex = highlight?.lineIndex ?: 0

    // ── OnlyCurrentLine mode ───────────────────────────────────
    if (displayMode == LyricsDisplayMode.OnlyCurrentLine) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            val line = lyrics.getOrNull(currentIndex)
            if (line != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = line.text,
                        color = animatedThemeColor,
                        fontSize = fontSize.sp,
                        fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                    if (showTranslation && line.translation != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = line.translation!!,
                            color = animatedThemeColor.copy(alpha = 0.7f),
                            fontSize = (fontSize * 0.7f).sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                Text(
                    text = "No Lyrics",
                    color = Color.Gray,
                    fontSize = 14.sp,
                )
            }
        }
        return
    }

    // ── ExpandDocument / Always mode ────────────────────────────
    val listState = rememberLazyListState()
    val fadeHeightDp = 33.dp
    val fadeHeightPx = with(LocalDensity.current) { fadeHeightDp.toPx() }

    // Scroll synchronization: smoothly scroll current line to 1/3 from top
    LaunchedEffect(highlight?.lineIndex) {
        val index = highlight?.lineIndex ?: return@LaunchedEffect
        if (index !in lyrics.indices) return@LaunchedEffect

        val currentLineTime = lyrics[index].timeMs
        val nextLineTime = lyrics.getOrNull(index + 1)?.timeMs

        // Dynamic duration: match the time gap to the next lyric line
        val dynamicDuration =
            if (nextLineTime != null) {
                (nextLineTime - currentLineTime).toInt().coerceIn(10, 1200)
            } else {
                500
            }

        val layoutInfo = listState.layoutInfo
        val visibleItem = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }

        if (visibleItem != null) {
            // Scroll so the current line sits at 1/3 from the top (like Salt Player)
            val viewportStart = layoutInfo.viewportStartOffset
            val viewportHeight = layoutInfo.viewportEndOffset - viewportStart
            val targetOffset = viewportStart + viewportHeight / 3

            val itemCenter = visibleItem.offset + visibleItem.size / 2
            val scrollDelta = itemCenter - targetOffset

            listState.animateScrollBy(
                scrollDelta.toFloat(),
                animationSpec = tween(
                    durationMillis = dynamicDuration,
                    easing = LinearOutSlowInEasing,
                ),
            )
        } else {
            listState.scrollToItem(index)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val boxHeight = maxHeight
        // Top padding: 1/3 of viewport so current line appears at 1/3 from top
        val estimatedLineHeightDp = (fontSize + 12).dp
        val topPadding = (boxHeight / 3 - estimatedLineHeightDp / 2).coerceAtLeast(0.dp)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    val height = size.height
                    val gradient = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            fadeHeightPx / height to Color.Black,
                            1f - (fadeHeightPx / height) to Color.Black,
                            1f to Color.Transparent,
                        ),
                    )
                    drawRect(brush = gradient, blendMode = BlendMode.DstIn)
                },
        ) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    top = topPadding,
                    bottom = boxHeight * 2 / 3, // allow last line to reach 1/3 position
                ),
                modifier = Modifier.fillMaxSize(),
            ) {
                itemsIndexed(
                    items = lyrics,
                    key = { i, _ -> i },
                ) { index, line ->
                    val isHighlighted = index == currentIndex
                    val dist = abs(index - currentIndex)

                    // Distance-based alpha: current=1.0, near=0.7, far=0.4
                    val targetAlpha = when {
                        isHighlighted -> 1f
                        dist <= 3 -> 0.7f
                        else -> 0.4f
                    }
                    val alpha by animateFloatAsState(
                        targetValue = targetAlpha,
                        animationSpec = tween(300),
                        label = "lyrics_alpha",
                    )

                    LyricLineItem(
                        line = line,
                        animatedThemeColor = animatedThemeColor,
                        isHighlighted = isHighlighted,
                        showTranslation = showTranslation,
                        alpha = alpha,
                        fontSize = fontSize,
                        isBold = isBold,
                        isCentered = isCentered,
                    )
                }
            }
        }
    }
}
