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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.wo.clipnote.InputActivity
import com.wo.clipnote.data.local.NoteEntity
import com.wo.clipnote.data.local.TagEntity
import com.wo.clipnote.service.OverlayService
import com.wo.clipnote.ui.components.NoteCard
import com.wo.clipnote.ui.components.TagDrawerContent
import com.wo.clipnote.ui.components.TagSelectorWithSearch
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: ClipNoteViewModel) {
    val context = LocalContext.current
    var isFabExpanded by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<NoteEntity?>(null) }
    var selectedNoteId by remember { mutableIntStateOf(-1) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val notes by viewModel.filteredNotes.collectAsState(initial = emptyList())
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    val tags by viewModel.allTags.collectAsState(initial = emptyList())

    fun deleteNoteWithUndo(note: NoteEntity) {
        if (selectedNoteId == note.id) {
            selectedNoteId = -1
        }
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
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
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
                                FloatingActionButton(
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
                                FloatingActionButton(
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                            contentDescription = "打开标签抽屉"
                        )
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { query ->
                            viewModel.updateSearchQuery(query)
                        },
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
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                }

                TagSelectorWithSearch(
                    allTags = listOf(TagEntity(id = -1, name = "全部", color = "#9E9E9E")) + tags,
                    selectedTags = selectedTag?.let(::listOf).orEmpty(),
                    onTagClick = { tagName ->
                        val nextTag = when {
                            tagName == "全部" -> null
                            selectedTag == tagName -> null
                            else -> tagName
                        }
                        viewModel.updateSelectedTag(nextTag)
                    }
                )

                if (notes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "🗒️",
                                style = MaterialTheme.typography.displayMedium
                            )
                            Text(
                                text = "暂无笔记",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "试试右下角添加一条灵感，或调整搜索与标签筛选。",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(notes, key = { it.id }) { note ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                        deleteNoteWithUndo(note)
                                        true
                                    } else {
                                        false
                                    }
                                },
                                positionalThreshold = { distance -> distance * 0.35f }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                enableDismissFromEndToStart = true,
                                backgroundContent = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFD32F2F), shape = MaterialTheme.shapes.large)
                                            .padding(horizontal = 20.dp, vertical = 24.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "删除笔记",
                                            tint = Color.White
                                        )
                                    }
                                }
                            ) {
                                NoteCard(
                                    note = note,
                                    isSelected = selectedNoteId == note.id,
                                    onClick = {
                                        selectedNoteId = if (selectedNoteId == note.id) -1 else note.id
                                    },
                                    onDelete = { deleteNoteWithUndo(note) },
                                    onDoubleClick = {
                                        selectedNoteId = note.id
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

    if (editingNote != null) {
        EditNoteScreen(
            note = editingNote!!,
            allTags = tags,
            onSave = { updatedNote ->
                viewModel.updateNote(updatedNote)
                selectedNoteId = updatedNote.id
                editingNote = null
            },
            onBack = {
                editingNote = null
            }
        )
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                TagDrawerContent(
                    tags = tags,
                    onDeleteTag = { tag ->
                        viewModel.deleteTag(tag)
                        if (selectedTag == tag.name) {
                            viewModel.updateSelectedTag(null)
                        }
                    },
                    onCreateTag = { name ->
                        val trimmedName = name.trim()
                        if (trimmedName.isNotEmpty() && tags.none { it.name.equals(trimmedName, ignoreCase = true) }) {
                            viewModel.addTag(trimmedName, "#6650A4")
                        }
                    }
                )
            }
        ) {
            listContent()
        }
    }
}
