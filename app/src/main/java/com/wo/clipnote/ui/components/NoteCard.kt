package com.wo.clipnote.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wo.clipnote.data.local.NoteEntity

// @Composable 就相当于告诉系统：这是一个 UI 组件函数
// note 和 onDelete 就是你熟悉的 Props
@Composable
fun NoteCard(
    note: NoteEntity,
    onDelete: () -> Unit // 传入一个删除回调函数
) {
    // Card 相当于前端的 <div class="card">
    Card(
        // Modifier 是 Compose 中最重要的概念，相当于前端的 style 和 class 集合
        modifier = Modifier
            .fillMaxWidth() // 相当于 width: 100%
            .padding(vertical = 8.dp), // 相当于 margin: 8px 0
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // Column 相当于 Flexbox 的 flex-direction: column
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 渲染笔记内容
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyLarge
            )

            // 简单渲染一下标签（如果有的话）
            if (note.tags.isNotEmpty()) {
                Text(
                    text = "标签: ${note.tags.joinToString(", ")}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}