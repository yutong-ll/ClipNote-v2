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

class ClipNoteViewModel(private val dao: AppDao) : ViewModel() {

    private var lastDeletedNote: NoteEntity? = null

    val searchQuery = MutableStateFlow("")
    val selectedTag = MutableStateFlow<String?>(null)
    val selectedSource = MutableStateFlow<String?>(null)

    val allTags: StateFlow<List<TagEntity>> = dao.getAllTags().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredNotes: StateFlow<List<NoteEntity>> = combine(
        dao.getAllNotes(),
        searchQuery,
        selectedTag,
        selectedSource
    ) { notes, query, tag, source ->
        var result = notes

        if (query.isNotBlank()) {
            result = result.filter { it.content.contains(query, ignoreCase = true) }
        }

        if (tag != null) {
            result = result.filter { it.tags.contains(tag) }
        }

        if (source != null) {
            result = result.filter { it.source.equals(source, ignoreCase = true) }
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

    fun updateSelectedSource(source: String?) {
        selectedSource.value = source
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

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateNote(note.copy(timestamp = System.currentTimeMillis()))
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
            lastDeletedNote = note
            dao.deleteNote(note)
        }
    }

    fun undoDelete() {
        lastDeletedNote?.let { note ->
            viewModelScope.launch(Dispatchers.IO) {
                dao.insertNote(note)
                lastDeletedNote = null
            }
        }
    }
}
