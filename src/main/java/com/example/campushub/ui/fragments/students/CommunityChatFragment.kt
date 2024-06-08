package com.example.campushub.ui.fragments.students

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campushub.R
import com.example.campushub.adapters.ChatRoomListAdapter
import com.example.campushub.adapters.ChatRooms
import com.example.campushub.databinding.FragmentCommunityChatBinding
import com.example.campushub.repositories.FirebaseClient
import com.example.campushub.ui.activities.HomeActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CommunityChatFragment : Fragment(), ChatRoomsCallback {
    @Inject
    lateinit var firebaseClient: FirebaseClient

    private val TAG = "CommunityChatFragment"
    private lateinit var binding: FragmentCommunityChatBinding
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatRoomListAdapter: ChatRoomListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentCommunityChatBinding.inflate(layoutInflater)
        chatRecyclerView = binding.chatRecyclerView
        chatRoomListAdapter = ChatRoomListAdapter(requireContext() , (activity as HomeActivity).supportFragmentManager)
        chatRecyclerView.adapter = chatRoomListAdapter
        chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        updateChatList()

        binding.createRoomButton.setOnClickListener {
            alertDialogActions()
        }

        return binding.root
    }

    private fun alertDialogActions() {
        val alertDialog = AlertDialog.Builder(requireContext()).create()
        val dialogView = layoutInflater.inflate(R.layout.create_room_dialog, null)
        val positiveButton = dialogView.findViewById<Button>(R.id.createRoomConfirmButton)
        positiveButton.setOnClickListener {
            if (validateRoomName(dialogView.findViewById<EditText>(R.id.roomNameTextField).text.toString())) {
                alertDialog.dismiss()
            } else {

            }
        }
        alertDialog.setView(dialogView)
        alertDialog.show()
    }

    private fun updateChatList() {
        val newChatRoomlist = firebaseClient.chatRoomsList(this)
    }

    private fun validateRoomName(roomName: String): Boolean {
        var isRoomAvailable = true;
        firebaseClient.validateRoomName(roomName) { booleanStatus: Boolean, stringStatus: String ->
            if (booleanStatus) {
                isRoomAvailable = true
                Toast.makeText(requireContext(), stringStatus, Toast.LENGTH_SHORT).show()
                updateChatList()
                Log.d(TAG, stringStatus)
            } else {
                isRoomAvailable = false
                Toast.makeText(requireContext(), stringStatus, Toast.LENGTH_SHORT).show()
                Log.d(TAG, stringStatus)
            }
        }
        return isRoomAvailable
    }


    companion object {

    }

    override fun onChatRoomsFetched(chatRoomsList: List<ChatRooms>) {
        chatRoomListAdapter.updateChatRoomsList(chatRoomsList)
    }
}


interface ChatRoomsCallback {
    fun onChatRoomsFetched(chatRoomsList: List<ChatRooms>)
}