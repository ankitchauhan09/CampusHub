package com.example.campushub.util

import java.util.Date

data class StudentModal(
    var name: String = "",
    var course: String = "",
    var universityName: String = "",
    var collegeName: String = "",
    var session: String = "",
    var contact: String = "",
    var dob: String = "",
    var email: String = "",
    var studentId: String = ""
)

data class TeacherModal(
    var name : String? = null,
    var email : String? = null,
    var subject : String? = null,
    var teacherId : String? = null,
    var facultyName : String? = null
)

data class LiveChatModal(
    var message: String = "",
    var classUid: String = "",
    var senderName: String = "",
    var senderId: String = "",
    var messageId: String = ""
)

data class NoticeModal(
    var noticeId : String? = null,
    var noticeTime : Date? = null,
    var noticeTitle : String? = null,
    var noticeContent : String? = null,
    var noticeImages : ArrayList<String> = ArrayList<String>()
)

data class UserModal(
    var uid : String = "",
    var role : String = "",
    var name : String = "",
    var email : String = ""
)

data class LiveClassModal(
    var classId: String,
    var teacherName: String,
    var subject: String,
    var topic: String,
    var date: Date = Date(),
    var hasStarted: Boolean = false,
    var token: String = "",
    var hasEnded: Boolean = false,
    var teacherImageName: String = ""
) {
    constructor() : this("", "", "", "", Date(), false, "", false, "")
}