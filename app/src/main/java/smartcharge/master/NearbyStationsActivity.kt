package smartcharge.master

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import smartcharge.master.api.OnChargingStationClickListener
import smartcharge.master.api.OpenChargeMapApi
import smartcharge.master.data.ChargingStation
import smartcharge.master.databinding.ActivityNearbyStationsBinding
import smartcharge.master.network.RetrofitOpenCharge

class NearbyStationsActivity : AppCompatActivity(), OnChargingStationClickListener {

    // View binding for the activity layout
    private lateinit var binding: ActivityNearbyStationsBinding
    // Variable to store the current location
    private var currentLocation: Location? = null
    // Variable to store the API key
    private var apiKey: String? = null

    // Called when the activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNearbyStationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the current location from the intent
        currentLocation = Location("").apply {
            latitude = intent.getDoubleExtra("CURRENT_LOCATION_LAT", 0.0)
            longitude = intent.getDoubleExtra("CURRENT_LOCATION_LNG", 0.0)
        }

        // Retrieve the API key from the intent
        apiKey = intent.getStringExtra("API_KEY")

        // Fetch the charging stations and set up the bottom navigation view
        fetchChargingStations()
        setupBottomNavigationView()
    }

    // Set up the bottom navigation view
    private fun setupBottomNavigationView(){
        //bottom navigation bar
        val navView: BottomNavigationView = binding.navView
        navView.selectedItemId = R.id.nearbystations // Highlight the nearby stations icon

        // Set the item selected listener for the bottom navigation view
        navView.setOnItemSelectedListener { item: MenuItem ->
            Log.d("NAVIGATION", "Selected item: ${item.itemId}")
            when (item.itemId) {
                R.id.maps -> {
                    val intent = Intent(this, MapsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nearbystations -> {
                    // Stay in the current activity
                    true
                }
                R.id.profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
    // Fetch the charging stations from the Open Charge Map API
    private fun fetchChargingStations() {
        // Show the progress bar and hide the recycler view
        binding.progressBar.visibility = View.VISIBLE
        binding.logoImageView.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE

        // Create an instance of the Open Charge Map API using Retrofit
        val api = RetrofitOpenCharge.retrofit.create(OpenChargeMapApi::class.java)
        // Prepare the API call with the provided API key
        val call = api.getChargingStations(apiKey ?: "")

        // Execute the API call asynchronously
        call.enqueue(object : Callback<List<ChargingStation>> {
            // Handle the response from the API call
            override fun onResponse(call: Call<List<ChargingStation>>, response: Response<List<ChargingStation>>) {
                if (response.isSuccessful) {
                    // If the response is successful, get the list of charging stations from the response body
                    response.body()?.let { stations ->
                        Log.d("API_RESPONSE", "Received ${stations.size} stations")
                        // Filter and sort the stations based on distance from the current location
                        val nearbyStations = stations.filter { station ->
                            val stationLocation = Location("").apply {
                                // Create a Location object for the station's location
                                latitude = station.AddressInfo.Latitude
                                longitude = station.AddressInfo.Longitude
                            }
                            // Calculate the distance from the current location to the station in kilometers
                            val distance = currentLocation?.distanceTo(stationLocation)?.div(1000) ?: 0f
                            // Include the station if it is within 10 kilometers
                            distance <= 10
                            // Timsort Algorithm
                        }.sortedBy { station ->
                            // Create a Location object for the station's location
                            val stationLocation = Location("").apply {
                                latitude = station.AddressInfo.Latitude
                                longitude = station.AddressInfo.Longitude
                            }
                            // Sort the stations by distance from the current location
                            currentLocation?.distanceTo(stationLocation) ?: 0f
                        }
                        // Display the nearby charging stations
                        displayChargingStations(nearbyStations)
                    }
                }else {
                    // If the response is not successful, log an error message
                    Log.e("API_ERROR", "Response not successful: ${response.errorBody()?.string()}")
                    // Hide the progress bar and logo and show a message
                    binding.progressBar.visibility = View.GONE
                    binding.logoImageView.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<List<ChargingStation>>, t: Throwable) {
                // Handle error
                Log.e("API_ERROR", "API call failed: ${t.message}")
                // Hide the progress bar and logo and show a message
                binding.progressBar.visibility = View.GONE
                binding.logoImageView.visibility = View.GONE
            }
        })
    }

    // Display the charging stations in the RecyclerView
    private fun displayChargingStations(stations: List<ChargingStation>) {
        Log.d("DISPLAY_STATIONS", "Displaying ${stations.size} stations")
        // Create and set the adapter for the RecyclerView
        val adapter = ChargingStationAdapter(stations, currentLocation, this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Hide the progress bar and logo and show the recycler view
        binding.progressBar.visibility = View.GONE
        binding.logoImageView.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
    }

    // Handle the click event on a charging station
    override fun onChargingStationClick(station: ChargingStation) {
        showBottomSheetDialog(station)
    }

    // Show the bottom sheet dialog with the charging station details
    private fun showBottomSheetDialog(station: ChargingStation) {
        val bottomSheet = BottomSheetDialog()
        bottomSheet.setChargingStation(station, currentLocation)
        bottomSheet.show(supportFragmentManager, bottomSheet.tag)
    }
}