package com.wo.clipnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wo.clipnote.data.local.AppDatabase
import com.wo.clipnote.data.local.NoteEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InputActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // 输入面板 UI
            Surface(modifier = Modifier.fillMaxSize(), color = Color.Black.copy(alpha = 0.5f)) {
                Box(contentAlignment = Alignment.Center) {
                    var text by remember { mutableStateOf("") }

                    Column(modifier = Modifier.padding(16.dp).background(Color.White).padding(16.dp)) {
                        TextField(value = text, onValueChange = { text = it }, label = { Text("记录内容...") })
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            saveNote(text)
                            finish() // 保存后关闭面板
                        }) {
                            Text("保存")
                        }
                    }
                }
            }
        }
    }

    private fun saveNote(content: String) {
        val db = AppDatabase.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            db.appDao().insertNote(NoteEntity(content = content, tags = emptyList(), source = "悬浮球", timestamp = System.currentTimeMillis()))
        }
    }
}