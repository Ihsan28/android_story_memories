package com.ihsan.memorieswithimagevideo.Utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.arthenica.mobileffmpeg.FFmpeg
import com.ihsan.memorieswithimagevideo.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class VideoCapture(private val cardView: CardView) {
    private val TAG = "VideoCapture"
    private var mediaRecorder: MediaRecorder? = null
    private val frameRate = 10L
    private val scheduleDelay = 1000L / frameRate
    var isFfmpegRecorderStarted: Boolean = false
    private var executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    fun startRecordingUsingFFMPEG() {
        if (isFfmpegRecorderStarted) {
            Toast.makeText(cardView.context, "recording already started", Toast.LENGTH_SHORT)
                .show()
            return
        }
        isFfmpegRecorderStarted = true
        var frameCount = 0

        //get bitmap from surface view
        val bitmap = captureView(cardView)

        //background executor loop by default in background thread
        executor.scheduleAtFixedRate({

            //runnable
            if (frameCount > 9999) {
                stopRecordingUsingFFMPEG()
            }

            //get bitmap from surface view
            val bitmap = captureView(cardView)

            //save bitmap to file with unique name
            val path =
                saveBitmapToFile(cardView.context, bitmap, "frame${"%04d".format(frameCount)}.jpg")
            Log.d(TAG, "startRecordingUsingFFMPEG: $frameCount")
            Log.d(TAG, "startRecordingUsingFFMPEG: $path")
            frameCount++

        }, 0, 100, TimeUnit.MILLISECONDS)

        Toast.makeText(cardView.context, "recording started", Toast.LENGTH_SHORT).show()
    }

    fun stopRecordingUsingFFMPEG() {
        if (!isFfmpegRecorderStarted) {
            Toast.makeText(cardView.context, "recording already stopped", Toast.LENGTH_SHORT)
                .show()
            return
        }

        try {
            isFfmpegRecorderStarted = false
            executor.shutdown()

            val output = getOutputFilePath()
            //get audio  resource
            val audioFile = File(cardView.context.cacheDir, "aylex.mp3")
            audioFile.outputStream().use { outputStream ->
                cardView.context.resources.openRawResource(R.raw.aylex).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            val command =
                "-framerate $frameRate -i ${getOutputFilePathImage()}frame%04d.jpg -i ${audioFile.absolutePath} -c:v libx264 -tune stillimage -c:a aac -b:a 192k -pix_fmt yuv420p -shortest $output"
            val commanz =
                "-framerate $frameRate -i ${getOutputFilePathImage()}frame%04d.jpg -i ${audioFile.absolutePath} -c:v libx264 -tune stillimage -c:a aac -b:a 192k -pix_fmt yuv420p -shortest $output"

            executeCommand(command)
            //delete frames
            //deleteFiles()

            Toast.makeText(cardView.context, "recording stop", Toast.LENGTH_SHORT).show()
            // reminder: show a success message or update a progress bar or something else

        } catch (e: Exception) {
            Log.d(TAG, "stopRecordingUsingFFMPEG: $e")
            Toast.makeText(cardView.context, "recording stop with catch: $e", Toast.LENGTH_SHORT)
                .show()
            // reminder: show a success message or update a progress bar or something else
        }

    }

    private fun deleteFiles(
        directory: File = cardView.context.filesDir,
        prefix: String = "frame"
    ) {
        val files = directory.listFiles { _, name -> name.startsWith(prefix) }
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.IO) {
                files?.map { file ->
                    file.delete()
                }
            }
        }
    }

    private fun saveBitmapToFile(context: Context, bitmap: Bitmap, filename: String): String {
        val file = File(getOutputFilePathImage(), filename)
        val fileOutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
        fileOutputStream.close()
        return file.path
    }

    private fun captureView(view: CardView): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
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

    /*fun startRecordingUsingMediaRecorder() {
        if (Build.VERSION_CODES.S <= Build.VERSION.SDK_INT) {

            mediaRecorder = MediaRecorder(surfaceView.context).apply {
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setOutputFile(getOutputFilePath())
                setPreviewDisplay(surfaceView.holder.surface)
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
                setPreviewDisplay(surfaceView.holder.surface)
                try {
                    prepare()
                    start()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }


    }*/

    fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            reset()
            release()
        }
        mediaRecorder = null
    }

    private fun executeCommand(command: String) {
        FFmpeg.execute(command)
    }

    private fun getOutputFilePath(): String {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        return "${dir.absolutePath}/animatedViewCapture_${System.currentTimeMillis()}.mp4"
    }

    private fun getOutputFilePathImage(): String {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        return "${dir.absolutePath}/"
    }
}