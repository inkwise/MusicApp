package com.inkwise.music.ui.main
import androidx.compose.foundation.layout.size
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.inkwise.music.ui.main.navigationPage.cloud.CloudSongsScreen
import com.inkwise.music.ui.main.navigationPage.home.HomeScreen
import com.inkwise.music.ui.main.navigationPage.local.LocalSongsScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import com.inkwise.music.R
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationContent(
    sheetState: SheetState, // 接收状态
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
// 侧边栏
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
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.toggleSidebar() }) {
                            Icon(Icons.Default.Menu, "菜单")
                        }
                    },
                    actions = {
                        // 添加一个按钮来打开底部抽屉
                        IconButton(onClick = { viewModel.toggleBottomDrawer() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_search),
                                contentDescription = "搜索",
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp),
                            )
                            
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
                    modifier = Modifier.padding(bottom = 80.dp)
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
                val shouldIntercept =
                    sheetState.targetValue == SheetValue.Expanded || pagerState.currentPage > 0

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
