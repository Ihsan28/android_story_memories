package com.ihsan.memorieswithimagevideo.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

import com.ihsan.memorieswithimagevideo.R
import com.ihsan.memorieswithimagevideo.adapter.EditViewPagerAdapter
import com.ihsan.memorieswithimagevideo.adapter.MiniPreviewAdapter
import com.ihsan.memorieswithimagevideo.data.Data
import com.ihsan.memorieswithimagevideo.data.Data.Companion.contentUris
import com.ihsan.memorieswithimagevideo.data.Data.Companion.currentIndex
import com.ihsan.memorieswithimagevideo.data.Data.Companion.mediaItems
import java.util.Formatter

private const val TAG = "EditSelectedFragment"

class EditSelectedFragment : Fragment() {
    private lateinit var viewPager: ViewPager2
    private lateinit var editViewPagerAdapter: EditViewPagerAdapter
    private lateinit var miniPreviewRecyclerView: RecyclerView
    private lateinit var miniPreviewAdapter: MiniPreviewAdapter

    //pick image launcher contract for image picker intent
    @SuppressLint("NotifyDataSetChanged")
    private val pickMediaContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val clipData = result.data!!.clipData
                //contentUris.value!!.clear()

                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val mediaUri = clipData.getItemAt(i).uri
                        contentUris.value!!.add(mediaUri)
                        val startTimeMs = "10" // Start time in milliseconds
                        val endTimeMs = "20" // End time in milliseconds
                        //videoCropper(mediaUri.toString(),mediaUri.toString(),startTimeMs,endTimeMs)
                    }
                } else {
                    val mediaUri = result.data!!.data
                    if (mediaUri != null) {
                        contentUris.value!!.clear()
                        contentUris.value!!.add(mediaUri)
                        val startTimeMs = "10" // Start time in milliseconds
                        val endTimeMs = "20" // End time in milliseconds
                        //videoCropper(mediaUri.toString(),mediaUri.toString(),startTimeMs,endTimeMs)
                    }
                }
                //update media items
                Data().mapContentUrisToMediaItems()
                currentIndex = 0
                callViewPagerAdapter()
            }
        }

    fun convertTimestampToString(timeInMs: Float): String {
        val totalSeconds = (timeInMs / 1000).toInt()
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        val formatter = Formatter()
        return if (hours > 0) {
            formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            formatter.format("%02d:%02d", minutes, seconds).toString()
        }
    }

/*
    private fun videoCropper(input: String, output: String, startPos: String, endPos: String) {

        val ffmpeg = FFmpeg.getInstance(requireContext())
        ffmpeg.loadBinary(object : FFmpegLoadBinaryResponseHandler {
            override fun onFinish() {
                Log.d("FFmpeg", "onFinish")
            }

            override fun onSuccess() {
                Log.d("FFmpeg", "onSuccess")
                //val changePlaybacSpeedCcommand = arrayOf("-i", input, "-vf", "\"setpts=$scale*PTS\"", output)
                //val compressVideoCommand = arrayOf("-i", input, "-vf", "scale=$w:$h", "-c:v", "libx264", "-preset", "veryslow", "-crf", "24", output)
                //val removeAudioCommand = arrayOf("-i", input, "-an", output)
                //val cropCommand = arrayOf("-i", input, "-filter:v", "crop=$w:$h:$x:$y", "-threads", "5", "-preset", "ultrafast", "-strict", "-2", "-c:a", "copy", output)
                val trimCommand =
                    arrayOf("-y", "-i", input, "-ss", startPos, "-to", endPos, "-c", "copy", output)

                try {
                    ffmpeg.execute(trimCommand, object : ExecuteBinaryResponseHandler() {
                        override fun onSuccess(message: String?) {
                            super.onSuccess(message)
                            Log.d(TAG, "onSuccess: " + message!!)
                        }

                        override fun onProgress(message: String?) {
                            super.onProgress(message)
                            Log.d(TAG, "onProgress: " + message!!)
                        }

                        override fun onFailure(message: String?) {
                            super.onFailure(message)
                            Log.e(TAG, "onFailure: " + message!!)
                        }

                        override fun onStart() {
                            super.onStart()
                            Log.d(TAG, "onStart")
                        }

                        override fun onFinish() {
                            super.onFinish()
                            Log.d(TAG, "onFinish")
                        }
                    })
                } catch (e: FFmpegCommandAlreadyRunningException) {
                    Log.e("FFmpeg", "FFmpeg runs already")
                }
            }

            override fun onFailure() {
                Log.e("FFmpeg", "onFailure")
            }

            override fun onStart() {
            }
        })


    }*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_selected, container, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Data().mapContentUrisToMediaItems()

        // Initialize
        viewPager = view.findViewById(R.id.viewPager)
        miniPreviewRecyclerView = view.findViewById(R.id.miniPreviewRecyclerView)
        val addButton = view.findViewById<Button>(R.id.addButton)
        val removeButton = view.findViewById<Button>(R.id.removeButton)

        // Register a callback to be invoked when the ViewPager2 changes its current item
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentIndex = position // Update the current index
                val centeredPosition = calculateCenteredPosition()
                scrollMiniPreviewToCenteredPosition(centeredPosition)
            }
        })

        contentUris.observe(viewLifecycleOwner) {
            editViewPagerAdapter.notifyDataSetChanged()
            miniPreviewAdapter.notifyDataSetChanged()
        }

        // Set up the ViewPager2 with the editViewPagerAdapter
        editViewPagerAdapter = contentUris.value?.let { EditViewPagerAdapter(it) }!!
        viewPager.adapter = editViewPagerAdapter

        // Set up the mini preview RecyclerView with the miniPreviewAdapter
        miniPreviewAdapter = MiniPreviewAdapter(mediaItems)

        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        miniPreviewRecyclerView.layoutManager = layoutManager
        miniPreviewRecyclerView.adapter = miniPreviewAdapter

        // Add an item click listener to the mini preview RecyclerView
        miniPreviewAdapter.setOnItemClickListener { position ->
            // Set the ViewPager2's current item based on the selected position
            viewPager.currentItem = position
        }

        addButton.setOnClickListener {
            pickMediaContent()
            editViewPagerAdapter.notifyDataSetChanged()
        }

        removeButton.setOnClickListener {
            if (contentUris.value!!.isNotEmpty()) {
                contentUris.value!!.removeAt(currentIndex)
                editViewPagerAdapter.notifyDataSetChanged()
                mediaItems.removeAt(currentIndex)
                miniPreviewAdapter.notifyDataSetChanged()
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

    private fun callViewPagerAdapter() {
        editViewPagerAdapter = contentUris.value?.let { EditViewPagerAdapter(it) }!!
        viewPager.adapter = editViewPagerAdapter
    }

    private fun calculateCenteredPosition(): Int {
        val currentContentUri =
            contentUris.value?.get(currentIndex) // Current content URI in ViewPager
        for ((index, pair) in mediaItems.withIndex()) {
            if (pair.first == currentContentUri) {
                return index
            }
        }
        return RecyclerView.NO_POSITION // Not found
    }

    private fun scrollMiniPreviewToCenteredPosition(centeredPosition: Int) {
        if (centeredPosition != RecyclerView.NO_POSITION) {
            // Scroll to the centered position with a smooth scroll effect
            miniPreviewRecyclerView.smoothScrollToPosition(centeredPosition)
        }
    }
}