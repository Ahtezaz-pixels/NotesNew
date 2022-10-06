package com.ahtezaz.notesapp.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ahtezaz.notesapp.databinding.ActivityNoteDetailScreenBinding
import com.ahtezaz.notesapp.db.model.Note
import com.ahtezaz.notesapp.utils.NoteSingleton
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds

class NoteDetailScreen : AppCompatActivity() {
    lateinit var note: Note
    val mediaPlayer = MediaPlayer()
    lateinit var adRequest: AdRequest

    lateinit var binding: ActivityNoteDetailScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteDetailScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        note = NoteSingleton.note!!
        /**
         * set values and image view
         */


        MobileAds.initialize(this
        ) { Toast.makeText(this@NoteDetailScreen, " successful ", Toast.LENGTH_SHORT).show(); }

        adRequest = AdRequest.Builder().build()

        // on below line we are loading our
        // ad view with the ad request
        binding.adView.loadAd(adRequest)
        Glide.with(this).load(note.image).into(binding.imageLoader)
        binding.title.text = note.title
        binding.location.text = note.location
        binding.desc.text = note.descriptor
        binding.playAudio.setOnClickListener {
            binding.StopAudio.isEnabled = true
            playAudioFile()
        }
        binding.StopAudio.setOnClickListener {
            mediaPlayer.stop()
            mediaPlayer.reset()
        }

    }

    private fun playAudioFile() {
        mediaPlayer.setDataSource(note.audioFilePath)
        mediaPlayer.prepare()
        mediaPlayer.start()
    }


}