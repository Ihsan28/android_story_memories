package com.ihsan.memorieswithimagevideo.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import com.ihsan.memorieswithimagevideo.R
import com.ihsan.memorieswithimagevideo.data.Data
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar

class VideoEditingFragment : Fragment() {
    private lateinit var videoView:VideoView
    private lateinit var videoPath: Uri
    private lateinit var rangeSeekBar: RangeSeekBar
    private var startTime:Long = 0
    private var endTime:Long = 0
    private var duration:Long = 0

    private val pickMediaContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val clipData = result.data!!.clipData
                //contentUris.value!!.clear()

                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val mediaUri = clipData.getItemAt(i).uri
                        Data.contentUris.value!!.add(mediaUri)
                        videoPath = mediaUri
                        Toast.makeText(requireContext(), videoPath.toString(), Toast.LENGTH_SHORT).show()
                        }
                } else {
                    val mediaUri = result.data!!.data
                    if (mediaUri != null) {
                        Data.contentUris.value!!.clear()
                        Data.contentUris.value!!.add(mediaUri)
                        videoPath = mediaUri
                        videoView.setVideoURI(videoPath)
                        Toast.makeText(requireContext(), videoPath.toString(), Toast.LENGTH_SHORT).show()
                        }
                }
                //update media items
                Data().mapContentUrisToMediaItems()
                Data.currentIndex = 0
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

        pickMediaContent()

        //videoView.setVideoURI(videoPath)

        videoView.setOnPreparedListener {
            duration = videoView.duration.toLong()
            rangeSeekBar.setRange(0f, duration.toFloat())
            rangeSeekBar.setValue(0f, duration.toFloat())
            startTime = 0
            endTime = duration

            videoView.seekTo(startTime.toInt())
        }

        rangeSeekBar.setOnRangeChangedListener( object : OnRangeChangedListener {
            override fun onRangeChanged(
                view: RangeSeekBar?,
                leftValue: Float,
                rightValue: Float,
                isFromUser: Boolean
            ) {
                startTime = leftValue.toLong()
                endTime = rightValue.toLong()
                videoView.seekTo(startTime.toInt())
            }

            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
                videoView.pause()
            }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
                videoView.seekTo(startTime.toInt())
                videoView.start()
            }
        })

        videoView.setOnCompletionListener {
            videoView.seekTo(startTime.toInt())
            videoView.start()
        }
    }
}