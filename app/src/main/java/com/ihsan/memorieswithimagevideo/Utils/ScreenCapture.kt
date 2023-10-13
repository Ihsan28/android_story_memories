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
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.Surface
import java.io.IOException
import java.nio.ByteBuffer

class ScreenCapture(private val activity: Activity) {
    private val TAG = "ScreenCapture"

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var screenDensity: Int = 0

    private var mediaCodec: MediaCodec? = null
    private var mediaMuxer: MediaMuxer? = null
    private var videoTrackIndex: Int = -1

    private val outputFilePath: String by lazy { getOutputFilePath("animatedMediaRecording") }

    fun initRecording(callback: (Intent) -> Unit) {
        val displayMetrics = activity.resources.displayMetrics
        screenDensity = displayMetrics.densityDpi

        Log.d(TAG, "initRecording: dense: $screenDensity")

        val projectionManager = activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        callback(projectionManager.createScreenCaptureIntent())
    }

    fun startRecording(resultCode: Int, data: Intent?) {
        val projectionManager = activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(resultCode, data!!)

        val displayMetrics = activity.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        Log.d(TAG, "startRecording: width: $screenWidth, height: $screenHeight")

        imageReader = ImageReader.newInstance(screenWidth, screenHeight, android.graphics.PixelFormat.RGBA_8888, 2)

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            screenWidth, screenHeight, screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface, null, null
        )

        //recording to local file
        try {
            val format = MediaFormat.createVideoFormat("video/avc", /*width*/720, /*height*/1280)
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            format.setInteger(MediaFormat.KEY_BIT_RATE, 6000000)
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)

            mediaCodec = MediaCodec.createEncoderByType("video/avc")
            mediaCodec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val surface = mediaCodec?.createInputSurface()

            mediaMuxer = MediaMuxer(outputFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            mediaCodec?.setCallback(object : MediaCodec.Callback() {
                override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
                    val buffer: ByteBuffer = codec.getOutputBuffer(index)!!
                    if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0 && info.size != 0) {
                        codec.releaseOutputBuffer(index, false)
                    } else {
                        mediaMuxer?.writeSampleData(videoTrackIndex, buffer, info)
                        codec.releaseOutputBuffer(index, false)
                    }
                }

                override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {}

                override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                    videoTrackIndex = mediaMuxer?.addTrack(format) ?: -1
                    mediaMuxer?.start()
                }

                override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {}
            })

            mediaCodec?.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun stopRecording() {
        virtualDisplay?.release()
        mediaProjection?.stop()
        imageReader?.close()

        Log.d(TAG, "stopRecording: $outputFilePath")

        try {
            // Only stop if we have started and actually written data
            if (mediaMuxer != null && videoTrackIndex != -1) {
                mediaMuxer?.stop()
            }
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Error stopping MediaMuxer", e)
        } finally {
            mediaCodec?.stop()
            mediaCodec?.release()
            mediaMuxer?.release()
        }
    }

    fun captureScreenshot(): Surface? {
        return imageReader?.surface
    }

    private fun getOutputFilePath(prefix: String="animatedViewCapture_"): String {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        return "${dir.absolutePath}/$prefix${System.currentTimeMillis()}.mp4"
    }
}
