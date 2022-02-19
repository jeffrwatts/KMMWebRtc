package com.jeffrwatts.kmmwebrtc.android

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.jeffrwatts.kmmwebrtc.RtcClient
import com.shepeliev.webrtckmp.*
import kotlinx.coroutines.launch
import org.webrtc.SurfaceViewRenderer

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
        private const val CAMERA_AUDIO_PERMISSION_REQUEST_CODE = 1
    }

    private val buttonStartVideo: Button by lazy { findViewById(R.id.buttonStartVideo) }
    private val buttonToggleCamera: Button by lazy { findViewById(R.id.buttonToggleCamera) }
    private val buttonMakeCall: Button by lazy { findViewById(R.id.buttonMakeCall) }
    private val buttonAnswerCall: Button by lazy { findViewById(R.id.buttonAnswerCall) }
    private val surfaceViewRemoteVideo: SurfaceViewRenderer by lazy { findViewById(R.id.surfaceViewRemoteVideo) }
    private val surfaceViewLocalVideo: SurfaceViewRenderer by lazy { findViewById(R.id.surfaceViewLocalVideo) }

    private lateinit var rtcClient: RtcClient
    private var localMediaStream: MediaStream? = null
    private var receivedOffer: SessionDescription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonStartVideo.isEnabled = false
        buttonStartVideo.setOnClickListener {
            if (localMediaStream != null) {
                stopVideo()
            } else {
                startVideo()
            }
        }

        buttonToggleCamera.isEnabled = false
        buttonToggleCamera.setOnClickListener {
            lifecycleScope.launch { localMediaStream?.videoTracks?.firstOrNull()?.switchCamera() }
        }

        buttonMakeCall.isEnabled = false
        buttonMakeCall.setOnClickListener {
            buttonMakeCall.isEnabled = false
            makeCall()
        }

        buttonAnswerCall.isEnabled = false
        buttonAnswerCall.setOnClickListener {
            buttonAnswerCall.isEnabled = false
            answerCall()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), CAMERA_AUDIO_PERMISSION_REQUEST_CODE)
        } else {
            onCameraAudioPermissionsGranted()
        }
    }

    private fun startVideo() = lifecycleScope.launch {
        try {
            localMediaStream = MediaDevices.getUserMedia(video = true)
            localMediaStream?.videoTracks?.firstOrNull()?.also { videoStreamtrack->
                videoStreamtrack.addSink(surfaceViewLocalVideo)
                rtcClient.setLocalVideoTrack(videoStreamtrack)
            }
            runOnUiThread {
                buttonStartVideo.text = "Stop Video"
                buttonToggleCamera.isEnabled = true
                buttonMakeCall.isEnabled = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception", e)
        }
    }

    private fun makeCall() = lifecycleScope.launch {
        try {
            rtcClient.makeCall(receiveVideo = true, receiveAudio = false)
        } catch (e: Exception) {
            Log.e(TAG, "Exception", e)
        }
    }

    private fun answerCall() = lifecycleScope.launch {
        try {
            receivedOffer?.let {
                rtcClient.answerCall(it, receiveVideo = true, receiveAudio = false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception", e)
        }
    }

    private fun stopVideo() {
        surfaceViewLocalVideo.release()
        localMediaStream?.release()
        localMediaStream = null
    }

    private fun onCameraAudioPermissionsGranted() {
        initializeWebRtc(this)

        buttonStartVideo.isEnabled = true

        rtcClient = RtcClient("android", "ios")

        rtcClient.onIncomingCall = { offer ->
            receivedOffer = offer
            runOnUiThread {
                buttonAnswerCall.isEnabled = true
                Toast.makeText(this, "Incoming Call from (Local)", Toast.LENGTH_LONG).show()
            }
        }

        rtcClient.onRemoteVideoTrack = { videoStreamTrack ->
            videoStreamTrack.addSink(surfaceViewRemoteVideo)
        }

        // Initialize local and loopback video surfaces.
        surfaceViewLocalVideo.init(eglBaseContext, null)
        surfaceViewRemoteVideo.init(eglBaseContext, null)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_AUDIO_PERMISSION_REQUEST_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            onCameraAudioPermissionsGranted()
        } else {
            Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_LONG).show()
        }
    }
}

