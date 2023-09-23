package com.ihsan.memorieswithimagevideo.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.ihsan.memorieswithimagevideo.R
import com.ihsan.memorieswithimagevideo.data.Data
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

private const val TAG = "VideoEditingFragment"
class VideoEditingFragment : Fragment() {
    private lateinit var videoView: VideoView
    private lateinit var videoPath: Uri
    private lateinit var rangeSeekBar: RangeSeekBar
    private lateinit var progressBar: ProgressBar
    private var startTime: Float = 0f
    private var endTime: Float = 0f
    private var duration: Float = 0f

    // Create a scheduled executor service with a single thread
    val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    val progressBarExecutor: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    val handler = Handler()

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

        //pick media content
        setPickMediaContract()
        pickMediaContent()

        videoViewSetOnPrepareListener()

        rangeSeekBarListener()

        videoView.setOnCompletionListener {
            videoView.seekTo(startTime.toInt() * 1000)
            videoView.start()
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

            //progressBar.max = videoView.duration/1000
            progressBar.max = 100

            progressBarExecutor.scheduleAtFixedRate({
                requireActivity().runOnUiThread {
                    Log.d(TAG, "onViewCreated: division result ${videoView.currentPosition / 1000f} & endTime $endTime")
                    if (videoView.currentPosition / 1000f <= endTime) {
                        Log.d(TAG, "onViewCreated: if ${videoView.currentPosition}")
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
                            videoPath = mediaUri
                            Toast.makeText(
                                requireContext(),
                                videoPath.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                            videoView.setVideoURI(videoPath)
                        }
                    } else {
                        val mediaUri = result.data!!.data
                        if (mediaUri != null) {
                            videoPath = mediaUri

                            Toast.makeText(
                                requireContext(),
                                videoPath.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d(TAG, "setPickMediaContract: ${videoPath.toString()}")
                            videoView.setVideoURI(videoPath)
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
        handler.removeCallbacksAndMessages(null)
    }
}