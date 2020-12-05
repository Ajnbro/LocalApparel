package cmsc436.semesterproject.localapparel

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

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
}
