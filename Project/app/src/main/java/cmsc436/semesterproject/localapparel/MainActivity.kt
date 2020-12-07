package cmsc436.semesterproject.localapparel

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

// CITATION: Lab7 modified
class MainActivity : AppCompatActivity() {

    private var registerButton: Button? = null
    private var loginButton: Button? = null
    private var logo: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        registerButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.loginButton)
        logo = findViewById(R.id.logo)

        logo!!.setImageResource(R.drawable.logo)

        registerButton!!.setOnClickListener {
            val intent = Intent(this@MainActivity, RegistrationActivity::class.java)
            startActivity(intent)
        }

        loginButton!!.setOnClickListener {
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
        }

    }

    // CITATION: Lab11
    // Ensures that location permissions have been properly granted
    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= 23 &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                "android.permission.ACCESS_FINE_LOCATION"
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                applicationContext,
                "android.permission.ACCESS_COARSE_LOCATION"
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this@MainActivity, arrayOf(
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.ACCESS_COARSE_LOCATION"
                ),
                FeedActivity.MY_PERMISSIONS_LOCATION
            )
        }
    }

    // CITATION: Lab11
    // Ensures that permissions have been properly granted for Location services
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            FeedActivity.MY_PERMISSIONS_LOCATION -> {
                var g = 0
                for (perm in permissions)
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) else {
                    Toast.makeText(
                        applicationContext,
                        "Must grant location permission to use app!",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }
}
