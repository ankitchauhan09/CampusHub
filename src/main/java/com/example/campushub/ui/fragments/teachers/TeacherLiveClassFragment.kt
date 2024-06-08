package com.example.campushub.ui.fragments.teachers

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campushub.R
import com.example.campushub.adapters.TeacherLiveClassRecyclerViewAdapter
import com.example.campushub.adapters.TeacherUpcomingClassAdapter
import com.example.campushub.databinding.FragmentTeacherLiveClassBinding
import com.example.campushub.repositories.FirebaseClient
import com.example.campushub.ui.activities.call.TeacherLiveClassActivity
import com.example.campushub.util.LiveClassModal
import com.smparkworld.parkdatetimepicker.ParkDateTimePicker
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class TeacherLiveClassFragment : Fragment() {

    private lateinit var binding: FragmentTeacherLiveClassBinding

    @Inject
    lateinit var firebaseClient: FirebaseClient

    //view variables
    private lateinit var createLiveCLassButton: Button

    //recycler view variables
    private lateinit var liveClassRecyclerViewAdapter: TeacherLiveClassRecyclerViewAdapter
    private lateinit var liveClassRecyclerView: RecyclerView
    private var liveClassList = ArrayList<LiveClassModal>()

    private lateinit var upComingClassRecyclerView: RecyclerView
    private lateinit var upcomingClassRecyclerViewAdapter: TeacherUpcomingClassAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTeacherLiveClassBinding.inflate(inflater)

        initializeRecyclerView()

        binding.createNewClassButton.setOnClickListener {
            createNewClass()
        }

        return binding.root
    }

    private fun createNewClass() {
        val alertDialog = AlertDialog.Builder(requireContext()).create()
        val createClassDialog =
            LayoutInflater.from(requireContext()).inflate(R.layout.create_class_dialog, null)

        val createClassSubjecText =
            createClassDialog.findViewById<EditText>(R.id.createClassSubjectName)
        val createClassTopicText = createClassDialog.findViewById<EditText>(R.id.createClassTopic)
        val createClassNowButton =
            createClassDialog.findViewById<Button>(R.id.createClassStartClassNowButton)
        val scheduleClassForLater =
            createClassDialog.findViewById<Button>(R.id.createClassScheduleForLaterButton)

        createClassNowButton.setOnClickListener {
            creatClassNow(createClassSubjecText, createClassTopicText)
        }
        scheduleClassForLater.setOnClickListener {
            scheduleClassForlater(createClassSubjecText, createClassTopicText)
        }

        alertDialog.setView(createClassDialog)
        alertDialog.show()
    }

    private fun scheduleClassForlater(subjectView: EditText, topicView: EditText) {
        val subjectText = subjectView.text.toString().trim()
        val topicText = topicView.text.toString().trim()
        val sharedPreferences =
            requireContext().getSharedPreferences("LOGGED_IN_USER_DETAILS", MODE_PRIVATE)
        val name = sharedPreferences.getString("displayedName", "")
        var date = Date()

        val choosenDate = ParkDateTimePicker.builder(this@TeacherLiveClassFragment)
            .setTitle("Choose date and time of the class")
            .setDayOfWeekTexts(arrayOf("Sun", "Mon", "Tue", "Wed", "Thus", "Fri", "Sat"))
            .setAmPmTexts(arrayOf("AM", "PM"))
            .setResetText("Reset")
            .setDoneText("OK")
            .setPrimaryColorInt(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.application_theme_background_blue
                )
            )
            .setMonthTitleFormatter { year, month ->
                "${year}-${String.format("%02d", month)}"
            }
            .setDateResultFormatter { year, month, day ->
                "${year}-${String.format("%02d", month)}-${String.format("%02d", day)}"
            }
            .setTimeResultFormatter { amPm, hour, minute ->
                "${amPm} ${String.format("%02d", hour)}h ${String.format("%02d", minute)}m"
            }
            .setDateTimeListener { dateTime ->
                val calendar: Calendar = Calendar.getInstance()
                calendar.set(Calendar.YEAR, dateTime.year)
                calendar.set(Calendar.MONTH, dateTime.month - 1) // Months are 0-indexed

                calendar.set(Calendar.DAY_OF_MONTH, dateTime.day)
                if (dateTime.amPm == "PM") {
                    calendar.set(Calendar.HOUR_OF_DAY, dateTime.hour + 12)
                } else {
                    calendar.set(Calendar.HOUR_OF_DAY, dateTime.hour)

                }
                calendar.set(Calendar.MINUTE, dateTime.minute)
                date = calendar.getTime()
                val liveClassModal =
                    LiveClassModal(
                        "",
                        name!!,
                        subjectText,
                        topicText,
                        date,
                        false,
                        "",
                        false,
                        ""
                    )
                firebaseClient.saveLiveClassModal(liveClassModal) { status, channelName, token ->
                    if (status) {
                        Toast.makeText(
                            requireContext(),
                            "Class scheduled for ${date.toString()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Error while stating the class",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .show()

    }

    private fun creatClassNow(subjectView: EditText, topicView: EditText) {
        val subjectText = subjectView.text.toString().trim()
        val topicText = topicView.text.toString().trim()
        val sharedPreferences =
            requireContext().getSharedPreferences("LOGGED_IN_USER_DETAILS", MODE_PRIVATE)
        val name = sharedPreferences.getString("displayedName", "")
        val liveClassModal =
            LiveClassModal("", name!!, subjectText, topicText, Date(), true, "", false, "")
        firebaseClient.saveLiveClassModal(liveClassModal) { status, channelName, token ->
            if (status) {
                val intent = Intent(requireContext(), TeacherLiveClassActivity::class.java)
                intent.putExtra("channelName", channelName)
                intent.putExtra("token", token)
                startActivity(intent)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Error while stating the class",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun initializeRecyclerView() {
        liveClassRecyclerView = binding.teacherLiveClassRecyclerView
        liveClassRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        liveClassRecyclerViewAdapter = TeacherLiveClassRecyclerViewAdapter(requireContext())
        liveClassRecyclerView.adapter = liveClassRecyclerViewAdapter

        upComingClassRecyclerView = binding.teacherLiveClassUpcomingClassesRecyclerView
        upComingClassRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        upcomingClassRecyclerViewAdapter = TeacherUpcomingClassAdapter(requireContext(), firebaseClient)
        upComingClassRecyclerView.adapter = upcomingClassRecyclerViewAdapter

        firebaseClient.fetchOnlineClasses {
            it?.let {classModals ->
                val todayDate = Date()
                var liveClassList = classModals.filter { it.hasStarted } as ArrayList<LiveClassModal>
                var upComingClassList =
                    classModals.filter { !it.hasStarted && it.date.after(todayDate) } as ArrayList<LiveClassModal>
                liveClassRecyclerViewAdapter.updateLiveClassList(liveClassList)
                upcomingClassRecyclerViewAdapter.updateUpcomingClassList(upComingClassList)
            }
        }

    }

    companion object {
    }
}