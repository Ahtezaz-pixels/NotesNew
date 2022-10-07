package com.ahtezaz.notesapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ahtezaz.mvvmnoting.repository.NoteRepository
import com.ahtezaz.mvvmnoting.ui.note_viewmodel.NoteViewModel
import com.ahtezaz.mvvmnoting.ui.note_viewmodel.NoteViewModelProviderFactory
import com.ahtezaz.notesapp.adapter.NoteRecycler
import com.ahtezaz.notesapp.databinding.ActivityNotesListBinding
import com.ahtezaz.notesapp.db.NoteDatabase
import com.ahtezaz.notesapp.db.model.Note
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds


class NotesListActivity : AppCompatActivity() {
    lateinit var binding: ActivityNotesListBinding
    lateinit var viewModel: NoteViewModel
    lateinit var noteAdapter: NoteRecycler
    var searchList: MutableList<Note> = mutableListOf()
    lateinit var adRequest: AdRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this) {
            Toast.makeText(this@NotesListActivity,
                " successful ",
                Toast.LENGTH_SHORT).show();
        }

        adRequest = AdRequest.Builder().build()

        // on below line we are loading our
        // ad view with the ad request
        binding.adView.loadAd(adRequest)
        val noteRepository = NoteRepository(NoteDatabase(this))
        val noteViewModelProvider = NoteViewModelProviderFactory(noteRepository)
        viewModel = ViewModelProvider(this, noteViewModelProvider)[NoteViewModel::class.java]
        /**
         * register recycler view
         */

        noteAdapter = NoteRecycler(this, listOf(), viewModel, binding.deleteListOfItem)
        binding.rvNotesItem.layoutManager = LinearLayoutManager(this)
        binding.rvNotesItem.adapter = noteAdapter
        /**
         *
         *add note click listener
         */
        registerAddNoteClickListener()

        /**
         *
         * fetch note from room and update recycler view
         */
        viewModel.getListOfNote().observe(this) {
            if (it.isNotEmpty()) {
                noteAdapter.noteItems = it
                searchList.addAll(it)
                Log.d("TAG", "onCreate: Observer has run ")
                noteAdapter.notifyDataSetChanged()

                Log.d("TAG", "onCreate: Observer has run :${noteAdapter.listOfNoteToDelete.size}")
            } else {
                noteAdapter.noteItems = listOf()
                noteAdapter.notifyDataSetChanged()
            }
        }
        /**
         * delete multiple items from notes at once
         */
        binding.deleteListOfItem.setOnClickListener {
            binding.deleteListOfItem.visibility = View.GONE
            Log.d("TAG",
                "onCreate: Run Before noteAdapter.listOfNoteToDelete. :${noteAdapter.listOfNoteToDelete.size}")
            for (item in noteAdapter.listOfNoteToDelete) viewModel.deleteNote(item)
            noteAdapter.listOfNoteToDelete = mutableListOf()
            Log.d("TAG",
                "onCreate: Run After noteAdapter.listOfNoteToDelete.l :${noteAdapter.listOfNoteToDelete.size}")


        }
        /**
         * search note in recycler view
         */
        binding.searchNotes.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d("TAG", "onQueryTextSubmit:Search Size ${searchList.size} :: Query = {$query}")
                val list = searchList.filter { note ->
                    note.title.contains(query!!)
                }
                Log.d("TAG", "onQueryTextSubmit:List Filter ${list.size} , ::::: Query = {$query}")
                noteAdapter.noteItems = list
                noteAdapter.notifyDataSetChanged()
                Toast.makeText(this@NotesListActivity, "${list.size}", Toast.LENGTH_SHORT).show()
                Log.d("TAG", "onQueryTextSubmit:Search Size ${searchList.size} :: Query = {$query}")
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {

                if (p0.isNullOrEmpty()) {
                    viewModel.getListOfNote().observe(this@NotesListActivity) {
                        if (it.isNotEmpty()) {
                            noteAdapter.noteItems = it
                            noteAdapter.notifyDataSetChanged()
                        }
                    }
                }
                return true
            }

        })
        requestPermission()
    }


    override fun onBackPressed() {
        super.onBackPressed()
        noteAdapter.listOfNoteToDelete = mutableListOf()
        binding.deleteListOfItem.visibility = View.GONE
    }

    private fun registerAddNoteClickListener() {
        binding.btnAddNote.setOnClickListener {
            startActivity(Intent(this, NotesActivity::class.java))
        }
    }


    private fun hasReadExternalStoragePermission() = ActivityCompat.checkSelfPermission(this,
        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun hasWriteExternalStoragePermission() = ActivityCompat.checkSelfPermission(this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun hasCoarseLocationPermission() = ActivityCompat.checkSelfPermission(this,
        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun hasFineLocationPermission() = ActivityCompat.checkSelfPermission(this,
        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun hasCameraPermission() = ActivityCompat.checkSelfPermission(this,
        Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun hasRecordAudioPermission() = ActivityCompat.checkSelfPermission(this,
        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    private fun requestPermission() {
        val permissionToRequest = mutableListOf<String>()
        if (!hasCoarseLocationPermission()) {
            permissionToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (!hasFineLocationPermission()) {
            permissionToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (!hasReadExternalStoragePermission()) {
            permissionToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (!hasWriteExternalStoragePermission()) {
            permissionToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!hasCameraPermission()) {
            permissionToRequest.add(Manifest.permission.CAMERA)
        }
        if (!hasRecordAudioPermission()) {
            permissionToRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        if (permissionToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionToRequest.toTypedArray(), 0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && grantResults.isNotEmpty()) {
            for (i in grantResults.indices) {
                Log.d("TAG", "onRequestPermissionsResult: ${permissions[i]} granted")
            }
        }
    }
}