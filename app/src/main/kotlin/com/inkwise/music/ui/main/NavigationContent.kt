package com.inkwise.music.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.inkwise.music.R
import com.inkwise.music.ui.main.navigationPage.auth.LoginScreen
import com.inkwise.music.ui.main.navigationPage.auth.RegisterScreen
import com.inkwise.music.ui.main.navigationPage.auth.UserProfileScreen
import com.inkwise.music.ui.main.navigationPage.cloud.CloudSongsScreen
import com.inkwise.music.ui.main.navigationPage.home.HomeScreen
import com.inkwise.music.ui.main.navigationPage.home.PlaylistDetailScreen
import com.inkwise.music.ui.main.navigationPage.local.LocalSongsScreen
import com.inkwise.music.ui.main.navigationPage.settings.SettingsScreen
import com.inkwise.music.ui.theme.LocalAppDimens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationContent(
    sheetState: SheetState,
    pagerState: PagerState,
    scope: CoroutineScope,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val dimens = LocalAppDimens.current
    val peekHeight = rememberSheetPeekHeight(dimens.sheetPeekHeightDp)

    // 监听登录需求事件，跳转时清除触发页避免返回循环
    LaunchedEffect(Unit) {
        viewModel.loginRequiredEvents.collect {
            navController.navigate("login") {
                popUpTo("home") { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(uiState.sidebarOpen) {
        if (uiState.sidebarOpen) {
            drawerState.open()
        } else {
            drawerState.close()
        }
    }

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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding()),
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.padding(bottom = peekHeight),
                ) {
                    composable("home") {
                        HomeScreen(
                            onNavigateToLocal = { navController.navigate("local") },
                            onNavigateToCloud = { navController.navigate("cloud") },
                            onNavigateToPlaylist = { id ->
                                navController.navigate("playlist/$id")
                            }
                        )
                    }
                    composable("local") {
                        LocalSongsScreen()
                    }
                    composable("cloud") {
                        CloudSongsScreen()
                    }
                    composable("settings") {
                        SettingsScreen()
                    }
                    composable("login") {
                        LoginScreen(
                            onNavigateToRegister = {
                                navController.navigate("register") {
                                    launchSingleTop = true
                                }
                            },
                            onSuccess = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("register") {
                        RegisterScreen(
                            onNavigateToLogin = {
                                navController.popBackStack()
                            },
                            onSuccess = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("profile") {
                        UserProfileScreen(
                            onLogout = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(
                        route = "playlist/{playlistId}",
                        arguments = listOf(
                            navArgument("playlistId") { type = NavType.LongType }
                        )
                    ) {
                        PlaylistDetailScreen()
                    }
                }

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
