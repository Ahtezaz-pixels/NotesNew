package com.ahtezaz.notesapp.ui

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.ahtezaz.mvvmnoting.repository.NoteRepository
import com.ahtezaz.mvvmnoting.ui.note_viewmodel.NoteViewModel
import com.ahtezaz.mvvmnoting.ui.note_viewmodel.NoteViewModelProviderFactory
import com.ahtezaz.notesapp.R
import com.ahtezaz.notesapp.databinding.ActivityNotesBinding
import com.ahtezaz.notesapp.db.NoteDatabase
import com.ahtezaz.notesapp.singleton.NoteConstant.INSERT
import com.ahtezaz.notesapp.singleton.NoteConstant.SUCCESS
import com.ahtezaz.notesapp.singleton.NoteConstant.imageCounter
import com.ahtezaz.notesapp.utils.ImageDialog
import com.ahtezaz.notesapp.utils.ImageDialogListener
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.util.*


class NotesActivity : AppCompatActivity() {
    lateinit var binding: ActivityNotesBinding
    lateinit var viewModel: NoteViewModel
    private var locationRequest: LocationRequest? = null
    private var locationManager: LocationManager? = null
    private var id = 0
    private var imageFilePath: String? = null
    private var userLocation: String? = null
    private var isEnabled = false
    private var audioFilePath: String? = null
     private var fileUri: Uri? = null

    companion object {
        private const val TAG = "TAG"

    }

    private lateinit var getImageFromGallery: ActivityResultLauncher<String>
    private lateinit var getAudioFile: ActivityResultLauncher<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val noteRepository = NoteRepository(NoteDatabase(this))
        val noteViewModelProvider = NoteViewModelProviderFactory(noteRepository)
        viewModel = ViewModelProvider(this, noteViewModelProvider)[NoteViewModel::class.java]
        /**
         * open gallery to upload image
         */
        getImageFromGallery =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                if (uri != null) {
                    imageFilePath = getRealPathFromUri(uri)
                    binding.tvInsert.text = SUCCESS
                    binding.tvInsert.setTextColor(ContextCompat.getColor(this, R.color.bg_green))
                    binding.imUpload.setImageURI(uri)
                } else {
                    binding.tvInsert.text = INSERT
                    imageFilePath = null
                }

            }
        /**
         * get Audio File From gallery
         */
        getAudioFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                showSnackbar(uri.toString())
                audioFilePath = getRealPathFromUri(uri)
                Toast.makeText(this, "$audioFilePath", Toast.LENGTH_SHORT).show()
                binding.tvAudio.text = audioFilePath
            } else {
                audioFilePath = null
            }
        }
        /**
         * launch Camera and get Picture
         */

        /**
         * on click listener for image upload
         */
        binding.imUpload.setOnClickListener {

            ImageDialog(this, object : ImageDialogListener {
                override fun onCameraButtonClick() {
                    fileUri = createImageUri()
                    takeImageResult.launch(fileUri)
                }

                override fun onGalleryButtonClick() {
                    getImageFromGallery.launch("image/*")
                }

            }).show()
        }
        /**
         * On Click get User Location
         */
        binding.tvLocation.setOnClickListener {
            getCurrentLocation()
        }
        /**
         * on click get Audio File
         */
        binding.tvAudio.setOnClickListener {
            getAudioFile.launch("audio/*")
        }
        /**
         * on click listener for save note
         */
        binding.btnSaveNote.setOnClickListener {
            playAudioFile()
//            if (isNoteValid()) {
//                id++
//                Log.d(TAG, "onCreate: $id")
//            } else {
//                showSnackbar("Invalid Note Fields")
//            }
        }
    }

    private fun createImageUri(): Uri? {
        ++imageCounter

        val image = File(applicationContext.filesDir, "camera_photo_$imageCounter.png")
        return FileProvider.getUriForFile(applicationContext,
            "com.ahtezaz.notesapp.fileProvider",
            image)
    }

    private val takeImageResult =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                binding.imUpload.setImageURI(fileUri)
                showSnackbar(fileUri.toString())
                imageFilePath = fileUri?.toString()
                showSnackbar(imageFilePath!!)
            } else {
                showSnackbar("Capture Image")
            }
        }

    private fun playAudioFile() {
        val mediaPlayer = MediaPlayer()
        Log.d(TAG, "playAudioFile: $audioFilePath")
        mediaPlayer.setDataSource(audioFilePath)
        mediaPlayer.prepare()
        mediaPlayer.start()
        Log.d(TAG, "playAudioFile: STARTED")
    }

    private fun getCurrentLocation() {
        locationRequest = LocationRequest.create()
        if (isGPSEnabled()) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            ) {
                LocationServices.getFusedLocationProviderClient(this@NotesActivity)
                    .requestLocationUpdates(locationRequest!!, object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            super.onLocationResult(locationResult)
                            LocationServices.getFusedLocationProviderClient(this@NotesActivity)
                                .removeLocationUpdates(this)
                            if (locationResult.locations.size > 0) {
                                val index = locationResult.locations.size - 1
                                val latitude = locationResult.locations[index].latitude
                                val longitude = locationResult.locations[index].longitude
                                val geocoder = Geocoder(this@NotesActivity, Locale.getDefault())
                                val addresses: List<Address> =
                                    geocoder.getFromLocation(latitude, longitude, 1)
                                val cityName: String = addresses[0].getAddressLine(0)
                                binding.tvLocation.text = cityName
                                userLocation = cityName
                            }
                        }
                    }, Looper.getMainLooper())
            } else {
                showSnackbar("Location Permission Required")
                userLocation = null
            }
        } else {
            showSnackbar("Turn On GPS")
            userLocation = null
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.tvTitle, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun isNoteValid(): Boolean {
        if (binding.tvTitle.text.isNullOrEmpty()) {
            setErrorMessage(binding.layoutNoteTitle, "Title Is Required")
            return false
        }

        if (userLocation.isNullOrEmpty()) {
            setErrorMessage(binding.layoutNoteLocation, "Location Is Required")
            return false
        }
        if (binding.tvDesc.text.isNullOrEmpty()) {
            setErrorMessage(binding.layoutNoteDesc, "Description Is Required")
            return false
        }
        if (imageFilePath.isNullOrEmpty()) {
            showSnackbar("Insert Image")
        }

        return true
    }

    private fun isGPSEnabled(): Boolean {

        if (locationManager == null) {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
        isEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        return isEnabled
    }

    private fun setErrorMessage(layoutNoteTitle: TextInputLayout, message: String) {
        layoutNoteTitle.helperText = message
    }


    private fun getRealPathFromUri(uri: Uri): String? {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri: Uri =
                    ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id))
                return getDataColumn(this, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(this, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {

            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(this,
                uri,
                null,
                null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    private fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?,
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor = uri?.let {
                context.contentResolver.query(it, projection, selection, selectionArgs, null)
            }
            if (cursor != null && cursor.moveToFirst()) {
                val index: Int = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }


    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.getAuthority()
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.getAuthority()
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.getAuthority()
    }

    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.getAuthority()
    }

}