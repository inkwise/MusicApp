package com.inkwise.music.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.inkwise.music.ui.cloud.CloudSongsScreen
import com.inkwise.music.ui.home.HomeScreen
import com.inkwise.music.ui.local.LocalSongsScreen
import androidx.navigation.compose.currentBackStackEntryAsState


sealed class MainRoute(val route: String, val title: String) {
    object Home : MainRoute("home", "首页")
    object Local : MainRoute("local", "本地音乐")
    object Cloud : MainRoute("cloud", "云端音乐")
}
private val routes = listOf(
    MainRoute.Home,
    MainRoute.Local,
    MainRoute.Cloud,
)
fun getTitle(route: String?): String =
    routes.find { it.route == route }?.title ?: "音乐播放器"
    
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(){
    BottomSheetScaffold(
        sheetPeekHeight = 40.dp,
        sheetDragHandle = null,
        sheetContent = {
            Box {
                Text(
                    text = "手柄区域",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
                BottomDrawerContent()
            }
        },
    ) {
    //主页面
        MainScreen2()
        
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen2(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
            
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
                    title = { Text(text = getTitle(currentRoute)) },
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
                    startDestination = MainRoute.Home.route,
                ) {
                    composable(MainRoute.Home.route) {
                        HomeScreen(
                            onNavigateToLocal = {
                                navController.navigate(MainRoute.Local.route)
                            },
                            onNavigateToCloud = {
                                navController.navigate(MainRoute.Cloud.route)
                            },
                        )
                    }
                    composable(MainRoute.Local.route) {
                        LocalSongsScreen()
                    }
                    composable(MainRoute.Cloud.route) {
                        CloudSongsScreen()
                    }
                }
            }
        }
    }

    // 底部抽屉 - 使用 ModalBottomSheet（带手柄）
    /*if (uiState.bottomDrawerOpen) {
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
fun SidebarContent(onNavigate: (String) -> Unit) {
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

// 底部抽屉组件（带手柄）
@Composable
fun BottomDrawerContent() {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
    ) {
        Text(
            text = "正在播放",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        // 播放器控制区域
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 封面
            Box(
                modifier =
                    Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 歌曲信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "歌曲名称",
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                )
                Text(
                    text = "艺术家",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }

            // 播放按钮
            IconButton(onClick = { /* 播放/暂停 */ }) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "播放",
                    modifier = Modifier.size(32.dp),
                )
            }
        }

        // 进度条
        Column(modifier = Modifier.padding(top = 16.dp)) {
            Slider(
                value = 0.3f,
                onValueChange = { /* 更新进度 */ },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "1:23",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "3:45",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // 控制按钮
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { /* 随机播放 */ }) {
                Icon(Icons.Default.Shuffle, "随机播放")
            }

            IconButton(onClick = { /* 上一曲 */ }) {
                Icon(Icons.Default.SkipPrevious, "上一曲", modifier = Modifier.size(32.dp))
            }

            FilledIconButton(
                onClick = { /* 播放/暂停 */ },
                modifier = Modifier.size(56.dp),
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "播放",
                    modifier = Modifier.size(32.dp),
                )
            }

            IconButton(onClick = { /* 下一曲 */ }) {
                Icon(Icons.Default.SkipNext, "下一曲", modifier = Modifier.size(32.dp))
            }

            IconButton(onClick = { /* 循环模式 */ }) {
                Icon(Icons.Default.Repeat, "循环")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
