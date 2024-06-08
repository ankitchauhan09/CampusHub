package com.example.campushub.adapters

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.campushub.R
import com.example.campushub.repositories.FirebaseClient
import com.example.campushub.ui.activities.call.TeacherLiveClassActivity
import com.example.campushub.util.LiveClassModal

class TeacherUpcomingClassAdapter(val context: Context, val firebaseClient: FirebaseClient) :
    RecyclerView.Adapter<TeacherUpcomingClassAdapter.TeacherUpcomingClassHolder>() {

    private val upcomingClassList = ArrayList<LiveClassModal>()

    fun updateUpcomingClassList(list: ArrayList<LiveClassModal>) {
        upcomingClassList.clear()
        upcomingClassList.addAll(list)
        notifyDataSetChanged()
    }

    inner class TeacherUpcomingClassHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(liveClassModal: LiveClassModal) {
            val subject = itemView.findViewById<TextView>(R.id.liveClassCardSubjectName)
            val teacherName = itemView.findViewById<TextView>(R.id.liveClassCardTeacherName)
            val topic = itemView.findViewById<TextView>(R.id.liveClassCardTopicName)
            val dateAndTime = itemView.findViewById<TextView>(R.id.liveClassCardDateAndTime)
            val liveClassAnimationText =
                itemView.findViewById<LottieAnimationView>(R.id.liveClassCardLiveAnimation)
            liveClassAnimationText.visibility = View.INVISIBLE

            subject.setText(liveClassModal.subject)
            teacherName.setText(liveClassModal.teacherName)
            topic.setText(liveClassModal.topic)
            dateAndTime.setText(liveClassModal.date.toString())

            val classId = liveClassModal.classId
            itemView.setOnClickListener {
                val alertDialog = AlertDialog.Builder(context)
                alertDialog.setTitle("Start class ? ")
                alertDialog.setPositiveButton("Yes", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {

                        firebaseClient.updateClassStatus(classId) {
                            if (it) {
                                val intent = Intent(context, TeacherLiveClassActivity::class.java)
                                intent.putExtra("channelName", classId)
                                intent.putExtra("token", liveClassModal.token)
                                context.startActivity(intent)
                                dialog?.dismiss()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Some error has occurred",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                })
                alertDialog.setNegativeButton("No", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dialog?.dismiss()
                    }
                })

                alertDialog.show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeacherUpcomingClassHolder {
        return TeacherUpcomingClassHolder(
            LayoutInflater.from(context).inflate(R.layout.live_class_card_layout, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return upcomingClassList.size
    }

    override fun onBindViewHolder(holder: TeacherUpcomingClassHolder, position: Int) {
        holder.bind(upcomingClassList[position])
    }

}