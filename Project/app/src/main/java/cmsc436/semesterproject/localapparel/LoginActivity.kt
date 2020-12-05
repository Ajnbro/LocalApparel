package cmsc436.semesterproject.localapparel

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

// CITATION: Lab7 modified
class LoginActivity : AppCompatActivity() {

    private var userEmail: EditText? = null
    private var userPassword: EditText? = null
    private var loginButton: Button? = null
    private var forgotPasswordButton: Button? = null
    private var progressBar: ProgressBar? = null

    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        // Firebase variables
        mAuth = FirebaseAuth.getInstance()

        // Login variables
        userEmail = findViewById(R.id.emailInput)
        userPassword = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton)
        progressBar = findViewById(R.id.progressBar)

        loginButton!!.setOnClickListener { loginUserAccount() }

        forgotPasswordButton!!.setOnClickListener { forgotPassword() }
    }

    private fun loginUserAccount() {
        progressBar?.visibility = View.VISIBLE

        val email: String = userEmail?.text.toString()
        val password: String = userPassword?.text.toString()

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(applicationContext, "Please enter an email address!", Toast.LENGTH_LONG).show()
            return
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(applicationContext, "Please enter your password!", Toast.LENGTH_LONG).show()
            return
        }

        mAuth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                progressBar?.visibility = View.GONE
                if (task.isSuccessful) {
                    Toast.makeText(applicationContext, "Login successful!", Toast.LENGTH_LONG)
                        .show()
                    val successfulLoginIntent = Intent(this@LoginActivity, FeedActivity::class.java)
                    startActivity(successfulLoginIntent)
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Login failed!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun forgotPassword() {
        val email: String = userEmail?.text.toString()
        userPassword!!.setText("")

        // Docs citation: https://firebase.google.com/docs/auth/android/manage-users#kotlin+ktx_8
        mAuth!!.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "A password reset email has been sent to $email!",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "An account with the email $email does not exist!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}