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
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
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
import com.ihsan.memorieswithimagevideo.data.MediaType

private const val TAG = "EditSelectedFragment"

class EditSelectedFragment : Fragment() {
    private lateinit var viewPager: ViewPager2
    private lateinit var editViewPagerAdapter: EditViewPagerAdapter
    private lateinit var miniPreviewRecyclerView: RecyclerView
    private lateinit var miniPreviewAdapter: MiniPreviewAdapter

    private lateinit var pickMediaContract: ActivityResultLauncher<Intent>
    private var currentContentUri = contentUris.value?.get(currentIndex)


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
            }
    */

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

        // Initialize
        viewPager = view.findViewById(R.id.viewPager)
        miniPreviewRecyclerView = view.findViewById(R.id.miniPreviewRecyclerView)
        val addButton = view.findViewById<Button>(R.id.addButton)
        val removeButton = view.findViewById<Button>(R.id.removeButton)
        val editButton = view.findViewById<Button>(R.id.editButton)
        Data().mapContentUrisToMediaItems()

        pickMediaContract = pickMediaContract()

        //initialize()

        // Register a callback to be invoked when the ViewPager2 changes its current item
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentIndex = position // Update the current index
                //val centeredPosition = calculateCenteredPosition()
                val centeredPosition = currentIndex
                scrollMiniPreviewToCenteredPosition(centeredPosition)
            }
        })

        contentUris.observe(viewLifecycleOwner) {
            editViewPagerAdapter.notifyDataSetChanged()
            miniPreviewAdapter.notifyDataSetChanged()
        }

        // Set up the ViewPager2 with the editViewPagerAdapter
        editViewPagerAdapter = contentUris.value?.let { EditViewPagerAdapter(it) }!!
        //editViewPagerAdapter= EditViewPagerAdapter(mediaItems.map { it.first })
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
            currentIndex = position
            scrollMiniPreviewToCenteredPosition(position)
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

        editButton.setOnClickListener {
            //navigate to edit fragment
            Log.d(TAG, "onViewCreated: ${mediaItems[currentIndex]}")

            if (mediaItems[currentIndex].second == MediaType.VIDEO) {
                val action =
                    EditSelectedFragmentDirections.actionEditSelectedFragmentToVideoEditingFragment(
                        currentIndex
                    )
                view.findNavController().navigate(action)
            } else if (mediaItems[currentIndex].second == MediaType.IMAGE) {
                Toast.makeText(
                    requireContext(),
                    "Not available ${mediaItems[currentIndex].second}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d(TAG, "onViewCreated: ${mediaItems[currentIndex].second}")
            }
        }
    }
    /*
        private fun initialize() {
            Toast.makeText(requireContext(), "init", Toast.LENGTH_SHORT).show()
            val ffmpeg = FFmpeg.getInstance(requireContext())
            try {
                ffmpeg.loadBinary(object : LoadBinaryResponseHandler() {
                    override fun onFinish() {
                        super.onFinish()
                        Toast.makeText(requireContext(), "Finish", Toast.LENGTH_SHORT).show()

                    }

                    override fun onSuccess() {
                        super.onSuccess()
                        Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()

                    }

                    override fun onFailure() {
                        super.onFailure()
                        Toast.makeText(requireContext(), "Failure", Toast.LENGTH_SHORT).show()
                    }

                    override fun onStart() {
                        super.onStart()
                        Toast.makeText(requireContext(), "Start", Toast.LENGTH_SHORT).show()
                    }
                })
            } catch (e: FFmpegNotSupportedException) {
                Log.e("FFmpeg", "Your device does not support FFmpeg")
            }
        }

        private fun trimVideo(input:String, output:String, startPos:String, endPos:String){
            val ffmpeg = FFmpeg.getInstance(requireContext())
            ffmpeg.loadBinary(object : FFmpegLoadBinaryResponseHandler {
                override fun onFinish() {
                    Log.d("FFmpeg", "onFinish")
                }

                override fun onSuccess() {
                    Log.d("FFmpeg", "onSuccess")
                    val command = arrayOf("-y", "-i", input, "-ss", startPos, "-to", endPos, "-c", "copy", output)

                    try {
                            ffmpeg.execute(command, object : ExecuteBinaryResponseHandler() {
                                override fun onSuccess(message: String?) {
                                    super.onSuccess(message)
                                    Log.d(TAG, "onSuccess: " + message!!)
                                    Toast.makeText(requireContext(), "onSuccess: $message", Toast.LENGTH_SHORT).show()
                                }

                                override fun onProgress(message: String?) {
                                    super.onProgress(message)
                                    Log.d(TAG, "onProgress: " + message!!)
                                    Toast.makeText(requireContext(), "onProgress: $message", Toast.LENGTH_SHORT).show()

                                }

                                override fun onFailure(message: String?) {
                                    super.onFailure(message)
                                    Log.e(TAG, "onFailure: " + message!!)
                                    Toast.makeText(requireContext(), "onFailure: $message", Toast.LENGTH_SHORT).show()

                                }

                                override fun onStart() {
                                    super.onStart()
                                    Log.d(TAG, "onStart")
                                    Toast.makeText(requireContext(), "onStart", Toast.LENGTH_SHORT).show()
                                }

                                override fun onFinish() {
                                    super.onFinish()
                                    Log.d(TAG, "onFinish")
                                    Toast.makeText(requireContext(), "onFinish", Toast.LENGTH_SHORT).show()
                                }
                            })
                        } catch (e: FFmpegCommandAlreadyRunningException) {
                            Log.e("FFmpeg", "FFmpeg runs already")
                        Toast.makeText(requireContext(), "exception", Toast.LENGTH_SHORT).show()
                        }
                }

                override fun onFailure() {
                    Log.e("FFmpeg", "onFailure")
                }

                override fun onStart() {
                }
            })
        }
    */

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

    private fun pickMediaContract(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val clipData = result.data!!.clipData
                //contentUris.value!!.clear()

                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val mediaUri = clipData.getItemAt(i).uri
                        contentUris.value!!.add(mediaUri)
                    }
                } else {
                    val mediaUri = result.data!!.data
                    if (mediaUri != null) {
                        contentUris.value!!.add(mediaUri)
                    }
                }
                //update media items
                Data().mapContentUrisToMediaItems()
                currentIndex = 0
                callViewPagerAdapter()
            }
        }
    }

    private fun callViewPagerAdapter() {
        editViewPagerAdapter = contentUris.value?.let { EditViewPagerAdapter(it) }!!
        viewPager.adapter = editViewPagerAdapter
    }

    private fun calculateCenteredPosition(): Int {
        currentContentUri =
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