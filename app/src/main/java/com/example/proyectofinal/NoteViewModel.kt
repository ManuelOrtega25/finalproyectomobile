package com.example.proyectofinal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _selectedFolderId = MutableStateFlow<Long>(1)
    val selectedFolderId: StateFlow<Long> = _selectedFolderId

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow("Todas")
    val selectedCategory: StateFlow<String> = _selectedCategory

    fun setSelectedFolderId(folderId: Long) {
        _selectedFolderId.value = folderId
    }

    // filtros
    val allNotes: StateFlow<List<Note>> = combine(
        repository.allNotes,
        _selectedFolderId,
        _searchQuery,
        _selectedCategory
    ) { notes, folderId, query, category ->
        notes.filter { note ->
            val matchesFolder = note.folderId == folderId
            val matchesQuery = query.isEmpty() ||
                note.title.contains(query, ignoreCase = true) ||
                note.content.contains(query, ignoreCase = true)

            if (category == "Archivadas") {
                matchesFolder && note.isArchived && matchesQuery
            } else {
                val notArchived = !note.isArchived
                val matchesCategory = category == "Todas" || note.category.equals(category, ignoreCase = true)
                matchesFolder && notArchived && matchesQuery && matchesCategory
            }
        }.sortedWith(
            compareByDescending<Note> { it.isPinned }.thenByDescending { it.date }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawNotesList: StateFlow<List<Note>> = combine(
        repository.allNotes,
        _selectedFolderId
    ) { notes, folderId ->
        notes.filter { it.folderId == folderId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun insertNote(note: Note, onInserted: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = repository.insert(note.copy(folderId = _selectedFolderId.value))
            onInserted?.invoke(id)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.delete(note)
        }
    }

    fun archiveNote(note: Note) {
        viewModelScope.launch {
            repository.update(note.copy(isArchived = true))
        }
    }

    fun unarchiveNote(note: Note) {
        viewModelScope.launch {
            repository.update(note.copy(isArchived = false))
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            repository.update(note)
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.update(note.copy(isPinned = !note.isPinned))
        }
    }
}
