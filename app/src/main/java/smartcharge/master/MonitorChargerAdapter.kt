package smartcharge.master

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import smartcharge.master.data.Charger
import smartcharge.master.databinding.ItemMonitorChargerBinding

// Adapter for displaying a list of chargers in a RecyclerView
class MonitorChargerAdapter (
    private val chargers: List<Charger>, // List of Charger objects to be displayed
    private val onRemoveClicked: (Charger, String) -> Unit // Callback function for when the remove button is clicked
) : RecyclerView.Adapter<MonitorChargerAdapter.MonitorChargerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonitorChargerViewHolder {
        // Inflate the item layout using view binding
        val binding = ItemMonitorChargerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MonitorChargerViewHolder(binding) // Return a new ViewHolder instance
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: MonitorChargerViewHolder, position: Int) {
        holder.bind(chargers[position]) // Bind the charger at the given position
    }

    // Return the total number of items
    override fun getItemCount(): Int = chargers.size

    // Inner class representing a ViewHolder for a single charger item
    inner class MonitorChargerViewHolder(private val binding: ItemMonitorChargerBinding) : RecyclerView.ViewHolder(binding.root) {
        // Bind the data from a Charger object to the views
        fun bind(charger: Charger) {
            // Set the text views with the charger data
            binding.chargerId.text = charger.chargerId
            binding.address.text = charger.address
            binding.chargerType.text = charger.chargerType
            binding.brandModel.text = charger.brandModel
            binding.powerRating.text = charger.powerRating
            binding.connectorType.text = charger.connectorType
            binding.availability.text = charger.availability
            binding.pricePerUnit.text = charger.pricePerUnit

            // Clear the commentEditText to prevent old data being shown
            binding.commentEditText.text.clear()

            // Load the photo using Glide
            Glide.with(binding.root.context)
                .load(charger.photoUrl)
                .placeholder(R.drawable.ic_smart_charge) // Optional placeholder image
                .into(binding.photo) // ImageView where the photo will be displayed

            // Set the click listeners for the PDF buttons
            binding.ownershipProofButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(charger.ownershipProofUrl)) // Create an intent to view the URL
                binding.root.context.startActivity(intent) // Start the activity with the intent
            }

            binding.installationCertificateButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(charger.installationCertificateUrl)) // Create an intent to view the URL
                binding.root.context.startActivity(intent) // Start the activity with the intent
            }

            binding.removeButton.setOnClickListener {
                val comment = binding.commentEditText.text.toString() // Get the text from the comment EditText
                if (comment.isEmpty()) {
                    Toast.makeText(binding.root.context, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
                } else {
                    // Call the onRemoveClicked callback function with the charger and comment
                    onRemoveClicked(charger, comment)
                }
            }
        }
    }
}