package com.example.notesmvvm.viewModel

import android.app.DownloadManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesmvvm.model.Note
import com.example.notesmvvm.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteActivityViewModel(private val repository: NoteRepository): ViewModel() {
    fun saveNote(newNote: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.addNote(newNote)
    }
    fun updateNote(existingNote: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateNote(existingNote)
    }
    fun deleteNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteNote(note)
    }
    fun searchNote(query: String): LiveData<List<Note>> = repository.searchNote(query)
    fun getAllNotes(): LiveData<List<Note>> = repository.getNote()
}