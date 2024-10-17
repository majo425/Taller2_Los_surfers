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

class Camera : AppCompatActivity() {

    private lateinit var btnTomarFoto: Button
    private lateinit var btnAbrirGaleria: Button
    private lateinit var imgPreview: ImageView
    private lateinit var imageUrl: Uri
    private val FILE_NAME = "photo.jpg"

    private val cameraContract =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                imgPreview.setImageURI(imageUrl) // Mostrar la imagen después de tomarla
            } else {
                Toast.makeText(this, "No se pudo tomar la foto", Toast.LENGTH_SHORT).show()
            }
        }

    private val galleryContract =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imgPreview.setImageURI(it) // Mostrar la imagen seleccionada de la galería
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
                    cameraContract.launch(imageUrl) // Lanzar el contrato de la cámara
                } else {
                    Toast.makeText(this, "No se encontró hardware de cámara.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        btnAbrirGaleria.setOnClickListener {
            openGallery() // No se necesita verificar permisos en Android 11 o superior
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
            true // Los permisos ya están concedidos
        }
    }

    private fun openGallery() {
        galleryContract.launch("image/*") // Lanzar el contrato de la galería
    }

    private fun createImageUri(): Uri {
        val image = File(filesDir, FILE_NAME)
        return FileProvider.getUriForFile(
            this,
            "com.icm.taller2_los_surfers.fileprovider",
            image
        ) // Crear URI para la imagen
    }

    private fun hasCameraHardware(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) // Comprobar si hay hardware de cámara
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 10
    }
}

