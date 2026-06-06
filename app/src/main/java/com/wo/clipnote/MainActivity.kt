package com.wo.clipnote

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.wo.clipnote.data.local.AppDatabase
import com.wo.clipnote.ui.screen.ClipNoteViewModel
import com.wo.clipnote.ui.screen.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ==========================================
        // 🌟 新增：悬浮窗权限检查与后台服务启动逻辑
        // ==========================================
        // 1. 检查当前 App 是否拥有“显示在其他应用上层”的权限
        if (!Settings.canDrawOverlays(this)) {
            // 如果没有权限，直接跳转到系统的授权设置页面
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        } else {
            // 如果已经有权限了，就在后台把我们的悬浮球跑起来！
            val serviceIntent = Intent(this, com.wo.clipnote.service.OverlayService::class.java)
            startService(serviceIntent)
        }
        // ==========================================


        // 下面是你原本的页面加载逻辑：
        // 1. 初始化底层数据库
        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.appDao()

        // 2. 初始化 ViewModel 大管家
        val viewModel = ClipNoteViewModel(dao)

        // 3. 将 UI 挂载到屏幕上
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}