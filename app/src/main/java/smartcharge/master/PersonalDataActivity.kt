package smartcharge.master

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import smartcharge.master.data.UserData
import smartcharge.master.databinding.ActivityPersonalDataBinding

class PersonalDataActivity : AppCompatActivity() {

    // View binding for the activity layout
    private lateinit var binding: ActivityPersonalDataBinding
    // Firebase database and reference
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    // Firebase authentication
    private lateinit var auth: FirebaseAuth

    // Called when the activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase authentication
        auth = FirebaseAuth.getInstance()
        // Initialize Firebase database and reference
        firebaseDatabase = FirebaseDatabase.getInstance("https://smartcharge-project-default-rtdb.asia-southeast1.firebasedatabase.app")
        databaseReference = firebaseDatabase.reference.child("users")

        // Load the user profile data from the database
        loadUserProfile()

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.applyChangesBtn.setOnClickListener {
            applyChanges()
        }
    }

    // Function to load user profile data from the database
    private fun loadUserProfile() {
        // Get the current user's ID and email
        val userId = auth.currentUser?.uid
        val userEmail = auth.currentUser?.email

        if (userId != null && userEmail != null) {
            // Set the email input field with the user's email
            binding.inputEmail.setText(userEmail)

            // Retrieve the user data from the database
            databaseReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get the user data from the snapshot
                    val user = dataSnapshot.getValue(UserData::class.java)
                    if (user != null) {
                        // Set hints with the current user data or default values
                        binding.inputFullName.hint = user.name ?: "Default Full Name"
                        binding.inputUserName.hint = user.userName ?: "Default Username"
                        binding.InputPhoneNo.hint = user.phoneNo ?: "Default Phone No"
                    } else {
                        // Set hints to prompt the user to fill in the data
                        binding.inputFullName.hint = "Fill in Full Name"
                        binding.inputUserName.hint = "Fill in Username"
                        binding.InputPhoneNo.hint = "Fill in Phone No"
                    }
                }
                // Handle any errors that occur when accessing the database
                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@PersonalDataActivity, "Database Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // Show a message if the user is not signed in
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to apply changes to the user profile
    private fun applyChanges() {
        // Get the current user's ID
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Get the updated values from the input fields
            val updatedName = binding.inputFullName.text.toString()
            val updatedUserName = binding.inputUserName.text.toString()
            val updatedPhoneNo = binding.InputPhoneNo.text.toString()

            // Validate the input fields
            if (!validateInputFields(updatedName, updatedUserName, updatedPhoneNo)) {
                return
            }

            // Create an updated UserData object with the new values
            val updatedUser = UserData(
                id = userId,
                name = updatedName,
                userName = updatedUserName,
                email = auth.currentUser?.email,
                phoneNo = updatedPhoneNo
            )

            // Update the user data in the database
            databaseReference.child(userId).setValue(updatedUser).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Show a message if the profile update is successful
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Show an error message if the profile update fails
                    Toast.makeText(this, "Failed to update profile: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Function to validate the input fields
    private fun validateInputFields(name: String, userName: String, phoneNo: String): Boolean {
        // Check if the name field is empty
        if (name.isEmpty()) {
            binding.inputFullName.error = "Full Name is required"
            binding.inputFullName.requestFocus()
            return false
        }

        // Check if the username field is empty
        if (userName.isEmpty()) {
            binding.inputUserName.error = "Username is required"
            binding.inputUserName.requestFocus()
            return false
        }

        // Check if the phone number field is empty or invalid
        if (phoneNo.isEmpty() || !Patterns.PHONE.matcher(phoneNo).matches()) {
            binding.InputPhoneNo.error = "Valid Phone Number is required"
            binding.InputPhoneNo.requestFocus()
            return false
        }

        return true
    }
}