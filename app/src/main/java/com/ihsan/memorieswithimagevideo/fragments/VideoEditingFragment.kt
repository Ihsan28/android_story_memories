package com.ihsan.memorieswithimagevideo.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import com.ihsan.memorieswithimagevideo.R
import com.ihsan.memorieswithimagevideo.data.Data
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

private const val TAG = "VideoEditingFragment"
class VideoEditingFragment : Fragment() {
    val REQUEST_PERMISSION_CODE = 1000
    private lateinit var videoView: VideoView
    private lateinit var videoUri: Uri
    private val output = "/storage/emulated/0/Download/output.mp4"
    private lateinit var rangeSeekBar: RangeSeekBar
    private lateinit var progressBar: ProgressBar
    private lateinit var doneButton: Button
    private var startTime: Float = 0f
    private var endTime: Float = 0f
    private var duration: Float = 0f

    // Create a scheduled executor service with a single thread
    val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val progressBarExecutor: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    private lateinit var pickMediaContract: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video_editing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoView = view.findViewById(R.id.videoView)
        rangeSeekBar = view.findViewById(R.id.rangeSeekBar)
        progressBar = view.findViewById(R.id.progressBar)
        doneButton = view.findViewById(R.id.doneBtn)

        //pick media content
        setPickMediaContract()
        pickMediaContent()

        videoViewSetOnPrepareListener()

        rangeSeekBarListener()

        videoView.setOnCompletionListener {
            videoView.seekTo(startTime.toInt() * 1000)
            videoView.start()
        }

        doneButton.setOnClickListener {
            // Check if we have permission to read and write to external storage
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                // If permissions are not granted, request them
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION_CODE)
            } else {
                // Permission is already granted, you can proceed with the operation
                executeFFmpegCommandToTrimVideo()
            }


            /*val rc: Int = FFmpeg.execute("-i file1.mp4 -c:v mpeg4 file2.mp4")

            when (rc) {
                RETURN_CODE_SUCCESS -> {
                    Log.i(Config.TAG, "Command execution completed successfully.")
                }
                RETURN_CODE_CANCEL -> {
                    Log.i(Config.TAG, "Command execution cancelled by user.")
                }
                else -> {
                    Log.i(
                        Config.TAG,
                        String.format("Command execution failed with rc=%d and the output below.", rc)
                    )
                    Config.printLastCommandOutput(Log.INFO)
                }
            }*/

            //val execution=FFmpeg.executeAsync("-i ${videoPath.path} -ss $startTime -to $endTime -c copy ${videoPath.path}")
        }
    }

    private fun executeFFmpegCommandToTrimVideo(){
        Toast.makeText(requireContext(), "Done", Toast.LENGTH_SHORT).show()

        val videoPath = getVideoFileFromContentUri(videoUri) // Get the file path from the content Uri

        val input = videoPath.toString()
        val command = arrayOf("-y", "-i", videoPath, "-ss", startTime.toString(), "-to", endTime.toString(), "-c", "copy", output)

        FFmpeg.executeAsync(command) { executionId, returnCode ->
            if (returnCode == RETURN_CODE_SUCCESS) {
                Log.i(Config.TAG, "Async command execution completed successfully.")
            } else if (returnCode == RETURN_CODE_CANCEL) {
                Log.i(Config.TAG, "Async command execution cancelled by user.")
            } else {
                Log.i(
                    Config.TAG,
                    String.format(
                        "Async command execution failed with returnCode=%d.",
                        returnCode
                    )
                )
            }
        }
    }

    private fun createTempVideoFile(): File? {
        try {
            val tempDir = requireContext().cacheDir // You can use another directory if needed
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val tempFileName = "temp_video_$timeStamp.mp4"
            val tempFile = File(tempDir, tempFileName)
            return tempFile
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun getVideoFileFromContentUri(contentUri: Uri): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = requireContext().contentResolver.query(contentUri, projection, null, null, null)
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
        cursor?.moveToFirst()
        val videoPath = cursor?.getString(columnIndex ?: -1)
        cursor?.close()

        return if (!videoPath.isNullOrEmpty()) {
            File(videoPath).path
        } else {
            null
        }
    }


    private fun videoViewSetOnPrepareListener() {
        videoView.setOnPreparedListener {
            duration = videoView.duration / 1000f
            rangeSeekBar.setRange(0f, duration)
            rangeSeekBar.setValue(0f, duration)
            startTime = 0f
            endTime = duration

            videoView.seekTo(startTime.toInt() * 1000)
            videoView.start()

            progressBar.max = 100

            progressBarExecutor.scheduleAtFixedRate({
                requireActivity().runOnUiThread {
                    if (videoView.currentPosition / 1000f <= endTime) {
                        val progress = (((videoView.currentPosition.toFloat()-startTime*1000) / (duration * 1000)) * 100).toInt()
                        progressBar.progress = progress
                    }
                }
            }, 0, 100, TimeUnit.MILLISECONDS)
        }
    }

    private fun rangeSeekBarListener() {
        rangeSeekBar.setOnRangeChangedListener(object : OnRangeChangedListener {
            override fun onRangeChanged(
                view: RangeSeekBar?,
                leftValue: Float,
                rightValue: Float,
                isFromUser: Boolean
            ) {
                startTime = leftValue
                endTime = rightValue
                duration = endTime - startTime
                videoView.seekTo(startTime.toInt() * 1000)
                        Log.d(TAG, "onRangeChanged: updated onRangeChanged")

                executor.scheduleAtFixedRate({
                    // Check if the current position is greater than or equal to endTime
                    if (videoView.currentPosition / 1000f >= endTime) {
                        //Log.d(TAG, "onRangeChanged: updated")
                        // Seek to the startTime and start playing
                        videoView.seekTo((startTime.toInt() * 1000))
                        videoView.start()
                    }
                }, 0, 500, TimeUnit.MILLISECONDS)
            }

            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
                videoView.pause()
            }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
                videoView.seekTo(startTime.toInt() * 1000)
                videoView.start()
            }
        })
    }

    private fun setPickMediaContract() {
        pickMediaContract =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val clipData = result.data!!.clipData
                    //contentUris.value!!.clear()

                    if (clipData != null) {
                        for (i in 0 until clipData.itemCount) {
                            val mediaUri = clipData.getItemAt(i).uri
                            videoUri = mediaUri
                            Toast.makeText(
                                requireContext(),
                                videoUri.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                            videoView.setVideoURI(videoUri)
                        }
                    } else {
                        val mediaUri = result.data!!.data
                        if (mediaUri != null) {
                            videoUri = mediaUri

                            Toast.makeText(
                                requireContext(),
                                videoUri.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d(TAG, "setPickMediaContract: ${videoUri.toString()}")
                            videoView.setVideoURI(videoUri)
                        }
                    }
                    //update media items
                    Data().mapContentUrisToMediaItems()
                }
            }
    }

    private fun pickMediaContent() {
        val pickImagesIntent = Intent(Intent.ACTION_PICK)
        pickImagesIntent.type = "image/*"
        pickImagesIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

        val pickVideosIntent = Intent(Intent.ACTION_PICK)
        pickVideosIntent.type = "video/*"
        pickVideosIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

        val chooserIntent = Intent.createChooser(
            pickImagesIntent,
            "Select Images and Videos"
        )
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickVideosIntent))

        try {
            pickMediaContract.launch(chooserIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle the exception, possibly by displaying an error message.
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
        progressBarExecutor.shutdown()
    }
}