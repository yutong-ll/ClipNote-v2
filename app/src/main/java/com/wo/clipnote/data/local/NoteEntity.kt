package com.wo.clipnote.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // 每条笔记的唯一ID
    val content: String,    // 笔记的具体内容
    val tags: List<String>, // 笔记包含的标签（比如 ["好文", "收藏"]，这里会自动用到上面的 Converters）
    val source: String?,     // 笔记来源（比如 "小红书", "微信"）
    val timestamp: Long = System.currentTimeMillis() // 记录保存的时间戳，默认取当前时间
)