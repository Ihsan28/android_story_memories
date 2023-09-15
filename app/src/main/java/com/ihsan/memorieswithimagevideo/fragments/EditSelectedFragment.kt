package com.ihsan.memorieswithimagevideo.fragments

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
    var newUri: Uri = Uri.parse("")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_selected, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Initialize contentUris with some URIs (images and videos)
        // ...

        viewPager = view.findViewById(R.id.viewPager)
        adapter = GalleryAdapter(contentUris)
        viewPager.adapter = adapter

        val addButton = view.findViewById<Button>(R.id.addButton)
        val removeButton = view.findViewById<Button>(R.id.removeButton)

        miniPreviewRecyclerView = view.findViewById(R.id.miniPreviewRecyclerView)

        val miniPreviewAdapter = MiniPreviewAdapter(contentUris.map {
            val ext=it.toString().substring(it.toString().lastIndexOf(".")+1)
            if(ext=="mp4"){
                Pair(it, MediaType.VIDEO)
            }else{
                Pair(it,MediaType.IMAGE)
            }
        })
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
}