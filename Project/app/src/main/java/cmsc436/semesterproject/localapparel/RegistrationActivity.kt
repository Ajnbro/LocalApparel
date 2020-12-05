package cmsc436.semesterproject.localapparel

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

// CITATION: Lab7 modified
class RegistrationActivity : AppCompatActivity() {

    // Registration variables
    private var emailInput: EditText? = null
    private var passwordInput: EditText? = null
    private var registrationButton: Button? = null
    private var progressBar: ProgressBar? = null
    private var validator = Validators()

    // Firebase variables
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration)

        mAuth = FirebaseAuth.getInstance()

        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        registrationButton = findViewById(R.id.registrationButton)
        progressBar = findViewById(R.id.progressBar)

        registrationButton!!.setOnClickListener { registerNewUser() }
    }

    private fun registerNewUser() {
        progressBar!!.visibility = View.VISIBLE

        val email: String = emailInput!!.text.toString()
        val password: String = passwordInput!!.text.toString()

        if (!validator.validEmail(email)) {
            Toast.makeText(applicationContext, "The specified email address is not valid!", Toast.LENGTH_LONG).show()
            progressBar!!.visibility = View.GONE
            return
        }
        if (!validator.validPassword(password)) {
            Toast.makeText(applicationContext, "The specified password is not valid! A password must be between 6 and 32 characters. It must also contain at least one number and one letter!", Toast.LENGTH_LONG).show()
            progressBar!!.visibility = View.GONE
            return
        }

        // Add the new account credentials to Firebase
        mAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(applicationContext, "Registration successful!", Toast.LENGTH_LONG).show()
                        progressBar!!.visibility = View.GONE

                        val intent = Intent(this@RegistrationActivity, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(applicationContext, "Registration failed! Please try again", Toast.LENGTH_LONG).show()
                        progressBar!!.visibility = View.GONE
                    }
                }
    }
}
