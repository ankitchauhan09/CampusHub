    package com.example.campushub.adapters

    import android.content.Context
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.TextView
    import androidx.recyclerview.widget.RecyclerView
    import com.denzcoskun.imageslider.ImageSlider
    import com.denzcoskun.imageslider.models.SlideModel
    import com.example.campushub.R
    import com.example.campushub.util.NoticeModal

    class NoticeBoardRecyclerViewAdapter(val context: Context) :
        RecyclerView.Adapter<NoticeBoardRecyclerViewAdapter.NoticeBoardRecyclerViewHolder>() {

        var noticeList = arrayListOf<NoticeModal>()

        inner class NoticeBoardRecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            fun bind(notice: NoticeModal) {
                Log.d("Adapter", "holder running")

                val noticeTitle = itemView.findViewById<TextView>(R.id.noticeTitleText)
                val noticeContent = itemView.findViewById<TextView>(R.id.noticeContentText)
                val noticeTiming = itemView.findViewById<TextView>(R.id.noticePublishTime)
                val noticeImageSlider = itemView.findViewById<ImageSlider>(R.id.imageSlider)

                val imageList = arrayListOf<SlideModel>()
                for (image in notice.noticeImages) {
                    imageList.add(SlideModel(image))
                }
                if (imageList.size >= 1) {
                    noticeImageSlider.setImageList(imageList)
                    noticeImageSlider.visibility = View.VISIBLE
                }
                noticeTitle.setText(notice.noticeTitle)
                noticeContent.setText(notice.noticeContent)
                noticeTiming.setText(notice.noticeTime.toString())
            }
        }

        fun updateNoticeList(list: ArrayList<NoticeModal>) {
            Log.d("Adapter", "Old list size: " + noticeList.size)
            noticeList.clear()
            noticeList.addAll(list)
            Log.d("Adapter", "New list size: " + noticeList.size)
            notifyDataSetChanged()
        }

        fun addNotice(notice: NoticeModal) {
            this.noticeList.add(notice)
            notifyItemInserted(noticeList.size - 1)
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): NoticeBoardRecyclerViewHolder {
            return NoticeBoardRecyclerViewHolder(
                LayoutInflater.from(context).inflate(R.layout.notice_layout, parent, false)
            )
        }

        override fun getItemCount(): Int {
            return noticeList.size
        }

        override fun onBindViewHolder(holder: NoticeBoardRecyclerViewHolder, position: Int) {
            holder.bind(noticeList[position])
        }
    }
