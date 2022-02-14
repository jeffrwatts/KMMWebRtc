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

    private val buttonLocalStartVideo: Button by lazy { findViewById(R.id.buttonLocalStartVideo) }
    private val buttonLocalToggle: Button by lazy { findViewById(R.id.buttonLocalToggle) }
    private val buttonCallLocal: Button by lazy { findViewById(R.id.buttonCallLocal) }
    private val buttonAnswerLoopback: Button by lazy { findViewById(R.id.buttonAnswerLoopback) }
    private val surfaceViewLocalVideo: SurfaceViewRenderer by lazy { findViewById(R.id.surfaceViewLocalVideo) }
    private val surfaceViewLoopBackRemote: SurfaceViewRenderer by lazy { findViewById(R.id.surfaceViewLoopBackRemote) }

    private lateinit var rtcClientLocal: RtcClient
    private var localMediaStream: MediaStream? = null

    private lateinit var rtcClientLoopback: RtcClient
    private var offerFromLocal: SessionDescription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO There is a bug when getting permissions.  Need to move all of the WebRtc stuff until
        // after permissions have been granted.
        initializeWebRtc(this)

        // Set up Local Client.
        rtcClientLocal = RtcClient("local", "loopback")
        rtcClientLocal.onIceCandidateLoopbackHack = { iceCandidate->
            rtcClientLoopback.loopbackHackAddIceCandidate(iceCandidate)
        }

        // Set up Loopback Client.
        rtcClientLoopback = RtcClient("loopback", "local")
        rtcClientLoopback.onIceCandidateLoopbackHack = { iceCandidate ->
            rtcClientLocal.loopbackHackAddIceCandidate(iceCandidate)
        }

        rtcClientLoopback.onIncomingCall = { offer ->
            offerFromLocal = offer
            runOnUiThread {
                buttonAnswerLoopback.isEnabled = true
                Toast.makeText(this, "Incoming Call from (Local)", Toast.LENGTH_LONG).show()
            }
        }

        rtcClientLoopback.onRemoteVideoTrack = { videoStreamTrack ->
            videoStreamTrack.addSink(surfaceViewLoopBackRemote)
        }

        buttonLocalStartVideo.isEnabled = false
        buttonLocalStartVideo.setOnClickListener {
            if (localMediaStream != null) {
                stopVideo()
            } else {
                startVideo()
            }
        }

        buttonLocalToggle.isEnabled = false
        buttonLocalToggle.setOnClickListener {
            lifecycleScope.launch { localMediaStream?.videoTracks?.firstOrNull()?.switchCamera() }
        }

        buttonCallLocal.isEnabled = false
        buttonCallLocal.setOnClickListener {
            buttonCallLocal.isEnabled = false
            makeCall()
        }

        buttonAnswerLoopback.isEnabled = false
        buttonAnswerLoopback.setOnClickListener {
            offerFromLocal?.let {
                buttonAnswerLoopback.isEnabled = false
                answerCall()
            }
        }

        // Initialize local and loopback video surfaces.
        surfaceViewLocalVideo.init(eglBaseContext, null)
        surfaceViewLoopBackRemote.init(eglBaseContext, null)

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
                rtcClientLocal.setLocalVideoTrack(videoStreamtrack)
            }
            runOnUiThread {
                buttonLocalStartVideo.text = "Stop Video"
                buttonLocalToggle.isEnabled = true
                buttonCallLocal.isEnabled = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception", e)
        }
    }

    private fun makeCall() = lifecycleScope.launch {
        try {
            rtcClientLocal.makeCall(receiveVideo = true, receiveAudio = false)
        } catch (e: Exception) {
            Log.e(TAG, "Exception", e)
        }
    }

    private fun answerCall() = lifecycleScope.launch {
        try {
            offerFromLocal?.let {
                rtcClientLoopback.answerCall(it, receiveVideo = true, receiveAudio = false)
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
        buttonLocalStartVideo.isEnabled = true
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

