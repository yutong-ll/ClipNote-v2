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
import androidx.compose.material3.Surface
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
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .heightIn(min = 220.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "标签",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
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
                        .heightIn(min = 136.dp, max = 220.dp),
                    label = { Text("记录灵感") },
                    placeholder = {
                        Text(
                            "输入此刻想保存的内容…",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    shape = MaterialTheme.shapes.large,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        cursorColor = MaterialTheme.colorScheme.primary
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
                shape = MaterialTheme.shapes.medium,
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
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
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
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedOptions.contains(option),
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                        selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                        disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        disabledSelectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}
