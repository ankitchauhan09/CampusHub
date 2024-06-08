package com.example.campushub.ui.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campushub.R
import com.example.campushub.adapters.ChatMessagesAdapter
import com.example.campushub.adapters.Messages
import com.example.campushub.databinding.ActivityChatMessageBinding
import com.example.campushub.repositories.FirebaseClient
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import javax.inject.Inject

@AndroidEntryPoint
class ChatMessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatMessageBinding
    private lateinit var progressDialog: ProgressDialog

    //bottom dialog vars
    private val CAMERA_ACTIVITY_CODE = 100
    private lateinit var uploadImgFromCamera: ImageView
    private lateinit var uploadImgFromGallery: ImageView

    @Inject
    lateinit var firebaseClient: FirebaseClient

    //chat variables
    private lateinit var uid: String
    private lateinit var roomId: String
    private lateinit var name: String
    private lateinit var room_name: String
    private lateinit var bottomSheetDialog: BottomSheetDialog

    //adapter variables and recycler view variables
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatMessagesAdapter: ChatMessagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChatMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //initializing the chat variables
        uid = intent.getStringExtra("uid")!!
        roomId = intent.getStringExtra("roomId")!!
        name = intent.getStringExtra("name")!!
        room_name = intent.getStringExtra("roomName")!!

        //initialising adapter and view variables
        chatRecyclerView = binding.chatMessageRecyclerView
        chatMessagesAdapter = ChatMessagesAdapter(this, uid, firebaseClient)
        chatRecyclerView.adapter = chatMessagesAdapter
        chatRecyclerView.layoutManager = LinearLayoutManager(this)

        //loading the messages
        loadMessages()

        //update static ui
        updateStaticUi()

    }

    private fun loadMessages() {
        firebaseClient.fetchMessages(roomId) { messages ->
            val newMessages = messages.filter { message ->
                !chatMessagesAdapter.messageList.any { it.messageId == message.messageId }
            }

            newMessages.forEach { message ->
                chatMessagesAdapter.updateMessages(message)
            }

            if (newMessages.isNotEmpty()) {
                chatRecyclerView.smoothScrollToPosition(chatMessagesAdapter.itemCount - 1)
            }
        }
    }

    private fun updateStaticUi() {

        binding.apply {
            this.roomName.text = room_name

            chatMessageBackButton.setOnClickListener {
                super.onBackPressed()
            }

            chatMessageUploadImageButton.setOnClickListener {
                setUpBottomDialog()

            }


            sendMessageButton.setOnClickListener {
                val msg = messageTextField.text.toString()
                val message = Messages("", msg, roomId, uid, "", name)
                sendMessage(message) {
                    if (!it) {
                        Toast.makeText(
                            this@ChatMessageActivity,
                            "Error sending message",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                messageTextField.setText("")
            }
        }

    }

    private fun setUpBottomDialog() {
        bottomSheetDialog = BottomSheetDialog(this@ChatMessageActivity)
        var sheetView = LayoutInflater.from(this@ChatMessageActivity)
            .inflate(R.layout.fragment_image_upload_bottom_sheet_dialog, null)
        uploadImgFromGallery = sheetView.findViewById<ImageView>(R.id.imageUploadByGallery)
        uploadImgFromCamera = sheetView.findViewById<ImageView>(R.id.imageUploadByCamera)
        uploadImgFromGallery.setOnClickListener {
            uploadImageByGallery()
        }
        uploadImgFromCamera.setOnClickListener {

            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    100
                );
                uploadImageByCamera()
            } else {
                uploadImageByCamera()
            }
        }

        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.show()
    }

    private fun uploadImageByGallery() {
        bottomSheetDialog.dismiss()
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        galleryImageLauncher.launch(intent)
    }

    var galleryImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                progressDialog = ProgressDialog(this@ChatMessageActivity)
                progressDialog.setTitle("Uploading Image")
                progressDialog.show()
                val imageUri = it.data?.data
                if (imageUri != null) {
                    firebaseClient.uploadImagesToStorage(imageUri) { status, downloadUrl ->
                        progressDialog.dismiss()
                        if (status) {
                            val message = Messages("", "", roomId, uid, downloadUrl, name)
                            sendMessage(message) {
                                if (it) {
                                    Toast.makeText(this, "Image sent", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this, "Image sent", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(this, "lola ka error", Toast.LENGTH_SHORT).show()
                        }
                    }

                } else {
                    Toast.makeText(this, "Image uri is null", Toast.LENGTH_SHORT).show()
                }

            }
        }

    private fun uploadImageByCamera() {
        bottomSheetDialog.dismiss()
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraImageLauncher.launch(intent)
    }

    var cameraImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            progressDialog = ProgressDialog(this@ChatMessageActivity)
            progressDialog.setTitle("Uploading Image")
            progressDialog.show()
            val extras = it.data?.extras
            val imageUri: Uri
            val imageBitmap = extras?.get("data") as Bitmap

            val result = WeakReference<Bitmap>(
                Bitmap.createScaledBitmap(imageBitmap, imageBitmap.width, imageBitmap.height, false)
                    .copy(
                        Bitmap.Config.RGB_565, true
                    )
            )
            val newBitmap = result.get()
            imageUri = saveImage(newBitmap, this@ChatMessageActivity)

            if (imageUri != null) {
                firebaseClient.uploadImagesToStorage(imageUri) { status, downloadUrl ->
                    progressDialog.dismiss()
                    if (status) {
                        val message = Messages("", "", roomId, uid, downloadUrl, name)
                        sendMessage(message) {
                            if (it) {
                                Toast.makeText(this, "Image sent", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Image sent", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "lola ka error", Toast.LENGTH_SHORT).show()
                    }
                }

            } else {
                Toast.makeText(this, "Image uri is null", Toast.LENGTH_SHORT).show()
            }

        }

    private fun saveImage(newBitmap: Bitmap?, chatMessageActivity: ChatMessageActivity): Uri {
        val imagesFolder = File(chatMessageActivity.cacheDir, "images")
        var uri: Uri? = null
        try {
            imagesFolder.mkdirs()
            val file = File(imagesFolder, "captured_image.jpg")
            val fileOutputStream = FileOutputStream(file)
            newBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
            uri = FileProvider.getUriForFile(
                chatMessageActivity,
                ".MainActivity" + ".provider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return uri!!
    }

    private fun sendMessage(message: Messages, success: (Boolean) -> Unit) {
        firebaseClient.sendMessage(message, success)
    }
}