package com.wo.clipnote.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.wo.clipnote.data.local.TagEntity

@Composable
fun FloatingInputPanel(
    initialDraft: String,
    tags: List<TagEntity>,
    sources: List<String>,
    onDraftChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: (content: String, selectedTags: List<String>, selectedSource: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var content by rememberSaveable { mutableStateOf(initialDraft) }
    var selectedTags by rememberSaveable { mutableStateOf(setOf<String>()) }
    var selectedSource by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(initialDraft) {
        if (content.isEmpty() && initialDraft.isNotEmpty()) {
            content = initialDraft
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 220.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "标签",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                TagSelectorWithSearch(
                    allTags = tags,
                    selectedTags = selectedTags.toList(),
                    onTagClick = { tagName ->
                        selectedTags = if (selectedTags.contains(tagName)) {
                            selectedTags - tagName
                        } else {
                            selectedTags + tagName
                        }
                    }
                )
            }

            ChipSelectorRow(
                title = "来源",
                options = sources,
                selectedOptions = selectedSource?.let { setOf(it) }.orEmpty(),
                onOptionToggle = { option ->
                    selectedSource = if (selectedSource == option) null else option
                }
            )

            OutlinedTextField(
                value = content,
                onValueChange = {
                    content = it
                    onDraftChange(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 200.dp),
                label = { Text("记录灵感...") },
                placeholder = { Text("输入此刻想保存的内容…", style = MaterialTheme.typography.bodyMedium) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                trailingIcon = {
                    if (content.isNotEmpty()) {
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
                        onSave(content, selectedTags.toList(), selectedSource)
                        content = ""
                        selectedTags = emptySet()
                        selectedSource = null
                        onDraftChange("")
                    }
                },
                shape = RoundedCornerShape(8.dp),
                enabled = content.isNotBlank()
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
    selectedOptions: Set<String>,
    onOptionToggle: (String) -> Unit
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
            items(options) { option ->
                FilterChip(
                    selected = selectedOptions.contains(option),
                    onClick = { onOptionToggle(option) },
                    label = { Text(text = option) },
                    colors = FilterChipDefaults.filterChipColors(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
    }
}
