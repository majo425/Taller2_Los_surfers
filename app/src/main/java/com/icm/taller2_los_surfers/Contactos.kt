package com.icm.taller2_los_surfers

import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.Manifest

class Contactos : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contactos)

        // Configurar el RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_contactos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Obtener la lista de contactos y configurar el adapter
        val contactosList = obtenerContactos()
        val sortedContactosList = contactosList.sorted()

        recyclerView.adapter = AdapterContactos(sortedContactosList)
    }

    private fun obtenerContactos(): List<String> {
        val contactosList = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            val cursor: Cursor? = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME),
                null,
                null,
                null
            )
            cursor?.use {
                while (it.moveToNext()) {
                    val nombre = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    contactosList.add(nombre)
                }
            }
        }
        return contactosList
    }
}