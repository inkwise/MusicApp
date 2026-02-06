package com.inkwise.music.ui.main
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
fun LyricsView(
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    val lyricsState by viewModel.lyricsState.collectAsState()
    val lyrics = lyricsState.lyrics?.lines.orEmpty()
    val highlight = lyricsState.highlight

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var userScrolling by remember { mutableStateOf(false) }
    var isProgrammaticScroll by remember { mutableStateOf(false) }

    /* ------------------------------------------------ */
    /* 监听用户手动滚动                                   */
    /* ------------------------------------------------ */
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { scrolling ->
                if (scrolling && !isProgrammaticScroll) {
                    userScrolling = true
                }
            }
    }

    /* ------------------------------------------------ */
    /* 用户停止滚动 1 秒后，恢复自动回中                    */
    /* ------------------------------------------------ */
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { scrolling ->
                if (!scrolling && userScrolling && !isProgrammaticScroll) {
                    delay(1_000)
                    userScrolling = false
                }
            }
    }

    /* ------------------------------------------------ */
    /* 自动回中（只由高亮行变化触发）                        */
    /* ------------------------------------------------ */
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

    /* ------------------------------------------------ */
    /* UI                                               */
    /* ------------------------------------------------ */
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState
    ) {
        itemsIndexed(lyrics) { index, line ->
            val isHighlighted = highlight?.lineIndex == index

            val animatedFontSize by animateFloatAsState(
                targetValue = if (isHighlighted) 30f else 20f,
                label = "lyrics_font_size"
            )

            val animatedAlpha by animateFloatAsState(
                targetValue = if (isHighlighted) 0.82f else 0.5f,
                label = "lyrics_alpha"
            )

            Text(
                text = line.text,
                modifier = Modifier
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
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

/* ------------------------------------------------ */
/* 慢速滚动到居中（不使用 animationSpec）              */
/* ------------------------------------------------ */
private suspend fun slowScrollToCenter(
    listState: LazyListState,
    index: Int
) {
    val layoutInfo = listState.layoutInfo
    val viewportCenter = layoutInfo.viewportSize.height / 2

    val itemInfo = layoutInfo.visibleItemsInfo
        .find { it.index == index }

    val targetOffset = if (itemInfo != null) {
        val itemCenter = itemInfo.offset + itemInfo.size / 2
        itemCenter - viewportCenter
    } else {
        null
    }

    if (targetOffset == null) {
        listState.scrollToItem(
            index,
            -viewportCenter
        )
        return
    }

    // 👇 手动分段慢滚
    val steps = 30          // 越大越慢
    val stepOffset = targetOffset / steps

    repeat(steps) {
        listState.scrollBy(stepOffset.toFloat())
        delay(16L)          // ~60fps
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
    // 是否展开（Expanded / PartiallyExpanded 都算）
    val sheetVisible = sheetState.currentValue != SheetValue.Hidden
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

    BackHandler(enabled = sheetVisible) {
        scope.launch {
            when {
                // ① 在第二页 → 回第一页
                pagerState.currentPage > 0 -> {
                    pagerState.animateScrollToPage(0)
                }

                // ② 在第一页 → 收起 Sheet
                else -> {
                    sheetState.partialExpand()
                }
            }
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
        },
    ) {
        MainScreen2()
    }
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
	val pagerNestedScrollConnection = PagerDefaults.nestedScrollConnection(pagerState)
    Box(
        modifier =
            modifier
                .fillMaxSize(),
    ) {
        // 背景图片 + 高斯模糊
		AndroidView(
        modifier =
            Modifier
                .fillMaxSize(),
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                rotation = 180f
            }
        },
        update = { imageView ->
            if (coverUri != null) {
                Glide.with(imageView)
                    .load(coverUri)
                    .transform(
                        jp.wasabeef.glide.transformations.BlurTransformation(
                            40,   // radius
                            20     // sampling（越大越省性能）
                        )
                    )
                    .transform(
	        BlurTransformation(40, 20) // 最终效果
	    )
                    .into(imageView)
            } else {
              //  imageView.setImageDrawable(null)
            }
        }
    )
    
    	//毛玻璃
    	Box(
		    modifier = Modifier
		        .fillMaxSize()
		        .blur(10.dp)
		        .background(Color.White.copy(alpha = 0.25f))
		)
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
            modifier = Modifier.fillMaxSize(),
            // 👇 手势限制可以放松
            //     userScrollEnabled = expandProgress > 0.3f
        ) { page ->
            when (page) {
                0 -> {
                    BottomDrawerContent(pagerState = pagerState)
                }

                1 -> {
                    PlayQueueBottomSheet(
                        playerViewModel = playerViewModel,
                        pagerNestedScrollConnection = pagerNestedScrollConnection
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottomDrawerContent(
    pagerState: PagerState,
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
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
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
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = currentSong?.artist ?: "@inkwise",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
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
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        //.size(64.dp)
                                        .padding(30.dp)              // 👈 用内边距控制大小
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
        Column(modifier = Modifier.padding(top = 8.dp)) {
            Slider(
                value =
                    if (playbackState.duration > 0) {
                        playbackState.currentPosition.toFloat() / playbackState.duration
                    } else {
                        0f
                    },
                onValueChange = { progress ->
                    playerViewModel.seekTo(
                        (progress * playbackState.duration).toLong(),
                    )
                },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    com.inkwise.music.ui.local
                        .formatTime(playbackState.currentPosition),
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    com.inkwise.music.ui.local
                        .formatTime(playbackState.duration),
                    style = MaterialTheme.typography.bodySmall,
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
            IconButton(onClick = { playerViewModel.skipToPrevious() }) {
                Icon(Icons.Default.SkipPrevious, null, modifier = Modifier.size(36.dp))
            }

            Spacer(Modifier.width(24.dp))

            FilledIconButton(
                onClick = { playerViewModel.playPause() },
                modifier = Modifier.size(64.dp),
            ) {
                Icon(
                    if (playbackState.isPlaying) {
                        Icons.Default.Pause
                    } else {
                        Icons.Default.PlayArrow
                    },
                    null,
                    modifier = Modifier.size(36.dp),
                )
            }

            Spacer(Modifier.width(24.dp))

            IconButton(onClick = { playerViewModel.skipToNext() }) {
                Icon(Icons.Default.SkipNext, null, modifier = Modifier.size(36.dp))
            }
        }

        // ---------- 底部五按钮 ----------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = { playerViewModel.toggleRepeatMode() }) {
                Icon(Icons.Default.Repeat, "播放模式")
            }
            IconButton(onClick = { /* 定时 */ }) {
                Icon(Icons.Default.Timer, "定时")
            }
            IconButton(onClick = { /* 音效 */ }) {
                Icon(Icons.Default.Equalizer, "音效")
            }
            IconButton(onClick = {
            	scope.launch {
                	pagerState.animateScrollToPage(1)
                }
            }) {
                Icon(Icons.Default.QueueMusic, "播放队列")
            }
            IconButton(onClick = { /* 菜单 */ }) {
                Icon(Icons.Default.MoreVert, "菜单")
            }
        }
    }
}
