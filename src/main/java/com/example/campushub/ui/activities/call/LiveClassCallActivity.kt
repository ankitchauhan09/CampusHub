package com.example.campushub.ui.activities.call


import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.SurfaceView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campushub.R
import com.example.campushub.adapters.LiveClassChatRecyclerViewAdapter
import com.example.campushub.agoraMedia.RtcTokenBuilder2
import com.example.campushub.databinding.ActivityLiveClassCallBinding
import com.example.campushub.repositories.FirebaseClient
import com.example.campushub.util.LiveChatModal
import com.example.campushub.util.LiveClassModal
import dagger.hilt.android.AndroidEntryPoint
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.VideoCanvas
import javax.inject.Inject


@AndroidEntryPoint
class LiveClassCallActivity : AppCompatActivity() {

    @Inject
    lateinit var firebaseClient: FirebaseClient

    private val TAG = "LiveClassCallActivity"

    private lateinit var binding: ActivityLiveClassCallBinding

    //agora variables
    private var agoraEngine: RtcEngine? = null
    private val appId: String = "f4e34bce9c004dba8364bb35e6d62712"
    private var token: String? = null
    private var channelName: String? = null
    private val appCertificate: String = "06e1e79cc2134376967d3426687fdecf"
    private var isJoined = false

    //class variables
    private lateinit var classInfo: LiveClassModal

    //handling live chat
    private lateinit var recyclerView: RecyclerView
    private lateinit var liveClassChatRecyclerViewAdapter: LiveClassChatRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLiveClassCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //setting the token and channelName
        token = intent.getStringExtra("token")
        channelName = intent.getStringExtra("channelName")

        //getting the recycler view
        recyclerView = binding.liveChatRecyclerView
        liveClassChatRecyclerViewAdapter = LiveClassChatRecyclerViewAdapter(this)
        recyclerView.adapter = liveClassChatRecyclerViewAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        handleChat()

        val container = binding.remoteViewFrame
        val textView = TextView(baseContext)
        textView.text = "Host has not started the class yet.."
        textView.gravity = Gravity.CENTER
        container.removeAllViews()
        container.addView(textView)


        val tokenBuilder = RtcTokenBuilder2()
        val timeStamp = (System.currentTimeMillis() / 1000 + 60).toInt()

        token = tokenBuilder.buildTokenWithUid(
            appId,
            appCertificate,
            channelName,
            0,
            RtcTokenBuilder2.Role.ROLE_PUBLISHER,
            timeStamp,
            timeStamp
        )

        initAgoraEngineAndJoinChannel()

    }

    private fun handleChat() {
        binding.apply {
            liveClassChatSend.setOnClickListener {
                val message = liveClassChatText.text.toString().trim()
                if (message.isNotEmpty() || message.equals("")) {
                    val sharedPreferences =
                        getSharedPreferences("LOGGED_IN_USER_DETAILS", MODE_PRIVATE)
                    val senderName = sharedPreferences.getString("displayedName", "")!!
                    val senderUid = sharedPreferences.getString("uid", "")!!
                    var liveChatModal =
                        LiveChatModal(message, classInfo.classId, senderName, senderUid, "")
                    firebaseClient.sendLiveChatMessage(liveChatModal) {
                        if (it) {
                            liveClassChatRecyclerViewAdapter.updateMessages(liveChatModal)
                            liveClassChatText.setText("")
                            recyclerView.smoothScrollToPosition(liveClassChatRecyclerViewAdapter.itemCount - 1)
                            Toast.makeText(
                                baseContext,
                                "Message Sent Successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(baseContext, "failed !! ", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }


    private fun initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine()

        agoraEngine!!.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
        agoraEngine!!.setClientRole(Constants.CLIENT_ROLE_AUDIENCE)
        agoraEngine!!.enableVideo()

        joinChannel()

    }

    private fun joinChannel() {
        agoraEngine!!.joinChannel(token, channelName, null, 0)
    }

    private fun initializeAgoraEngine() {
        try {
            agoraEngine = RtcEngine.create(baseContext, appId, mRtcEventHandler)
        } catch (e: Exception) {
            Log.d(TAG, "Exception : ${e.message.toString()}")
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread { setupRemoteVideo(uid) }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread { onRemoteUserLeft() }
        }

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            runOnUiThread { showMessage("") }
        }
    }

    private fun setupRemoteVideo(uid: Int) {
        val container = binding.remoteViewFrame
        container.removeAllViews()

        if (container.childCount >= 1) {
            val textView = TextView(baseContext)
            textView.text = "User has not joined the call yet"
            textView.gravity = Gravity.CENTER
            container.addView(textView)
            return
        }

        val surfaceView = SurfaceView(baseContext)
        container.addView(surfaceView)
        agoraEngine!!.setupRemoteVideo(
            VideoCanvas(
                surfaceView,
                VideoCanvas.RENDER_MODE_FIT,
                uid
            )
        )
        surfaceView.tag = uid
        isJoined = true
    }

    fun onRemoteUserLeft() {
        val container = binding.remoteViewFrame
        container.removeAllViews()

        val textView = TextView(baseContext)
        textView.text = "Host ended the class"
        textView.gravity = Gravity.CENTER
        container.addView(textView)

        showMessage("The class has been ended by the host...")
    }


    override fun onDestroy() {
        super.onDestroy()
        agoraEngine!!.leaveChannel()
        RtcEngine.destroy()
        agoraEngine = null
    }

}