package com.wo.clipnote

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.wo.clipnote.data.local.AppDatabase
import com.wo.clipnote.ui.components.FloatingInputPanel
import com.wo.clipnote.ui.screen.ClipNoteViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InputActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val sharedPrefs = getSharedPreferences("ClipNotePrefs", Context.MODE_PRIVATE)
        val initialDraft = sharedPrefs.getString("note_draft", "") ?: ""
        val dao = AppDatabase.getDatabase(applicationContext).appDao()
        val viewModel = ClipNoteViewModel(dao)

        setContent {
            MaterialTheme {
                val panelMaxHeight = rememberPanelMaxHeight()
                val tagEntities by viewModel.allTags.collectAsState(initial = emptyList())
                val tagNames = tagEntities.map { it.name }
                val sources = remember { listOf("微信", "网页", "小红书") }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { finish() }
                        )
                ) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .imePadding()
                            .heightIn(max = panelMaxHeight)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {}
                            ),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        FloatingInputPanel(
                            initialDraft = initialDraft,
                            tags = tagNames,
                            sources = sources,
                            onDraftChange = { latestDraft ->
                                lifecycleScope.launch(Dispatchers.IO) {
                                    sharedPrefs.edit().putString("note_draft", latestDraft).apply()
                                }
                            },
                            onCancel = {
                                finish()
                            },
                            onSave = { content, selectedTags, selectedSource ->
                                viewModel.addNote(
                                    content = content,
                                    tags = selectedTags,
                                    source = selectedSource ?: "悬浮球"
                                )
                                sharedPrefs.edit().putString("note_draft", "").apply()
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberPanelMaxHeight() =
    (LocalConfiguration.current.screenHeightDp * 0.55f).dp.coerceAtLeast(360.dp)
