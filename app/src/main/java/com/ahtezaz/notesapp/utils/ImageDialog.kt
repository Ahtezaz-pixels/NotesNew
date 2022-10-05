package com.ahtezaz.notesapp.utils

import android.content.Context
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatDialog
import com.ahtezaz.notesapp.databinding.DialogueCameraBinding

class ImageDialog(context: Context, var imageDialogListener: ImageDialogListener) :
    AppCompatDialog(context) {
    lateinit var binding: DialogueCameraBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogueCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lauchCamera.setOnClickListener {
            imageDialogListener.onCameraButtonClick()
            cancel()
        }

        binding.launchGallery.setOnClickListener {
            imageDialogListener.onGalleryButtonClick()
            cancel()
        }

    }
}