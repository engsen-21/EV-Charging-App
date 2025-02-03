package smartcharge.master

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import smartcharge.master.data.UserData
import smartcharge.master.databinding.ActivityRegisterBinding

class Register : AppCompatActivity() {

    //Variable
    private lateinit var binding: ActivityRegisterBinding;
    //Realtime Database
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    //Firebase Authentication
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        // Connect RealTime Database
        firebaseDatabase = FirebaseDatabase.getInstance("https://smartcharge-project-default-rtdb.asia-southeast1.firebasedatabase.app")
        // Pass pathString
        databaseReference = firebaseDatabase.reference.child("users")

        //Register Button
        binding.regBtn.setOnClickListener{
            val registerName = binding.inputFullName.text.toString()
            val registerUserName = binding.inputUserName.text.toString()
            val registerEmail = binding.inputEmail.text.toString()
            val registerPhoneNo = binding.InputPhoneNo.text.toString()
            val registerPassword = binding.inputPassword.text.toString()

            if (validateInputs(registerName, registerUserName, registerEmail, registerPhoneNo, registerPassword)) {
                registerUser(registerName, registerUserName, registerEmail, registerPhoneNo, registerPassword)
            }
        }

        //Back Button Code
        binding.btnRegisterBack.setOnClickListener {
            // Create an Intent to navigate back to the LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)  // Start the LoginActivity
            finish()  // Finish the current activity
        }

        //Login TextView back to LoginPage
        binding.regToLogin.setOnClickListener {
            // Create an Intent to navigate back to the LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)  // Start the LoginActivity
            finish()  // Finish the current activity
        }

    }

    // Function to validate user input fields
    private fun validateInputs (name: String, username: String, email: String, phoneNo: String, password: String): Boolean {
        // Check if any field is empty
        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || phoneNo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are mandatory", Toast.LENGTH_SHORT).show()
            return false
        }
        // Check if the email address is valid
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return false
        }
        // Check if the phone number is valid (digit only)
        if (!Patterns.PHONE.matcher(phoneNo).matches()) {
            Toast.makeText(this, "Please enter a digit number only", Toast.LENGTH_SHORT).show()
            return false
        }
        // Check if the password is at least 6 characters long
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
            return false // Return false if the password is too short
        }
        return true
    }

    private fun registerUser(name: String, username: String, email: String, phoneNo: String, password: String) {
        // Check for Existing Email in Realtime Database
        databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // If email does not exist, register the user with Firebase Authentication
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this@Register) { task ->
                            if (task.isSuccessful) {
                                // Registration successful, get user and store additional data in the Realtime Database
                                val firebaseUser = auth.currentUser
                                val id = firebaseUser?.uid
                                // Create User data object
                                val userData = UserData(id, name, username, email, phoneNo, password)
                                // Save userData object to the database at id location
                                databaseReference.child(id!!).setValue(userData)
                                    .addOnCompleteListener { databaseTask ->
                                        if (databaseTask.isSuccessful) {
                                            Toast.makeText(this@Register, "Register Successful", Toast.LENGTH_SHORT).show()
                                            startActivity(Intent(this@Register, LoginActivity::class.java))
                                            finish()
                                        } else {
                                            Toast.makeText(this@Register, "Database Error: ${databaseTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(this@Register, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this@Register, "User already exists", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@Register, "Database Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}