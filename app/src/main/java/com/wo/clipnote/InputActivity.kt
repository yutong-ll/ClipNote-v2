package com.wo.clipnote

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.wo.clipnote.data.local.AppDatabase
import com.wo.clipnote.data.local.NoteEntity
import com.wo.clipnote.ui.components.FloatingInputPanel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InputActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 让内容可以沉浸式绘制到系统状态栏和导航栏下方，实现无缝透明效果
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // ✅ 优化点 1：将 SharedPreferences 的读取提前到 UI 渲染之外，避免阻塞重绘
        val sharedPrefs = getSharedPreferences("ClipNotePrefs", Context.MODE_PRIVATE)
        val initialDraft = sharedPrefs.getString("note_draft", "") ?: ""

        setContent {
            MaterialTheme {
                val panelMaxHeight = rememberPanelMaxHeight()

                // ✅ 优化点 2：为重构后的 FloatingInputPanel 准备外部数据源
                // 如果你已经在 ViewModel 实现了从 Room 数据库获取标签，这里可以改为 collectAsState()
                var tags by remember { mutableStateOf(listOf("灵感", "待办", "摘录")) }
                val sources = remember { listOf("微信", "网页", "小红书") }
                
                // (预留口子：获取数据库真实标签)
                // val db = remember { AppDatabase.getDatabase(this@InputActivity) }
                // LaunchedEffect(Unit) {
                //     db.appDao().getAllTags().collect { tagEntities -> 
                //         tags = tagEntities.map { it.name }
                //     }
                // }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        // ✅ 优化点 3：让黑色背景可点击，点击时关闭页面，同时拦截掉穿透到下层 App 的事件
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null, // 去除点击波纹效果
                            onClick = { finish() }
                        )
                ) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .imePadding()
                            .heightIn(max = panelMaxHeight)
                            // ✅ 消费掉 Surface 本身的点击事件，防止用户点到输入面板白色区域时触发上面的 finish()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {} 
                            ),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        FloatingInputPanel(
                            initialDraft = initialDraft,
                            tags = tags,           // 对接新参数
                            sources = sources,     // 对接新参数
                            onDraftChange = { latestDraft ->
                                // 使用协程异步保存草稿，防止打字过快时发生 UI 卡顿
                                lifecycleScope.launch(Dispatchers.IO) {
                                    sharedPrefs.edit().putString("note_draft", latestDraft).apply()
                                }
                            },
                            onCancel = {
                                finish()
                            },
                            onSave = { content, selectedTag, selectedSource ->
                                sharedPrefs.edit().putString("note_draft", "").apply()
                                saveNote(
                                    content = content,
                                    selectedTag = selectedTag,
                                    selectedSource = selectedSource
                                )
                                finish()
                            },
                            onAddTagClick = {
                                // TODO: 这里可以唤起一个简易的弹窗，让用户输入新的 Tag 并插入数据库
                            }
                        )
                    }
                }
            }
        }
    }

    private fun saveNote(content: String, selectedTag: String?, selectedSource: String?) {
        val db = AppDatabase.getDatabase(this)
        // ✅ 优化点 4：将 CoroutineScope 替换为 Activity 原生的 lifecycleScope，防止内存泄漏
        lifecycleScope.launch(Dispatchers.IO) {
            db.appDao().insertNote(
                NoteEntity(
                    content = content,
                    tags = listOfNotNull(selectedTag),
                    source = selectedSource ?: "悬浮球",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}

@Composable
private fun rememberPanelMaxHeight() =
    (LocalConfiguration.current.screenHeightDp * 0.55f).dp.coerceAtLeast(360.dp)