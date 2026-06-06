package com.wo.clipnote

import android.os.Bundle
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

        // 1. 初始化底层数据库 (把当前应用的上下文传给 Room)
        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.appDao()

        // 2. 初始化 ViewModel 大管家
        val viewModel = ClipNoteViewModel(dao)

        // 3. 将 UI 挂载到屏幕上
        setContent {
            // 使用 Material 3 的默认主题包裹
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 这里原本是 Greeting("Android")，现在替换成我们的主界面！
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}