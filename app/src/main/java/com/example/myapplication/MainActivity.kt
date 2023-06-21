package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var sensorManager: SensorManager
    private lateinit var altitudeSensor: Sensor
    private val SENSOR_TYPE_ALTITUDE = Sensor.TYPE_PRESSURE
    private lateinit var altitudeTextView: TextView
    private val sensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // Not used
        }
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == SENSOR_TYPE_ALTITUDE) {
                val altitude = event.values[0]
                altitudeTextView.text = altitude.toString()
            }
        }
    }

    private lateinit var locationManager: LocationManager
    private lateinit var latitudeTextView: TextView
    private lateinit var longitudeTextView: TextView
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            // Update the latitude and longitude text views with the user's current location
            // Limit latitude and longitude to 5 decimal places
            val latitudeStr = String.format("%.5f", location.latitude)
            val longitudeStr = String.format("%.5f", location.longitude)
            latitudeTextView.text = latitudeStr
            longitudeTextView.text = longitudeStr
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        altitudeSensor = sensorManager.getDefaultSensor(SENSOR_TYPE_ALTITUDE)
        altitudeTextView = findViewById(R.id.altitude_textview)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        latitudeTextView = findViewById(R.id.latitude_textview)
        longitudeTextView = findViewById(R.id.longitude_textview)

        // Check if the user has granted permission to access fine location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Request location updates from the GPS provider
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0f, locationListener)
        } else {
            // Request permission to access fine location
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
        }

        sensorManager.registerListener(sensorListener, altitudeSensor, SensorManager.SENSOR_DELAY_NORMAL)

        val refreshButton: Button = findViewById(R.id.refresh_button)
        refreshButton.setOnClickListener {
            updateLocationData()
        }

        val mapButton: Button = findViewById(R.id.map_button)
        mapButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            val latitude = latitudeTextView.text.toString().toDouble()
            val longitude = longitudeTextView.text.toString().toDouble()
            intent.putExtra("latitude", latitude)
            intent.putExtra("longitude", longitude)
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if the user granted permission to access fine location
        if (requestCode == 0 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Request location updates from the GPS provider
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0f, locationListener)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(sensorListener)
        locationManager.removeUpdates(locationListener)
    }

    private fun updateLocationData() {
        // Get the user's current location
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (location != null) {
            // Update the latitude and longitude text views with the user's current location
            // Limit latitude and longitude to 5 decimal places
            latitudeTextView.text = "0"
            longitudeTextView.text = "0"
            altitudeTextView.text = "0"
            val latitudeStr = String.format("%.5f", location.latitude)
            val longitudeStr = String.format("%.5f", location.longitude)
            latitudeTextView.text = latitudeStr
            longitudeTextView.text = longitudeStr

            // Update the altitude text view with the user's current altitude
            val altitude = location.altitude
            altitudeTextView.text = altitude.toString()
        }
    }
}