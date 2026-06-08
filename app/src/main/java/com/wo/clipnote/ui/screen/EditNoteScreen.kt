package com.wo.clipnote.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.wo.clipnote.data.local.NoteEntity
import com.wo.clipnote.data.local.TagEntity
import com.wo.clipnote.ui.components.TagSelectorWithSearch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    note: NoteEntity,
    allTags: List<TagEntity>,
    onSave: (NoteEntity) -> Unit,
    onBack: () -> Unit
) {
    var content by remember(note.id) {
        mutableStateOf(
            TextFieldValue(
                text = note.content,
                selection = androidx.compose.ui.text.TextRange(note.content.length)
            )
        )
    }
    var source by remember(note.id) { mutableStateOf(note.source.orEmpty()) }
    var selectedTags by remember(note.id) { mutableStateOf(note.tags) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    val contentFocusRequester = remember { FocusRequester() }

    val hasChanges = content.text != note.content ||
        source != note.source.orEmpty() ||
        selectedTags != note.tags

    LaunchedEffect(note.id) {
        contentFocusRequester.requestFocus()
    }

    fun handleBack() {
        if (hasChanges) {
            showDiscardDialog = true
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑笔记") },
                navigationIcon = {
                    IconButton(onClick = ::handleBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            onSave(
                                note.copy(
                                    content = content.text.trim(),
                                    tags = selectedTags,
                                    source = source.trim().ifBlank { null }
                                )
                            )
                        },
                        enabled = content.text.trim().isNotEmpty()
                    ) {
                        Text("保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "笔记内容",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .heightIn(min = 280.dp),
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    val editorScrollState = rememberScrollState()
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(editorScrollState)
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        if (content.text.isEmpty()) {
                            Text(
                                text = "请输入完整笔记内容",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        BasicTextField(
                            value = content,
                            onValueChange = { content = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(contentFocusRequester),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "标签",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                TagSelectorWithSearch(
                    allTags = allTags,
                    selectedTags = selectedTags,
                    onTagClick = { tagName ->
                        selectedTags = if (selectedTags.contains(tagName)) {
                            selectedTags - tagName
                        } else {
                            selectedTags + tagName
                        }
                    }
                )
            }

            OutlinedTextField(
                value = source,
                onValueChange = { source = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("来源") },
                placeholder = { Text("请输入来源，例如小红书 / 微信 / 网页") }
            )
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("放弃未保存修改？") },
            text = { Text("你已经修改了当前笔记，返回后这些内容将不会保存。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onBack()
                    }
                ) {
                    Text("放弃修改")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("继续编辑")
                }
            }
        )
    }
}
