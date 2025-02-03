package smartcharge.master

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import smartcharge.master.data.Charger
import smartcharge.master.databinding.ActivityAdminMonitorChargersBinding

    private lateinit var binding: ActivityAdminMonitorChargersBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var chargerAdapter: MonitorChargerAdapter
    private val chargersList = mutableListOf<Charger>()

class AdminMonitorChargersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMonitorChargersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance("https://smartcharge-project-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("chargers")

        // Setup RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        chargerAdapter = MonitorChargerAdapter(chargersList, ::onRemoveClicked)
        binding.recyclerView.adapter = chargerAdapter

        // Load chargers from Firebase
        loadChargers()

        // Setup Bottom navigation bar
        setupBottomNavigationView()

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
        // Query to get chargers with status 'approved'
        databaseReference.orderByChild("status").equalTo("approved")
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
                    Toast.makeText(this@AdminMonitorChargersActivity, "Failed to load chargers: ${error.message}", Toast.LENGTH_SHORT).show()
                    Log.e("AdminMonitorChargersActivity", "Database error: ${error.message}")
                }
            })
    }

    // Function to handle reject of a charger
    private fun onRemoveClicked(charger: Charger, comment: String) {
        // Update the status of the charger to 'approved' in Firebase
        databaseReference.child(charger.chargerId!!)
            .child("status").setValue("rejected")
            .addOnSuccessListener {
                // Update the comment
                databaseReference.child(charger.chargerId!!).child("comment").setValue(comment)
                Toast.makeText(this, "Charger removed successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to remove charger", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupBottomNavigationView() {
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.selectedItemId = R.id.admin_monitor_chargers

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.admin -> {
                    val intent = Intent(this, AdminActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.admin_reports -> {
                    val intent = Intent(this, AdminReportsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.admin_monitor_chargers -> {
                    true
                }
                else -> false
            }
        }
    }
}