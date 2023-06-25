package com.example.myapplication

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore

class UserActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        var pos = 0
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val loginFields = findViewById<LinearLayout>(R.id.login_fields)
        val registerFields = findViewById<LinearLayout>(R.id.register_fields)
        val submitButton = findViewById<Button>(R.id.submit_button)
        val usernameLogin = findViewById<EditText>(R.id.username_login)
        val passwordLogin = findViewById<EditText>(R.id.password_login)
        val usernameRegister = findViewById<EditText>(R.id.username_register)
        val passwordRegister = findViewById<EditText>(R.id.password_register)
        val confirmPasswordRegister = findViewById<EditText>(R.id.confirm_password_register)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                pos = tab?.position!!
                when (tab?.position) {
                    0 -> {
                        loginFields.visibility = View.VISIBLE
                        registerFields.visibility = View.GONE
                    }
                    1 -> {
                        loginFields.visibility = View.GONE
                        registerFields.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })


        submitButton.setOnClickListener {

            if(pos.toString() == "0"){
                Log.d("Button", usernameLogin.text.toString() + " " + passwordLogin.text.toString())
                if(usernameLogin.text.toString().length > 0  && passwordLogin.text.toString().length > 0) {
                    loginUser(usernameLogin.text.toString(), passwordLogin.text.toString())
                }
            }else{

                if(usernameRegister.text.toString().length > 0  && passwordRegister.text.toString().length > 0 && confirmPasswordRegister.text.toString().length > 0 ){
                    if( passwordRegister.text.toString() == confirmPasswordRegister.text.toString() ){
                        registerUser(usernameRegister.text.toString(), passwordRegister.text.toString())
                    }else{
                        Toast.makeText(this, "passwords must be the same", Toast.LENGTH_SHORT).show()
                    }

                }else{
                    Toast.makeText(this, "Please fill all the blanks", Toast.LENGTH_SHORT).show()
                }
                Log.d("Button", usernameRegister.text.toString() + " " + passwordRegister.text.toString())
            }

        }
    }
    private fun loginUser(username: String, password: String) {
        val usersRef = db.collection("users")
        usersRef.document(username)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userData = document.data
                    if (userData != null && userData["password"] == password) {
                        // Login successful, store user data and go to main activity
                        val sharedPref = getSharedPreferences("my_app", Context.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString("username", username)
                            apply()
                        }
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Login failed, show error message
                        Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Login failed, show error message
                    Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                // Login failed, show error message
                Toast.makeText(this, "Login failed. ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun registerUser(username: String, password: String) {
        val user = hashMapOf(
            "username" to username,
            "password" to password
        )
        val usersRef = db.collection("users")
        usersRef.document(username)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // User already exists, show error message
                    Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show()
                } else {
                    // User does not exist, create new document
                    usersRef.document(username)
                        .set(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Registration failed. ${it.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error checking username. ${it.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}