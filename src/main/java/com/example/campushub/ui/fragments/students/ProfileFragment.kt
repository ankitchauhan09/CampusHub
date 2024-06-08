package com.example.campushub.ui.fragments.students

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.campushub.R
import com.example.campushub.databinding.FragmentProfileBinding
import com.example.campushub.ui.activities.HomeActivity
import com.example.campushub.ui.activities.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject



class ProfileFragment @Inject constructor(
    val auth: FirebaseAuth
) : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater)

        binding.manageAccountButton.setOnClickListener {

            val homeActivity = activity as HomeActivity
            val transaction = homeActivity.supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frameLayout, UpdateProfileFragment())
            transaction.addToBackStack(null)
            transaction.commit()

        }

        binding.profileLogoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }


    companion object {

    }
}