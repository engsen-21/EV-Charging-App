package smartcharge.master

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import smartcharge.master.data.ChargingStation
import kotlin.math.roundToInt

//BottomSheetDialogFragment is to populate fields with the data from API response
class BottomSheetDialog : BottomSheetDialogFragment() {

    // Declaration of variables
    private lateinit var station: ChargingStation //Charging station data
    private var currentLocation: Location? = null // Current location data

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_bottom_sheet_dialog, container, false)
    }

    // Method called after the view has been created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize views by finding them by their IDs
        val locationImage = view.findViewById<ImageView>(R.id.location_image)
        val stationName = view.findViewById<TextView>(R.id.station_name)
        val locationName = view.findViewById<TextView>(R.id.address_name)
        val availabilityStatus = view.findViewById<TextView>(R.id.availability_status)
        val distanceText = view.findViewById<TextView>(R.id.distance)
        val quantityPlug = view.findViewById<TextView>(R.id.quantity_plug)
        val fullAddress = view.findViewById<TextView>(R.id.full_address)
        val chargingType = view.findViewById<TextView>(R.id.charging_type)
        val chargingPrice = view.findViewById<TextView>(R.id.charging_cost)
        val navigationIcon = view.findViewById<Button>(R.id.navigation_icon)

        // Set the text of the station name and address name
        stationName.text = station.AddressInfo.Title
        locationName.text = station.AddressInfo.AddressLine1

        // Check availability status
        val isAvailable = station.Connections.any { it.StatusTypeID == 50} // 50 indicates "Available" in Open Charge Map Define
        availabilityStatus.text = if (isAvailable) "Available" else "Not Available"

        // Calculate distance to the station from current location
        val distance = currentLocation?.let {
            val results = FloatArray(1) // Array to hold distance results
            // Calculate the distance between current location and station
            Location.distanceBetween(it.latitude, it.longitude, station.AddressInfo.Latitude, station.AddressInfo.Longitude, results)
            results[0] / 1000 // convert distance in kilometers
        } ?: 0f // If current location is null, distance is 0
        distanceText.text = "Distance: ${distance.roundToInt()} km" // Set distance text

        // Display quantity of plugs
        val totalPlugs = station.Connections.sumOf { it.Quantity ?: 0 } // Sum the quantity of plugs
        quantityPlug.text = "Quantity: $totalPlugs" // Set quantity plug text

        // Set the full address text
        fullAddress.text = """
            ${station.AddressInfo.AddressLine1 ?: ""}
            ${station.AddressInfo.Town ?: ""}
            ${station.AddressInfo.StateOrProvince ?: ""}
            ${station.AddressInfo.Postcode ?: ""}
            """.trimIndent()

        // Set the charging type and price
        chargingType.text = "Charging Type: ${station.Connections.firstOrNull()?.ConnectionType?.Title ?: "Unknown"}"
        chargingPrice.text = "Charging Price: ${station.UsageCost ?: "Unknown"} MYR"

        // Load image using Glide
        val imageUrl = station.MediaItems?.firstOrNull()?.ItemURL
        Glide.with(this)
            .load(imageUrl?.takeIf { it.isNotBlank() } ?: R.drawable.ic_evcharging) // Load image if URL is valid, otherwise load default resource
            .into(locationImage)

        // Set click listener for navigation button
        navigationIcon.setOnClickListener {
            // Create a URI for navigation intent
            val gmmIntentUri = Uri.parse("google.navigation:q=${station.AddressInfo.Latitude},${station.AddressInfo.Longitude}&mode=d&query=restaurants")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri) // Create an intent for navigation
            mapIntent.setPackage("com.google.android.apps.maps") // Set package for Google Maps
            startActivity(mapIntent)
        }
    }

    // Method to set the charging station and current location to pass the data to MapsActivity use
    fun setChargingStation(station: ChargingStation, currentLocation: Location?) {
        this.station = station // Set the station
        this.currentLocation = currentLocation // Set the current location
    }
}