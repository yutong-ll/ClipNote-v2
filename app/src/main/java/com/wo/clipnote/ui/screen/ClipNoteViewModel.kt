package com.wo.clipnote.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wo.clipnote.data.local.AppDao
import com.wo.clipnote.data.local.NoteEntity
import com.wo.clipnote.data.local.TagEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ViewModel 接收 DAO 作为参数，这样它就能操作数据库了
class ClipNoteViewModel(private val dao: AppDao) : ViewModel() {

    // 1. 定义 UI 的筛选状态 (相当于 React 的 useState 或者 Vue 的 ref)
    // MutableStateFlow 表示这是一个可变的数据流，默认值是空字符串或 null
    val searchQuery = MutableStateFlow("")
    val selectedTag = MutableStateFlow<String?>(null)

    val allTags: StateFlow<List<TagEntity>> = dao.getAllTags().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // 2. 核心魔法：组合数据流 (相当于 Vue 的 computed 计算属性)
    // 这里我们将数据库里的原始数据 (dao.getAllNotes()) 和上面的搜索词、标签组合起来
    val filteredNotes: StateFlow<List<NoteEntity>> = combine(
        dao.getAllNotes(),
        searchQuery,
        selectedTag
    ) { notes, query, tag ->
        var result = notes

        if (query.isNotBlank()) {
            result = result.filter { it.content.contains(query, ignoreCase = true) }
        }

        if (tag != null) {
            result = result.filter { it.tags.contains(tag) }
        }

        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun updateSelectedTag(tag: String?) {
        selectedTag.value = tag
    }

    fun addNote(content: String, tags: List<String>, source: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val newNote = NoteEntity(
                content = content,
                tags = tags,
                source = source,
                timestamp = System.currentTimeMillis()
            )
            dao.insertNote(newNote)
        }
    }

    fun addTag(name: String, color: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertTag(TagEntity(name = name, color = color))
        }
    }

    fun updateTag(tag: TagEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateTag(tag)
        }
    }

    fun deleteTag(tag: TagEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteTag(tag)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteNote(note)
        }
    }
}
