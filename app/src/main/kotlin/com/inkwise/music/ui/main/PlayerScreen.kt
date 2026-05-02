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
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.*
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.inkwise.music.ui.player.PlayerViewModel
import com.inkwise.music.ui.theme.harmonizeToPlayerBackground
import com.inkwise.music.ui.theme.toSoftBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun playerScreen(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val playbackState by playerViewModel.playbackState.collectAsState()
    val currentSong = playbackState.currentSong
    val coverUri = currentSong?.albumArt

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // ---------------- Pager 修复逻辑 ----------------

    val fixStuckConnection =
        remember {
            object : NestedScrollConnection {
                override suspend fun onPostFling(
                    consumed: Velocity,
                    available: Velocity,
                ): Velocity {
                    if (pagerState.currentPageOffsetFraction != 0f) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.targetPage)
                        }
                    }
                    return super.onPostFling(consumed, available)
                }
            }
        }

    val flingBehavior =
        PagerDefaults.flingBehavior(
            state = pagerState,
            snapPositionalThreshold = 0.08f,
            snapAnimationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
        )

    // ---------------- 背景主题色状态 ----------------

    var themeColor by remember { mutableStateOf(Color(0xFFECEFF1)) }
    var primaryColor by remember { mutableStateOf(Color(0xFF2196F3)) }
    val animatedPrimaryColor by animateColorAsState(
        targetValue = primaryColor,
        animationSpec = tween(800), // 增加颜色渐变时间，让过渡更丝滑
        label = "PrimaryColorAnimation",
    )

    val backgroundColor =
        remember(animatedPrimaryColor) {
            animatedPrimaryColor.toSoftBackground()
        }

    // ---------------- 取色逻辑 ----------------

    AsyncImage(
        model =
            ImageRequest
                .Builder(context)
                .data(coverUri)
                .allowHardware(false) // 必须
                .size(150)
                .build(),
        contentDescription = null,
        modifier =
            Modifier
                .size(1.dp)
                .alpha(0f),
        onSuccess = { success ->
            val drawable = success.result.drawable
            val bitmap = (drawable as? BitmapDrawable)?.bitmap ?: return@AsyncImage

            Palette.from(bitmap).generate { palette ->
                palette?.let { p ->
                    // 优先取有活力的颜色作为主色，取不到再退化
                    val pickedColor =
                        p.getVibrantColor(
                            p.getMutedColor(
                                p.getDominantColor(android.graphics.Color.GRAY),
                            ),
                        )
                    primaryColor = Color(pickedColor)
                    themeColor = harmonizeToPlayerBackground(pickedColor)
                }
            }
        },
    )

    // ---------------- UI ----------------

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        // ---------- 图1风格背景 (核心修改区) ----------

        AnimatedContent(
            targetState = coverUri,
            transitionSpec = {
                fadeIn(tween(600)) togetherWith fadeOut(tween(800))
            },
            label = "BackgroundTransition",
        ) { targetUri ->

            Box(modifier = Modifier.fillMaxSize()) {
                // 1️⃣ 第一层：极浅底色渐变 (奠定椒盐音乐那种通透感)
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            backgroundColor,
                                            backgroundColor.copy(alpha = 0.6f),
                                            Color(0xFFF5F7FA), // 底部偏向干净的灰白色
                                        ),
                                ),
                            ),
                )

                // 2️⃣ 第二层：大半径模糊封面 (灵魂所在，形成晕染效果)
                Image(
                    painter =
                        rememberAsyncImagePainter(
                            ImageRequest
                                .Builder(context)
                                .data(targetUri)
                                .allowHardware(false)
                                .size(100) // 采样不需要太大
                                .build(),
                        ),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                alpha = 0.25f // 提高透明度，配合大模糊使用
                            }
                            // 使用 Unbounded 防止边缘出现黑边，半径加大到 50dp
                            .blur(radius = 50.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded),
                )

                // 3️⃣ 第三层：中心高光微调 (模拟屏幕或光线的折射感，提亮状态栏区域)
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors =
                                        listOf(
                                            Color.White.copy(alpha = 0.2f),
                                            Color.Transparent,
                                        ),
                                    center = Offset(500f, 300f), // 偏上方的高光
                                    radius = 1200f,
                                ),
                            ),
                )
            }
        }

        // ---------- Pager 内容 ----------

        VerticalPager(
            state = pagerState,
            key = { it },
            modifier =
                Modifier
                    .fillMaxSize()
                    .nestedScroll(fixStuckConnection),
            beyondViewportPageCount = 1,
            flingBehavior = flingBehavior,
        ) { page ->

            when (page) {
                0 -> {
                    BottomDrawerContent(
                        pagerState = pagerState,
                        animatedThemeColor = animatedPrimaryColor,
                    )
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

