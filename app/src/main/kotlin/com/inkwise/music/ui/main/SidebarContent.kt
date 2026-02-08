package com.inkwise.music.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


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
