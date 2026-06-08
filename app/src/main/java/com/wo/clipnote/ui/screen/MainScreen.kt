package com.wo.clipnote.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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

    // 【核心魔法】订阅 ViewModel 中的笔记列表数据。
    // 只要数据库内容、搜索关键词或标签筛选条件发生变化，这里都会自动收到最新结果并触发界面刷新。
    val notes by viewModel.filteredNotes.collectAsState()

    // 订阅当前搜索框文字状态，实现输入框与 ViewModel 的双向绑定。
    val searchQuery by viewModel.searchQuery.collectAsState()

    // 订阅当前选中的标签状态，用于驱动芯片组选中态。
    val selectedTag by viewModel.selectedTag.collectAsState()

    // 当前阶段先使用一组固定的 Mock 标签数据。
    // 后续如果接入数据库或配置中心，只需要把这里替换成真实数据源即可。
    val mockTags = listOf("全部", "灵感", "待办", "摘录")

    // Scaffold 是一个页面脚手架，帮你预设好了顶部导航栏、底部悬浮按钮等位置。
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ClipNote 知识捕获器") }
            )
        }
    ) { paddingValues ->
        // 页面主体内容。
        // 这里保留原来的列表区域不动，只在它的上方插入搜索与标签筛选模块，符合你的限制要求。
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 顶部搜索框：使用 Material 3 的 OutlinedTextField，配合搜索图标形成更明确的搜索语义。
            // 输入内容直接写回 ViewModel，保证状态单一来源。
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    viewModel.updateSearchQuery(query)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge,
                textStyle = MaterialTheme.typography.bodyLarge,
                placeholder = {
                    Text(
                        text = "搜索笔记内容…",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "搜索图标",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            // 标签筛选芯片组：使用 LazyRow 实现横向滚动。
            // 第一项“全部”用于清空标签筛选，其余标签则写入 ViewModel 的 selectedTag 状态。
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(mockTags) { tag ->
                    val isAllTag = tag == "全部"

                    // “全部”选中时，等价于 selectedTag 为 null；其他标签则按具体值匹配。
                    val isSelected = if (isAllTag) {
                        selectedTag == null
                    } else {
                        selectedTag == tag
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isAllTag) {
                                // 点击“全部”时清空标签筛选，让列表回到不过滤状态。
                                viewModel.updateSelectedTag(null)
                            } else {
                                // 点击普通标签时支持二次点击取消，交互会更自然。
                                val nextTag = if (selectedTag == tag) null else tag
                                viewModel.updateSelectedTag(nextTag)
                            }
                        },
                        label = {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    )
                }
            }

            // LazyColumn 是 Android 的“虚拟列表”，性能极高，只渲染屏幕内可见的元素。
            // 根据你的要求，这一段笔记列表逻辑保持原样，不在本次任务中做结构性调整。
            LazyColumn {
                items(notes) { note ->
                    // 调用现有的 NoteCard 组件展示单条笔记。
                    NoteCard(
                        note = note,
                        onDelete = { viewModel.deleteNote(note) }
                    )
                }
            }
        }
    }
}
