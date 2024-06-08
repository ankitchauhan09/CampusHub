package com.example.campushub.ui.fragments.students

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campushub.adapters.LiveUpcomingClassAdapter
import com.example.campushub.adapters.LiveClassRecyclerViewAdapter
import com.example.campushub.databinding.FragmentLiveClassBinding
import com.example.campushub.repositories.FirebaseClient
import com.example.campushub.util.LiveClassModal
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date
import javax.inject.Inject



@AndroidEntryPoint
class LiveClassFragment : Fragment() {

    private lateinit var binding: FragmentLiveClassBinding

    private lateinit var liveUpcomingClassRecyclerView: RecyclerView
    private lateinit var liveUpcomingClassAdapter: LiveUpcomingClassAdapter

    private lateinit var liveClassRecyclerView: RecyclerView
    private lateinit var liveClassRecyclerViewAdapter: LiveClassRecyclerViewAdapter
    private var classList: MutableList<LiveClassModal> = mutableListOf<LiveClassModal>()
    private val todayDate = Date()

    @Inject
    lateinit var firebaseClient: FirebaseClient

    @Inject
    lateinit var storage: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLiveClassBinding.inflate(inflater)

        liveUpcomingClassRecyclerView = binding.liveClassUpcomingClassesRecyclerView
        liveUpcomingClassAdapter = LiveUpcomingClassAdapter(requireContext())
        liveUpcomingClassRecyclerView.adapter = liveUpcomingClassAdapter
        liveUpcomingClassRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        liveClassRecyclerView = binding.liveClassRecyclerView
        liveClassRecyclerViewAdapter = LiveClassRecyclerViewAdapter(requireContext())
        liveClassRecyclerView.adapter = liveClassRecyclerViewAdapter
        liveClassRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        fetchClasses()

        return binding.root
    }

    private fun fetchClasses() {
        firebaseClient.fetchOnlineClasses { classModals ->
            var liveClassList = classModals.filter { it.hasStarted }
            var upComingClassList =
                classModals.filter { !it.hasStarted && it.date.after(todayDate) }
            liveClassRecyclerViewAdapter.setLiveClassesList(liveClassList)
            liveUpcomingClassAdapter.setUpComingClassList(upComingClassList)
        }
    }
}

