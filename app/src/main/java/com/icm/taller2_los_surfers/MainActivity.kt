package com.icm.taller2_los_surfers

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.Manifest
import android.widget.ImageButton

class MainActivity : AppCompatActivity() {

    private val CONTACTS_PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<ImageButton>(R.id.btnContactos).setOnClickListener {
            requestContactsPermission()
        }
    }

    private fun requestContactsPermission() {
        // Verificar si ya se tiene el permiso
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            // Solicitar el permiso
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), CONTACTS_PERMISSION_REQUEST_CODE)
        } else {
            // Si ya se tiene el permiso, abrir la nueva actividad
            val intent = Intent(this, Contactos::class.java)
            startActivity(intent)
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CONTACTS_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, abrir la nueva actividad
                val intent = Intent(this, Contactos::class.java)
                startActivity(intent)
            } else {
                // Permiso denegado
                Toast.makeText(this, "Funcionalidades reducidas", Toast.LENGTH_SHORT).show()
            }
        }
    }
}