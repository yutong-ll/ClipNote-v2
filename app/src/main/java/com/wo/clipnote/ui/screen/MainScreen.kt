package com.wo.clipnote.ui.screen

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wo.clipnote.InputActivity
import com.wo.clipnote.data.local.NoteEntity
import com.wo.clipnote.service.OverlayService
import com.wo.clipnote.ui.components.NoteCard
import com.wo.clipnote.ui.components.TagDrawerContent
import com.wo.clipnote.ui.theme.DefaultTagColorHex
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: ClipNoteViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var isFabExpanded by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<NoteEntity?>(null) }
    var pendingDeleteNote by remember { mutableStateOf<NoteEntity?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val notes by viewModel.filteredNotes.collectAsState(initial = emptyList())
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    val selectedSource by viewModel.selectedSource.collectAsState()
    val tags by viewModel.allTags.collectAsState(initial = emptyList())

    fun deleteNoteWithUndo(note: NoteEntity) {
        viewModel.deleteNote(note)
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = "笔记已删除",
                actionLabel = "撤销",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            }
        }
    }

    val listContent: @Composable () -> Unit = {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            topBar = {
                TopAppBar(
                    title = {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "ClipNote",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "极简记录与整理",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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
                            FloatingActionButton(
                                onClick = {
                                    if (Settings.canDrawOverlays(context)) {
                                        context.startService(Intent(context, OverlayService::class.java))
                                    }
                                    isFabExpanded = false
                                },
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "启动快捷操作"
                                )
                            }

                            FloatingActionButton(
                                onClick = {
                                    context.startActivity(Intent(context, InputActivity::class.java))
                                    isFabExpanded = false
                                },
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "添加笔记"
                                )
                            }
                        }
                    }

                    FloatingActionButton(
                        onClick = { isFabExpanded = !isFabExpanded },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = if (isFabExpanded) "收起快捷操作" else "展开快捷操作"
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "打开标签抽屉",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = viewModel::updateSearchQuery,
                            modifier = Modifier.weight(1f),
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
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        IconButton(onClick = {
                            val nextSource = when (selectedSource) {
                                null -> "手动输入"
                                "手动输入" -> "微信"
                                "微信" -> "网页"
                                "网页" -> "截图"
                                "截图" -> "剪贴板"
                                else -> null
                            }
                            viewModel.updateSelectedSource(nextSource)
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.FilterList,
                                contentDescription = "切换来源筛选",
                                tint = if (selectedSource == null) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                        }
                    }
                }

                if (selectedSource != null || selectedTag != null) {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (selectedTag != null) {
                                    Text(
                                        text = "标签：$selectedTag",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (selectedSource != null) {
                                    Text(
                                        text = "来源：$selectedSource",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            TextButton(onClick = {
                                viewModel.updateSelectedTag(null)
                                viewModel.updateSelectedSource(null)
                            }) {
                                Text("清除")
                            }
                        }
                    }
                }

                if (notes.isEmpty()) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "暂无笔记",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "试试右下角添加一条灵感，或调整搜索、标签与来源筛选。",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 96.dp)
                    ) {
                        items(notes, key = { it.id }) { note ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                        pendingDeleteNote = note
                                    }
                                    false
                                },
                                positionalThreshold = { distance -> distance * 0.35f }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                enableDismissFromEndToStart = true,
                                backgroundContent = {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = MaterialTheme.shapes.large,
                                        color = MaterialTheme.colorScheme.errorContainer,
                                        tonalElevation = 0.dp,
                                        shadowElevation = 0.dp
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp, vertical = 24.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.DeleteOutline,
                                                contentDescription = "删除提示",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            ) {
                                NoteCard(
                                    note = note,
                                    isSelected = false,
                                    onClick = {},
                                    onDelete = { pendingDeleteNote = note },
                                    onDoubleClick = {
                                        editingNote = note
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (pendingDeleteNote != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteNote = null },
            title = { Text("删除笔记") },
            text = { Text("确认删除这条笔记吗？确认后才会执行删除。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteNote?.let(::deleteNoteWithUndo)
                        pendingDeleteNote = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteNote = null }) {
                    Text("取消")
                }
            }
        )
    }

    if (editingNote != null) {
        EditNoteScreen(
            note = editingNote!!,
            allTags = tags,
            onSave = { updatedNote ->
                viewModel.updateNote(updatedNote)
                editingNote = null
            },
            onBack = {
                editingNote = null
            }
        )
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                TagDrawerContent(
                    tags = tags,
                    selectedTag = selectedTag,
                    onBack = {
                        scope.launch { drawerState.close() }
                    },
                    onTagFilterClick = { tagName ->
                        viewModel.updateSelectedTag(tagName)
                        scope.launch { drawerState.close() }
                    },
                    onDeleteTag = { tag ->
                        viewModel.deleteTag(tag)
                        if (selectedTag == tag.name) {
                            viewModel.updateSelectedTag(null)
                        }
                    },
                    onCreateTag = { name, color ->
                        val trimmedName = name.trim()
                        if (trimmedName.isNotEmpty() && tags.none { it.name.equals(trimmedName, ignoreCase = true) }) {
                            viewModel.addTag(trimmedName, color.ifBlank { DefaultTagColorHex })
                        }
                    },
                    onUpdateTag = { updatedTag ->
                        val trimmedName = updatedTag.name.trim()
                        if (trimmedName.isNotEmpty() && tags.none { it.id != updatedTag.id && it.name.equals(trimmedName, ignoreCase = true) }) {
                            val previousName = tags.firstOrNull { it.id == updatedTag.id }?.name
                            viewModel.updateTag(updatedTag.copy(name = trimmedName))
                            if (selectedTag == previousName && previousName != trimmedName) {
                                viewModel.updateSelectedTag(trimmedName)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.82f)
                )
            }
        ) {
            listContent()
        }
    }
}
