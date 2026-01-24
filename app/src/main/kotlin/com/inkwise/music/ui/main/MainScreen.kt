package com.inkwise.music.ui.main

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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset

import kotlin.math.absoluteValue
import kotlin.math.roundToInt

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
/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(){
    val dimens = LocalAppDimens.current
    // ① BottomSheet 状态
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()
    
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = dimens.sheetPeekHeightDp,
        sheetDragHandle = null,
        sheetContainerColor = Color.Transparent,
        sheetContent = {
            Box {
                playerScreen()
                controlContent(
                    onClick = {
                        scope.launch {
                            scaffoldState.bottomSheetState.expand()
                        }
                    }
                )
            }
        },
    ) {
        //主页面
        MainScreen2()
        
    }
}*/
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

            expandProgress = ((start - offset) / peekHeightPx)
            .coerceIn(0f, 1f)
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = dimens.sheetPeekHeightDp,
        sheetDragHandle = null,
        //sheetContainerColor = Color.Transparent,
        sheetContent = {
            Box {

                // 背景播放器：展开时显示
                playerScreen(
                    modifier = Modifier.alpha(expandProgress)
                )

                // 控制栏：收起时显示
                controlContent(
                    modifier = Modifier.alpha(1f - expandProgress),
                    onClick = {
                        scope.launch {
                            scaffoldState.bottomSheetState.expand()
                        }
                    }
                )
             
            }
        }
    ) {
        MainScreen2()
    }
}

data class Song2(
    val id: Int,
    val name: String,
    val artist: String
)

// 测试用的歌曲列表
val mockSongList = listOf(
    Song2(1, "晴天", "周杰伦"),
    Song2(2, "七里香", "周杰伦"),
    Song2(3, "稻香", "周杰伦"),
    Song2(4, "青花瓷", "周杰伦"),
    Song2(5, "夜曲", "周杰伦"),
    Song2(6, "简单爱", "周杰伦"),
    Song2(7, "告白气球", "周杰伦"),
    Song2(8, "等你下课", "周杰伦")
)

//手柄区域
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun controlContent(
    modifier: Modifier,
    onClick: () -> Unit
) {
    var index by remember { mutableIntStateOf(1) }
    val songs =         mockSongList
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(
                indication = null, // 🚫 去掉波纹
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onClick()
            }
    ){
        // 滑动控件
       // MusicPlayerTestScreen()
       SwipeSongSwitcherTest()
        //控制层
        controlContent2()
    }
}
@Composable
fun controlContent2(

    modifier: Modifier = Modifier,
    onIcon1Click: () -> Unit = {},
    onIcon2Click: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // 左侧图片（Glide）
        AndroidView(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp)),
            factory = { context ->
                ImageView(context).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    Glide.with(this)
                        .load(R.drawable.test)
                        .into(this)
                }
            }
        )

        // 中间撑开
        Spacer(modifier = Modifier.weight(1f))

        // 右侧第一个 Icon
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "播放暂停",
            modifier = Modifier
                .size(28.dp)
                .clickable { onIcon1Click() }
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 右侧第二个 Icon
        Icon(
            imageVector = Icons.Default.SkipNext,
            contentDescription = "下一首",
            modifier = Modifier
                .size(28.dp)
                .clickable { onIcon2Click() }
        )
    }
}
/*
@Composable
fun SwipeSongSwitcher(
    current: String,
    prev: String,
    next: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    height: Dp = 64.dp
) {
    val scope = rememberCoroutineScope()

    // 整体横向偏移
    val offsetX = remember { Animatable(0f) }

    // 屏幕宽度（用于阈值判断）
    var widthPx by remember { mutableFloatStateOf(1f) }
        val showPrevAlpha by remember {
        derivedStateOf {
            if (offsetX.value > 0f) 1f else 0f
        }
    }
    
    val showNextAlpha by remember {
        derivedStateOf {
            if (offsetX.value < 0f) 1f else 0f
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .onSizeChanged { widthPx = it.width.toFloat() }
            .clipToBounds()
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    scope.launch {
                        offsetX.snapTo(
                            (offsetX.value + delta)
                                .coerceIn(-widthPx, widthPx)
                        )
                    }
                },
                onDragStopped = {
                    when {
                        offsetX.value > widthPx * 0.25f -> onNext()
                        offsetX.value < -widthPx * 0.25f -> onPrev()
                    }
                    scope.launch {
                        offsetX.animateTo(
                            0f,
                            animationSpec = spring(stiffness = Spring.StiffnessMedium)
                        )
                    }
                }
            )
    ) {
        Row(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .fillMaxHeight()
        ) {
        
            if (offsetX.value > 0) {
                // show prev
            }
            
            if (offsetX.value < 0) {
                // show next
            }
            SongItem(prev, height, modifier = Modifier.alpha(showPrevAlpha))
            SongItem(current, height, modifier = Modifier.alpha(1f))
            SongItem(next, height, modifier = Modifier.alpha(showNextAlpha))
            
            
            
            
        }
    }
}*/
@Composable
fun SwipeSongSwitcher(
    current: String,
    prev: String,
    next: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    height: Dp = 64.dp
) {
    val scope = rememberCoroutineScope()

    // 位移动画
    val offsetX = remember { Animatable(0f) }

    // 宽度（用于阈值判断）
    var widthPx by remember { mutableFloatStateOf(1f) }

    // ⭐ 显隐状态（关键）
    var showPrev by remember { mutableStateOf(false) }
    var showNext by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .onSizeChanged { widthPx = it.width.toFloat() }
            .clipToBounds()
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    scope.launch {
                        val newOffset = (offsetX.value + delta)
                            .coerceIn(-widthPx, widthPx)

                        offsetX.snapTo(newOffset)

                        // ⭐ 根据方向立刻更新显隐
                        showPrev = newOffset > 0f
                        showNext = newOffset < 0f
                    }
                },
                onDragStopped = {
                    // ⭐ 松手瞬间直接隐藏
                    showPrev = false
                    showNext = false

                    when {
                        offsetX.value > widthPx * 0.25f -> onPrev()
                        offsetX.value < -widthPx * 0.25f -> onNext()
                    }

                    scope.launch {
                        offsetX.animateTo(
                            0f,
                            animationSpec = spring(stiffness = Spring.StiffnessMedium)
                        )
                    }
                }
            )
    ) {
        Row(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .fillMaxHeight()
        ) {

            // 上一首（只负责显示，不参与逻辑）
            SongItem(
                title = prev,
                height = height,
                modifier = Modifier.alpha(if (showPrev) 1f else 0f)
            )

            // 当前歌曲（永远显示）
            SongItem(
                title = current,
                height = height,
                modifier = Modifier.alpha(1f)
            )

            // 下一首
            SongItem(
                title = next,
                height = height,
                modifier = Modifier.alpha(if (showNext) 1f else 0f)
            )
        }
    }
}
@Composable
fun SongItem(
    title: String,
    height: Dp,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1
        )
    }
}
@Composable
fun SwipeSongSwitcherTest() {
    var index by remember { mutableIntStateOf(1) }

    val songs = listOf(
        "稻香",
        "七里香",
        "晴天",
        "测试"
    )

    SwipeSongSwitcher(
        prev = songs[(index - 1 + songs.size) % songs.size],
        current = songs[index],
        next = songs[(index + 1) % songs.size],
        onPrev = {
            index = (index + 1) % songs.size
        },
        onNext = {
            index = (index - 1 + songs.size) % songs.size
            
        }
    )
}

//播放器页面
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun playerScreen(
    modifier: Modifier
) {
            
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {

        // 背景图片 + 高斯模糊
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                ImageView(context).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP

                    Glide.with(this)
                        .load(R.drawable.test) // drawable/test.jpg
                        .transform(
                            jp.wasabeef.glide.transformations.BlurTransformation(
                                40, // 模糊半径（0~25）
                                3   // 采样率，越大越省性能
                            )
                        )
                        .into(this)
                }
            }
        )
        // 你原本的播放器内容（盖在上面）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // 播放器 UI 写这里
            
            
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen2(
    viewModel: MainViewModel = viewModel()
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
                modifier = Modifier.width(280.dp)
            ) {
                SidebarContent(
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // 避免重复导航
                            launchSingleTop = true
                        }
                        viewModel.closeSidebar()
                    }
                )
            }
        },
        gesturesEnabled = true
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
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // 导航内容
                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    composable("home") {
                        HomeScreen(
                            onNavigateToLocal = { navController.navigate("local") },
                            onNavigateToCloud = { navController.navigate("cloud") }
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
    
    // 底部抽屉 - 使用 ModalBottomSheet（带手柄）
   /* if (uiState.bottomDrawerOpen) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeBottomDrawer() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
            dragHandle = {
                // 自定义手柄
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        ) {
            BottomDrawerContent()
        }
    }*/
}

// 侧边栏内容
@Composable
fun SidebarContent(
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        Text(
            text = "菜单",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        Divider()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        DrawerMenuItem(
            icon = Icons.Default.Home,
            text = "主页",
            onClick = { onNavigate("home") }
        )
        
        DrawerMenuItem(
            icon = Icons.Default.MusicNote,
            text = "本地音乐",
            onClick = { onNavigate("local") }
        )
        
        DrawerMenuItem(
            icon = Icons.Default.Cloud,
            text = "云端音乐",
            onClick = { onNavigate("cloud") }
        )
        
        DrawerMenuItem(
            icon = Icons.Default.Favorite,
            text = "我的收藏",
            onClick = { onNavigate("favorites") }
        )
        
        DrawerMenuItem(
            icon = Icons.Default.Settings,
            text = "设置",
            onClick = { onNavigate("settings") }
        )
    }
}

@Composable
fun DrawerMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = text)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text)
    }
}

// 底部抽屉组件（带手柄）- 集成播放器
@Composable
fun BottomDrawerContent(
    playerViewModel: com.inkwise.music.ui.player.PlayerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val playbackState by playerViewModel.playbackState.collectAsState()
    val currentSong = playbackState.currentSong
    var showPlayQueue by remember { mutableStateOf(false) }

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
            onClick = { showPlayQueue = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.QueueMusic, "播放队列")
            Spacer(modifier = Modifier.width(8.dp))
            Text("播放队列")
        }
        if (showPlayQueue) {
            PlayQueueBottomSheet(
                onDismiss = { showPlayQueue = false },
                playerViewModel = playerViewModel 
            )
        }
        
    }
}

