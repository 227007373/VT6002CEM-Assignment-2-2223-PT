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
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var sensorManager: SensorManager
    private lateinit var altitudeSensor: Sensor
    private lateinit var temperatureSensor: Sensor
    private lateinit var humiditySensor: Sensor
    private val SENSOR_TYPE_ALTITUDE = Sensor.TYPE_PRESSURE
    private val SENSOR_TYPE_TEMPERATURE = Sensor.TYPE_AMBIENT_TEMPERATURE
    private val SENSOR_TYPE_HUMIDITY = Sensor.TYPE_RELATIVE_HUMIDITY
    private lateinit var altitudeTextView: TextView
    private lateinit var temperatureTextView: TextView
    private lateinit var humidityTextView: TextView
    private val sensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // Not used
        }

        override fun onSensorChanged(event: SensorEvent) {
            val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL)
            for (sensor in sensorList) {
                if (sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
//                    Log.d("TEST",sensor.toString())
                }
            }
            when (event.sensor.type) {
                SENSOR_TYPE_ALTITUDE -> {
                    val altitude = event.values[0]
                    altitudeTextView.text = altitude.toString() + " (hPa)"
                }
                SENSOR_TYPE_TEMPERATURE -> {
                    val temperature = event.values[0]
                    temperatureTextView.text = temperature.toString() + " ℃"
                }
                SENSOR_TYPE_HUMIDITY -> {
                    val humidity = event.values[0]
                    humidityTextView.text = humidity.toString() + " %"
                }
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
        temperatureSensor = sensorManager.getDefaultSensor(SENSOR_TYPE_TEMPERATURE)
        humiditySensor = sensorManager.getDefaultSensor(SENSOR_TYPE_HUMIDITY)
        altitudeTextView = findViewById(R.id.altitude_textview)
        temperatureTextView = findViewById(R.id.temperature_textview)
        humidityTextView = findViewById(R.id.humidity_textview)

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
        sensorManager.registerListener(sensorListener, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(sensorListener, humiditySensor, SensorManager.SENSOR_DELAY_NORMAL)

        val refreshButton: Button = findViewById(R.id.refresh_button)
        refreshButton.setOnClickListener {
            updateLocationData()
        }

        val mapButton: Button = findViewById(R.id.map_button)
        val compassButton: Button = findViewById(R.id.compass_button)
        mapButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            val latitude = latitudeTextView.text.toString().toDouble()
            val longitude = longitudeTextView.text.toString().toDouble()
            intent.putExtra("latitude", latitude)
            intent.putExtra("longitude", longitude)
            startActivity(intent)
        }
        compassButton.setOnClickListener {
            val intent = Intent(this, CompassActivity::class.java)
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
        // Unregister the sensor listener to avoid memory leaks
        sensorManager.unregisterListener(sensorListener)
        // Remove the location listener to avoid memory leaks
        locationManager.removeUpdates(locationListener)
    }

    private fun updateLocationData() {
        // Check if the user has granted permission to access fine location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Get the last known location from the GPS provider
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            // If the location is not null, update the latitude and longitude text views
            if (location != null) {
                // Limit latitude and longitude to 5 decimal places
                val latitudeStr = String.format("%.5f", location.latitude)
                val longitudeStr = String.format("%.5f", location.longitude)
                latitudeTextView.text = latitudeStr
                longitudeTextView.text = longitudeStr
            }
        } else {
            // Request permission to access fine location
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
        }
    }
}