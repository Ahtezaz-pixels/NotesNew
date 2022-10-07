package com.ahtezaz.notesapp.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.ahtezaz.mvvmnoting.ui.note_viewmodel.NoteViewModel
import com.ahtezaz.notesapp.databinding.NoteViewerBinding
import com.ahtezaz.notesapp.db.model.Note
import com.ahtezaz.notesapp.ui.NoteDetailScreen
import com.ahtezaz.notesapp.utils.NoteSingleton


class NoteRecycler(
    private val context: Context,
    var noteItems: List<Note>,
    private val viewModel: NoteViewModel,
    var deleteListOfItem: ImageView,
) : RecyclerView.Adapter<NoteRecycler.NoteViewHolder>() {
    val TAG = "TAG"
    var listOfNoteToDelete: MutableList<Note> = mutableListOf()

    inner class NoteViewHolder(val binding: NoteViewerBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = NoteViewerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)

    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = noteItems[position]
        holder.binding.noteTitle.text = note.title
        holder.binding.noteLocation.text = note.location

        holder.binding.card.setBackgroundColor(ContextCompat.getColor(context,
            com.ahtezaz.notesapp.R.color.white))

        /**
         * shows notes details on click
         */
        holder.itemView.setOnClickListener {
            if (listOfNoteToDelete.isEmpty()) {
                NoteSingleton.note = noteItems[position]
                context.startActivity(Intent(context, NoteDetailScreen::class.java))
                Log.d(TAG, "onCreate: Single Click Run")
            } else {
                onClickHandlerForMultipleDelete(holder)
            }
        }

        /**
         * show details for long click listener
         */

        Log.d(TAG, "onCreate: On Bind Run")
        holder.itemView.setOnLongClickListener {
            onClickHandlerForMultipleDelete(holder)
            true
        }


    }

    private fun onClickHandlerForMultipleDelete(holder: NoteViewHolder) {
        Log.d(TAG, "onCreate: Run After Delete")
        holder.binding.card.setBackgroundColor(ContextCompat.getColor(context,
            com.ahtezaz.notesapp.R.color.white))
        if (listOfNoteToDelete.contains(noteItems[holder.adapterPosition])) {
            holder.binding.card.setBackgroundColor(ContextCompat.getColor(context,
                com.ahtezaz.notesapp.R.color.white))
            listOfNoteToDelete.remove(noteItems[holder.adapterPosition])
        } else {
            holder.binding.card.setBackgroundColor(ContextCompat.getColor(context,
                com.ahtezaz.notesapp.R.color.colorCard))
            listOfNoteToDelete.add(noteItems[holder.adapterPosition])
        }
        if (listOfNoteToDelete.isEmpty()) {
            deleteListOfItem.visibility = View.GONE
        } else {
            deleteListOfItem.visibility = View.VISIBLE
        }

    }

    override fun getItemCount(): Int {
        return noteItems.size
    }
}