package com.example.campushub.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.campushub.R
import com.example.campushub.util.LiveClassModal

class LiveUpcomingClassAdapter(val context: Context) :
    RecyclerView.Adapter<LiveUpcomingClassAdapter.LiveUpcomingClassHolder>() {

    private var upcomingClassList = mutableListOf<LiveClassModal>()

    fun setUpComingClassList(classList: List<LiveClassModal>) {
        this.upcomingClassList = classList as MutableList<LiveClassModal>
        notifyDataSetChanged()
    }

    inner class LiveUpcomingClassHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(liveClassModal: LiveClassModal) {
            val liveClassAnimation =
                itemView.findViewById<LottieAnimationView>(R.id.liveClassCardLiveAnimation)
            liveClassAnimation.visibility = View.INVISIBLE

            val subjectName = liveClassModal.subject
            val topic = liveClassModal.topic
            val teacherName: String = liveClassModal.teacherName
            val classTiming = liveClassModal.date
            val token: String = liveClassModal.token

            itemView.findViewById<TextView>(R.id.liveClassCardSubjectName).text = subjectName
            itemView.findViewById<TextView>(R.id.liveClassCardTopicName).text = topic
            itemView.findViewById<TextView>(R.id.liveClassCardTeacherName).text = teacherName
            itemView.findViewById<TextView>(R.id.liveClassCardDateAndTime).text = classTiming.toString()

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveUpcomingClassHolder {
        return LiveUpcomingClassHolder(
            LayoutInflater.from(context).inflate(R.layout.live_class_card_layout, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return upcomingClassList.size
    }

    override fun onBindViewHolder(holder: LiveUpcomingClassHolder, position: Int) {
        holder.bind(upcomingClassList[position])
    }
}