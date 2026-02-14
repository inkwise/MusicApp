package com.inkwise.music.ui.main

import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.inkwise.music.ui.player.PlayerViewModel
import kotlinx.coroutines.launch

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
        label = "ColorAnimation",
    )

    val context = LocalContext.current

    Box(
        modifier =
            modifier
                .fillMaxSize(),
    ) {
        // 主题色获取
        // 我们用一个 0 尺寸的 AsyncImage 来偷偷提取颜色
        AsyncImage(
            model =
                ImageRequest
                    .Builder(context)
                    .data(coverUri)
                    .allowHardware(false) // 必须关闭硬件加速才能拿 Bitmap
                    .size(150) // 极小尺寸提速
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
                            val colorInt =
                                p.getVibrantColor(
                                    p.getMutedColor(
                                        p.getDominantColor(defaultColor.toArgb()),
                                    ),
                                )

                            // ... 在获取颜色后
                            val extractedColor = Color(colorInt)

                            // 将提取到的颜色与黑色 (Black) 进行混合
                            // 0.3f 代表混合 30% 的黑色，70% 的原色。数值越大，颜色越深。
                            themeColor = lerp(extractedColor, Color.Black, 0.5f)
                        }
                    }
                }
            },
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
                    modifier =
                        Modifier
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
                    BottomDrawerContent(pagerState = pagerState, animatedThemeColor = animatedThemeColor)
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
