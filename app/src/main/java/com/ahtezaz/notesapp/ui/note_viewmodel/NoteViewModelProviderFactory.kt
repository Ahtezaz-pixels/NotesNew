package com.ahtezaz.mvvmnoting.ui.note_viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ahtezaz.mvvmnoting.repository.NoteRepository

class NoteViewModelProviderFactory(private val repository: NoteRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NoteViewModel(repository) as T
    }
}