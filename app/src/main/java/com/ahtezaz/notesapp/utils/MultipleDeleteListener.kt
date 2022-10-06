package com.ahtezaz.notesapp.utils

import com.ahtezaz.notesapp.db.model.Note

interface MultipleDeleteListener {

    fun deleteNotes(notesToDelete: List<Note>)
}