package com.wo.clipnote.ui.screen

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.wo.clipnote.InputActivity
import com.wo.clipnote.data.local.TagEntity
import com.wo.clipnote.service.OverlayService
import com.wo.clipnote.ui.components.NoteCard

private val tagColorOptions = listOf(
    "#6650A4",
    "#4CAF50",
    "#2196F3",
    "#FF9800",
    "#E91E63",
    "#009688",
    "#9C27B0",
    "#795548"
)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: ClipNoteViewModel) {
    val context = LocalContext.current
    var isFabExpanded by remember { mutableStateOf(false) }
    var showTagDialog by rememberSaveable { mutableStateOf(false) }
    var editingTag by remember { mutableStateOf<TagEntity?>(null) }

    val notes by viewModel.filteredNotes.collectAsState(initial = emptyList())
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    val tags by viewModel.allTags.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ClipNote 知识捕获器") }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedVisibility(visible = isFabExpanded) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                tonalElevation = 3.dp,
                                shadowElevation = 2.dp
                            ) {
                                Text(
                                    text = "悬浮球开关",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                            androidx.compose.material3.FloatingActionButton(
                                onClick = {
                                    if (Settings.canDrawOverlays(context)) {
                                        context.startService(Intent(context, OverlayService::class.java))
                                    }
                                    isFabExpanded = false
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "启动悬浮球"
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                tonalElevation = 3.dp,
                                shadowElevation = 2.dp
                            ) {
                                Text(
                                    text = "添加笔记",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                            androidx.compose.material3.FloatingActionButton(
                                onClick = {
                                    context.startActivity(Intent(context, InputActivity::class.java))
                                    isFabExpanded = false
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "添加笔记"
                                )
                            }
                        }
                    }
                }

                ExtendedFloatingActionButton(
                    onClick = { isFabExpanded = !isFabExpanded },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = if (isFabExpanded) "收起快捷操作" else "展开快捷操作"
                        )
                    },
                    text = {
                        Text(if (isFabExpanded) "收起" else "快捷操作")
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "搜索图标",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = OutlinedTextFieldDefaults.colors()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "标签管理",
                    style = MaterialTheme.typography.titleMedium
                )
                Button(
                    onClick = {
                        editingTag = null
                        showTagDialog = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text("新增标签")
                }
            }

            if (tags.isEmpty()) {
                Text(
                    text = "暂无标签，点击右侧新增",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedTag == null,
                            onClick = { viewModel.updateSelectedTag(null) },
                            label = {
                                Text(
                                    text = "全部",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        )
                    }

                    items(tags) { tag ->
                        FilterChip(
                            selected = selectedTag == tag.name,
                            onClick = {
                                val nextTag = if (selectedTag == tag.name) null else tag.name
                                viewModel.updateSelectedTag(nextTag)
                            },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(parseTagColor(tag.color))
                                    )
                                    Text(
                                        text = tag.name,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        )
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tags, key = { it.id }) { tag ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(parseTagColor(tag.color))
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                                                shape = CircleShape
                                            )
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(tag.name, style = MaterialTheme.typography.titleSmall)
                                        Text(tag.color, style = MaterialTheme.typography.bodySmall)
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            editingTag = tag
                                            showTagDialog = true
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = "编辑标签"
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteTag(tag)
                                            if (selectedTag == tag.name) {
                                                viewModel.updateSelectedTag(null)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "删除标签"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notes) { note ->
                    NoteCard(
                        note = note,
                        onDelete = { viewModel.deleteNote(note) }
                    )
                }
            }
        }
    }

    if (showTagDialog) {
        TagEditorDialog(
            initialTag = editingTag,
            onDismiss = {
                showTagDialog = false
                editingTag = null
            },
            onConfirm = { name, color ->
                val trimmedName = name.trim()
                if (trimmedName.isNotEmpty()) {
                    val duplicate = tags.any { existing ->
                        existing.name.equals(trimmedName, ignoreCase = true) && existing.id != editingTag?.id
                    }
                    if (!duplicate) {
                        val currentTag = editingTag
                        if (currentTag == null) {
                            viewModel.addTag(trimmedName, color)
                        } else {
                            viewModel.updateTag(currentTag.copy(name = trimmedName, color = color))
                            if (selectedTag == currentTag.name && currentTag.name != trimmedName) {
                                viewModel.updateSelectedTag(trimmedName)
                            }
                        }
                        showTagDialog = false
                        editingTag = null
                    }
                }
            }
        )
    }
}

@Composable
private fun TagEditorDialog(
    initialTag: TagEntity?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, color: String) -> Unit
) {
    var tagName by rememberSaveable(initialTag?.id) { mutableStateOf(initialTag?.name.orEmpty()) }
    var selectedColor by rememberSaveable(initialTag?.id) {
        mutableStateOf(initialTag?.color ?: tagColorOptions.first())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialTag == null) "新增标签" else "编辑标签")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = tagName,
                    onValueChange = { tagName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("标签名") },
                    placeholder = { Text("请输入标签名") }
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "选择颜色",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(tagColorOptions) { colorHex ->
                            val isSelected = selectedColor.equals(colorHex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(parseTagColor(colorHex))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                        },
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = colorHex }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(tagName, selectedColor) },
                enabled = tagName.trim().isNotEmpty()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

private fun parseTagColor(colorHex: String): Color {
    return runCatching { Color(android.graphics.Color.parseColor(colorHex)) }
        .getOrDefault(Color.Gray)
}
