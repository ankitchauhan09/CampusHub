package com.example.campushub.ui.activities

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.campushub.R
import com.example.campushub.databinding.ActivityLoginBinding
import com.example.campushub.repositories.FirebaseClient
import com.example.campushub.util.StudentModal
import com.example.campushub.util.TeacherModal
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private val TAG = "LoginActivity"

    @Inject
    lateinit var firebaseClient: FirebaseClient

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences.Editor

    //google sign in variables
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInOptions: GoogleSignInOptions

    //facebook sign in variable
    private lateinit var callbackManager: CallbackManager

    //user variables
    private var role: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("USER_AUTHENTICATION_DETAILS", MODE_PRIVATE).edit()

        binding.navigateToSignup.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
            finish()
        }

        try {
            role = intent.getStringExtra("role")!!
        } catch (e: Exception) {

        }

        init()
    }

    private fun init() {

        binding.apply {
            loginButton.setOnClickListener {
                val progressDialog = ProgressDialog(this@LoginActivity)
                progressDialog.setTitle("Logging in")
                progressDialog.show()

                if (usernameTextField.text.toString().trim().equals("")) {
                    usernameTextField.error = "Cannot be left empty.."
                    progressDialog.dismiss()
                }
                if (passwordTextField.text.toString().trim().equals("")) {
                    passwordTextField.error = "Cannot be left empty"
                    progressDialog.dismiss()
                }
                if (!usernameTextField.text.toString().trim()
                        .equals("") && !passwordTextField.text.toString().trim().equals("")
                ) {
                    var email = binding.usernameTextField.text.toString()
                    var password = binding.passwordTextField.text.toString()
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                if (user!!.isEmailVerified) {
                                    if (user != null) {
                                        if (!role.equals("")) {
                                            progressDialog.dismiss()
                                            if (role.equals("Student")) {
                                                val studentModal = StudentModal(
                                                    user.displayName.toString(),
                                                    "",
                                                    "",
                                                    "",
                                                    "",
                                                    "",
                                                    "",
                                                    email,
                                                    ""
                                                )
                                                firebaseClient.saveStudentUserInfo(studentModal) {
                                                    if (it) {
                                                        saveUserDetails(user, role)
                                                        val intent = Intent(
                                                            this@LoginActivity,
                                                            HomeActivity::class.java
                                                        )
                                                        startActivity(intent)
                                                    } else {
                                                        MotionToast.createToast(
                                                            this@LoginActivity,
                                                            "Some Error has occurred",
                                                            "Some error occurred during login please contact the administrator",
                                                            MotionToastStyle.ERROR,
                                                            MotionToast.GRAVITY_BOTTOM,
                                                            MotionToast.SHORT_DURATION,
                                                            ResourcesCompat.getFont(
                                                                this@LoginActivity,
                                                                R.font.allerta
                                                            )
                                                        )
                                                    }
                                                }
                                            } else if (role.equals("Teacher")) {
                                                val teacherModal = TeacherModal(
                                                    user.displayName.toString(), email, "", "", ""
                                                )
                                                firebaseClient.saveTeacherUserInfo(teacherModal) {
                                                    if (it) {
                                                        saveUserDetails(user, role)
                                                        val intent = Intent(
                                                            this@LoginActivity,
                                                            HomeActivity::class.java
                                                        )
                                                        startActivity(intent)
                                                    } else {
                                                        MotionToast.createToast(
                                                            this@LoginActivity,
                                                            "Some Error has occurred",
                                                            "Some error occurred during login please contact the administrator",
                                                            MotionToastStyle.ERROR,
                                                            MotionToast.GRAVITY_BOTTOM,
                                                            MotionToast.SHORT_DURATION,
                                                            ResourcesCompat.getFont(
                                                                this@LoginActivity,
                                                                R.font.allerta
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            firebaseClient.fetchRoleWithEmail(email) { status, userRole ->
                                                progressDialog.dismiss()
                                                if (status == true) {
                                                    role = userRole
                                                    if (role.equals("Student")) {
                                                        val studentModal = StudentModal(
                                                            user.displayName.toString(),
                                                            "",
                                                            "",
                                                            "",
                                                            "",
                                                            "",
                                                            "",
                                                            email,
                                                            ""
                                                        )
                                                        firebaseClient.saveStudentUserInfo(
                                                            studentModal
                                                        ) {
                                                            if (it) {
                                                                saveUserDetails(user, role)
                                                                val intent = Intent(
                                                                    this@LoginActivity,
                                                                    HomeActivity::class.java
                                                                )
                                                                startActivity(intent)
                                                            } else {
                                                                MotionToast.createToast(
                                                                    this@LoginActivity,
                                                                    "Some Error has occurred",
                                                                    "Some error occurred during login please contact the administrator",
                                                                    MotionToastStyle.ERROR,
                                                                    MotionToast.GRAVITY_BOTTOM,
                                                                    MotionToast.SHORT_DURATION,
                                                                    ResourcesCompat.getFont(
                                                                        this@LoginActivity,
                                                                        R.font.allerta
                                                                    )
                                                                )
                                                            }
                                                        }
                                                    } else if (role.equals("Teacher")) {
                                                        val teacherModal = TeacherModal(
                                                            user.displayName.toString(),
                                                            email,
                                                            "",
                                                            "",
                                                            ""
                                                        )
                                                        firebaseClient.saveTeacherUserInfo(
                                                            teacherModal
                                                        ) {
                                                            if (it) {
                                                                saveUserDetails(user, role)
                                                                val intent = Intent(
                                                                    this@LoginActivity,
                                                                    HomeActivity::class.java
                                                                )
                                                                startActivity(intent)
                                                            } else {
                                                                progressDialog.dismiss()
                                                                MotionToast.createToast(
                                                                    this@LoginActivity,
                                                                    "Some Error has occurred",
                                                                    "Some error occurred during login please contact the administrator",
                                                                    MotionToastStyle.ERROR,
                                                                    MotionToast.GRAVITY_BOTTOM,
                                                                    MotionToast.SHORT_DURATION,
                                                                    ResourcesCompat.getFont(
                                                                        this@LoginActivity,
                                                                        R.font.allerta
                                                                    )
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    progressDialog.dismiss()
                                    MotionToast.createToast(
                                        this@LoginActivity,
                                        "Unverified email",
                                        "Please verify the email through the link sent to your email",
                                        MotionToastStyle.ERROR,
                                        MotionToast.GRAVITY_BOTTOM,
                                        MotionToast.SHORT_DURATION,
                                        ResourcesCompat.getFont(this@LoginActivity, R.font.allerta)
                                    )
                                }
                            } else {
                                progressDialog.dismiss()
                                MotionToast.createToast(
                                    this@LoginActivity,
                                    "INVALID CREDENTIALS",
                                    "Username or Password is Invalid",
                                    MotionToastStyle.ERROR,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.SHORT_DURATION,
                                    ResourcesCompat.getFont(this@LoginActivity, R.font.allerta)
                                )
                            }
                        } .addOnFailureListener {
                            progressDialog.dismiss()
                        }
                }
            }
        }

    }

    fun saveUserDetails(user: FirebaseUser, role: String) {
        val uid = user.uid
        val email = user.email
        val displayedName = user.displayName

        val sharedPreferences = getSharedPreferences("LOGGED_IN_USER_DETAILS", MODE_PRIVATE).edit()
        sharedPreferences.putString("uid", uid)
        sharedPreferences.putString("email", email)
        sharedPreferences.putString("displayedName", displayedName)
        sharedPreferences.putString("role", role)
        sharedPreferences.putBoolean("isLoggedIn", true)
        sharedPreferences.putString("course", "")
        sharedPreferences.commit()
    }
}