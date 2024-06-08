package com.example.campushub.repositories

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.example.campushub.adapters.ChatRooms
import com.example.campushub.adapters.Messages
import com.example.campushub.agoraMedia.RtcTokenBuilder2
import com.example.campushub.ui.fragments.students.ChatRoomsCallback
import com.example.campushub.util.AgoraConstants.Companion.APP_CERTIFICATE
import com.example.campushub.util.AgoraConstants.Companion.APP_ID
import com.example.campushub.util.LiveChatModal
import com.example.campushub.util.LiveClassModal
import com.example.campushub.util.NoticeModal
import com.example.campushub.util.StudentModal
import com.example.campushub.util.TeacherModal
import com.example.campushub.util.UserModal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FirebaseClient @Inject constructor(
    val auth: FirebaseAuth,
    val dbReference: DatabaseReference,
    val storageReference: StorageReference
) {
    private val TAG = "FirebaseClient"

    fun validateRoomName(roomName: String, status: (Boolean, String) -> Unit) {

        if (roomName.trim().equals("")) {
            status(false, "INVALID CHAT ROOM NAME")
        } else {
            val roomReference = dbReference.child("room_chats").child("rooms")
            roomReference.orderByChild("roomName").equalTo(roomName)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            status(false, "Room with name $roomName already exists")
                        } else {
                            val newRoomId = roomReference.push().key
                            val roomToPushInDb = ChatRooms(newRoomId, roomName)
                            roomReference.child(newRoomId!!).setValue(roomToPushInDb)
                            status(true, "Room Created Successfully")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
        }
    }

    private fun fetchAllMembers(roomId: String): MutableList<String> {
        var membersList = mutableListOf<String>()

        dbReference.child("room_chat").child("rooms")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (membersSnapshot in snapshot.children) {
                        val member = membersSnapshot.value.toString()
                        membersList.add(member)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        return membersList;
    }

    fun chatRoomsList(callback: ChatRoomsCallback) {
        val chatRoomsList = mutableListOf<ChatRooms>()
        val roomReference = dbReference.child("room_chats").child("rooms")
        roomReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (chatRoomsSnapshot in snapshot.children) {
                    val chatRoom = chatRoomsSnapshot.getValue(ChatRooms::class.java)
                    chatRoom?.let { chatRoomsList.add(it) }
                }
                callback.onChatRoomsFetched(chatRoomsList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("fetchingRoomList", error.message.toString())
            }
        })
    }

    fun uploadImagesToStorage(imageUri: Uri, success: (Boolean, String) -> Unit) {
        val imageReference = storageReference.child("chat_images/ ${UUID.randomUUID().toString()}")
        imageReference.putFile(imageUri)
            .addOnCompleteListener {
                imageReference.downloadUrl.addOnSuccessListener {
                    var downloadUrl = it.toString()
                    Log.d(TAG, downloadUrl)
                    if (downloadUrl != null) {
                        Log.d(TAG, downloadUrl)
                        success(true, downloadUrl)
                    }
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "Error uploading image")
                success(false, "")
            }
    }


    fun sendMessage(message: Messages, success: (Boolean) -> Unit) {
        val messageReference = dbReference.child("room_chats").child("messages")
        val messageId = messageReference.push().key
        message.messageId = messageId

        if (messageId != null && !message.messageId.equals("")) {
            messageReference.child(messageId).setValue(message)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        success(true)
                    } else {
                        success(false)
                    }
                }
        }
    }

    fun fetchMessages(roomId: String, callback: (List<Messages>) -> Unit) {
        val messageReference =
            dbReference.child("room_chats").child("messages").orderByChild("roomId").equalTo(roomId)
        messageReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var messages = mutableListOf<Messages>()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(Messages::class.java)
                    message?.let { messages.add(it) }
                }
                callback(messages)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    fun deleteMessge(messageid: String?, success: (String, Boolean) -> Unit) {
        val messageReference = dbReference.child("room_chats").child("messages").child(messageid!!)
        messageReference.removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    success("", true)
                } else {
                    success(it.exception?.message.toString(), false)
                }
            }

    }


    fun sendLiveChatMessage(message: LiveChatModal, success: (Boolean) -> Unit) {
        val liveChatMessageReference = dbReference.child("live_chats").child("messages")
        val messageId = liveChatMessageReference.push().key
        message.messageId = messageId!!
        if (messageId != null || message != null) {
            liveChatMessageReference.child(messageId).setValue(message)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        success(true)
                    } else {
                        success(false)
                    }
                }
                .addOnFailureListener {
                    success(false)
                }
        }
    }

    fun fetchOnlineClasses(callback: (ArrayList<LiveClassModal>) -> Unit) {
        val lifeClassesReference = dbReference.child("classes").child("live_classes")
        lifeClassesReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var classList = arrayListOf<LiveClassModal>()
                for (classSnapshot in snapshot.children) {
                    val classModal = classSnapshot.getValue(LiveClassModal::class.java)
                    if (classModal != null) {
                        classList.add(classModal)
                    }
                }
                callback(classList)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    fun downloadFileFromStorage(imageUrl: String): Bitmap? {
        var bitmap: Bitmap? = null
        val imageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
        imageReference.getBytes(1024 * 1024)
            .addOnSuccessListener {
                bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            }
            .addOnFailureListener {
                Log.e(TAG, "Error downloading image from Storage", it)
            }
        return bitmap
    }

    fun updateStudentDetails(studentModal: StudentModal, success: (Boolean) -> Unit) {
        val studentReference = dbReference.child("student").child(studentModal.studentId)
        studentReference.setValue(studentModal).addOnCompleteListener {
            if (it.isSuccessful) {
                success(true)
            } else {
                success(false)
            }
        }.addOnFailureListener {
            success(false)
        }
    }

    fun fetchStudent(studentId: String, response: (Boolean, StudentModal?) -> Unit) {
        val studentReference = dbReference.child("student").child(studentId)
        studentReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var studentDetails = snapshot.getValue(StudentModal::class.java)
                    if (studentDetails != null) {
                        response(true, studentDetails)
                    } else {
                        response(false, null)
                    }
                } else {
                    response(false, null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    fun saveStudentUserInfo(
        studentModal: StudentModal,
        callback: (Boolean) -> Unit
    ) {

        val userInfoReference =
            dbReference.child("users_info").orderByChild("email").equalTo(studentModal.email)
        userInfoReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    if (snapshot.children.firstOrNull()?.child("role")?.value == "Student") {
                        callback(true)
                        return
                    }
                } else {
                    saveStudentDetails(studentModal) {
                        if (it) {
                            callback(true)
                        } else {
                            callback(false)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun saveStudentDetails(
        studentModal: StudentModal,
        callback: (Boolean) -> Unit
    ) {

        val studentReference = dbReference.child("student")
        val studentId = studentReference.push().key
        if (studentId != null) {
            studentModal.studentId = studentId
            studentReference.child(studentId).setValue(studentModal)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val userModal = UserModal(
                            studentId, "Student", studentModal.name, studentModal.email
                        )
                        val userRef =
                            dbReference.child("users_info").child(studentId).setValue(userModal)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        callback(true)
                                    } else {
                                        callback(false)
                                    }
                                }.addOnFailureListener { callback(false) }
                    } else {
                        callback(false)
                    }
                }.addOnFailureListener { callback(false) }
        }
    }


    fun saveTeacherUserInfo(teacherModal: TeacherModal, callback: (Boolean) -> Unit) {

        val userInfoReference =
            dbReference.child("users_info").orderByChild("email").equalTo(teacherModal.email)
        userInfoReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    if (snapshot.children.firstOrNull()?.child("role")?.value == "Teacher") {
                        callback(true)
                        return
                    }
                } else {
                    saveTeacherDetails(teacherModal) {
                        if (it) {
                            callback(true)
                        } else {
                            callback(false)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun saveTeacherDetails(teacherModal: TeacherModal, callback: (Boolean) -> Unit) {
        val teacherReference = dbReference.child("Teacher")
        val teacherId = teacherReference.push().key
        if (teacherId != null) {
            teacherModal.teacherId = teacherId
            teacherReference.child(teacherId).setValue(teacherModal)
                .addOnCompleteListener {
                    val userModal = UserModal(
                        teacherId, "Teacher", teacherModal.name!!, teacherModal.email!!
                    )
                    val userRef =
                        dbReference.child("users_info").child(teacherId).setValue(userModal)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    callback(true)
                                } else {
                                    callback(false)
                                }
                            }.addOnFailureListener { callback(false) }
                }.addOnFailureListener {
                    callback(false)
                }
        }
    }

    fun fetchRoleWithEmail(email: String, callback: (Boolean, String) -> Unit) {
        val userInfoReference = dbReference.child("users_info").orderByChild("email").equalTo(email)
        userInfoReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (users in snapshot.children) {
                        val userInfo = users.getValue(UserModal::class.java)
                        if (userInfo != null) {
                            callback(true, userInfo.role)
                        } else {
                            callback(false, "")
                        }
                    }
                } else {
                    callback(false, "")
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }


    fun fetchNotices(callback: (Boolean, ArrayList<NoticeModal>?) -> Unit) {
        val noticeReference = dbReference.child("notice")
        noticeReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val noticeList = arrayListOf<NoticeModal>()
                for (notices in snapshot.children) {
                    val notice = notices.getValue(NoticeModal::class.java)
                    Log.d("noticesa", notice.toString())
                    if (notice != null) {
                        noticeList.add(notice)
                    }
                }
                callback(true, noticeList)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun uploadMultipleImagesFromUri(
        imageUriList: ArrayList<Uri>,
        callback: (Boolean, ArrayList<String>) -> Unit
    ) {
        val downloadUriList = ArrayList<String>()
        val noticeImageStorageReference = this.storageReference.child("notice_images")
        var uploadCount = 0

        for (image in imageUriList) {
            val imageRef = noticeImageStorageReference.child("${UUID.randomUUID().toString()}")
            imageRef.putFile(image)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        downloadUriList.add(downloadUrl.toString())
                        uploadCount++
                        if (uploadCount == imageUriList.size) {
                            callback(true, downloadUriList)
                        }
                    }
                }
                .addOnFailureListener {
                    callback(false, arrayListOf<String>())
                }
        }
    }


    fun saveNoticeModal(
        noticeModal: NoticeModal,
        imageUriList: ArrayList<Uri>,
        callback: (Boolean) -> Unit
    ) {
        val noticeReference = dbReference.child("notice")
        val noticeId = noticeReference.push().key
        noticeId?.let {
            noticeModal.noticeId = noticeId
            uploadMultipleImagesFromUri(imageUriList) { status, downloadUriList ->
                if (status) {
                    downloadUriList?.let {
                        noticeModal.noticeImages.clear()
                        noticeModal.noticeImages.addAll(downloadUriList)
                        noticeReference.child(noticeId).setValue(noticeModal)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    callback(true)
                                } else {
                                    callback(false)
                                }
                            }.addOnFailureListener {
                                callback(false)
                            }
                    }
                } else {
                    callback(false)
                }
            }
        }
    }

    fun saveLiveClassModal(
        liveClassModal: LiveClassModal,
        callback: (Boolean, String, String) -> Unit
    ) {
        val liveClassReference = dbReference.child("classes").child("live_classes")
        val classId = liveClassReference.push().key

        val token = generateToken(classId!!)
        if (token != null) {
            liveClassModal.classId = classId
            liveClassModal.token = token
            liveClassReference.child(classId)
                .setValue(liveClassModal)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        callback(true, classId, token)
                    } else {
                        callback(false, "", "")
                    }
                }.addOnFailureListener {
                    callback(false, "", "")
                }
        }
    }

    private fun generateToken(channelName: String): String {
        val timestamp = (System.currentTimeMillis() / 1000 + 60).toInt()
        return RtcTokenBuilder2().buildTokenWithUid(
            APP_ID, APP_CERTIFICATE, channelName, 0, RtcTokenBuilder2.Role.ROLE_PUBLISHER,
            timestamp, timestamp
        )
    }

    fun deleteLiveClassModal(classId: String, callback: (Boolean) -> Unit) {
        val classReference = dbReference.child("classes").child("live_classes").child(classId)
            .removeValue().addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true)
                } else {
                    callback(false)
                }
            }
    }

    fun updateClassStatus(classId : String, callback: (Boolean) -> Unit) {
        val classReference = dbReference.child("classes").child("live_classes").child(classId)
        val updateStatusMap = HashMap<String, Boolean>()
        updateStatusMap.put("hasStarted", true)
        classReference.updateChildren(updateStatusMap as Map<String, Boolean>).addOnCompleteListener {
            if(it.isSuccessful) {
                callback(true)
            } else {
                callback(false)
            }
        }.addOnFailureListener {
            callback(false)
        }
    }

}