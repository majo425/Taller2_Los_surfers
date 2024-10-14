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
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.icm.taller2_los_surfers.databinding.ActivityMapaBinding
import org.json.JSONObject
import java.io.File
import java.io.IOException
import android.Manifest


class Mapa : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapaBinding

    private lateinit var sensorManager: SensorManager
    private lateinit var lightSensor: Sensor
    private lateinit var lightSensorListener: SensorEventListener

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var ultimaLocacion: Location? = null

    // Inicializar el objeto Geocoder
    private lateinit var mGeocoder: Geocoder

    // Límites geográficos para la búsqueda de direcciones
    companion object {
        const val lowerLeftLatitude = 1.396967
        const val lowerLeftLongitude = -78.903968
        const val upperRightLatitude = 11.983639
        const val upperRightLongitude = -71.869905
    }

    private val LOCATION_PERMISSION_REQUEST_CODE = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Geocoder y locacion del cliente
        mGeocoder = Geocoder(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtener el SupportMapFragment y notificar cuando el mapa esté listo.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Inicializar sensores
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!!

        //Listener del sensor luz
        lightSensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (::mMap.isInitialized) {
                    if (event != null) {
                        if (event.values[0] < 3000) {
                            Log.i("MAPS", "DARK MAP" + event.values[0])
                            mMap.setMapStyle(
                                MapStyleOptions.loadRawResourceStyle(
                                    baseContext,
                                    R.raw.night_mode
                                )
                            )  //dark mode
                        } else {
                            Log.i("MAPS", "LIGHT MAP" + event.values[0])
                            mMap.setMapStyle(
                                MapStyleOptions.loadRawResourceStyle(
                                    baseContext,
                                    R.raw.default_mode
                                )
                            ) //LIGHT MODE
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            }
        }

        // Listener para el EditText
        binding.texto.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                buscarDireccion(binding.texto.text.toString())
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
        seguirUbicacion()
    }
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(lightSensorListener)
        detenerSeguimientoUbicacion()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.night_mode))

        obtenerUbicacionActual()

        mMap.setOnMapLongClickListener { latLng -> marcarPosicion(latLng) }
    }

    private fun obtenerUbicacionActual() {
        if (!verificarPermisosUbicacion()) {
            return
        }

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val posicion = LatLng(location.latitude, location.longitude)
                    mMap.addMarker(MarkerOptions().position(posicion).title("Ubicación actual"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicion, 15F))
                    ultimaLocacion = location
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }



    private fun seguirUbicacion() {
        if (!verificarPermisosUbicacion()) {
            return
        }

        try {
            val locationRequest = LocationRequest.create().apply {
                interval = 10000
                fastestInterval = 5000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult ?: return
                    for (location in locationResult.locations) {
                        actualizarUbicacion(location)
                    }
                }
            }
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            // Maneja la excepción si los permisos fallan en tiempo de ejecución
            e.printStackTrace()
        }
    }

    private fun detenerSeguimientoUbicacion() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun actualizarUbicacion(location: Location) {
        ultimaLocacion?.let{
            val distancia = it.distanceTo(location)
            if (distancia > 30) {
                val posicion = LatLng(location.latitude, location.longitude)
                mMap.clear()
                mMap.addMarker(MarkerOptions().position(posicion).title("Nueva ubicación"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicion, 15F))
                guardarUbicacion(location)
                ultimaLocacion = location
            }
        }
    }

    private fun guardarUbicacion(location: Location) {
        val data = mapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "timestamp" to System.currentTimeMillis()
        )
        val archivo = File(applicationContext.filesDir, "assets/locaciones.json")
        archivo.appendText(JSONObject(data).toString()+"\n")
    }

    private fun buscarDireccion(direccion: String){
        try {
            val addresses: List<Address>? = mGeocoder.getFromLocationName(
                direccion, 1, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude
            )
            if(addresses != null && addresses.isNotEmpty()){
                val addressResult = addresses[0]
                val posicion = LatLng(addressResult.latitude, addressResult.longitude)
                mMap.addMarker(MarkerOptions().position(posicion).title(direccion))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicion, 15F))

                // Calcular y mostrar la distancia
                if (ultimaLocacion != null) {
                    val distancia = calcularDistancia(posicion)
                    Toast.makeText(this, "Distancia: $distancia metros", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Ubicación actual no disponible", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Dirección no encontrada", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error al buscar la dirección", Toast.LENGTH_SHORT).show()
        }
    }


    private fun marcarPosicion(latLng: LatLng) {
        try {
            val addresses: List<Address>? = mGeocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            val direccion = addresses?.firstOrNull()?.getAddressLine(0) ?: "Sin dirección"
            mMap.addMarker(MarkerOptions().position(latLng).title(direccion))

            // Mover la cámara al marcador creado
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F))

            // Mostrar distancia
            if (ultimaLocacion != null) {
                val distancia = calcularDistancia(latLng)
                Toast.makeText(this, "Distancia: $distancia metros", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ubicación actual no disponible", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error al obtener la dirección", Toast.LENGTH_SHORT).show()
        }
    }



    private fun calcularDistancia(destino:LatLng): Float {
        val resultado = FloatArray(1)
        Location.distanceBetween(ultimaLocacion!!.latitude, ultimaLocacion!!.longitude, destino.latitude, destino.longitude, resultado)
        return resultado[0]
    }

    private fun verificarPermisosUbicacion(): Boolean {
        return if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            false // No tiene permisos, los estamos solicitando
        } else {
            true // Ya tiene permisos
        }
    }

}