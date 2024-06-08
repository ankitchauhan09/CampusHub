package com.example.campushub.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.campushub.R
import com.example.campushub.util.LiveChatModal

class LiveClassChatRecyclerViewAdapter(context : Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
     var chatList = mutableListOf<LiveChatModal>()

    inner class LiveClassChatRecyclerViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bind() {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return LiveClassChatRecyclerViewHolder(
            LayoutInflater.from(parent.context)
            .inflate(R.layout.live_class_chat_layout, parent, false))
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    }

    fun updateMessages(message : LiveChatModal) {
        if(message != null) {
            chatList.add(message)
            notifyItemInserted(chatList.size - 1)
        }
    }
}