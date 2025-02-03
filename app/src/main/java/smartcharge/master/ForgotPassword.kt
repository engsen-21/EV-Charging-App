package smartcharge.master

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class ForgotPassword : AppCompatActivity() {

    // Declaration of UI elements and FirebaseAuth instance
    private lateinit var btnReset: Button;
    private lateinit var btnBack: ImageView;
    private lateinit var edtEmail: EditText;
    private lateinit var progressBar: ProgressBar;
    private lateinit var firebaseAuth: FirebaseAuth;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Initialization of UI elements
        btnBack = findViewById(R.id.btnForgetPassBack)
        btnReset= findViewById(R.id.btnReset)
        edtEmail= findViewById(R.id.inputEmail)
        progressBar= findViewById(R.id.progressBar)

        // Initialization of FirebaseAuth instance
        firebaseAuth = FirebaseAuth.getInstance();

        // Set up the listener for the reset button
        btnReset.setOnClickListener {
            // Get the email from the EditText and trim is use to remove the space before and after the string entered
            val strEmail = edtEmail.text.toString().trim();
            if(!TextUtils.isEmpty(strEmail)){
                Log.d("ForgotPassword", "Reset button clicked, email: $strEmail")
                // If not empty, call the resetPassword function
                resetPassword(strEmail)
            }else{
                // If empty, show an error on the EditText
                edtEmail.error = "Email field can't be empty"
            }
        }

        //Back Button Code
        btnBack.setOnClickListener {
            // Create an Intent to navigate back to the LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)  // Start the LoginActivity
            finish()  // Finish the current activity
        }

    }

    private fun resetPassword(strEmail: String) {
        progressBar.visibility = View.VISIBLE
        btnReset.visibility = View.GONE

        firebaseAuth.sendPasswordResetEmail(strEmail)
            .addOnCompleteListener { task ->
                progressBar.visibility = View.GONE
                btnReset.visibility = View.VISIBLE

                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Reset Password link has been sent to your registered Email.",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val error = task.exception
                    if (error is FirebaseAuthInvalidUserException) {
                        Toast.makeText(this, "User does not exist.", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorMessage = error?.message ?: "Error sending password reset email."
                        Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
    }