    package com.example.campushub.ui.fragments.teachers

    import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.models.SlideModel
import com.example.campushub.R
import com.example.campushub.adapters.NoticeBoardRecyclerViewAdapter
import com.example.campushub.databinding.FragmentTeacherHomeBinding
import com.example.campushub.repositories.FirebaseClient
import com.example.campushub.util.NoticeModal
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date
import javax.inject.Inject

    @AndroidEntryPoint
    class TeacherHomeFragment : Fragment() {

        private lateinit var binding: FragmentTeacherHomeBinding

        @Inject
        lateinit var firebaseClient: FirebaseClient

        //view variables
        private lateinit var noticeRecyclerView: RecyclerView
        private lateinit var noticeRecyclerViewAdapter: NoticeBoardRecyclerViewAdapter
        private lateinit var postDialog : Dialog

        //dialog view variables
        private lateinit var dialogView: View
        private lateinit var dialogViewTitle: EditText
        private lateinit var dialogViewContent: EditText
        private lateinit var dialogViewImageSlider: ImageSlider
        private lateinit var dialogViewUploadImageButton: Button
        private lateinit var dialogViewPostNotice: Button

        //helper variables
        private var imageList: ArrayList<SlideModel> = ArrayList<SlideModel>()
        private var imageUriList: ArrayList<Uri> = ArrayList<Uri>()
        private var noticeList: ArrayList<NoticeModal> = ArrayList<NoticeModal>()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            binding = FragmentTeacherHomeBinding.inflate(inflater)

            initalizeRecyclerViewComponents()

            initializeDialogViewComponents()

            binding.newPostButton.setOnClickListener {
                initializeDialogViewComponents()
                imageUriList.clear()
                imageList.clear()
                postDialog = Dialog(requireContext())
                postDialog.setContentView(dialogView)
                postDialog.show()
            }

            return binding.root
        }

        private fun initializeDialogViewComponents() {
            dialogView = layoutInflater.inflate(R.layout.activity_new_announcement_handler, null)
            dialogViewTitle = dialogView.findViewById<EditText>(R.id.newAnnouncementDialogTitle)
            dialogViewContent = dialogView.findViewById<EditText>(R.id.newAnnouncementDesciption)
            dialogViewImageSlider = dialogView.findViewById<ImageSlider>(R.id.newAnnouncementImageSlider)
            dialogViewUploadImageButton =
                dialogView.findViewById<Button>(R.id.newAnnouncementUploadImages)
            dialogViewPostNotice = dialogView.findViewById<Button>(R.id.newAnnouncementPostButton)

    //        listeners for the dialogView button
            dialogViewUploadImageButton.setOnClickListener {
                val intent = Intent()
                intent.setType("image/*")
                intent.setAction(Intent.ACTION_GET_CONTENT)
                uploadImageIntentLauncher.launch(intent)
            }

            dialogViewPostNotice.setOnClickListener {
                //fetch the data from input fields
                val title = dialogViewTitle.text.toString().trim()
                val content = dialogViewContent.text.toString().trim()

                val noticeModal = NoticeModal("", Date(), title, content, arrayListOf<String>())
                firebaseClient.saveNoticeModal(noticeModal, imageUriList) { status ->
                    if (status) {
                        dialogView
                        firebaseClient.fetchNotices { status, noticeModalArrayList ->
                            if (status) {
                                postDialog.dismiss()
                                noticeModalArrayList?.let {
                                    noticeList.clear()
                                    noticeList.addAll(noticeModalArrayList)
                                    noticeRecyclerViewAdapter.updateNoticeList(noticeList)
                                }
                            }
                        }
                        Toast.makeText(
                            requireContext(),
                            "Post submitted successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(requireContext(), "Error uploading post", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

        //upload image intent launcher
        private val uploadImageIntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    val imageUri = it.data?.data
                    imageUri?.let {
                        imageUriList.add(imageUri)
                        imageList.add(SlideModel(imageUri.toString()))
                        dialogViewImageSlider.setImageList(imageList)
                        dialogViewImageSlider.visibility = View.VISIBLE
                    }
                }
            }

        private fun initalizeRecyclerViewComponents() {
            noticeRecyclerView = binding.noticeBoardRecyclerView
            noticeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            noticeRecyclerViewAdapter = NoticeBoardRecyclerViewAdapter(requireContext())
            noticeRecyclerView.adapter = noticeRecyclerViewAdapter
            firebaseClient.fetchNotices { status, noticeModalArrayList ->
                if (status) {
                    noticeList.clear()
                    Log.d("noti", noticeModalArrayList.toString())
                    noticeList.addAll(noticeModalArrayList!!)
                    noticeRecyclerViewAdapter.updateNoticeList(noticeList)
                    noticeRecyclerViewAdapter.notifyDataSetChanged()
                }
            }
        }

    }