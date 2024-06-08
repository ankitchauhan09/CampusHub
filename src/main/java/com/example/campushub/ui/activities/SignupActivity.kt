package com.example.campushub.ui.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.campushub.R
import com.example.campushub.databinding.ActivitySignupBinding
import com.example.campushub.repositories.FirebaseClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.AndroidEntryPoint
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import xyz.teamgravity.imageradiobutton.GravityImageRadioButton
import javax.inject.Inject


@AndroidEntryPoint
class SignupActivity : AppCompatActivity() {

    @Inject
    lateinit var firebaseClient: FirebaseClient

    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivitySignupBinding
    private lateinit var progressDialog: ProgressDialog

    private var role: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        progressDialog = ProgressDialog(this).apply {
            setTitle("Loading")
            setMessage("Please wait...")
            setCancelable(false)
        }

        binding.roleGroup.setOnCheckedChangeListener { _, radioButton, _, _ ->
            role = (radioButton as GravityImageRadioButton).text()
            Toast.makeText(this@SignupActivity, role, Toast.LENGTH_SHORT).show()
        }

        binding.navigateToLogin.setOnClickListener {
            startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
            finish()
        }

        binding.registerButton.setOnClickListener {
            val name = binding.nameTextField.text.toString().trim()
            val email = binding.emailTextField.text.toString().trim()
            val password = binding.passTextField.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role.isEmpty()) {
                showToast("Fields Empty!!", "Please fill all the details", MotionToastStyle.ERROR)
            } else {
                progressDialog.show()

                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = firebaseAuth.currentUser
                            user?.updateProfile(
                                UserProfileChangeRequest.Builder().setDisplayName(name).build()
                            )?.addOnCompleteListener { profileTask ->
                                if (profileTask.isSuccessful) {
                                    user.sendEmailVerification()
                                        .addOnCompleteListener { emailTask ->
                                            progressDialog.dismiss()
                                            if (emailTask.isSuccessful) {
                                                showToast(
                                                    "Verification Email Sent",
                                                    "Please verify your email address.",
                                                    MotionToastStyle.SUCCESS
                                                )
                                                startActivity(
                                                    Intent(
                                                        this@SignupActivity,
                                                        LoginActivity::class.java
                                                    ).putExtra("role", role)
                                                )
                                                finish()
                                            } else {
                                                showToast(
                                                    "ERROR",
                                                    "Failed to send email verification",
                                                    MotionToastStyle.ERROR
                                                )
                                            }
                                        }
                                } else {
                                    progressDialog.dismiss()
                                    showToast("ERROR", "Unknown error..", MotionToastStyle.ERROR)
                                }
                            }
                        } else {
                            progressDialog.dismiss()
                            val errorMessage = task.exception?.message ?: "Unknown error"
                            if (errorMessage.contains("email address is already in use")) {
                                showToast(
                                    "User already exists..",
                                    "User with the given email already exists. Please login..",
                                    MotionToastStyle.ERROR
                                )
                            } else {
                                showToast("ERROR", errorMessage, MotionToastStyle.ERROR)
                            }
                        }
                    }
            }
        }
    }

    private fun showToast(title: String, message: String, style: MotionToastStyle) {
        MotionToast.createToast(
            this@SignupActivity,
            title,
            message,
            style,
            MotionToast.GRAVITY_BOTTOM,
            MotionToast.SHORT_DURATION,
            ResourcesCompat.getFont(this@SignupActivity, R.font.allerta)
        )
    }

    override fun onResume() {
        super.onResume()
        // Check if user is already authenticated and email is verified
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null && currentUser.isEmailVerified) {
            startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
            finish()
        }
    }
}
