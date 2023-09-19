package com.ihsan.memorieswithimagevideo.fragments

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.ihsan.memorieswithimagevideo.R
import com.ihsan.memorieswithimagevideo.Utils.CustomPageTransformer
import com.ihsan.memorieswithimagevideo.adapter.ViewPagerAdapter
import com.ihsan.memorieswithimagevideo.data.Data.Companion.contentUris
import com.ihsan.memorieswithimagevideo.data.Data.Companion.currentIndex

class MemoriesFragment : Fragment() {
    private lateinit var viewPager2: ViewPager2
    private lateinit var pickImageButton: Button
    private lateinit var editButton: Button
    private lateinit var exportButton: Button
    private lateinit var mediaPlayer: MediaPlayer

    //pick image launcher contract for image picker intent
    private val pickMediaContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val clipData = result.data!!.clipData
                contentUris.value!!.clear()
                mediaPlayer.start()
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val mediaUri = clipData.getItemAt(i).uri
                        contentUris.value!!.add(mediaUri)
                    }
                    callViewPagerAdapter()
                } else {
                    val mediaUri = result.data!!.data
                    if (mediaUri != null) {
                        contentUris.value!!.clear()
                        contentUris.value!!.add(mediaUri)
                        callViewPagerAdapter()
                    }
                }
                currentIndex = 0
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_memories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager2 = view.findViewById(R.id.viewPager2)
        viewPager2.offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
        viewPager2.setPageTransformer(CustomPageTransformer())

        editButton = view.findViewById(R.id.edit)
        exportButton = view.findViewById(R.id.export)
        pickImageButton = view.findViewById(R.id.pickImageButton)

        // Initialize MediaPlayer
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.aylex)
        mediaPlayer.isLooping = true

        mediaPlayer.setOnCompletionListener {
            // Animation has ended, stop the audio
            mediaPlayer.stop()
        }

        if (contentUris.value!!.isNotEmpty()) {
            currentIndex = 0
            mediaPlayer.start()
            callViewPagerAdapter()
        }

        pickImageButton.setOnClickListener {
            pickMediaContent()
        }

        editButton.setOnClickListener {
            val fragment = EditSelectedFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        exportButton.setOnClickListener {
            val fragment = VideoMemoryFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
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
        if (contentUris.value!!.isNotEmpty()) {
            val tabMatchAdapter =
                ViewPagerAdapter(childFragmentManager, lifecycle)
            viewPager2.adapter = tabMatchAdapter
        }
    }
}