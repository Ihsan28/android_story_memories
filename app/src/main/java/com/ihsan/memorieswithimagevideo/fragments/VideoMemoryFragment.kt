package com.ihsan.memorieswithimagevideo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import androidx.fragment.app.Fragment
import com.ihsan.memorieswithimagevideo.R
import com.ihsan.memorieswithimagevideo.data.Data.Companion.contentUris
import com.ihsan.memorieswithimagevideo.data.Data.Companion.currentIndex

class VideoMemoryFragment : Fragment() {
    private lateinit var videoView: VideoView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video_memory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        videoView = view.findViewById(R.id.videoView)
        val uri = contentUris.value!![currentIndex]
        val ext = uri.toString().substring(uri.toString().lastIndexOf(".") + 1)
        //if (listOfAllowedVideoFormat.contains(ext)) {
        videoView.setVideoURI(uri)
        videoView.setOnPreparedListener {
            it.start()
            it.isLooping = true
        }
        //}


    }
}