package smartcharge.master

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class test : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_test)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }



//    //Initialize
//    private lateinit var mMap: GoogleMap
//    private lateinit var binding: ActivityMapsBinding
//    private lateinit var currentLocation: Location
//    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
//    private val permissionCode = 101
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityMapsBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        // Initialize FusedLocationProviderClient to get user location
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
//        // Get the current location of the user
//        getCurrentLocationUser()
//    }
//
//    private fun getCurrentLocationUser() {
//        // Check for location permissions
//        if (ActivityCompat.checkSelfPermission(
//                this, android.Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this, android.Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // Request location permissions if not granted
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
//                permissionCode
//            )
//            return
//        }
//
//        // Get the last known location of the user
//        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
//            location?.let {
//                currentLocation = it
//                runOnUiThread {
//                    // Display the current location coordinates
//                    Toast.makeText(applicationContext, "${currentLocation.latitude}, ${currentLocation.longitude}", Toast.LENGTH_LONG).show()
//
//                    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//                    val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
//                    mapFragment.getMapAsync(this)
//                }
//            }
//        }.addOnFailureListener {
//            // Handle the error if location fetching fails
//            Toast.makeText(
//                applicationContext,
//                "Failed to get current location",
//                Toast.LENGTH_SHORT
//            ).show()
//        }
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        // Handle the result of permission request
//        when(requestCode){
//            permissionCode -> if(grantResults.isNotEmpty() && grantResults[0]==
//                PackageManager.PERMISSION_GRANTED){
//                // If permission granted, get the current location of the user
//                getCurrentLocationUser()
//            }
//        }
//    }
//
//    override fun onMapReady(googleMap: GoogleMap) {
//        mMap = googleMap
//
//        //Get User current location in Maps
//        if (::currentLocation.isInitialized) {
//            // Create a LatLng object with current location coordinates
//            val userLatLng = LatLng(currentLocation.latitude, currentLocation.longitude)
//            // Create a marker
//            val markerOptions = MarkerOptions().position(userLatLng).title("You are here")
//
//            with(mMap) {
//                //Zoom user to their location with level of 15f
//                moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
//                addMarker(markerOptions)
//            }
//        }
//    }
//
//    // Fetch charging stations from the Open Charge Map API
//    private fun fetchChargingStations() {
//        RetrofitOpenCharge.api.getChargingStations(
//            output = "json",
//            countryCode = "MY", // Change countryCode to Malaysia
//            maxResults = 50,
//            compact = true,
//            verbose = false
//        ).enqueue(object : Callback<List<ChargingStation>> {
//            override fun onResponse(call: Call<List<ChargingStation>>, response: Response<List<ChargingStation>>){
//                if (response.isSuccessful){
//                    response.body()?.let { stations ->
//                        Log.d("MapsActivity", "Fetched ${stations.size} stations")
//                        for (station in stations) {
//                            val position = LatLng(station.AddressInfo.Latitude, station.AddressInfo.Longitude)
//                            Log.d("MapsActivity", "Station: ${station.AddressInfo.Title} at (${station.AddressInfo.Latitude}, ${station.AddressInfo.Longitude})")
//                            val markerOptions = MarkerOptions().position(position).title(station.AddressInfo.Title)
//                            runOnUiThread {
//                                val marker = mMap.addMarker(markerOptions)
//                                marker?.tag = station
//                            }
//                        }
//
////                        // Set a click listener for markers show bottomsheet
////                        mMap.setOnMarkerClickListener { marker ->
////                            val station = marker.tag as? ChargingStation
////                            station?.let { showBottomSheet(it) }
////                            true
////                        }
//                    } ?: run {
//                        Log.e("MapsActivity", "Response body is null")
//                    }
//                } else {
//                    Log.e("MapsActivity", "Failed response: ${response.errorBody()?.string()}")
//                }
//            }
//            override fun onFailure(call: Call<List<ChargingStation>>, t: Throwable) {
//                Toast.makeText(this@MapsActivity, "Failed to fetch charging stations", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
}