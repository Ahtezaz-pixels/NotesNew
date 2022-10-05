package com.ahtezaz.notesapp.utils

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialog
import com.ahtezaz.notesapp.databinding.DialogRecorderBinding

class AudioDialog(context: Context, var audioDialogListener: AudioDialogListener) :
    AppCompatDialog(context) {
    lateinit var binding: DialogRecorderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogRecorderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lauchRecorder.setOnClickListener {
            cancel()
            audioDialogListener.onAudioRecorder()
        }

        binding.launchFromAppRecord.setOnClickListener {
            audioDialogListener.onAudioFromPhone()
            cancel()
        }

    }
}