package com.wo.clipnote.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wo.clipnote.ui.components.NoteCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: ClipNoteViewModel) {

    // 【核心魔法】订阅 ViewModel 中的数据！
    // 只要数据库一变，这个 notes 就会自动更新，触发下方 UI 重绘 (类似 React 的 useEffect 绑定)
    val notes by viewModel.filteredNotes.collectAsState()

    // Scaffold 是一个页面脚手架，帮你预设好了顶部导航栏、底部悬浮按钮等位置
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ClipNote 知识捕获器") }
            )
        }
    ) { paddingValues ->
        // 页面主体内容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {

            // LazyColumn 是 Android 的“虚拟列表”，性能极高，只渲染屏幕内可见的元素
            // 相当于前端的 map() 循环渲染
            LazyColumn {
                items(notes) { note ->
                    // 调用我们刚才写的 NoteCard 组件
                    NoteCard(
                        note = note,
                        onDelete = { viewModel.deleteNote(note) }
                    )
                }
            }
        }
    }
}