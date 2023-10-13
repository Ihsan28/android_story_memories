package com.ihsan.memorieswithimagevideo.Utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.Surface
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.ihsan.memorieswithimagevideo.R
import java.io.IOException
import java.nio.ByteBuffer

class ScreenCaptureV2(): AppCompatActivity(){
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null
    private var mediaProjectionCallback: MediaProjectionCallback? = null

    private val REQUEST_CODE = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_recorder)

        // Initialize MediaRecorder and set its configurations
        mediaRecorder = MediaRecorder().apply {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile("/path/to/outputfile.mp4") // Set your file path here
            setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setVideoEncodingBitRate(512 * 1000)
            setVideoFrameRate(30)
            prepare()
        }

        // Start screen recording on button click
        findViewById<Button>(R.id.startRecordingButton).setOnClickListener {
            val projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_CODE)
        }

        // Stop screen recording on button click
        findViewById<Button>(R.id.stopRecordingButton).setOnClickListener {
            mediaRecorder?.stop()
            mediaRecorder?.reset()
            stopScreenSharing()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE) {
            mediaProjectionCallback = MediaProjectionCallback()
            mediaProjection = (getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager)
                .getMediaProjection(resultCode, data).also {
                    it.registerCallback(mediaProjectionCallback, null)
                    virtualDisplay = it.createVirtualDisplay(
                        "MainActivity",
                        DISPLAY_WIDTH, DISPLAY_HEIGHT, SCREEN_DENSITY,
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                        mediaRecorder?.surface, null, null
                    )
                    mediaRecorder?.start()
                }
        }
    }

    private fun stopScreenSharing() {
        virtualDisplay?.release()
        destroyMediaProjection()
    }

    private fun destroyMediaProjection() {
        mediaProjection?.unregisterCallback(mediaProjectionCallback)
        mediaProjection?.stop()
        mediaProjection = null
    }

    private inner class MediaProjectionCallback : MediaProjection.Callback() {
        override fun onStop() {
            mediaRecorder?.stop()
            mediaRecorder?.reset()
            mediaProjection = null
            stopScreenSharing()
        }
    }

    companion object {
        private const val DISPLAY_WIDTH = 720
        private const val DISPLAY_HEIGHT = 1280
        private const val SCREEN_DENSITY = 320
    }
}
