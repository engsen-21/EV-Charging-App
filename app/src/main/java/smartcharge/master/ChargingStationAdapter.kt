package smartcharge.master

import android.location.Location
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import smartcharge.master.data.ChargingStation
import androidx.recyclerview.widget.RecyclerView
import smartcharge.master.api.OnChargingStationClickListener
import smartcharge.master.databinding.ActivityItemChargingStationBinding
import kotlin.math.roundToInt

// Adapter class for the RecyclerView to display charging stations
class ChargingStationAdapter(
    private val stations: List<ChargingStation>, // List of charging stations to be displayed
    private val currentLocation: Location?, // Current location of the user
    private val itemClickListener: OnChargingStationClickListener // Click listener for charging station items
) : RecyclerView.Adapter<ChargingStationAdapter.ChargingStationViewHolder>() {

    // Method to create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChargingStationViewHolder {
        // Inflate the item layout for each charging station
        val binding = ActivityItemChargingStationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChargingStationViewHolder(binding)
    }

    // Method to replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ChargingStationViewHolder, position: Int) {
        // Bind the charging station data to the view holder
        holder.bind(stations[position], itemClickListener)
    }

    // Method to return the size of the dataset (invoked by the layout manager)
    override fun getItemCount() = stations.size

    // ViewHolder class that provides a reference to each view in the item layout
    inner class ChargingStationViewHolder(private val binding: ActivityItemChargingStationBinding) : RecyclerView.ViewHolder(binding.root) {

        // Method to bind the charging station data to the views
        fun bind(station: ChargingStation, itemClickListener: OnChargingStationClickListener) {
            // Log the binding process for debugging
            Log.d("BINDING_STATION", "Binding station: ${station.AddressInfo.Title}")

            // Set the text views with charging station data
            binding.stationName.text = station.AddressInfo.Title
            binding.addressName.text = station.AddressInfo.AddressLine1
            binding.availabilityStatus.text = if (station.Connections.any { it.StatusTypeID == 50 }) "Currently Available" else "Not Available"

            // Calculate the distance from the current location to the charging station
            val distance = currentLocation?.let {
                // Create a FloatArray to store the result of the distance calculation
                val results = FloatArray(1)
                // Calculate the distance between the current location and the charging station
                Location.distanceBetween(
                    it.latitude, // Latitude of the current location
                    it.longitude, // Longitude of the current location
                    station.AddressInfo.Latitude, // Latitude of the charging station
                    station.AddressInfo.Longitude, // Longitude of the charging station
                    results // Array to store the calculated distance
                )
                results[0] / 1000 } ?: 0f // Convert distance from meters to kilometers, If currentLocation is null, use a default value of 0.0f (indicating 0 km)
            binding.distance.text = "${distance.roundToInt()} KM" // Display the distance in kilometers, rounded to the nearest integer

            // Set the click listener for the charging station item
            binding.root.setOnClickListener {
                itemClickListener.onChargingStationClick(station)
            }
        }
    }
}
