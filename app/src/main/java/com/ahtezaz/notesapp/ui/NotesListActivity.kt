package com.ahtezaz.notesapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ahtezaz.mvvmnoting.repository.NoteRepository
import com.ahtezaz.mvvmnoting.ui.note_viewmodel.NoteViewModel
import com.ahtezaz.mvvmnoting.ui.note_viewmodel.NoteViewModelProviderFactory
import com.ahtezaz.notesapp.adapter.NoteRecycler
import com.ahtezaz.notesapp.databinding.ActivityNotesListBinding
import com.ahtezaz.notesapp.db.NoteDatabase

class NotesListActivity : AppCompatActivity() {
    lateinit var binding: ActivityNotesListBinding
    lateinit var viewModel: NoteViewModel
    lateinit var noteAdapter: NoteRecycler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val noteRepository = NoteRepository(NoteDatabase(this))
        val noteViewModelProvider = NoteViewModelProviderFactory(noteRepository)
        viewModel = ViewModelProvider(this, noteViewModelProvider)[NoteViewModel::class.java]
        /**
         * register recycler view
         */

        noteAdapter = NoteRecycler(this, listOf(), viewModel)
        binding.rvNotesItem.layoutManager = LinearLayoutManager(this)
        binding.rvNotesItem.adapter = noteAdapter
        registerAddNoteClickListener()
        viewModel.getListOfNote().observe(this, Observer {
            if (it.isNotEmpty()) {
                noteAdapter.noteItems = it
                noteAdapter.notifyDataSetChanged()
            }
        })

        requestPermission()
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

    private fun requestPermission() {
        var permissionToRequest = mutableListOf<String>()
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