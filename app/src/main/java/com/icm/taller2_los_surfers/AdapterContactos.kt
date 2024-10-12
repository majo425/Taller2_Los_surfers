package com.icm.taller2_los_surfers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView

class AdapterContactos (private val contactosList: List<String>) : RecyclerView.Adapter<AdapterContactos.ContactoViewHolder>() {

        class ContactoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val contactName: TextView = itemView.findViewById(R.id.contact_name)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactoViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_adapter_contactos, parent, false)
            return ContactoViewHolder(view)
        }

        override fun onBindViewHolder(holder: ContactoViewHolder, position: Int) {
            // Concatenar el número del contacto con su nombre
            holder.contactName.text = "${position + 1}   ${contactosList[position]}"
        }

        override fun getItemCount(): Int {
            return contactosList.size
        }
}