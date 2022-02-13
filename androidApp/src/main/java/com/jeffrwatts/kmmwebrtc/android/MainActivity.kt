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
import com.jeffrwatts.kmmwebrtc.DogModel
import com.jeffrwatts.kmmwebrtc.FirebaseSignalingChannel
import com.shepeliev.webrtckmp.MediaDevices
import com.shepeliev.webrtckmp.MediaStream
import com.shepeliev.webrtckmp.eglBaseContext
import com.shepeliev.webrtckmp.initializeWebRtc
import kotlinx.coroutines.launch
import org.webrtc.SurfaceViewRenderer

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
        private const val CAMERA_AUDIO_PERMISSION_REQUEST_CODE = 1
    }

    private var localMediaStream: MediaStream? = null
    private val buttonStartStop: Button by lazy { findViewById(R.id.buttonStartStop) }
    private val buttonToggleCamera: Button by lazy { findViewById(R.id.buttonToggleCamera) }
    private val surfaceViewLocalVideo: SurfaceViewRenderer by lazy { findViewById(R.id.surfaceViewLocalVideo) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeWebRtc(this)

        buttonStartStop.isEnabled = false
        buttonStartStop.setOnClickListener {
            if (localMediaStream != null) {
                stopVideo()
            } else {
                startVideoAsync()
            }
        }
        buttonToggleCamera.isEnabled = false
        buttonToggleCamera.setOnClickListener {
            lifecycleScope.launch { localMediaStream?.videoTracks?.firstOrNull()?.switchCamera() }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), CAMERA_AUDIO_PERMISSION_REQUEST_CODE)
        } else {
            onCameraAudioPermissionsGranted()
        }
    }

    private fun startVideoAsync() = lifecycleScope.launchWhenStarted{
        try {
            localMediaStream = MediaDevices.getUserMedia(video = true)
            localMediaStream?.videoTracks?.firstOrNull()?.also {
                surfaceViewLocalVideo.init(eglBaseContext, null)
                it.addSink(surfaceViewLocalVideo)
            }
            runOnUiThread {
                buttonStartStop.text = "Stop Video"
                buttonToggleCamera.isEnabled = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception", e)
        }
    }

    private fun stopVideo() {
        surfaceViewLocalVideo.release()
        localMediaStream?.release()
        localMediaStream = null
        buttonStartStop.text = "Start Video"
        buttonToggleCamera.isEnabled = false
    }

    private fun onCameraAudioPermissionsGranted() {
        buttonStartStop.isEnabled = true
        testDogsApi()
        testDogsFirebase()
    }

    private fun testDogsApi() {
        lifecycleScope.launch {
            try {
                val dogList = DogModel().getDogs()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Got ${dogList.size} dogs via API", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception when getting Dogs via API.", e)
            }
        }
    }

    private fun testDogsFirebase() {
        lifecycleScope.launch {
            try {
                val dogList = FirebaseSignalingChannel().getDogs()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Got ${dogList.size} dogs via Firebase", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception when getting Dogs via Firebase.", e)
            }
        }
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

