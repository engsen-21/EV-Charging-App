package smartcharge.master

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import smartcharge.master.data.Charger
import smartcharge.master.databinding.ActivityAdminBinding

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var chargerAdapter: ChargerAdapter // Declare adapter for RecyclerView
    private val chargersList = mutableListOf<Charger>() // List to hold Charger objects
    private var adminId: String? = null // Variable to hold the admin ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the binding object and set the content view
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the adminId from the intent or saved instance state
        adminId = intent.getStringExtra("adminId")

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance("https://smartcharge-project-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("chargers")

        // Setup RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        chargerAdapter = ChargerAdapter(chargersList, ::onApproveClicked, ::onRejectClicked)
        binding.recyclerView.adapter = chargerAdapter

        // Load chargers from Firebase
        loadChargers()

        // Setup Bottom navigation bar
        setupBottonNavigatioView()

        // Set up the logout button
        binding.logoutButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            Toast.makeText(this, "Logout Successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // Function to load chargers from Firebase
    private fun loadChargers() {
        // Query to get chargers with status 'pending'
        databaseReference.orderByChild("status").equalTo("pending")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chargersList.clear()
                    // Iterate through each child in the snapshot and add it to the list
                    for (dataSnapshot in snapshot.children) {
                        val charger = dataSnapshot.getValue(Charger::class.java)
                        charger?.let { chargersList.add(it) }
                    }
                    // Notify adapter of data changes
                    chargerAdapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@AdminActivity, "Failed to load chargers: ${error.message}", Toast.LENGTH_SHORT).show()
                    Log.e("AdminActivity", "Database error: ${error.message}")
                }
            })
    }

    // Function to handle approval of a charger
    private fun onApproveClicked(charger: Charger, comment: String) {
        // Update the status of the charger to 'approved' in Firebase
        databaseReference.child(charger.chargerId!!)
            .child("status").setValue("approved")
            .addOnSuccessListener {
                // Update the comment and admin ID
                databaseReference.child(charger.chargerId!!).child("comment").setValue(comment)
                databaseReference.child(charger.chargerId!!).child("adminId").setValue(adminId)
                Toast.makeText(this, "Charger approved successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to approve charger", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to handle rejection of a charger
    private fun onRejectClicked(charger: Charger, comment: String) {
        // Update the status of the charger to 'rejected' in Firebase
        databaseReference.child(charger.chargerId!!)
            .child("status").setValue("rejected")
            .addOnSuccessListener {
                // Update the comment and admin ID
                databaseReference.child(charger.chargerId!!).child("comment").setValue(comment)
                databaseReference.child(charger.chargerId!!).child("adminId").setValue(adminId)
                Toast.makeText(this, "Charger rejected successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to reject charger", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to setup the bottom navigation view
    private fun setupBottonNavigatioView() {
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.selectedItemId = R.id.admin

        // Set up navigation item selection listener
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.admin ->{
                    true
                }
                R.id.admin_reports -> {
                    val intent = Intent(this, AdminReportsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.admin_monitor_chargers -> {
                    val intent = Intent(this, AdminMonitorChargersActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}