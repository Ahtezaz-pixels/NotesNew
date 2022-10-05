package com.ahtezaz.notesapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ahtezaz.notesapp.databinding.ActivityNoteDetailScreenBinding
import com.ahtezaz.notesapp.db.model.Note
import com.ahtezaz.notesapp.utils.NoteSingleton

class NoteDetailScreen : AppCompatActivity() {
    lateinit var note: Note
    lateinit var binding: ActivityNoteDetailScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteDetailScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        note = NoteSingleton.note!!
        /**
         * set values and image view
         */
        binding.title.text = note.title
        binding.location.text = note.location
        binding.desc.text = note.descriptor
//        binding.imageLoader.setImageBitmap(note.image)

    }


}