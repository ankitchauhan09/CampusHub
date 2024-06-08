package com.example.campushub.ui.fragments.students

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campushub.adapters.NoticeBoardRecyclerViewAdapter
import com.example.campushub.databinding.FragmentHomeBinding
import com.example.campushub.repositories.FirebaseClient
import com.example.campushub.util.NoticeModal
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var noticeBoardRecyclerView: RecyclerView
    private lateinit var noticeBoardRecyclerViewAdapter: NoticeBoardRecyclerViewAdapter

    @Inject
    lateinit var firebaseClient: FirebaseClient

    private var noticeList: ArrayList<NoticeModal> = arrayListOf<NoticeModal>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater)

        updateList()

        noticeBoardRecyclerView = binding.noticeBoardRecyclerView
        noticeBoardRecyclerViewAdapter = NoticeBoardRecyclerViewAdapter(requireContext())
        noticeBoardRecyclerView.adapter = noticeBoardRecyclerViewAdapter
        noticeBoardRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)

        return binding.root
    }

    private fun updateList() {
        firebaseClient.fetchNotices() { it, notices ->
            if (it) {
                notices?.let {
                    noticeList.clear()
                    noticeList.addAll(notices!!)
                    noticeBoardRecyclerViewAdapter.updateNoticeList(noticeList)
                }
            }
        }
    }

    companion object {
    }
}