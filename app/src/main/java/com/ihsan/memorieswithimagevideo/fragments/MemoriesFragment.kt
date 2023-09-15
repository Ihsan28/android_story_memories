package com.ihsan.memorieswithimagevideo.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.viewpager2.widget.ViewPager2
import com.ihsan.memorieswithimagevideo.R
import com.ihsan.memorieswithimagevideo.Utils.CustomPageTransformer
import com.ihsan.memorieswithimagevideo.adapter.ViewPagerAdapter
import com.ihsan.memorieswithimagevideo.data.Data
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MemoriesFragment : Fragment() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private lateinit var viewPager2: ViewPager2
    private lateinit var pickImageButton: Button
    private lateinit var editButton: Button
    private lateinit var exportButton: Button

    //pick image launcher contract for image picker intent
    private val pickImageContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val clipData = result.data!!.clipData
                Data.contentUris.clear()
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val imageUri = clipData.getItemAt(i).uri
                        Data.contentUris.add(imageUri)
                    }
                    coroutineScope.launch {
                        callViewPagerAdapter()
                    }
                } else {
                    val imageUri = result.data!!.data
                    if (imageUri != null) {
                        Data.contentUris.clear()
                        Data.contentUris.add(imageUri)
                    }
                    coroutineScope.launch {
                        callViewPagerAdapter()
                    }
                }

                // Ensure there are at least 2 images and at most 5 images
                if (Data.contentUris.size > 5) {
                    Data.contentUris = Data.contentUris.subList(0, 5)
                } else if (Data.contentUris.size < 2) {
                    // Handle the case when less than 2 images are selected
                    // Show a message or take appropriate action
                    return@registerForActivityResult
                }
                Data.currentIndex = 0
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
        viewPager2.setPageTransformer(CustomPageTransformer())

        editButton = view.findViewById(R.id.edit)
        exportButton = view.findViewById(R.id.export)
        pickImageButton = view.findViewById(R.id.pickImageButton)

        pickImageButton.setOnClickListener {
            pickImages()
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

    private fun pickImages() {
        val pickImageIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickImageIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        pickImageContract.launch(pickImageIntent)
    }

    private fun callViewPagerAdapter() {
        if (Data.contentUris.isNotEmpty()) {
            val tabMatchAdapter =
                ViewPagerAdapter(childFragmentManager, lifecycle)
            viewPager2.adapter = tabMatchAdapter
        }
    }
}