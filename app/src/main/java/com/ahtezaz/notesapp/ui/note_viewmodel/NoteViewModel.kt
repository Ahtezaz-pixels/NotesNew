package com.ahtezaz.mvvmnoting.ui.note_viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahtezaz.mvvmnoting.repository.NoteRepository
import com.ahtezaz.notesapp.db.model.Note
import kotlinx.coroutines.launch


class NoteViewModel(private val repository: NoteRepository) : ViewModel() {


    fun insertNote(note: Note) = viewModelScope.launch {
        repository.insertNote(note)
    }

    fun deleteNote(note: Note) = viewModelScope.launch {
        repository.deleteNote(note)
    }

    fun getListOfNote() = repository.getListOfNotes()
}