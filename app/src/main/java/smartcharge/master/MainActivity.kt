package smartcharge.master

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import smartcharge.master.ui.theme.SmartChargeTheme

class MainActivity : AppCompatActivity() {

    companion object {
        private const val SPLASH_SCREEN_DURATION = 5000 // // Duration for splash screen display 5 seconds
    }

    // Variables
    private lateinit var topAnim: Animation
    private lateinit var bottomAnim: Animation
    private lateinit var image: ImageView
    private lateinit var logo: TextView
    private lateinit var slogan: TextView
    
    // Suppresses Lint warnings related to the use of resource types
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the activity to full screen mode
        window.setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        // Load animations from resources
        topAnim = AnimationUtils.loadAnimation(this,R.animator.top_animation)
        bottomAnim = AnimationUtils.loadAnimation(this,R.animator.bottom_animation)

        //Hooks =  Find views by their ID
        image = findViewById(R.id.imageView2)
        logo = findViewById(R.id.textView)
        slogan = findViewById(R.id.textView2)

        //Apply animations to the views
        image.animation = topAnim
        logo.animation = bottomAnim
        slogan.animation = bottomAnim

        // Delay for SPLASH_SCREEN_DURATION before starting the LoginActivity
        // Sets a delay for the splash screen duration before executing the code inside the postDelayed method.
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, SPLASH_SCREEN_DURATION.toLong())
    }
}