package smartcharge.master

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import smartcharge.master.data.Charger
import smartcharge.master.data.UserData
import smartcharge.master.databinding.ActivityHomeChargerBinding
import java.io.ByteArrayOutputStream

class HomeChargerActivity : AppCompatActivity() {

    // Binding object for activity layout
    private lateinit var binding: ActivityHomeChargerBinding

    // Firebase instances for authentication, database, and storage
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storage: FirebaseStorage

    // Declare user and file URIs
    private var currentUser: UserData? = null
    private var ownershipProofUri: Uri? = null
    private var installationCertificateUri: Uri? = null
    private var photoUri: Uri? = null

    // Constants for file picker requests
    companion object {
        private const val PICK_OWNERSHIP_PDF_REQUEST = 1
        private const val PICK_INSTALLATION_PDF_REQUEST = 2
        private const val PICK_PHOTO_REQUEST = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize binding with the layout
        binding = ActivityHomeChargerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance("https://smartcharge-project-default-rtdb.asia-southeast1.firebasedatabase.app")
        databaseReference = firebaseDatabase.reference.child("users")
        storage = FirebaseStorage.getInstance("gs://smartcharge-project.appspot.com")

        // Load current user data from Firebase
        val userRef = databaseReference.child(auth.currentUser?.uid!!)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Get user data from Firebase
                currentUser = snapshot.getValue(UserData::class.java)
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        // Set onClickListeners for file upload buttons
        binding.btnUploadOwnershipProof.setOnClickListener { pickFile(PICK_OWNERSHIP_PDF_REQUEST) }
        binding.btnUploadInstallationCertificate.setOnClickListener { pickFile(PICK_INSTALLATION_PDF_REQUEST) }
        binding.btnUploadPhotos.setOnClickListener { pickFile(PICK_PHOTO_REQUEST) }

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Set onClickListener for submit button
        binding.btnSubmitCharger.setOnClickListener { submitCharger() }
    }

    // Function to open file picker
    private fun pickFile(requestCode: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        // Set intent type based on request code
        intent.type = if (requestCode == PICK_PHOTO_REQUEST) "image/*" else "application/pdf"
        startActivityForResult(Intent.createChooser(intent, "Select File"), requestCode)
    }

    // Handle result from file picker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            // Set URI and update button text based on request code
            when (requestCode) {
                PICK_OWNERSHIP_PDF_REQUEST -> {
                    // Set URI and update button text for ownership proof PDF upload
                    ownershipProofUri = data.data
                    binding.btnUploadOwnershipProof.text = "PDF Selected"
                }
                PICK_INSTALLATION_PDF_REQUEST -> {
                    // Set URI and update button text for installation certificate PDF upload
                    installationCertificateUri = data.data
                    binding.btnUploadInstallationCertificate.text = "PDF Selected"
                }
                PICK_PHOTO_REQUEST -> {
                    // Set URI and update button text for photo upload
                    photoUri = data.data
                    binding.btnUploadPhotos.text = "Photo Selected"
                }
            }
        }
    }

    // Function to submit charger data to Firebase
    private fun submitCharger() {
        // Get values from UI elements
        val address = binding.etHomeAddress.text.toString().trim()
        val chargerType = binding.spinnerChargerType.selectedItem.toString()
        val brandModel = binding.spinnerBrandModel.selectedItem.toString()
        val powerRating = binding.spinnerPowerRating.selectedItem.toString()
        val connectorType = binding.spinnerConnectorType.selectedItem.toString()
        val availability = binding.spinnerAvailability.selectedItem.toString()
        val latitude = binding.etLatitude.text.toString().toDoubleOrNull()
        val longitude = binding.etLongitude.text.toString().toDoubleOrNull()
        val pricePerUnit = binding.spinnerPricePerUnit.selectedItem.toString()

        // Check if required fields are filled and user data is available
        if (validateInputs(address, latitude, longitude)) {
            // Check if all required files are selected
            if (ownershipProofUri == null || installationCertificateUri == null || photoUri == null) {
                Toast.makeText(this, "Please upload all required files", Toast.LENGTH_SHORT).show()
                return
            }
            // Generate unique ID for the charger
            val chargerId = firebaseDatabase.reference.child("chargers").push().key
            val charger = Charger(
                chargerId = chargerId,
                userId = currentUser?.id,
                userName = currentUser?.userName,
                phoneNo = currentUser?.phoneNo,
                latitude = latitude,
                longitude = longitude,
                address = address,
                chargerType = chargerType,
                brandModel = brandModel,
                powerRating = powerRating,
                connectorType = connectorType,
                availability = availability,
                pricePerUnit = pricePerUnit,
                status = "pending",
                comment = null,
                adminId = null // Initialize adminId to null
            )

            // Save charger data to Firebase Real Time Database
            chargerId?.let {
                val chargerRef = firebaseDatabase.reference.child("chargers").child(it)
                chargerRef.setValue(charger)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Upload associated files to Firebase Storage and update database with URLs
                            uploadOwnershipProof(it)
                            uploadInstallationCertificate(it)
                            uploadPhoto(it)

                            // Navigate back to ProfileActivity after successful submission
                            val intent = Intent(this, ProfileActivity::class.java)
                            startActivity(intent)
                            finish() // Finish current activity to remove it from the back stack
                            Toast.makeText(this, "Submission Successfully. Please Wait For Admin Approve.", Toast.LENGTH_SHORT).show()
                        } else {
                            // Show error message
                            Toast.makeText(this, "Submission failed. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        } else {
            // Show error message
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
        }
    }

    // Validate inputs
    private fun validateInputs(address: String, latitude: Double?, longitude: Double?): Boolean {
        // Check if address is empty
        if (address.isEmpty()) {
            binding.etHomeAddress.error = "Address is required"
            return false
        }
        // Check if latitude is valid
        if (latitude == null) {
            binding.etLatitude.error = "Latitude is required"
            return false
        }
        // Check if longitude is valid
        if (longitude == null) {
            binding.etLongitude.error = "Longitude is required"
            return false
        }
        return true
    }

    // Function to upload ownership proof file to Firebase Storage
    private fun uploadOwnershipProof(chargerId: String) {
        ownershipProofUri?.let {
            val storageRef = storage.reference.child("ChargerDetails/$chargerId/ownershipProof.pdf")
            storageRef.putFile(it)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        // Update database with the file URL
                        firebaseDatabase.reference.child("chargers").child(chargerId).child("ownershipProofUrl").setValue(uri.toString())
                    }
                }
                .addOnFailureListener {
                    // Handle file upload failure
                    Toast.makeText(this, "Ownership proof upload failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Function to upload installation certificate file to Firebase Storage
    private fun uploadInstallationCertificate(chargerId: String) {
        installationCertificateUri?.let {
            val storageRef = storage.reference.child("ChargerDetails/$chargerId/installationCertificate.pdf")
            storageRef.putFile(it)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        // Update database with the file URL
                        firebaseDatabase.reference.child("chargers").child(chargerId).child("installationCertificateUrl").setValue(uri.toString())
                    }
                }
                .addOnFailureListener {
                    // Handle file upload failure
                    Toast.makeText(this, "Installation certificate upload failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Function to upload photo file to Firebase Storage
    private fun uploadPhoto(chargerId: String) {
        photoUri?.let {
            val storageRef = storage.reference.child("ChargerDetails/$chargerId/photo.jpg")
            storageRef.putFile(it)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        // Update database with the file URL
                        firebaseDatabase.reference.child("chargers").child(chargerId).child("photoUrl").setValue(uri.toString())
                    }
                }
                .addOnFailureListener {
                    // Handle file upload failure
                    Toast.makeText(this, "Photo upload failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
        }
    }

}