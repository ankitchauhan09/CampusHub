package com.example.campushub.adapters

import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.graphics.Bitmap
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campushub.R
import com.example.campushub.repositories.FirebaseClient


data class Messages(
    var messageId: String? = "",
    var messageContent: String? = "",
    var roomId: String? = "",
    var senderId: String? = "",
    var imageUrl: String = "",
    var senderName: String? = ""
)

class ChatMessagesAdapter(
    val context: Context,
    val uid: String,
    val firebaseClient: FirebaseClient // Inject FirebaseClient here
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2

    var messageList = mutableListOf<Messages>()
//    @Inject lateinit var firebaseClient: FirebaseClient

    inner class LeftMessageHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        fun bind(messages: Messages, context: Context, firebaseClient: FirebaseClient) {
            val messageContent = messages.messageContent
            val textField = itemView.findViewById<TextView>(R.id.sentMessageContent)
            val senderName = itemView.findViewById<TextView>(R.id.senderName)
            val imageView = itemView.findViewById<ImageView>(R.id.sendMessageImage)

            if(messages.imageUrl.equals("")) {
                imageView.visibility = GONE
            } else {
                imageView.visibility = VISIBLE
                Glide.with(context).load(messages.imageUrl).into(imageView)
            }

            senderName.text = messages.senderName
            textField.text = messageContent
            itemView.setOnLongClickListener {
                showPopMenu(itemView, messages, context, firebaseClient)
                true
            }

        }

        private fun showPopMenu(
            view: View?,
            messages: Messages,
            context: Context,
            firebaseClient: FirebaseClient
        ) {
            val popupMenu = PopupMenu(context, view)
            popupMenu.menuInflater.inflate(R.menu.messages_popup_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.deleteMessage -> {
                        val position = adapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            val messageid = messages.messageId
                            firebaseClient.deleteMessge(messageid) { errorMessage, status ->
                                if (!status) {
                                    Toast.makeText(
                                        context,
                                        "Error while deleting the message : $errorMessage",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    messageList.removeAt(position)
                                    notifyItemRemoved(position)
                                }
                            }
                        }
                    }

                    R.id.copyMessage -> {
                        val clipboard =
                            context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = android.content.ClipData.newPlainText(
                            "Copied text",
                            messages.messageContent
                        )
                        clipboard.setPrimaryClip(clip)
                    }
                }
                return@setOnMenuItemClickListener true
            }
            popupMenu.show()
        }
    }

    inner class RightMessageHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        fun bind(messages: Messages, context: Context, firebaseClient: FirebaseClient) {
            val messageContent = messages.messageContent
            val textField = itemView.findViewById<TextView>(R.id.receivingMessageContent)
            val senderName = itemView.findViewById<TextView>(R.id.receivingSenderName)
            val imageView = itemView.findViewById<ImageView>(R.id.messageReceivedImage)

            if(messages.imageUrl.equals("")) {
                imageView.visibility = GONE
            } else {
                imageView.visibility = VISIBLE
                Glide.with(context).load(messages.imageUrl).into(imageView)
            }

            senderName.text = messages.senderName
            textField.text = messageContent

            itemView.setOnLongClickListener {
                showPopMenu(itemView, messages, context, firebaseClient)
                true
            }

        }

        private fun showPopMenu(
            view: View?,
            messages: Messages,
            context: Context,
            firebaseClient: FirebaseClient
        ) {
            val popupMenu = PopupMenu(context, view)
            popupMenu.menuInflater.inflate(R.menu.messages_popup_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.deleteMessage -> {
                        val position = adapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            val messageid = messages.messageId
                            firebaseClient.deleteMessge(messageid) { errorMessage, status ->
                                if (!status) {
                                    Toast.makeText(
                                        context,
                                        "Error while deleting the message : $errorMessage",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    messageList.removeAt(position)
                                    notifyItemRemoved(position)
                                }
                            }
                        }
                    }

                    R.id.copyMessage -> {}
                }
                return@setOnMenuItemClickListener true
            }
            popupMenu.show()
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (messageList[position].senderId == uid) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            LeftMessageHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.message_sent_layout, parent, false)
            )
        } else {
            RightMessageHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_messages_received, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            (holder as LeftMessageHolder).bind(messageList[position], context, firebaseClient)
        } else {
            (holder as RightMessageHolder).bind(messageList[position], context, firebaseClient)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    fun updateMessages(message: Messages) {
        this.messageList.add(message);
        notifyItemInserted(messageList.size - 1);
    }

}