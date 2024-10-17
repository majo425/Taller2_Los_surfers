package com.icm.taller2_los_surfers

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

class Camara : AppCompatActivity() {

        private lateinit var btnTomarFoto: Button
        private lateinit var btnAbrirGaleria: Button
        private lateinit var imgPreview: ImageView
        private lateinit var currentPhotoPath: String
        private lateinit var imageUrl: Uri
        private val FILE_NAME = "photo.jpg"

        private val cameraContract = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                imgPreview.setImageURI(null)
                imgPreview.setImageURI(imageUrl)
            } else {
                Toast.makeText(this, "No se pudo tomar la foto", Toast.LENGTH_SHORT).show()
            }
        }

        private val galleryContract = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imgPreview.setImageURI(it)
            }
        }

        companion object {
            const val CAMERA_REQUEST_CODE = 10
            const val GALLERY_PERMISSION_REQUEST_CODE = 11
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_camara)

            btnTomarFoto = findViewById(R.id.camara)
            btnAbrirGaleria = findViewById(R.id.galeria)
            imgPreview = findViewById(R.id.foto)
            imageUrl = createImageUri()

            btnTomarFoto.setOnClickListener {
                if (checkAndRequestPermissions(arrayOf(android.Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)) {
                    if (hasCameraHardware()) {
                        cameraContract.launch(imageUrl)
                    } else {
                        Toast.makeText(this, "No se encontró hardware de cámara.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            btnAbrirGaleria.setOnClickListener {
                if (checkAndRequestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), GALLERY_PERMISSION_REQUEST_CODE)) {
                    openGallery()
                }
            }
        }

        private fun checkAndRequestPermissions(permissions: Array<String>, requestCode: Int): Boolean {
            val permissionsToRequest = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }.toTypedArray()

            return if (permissionsToRequest.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, permissionsToRequest, requestCode)
                false
            } else {
                true
            }
        }

        private fun openGallery() {
            galleryContract.launch("image/*")
        }

        private fun createImageUri(): Uri {
            val image = File(filesDir, FILE_NAME)
            return FileProvider.getUriForFile(this, "com.icm.taller2_los_surfers.fileprovider", image)
        }

        private fun hasCameraHardware(): Boolean {
            return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        }

        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                when (requestCode) {
                    CAMERA_REQUEST_CODE -> {
                        if (hasCameraHardware()) {
                            cameraContract.launch(imageUrl)
                        }
                    }
                    GALLERY_PERMISSION_REQUEST_CODE -> {
                        openGallery()
                    }
                }
            }
        }
    }
