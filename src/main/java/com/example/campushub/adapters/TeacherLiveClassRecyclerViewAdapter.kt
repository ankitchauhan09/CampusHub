package com.example.campushub.adapters

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.campushub.R
import com.example.campushub.ui.activities.call.TeacherLiveClassActivity
import com.example.campushub.util.LiveClassModal

class TeacherLiveClassRecyclerViewAdapter constructor(val context: Context) :
    RecyclerView.Adapter<TeacherLiveClassRecyclerViewAdapter.TeacherLiveClassRecyclerViewHolder>() {

    //list variables
    private var liveClassList = arrayListOf<LiveClassModal>()

    inner class TeacherLiveClassRecyclerViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bind(liveClassModal: LiveClassModal) {
            val subject = itemView.findViewById<TextView>(R.id.liveClassCardSubjectName)
            val teacherName = itemView.findViewById<TextView>(R.id.liveClassCardTeacherName)
            val topic = itemView.findViewById<TextView>(R.id.liveClassCardTopicName)
            val dateAndTime = itemView.findViewById<TextView>(R.id.liveClassCardDateAndTime)

            subject.setText(liveClassModal.subject)
            teacherName.setText(liveClassModal.teacherName)
            topic.setText(liveClassModal.topic)
            dateAndTime.setText(liveClassModal.date.toString())

            val classId = liveClassModal.classId

            itemView.setOnClickListener {
                val alertDialog = AlertDialog.Builder(context)
                alertDialog.setTitle("Start class ? ")
                alertDialog.setPositiveButton("Yes", object : DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        val intent = Intent(context, TeacherLiveClassActivity::class.java)
                        intent.putExtra("channelName", classId)
                        intent.putExtra("token", liveClassModal.token)
                        context.startActivity(intent)
                    }
                })
                alertDialog.show()
            }
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TeacherLiveClassRecyclerViewHolder {
        return TeacherLiveClassRecyclerViewHolder(
            LayoutInflater.from(context).inflate(R.layout.live_class_card_layout, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return liveClassList.size
    }

    override fun onBindViewHolder(holder: TeacherLiveClassRecyclerViewHolder, position: Int) {
        holder.bind(liveClassList[position])
    }

    fun updateLiveClassList(list: ArrayList<LiveClassModal>) {
        liveClassList.clear()
        liveClassList.addAll(list)
        notifyDataSetChanged()
    }

}