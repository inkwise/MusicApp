package com.inkwise.music

import dagger.hilt.android.AndroidEntryPoint   // ← 這一行最關鍵！
import android.os.Environment
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Button

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.inkwise.music.ui.main.MainScreen
import com.inkwise.music.ui.theme.ComposeEmptyActivityTheme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


import androidx.compose.ui.platform.LocalContext

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
// Compose runtime
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider

// Compose UI platform
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity

// Compose unit
import androidx.compose.ui.unit.dp

// 你的 dimens 定义
import com.inkwise.music.ui.theme.AppDimens
import com.inkwise.music.ui.theme.LocalAppDimens

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeEmptyActivityTheme {
                val configuration = LocalConfiguration.current
                val density = LocalDensity.current
        
                val dimens = remember(configuration, density) {
                    with(density) {
                        val sheetPeekHeightDp = 80.dp
                        // 示例：测试用 px 宽度（比如 100dp 转 px）
                        val testWidthPx = 100.dp.toPx().toInt()
                        AppDimens(
                            sheetPeekHeightDp = sheetPeekHeightDp,
                            testWidthPx = testWidthPx
                        )
                    }
                }
        
                CompositionLocalProvider(
                    LocalAppDimens provides dimens
                ) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        AllFilesPermissionScreen()
                    }
                }
            }
        }
    }
}

fun hasAllFilesPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        true
    }
}


fun requestAllFilesPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:${context.packageName}")
            context.startActivity(intent)
        } catch (e: Exception) {
            // 某些国产 ROM 不支持
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            context.startActivity(intent)
        }
    }
}

@Composable
fun AllFilesPermissionScreen() {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(hasAllFilesPermission()) }

    LaunchedEffect(Unit) {
        granted = hasAllFilesPermission()
    }

    /*Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {*/
        


        if (!granted) {
            Button(onClick = {
                requestAllFilesPermission(context)
            }) {
                Text("前往系统设置开启")
            }
        }else{
            MainScreen()
        }
    //}
}