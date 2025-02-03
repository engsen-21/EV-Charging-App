package smartcharge.master

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import smartcharge.master.data.UserData
import smartcharge.master.databinding.ActivityMapsBinding
import smartcharge.master.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater) // Inflate the layout using view binding
        setContentView(binding.root) // Set the content view to the binding's root

        //Database to Retreive Data
        auth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance("https://smartcharge-project-default-rtdb.asia-southeast1.firebasedatabase.app")
        databaseReference = firebaseDatabase.reference.child("users")

        loadUserProfile()
        setupBottomNavigationView()

        // Load switch state from SharedPreferences
        // Get the SharedPreferences object using the name "app_prefs" and private mode
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        // Retrieve the boolean value for "notifications_enabled" key, default to false if not found
        val isNotificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", false)
        // Set the switch state (checked/unchecked) based on the retrieved value
        binding.notificationLayout.findViewById<Switch>(R.id.notification_switch_layout).isChecked = isNotificationsEnabled

        // Handle notification switch state changes
        // Find the Switch view by its ID and set an OnCheckedChangeListener
        binding.notificationLayout.findViewById<Switch>(R.id.notification_switch_layout).setOnCheckedChangeListener { _, isChecked ->
            // Use the SharedPreferences editor to save the new switch state
            with(sharedPreferences.edit()) {
                putBoolean("notifications_enabled", isChecked) // Save the new state (checked/unchecked)
                apply() // Apply the changes asynchronously
            }

            if (isChecked) {
                // If the switch is checked, subscribe to the "all" topic for push notifications
                FirebaseMessaging.getInstance().subscribeToTopic("all")
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Successfully subscribed
                            Toast.makeText(applicationContext, "Allowed Push Notification", Toast.LENGTH_SHORT).show()
                            Log.d("NotificationSwitch", "Successfully subscribed to 'all' topic")
                        } else {
                            // Subscription failed
                            Toast.makeText(applicationContext, "Subscription failed", Toast.LENGTH_SHORT).show()
                            Log.d("NotificationSwitch", "Failed to subscribe to 'all' topic", task.exception)
                        }
                    }
            } else {
                // If the switch is unchecked, unsubscribe from the "all" topic to stop receiving push notifications
                FirebaseMessaging.getInstance().unsubscribeFromTopic("all")
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Successfully unsubscribed
                            Toast.makeText(applicationContext, "Not Allow Notification", Toast.LENGTH_SHORT).show()
                            Log.d("NotificationSwitch", "Successfully unsubscribed from 'all' topic")
                        } else {
                            // Unsubscription failed
                            Toast.makeText(applicationContext, "Unsubscription failed", Toast.LENGTH_SHORT).show()
                            Log.d("NotificationSwitch", "Failed to unsubscribe from 'all' topic", task.exception)
                        }
                    }
            }
        }
        // Set click listeners for the options
        binding.personalDataLayout.setOnClickListener {
            val intent = Intent(this, PersonalDataActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.homeChargerLayout.setOnClickListener {
            val intent = Intent(this, HomeChargerActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.viewChargerLayout.setOnClickListener {
            val intent = Intent(this, viewChargerActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.reportLayout.setOnClickListener {
            val intent = Intent(this, ReportActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.contactUsLayout.setOnClickListener {
            val intent = Intent(this, ContactUsActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.privacyPolicyLayout.setOnClickListener {
            val intent = Intent(this, PrivacyPolicyActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.aboutUsLayout.setOnClickListener {
            val intent = Intent(this, AboutUsActivity::class.java)
            startActivity(intent)
            finish()
        }

            binding.logoutLayout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
                Toast.makeText(this, "Logout Successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupBottomNavigationView(){
        //bottom navigation bar
        val navView: BottomNavigationView = binding.navView
        navView.selectedItemId = R.id.profile // Highlight the maps icon

        navView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.maps -> {
                    val intent = Intent(this, MapsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nearbystations -> {
                    val intent = Intent(this, NearbyStationsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.profile -> {
                    true
                }
                else -> false
            }
        }
    }
    private fun loadUserProfile() {
        // Retrieve the currently authenticated user's UID
        val userId = auth.currentUser?.uid
        // Retrieve the currently authenticated user's email
        val userEmail = auth.currentUser?.email

        // Check if both userId and userEmail are not null
        if (userId != null && userEmail != null) {
            // Set the email TextView in the binding to display the user's email
            binding.email.text = userEmail

            // Reference the user's data in the Realtime Database using the userId
            databaseReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                // Handle the event when data is read successfully from the database
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(UserData::class.java)
                    if (user != null) {
                        // Set the username or default if null
                        binding.username.text = user.userName ?: "Default Username"
                    } else {
                        binding.username.text = "Default Username"
                    }
                }
                // Handle the event when there is an error reading data from the database
                override fun onCancelled(databaseError: DatabaseError) {
                    // Show a toast message indicating the database error
                    Toast.makeText(this@ProfileActivity, "Database Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // If the user is not signed in (userId or userEmail is null), show a toast message
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show()
        }
    }
}