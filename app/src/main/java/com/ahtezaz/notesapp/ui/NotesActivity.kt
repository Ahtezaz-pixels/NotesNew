package com.ahtezaz.notesapp.ui

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.media.MediaRecorder
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
import com.ahtezaz.notesapp.db.model.Note
import com.ahtezaz.notesapp.singleton.NoteConstant.INSERT
import com.ahtezaz.notesapp.singleton.NoteConstant.NOTE_ID_COUNTER
import com.ahtezaz.notesapp.singleton.NoteConstant.SUCCESS
import com.ahtezaz.notesapp.singleton.NoteConstant.audioCounter
import com.ahtezaz.notesapp.singleton.NoteConstant.imageCounter
import com.ahtezaz.notesapp.utils.*
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.io.IOException
import java.util.*

class NotesActivity : AppCompatActivity() {
    lateinit var binding: ActivityNotesBinding
    lateinit var viewModel: NoteViewModel
    private var locationRequest: LocationRequest? = null
    private var locationManager: LocationManager? = null
    private var imageFilePath: String? = null
    private var userLocation: String? = null
    private var isEnabled = false
    private var audioFilePath: String? = null
    private var fileUri: Uri? = null
    lateinit var mediaRecorder: MediaRecorder
    private var internalDirectoryPath = ""
    lateinit var addNote: Note


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
         * ads loader
         */

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
            executeImageUploadListener()
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
            executeAudioListener()
        }
        /**
         * on click listener for save note
         */
        binding.btnSaveNote.setOnClickListener {
            ++NOTE_ID_COUNTER
            Log.d(TAG, "CIDKASJKJASDAKSJD: $NOTE_ID_COUNTER")
            if (isNoteValid()) {


                addNote = Note(NOTE_ID_COUNTER,
                    binding.tvTitle.text.toString(),
                    userLocation!!,
                    binding.tvDesc.text.toString(),
                    imageFilePath!!,
                    audioFilePath!!)
                Log.d(TAG, "CIDKASJKJASDAKSJD: $NOTE_ID_COUNTER")
                viewModel.insertNote(addNote)
                showSnackbarGreen("Note Inserted Successfully")
                setValuesToNull()
            }


        }
    }

    private fun executeAudioListener() {
        AudioDialog(this, object : AudioDialogListener {
            override fun onAudioFromPhone() {
                PhoneAudioRecordDialogue(this@NotesActivity, object : RecorderListener {

                    override fun startRecording() {
                        if (ActivityCompat.checkSelfPermission(this@NotesActivity,
                                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                        ) {

                            internalDirectoryPath = applicationContext.filesDir.absolutePath
                            val filePath = File(internalDirectoryPath)
                            mediaRecorder = MediaRecorder()
                            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT)
                            mediaRecorder.setAudioChannels(1)
                            mediaRecorder.setAudioSamplingRate(8000)
                            mediaRecorder.setAudioEncodingBitRate(44100)
                            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

                            if (!filePath.exists()) {
                                filePath.mkdirs()
                            }
                            ++audioCounter
                            val fileName = "$filePath/recording$audioCounter.mp3"
                            showSnackbar(fileName)
                            mediaRecorder.setOutputFile(fileName)

                            try {
                                mediaRecorder.prepare()
                            } catch (e: IOException) {
                                e.printStackTrace()
                                Toast.makeText(this@NotesActivity,
                                    "Sorry! file creation failed!" + e.message,
                                    Toast.LENGTH_SHORT).show()
                                return
                            }
                            mediaRecorder.start()
                            audioFilePath = fileName

                        } else {
                            showSnackbar("Record Audio Permission Required")
                        }
                    }

                    override fun stopRecording() {
                        mediaRecorder.stop()
                        binding.tvAudio.text = audioFilePath
                    }

                }).show()
            }

            override fun onAudioRecorder() {
                getAudioFile.launch("audio/*")
            }
        }).show()

    }

    private fun executeImageUploadListener() {
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

    private fun setValuesToNull() {
        imageFilePath = null
        binding.imUpload.setImageResource(R.drawable.ic_baseline_photo_camera_24)
        binding.tvTitle.text = null
        userLocation = null
        binding.tvLocation.text = null
        audioFilePath = null
        binding.tvAudio.text = null
        binding.tvDesc.text = null
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
        Snackbar.make(binding.tvTitle, message, Snackbar.LENGTH_SHORT)
            .setTextColor(ContextCompat.getColor(this, R.color.red)).show()
    }

    private fun showSnackbarGreen(message: String) {
        Snackbar.make(binding.tvTitle, message, Snackbar.LENGTH_SHORT)
            .setTextColor(ContextCompat.getColor(this, R.color.bg_green)).show()
    }

    private fun isNoteValid(): Boolean {
        if (imageFilePath.isNullOrEmpty()) {
            showSnackbar("Image Is Required")
            return false
        }
        if (binding.tvTitle.text.isNullOrEmpty()) {
            setErrorMessage(binding.layoutNoteTitle, "Title Is Required")
            showSnackbar("Title Is Required")
            return false
        }

        if (userLocation.isNullOrEmpty()) {
            setErrorMessage(binding.layoutNoteLocation, "Location Is Required")
            showSnackbar("Location Is Required")
            return false
        }
        if (audioFilePath.isNullOrEmpty()) {
            setErrorMessage(binding.layoutNoteLocation, "Location Is Required")
            showSnackbar("Audio File Is Required")
            return false
        }

        if (binding.tvDesc.text.isNullOrEmpty()) {
            setErrorMessage(binding.layoutNoteDesc, "Description Is Required")
            return false
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
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

}