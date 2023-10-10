package com.ihsan.memorieswithimagevideo.Utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.ihsan.memorieswithimagevideo.R
import com.ihsan.memorieswithimagevideo.fragments.AnimationRecordingCallbacks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class VideoCapture(private val cardView: CardView, private val recordingCallbacks: AnimationRecordingCallbacks) {

    private val TAG = "VideoCapture"
    private val prefixOfFrame= "frame"
    private var mediaRecorder: MediaRecorder? = null

    private val cacheDirectory = cardView.context.cacheDir
    private val frameRate = 24
    private val scheduleDelay = 1000L / frameRate
    private var isFfmpegRecorderStarted: Boolean = false
    var isReadyToExport:Boolean= false
    private var executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r).apply { priority = Thread.MAX_PRIORITY }
    }
    private var reusableBitmap: Bitmap? = null
    private var canvas: Canvas? = null

    fun startRecordingFFMPEG() {
        if (isFfmpegRecorderStarted) {
            Log.d(TAG, "startRecordingFFMPEG: recording already started")
            return
        }
        //clear files if exist from previous recording
        clearCacheDir()

        recordingCallbacks.onRecordingStarted()
        isFfmpegRecorderStarted = true
        isReadyToExport = false
        var frameCount = 0

        //background executor loop by default in background thread
        executor.scheduleAtFixedRate({
            if (frameCount > 9999) {
                stopRecordingUsingFFMPEG()
                return@scheduleAtFixedRate
            }
            // Save bitmap to file with a unique name
            val fileName="frame${"%04d".format(frameCount)}.jpg"

            CoroutineScope(Dispatchers.IO).launch {
                // Check if recording should stop
                try {
                    // Save bitmap to file with a unique name
                    val path = saveBitmapToFile(fileName)
                    Log.d(TAG, "startRecordingUsingFFMPEG: $path")
                } catch (e: Exception) {
                    Log.e(TAG, "Error capturing frame: $e")
                }
            }
            frameCount++
        }, 0, scheduleDelay, TimeUnit.MILLISECONDS)

        Log.i(TAG, "startRecordingFFMPEG: recording started")
    }

    fun stopRecordingUsingFFMPEG() {
        if (!isFfmpegRecorderStarted) {
            Log.d(TAG, "stopRecordingUsingFFMPEG: recording already stopped")
            return
        }

        try {

            CoroutineScope(Dispatchers.IO).launch {
                isFfmpegRecorderStarted = false
                isReadyToExport = true
                executor.shutdown()
                withContext(Dispatchers.Main){
                    recordingCallbacks.onRecordingStopped()
                }

                cacheDirectory.listFiles()
                    ?.let { imagesPath -> recordingCallbacks.onFrameAvailable(imagesPath.map { it.absolutePath }) }
                withContext(Dispatchers.Main){
                    recordingCallbacks.onExportReady()
                }
                //exportVideoFFMPEG()
                Log.i(TAG, "stopRecordingUsingFFMPEG: recording stopped")
            }

        } catch (e: Exception) {
            Log.e(TAG, "stopRecordingUsingFFMPEG: $e")
        }
    }

    fun exportVideoFFMPEG() {
        recordingCallbacks.onExportStarted()
        CoroutineScope(Dispatchers.IO).launch{
            val output = getOutputFilePath()
            //get audio  resource
            val audioFile = File(cacheDirectory, "aylex.mp3")
            audioFile.outputStream().use { outputStream ->
                cardView.context.resources.openRawResource(R.raw.aylex).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            val command =
                "-framerate $frameRate -i ${cacheDirectory}/frame%04d.jpg -i ${audioFile.absolutePath} -c:v libx264 -tune stillimage -c:a aac -b:a 192k -pix_fmt yuv420p -shortest $output"

            executeCommand(command)

            //delete frames
            deleteFiles()
        }
    }

    private fun executeCommand(command: String) {
        val totalFrames = cacheDirectory.listFiles { _, name -> name.startsWith(prefixOfFrame) }?.size

        Config.enableLogCallback { logMessage ->
            // Extract relevant information from the log message
            val log = logMessage.text
            val frameIndex = log.indexOf("frame=")
            val fpsIndex = log.indexOf("fps=")
            if (frameIndex != -1 && fpsIndex != -1) {
                val frame = log.substring(frameIndex + 6, fpsIndex).trim().toIntOrNull()
                if (frame != null) {
                    val percentage= ((frame * 100.00) / totalFrames!!).roundToInt()
                    // Update UI with progress information
                    recordingCallbacks.onExportProgress(percentage)
                }
            }
        }

        try {
            FFmpeg.execute(command)
            recordingCallbacks.onExportFinished()
        } catch (e: Exception) {
            recordingCallbacks.onExportFailed(e)
        }
    }

    private fun deleteFiles(
        directory: File = cacheDirectory,
        prefix: String = prefixOfFrame
    ) {
        val files = directory.listFiles { _, name -> name.startsWith(prefix) }
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.IO) {
                files?.map { file ->
                    file.delete()
                }
            }
        }

        isReadyToExport = false
    }

    private fun clearCacheDir(cacheDir: File= cacheDirectory) {
        try {
            val files = cacheDir.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isDirectory) {
                        clearCacheDir(file)
                    }
                    file.delete()
                }
            }
            isReadyToExport = false
        } catch (e: Exception) {
            // Handle the exception
            e.printStackTrace()
        }
    }

    private fun saveBitmapToFile( filename: String): String {
        //get bitmap from surface view
        val bitmap = captureView(cardView)
        //save bitmap to file
        val file = File(cacheDirectory, filename)
        file.outputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        }

        bitmap.recycle()
        return file.path
    }

    private fun getOutputFilePath(): String {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        return "${dir.absolutePath}/animatedViewCapture_${System.currentTimeMillis()}.mp4"
    }

    private fun captureView(view: CardView): Bitmap {
        val bitmap = reusableBitmap ?: Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = this.canvas ?: Canvas()
        canvas.setBitmap(bitmap) // Set the bitmap to the canvas
        view.draw(canvas)
        return bitmap
    }

    fun createVideoFromImages(imagePaths: List<String>) {

        val outputVideoPath = getOutputFilePath()

        // Launch a coroutine in the IO Dispatcher (background thread)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Your long-running task to create video from images
                val imageFiles = imagePaths.joinToString("|")
                val command =
                    "-i concat:$imageFiles -c:v libx264 -vf fps=25 -pix_fmt yuv420p $outputVideoPath"

                executeCommand(command)

                // update the UI on the Main Dispatcher (UI thread)
                withContext(Dispatchers.Main) {
                    // reminder: show a success message or update a progress bar or something else
                }
            } catch (e: Exception) {

                withContext(Dispatchers.Main) {
                    // update UI to indicate error or show a toast
                }
            }
        }
    }

    fun startRecordingMediaRecorder() {
        Toast.makeText(cardView.context, "recording started", Toast.LENGTH_SHORT).show()
        if (Build.VERSION_CODES.S <= Build.VERSION.SDK_INT) {

            mediaRecorder = MediaRecorder(cardView.context).apply {
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setOutputFile(getOutputFilePath())
                //setPreviewDisplay(cardView.holder.surface)
                try {
                    prepare()
                    start()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else {
            mediaRecorder = MediaRecorder().apply {
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setOutputFile(getOutputFilePath())
                //setPreviewDisplay(cardView.holder.surface)
                try {
                    prepare()
                    start()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun stopRecordingMediaRecorder() {
        Toast.makeText(cardView.context, "recording stop", Toast.LENGTH_SHORT).show()
        mediaRecorder?.apply {
            stop()
            reset()
            release()
        }
        mediaRecorder = null
    }
}
