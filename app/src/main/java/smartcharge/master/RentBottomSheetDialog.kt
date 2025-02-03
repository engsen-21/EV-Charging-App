package smartcharge.master

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import smartcharge.master.data.Charger

class RentBottomSheetDialog : BottomSheetDialogFragment() {

    private lateinit var charger: Charger

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_rent_bottom_sheet_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chargerImage = view.findViewById<ImageView>(R.id.charger_image)
        val homeAddress = view.findViewById<TextView>(R.id.home_address)
        val ownerPhone = view.findViewById<TextView>(R.id.owner_phone)
        val chargerType = view.findViewById<TextView>(R.id.charger_type)
        val brandModel = view.findViewById<TextView>(R.id.brand_model)
        val powerRating = view.findViewById<TextView>(R.id.power_rating)
        val connectorType = view.findViewById<TextView>(R.id.connector_type)
        val availability = view.findViewById<TextView>(R.id.availability)
        val pricePerUnit = view.findViewById<TextView>(R.id.price_per_unit)
        val navigateButton = view.findViewById<Button>(R.id.navigation_button)

        // Populate the data
        // Assuming you have a method to load the image into the ImageView
        // loadImageIntoView(charger.photoUrl, chargerImage)
        homeAddress.text = charger.address
        ownerPhone.text = charger.phoneNo
        chargerType.text = charger.chargerType
        brandModel.text = charger.brandModel
        powerRating.text = charger.powerRating
        connectorType.text = charger.connectorType
        availability.text = charger.availability
        pricePerUnit.text = charger.pricePerUnit

        val imageUrl = charger.photoUrl
        Log.d("RentBottomSheetDialog", "Loading image URL: $imageUrl")

        // Load image using Glide, with a default image for null URLs
        Glide.with(this)
            .load(imageUrl?.takeIf { it.isNotBlank() } ?: R.drawable.ic_evcharging)
            .into(chargerImage)

        navigateButton.setOnClickListener {
            // Create a URI for navigation intent with nearby restaurants mode=d = drive then query to restaurants
            val gmmIntentUri = Uri.parse("google.navigation:q=${charger.latitude},${charger.longitude}&mode=d&query=restaurants")
            // Create an intent for navigation gmm Google Map Mobile
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            // Set package for Google Maps //view the specified gmmIntentUri
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }
    }

    fun setCharger(charger: Charger) {
        this.charger = charger
    }
}