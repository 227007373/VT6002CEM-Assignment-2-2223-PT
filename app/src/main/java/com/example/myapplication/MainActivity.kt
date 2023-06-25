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
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import org.w3c.dom.Text
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    private val sharedPref by lazy { getSharedPreferences("my_app", Context.MODE_PRIVATE) }
    private lateinit var editText: EditText
    private lateinit var submitButton: Button
    private lateinit var loginbutton: Button
    private lateinit var logoutbutton: Button
    private lateinit var journey_button: Button
    private lateinit var sensorManager: SensorManager
    private lateinit var altitudeSensor: Sensor
    private lateinit var temperatureSensor: Sensor
    private lateinit var humiditySensor: Sensor
    private lateinit var last_journey: TextView
    private val SENSOR_TYPE_ALTITUDE = Sensor.TYPE_PRESSURE
    private val SENSOR_TYPE_TEMPERATURE = Sensor.TYPE_AMBIENT_TEMPERATURE
    private val SENSOR_TYPE_HUMIDITY = Sensor.TYPE_RELATIVE_HUMIDITY
    private lateinit var altitudeTextView: TextView
    private lateinit var temperatureTextView: TextView
    private lateinit var humidityTextView: TextView
    private lateinit var warningTextView: TextView
    private var username: Any? = null

    private val sensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // Not used
        }
        private var lastAltitude: Float? = null
        private var lastAltitudeTime: Long = 0
        private var temperature:Float = 0.0F
        private  var humidity:Float = 0.0F
        override fun onSensorChanged(event: SensorEvent) {
            if (event?.sensor?.type == Sensor.TYPE_PRESSURE) {
                val altitude = event.values[0]
                altitudeTextView.text = altitude.toString() + " (hPa)"

                val currentTime = System.currentTimeMillis()
                if (lastAltitude != null && currentTime - lastAltitudeTime < 1000) {
                    val altitudeDiff = altitude - lastAltitude!!
                    val timeDiff = (currentTime - lastAltitudeTime) / 1000f
                    val altitudeRate = altitudeDiff / timeDiff
                    if (altitudeRate > 10) {
                        makeEmergencyCall("12345678")
                        // Altitude is increasing too fast
                        // Do something here, such as triggering an alarm or notifying the user
                    }
                }

                lastAltitude = altitude
                lastAltitudeTime = currentTime
            }

            when (event.sensor.type) {
                SENSOR_TYPE_ALTITUDE -> {
                    val altitude = event.values[0]
                    altitudeTextView.text = altitude.toString() + " (hPa)"
                }
                SENSOR_TYPE_TEMPERATURE -> {
                    temperature = event.values[0]
                    temperatureTextView.text = temperature.toString() + " ℃"
                    checkTemperature(temperature,humidity)
                }
                SENSOR_TYPE_HUMIDITY -> {
                    humidity = event.values[0]
                    humidityTextView.text = humidity.toString() + " %"
                    checkTemperature(temperature,humidity)
                }
            }
        }

    }
    private fun checkTemperature(temperature: Float, humidity: Float) {
        if (temperature > 30) {
            // Temperature is too high, show a warning message
            if(humidity > 70){
                warningTextView.text = "Staying hydrated!🥵 and be ware the wet gouund"
            }else{
            warningTextView.text = "Staying hydrated!🥵"
            }
        } else if (temperature < 10) {
            // Temperature is too low, show a warning message
            if(humidity > 70){
                warningTextView.text = "It is too cold!🥶 and be ware the wet gouund"
            }else {
                warningTextView.text = "It is too cold!🥶"
            }
        }else{
            if(humidity > 70){
                warningTextView.text = "Be ware the wet gouund"
            }else {
                warningTextView.text = "Be Safe👀"
            }
        }
    }
    private val REQUEST_CODE_CALL_PHONE_PERMISSION = 123
    private fun makeEmergencyCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // Request permission to make a phone call
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CODE_CALL_PHONE_PERMISSION)
            return
        }
        Log.d("TEST","CALLING")
        startActivity(intent)
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
        FirebaseApp.initializeApp(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        altitudeSensor = sensorManager.getDefaultSensor(SENSOR_TYPE_ALTITUDE)
        temperatureSensor = sensorManager.getDefaultSensor(SENSOR_TYPE_TEMPERATURE)
        humiditySensor = sensorManager.getDefaultSensor(SENSOR_TYPE_HUMIDITY)
        altitudeTextView = findViewById(R.id.altitude_textview)
        temperatureTextView = findViewById(R.id.temperature_textview)
        humidityTextView = findViewById(R.id.humidity_textview)
        warningTextView = findViewById(R.id.warn_1)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        latitudeTextView = findViewById(R.id.latitude_textview)
        longitudeTextView = findViewById(R.id.longitude_textview)
        last_journey = findViewById(R.id.last_journey)
        editText = findViewById(R.id.editText)
        submitButton = findViewById(R.id.submitButton)
//        val toolbar = findViewById(R.id.toolbar)
        loginbutton = findViewById(R.id.toolbar_button)
        logoutbutton = findViewById(R.id.toolbar_button_logout)
        loginbutton.setOnClickListener {
            // Start the new activity
            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
        }
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
        username = sharedPref.getString("username", null)

        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("my_collection")


        submitButton.setOnClickListener {
            val name = editText.text.toString()
            val latitude = 0.0 // TODO: Get the device's current latitude
            val longitude = 0.0 // TODO: Get the device's current longitude
            val altitude = 0.0 // TODO: Get the device's current altitude
            val temperature = 0.0 // TODO: Get the device's current temperature
            val humidity = 0.0 // TODO: Get the device's current humidity
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
            val formattedDateTime = currentDateTime.format(formatter)
            Log.d("DATA", "You were at "+latitudeTextView.text.toString()+", "+longitudeTextView.text.toString()+" on " + formattedDateTime)

            val stringToStore = "You were at "+latitudeTextView.text.toString()+", "+longitudeTextView.text.toString()+" on " + formattedDateTime + "Remarks: " + editText.text.toString()

            val db = FirebaseFirestore.getInstance()
            val collectionRef = db.collection("my_collection")
            collectionRef.document(username as String).set(mapOf("name" to stringToStore))

            collectionRef.document(username as String).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val name = documentSnapshot.getString("name")
                        if (name != null) {
                            last_journey.text  = name
                            // Do something with the name
                        }
                    } else {
                        // Handle document not found
                    }
                }
                .addOnFailureListener { e ->
                    // Handle error
                }
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


        if (username != null) {
            Toast.makeText(this, "Welcome, $username!", Toast.LENGTH_SHORT).show()
            loginbutton.visibility = View.GONE
            logoutbutton.visibility = View.VISIBLE

            collectionRef.document(username as String).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val name = documentSnapshot.getString("name")
                        if (name != null) {
                            last_journey.text  = name
                            // Do something with the name
                        }
                    } else {
                        // Handle document not found
                    }
                }
                .addOnFailureListener { e ->
                    // Handle error
                }

            editText.visibility=View.VISIBLE
            submitButton.visibility=View.VISIBLE
            last_journey.visibility=View.VISIBLE
        } else {
            loginbutton.visibility = View.VISIBLE
            logoutbutton.visibility = View.GONE
            editText.visibility=View.GONE
            submitButton.visibility=View.GONE
            last_journey.visibility=View.GONE
            // User is not authenticated, go back to login activity
//            val intent = Intent(this, UserActivity::class.java)
//            startActivity(intent)
//            finish()
        }

        logoutbutton.setOnClickListener {
            logoutUser()
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
    private fun logoutUser() {
        // Clear user data from SharedPreferences
        with(sharedPref.edit()) {
            remove("username")
            apply()
        }

        // Set username to null and go back to login activity
        username = sharedPref.getString("username", null)
        loginbutton.visibility = View.VISIBLE
        logoutbutton.visibility = View.GONE
        editText.visibility=View.GONE
        submitButton.visibility=View.GONE
        last_journey.visibility=View.GONE
        Log.d("TEST",username.toString())

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