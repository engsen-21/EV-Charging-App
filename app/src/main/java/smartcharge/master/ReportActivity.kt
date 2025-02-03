package smartcharge.master

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import smartcharge.master.data.Report
import smartcharge.master.databinding.ActivityReportBinding
import java.util.UUID

class ReportActivity : AppCompatActivity() {

    // Lateinit properties for view binding and Firebase references
    private lateinit var binding: ActivityReportBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var storageReference: StorageReference

    // Variable to store selected image URI
    private var selectedImageUri: Uri? = null

    companion object {
        // Variable to store selected image URI
        private const val PICK_PHOTO_REQUEST = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Database references
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://smartcharge-project-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child("reports")
        storageReference = FirebaseStorage.getInstance().reference.child("ReportDetail")

        // Set click listener for the submit button
        binding.submitReportButton.setOnClickListener {
            submitReport()
        }

        // Set click listener for the upload photo button
        binding.btnUploadPhotos.setOnClickListener {
            openImagePicker()
        }

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Function to open the image picker
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "Select Photo"), PICK_PHOTO_REQUEST)
    }

    // Handle the result from the image picker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            when (requestCode) {
                PICK_PHOTO_REQUEST -> {
                    // Store the selected image URI and update button text
                    selectedImageUri = data.data
                    binding.btnUploadPhotos.text = "Photo Selected"
                }
            }
        }
    }

    // Function to submit the report
    private fun submitReport() {
        // Get input values from the user
        val addressName = binding.addressNameEditText.text.toString().trim()
        val comment = binding.commentEditText.text.toString().trim()
        val selectedUsageId = binding.usageRadioGroup.checkedRadioButtonId
        val usage = if (selectedUsageId != -1) findViewById<RadioButton>(selectedUsageId).text.toString() else ""
        val chargerType = binding.spinnerChargerType.selectedItem.toString().trim()

        // Validate inputs
        if (addressName.isEmpty() || comment.isEmpty() || usage.isEmpty() || chargerType.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the user ID from Firebase Auth
        val userId = auth.currentUser?.uid ?: return

        // Create report object
        val report = Report(
            addressName = addressName,
            comment = comment,
            usage = usage,
            chargerType = chargerType,
            photoUrl = "", // Placeholder for photo URL
            userId = userId
        )

        // Save report to Firebase
        val reportId = database.push().key ?: return
        database.child(reportId).setValue(report)
            .addOnSuccessListener {
                // Provide immediate feedback
                Toast.makeText(this, "Report submitted successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                finish()
                // Upload the photo in the background
                uploadPhoto(reportId)
            }
            .addOnFailureListener { e ->
                Log.e("ReportActivity", "Failed to submit report", e)
                Toast.makeText(this, "Failed to submit report", Toast.LENGTH_SHORT).show()
            }
    }

    // Get the user ID from Firebase Auth
    private fun uploadPhoto(reportId: String) {
        selectedImageUri?.let { uri ->
            val storageRef = storageReference.child("$reportId.jpg")
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    // Function to upload the photo to Firebase Storage
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        // Update the report with the photo URL
                        database.child(reportId).child("photoUrl").setValue(downloadUrl.toString())
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ReportActivity", "Failed to upload photo", e)
                    Toast.makeText(this, "Failed to upload photo", Toast.LENGTH_SHORT).show()
                }
        }
    }
}