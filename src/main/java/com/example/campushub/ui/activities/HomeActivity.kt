package com.example.campushub.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.etebarian.meowbottomnavigation.MeowBottomNavigation
import com.example.campushub.R
import com.example.campushub.databinding.ActivityHomeBinding
import com.example.campushub.ui.fragments.students.CommunityChatFragment
import com.example.campushub.ui.fragments.students.HomeFragment
import com.example.campushub.ui.fragments.students.LiveClassFragment
import com.example.campushub.ui.fragments.students.ProfileFragment
import com.example.campushub.ui.fragments.teachers.TeacherHomeFragment
import com.example.campushub.ui.fragments.teachers.TeacherLiveClassFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    lateinit var bottomNavigation: MeowBottomNavigation
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    @Inject
    lateinit var auth: FirebaseAuth

    private var role: String? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bottomNavigation = binding.bottomNavigation
        bottomNavigation.show(1, true)

        bottomNavigation.add(MeowBottomNavigation.Model(1, R.drawable.home_bottom_icon))
        bottomNavigation.add(MeowBottomNavigation.Model(2, R.drawable.community_chat_bottom_icon))
        bottomNavigation.add(MeowBottomNavigation.Model(3, R.drawable.live_class_bottom_icon))
        bottomNavigation.add(MeowBottomNavigation.Model(4, R.drawable.profile_bottom_icon))



        drawerLayout = binding.drawerLayout
        navigationView = binding.navigationView
        toolbar = binding.toolbar

        val sharedPreferences = getSharedPreferences("LOGGED_IN_USER_DETAILS", MODE_PRIVATE)
        val name = sharedPreferences.getString("displayedName", "")
        if (!name.equals("")) {
            val headerLayout = navigationView.getHeaderView(0)
            val studentNameTextField =
                headerLayout.findViewById<TextView>(R.id.studentNameTextField)
            studentNameTextField.text = name
        }
        role = sharedPreferences.getString("role", "")

        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this@HomeActivity,
            drawerLayout,
            toolbar,
            R.string.OpenDrawer,
            R.string.CloseDrawer
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (role.equals("Teacher")) {
            loadFragment(TeacherHomeFragment())
        } else {
            loadFragment(HomeFragment())
        }
        meowNavigation()

        navigationViewListeners()
    }

    private fun navigationViewListeners() {
        this.navigationView.setNavigationItemSelectedListener { navItem ->
            when (navItem.itemId) {

                R.id.home_toolBar_sidebar -> {
                    if (role.equals("Teacher")) {
                        loadFragment(TeacherHomeFragment())
                    } else {
                        loadFragment(HomeFragment())
                    }
                }

                R.id.community_chat_toolBar_sidebar -> {
                    loadFragment(CommunityChatFragment())
                }

                R.id.live_classes_toolBar_sidebar -> {
                    if (role.equals("Teacher")) {
                        loadFragment(TeacherLiveClassFragment())
                    } else {
                        loadFragment(LiveClassFragment())
                    }
                }

                R.id.profile_toolBar_sidebar -> {
                    loadFragment(ProfileFragment(auth))
                }

                R.id.logout_button -> {
                    val sharedPreferences =
                        getSharedPreferences("LOGGED_IN_USER_DETAILS", MODE_PRIVATE)

                    val sharedPreferencesEditMode =
                        getSharedPreferences("LOGGED_IN_USER_DETAILS", MODE_PRIVATE).edit()
                    sharedPreferencesEditMode.remove("uid")
                    sharedPreferencesEditMode.remove("email")
                    sharedPreferencesEditMode.remove("displayedName")
                    sharedPreferencesEditMode.putBoolean("isLoggedIn", false)
                    sharedPreferencesEditMode.commit()
                    auth.signOut()
                    startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                    finish()

                }

            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }


    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    private fun meowNavigation() {
        bottomNavigation.setOnClickMenuListener {
            when (it.id) {
                1 -> {
                    if (role.equals("Teacher")) {
                        loadFragment(TeacherHomeFragment())
                    } else {
                        loadFragment(HomeFragment())
                    }
                }

                2 -> {
                    loadFragment(CommunityChatFragment())
                }

                3 -> {
                    if (role.equals("Teacher")) {
                        loadFragment(TeacherLiveClassFragment())
                    } else {
                        loadFragment(LiveClassFragment())
                    }
                }

                4 -> {
                    loadFragment(ProfileFragment(auth))
                }

                else -> Unit
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        Log.d("REPLACING", "replacing fragment")
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout, fragment)
        transaction.commit()

        when (fragment) {
            is HomeFragment -> {
                bottomNavigation.show(1, true)
            }

            is CommunityChatFragment -> {
                bottomNavigation.show(2, true)
            }

            is LiveClassFragment -> {
                bottomNavigation.show(3, true)
            }

            is ProfileFragment -> {
                bottomNavigation.show(4, true)
            }

            is TeacherHomeFragment -> {
                bottomNavigation.show(1, true)
            }

            is TeacherLiveClassFragment ->
                bottomNavigation.show(3, true)
        }
    }

}