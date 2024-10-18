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
import android.Manifest
import android.content.ContentValues
import android.media.ExifInterface
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File

class Camera : AppCompatActivity() {

    private lateinit var btnTomarFoto: Button
    private lateinit var btnAbrirGaleria: Button
    private lateinit var imgPreview: ImageView
    private lateinit var imageUrl: Uri
    private val FILE_NAME = "photo.jpg"

    private val cameraContract =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                imgPreview.setImageURI(null)
                imgPreview.setImageURI(imageUrl)
                saveImageToGallery(imageUrl)
                checkImageResolutionUsingExif(imageUrl)
            } else {
                Toast.makeText(this, "No se pudo tomar la foto", Toast.LENGTH_SHORT).show()
            }
        }

    private val galleryContract =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imgPreview.setImageURI(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        btnTomarFoto = findViewById(R.id.camara)
        btnAbrirGaleria = findViewById(R.id.galeria)
        imgPreview = findViewById(R.id.foto)
        imageUrl = createImageUri()

        btnTomarFoto.setOnClickListener {
            if (checkAndRequestPermissions(
                    arrayOf(android.Manifest.permission.CAMERA),
                    CAMERA_REQUEST_CODE
                )
            ) {
                if (hasCameraHardware()) {
                    imageUrl = createImageUri()
                    cameraContract.launch(imageUrl)
                } else {
                    Toast.makeText(this, "No se encontró hardware de cámara.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        btnAbrirGaleria.setOnClickListener {
            openGallery()
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
        if (checkAndRequestPermissions(
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                GALLERY_REQUEST_CODE
            )
        ) {
            galleryContract.launch("image/*")
        }
    }


    private fun createImageUri(): Uri {
        val image = File(filesDir, FILE_NAME)
        return FileProvider.getUriForFile(
            this,
            "com.icm.taller2_los_surfers.fileprovider",
            image
        )
    }

    private fun hasCameraHardware(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    private fun saveImageToGallery(uri: Uri) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
        val resolver = contentResolver
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        resolver.openOutputStream(imageUri!!).use { outputStream ->
            contentResolver.openInputStream(uri)?.copyTo(outputStream!!)
        }

        Toast.makeText(this, "Imagen guardada en la galería", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    cameraContract.launch(imageUrl)
                } else {
                    Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
                }
            }
            GALLERY_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    galleryContract.launch("image/*")
                } else {
                    Toast.makeText(this, "Permiso de galería denegado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkImageResolutionUsingExif(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val exifInterface = inputStream?.let { ExifInterface(it) }

            val width = exifInterface?.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, -1) ?: -1
            val height = exifInterface?.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, -1) ?: -1

            inputStream?.close()

            Log.d("ExifResolution", "Ancho: $width, Alto: $height")

            if (width >= 1000 && height >= 1000) {
                Toast.makeText(this, "La resolución de la imagen es óptima", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Resolución de imagen baja", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al leer los metadatos de la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 10
        private const val GALLERY_REQUEST_CODE = 11
    }
}

