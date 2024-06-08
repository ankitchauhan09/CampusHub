package com.example.campushub.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campushub.R
import com.example.campushub.ui.activities.call.LiveClassCallActivity
import com.example.campushub.util.LiveClassModal

//@AndroidEntryPoint
class LiveClassRecyclerViewAdapter constructor(
    val context: Context
) : RecyclerView.Adapter<LiveClassRecyclerViewAdapter.LiveClassRecyclerViewHolder>() {


    private var liveClassesList = mutableListOf<LiveClassModal>()

    public fun setLiveClassesList(classList: List<LiveClassModal>) {
        liveClassesList.clear()
        liveClassesList.addAll(classList)
        notifyDataSetChanged()
    }


    inner class LiveClassRecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(liveClassModal: LiveClassModal) {
            val subjectName = liveClassModal.subject
            val topic = liveClassModal.topic
            val teacherName: String = liveClassModal.teacherName
            val classTiming = liveClassModal.date
            val token: String = liveClassModal.token

            itemView.findViewById<TextView>(R.id.liveClassCardSubjectName).text = subjectName
            itemView.findViewById<TextView>(R.id.liveClassCardTopicName).text = topic
            itemView.findViewById<TextView>(R.id.liveClassCardTeacherName).text = teacherName
            itemView.findViewById<TextView>(R.id.liveClassCardDateAndTime).text =
                classTiming.toString()

            itemView.setOnClickListener {
                val classId = liveClassModal.classId
                if (!classId.isNullOrEmpty() && !token.isNullOrEmpty()) {
                    val intent = Intent(context, LiveClassCallActivity::class.java)
                    intent.putExtra("token", token)
                    intent.putExtra("channelName", classId)
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveClassRecyclerViewHolder {
        return LiveClassRecyclerViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.live_class_card_layout, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return liveClassesList.size
    }

    override fun onBindViewHolder(holder: LiveClassRecyclerViewHolder, position: Int) {
        holder.bindView(liveClassesList[position])
    }

}