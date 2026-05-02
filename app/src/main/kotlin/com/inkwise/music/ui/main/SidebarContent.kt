package com.inkwise.music.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkwise.music.data.prefs.PreferencesManager
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch

@Composable
fun SidebarContent(
    onNavigate: (String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // 直接从 Application 获取 PreferencesManager 单例，确保状态一致
    val entryPoint = dagger.hilt.android.EntryPointAccessors.fromApplication(
        context,
        com.inkwise.music.data.prefs.PreferencesManagerEntryPoint::class.java
    )
    val prefs = entryPoint.prefs()

    val isLoggedIn by prefs.isLoggedIn.collectAsState(initial = false)
    val username by prefs.username.collectAsState(initial = null)

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        // 用户信息区域 - 顶部
        if (isLoggedIn) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { onNavigate("profile") })
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = username ?: "",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "已登录 - 查看资料",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = {
                    scope.launch { prefs.clearAuthData() }
                }) {
                    Text("退出", fontSize = 12.sp)
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "未登录",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row {
                        TextButton(onClick = { onNavigate("login") }) {
                            Text("登录", fontSize = 13.sp)
                        }
                        TextButton(onClick = { onNavigate("register") }) {
                            Text("注册", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        Text(
            text = "菜单",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        HorizontalDivider()

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

        Spacer(modifier = Modifier.weight(1f))
        // 避免被底部播放器控件遮挡
        Spacer(modifier = Modifier.height(60.dp))
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
