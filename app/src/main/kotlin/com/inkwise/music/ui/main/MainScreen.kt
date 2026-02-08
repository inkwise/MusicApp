package com.inkwise.music.ui.main
import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.toArgb
import androidx.palette.graphics.Palette // 确保 build.gradle 有 implementation("androidx.palette:palette-ktx:1.0.0")
import coil.compose.AsyncImage
import androidx.compose.ui.unit.DpSize

import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import jp.wasabeef.glide.transformations.BlurTransformation
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.activity.compose.BackHandler
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asComposeRenderEffect
import android.graphics.RenderEffect
import android.graphics.Shader
import kotlinx.coroutines.delay
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.input.nestedscroll.NestedScrollSource

import androidx.compose.foundation.Image // 必须手动引入，防止和 Icon 混淆
import androidx.compose.runtime.remember
import coil.compose.rememberAsyncImagePainter // 核心报错修正

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween

import androidx.compose.foundation.Image // 必须手动引入，防止和 Icon 混淆
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.graphicsLayer

import androidx.compose.runtime.remember
import coil.compose.rememberAsyncImagePainter // 核心报错修正
import coil.request.CachePolicy

import androidx.compose.runtime.key
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
// 注意：drawRect 和 drawContent 是在 DrawScope 作用域内的，通常不需要单独 import
// 但确保你引入了下面这个
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas

// 动画核心
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
// 如果你使用了 alpha 渐变逻辑，还需要这个
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.inkwise.music.ui.home.HomeScreen
import com.inkwise.music.data.model.Song
import com.inkwise.music.ui.local.LocalSongsScreen
import com.inkwise.music.ui.cloud.CloudSongsScreen
import com.inkwise.music.ui.queue.PlayQueueBottomSheet
import com.inkwise.music.ui.theme.LocalAppDimens
import com.inkwise.music.R
import android.widget.ImageView
import com.bumptech.glide.Glide
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.interaction.MutableInteractionSource

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset

import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import androidx.hilt.navigation.compose.hiltViewModel
import com.inkwise.music.ui.player.PlayerViewModel
import androidx.compose.foundation.ExperimentalFoundationApi

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.itemsIndexed

import androidx.compose.foundation.lazy.rememberLazyListState

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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = if (isHighlighted) {
                    animatedThemeColor
                } else {
                    animatedThemeColor.copy(alpha = 0.5f)
                },
                fontSize = 14.sp,
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}

@Composable
fun LyricsView(
    viewModel: PlayerViewModel,
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
fun ReboundHorizontalDrag(
    onPrev: () -> Unit,
    onNext: () -> Unit,
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val playQueue by playerViewModel.playQueue.collectAsState()
    val currentIndex by playerViewModel.currentIndex.collectAsState()
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }

    val triggerDistance = 120f // 触发距离（px）
    val triggerVelocity = 1200f // 触发速度（px/s）

    // 将位移距离转为布尔值
    val isVisible by remember {
        derivedStateOf {
            offsetX.value > 0f
        }
    }
    val isVisible2 by remember {
        derivedStateOf {
            offsetX.value < 0f
        }
    }

    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                // .background(Color.Red)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state =
                        rememberDraggableState { delta ->
                            scope.launch {
                                offsetX.snapTo(offsetX.value + delta)
                            }
                        },
                    onDragStopped = { velocity ->
                        val drag = offsetX.value

                        val shouldPrev =
                            drag > triggerDistance ||
                                velocity > triggerVelocity

                        val shouldNext =
                            drag < -triggerDistance ||
                                velocity < -triggerVelocity

                        if (shouldPrev) {
                            onPrev()
                        } else if (shouldNext) {
                            onNext()
                        }

                        // 无论如何都回中
                        scope.launch {
                            offsetX.animateTo(
                                targetValue = 0f,
                                animationSpec =
                                    spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        // 决定回去的力度，StiffnessLow 会更柔和
                                        // stiffness = Spring.StiffnessMedium
                                        stiffness = Spring.StiffnessMedium,
                                    ),
                                initialVelocity = velocity,
                            )
                        }
                    },
                ),
        contentAlignment = Alignment.Center, // 确保内容整体居中
    ) {
        // 这里拿到的 maxWidth 是该布局能占据的最大宽度
        val halfWidth = maxWidth * 0.5f

        Row(
            modifier =
                Modifier
                    .fillMaxHeight()
                    // 总宽度：3个 50% = 1.5倍
                    .width(halfWidth * 3)
                    // 关键点 2：使用 wrapContentWidth(unbounded = true)
                    // 这允许 Row 的宽度超过父布局的最大约束而不被强制压缩
                    .wrapContentWidth(align = Alignment.CenterHorizontally, unbounded = true),
            // 关键：为了让中间的布局居中，我们需要向左偏移半个组件的宽度（即 25% 的总显示宽度）
            // .offset(x = -halfWidth * 0.5f),
            // verticalAlignment = Alignment.CenterVertically
        ) {
            val itemModifier = Modifier.width(halfWidth).fillMaxHeight()

            // ⬅ 上一首
            SongPage(
                text = "上一首",
                song = playQueue.getOrNull(currentIndex - 1),
                // enabled = currentIndex > 0,
                modifier = itemModifier,
                alignRight = true,
                isVisible = isVisible,
            )

            // 🎵 当前
            SongPage(
                text = " ",
                song = playQueue.getOrNull(currentIndex),
                //    enabled = true,
                modifier = itemModifier,
                alignRight = false,
                isVisible = true,
            )

            // ➡ 下一首
            SongPage(
                text = "下一首",
                song = playQueue.getOrNull(currentIndex + 1),
                //    enabled = currentIndex < playQueue.lastIndex,
                modifier = itemModifier,
                alignRight = false,
                isVisible = isVisible2,
            )
        }
    }
}

@Composable
fun SongPage(
    text: String,
    modifier: Modifier,
    song: Song?,
    alignRight: Boolean = false,
    isVisible: Boolean,
) {
    Column(
        modifier =
            modifier
                .fillMaxHeight(),
        horizontalAlignment =
            if (alignRight) {
                Alignment.End
            } else {
                Alignment.Start
            },
        verticalArrangement = Arrangement.Center,
    ) {
        // Text / Icon / whatever

        if (song != null && isVisible) {
            Text(
                text = song.title,
                maxLines = 1,
            )
            Text(
                text = text,
                maxLines = 1,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val dimens = LocalAppDimens.current
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()

    var expandProgress by remember { mutableStateOf(0f) }
    var initialOffset by remember { mutableStateOf<Float?>(null) }
    val density = LocalDensity.current
    val peekHeightPx = with(density) { dimens.sheetPeekHeightDp.toPx() }
    // val pagerState = rememberPagerState { 2 }
    val savedPage = rememberSaveable { mutableIntStateOf(0) }
    val sheetState = scaffoldState.bottomSheetState

    val pagerState =
        rememberPagerState(
            initialPage = savedPage.intValue,
            pageCount = { 2 },
        )
    LaunchedEffect(pagerState.currentPage) {
        savedPage.intValue = pagerState.currentPage
    }
    LaunchedEffect(sheetState.currentValue) {
        if (sheetState.currentValue == SheetValue.Expanded) {
            pagerState.scrollToPage(savedPage.intValue)
        }
    }
    // 监听 BottomSheet 拖拽
    LaunchedEffect(scaffoldState.bottomSheetState) {
        snapshotFlow {
            scaffoldState.bottomSheetState.requireOffset()
        }.collect { offset: Float ->

            // 第一次记录“收起时”的 offset
            if (initialOffset == null) {
                initialOffset = offset
            }

            val start = initialOffset ?: return@collect

            expandProgress =
                ((start - offset) / peekHeightPx)
                    .coerceIn(0f, 1f)
        }
    }




    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = dimens.sheetPeekHeightDp,
        sheetDragHandle = null,
        // sheetContainerColor = Color.Transparent,
        sheetContent = {
            Box {
                // 背景播放器：展开时显示
                playerScreen(
                    pagerState = pagerState,
                    modifier = Modifier.alpha(expandProgress),
                )

                // 控制栏：收起时显示
                controlContent(
                    modifier = Modifier.alpha(1f - expandProgress),
                    onClick = {
                        scope.launch {
                            scaffoldState.bottomSheetState.expand()
                        }
                    },
                    showPlayQueue = {
                        scope.launch {
                            // 展开 Sheet
                            scaffoldState.bottomSheetState.expand()
                            // 直接切第二页
                            pagerState.animateScrollToPage(1)
                        }
                    },
                )
            }
            
            // --- 核心：将 BackHandler 放在这里 ---
        // 使用 currentValue 配合 targetValue 确保在动画过程中也能精准拦截
        val isExpanded = sheetState.currentValue == SheetValue.Expanded || sheetState.targetValue == SheetValue.Expanded
        val isAtSecondPage = pagerState.currentPage > 0
        
        BackHandler(enabled = isExpanded || isAtSecondPage) {
            scope.launch {
                if (pagerState.currentPage > 0) {
                    // 如果在第二页，先回第一页
                    pagerState.animateScrollToPage(0)
                } else {
                    // 如果在第一页且展开，则收起
                    sheetState.partialExpand()
                }
            }
        }
        },
    ) {
        MainScreen2(            sheetState = sheetState,
            pagerState = pagerState,
            scope = scope)
    }
    
    // 1. 精确定义什么时候需要拦截返回键
	/*val shouldInterceptBack = sheetState.currentValue == SheetValue.Expanded || pagerState.currentPage > 0
	
	BackHandler(enabled = shouldInterceptBack) {
	    scope.launch {
	        when {
	            // ① 如果 Pager 在第二页（播放队列），先回第一页（播放器主页）
	            pagerState.currentPage > 0 -> {
	                pagerState.animateScrollToPage(0)
	            }
	            // ② 如果已经在第一页且是展开状态，则收起（折叠）Sheet
	            sheetState.currentValue == SheetValue.Expanded -> {
	                sheetState.partialExpand()
	            }
	        }
	    }
	}*/
}

// 播放器页面
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun playerScreen(
    modifier: Modifier,
    pagerState: PagerState,
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val playbackState by playerViewModel.playbackState.collectAsState()
    val currentSong = playbackState.currentSong
    val coverUri = currentSong?.albumArt
    // 1. 必须先定义 scope
    val scope = rememberCoroutineScope()
	
    // 1. 创建一个嵌套滚动连接器，专门处理“卡住”的情况
    val fixStuckConnection =
        remember {
            object : NestedScrollConnection {
                // 当用户松开手，且所有子组件（LazyColumn）完成惯性滑动后触发
                override suspend fun onPostFling(
                    consumed: Velocity,
                    available: Velocity,
                ): Velocity {
                    // 如果 Pager 停在半路（偏移量不为 0）
                    if (pagerState.currentPageOffsetFraction != 0f) {
                        // 强制让 Pager 滚动到它“想去”的那一页
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.targetPage)
                        }
                    }
                    return super.onPostFling(consumed, available)
                }
            }
        }
    // 自定义 Fling 行为
    // 使用这种方式定义，参数名更准确
    val flingBehavior =
        PagerDefaults.flingBehavior(
            state = pagerState,
            // 关键：只要滑动超过 15% 就视为翻页，防止回弹
            snapPositionalThreshold = 0.08f,
            // 这里的 snapAnimationSpec 对应松手后的吸附动画
            snapAnimationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
        )
    // --- 1. 颜色状态定义 ---
	val defaultColor = Color.DarkGray
	var themeColor by remember { mutableStateOf(defaultColor) }
	
	// 动态颜色过渡动画
	val animatedThemeColor by animateColorAsState(
	    targetValue = themeColor,
	    animationSpec = tween(600),
	    label = "ColorAnimation"
	)
	
	val context = LocalContext.current
	

    Box(
        modifier =
            modifier
                .fillMaxSize(),
    ) {
    	//主题色获取
    	// 我们用一个 0 尺寸的 AsyncImage 来偷偷提取颜色
AsyncImage(
    model = ImageRequest.Builder(context)
        .data(coverUri)
        .allowHardware(false) // 必须关闭硬件加速才能拿 Bitmap
        .size(150)            // 极小尺寸提速
        .build(),
    contentDescription = null,
    modifier = Modifier.size(1.dp).alpha(0f), // 隐藏它
    onSuccess = { success ->
        // 修正 Unresolved reference 'result' 和 'bitmap'
        val drawable = success.result.drawable
        if (drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap
            // 修正 Palette 命名冲突：明确使用 androidx.palette.graphics.Palette
            androidx.palette.graphics.Palette.from(bitmap).generate { palette ->
                palette?.let { p ->
                    // 尝试取几种颜色，按优先级排序
                    val colorInt = p.getVibrantColor(
                        p.getMutedColor(
                            p.getDominantColor(defaultColor.toArgb())
                        )
                    )
                    
                    // ... 在获取颜色后
					val extractedColor = Color(colorInt)
					
					// 将提取到的颜色与黑色 (Black) 进行混合
					// 0.3f 代表混合 30% 的黑色，70% 的原色。数值越大，颜色越深。
					themeColor = lerp(extractedColor, Color.Black , 0.5f)
                    
                }
            }
        }
    }
)
        // 背景图片 + 高斯模糊

        // 2. 在 Box 中通过 Image 渲染，并添加强制重绘逻辑
        AnimatedContent(
            targetState = coverUri,
            transitionSpec = {
                // 定义切歌时的过渡效果：淡入淡出，时长 600ms
                fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(600))
            },
            label = "BackgroundAnimation",
        ) { targetUri ->
            // 这里的 targetUri 就是当前最新的图片地址
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .blur(radius = 200.dp), // 在动画容器内部应用模糊
            ) {
                androidx.compose.foundation.Image(
                    painter =
                        rememberAsyncImagePainter(
                            model =
                                ImageRequest
                                    .Builder(LocalContext.current)
                                    .data(targetUri)
                                    .size(100) // 强制小图模式，极速加载
                                    .build(),
                        ),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                    	.fillMaxSize()
                    	// Y 轴缩放为 -1 表示垂直翻转
                    	.graphicsLayer(scaleY = -1f),
                )

                // 遮罩层也放在里面，跟随动画
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.8f)),
                )
            }
        }
        // 你原本的播放器内容（盖在上面）
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
        ) {
        }
        VerticalPager(
            state = pagerState,
            key = { it },
            modifier =
                Modifier
                    .fillMaxSize()
                    .nestedScroll(fixStuckConnection),
            // 拦截并修复状态,
            beyondViewportPageCount = 1, // 预加载相邻页，防止卡顿处出现空白
            flingBehavior = flingBehavior, // 应用自定义行为
        ) { page ->
            when (page) {
                0 -> {
                    BottomDrawerContent(pagerState = pagerState,animatedThemeColor=animatedThemeColor)
                }

                1 -> {
                    PlayQueueBottomSheet(
                        playerViewModel = playerViewModel,
                    )
                }
            }
        }
    }
}

// 手柄区域
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun controlContent(
    modifier: Modifier,
    onClick: () -> Unit,
    showPlayQueue: () -> Unit,
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(100.dp)
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
                .height(56.dp)
                .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(64.dp)
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

@Composable
fun SongItem(
    title: String,
    height: Dp,
    modifier: Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxHeight()
                .width(100.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen2(
    sheetState: SheetState,      // 接收状态
    pagerState: PagerState,
    scope: CoroutineScope,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    // 同步 ViewModel 和侧边栏状态
    LaunchedEffect(uiState.sidebarOpen) {
        if (uiState.sidebarOpen) {
            drawerState.open()
        } else {
            drawerState.close()
        }
    }

    // 监听侧边栏关闭
    LaunchedEffect(drawerState.isClosed) {
        if (drawerState.isClosed && uiState.sidebarOpen) {
            viewModel.closeSidebar()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
            ) {
                SidebarContent(
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // 避免重复导航
                            launchSingleTop = true
                        }
                        viewModel.closeSidebar()
                    },
                )
            }
        },
        gesturesEnabled = true,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("音乐播放器") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.toggleSidebar() }) {
                            Icon(Icons.Default.Menu, "菜单")
                        }
                    },
                    actions = {
                        // 添加一个按钮来打开底部抽屉
                        IconButton(onClick = { viewModel.toggleBottomDrawer() }) {
                            Icon(Icons.Default.MusicNote, "播放器")
                        }
                    },
                )
            },
        ) { padding ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
            ) {
                // 导航内容
                NavHost(
                    navController = navController,
                    startDestination = "home",
                ) {
                    composable("home") {
                        HomeScreen(
                            onNavigateToLocal = { navController.navigate("local") },
                            onNavigateToCloud = { navController.navigate("cloud") },
                        )
                    }
                    composable("local") {
                        LocalSongsScreen()
                    }
                    composable("cloud") {
                        CloudSongsScreen()
                    }
                }
                
                    // --- 关键：在这里定义 BackHandler ---
    // 使用 targetValue 能更早感知到“正在展开”的状态，比 currentValue 更灵敏
    val shouldIntercept = sheetState.targetValue == SheetValue.Expanded || pagerState.currentPage > 0

    BackHandler(enabled = shouldIntercept) {
        scope.launch {
            if (pagerState.currentPage > 0) {
                pagerState.animateScrollToPage(0)
            } else {
                sheetState.partialExpand()
            }
        }
    }
            }
        }
    }
    
    
}

// 侧边栏内容
@Composable
fun SidebarContent(
    onNavigate: (String) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxHeight()
                .padding(16.dp),
    ) {
        Text(
            text = "菜单",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 16.dp),
        )

        Divider()

        Spacer(modifier = Modifier.height(16.dp))

        DrawerMenuItem(
            icon = Icons.Default.Home,
            text = "主页",
            onClick = { onNavigate("home") },
        )

        DrawerMenuItem(
            icon = Icons.Default.MusicNote,
            text = "本地音乐",
            onClick = { onNavigate("local") },
        )

        DrawerMenuItem(
            icon = Icons.Default.Cloud,
            text = "云端音乐",
            onClick = { onNavigate("cloud") },
        )

        DrawerMenuItem(
            icon = Icons.Default.Favorite,
            text = "我的收藏",
            onClick = { onNavigate("favorites") },
        )

        DrawerMenuItem(
            icon = Icons.Default.Settings,
            text = "设置",
            onClick = { onNavigate("settings") },
        )
    }
}

@Composable
fun DrawerMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = text)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text)
    }
}

// 底部抽屉组件（带手柄）- 集成播放器

/*
@Composable
fun BottomDrawerContent(
	pagerState: PagerState,
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val playbackState by playerViewModel.playbackState.collectAsState()
    val currentSong = playbackState.currentSong
    var showPlayQueue by remember { mutableStateOf(false) }
	val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "正在播放",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 播放器控制区域
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 封面
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 歌曲信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentSong?.title ?: "未播放",
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1
                )
                Text(
                    text = currentSong?.artist ?: "无",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            // 播放按钮
            IconButton(onClick = { playerViewModel.playPause() }) {
                Icon(
                    if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (playbackState.isPlaying) "暂停" else "播放",
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // 进度条
        Column(modifier = Modifier.padding(top = 16.dp)) {
            Slider(
                value = if (playbackState.duration > 0) {
                    playbackState.currentPosition.toFloat() / playbackState.duration.toFloat()
                } else 0f,
                onValueChange = { progress ->
                    val newPosition = (progress * playbackState.duration).toLong()
                    playerViewModel.seekTo(newPosition)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = com.inkwise.music.ui.local.formatTime(playbackState.currentPosition),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = com.inkwise.music.ui.local.formatTime(playbackState.duration),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 控制按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 随机播放按钮
            IconButton(onClick = { playerViewModel.toggleShuffle() }) {
                Icon(
                    Icons.Default.Shuffle,
                    "随机播放",
                    tint = if (playbackState.shuffleMode) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // 上一曲
            IconButton(onClick = { playerViewModel.skipToPrevious() }) {
                Icon(Icons.Default.SkipPrevious, "上一曲", modifier = Modifier.size(32.dp))
            }

            // 播放/暂停
            FilledIconButton(
                onClick = { playerViewModel.playPause() },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (playbackState.isPlaying) "暂停" else "播放",
                    modifier = Modifier.size(32.dp)
                )
            }

            // 下一曲
            IconButton(onClick = { playerViewModel.skipToNext() }) {
                Icon(Icons.Default.SkipNext, "下一曲", modifier = Modifier.size(32.dp))
            }

            // 循环模式按钮
            IconButton(onClick = { playerViewModel.toggleRepeatMode() }) {
                val (icon, tint) = when (playbackState.repeatMode) {
                    com.inkwise.music.data.model.RepeatMode.ONE ->
                        Icons.Default.RepeatOne to MaterialTheme.colorScheme.primary
                    com.inkwise.music.data.model.RepeatMode.ALL ->
                        Icons.Default.Repeat to MaterialTheme.colorScheme.primary
                    else ->
                        Icons.Default.Repeat to MaterialTheme.colorScheme.onSurfaceVariant
                }
                Icon(icon, "循环", tint = tint)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 播放队列按钮
        TextButton(
            onClick = {
	        	scope.launch {
                	pagerState.animateScrollToPage(1)
                	}
	        },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.QueueMusic, "播放队列")
            Spacer(modifier = Modifier.width(8.dp))
            Text("播放队列")
        }

    }
}

*/

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
         
                      /*  Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
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
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }*/
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
        viewModel = viewModel,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)   // ✅ 明确高度（推荐）
    )
}
	                     
                    }

                    1 -> {
                        // 歌词页（占位）
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            LyricsView(playerViewModel)
                        }
                    }
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
                    com.inkwise.music.ui.local
                        .formatTime(playbackState.currentPosition),
                    style = MaterialTheme.typography.bodySmall,
                    color = animatedThemeColor
                )
                Text(
                    com.inkwise.music.ui.local
                        .formatTime(playbackState.duration),
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
