package com.example.myapplication

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

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

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var latitudeTextView: TextView
    private lateinit var longitudeTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        altitudeSensor = sensorManager.getDefaultSensor(SENSOR_TYPE_ALTITUDE)
        altitudeTextView = findViewById(R.id.altitude_textview)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        latitudeTextView = findViewById(R.id.latitude_textview)
        longitudeTextView = findViewById(R.id.longitude_textview)

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    // Update the latitude and longitude text views with the user's current location
                    latitudeTextView.text = location.latitude.toString()
                    longitudeTextView.text = location.longitude.toString()
                }
            }
        }

        // Request location updates every 10 seconds
        val locationRequest = LocationRequest.create()
            .setInterval(10000)
            .setFastestInterval(5000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        sensorManager.registerListener(sensorListener, altitudeSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(sensorListener)
    }
}