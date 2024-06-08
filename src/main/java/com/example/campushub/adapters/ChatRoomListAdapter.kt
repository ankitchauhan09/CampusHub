package com.example.campushub.adapters


import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campushub.databinding.ChatMessagesListLayoutBinding
import com.example.campushub.repositories.FirebaseClient
import com.example.campushub.ui.activities.ChatMessageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import javax.inject.Inject


data class ChatRooms(
    val roomId: String? = null,
    val roomName: String? = null,
    val members: List<String>? = null
)

class ChatRoomListAdapter(
    private val context: Context,
    private val fragmentManager: FragmentManager
) :
    RecyclerView.Adapter<ChatRoomListAdapter.ChatRoomListHolder>() {

    private lateinit var roomReference: DatabaseReference
    private var chatRoomList: List<ChatRooms> = emptyList()
    @Inject
     lateinit var firebaseClient: FirebaseClient
    @Inject
     lateinit var firebaseAuth: FirebaseAuth


    inner class ChatRoomListHolder(private val binding: ChatMessagesListLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(chatRoom: ChatRooms, fargmentManager: FragmentManager) {
            val roomId = chatRoom.roomId
            val roomName = chatRoom.roomName
            val roomMembers = chatRoom.members
//                val numberOfParticipants = roomMembers!!.size

            binding.roomId.text = roomId.toString().trim()
            binding.roomName.text = roomName.toString().trim()
//                binding.roomNumberOfParticipant.text = numberOfParticipants.toString().trim()

            binding.chatRoomIndividual.setOnClickListener {
                val sharedPreferences = context.getSharedPreferences("LOGGED_IN_USER_DETAILS", MODE_PRIVATE)
                val uid = sharedPreferences.getString("uid","")
                val name = sharedPreferences.getString("displayedName", "")

                confirmJoining {
                    if (it) {
                        
                        context.startActivity(Intent(context, ChatMessageActivity::class.java).apply {
                            putExtra("uid", uid)
                            putExtra("name", name)
                            putExtra("roomName", roomName)
                            putExtra("roomId", roomId)
                        })
                    }
                }
            }
        }

        private fun confirmJoining(response: (Boolean) -> Unit) {
            val alertDialog = AlertDialog.Builder(context)
            alertDialog.setTitle("YOU WANT TO JOIN THIS ROOM ? ")
            alertDialog.setPositiveButton("YES", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    response(true)
                }
            })
            alertDialog.setNegativeButton("CANCEL", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    response(false)
                }
            })
            alertDialog.show()
        }

    }

    fun updateChatRoomsList(newChatRoomList: List<ChatRooms>) {
        chatRoomList = newChatRoomList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomListHolder {
        val binding = ChatMessagesListLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatRoomListHolder(binding)
    }

    override fun getItemCount(): Int {
        return chatRoomList.size
    }

    override fun onBindViewHolder(holder: ChatRoomListHolder, position: Int) {
        holder.bindData(chatRoomList[position], fragmentManager)
    }

}