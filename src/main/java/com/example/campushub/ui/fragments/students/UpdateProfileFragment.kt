package com.example.campushub.ui.fragments.students

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.campushub.databinding.FragmentUpdateProfileBinding
import com.example.campushub.repositories.FirebaseClient
import com.example.campushub.util.StudentModal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class UpdateProfileFragment : Fragment() {

    private lateinit var binding: FragmentUpdateProfileBinding

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var firebaseClient: FirebaseClient

    //form helper variables aka adapters
    private val courseSession = arrayOf("Select Session", "2021-24", "2022-25", "2023-26")
    private val courses = arrayOf("Select Course", "BCA", "BBA", "B.COM", "MCA", "MBA", "M.COM")
    private lateinit var dobView: EditText

    private lateinit var studentId: String
    private lateinit var oldCourse: String

    //form variable
    private var dob: String? = null
    private lateinit var coursesSpinner: Spinner
    private lateinit var courseSessionSpinner: Spinner
    private var course: String? = null
    private var session: String? = null
    private var firstName: String? = null
    private var lastName: String? = null
    private var universityName: String? = null
    private var collegeName: String? = null
    private var contact: String? = null
    private var email: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUpdateProfileBinding.inflate(layoutInflater)
        dobView = binding.updateDobTextField

        //fetching the current logged in student email
        val sharePreferences =
            requireContext().getSharedPreferences("LOGGED_IN_USER_DETAILS", MODE_PRIVATE)
        email = sharePreferences.getString("email", "")
        studentId = sharePreferences.getString("uid", "")!!
        oldCourse = sharePreferences.getString("course", "")!!
        var studentName = sharePreferences.getString("displayedName", "")!!
        binding.studentCourseLabelView.setText(oldCourse)
        binding.studentNameLabelView.setText(studentName)


        dobView.setOnClickListener {
            var calendar = Calendar.getInstance()
            var year = calendar.get(Calendar.YEAR)
            var month = calendar.get(Calendar.MONTH)
            var day = calendar.get(Calendar.DAY_OF_MONTH)

            var datePickerDialog =
                DatePickerDialog(requireContext(), object : DatePickerDialog.OnDateSetListener {
                    override fun onDateSet(
                        view: DatePicker?,
                        year: Int,
                        month: Int,
                        dayOfMonth: Int
                    ) {
                        var monthStr = if (month / 10 == 0) "0$month" else month
                        var dayOfMonthStr = if (dayOfMonth / 10 == 0) "0$dayOfMonth" else dayOfMonth
                        var selectedDate = "$year-$monthStr-$dayOfMonthStr"
                        dobView.setText(selectedDate as CharSequence)
                    }

                }, year, month, day)
            datePickerDialog.show()

        }
        coursesSpinner = binding.updateCourseSpinner
        courseSessionSpinner = binding.updateCourseSession

        var coursesArrayAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, courses)
        coursesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
        coursesSpinner.adapter = coursesArrayAdapter
        coursesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                course = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }


        var courseSessionArrayAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, courseSession)
        courseSessionArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
        courseSessionSpinner.adapter = courseSessionArrayAdapter
        courseSessionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                session = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        binding.profileUpdateConfirmButton.setOnClickListener {

            firstName = binding.updateFirstNameTextField.text.toString()
            lastName = binding.updateLastNameTextField.text.toString()
            contact = binding.updateContactTextField.text.toString()
            dob = binding.updateDobTextField.text.toString()
            universityName = binding.updateUniversityNameTextField.text.toString()
            collegeName = binding.updateCollegeNameTextField.text.toString()

            if (coursesSpinner.selectedItemPosition == 0) {
                Toast.makeText(requireContext(), "Select the course", Toast.LENGTH_SHORT).show()
            } else {
                if (courseSessionSpinner.selectedItemPosition == 0) {
                    Toast.makeText(requireContext(), "Select the session", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    if (firstName.equals("") || lastName.equals("") || contact.equals("") || dob.equals(
                            ""
                        ) || universityName.equals("") || collegeName.equals("")
                    ) {
                        Toast.makeText(
                            requireContext(),
                            "Please fill the details",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        updateUserDetails()
                    }
                }
            }
        }

        return binding.root
    }

    private fun updateUserDetails() {
        val studentModal = StudentModal(
            "$firstName $lastName",
            course!!,
            universityName!!,
            collegeName!!,
            session!!,
            contact!!,
            dob!!,
            email!!,
            studentId
        )
        val currentUser = auth.currentUser
        if (currentUser != null) {
            currentUser.updateProfile(
                UserProfileChangeRequest.Builder().setDisplayName("$firstName $lastName").build()
            ).addOnCompleteListener {
                if (it.isSuccessful) {
                    firebaseClient.updateStudentDetails(studentModal) {
                        if (it) {
                            val sharedPreferences = requireContext().getSharedPreferences(
                                "LOGGED_IN_USER_DETAILS",
                                MODE_PRIVATE
                            ).edit()
                            sharedPreferences.putString("email", email)
                            sharedPreferences.putString("displayedName", "$firstName $lastName")
                            sharedPreferences.putString("course", course)
                            sharedPreferences.commit()

                            updateStaticFields()

                            Toast.makeText(
                                requireContext(),
                                "Profile Updated Successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Error updating the profile",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error updating the profile",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun updateStaticFields() {
        var progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Saving user details..")
        progressDialog.show()

        firebaseClient.fetchStudent(studentId){ it, studentModal ->
            progressDialog.dismiss()
            if(it) {
                binding.studentNameLabelView.setText(studentModal!!.name)
                binding.studentCourseLabelView.setText(studentModal!!.course)

            } else {
                Toast.makeText(requireContext(), "Some error has occurred", Toast.LENGTH_SHORT).show()
            }
        }

    }


    companion object {

    }
}