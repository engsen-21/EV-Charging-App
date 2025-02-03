package smartcharge.master

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import smartcharge.master.data.UserData
import smartcharge.master.databinding.ActivityLoginBinding

class LoginActivity: AppCompatActivity(){

    // Binding object for activity layout
    private lateinit var binding:ActivityLoginBinding

    // Firebase instances for database
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var userDatabaseReference: DatabaseReference
    private lateinit var adminDatabaseReference: DatabaseReference

    // Firebase Authentication
    private lateinit var auth: FirebaseAuth

    // Google Sign-In client
    private var oneTapClient: SignInClient? = null

    // Google Sign-In request
    private lateinit var signInRequest: BeginSignInRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize binding with the layout
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase database and references
        firebaseDatabase = FirebaseDatabase.getInstance("https://smartcharge-project-default-rtdb.asia-southeast1.firebasedatabase.app")
        // Refer child name as users and admins
        userDatabaseReference = firebaseDatabase.reference.child("users")
        adminDatabaseReference = firebaseDatabase.reference.child("admins")

        // Initialize Firebase authentication
        auth = Firebase.auth

        //Login Button
        binding.loginBtn.setOnClickListener{
            val loginEmail = binding.InputEmailAddress.text.toString()
            val loginPassword = binding.InputPassword.text.toString()

            // Check if email and password fields are not empty
            if(loginEmail.isNotEmpty() && loginPassword.isNotEmpty()){
                loginUser(loginEmail, loginPassword)
            } else {
                Toast.makeText(this@LoginActivity, "All fields are mandatory", Toast.LENGTH_SHORT).show()
            }
        }

        // Initialize Google Sign-In client and request
        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.default_web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(false)
            .build()

        // Navigate to Register Activity
        binding.textViewSignUp.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
            finish()
        }
        // Navigate to Forgot Password Activity
        binding.forgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Login Function
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Check if the user is an admin
                    checkIfAdmin(email, password)
                } else {
                    Toast.makeText(this@LoginActivity, "Email or Password Incorrect", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Function to check if the user is an admin
    private fun checkIfAdmin(email: String, password: String) {
        adminDatabaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var isAdmin = false
                for (adminSnapshot in dataSnapshot.children) {
                    val adminEmail = adminSnapshot.child("email").getValue(String::class.java)
                    val adminPassword = adminSnapshot.child("password").getValue(String::class.java)
                    if (email == adminEmail && password == adminPassword) {
                        isAdmin = true
                        break
                    }
                }
                if (isAdmin) {
                    Toast.makeText(this@LoginActivity, "Welcome Admin", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, AdminActivity::class.java))
                    finish()
                } else {
                    updatePasswordInDatabase(auth.currentUser, password)
                    startActivity(Intent(this@LoginActivity, MapsActivity::class.java))
                    finish()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@LoginActivity, "Failed to access database", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Function to update password in Realtime Database
    private fun updatePasswordInDatabase(user: FirebaseUser?, newPassword: String) {
        user?.let {
            val userId = it.uid
            userDatabaseReference.child(userId).child("password").setValue(newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login Successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to update password in database: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }


    fun signingGoogle(view:View){ // Function to start the Google Sign-In process on button click
        CoroutineScope(Dispatchers.Main).launch {// Launch a coroutine on the main dispatcher
            signingGoogle() // Call the signingGoogle function
        }
    }

    // Suspend function to handle Google Sign-In process
    private suspend fun signingGoogle(){
        try {
            val result = oneTapClient?.beginSignIn(signInRequest)?.await() // Start the sign-in process and wait for the result
            if (result != null) {
                val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent).build()
                signInResultLauncher.launch(intentSenderRequest)
            } else {
                Toast.makeText(this@LoginActivity, "Result is null in signingGoogle()", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace() // Print the stack trace for debugging
            Toast.makeText(this@LoginActivity, "Error in signingGoogle()", Toast.LENGTH_SHORT).show()
        }
    }

    // Register a launcher for the sign-in result
    private val signInResultLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) { // Check if the result is OK
                // Get the sign-in result
                val credential = oneTapClient!!.getSignInCredentialFromIntent(result.data)
                val idToken = credential.googleIdToken
                if (idToken != null){
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null) // Get the Firebase credential
                    auth.signInWithCredential(firebaseCredential).addOnCompleteListener{
                        if(it.isSuccessful){ // Check if sign-in is successful
                            Toast.makeText(this, "Google Sign-In Complete", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this@LoginActivity, MapsActivity::class.java)) // Navigate to HomeActivity
                            finish() // Finish current activity
                        } else {
                            Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show() // Show a toast message if sign-in fails
                        }
                    }
                }
            } else {
                // Show a toast message if sign-in is canceled
                Toast.makeText(this, "Google Sign-In Canceled", Toast.LENGTH_SHORT).show()
            }
        }

//    override fun onStart() {
//        super.onStart()
//        val currentUser = auth.currentUser
//        if (currentUser !=null)run{
//            // User is already logged in, navigate to MapsActivity
//            startActivity(Intent(this@LoginActivity, MapsActivity::class.java))
//            finish()
//        }
//    }
}