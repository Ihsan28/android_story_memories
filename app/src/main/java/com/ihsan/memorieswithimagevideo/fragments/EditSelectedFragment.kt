package com.ihsan.memorieswithimagevideo.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
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

class EditSelectedFragment : Fragment() {
    private lateinit var viewPager: ViewPager2
    private lateinit var editViewPagerAdapter: EditViewPagerAdapter
    private lateinit var miniPreviewRecyclerView: RecyclerView
    private lateinit var miniPreviewAdapter: MiniPreviewAdapter

    //pick image launcher contract for image picker intent
    @SuppressLint("NotifyDataSetChanged")
    private val pickImageContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val clipData = result.data!!.clipData

                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val imageUri = clipData.getItemAt(i).uri
                        contentUris.value?.add(imageUri)
                    }

                    callViewPagerAdapter()

                } else {
                    val imageUri = result.data!!.data
                    if (imageUri != null) {
                        contentUris.value?.clear()
                        contentUris.value?.add(imageUri)
                    }
                    callViewPagerAdapter()
                }

                currentIndex = 0
            }
        }

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
            pickImages()
            editViewPagerAdapter.notifyDataSetChanged()
        }

        removeButton.setOnClickListener {
            if (contentUris.value!!.isNotEmpty()) {
                contentUris.value!!.removeAt(currentIndex)
                editViewPagerAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun pickImages() {
        val pickImageIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickImageIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        pickImageContract.launch(pickImageIntent)
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