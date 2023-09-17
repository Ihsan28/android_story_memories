package com.ihsan.memorieswithimagevideo.fragments

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.ihsan.memorieswithimagevideo.R
import com.ihsan.memorieswithimagevideo.adapter.GalleryAdapter
import com.ihsan.memorieswithimagevideo.adapter.MiniPreviewAdapter
import com.ihsan.memorieswithimagevideo.data.Data.Companion.contentUris
import com.ihsan.memorieswithimagevideo.data.Data.Companion.currentIndex
import com.ihsan.memorieswithimagevideo.data.MediaType

class EditSelectedFragment : Fragment() {
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: GalleryAdapter
    private lateinit var miniPreviewRecyclerView: RecyclerView
    val mediaItems=contentUris.map {
        val ext=it.toString().substring(it.toString().lastIndexOf(".")+1)
        if(ext=="mp4"){
            Pair(it, MediaType.VIDEO)
        }else{
            Pair(it,MediaType.IMAGE)
        }
    }
    var newUri: Uri = Uri.parse("")
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

        viewPager = view.findViewById(R.id.viewPager)
        adapter = GalleryAdapter(contentUris)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentIndex = position // Update the current index
                val centeredPosition = calculateCenteredPosition()
                scrollMiniPreviewToCenteredPosition(centeredPosition)
            }
        })


        val addButton = view.findViewById<Button>(R.id.addButton)
        val removeButton = view.findViewById<Button>(R.id.removeButton)

        miniPreviewRecyclerView = view.findViewById(R.id.miniPreviewRecyclerView)

        val miniPreviewAdapter = MiniPreviewAdapter(mediaItems)

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
            // Implement logic to add a new item (image or video) to contentUris
            // Then, notify the adapter to update the view
            contentUris.add(newUri)
            adapter.notifyDataSetChanged()
        }

        removeButton.setOnClickListener {
            // Implement logic to remove an item from contentUris
            // Then, notify the adapter to update the view
            if (contentUris.isNotEmpty()) {
                contentUris.removeAt(currentIndex)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun calculateCenteredPosition(): Int {
        val currentContentUri = contentUris[currentIndex] // Current content URI in ViewPager
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