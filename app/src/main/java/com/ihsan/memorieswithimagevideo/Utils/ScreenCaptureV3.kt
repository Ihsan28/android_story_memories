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
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.Surface
import java.io.IOException
import java.nio.ByteBuffer

class ScreenCaptureV3(private val activity: Activity) {
    private var mediaProjection: MediaProjection? = null
    private var mediaRecorder: MediaRecorder? = null

    fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(outputFilePath)
            setVideoSize(displayWidth, displayHeight)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setVideoEncodingBitRate(512 * 1000)
            setVideoFrameRate(30)
            prepare()
        }

        val callback = object : MediaProjection.Callback() {
            override fun onStop() {
                mediaRecorder?.stop()
                mediaRecorder?.reset()
                mediaProjection = null
            }
        }

        mediaProjection = projectionManager.getMediaProjection(resultCode, data)
        mediaProjection?.registerCallback(callback, handler)
        val surface = mediaRecorder?.surface
        mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            displayWidth, displayHeight, screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            surface, null, null
        )
        mediaRecorder?.start()
    }

    fun stopRecording() {
        mediaProjection?.stop()
    }
}
