package smartcharge.master

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import smartcharge.master.data.Charger

class viewChargerActivity : AppCompatActivity() {

    // Lateinit properties for Firebase references and UI elements
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var chargersRecyclerView: RecyclerView
    private lateinit var chargerStatusAdapter: ChargerStatusAdapter
    private lateinit var SubmitAgain: Button
    private lateinit var backButton: ImageView

    // Lateinit properties for Firebase references and UI elements
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // onCreate is called when the activity is first created
        setContentView(R.layout.activity_view_charger)

        // Set the content view to the specified layout
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://smartcharge-project-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child("chargers")

        // Initialize Firebase Auth and Database references
        chargersRecyclerView = findViewById(R.id.chargersRecyclerView)
        SubmitAgain = findViewById(R.id.SubmitAgain)
        backButton = findViewById(R.id.backButton)

        // Set LinearLayoutManager with reverseLayout and stackFromEnd
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true

        // Set the layout manager and adapter for the RecyclerView
        chargersRecyclerView.layoutManager = LinearLayoutManager(this)
        chargerStatusAdapter = ChargerStatusAdapter(mutableListOf())
        chargersRecyclerView.adapter = chargerStatusAdapter

        // Set the layout manager and adapter for the RecyclerView
        loadChargerStatuses()

        SubmitAgain.setOnClickListener {
            val intent = Intent(this, HomeChargerActivity::class.java)
            startActivity(intent)
        }

        backButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()  // Finish the current activity
        }
    }

    // Load the charger statuses from the database
    private fun loadChargerStatuses() {
        // Function to load charger statuses from the database
        val userId = auth.currentUser?.uid ?: return
        Log.d("ViewChargerActivity", "Current User ID: $userId")
        // Get the current user ID from Firebase Auth
        database.orderByChild("userId").equalTo(userId).addValueEventListener(object : ValueEventListener {
            // Query the database for chargers with the current user ID
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val chargerStatuses = mutableListOf<Charger>()
                Log.d("ViewChargerActivity", "DataSnapshot children count: ${dataSnapshot.childrenCount}")
                // onDataChange is called when data at the reference changes
                for (chargerSnapshot in dataSnapshot.children) {
                    Log.d("ViewChargerActivity", "ChargerSnapshot: ${chargerSnapshot.value}")
                    val chargerStatus = chargerSnapshot.getValue(Charger::class.java)
                    Log.d("ViewChargerActivity", "Fetched Charger: $chargerStatus")
                    chargerStatus?.let { chargerStatuses.add(it)}
                }
                // Iterate through the dataSnapshot children and map to Charger objects
                chargerStatusAdapter.updateChargerStatuses(chargerStatuses)
                Log.d("ViewChargerActivity", "Charger Statuses Updated: $chargerStatuses")
            }

            // Update the adapter with the fetched charger statuses
            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("ViewChargerActivity", "Database Error: $databaseError")
            }
        })
    }
}