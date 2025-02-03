package smartcharge.master

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import smartcharge.master.data.Charger
import smartcharge.master.databinding.ItemChargerBinding

// Adapter class for the RecyclerView to display chargers
class ChargerAdapter(
    private val chargers: List<Charger>, // List of chargers to be displayed
    // take a Charger object and a String (the comment) as parameters.
    // These lambdas are passed when creating an instance of ChargerAdapter.
    private val onApproveClicked: (Charger, String) -> Unit, // Lambda function to handle approve button click
    private val onRejectClicked: (Charger, String) -> Unit // Lambda function to handle reject button click
) : RecyclerView.Adapter<ChargerAdapter.ChargerViewHolder>(){

    // Method to create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChargerViewHolder {
        // Inflate the item layout for each charger
        val binding = ItemChargerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChargerViewHolder(binding)
    }

    // Method to replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ChargerViewHolder, position: Int) {
        // Bind the charger data to the view holder
        holder.bind(chargers[position])
    }

    // Method to return the size of the dataset (invoked by the layout manager)
    override fun getItemCount(): Int = chargers.size

    // ViewHolder class that provides a reference to each view in the item layout
    inner class ChargerViewHolder(private val binding: ItemChargerBinding) : RecyclerView.ViewHolder(binding.root) {
        // Method to bind the charger data to the views
        fun bind(charger: Charger) {
            // Set the text views with charger data
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
                // URL of the photo
                .load(charger.photoUrl)
                .placeholder(R.drawable.ic_smart_charge) // Optional placeholder image
                .into(binding.photo) // ImageView to load the photo into

            // Set the click listener for the ownership proof button
            // Then Open the URL of the ownership proof in a browser
            binding.ownershipProofButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(charger.ownershipProofUrl))
                binding.root.context.startActivity(intent)
            }

            binding.installationCertificateButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(charger.installationCertificateUrl))
                binding.root.context.startActivity(intent)
            }

            // Set the click listener for the approve button
            binding.approveButton.setOnClickListener {
                val comment = binding.commentEditText.text.toString()
                if (comment.isEmpty()) {
                    // Show a toast message if the comment is empty
                    Toast.makeText(binding.root.context, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
                } else {
                    // Call the onApproveClicked lambda function with the charger and comment
                    onApproveClicked(charger, comment)
                }
            }
            binding.rejectButton.setOnClickListener {
                val comment = binding.commentEditText.text.toString()
                if (comment.isEmpty()) {
                    Toast.makeText(binding.root.context, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
                } else {
                    onRejectClicked(charger, comment)
                }
            }
        }
    }
}