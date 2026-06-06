package com.wo.clipnote.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wo.clipnote.data.local.AppDao
import com.wo.clipnote.data.local.NoteEntity
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

    // 2. 核心魔法：组合数据流 (相当于 Vue 的 computed 计算属性)
    // 这里我们将数据库里的原始数据 (dao.getAllNotes()) 和上面的搜索词、标签组合起来
    val filteredNotes: StateFlow<List<NoteEntity>> = combine(
        dao.getAllNotes(),
        searchQuery,
        selectedTag
    ) { notes, query, tag ->
        // 这里的代码决定了最终展示给用户什么数据
        var result = notes

        // 如果有搜索词，按内容过滤
        if (query.isNotBlank()) {
            result = result.filter { it.content.contains(query, ignoreCase = true) }
        }

        // 如果选中了标签，按标签过滤
        if (tag != null) {
            result = result.filter { it.tags.contains(tag) }
        }

        result // 返回过滤后的结果
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // 当没有界面监听时，节约资源
        initialValue = emptyList() // 初始默认值为空列表
    )

    // ==========================================
    // 3. 定义供 UI 调用的操作方法 (Actions/Methods)
    // ==========================================

    // 更新搜索词
    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    // 更新选中的标签
    fun updateSelectedTag(tag: String?) {
        selectedTag.value = tag
    }

    // 新增笔记（因为数据库操作是耗时的，所以必须放在协程 viewModelScope.launch 里异步执行）
    fun addNote(content: String, tags: List<String>, source: String? = null) {
        viewModelScope.launch {
            val newNote = NoteEntity(content = content, tags = tags, source = source)
            dao.insertNote(newNote)
        }
    }

    // 删除笔记
    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            dao.deleteNote(note)
        }
    }
}