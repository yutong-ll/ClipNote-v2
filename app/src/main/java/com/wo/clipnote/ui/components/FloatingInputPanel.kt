package com.wo.clipnote.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FloatingInputPanel(
    initialDraft: String,
    tags: List<String>,    // ✅ 改为从外部(数据库)传入真实标签流
    sources: List<String>, // ✅ 改为从外部传入来源配置
    onDraftChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: (content: String, selectedTag: String?, selectedSource: String?) -> Unit,
    onAddTagClick: () -> Unit, // ✅ 新增：点击新建标签的回调
    modifier: Modifier = Modifier
) {
    // 使用 rememberSaveable 保证配置更改（如键盘弹出）时状态不丢失
    var content by rememberSaveable { mutableStateOf(initialDraft) }
    var selectedTag by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedSource by rememberSaveable { mutableStateOf<String?>(null) }

    // 当外部草稿箱数据发生本质变化时，同步到本地状态
    LaunchedEffect(initialDraft) {
        if (content.isEmpty() && initialDraft.isNotEmpty()) {
            content = initialDraft
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. 核心输入区（支持长文本滚动） ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. 标签与来源选择器 ---
            ChipSelectorRow(
                title = "标签",
                options = tags,
                selected = selectedTag,
                onSelected = { option ->
                    selectedTag = if (selectedTag == option) null else option
                },
                showAddAction = true, // ✅ 针对标签开启“新建”功能
                onAddClick = onAddTagClick
            )

            ChipSelectorRow(
                title = "来源",
                options = sources,
                selected = selectedSource,
                onSelected = { option ->
                    selectedSource = if (selectedSource == option) null else option
                },
                showAddAction = false
            )

            OutlinedTextField(
                value = content,
                onValueChange = {
                    content = it
                    onDraftChange(it) // 实时静默保存草稿
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 200.dp), // 稍微放大输入区域，更利于阅读
                label = { Text("记录灵感...") },
                placeholder = { Text("输入此刻想保存的内容…", style = MaterialTheme.typography.bodyMedium) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                trailingIcon = {
                    if (content.isNotEmpty()) {
                        // ✅ 将 TextButton 替换为标准的 IconButton
                        IconButton(onClick = {
                            content = ""
                            onDraftChange("")
                        }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "清空输入内容",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }

        // --- 3. 底部操作区 ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("取消")
            }

            Button(
                onClick = {
                    if (content.isNotBlank()) {
                        onSave(content, selectedTag, selectedSource)
                        content = ""
                        onDraftChange("")
                    }
                },
                shape = RoundedCornerShape(8.dp),
                enabled = content.isNotBlank() // ✅ 内容为空时自动禁用保存按钮，防止写入空数据
            ) {
                Text("保存")
            }
        }
    }
}

@Composable
private fun ChipSelectorRow(
    title: String,
    options: List<String>,
    selected: String?,
    onSelected: (String) -> Unit,
    showAddAction: Boolean = false,
    onAddClick: (() -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            // ✅ 如果开启了新建操作，在最左侧渲染一个额外的药丸
            if (showAddAction && onAddClick != null) {
                item {
                    FilterChip(
                        selected = false,
                        onClick = onAddClick,
                        label = { Text("新建") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "新增",
                                modifier = Modifier.padding(end = 2.dp)
                            )
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            items(options) { option ->
                FilterChip(
                    selected = selected == option,
                    onClick = { onSelected(option) },
                    label = { Text(text = option) },
                    modifier = Modifier.widthIn(min = 48.dp),
                    colors = FilterChipDefaults.filterChipColors(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
    }
}