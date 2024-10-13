package com.icm.taller2_los_surfers

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.icm.taller2_los_surfers.databinding.ActivityMapaBinding
import java.io.IOException


class Mapa : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapaBinding

    private lateinit var sensorManager: SensorManager
    private lateinit var lightSensor: Sensor
    private lateinit var lightSensorListener: SensorEventListener

    // Inicializar el objeto Geocoder
    private lateinit var mGeocoder: Geocoder

    // Declarar la constante para el código de solicitud de permisos
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    // Límites geográficos para la búsqueda de direcciones
    companion object {
        const val lowerLeftLatitude = 1.396967
        const val lowerLeftLongitude = -78.903968
        const val upperRightLatitude = 11.983639
        const val upperRightLongitude = -71.869905
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Geocoder
        mGeocoder = Geocoder(this)

        // Pedir permisos de localización
        pedirPermiso(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            "Necesitamos acceder a tu ubicación para mostrar el mapa",
            LOCATION_PERMISSION_REQUEST_CODE
        )

        // Obtener el SupportMapFragment y notificar cuando el mapa esté listo.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Inicializar sensores
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!!

        //Asociar el listener
        lightSensorListener = object : SensorEventListener {
            override fun onSensorChanged(event : SensorEvent?) {
                if(::mMap.isInitialized){
                    if (event != null) {
                        if(event.values[0]<3000){
                            Log.i("MAPS", "DARK MAP" + event.values[0])
                            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(baseContext, R.raw.night_mode))  //dark mode
                        } else{
                            Log.i("MAPS", "LIGHT MAP" + event.values[0])
                            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(baseContext, R.raw.default_mode)) //LIGHT MODE
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor,accuracy: Int) {
            }
        }
        // Listener para el EditText
        /*binding.texto.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val addressString = binding.texto.text.toString()
                if (addressString.isNotEmpty()) {
                    try {
                        val addresses = mGeocoder.getFromLocationName(addressString, 2)
                        if (addresses != null && addresses.isNotEmpty()) {
                            val addressResult = addresses[0]
                            val position = LatLng(addressResult.latitude, addressResult.longitude)
                            if (::mMap.isInitialized) {
                                // Agregar marcador al mapa
                                mMap.addMarker(MarkerOptions().position(position).title(addressString))
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15F))
                            }
                        } else {
                            Toast.makeText(this, "Dirección no encontrada", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this, "Error al buscar la dirección", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "La dirección está vacía", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }*/
    // Listener para el EditText
    binding.texto.setOnEditorActionListener { v, actionId, event ->
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            val addressString = binding.texto.text.toString()
            if (addressString.isNotEmpty()) {
                try {
                    // Llamada a Geocoder con límites geográficos
                    val addresses: List<Address>? = mGeocoder.getFromLocationName(
                        addressString,
                        2,
                        lowerLeftLatitude,
                        lowerLeftLongitude,
                        upperRightLatitude,
                        upperRightLongitude
                    )

                    if (addresses != null && addresses.isNotEmpty()) {
                        val addressResult = addresses[0]
                        val position = LatLng(addressResult.latitude, addressResult.longitude)
                        if (::mMap.isInitialized) {
                            // Agregar marcador al mapa
                            mMap.addMarker(MarkerOptions().position(position).title(addressString))
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15F))
                        }
                    } else {
                        Toast.makeText(this, "Dirección no encontrada", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error al buscar la dirección", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "La dirección está vacía", Toast.LENGTH_SHORT).show()
            }
            true
        } else {
            false
        }
    }
}

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(lightSensorListener, lightSensor,
            SensorManager.SENSOR_DELAY_NORMAL)
    }
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(lightSensorListener)
    }

    override fun onMapReady(googleMap: GoogleMap) {
            mMap = googleMap

            mMap.uiSettings.isZoomGesturesEnabled = true
            mMap.uiSettings.isZoomControlsEnabled = true
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.night_mode))

            //mMap.moveCamera(CameraUpdateFactory.zoomTo(10F))

            // Añadir un marcador en Sydney y mover la cámara
            val sydney = LatLng(4.62, -74.06)

            val pinJave = mMap.addMarker(
                MarkerOptions().position(sydney).title("Marker in Jave").snippet("La vida es bella")
                    .alpha(0.5F)
            )

            mMap.moveCamera(CameraUpdateFactory.zoomTo(10F))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 10F))

            pinJave?.isVisible = true
    }

    // Método para solicitar permisos
    private fun pedirPermiso(
        context: Activity,
        permisos: Array<String>,
        justificacion: String,
        idCode: Int
    ) {
        if (permisos.any {
                ContextCompat.checkSelfPermission(
                    context,
                    it
                ) != PackageManager.PERMISSION_GRANTED
            }) {
            // Mostrar justificación si es necesario
            if (permisos.any { ActivityCompat.shouldShowRequestPermissionRationale(context, it) }) {
                Toast.makeText(context, justificacion, Toast.LENGTH_SHORT).show()
            }
            ActivityCompat.requestPermissions(context, permisos, idCode)
        }
    }

    // Manejar el resultado de la solicitud de permisos
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    mMap.isMyLocationEnabled = true
                }
            } else {
                Toast.makeText(this, "Permiso de localización denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}