package smartcharge.master

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import smartcharge.master.data.ChargingStation
import smartcharge.master.api.OpenChargeMapApi
import smartcharge.master.data.AddressInfo
import smartcharge.master.data.Charger
import smartcharge.master.data.Connection
import smartcharge.master.network.RetrofitOpenCharge
import smartcharge.master.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private const val PERMISSION_CODE = 101 // Constant for permission request code
        private const val DEFAULT_ZOOM_LEVEL = 15f // Default zoom level for the map
    }

    private lateinit var mMap: GoogleMap // GoogleMap instance
    //Open Charge Map API
    private val apiKey = "5db0fd34-ffee-4845-a38c-73c3029bf25e"
    private lateinit var binding: ActivityMapsBinding // View binding for the activity
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient // Location provider client for fused location services
    private var currentLocation: Location? = null // Variable to store current location

    private var allChargingStations: List<ChargingStation> = emptyList() // List to store all charging stations
    private var userLocationMarker: Marker? = null // Marker for user location

    private lateinit var firebaseDatabase: FirebaseDatabase // Firebase Realtime Database instance
    private lateinit var databaseReference: DatabaseReference // Database reference to "chargers" node
    //private lateinit var placesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater) // Inflate the layout using view binding
        setContentView(binding.root) // Set the content view to the binding's root

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this) // Initialize the fused location provider client
        checkLocationPermissions() // Check for location permissions

        // Initialize Places
        if (!Places.isInitialized()) {
            Log.d(TAG, "Initializing Places API")
            Places.initialize(applicationContext, getString(R.string.google_maps_key))
            //getString(R.string.google_maps_key)//
        }
            //placesClient = Places.createClient(this)//

        // Initialize Firebase Database
        firebaseDatabase = FirebaseDatabase.getInstance("https://smartcharge-project-default-rtdb.asia-southeast1.firebasedatabase.app")
        databaseReference = firebaseDatabase.reference.child("chargers")

        setupBottomNavigationView()
        setupAutocompleteFragment()
        setupFilterButtons()
    }

    private fun setupAutocompleteFragment(){
        // Find the AutocompleteSupportFragment in the layout using its ID
        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        // Create a rectangular boundary for the autocomplete suggestions, restricted to Malaysia
        val bias = RectangularBounds.newInstance(
            LatLng(1.4939, 100.0857), // Southwest corner of Malaysia
            LatLng(7.3558, 119.1812)  // Northeast corner of Malaysia
        )
        // Set the location bias for the autocomplete fragment to the defined rectangular boundary
        autocompleteFragment.setLocationBias(bias)

        // Restrict the autocomplete suggestions to places in Malaysia
        autocompleteFragment.setCountries("MY")

        // Specify the place fields to return from the autocomplete query
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
        // Set a listener to handle the event when a place is selected from the autocomplete suggestions
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // Log the selected place name for debugging purposes
                Log.d(TAG, "Place selected: ${place.name}")
                // Move the camera to the selected place
                place.latLng?.let {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, DEFAULT_ZOOM_LEVEL))
                    // Remove the previous user location marker if it exists
                    userLocationMarker?.remove()
                    // Add a new marker at the selected place and set its title to the place name
                    userLocationMarker = mMap.addMarker(MarkerOptions().position(it).title(place.name))
                }
            }

            override fun onError(status: com.google.android.gms.common.api.Status) {
                // Handle any errors that occur during the place selection process
            }
        })

        // Setup button to return to current location
        binding.btnCurrentLocation.setOnClickListener {
            Log.d(TAG, "Current location button clicked")
            currentLocation?.let {
                val userLatLng = LatLng(it.latitude, it.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, DEFAULT_ZOOM_LEVEL))
                userLocationMarker?.remove()
                userLocationMarker = mMap.addMarker(MarkerOptions().position(userLatLng).title("You are here"))
            }
        }
    }

    private fun setupBottomNavigationView(){
        //bottom navigation bar
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.selectedItemId = R.id.maps // Highlight the maps icon

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.maps -> {
                    // Stay in the current activity
                    true
                }
                R.id.nearbystations -> {
                    val intent = Intent(this, NearbyStationsActivity::class.java)
                    currentLocation?.let {
                        intent.putExtra("CURRENT_LOCATION_LAT", it.latitude)
                        intent.putExtra("CURRENT_LOCATION_LNG", it.longitude)
                    }
                    intent.putExtra("API_KEY", apiKey) // Pass the apiKey
                    startActivity(intent)
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

    private fun setupFilterButtons(){
        // Find the button views by their IDs
        val filterButtonLevel1: Button = findViewById(R.id.btn_filter_level_1)
        val filterButtonLevel2: Button = findViewById(R.id.btn_filter_level_2)
        val filterButtonLevel3: Button = findViewById(R.id.btn_filter_level_3)

        // Create a mutable set to keep track of the selected filter levels
        val selectedLevels = mutableSetOf<Int>()

        // Set click listener for the Level 1 button
        filterButtonLevel1.setOnClickListener{
            Log.d(TAG, "Level 1 button clicked")
            toggleFilterLevel(1, selectedLevels, filterButtonLevel1)
            filterChargingStations(selectedLevels)
        }

        filterButtonLevel2.setOnClickListener {
            Log.d(TAG, "Level 2 button clicked")
            toggleFilterLevel(2, selectedLevels, filterButtonLevel2)
            filterChargingStations(selectedLevels)
        }

        filterButtonLevel3.setOnClickListener {
            Log.d(TAG, "Level 3 button clicked")
            toggleFilterLevel(3, selectedLevels, filterButtonLevel3)
            filterChargingStations(selectedLevels)
        }
    }

    private fun toggleFilterLevel(level: Int, selectedLevels: MutableSet<Int>, button: Button) {
        if (selectedLevels.contains(level)) {
            // If the level is already selected, remove it from the set
            selectedLevels.remove(level)
            button.isSelected = false
        } else {
            // If the level is not selected, add it to the set
            selectedLevels.add(level)
            button.isSelected = true
        }
        Log.d(TAG, "Selected levels after toggle: $selectedLevels")
    }

    private fun filterChargingStations(selectedLevels: Set<Int>) {
        Log.d(TAG, "Filtering stations with levels: $selectedLevels")

        // Store user location marker position
        val userLatLng = currentLocation?.let {
            LatLng(it.latitude, it.longitude)
        }

        mMap.clear()

        // Re-add the user location marker
        userLatLng?.let {
            userLocationMarker = mMap.addMarker(
                MarkerOptions()
                    .position(it)
                    .title("You are here")
            )
        }
        // If no levels are selected, display all charging stations
        val stationsToDisplay = if (selectedLevels.isEmpty()) {
            // If no levels are selected, display all charging stations
            allChargingStations
        } else {
            // Otherwise, filter the charging stations to include only those with selected levels
            allChargingStations.filter { station ->
                station.Connections.any { connection ->
                    selectedLevels.contains(connection.LevelID)
                }
            }
        }

        // Log the number of stations to display
        Log.d(TAG, "Displaying ${stationsToDisplay.size} stations")
        // Display the filtered or all charging stations
        displayChargingStations(stationsToDisplay)
        // Display Approved charging station
        loadApprovedChargers()

//        // Fetch and display nearby amenities for each charging station
//        stationsToDisplay.forEach { station ->
//            fetchNearbyAmenities(LatLng(station.AddressInfo.Latitude, station.AddressInfo.Longitude))
//        }
    }

    //Checks if fine and coarse location permissions are not granted. If not then requests fine location permission.
    private fun checkLocationPermissions() {
        // Check if fine location permission is not granted and coarse location permission is not granted
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If location permissions are not granted, request them
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_CODE)
        } else {
            // If permissions are already granted, get the current location
            getCurrentLocationUser()
        }
    }

    //obtain the user's current location
    private fun getCurrentLocationUser() {
        try {
            // Check if both fine and coarse location permissions are granted
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Request the last known location
                fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        // If location is successfully retrieved, set it to currentLocation
                        currentLocation = it
                        // Show the current location on the map
                        showCurrentLocationOnMap()
                    } ?: run {
                        // If location is null, show an error message
                        showError("Failed to get current location")
                    }
                }.addOnFailureListener {
                    // If there is a failure in retrieving location, show an error message
                    showError("Failed to get current location")
                }
            } else {
                showError("Location permission not granted")
            }
        } catch (e: SecurityException) {
            showError("Security exception: ${e.message}")
        }
    }

    //display the user's current location on the map.
    private fun showCurrentLocationOnMap() {
        currentLocation?.let {
            // If currentLocation is not null, get the map fragment and set up the map
            val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
        }
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if the request code matches and permissions are granted
        if (requestCode == PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // If permission is granted, get the current location
            getCurrentLocationUser()
        } else {
            // If permission is denied, show an error message
            showError("Location permission denied")
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap // Set the GoogleMap instance
        currentLocation?.let {
            // If currentLocation is not null, create a LatLng object with the current location coordinates
            val userLatLng = LatLng(it.latitude, it.longitude)
            // Create marker options for the current location
            val markerOptions = MarkerOptions().position(userLatLng).title("You are here")

            mMap.apply {
                // Move the camera to the current location and add a marker
                moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, DEFAULT_ZOOM_LEVEL))
                addMarker(markerOptions)
            }
            //Fetch Open Charge Data
            fetchChargingStations()
            //Fetch From Real Time Database
            loadApprovedChargers()
        }
        // Enable Bottom Sheet
        mMap.setOnMarkerClickListener { marker ->
            Log.d("MapsActivity", "Marker clicked: ${marker.title}")
            val station = marker.tag as? ChargingStation
            station?.let {
                Log.d("MapsActivity", "ChargingStation marker clicked")
                val bottomSheet = BottomSheetDialog()
                bottomSheet.setChargingStation(it, currentLocation)
                bottomSheet.show(supportFragmentManager, bottomSheet.tag)
            } ?: run {
                val charger = marker.tag as? Charger
                charger?.let {
                    Log.d("MapsActivity", "Charger marker clicked")
                    val bottomSheet = RentBottomSheetDialog()
                    bottomSheet.setCharger(it)
                    Log.d("MapsActivity", "Showing RentBottomSheetDialog for Charger")
                    bottomSheet.show(supportFragmentManager, bottomSheet.tag)
                }
            }
            true
        }
    }

    // Fetch from Open Charge Map
    private fun fetchChargingStations() {
        // Create an instance of the Open Charge Map API using Retrofit
        val api = RetrofitOpenCharge.retrofit.create(OpenChargeMapApi::class.java)
        // Prepare the API call with the provided API key
        val call = api.getChargingStations(apiKey)

        // Execute the API call asynchronously
        call.enqueue(object : Callback<List<ChargingStation>> {
            // Handle the response from the API call
            override fun onResponse(call: Call<List<ChargingStation>>, response: Response<List<ChargingStation>>) {
                // Check if the response is successful
                if (response.isSuccessful) {
                    // Get the response body (list of charging stations) if it is not null
                    response.body()?.let { stations ->
                        // Store the list of all charging stations
                        allChargingStations = stations
                        // Display the charging stations on the map
                        displayChargingStations(stations)
                    } ?: run {
                        // If the response is not successful, log the error message with the response error body
                        Log.e("MapsActivity", "Response body is null")
                    }
                } else {
                    // If response is not successful, log the error
                    Log.e("MapsActivity", "Failed response: ${response.errorBody()?.string()}")
                }
            }
            override fun onFailure(call: Call<List<ChargingStation>>, t: Throwable) {
                // If the API call fails, show an error message
                showError("Failed to fetch charging stations")
            }
        })
    }

    // Function to load approved chargers from the real-time database
    private fun loadApprovedChargers() {
        // Query the database for chargers with a status of "approved"
        databaseReference.orderByChild("status").equalTo("approved")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                // Called when the data is successfully read from the database
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Check if there are any approved chargers
                    if (snapshot.exists()) {
                        // Loop through each charger snapshot in the database
                        for (chargerSnapshot in snapshot.children) {
                            // Convert the snapshot to a Charger object
                            val charger = chargerSnapshot.getValue(Charger::class.java)
                            charger?.let {
                                // Get latitude and longitude from the charger object, skip if null
                                val latitude = it.latitude ?: return@let// Skip if latitude is null
                                val longitude = it.longitude ?: return@let// Skip if longitude is null
                                val location = LatLng(latitude, longitude)

                                // Get the custom marker icon from the vector drawable resource
                                val customIcon = getBitmapDescriptorFromVector(this@MapsActivity, R.drawable.ic_rentchargingstation)

                                // Add a marker on the map at the charger location with a custom icon
                                val marker = mMap.addMarker(
                                    MarkerOptions()
                                        .position(location)
                                        .title(it.address)
                                        .icon(customIcon) // Set the custom icon
                                )
                                marker?.tag = charger // Set the marker tag to the charger object for future reference
                            }
                        }
                    }else {
                        Log.d("MapsActivity", "No approved chargers found in database")
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    // Function to display charging stations on the map
    private fun displayChargingStations(chargingStations: List<ChargingStation>) {
        // Iterate through each charging station in the list
        for (station in chargingStations) {
            // Get the position (latitude and longitude) of the charging station
            val position = LatLng(station.AddressInfo.Latitude, station.AddressInfo.Longitude)

            // Determine the appropriate icon based on the charging level
            val iconResId = when (station.Connections.firstOrNull()?.LevelID) {
                1 -> R.drawable.ic_chargingstationlv1 // Level 1 charging icon
                2 -> R.drawable.ic_chargingstationlv2 // Level 2 charging icon
                3 -> R.drawable.ic_chargingstationlv3 // Level 3 charging icon
                else -> R.drawable.ic_chargingstationdefault // Default icon for unknown levels
            }

            // Create the marker with the custom icon
            val markerOptions = MarkerOptions()
                .position(position) // Set the position of the marker
                .title(station.AddressInfo.Title) // Set the title of the marker
                .icon(getBitmapDescriptorFromVector(this, iconResId)) // Set the custom icon for the marker
            // Add the marker to the map
            val marker = mMap.addMarker(markerOptions)
            // Tag the marker with the station object for future reference
            marker?.tag = station
        }
    }

    // Helper function to convert VectorDrawable to BitmapDescriptor
    private fun getBitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        // Get the vector drawable resource using the provided context and resource ID
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)!!

        // Set the bounds of the drawable to match its intrinsic width and height
        vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)

        // Create a bitmap with the same dimensions as the drawable
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)

        // Create a canvas to draw the drawable onto the bitmap
        val canvas = Canvas(bitmap)

        // Draw the vector drawable onto the canvas, which renders it onto the bitmap
        vectorDrawable.draw(canvas)

        // Convert the bitmap to a BitmapDescriptor and return it
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun showError(message: String) {
        // Show a toast message with the given error message
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

//    private fun fetchNearbyAmenities(location: LatLng) {
//        val placeFields = listOf(Place.Field.NAME, Place.Field.LAT_LNG)
//        val request = FindCurrentPlaceRequest.newInstance(placeFields)
//
//        placesClient.findCurrentPlace(request)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val response: FindCurrentPlaceResponse = task.result
//                    for (placeLikelihood in response.placeLikelihoods) {
//                        val place = placeLikelihood.place
//                        val placeLatLng = place.latLng
//                        if (placeLatLng != null) {
//                            mMap.addMarker(
//                                MarkerOptions()
//                                    .position(placeLatLng)
//                                    .title(place.name)
//                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
//                            )
//                        }
//                    }
//                } else {
//                    val exception = task.exception
//                    Log.e(TAG, "Place not found: ${exception?.message}")
//                }
//            }
}