package smartcharge.master

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import smartcharge.master.data.Charger

// Adapter class for the RecyclerView to display charger statuses
class ChargerStatusAdapter (private var chargers: MutableList<Charger>) :
    RecyclerView.Adapter<ChargerStatusAdapter.ChargerStatusViewHolder>() {

    // ViewHolder class that provides a reference to each view in the item layout
    class ChargerStatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Declare TextViews for each item property
        val addressTextView: TextView = itemView.findViewById(R.id.addressTextView)
        val brandModelTextView: TextView = itemView.findViewById(R.id.brandModelTextView)
        val chargerTypeTextView: TextView = itemView.findViewById(R.id.chargerTypeTextView)
        val connectorTypeTextView: TextView = itemView.findViewById(R.id.connectorTypeTextView)
        val powerRatingTextView: TextView = itemView.findViewById(R.id.powerRatingTextView)
        val pricePerUnitTextView: TextView = itemView.findViewById(R.id.pricePerUnitTextView)
        val latitudeTextView: TextView = itemView.findViewById(R.id.latitudeTextView)
        val longitudeTextView: TextView = itemView.findViewById(R.id.longitudeTextView)
        val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
        val commentTextView: TextView = itemView.findViewById(R.id.commentTextView)
    }

    // Method to create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChargerStatusViewHolder {
        // Inflate the item layout for each charger status
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_charger_status, parent, false)
        return ChargerStatusViewHolder(view)
    }

    // Method to replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ChargerStatusViewHolder, position: Int) {
        // Get the charger status at the given position
        val chargerStatus = chargers[position]

        // Bind the charger status data to the TextViews
        holder.addressTextView.text = "Address: ${chargerStatus.address}"
        holder.brandModelTextView.text = "Brand/Model: ${chargerStatus.brandModel}"
        holder.chargerTypeTextView.text = "Charger Type: ${chargerStatus.chargerType}"
        holder.connectorTypeTextView.text = "Connector Type: ${chargerStatus.connectorType}"
        holder.powerRatingTextView.text = "Power Rating: ${chargerStatus.powerRating}"
        holder.pricePerUnitTextView.text = "Price Per Unit: ${chargerStatus.pricePerUnit}"
        holder.latitudeTextView.text = "Latitude: ${chargerStatus.latitude}"
        holder.longitudeTextView.text = "Longitude: ${chargerStatus.longitude}"
        holder.statusTextView.text = "Status: ${chargerStatus.status}"
        holder.commentTextView.text = "Comment: ${chargerStatus.comment}"
    }

    // Method to return the size of the dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return chargers.size
    }

    // Method to update the list of charger statuses
    fun updateChargerStatuses(newChargerStatuses: List<Charger>) {
        // Clear the old list and add all items from the new list
        chargers.clear()
        chargers.addAll(newChargerStatuses)
        // Notify the adapter that the data has changed
        notifyDataSetChanged()
    }
}