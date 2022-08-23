package com.example.notesmvvm.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.notesmvvm.repository.NoteRepository

class NoteActivityViewModelFactory(private val repository: NoteRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NoteActivityViewModel(repository) as T
    }
}