package com.example.campushub.ui.activities.call

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.campushub.R
import com.example.campushub.databinding.ActivityTeacherLiveClassBinding
import com.example.campushub.repositories.FirebaseClient
import com.example.campushub.util.AgoraConstants.Companion.APP_ID
import dagger.hilt.android.AndroidEntryPoint
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.VideoCanvas
import javax.inject.Inject

@AndroidEntryPoint
class TeacherLiveClassActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherLiveClassBinding

    @Inject
    lateinit var firebaseClient: FirebaseClient

    //agora stream variables
    private lateinit var channelName: String
    private lateinit var token: String
    private var agoraEngine: RtcEngine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTeacherLiveClassBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        channelName = intent.getStringExtra("channelName")!!
        token = intent.getStringExtra("token")!!


        initAgoraEngineAndJoinChannel()

        binding.endCall.setOnClickListener { onEndCallClicked() }
        binding.switchAudio.setOnClickListener { onLocalAudioMuteClicked(binding.switchAudio) }
        binding.switchCamera.setOnClickListener { switchCameraClicked() }

    }

    private fun initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine()
        agoraEngine!!.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
        agoraEngine!!.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
        agoraEngine!!.enableVideo()
        setupLocalVideo()
        joinChannel()
    }

    private fun setupLocalVideo() {
        val container = binding.localUser
        val surfaceView = SurfaceView(baseContext)
        surfaceView.setZOrderMediaOverlay(true)
        container.addView(surfaceView)
        agoraEngine!!.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0))
    }

    private fun joinChannel() {
        agoraEngine!!.joinChannel(token, channelName, null, 0)
    }

    private fun setupRemoteVideo(uid: Int) {
//        val container = binding.remoteUser
//
//        if (container.childCount >= 1) {
//            return
//        }
//        val surfaceView = SurfaceView(baseContext)
//        container.addView(surfaceView)
//        agoraEngine!!.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid))
//        surfaceView.tag = uid
    }

    override fun onDestroy() {
        super.onDestroy()
        agoraEngine!!.leaveChannel()
        RtcEngine.destroy()
        agoraEngine = null
    }

    fun onLocalAudioMuteClicked(view: View) {
        val iv = view as ImageView
        if (iv.isSelected) {
            iv.isSelected = false
            iv.clearColorFilter()
        } else {
            iv.isSelected = true
            iv.setColorFilter(
                resources.getColor(androidx.appcompat.R.color.material_blue_grey_800),
                PorterDuff.Mode.MULTIPLY
            )
        }
        agoraEngine!!.muteLocalAudioStream(iv.isSelected)
    }

    fun switchCameraClicked() {
        agoraEngine!!.switchCamera()
    }

    fun onEndCallClicked() {
        firebaseClient.deleteLiveClassModal(channelName) {
            if (it) {
                finish()
            } else {

            }
        }
    }


    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread { setupRemoteVideo(uid) }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            //do nothing eat 5 star
        }

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            runOnUiThread { println("Join Channel Success : $uid") }
        }
    }


    private fun initializeAgoraEngine() {
        try {
            agoraEngine = RtcEngine.create(baseContext, APP_ID, mRtcEventHandler)
        } catch (e: Exception) {
            println("Exception ${e.message.toString()}")
            e.printStackTrace()
        }
    }

}