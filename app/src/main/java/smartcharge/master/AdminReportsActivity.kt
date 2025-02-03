package smartcharge.master

import android.content.Intent
import android.os.Bundle
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
import smartcharge.master.data.Report
import smartcharge.master.databinding.ActivityAdminBinding
import smartcharge.master.databinding.ActivityAdminReportsBinding

class AdminReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminReportsBinding // Declare binding object for accessing views
    private lateinit var database: DatabaseReference // Declare database reference object for Firebase
    private lateinit var reportsAdapter: AdminReportsAdapter // Declare adapter for RecyclerView
    private val reportsList: MutableList<Report> = mutableListOf()  // List to hold Report objects

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance("https://smartcharge-project-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child("reports")

        // Setup RecyclerView with a linear layout manager
        binding.reportsRecyclerView.layoutManager = LinearLayoutManager(this)
        // Initialize the adapter and set it to the RecyclerView
        reportsAdapter = AdminReportsAdapter(reportsList)
        binding.reportsRecyclerView.adapter = reportsAdapter

        // Load reports data from Firebase
        loadReports()

        //Set up bottom navigation bar
        setupBottomNavigationView()

        // Set up the logout button
        binding.logoutButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            Toast.makeText(this, "Logout Successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // Function to setup the bottom navigation view
    private fun setupBottomNavigationView() {
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.selectedItemId = R.id.admin_reports

        // Set up navigation item selection listener
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.admin -> {
                    val intent = Intent(this, AdminActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.admin_reports -> {
                    // Stay in the current activity
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

    // Function to load reports from Firebase
    private fun loadReports() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear the previous list of reports
                reportsList.clear()
                // Iterate through each child in the snapshot and add it to the list
                for (reportSnapshot in snapshot.children) {
                    val report = reportSnapshot.getValue(Report::class.java)
                    if (report != null) {
                        reportsList.add(report)
                    }
                }
                // Notify adapter of data changes
                reportsAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors.
            }
        })
    }
}